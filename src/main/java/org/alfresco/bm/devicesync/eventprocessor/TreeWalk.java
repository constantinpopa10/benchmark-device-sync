package org.alfresco.bm.devicesync.eventprocessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.data.GetChildrenData;
import org.alfresco.bm.devicesync.data.TreeWalkData;
import org.alfresco.bm.devicesync.util.CMISSessionFactory;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class TreeWalk extends AbstractEventProcessor
{
    private CMISSessionFactory cmisSessionFactory;
    private boolean splitIntoEvents;

    public TreeWalk(CMISSessionFactory cmisSessionFactory,
            boolean splitIntoEvents)
    {
        this.cmisSessionFactory = cmisSessionFactory;
        this.splitIntoEvents = splitIntoEvents;
    }

    private void treeWalk(Folder folder, TreeWalkData treeWalkData)
            throws IOException
    {
        treeWalkData.incrementMaxFolderDepth();

        ItemIterable<CmisObject> children = folder.getChildren();

        int numFolders = 0;
        int numDocuments = 0;

        // We have to iterate using paging
        List<Folder> childFolders = new LinkedList<>();
        long skip = 0L;
        ItemIterable<CmisObject> pageOfChildren = children.skipTo(skip);
        while (pageOfChildren.getPageNumItems() > 0L)
        {
            for (CmisObject child : pageOfChildren)
            {
                skip++;

                ObjectType childType = child.getType();
                String nodeType = childType.getId();
                if (nodeType.equals("cmis:folder"))
                {
                    numFolders++;
                    Folder childFolder = (Folder) child;
                    childFolders.add(childFolder);
                }
                else if (nodeType.equals("cmis:document"))
                {
                    Document document = (Document) child;
                    ContentStream stream = document.getContentStream();
                    InputStream in = stream.getStream();
                    byte[] buf = new byte[8092];
                    int contentSize = 0;
                    int c = -1;
                    do
                    {
                        c = in.read(buf);
                        if (c != -1)
                        {
                            contentSize += c;
                        }
                    }
                    while (c != -1);

                    treeWalkData.updateMaxContentSize(contentSize);
                    treeWalkData.updateMinContentSize(contentSize);
                    treeWalkData.incrementTotalContentSize(contentSize);

                    numDocuments++;
                }
            }
            // Get the next page of children
            pageOfChildren = children.skipTo(skip);
        }

        treeWalkData.incrementNumDocuments(numDocuments);
        treeWalkData.incrementNumFolders(numFolders);

        for (Folder childFolder : childFolders)
        {
            treeWalk(childFolder, treeWalkData);
        }
    }

    private void treeWalk(Session session, TreeWalkData treeWalkData,
            List<Event> nextEvents) throws IOException
    {
        String siteId = treeWalkData.getSiteId();
        StringBuilder sb = new StringBuilder("/Sites/");
        sb.append(siteId);
        sb.append("/documentLibrary");
        String path = sb.toString();
        Folder folder = (Folder) session.getObjectByPath(path);

        if (splitIntoEvents)
        {
            String folderId = folder.getId();
            GetChildrenData getChildrenData = new GetChildrenData(folderId, 0,
                    0, treeWalkData.getUsername(), treeWalkData.getSiteId(), 0,
                    0, 0, 0, 0, 0);
            Event event = new Event("treeWalkGetChildren",
                    System.currentTimeMillis(), getChildrenData.toDBObject());
            nextEvents.add(event);
        }
        else
        {
            treeWalk(folder, treeWalkData);
        }
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
        super.suspendTimer();

        DBObject dbObject = (DBObject) event.getData();
        TreeWalkData treeWalkData = TreeWalkData.fromDBObject(dbObject);
        String username = treeWalkData.getUsername();
        Session session = cmisSessionFactory.getCMISSession(username);

        List<Event> nextEvents = new LinkedList<Event>();

        try
        {
            super.resumeTimer();

            treeWalk(session, treeWalkData, nextEvents);

            super.suspendTimer();

            return new EventResult(treeWalkData.toDBObject(), nextEvents, true);
        }
        catch (Exception e)
        {
            logger.error("Exception occurred during event processing", e);
            throw e;
        }
    }
}

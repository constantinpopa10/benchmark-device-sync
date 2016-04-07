package org.alfresco.bm.devicesync.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.data.GetChildrenData;
import org.alfresco.bm.devicesync.data.TreeWalkData;
import org.alfresco.bm.event.Event;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class TreeWalkHelper
{
    private static Log logger = LogFactory.getLog(TreeWalkHelper.class);

    private CMISSessionFactory cmisSessionFactory;
    private boolean splitIntoEvents;

    public TreeWalkHelper(CMISSessionFactory cmisSessionFactory, boolean splitIntoEvents)
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
                    Folder childFolder = (Folder) child;

                    logger.debug("Processing folder " + childFolder.getPath() + ", numFolders " + numFolders);

                    childFolders.add(childFolder);

                    numFolders++;

                    logger.debug("Processed folder " + childFolder.getPath() + ", numFolders " + numFolders);
                }
                else if (nodeType.equals("cmis:document"))
                {
                    Document document = (Document) child;

                    logger.debug("Processing document " + document.getPaths() + ", numDocuments " + numDocuments);

                    long start = System.currentTimeMillis();

                    int contentSize = 0;

                    ContentStream stream = document.getContentStream();
                    if(stream != null)
                    {
                        InputStream in = stream.getStream();
                        byte[] buf = new byte[8092];
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
                    }

                    long end = System.currentTimeMillis();

                    numDocuments++;

                    logger.debug("Processed document " + document.getPaths() + ", contentSize = " + contentSize
                            + " in " + (end - start)
                            + "ms, numDocuments " + numDocuments);
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

    private Folder getFolder(Session session, TreeWalkData treeWalkData)
    {
        Folder folder = null;

        String objectId = treeWalkData.getObjectId();
        if(objectId != null)
        {
            folder = (Folder) session.getObject(objectId);
        }
        else
        {
            String path = treeWalkData.getPath();
            folder = (Folder) session.getObjectByPath(path);
        }

        return folder;
    }

    public void treeWalk(TreeWalkData treeWalkData, List<Event> nextEvents) throws IOException
    {
        Session session = cmisSessionFactory.getCMISSession(treeWalkData.getUsername());

        Folder folder = getFolder(session, treeWalkData);

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

}

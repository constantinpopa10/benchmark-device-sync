package org.alfresco.bm.devicesync.eventprocessor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.data.GetChildrenData;
import org.alfresco.bm.devicesync.data.GetDocumentData;
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

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class TreeWalkGetChildren extends AbstractEventProcessor
{
    private CMISSessionFactory cmisSessionFactory;

    public TreeWalkGetChildren(CMISSessionFactory cmisSessionFactory)
    {
        this.cmisSessionFactory = cmisSessionFactory;
    }

    private void getChildren(Folder folder, GetChildrenData getChildrenData,
            List<Event> nextEvents) throws IOException
    {
        getChildrenData.incrementMaxFolderDepth();
        getChildrenData.incrementNumFolders(1);

        ItemIterable<CmisObject> children = folder.getChildren();

        int numFoldersFetched = 0;
        int numDocumentsFetched = 0;

        // We have to iterate using paging
        long skip = 0L;
        ItemIterable<CmisObject> pageOfChildren = children.skipTo(skip);
        while (pageOfChildren.getPageNumItems() > 0L)
        {
            for (CmisObject child : pageOfChildren)
            {
                skip++;
                numFoldersFetched++;

                ObjectType childType = child.getType();
                String nodeType = childType.getId();
                if (nodeType.equals("cmis:folder"))
                {
                    Folder childFolder = (Folder) child;
                    String folderId = childFolder.getId();
                    getChildrenData.setObjectId(folderId);
                    GetChildrenData getChildrenData1 = new GetChildrenData(
                            folderId, getChildrenData.getNumFolders(),
                            getChildrenData.getNumDocuments(),
                            getChildrenData.getUsername(),
                            getChildrenData.getSiteId(),
                            getChildrenData.getTotalContentSize(),
                            getChildrenData.getMaxContentSize(),
                            getChildrenData.getMinContentSize(),
                            getChildrenData.getMaxFolderDepth(), 0, 0);
                    Event event = new Event("treeWalkGetChildren",
                            System.currentTimeMillis(),
                            getChildrenData1.toDBObject());
                    nextEvents.add(event);
                }
                else if (nodeType.equals("cmis:document"))
                {
                    numDocumentsFetched++;

                    Document document = (Document) child;
                    String documentId = document.getId();
                    GetDocumentData getContentData = new GetDocumentData(
                            documentId, getChildrenData.getNumFolders(),
                            getChildrenData.getNumDocuments(),
                            getChildrenData.getUsername(),
                            getChildrenData.getSiteId(),
                            getChildrenData.getTotalContentSize(),
                            getChildrenData.getMaxContentSize(),
                            getChildrenData.getMinContentSize(),
                            getChildrenData.getMaxFolderDepth());
                    Event event = new Event("treeWalkGetDocument",
                            System.currentTimeMillis(),
                            getContentData.toDBObject());
                    nextEvents.add(event);
                }
            }
            // Get the next page of children
            pageOfChildren = children.skipTo(skip);
        }

        getChildrenData.setNumDocumentsFetched(numDocumentsFetched);
        getChildrenData.setNumFoldersFetched(numFoldersFetched);
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
        super.suspendTimer();

        DBObject dbObject = (DBObject) event.getData();
        GetChildrenData getChildrenData = GetChildrenData.fromDBObject(dbObject);
        String username = getChildrenData.getUsername();
        Session session = cmisSessionFactory.getCMISSession(username);
        String folderId = getChildrenData.getObjectId();

        List<Event> nextEvents = new LinkedList<Event>();

        try
        {
            Folder folder = (Folder) session.getObject(folderId);

            super.resumeTimer();

            getChildren(folder, getChildrenData, nextEvents);

            super.suspendTimer();

            return new EventResult(getChildrenData.toDBObject(), nextEvents,
                    true);
        }
        catch (Exception e)
        {
            logger.error("Exception occurred during event processing", e);
            throw e;
        }
    }
}

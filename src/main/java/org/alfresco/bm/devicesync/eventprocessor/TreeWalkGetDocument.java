package org.alfresco.bm.devicesync.eventprocessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.data.GetDocumentData;
import org.alfresco.bm.devicesync.util.CMISSessionFactory;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class TreeWalkGetDocument extends AbstractEventProcessor
{
    private CMISSessionFactory cmisSessionFactory;

    public TreeWalkGetDocument(CMISSessionFactory cmisSessionFactory)
    {
        this.cmisSessionFactory = cmisSessionFactory;
    }

    private void getDocument(Document document,
            GetDocumentData getDocumentData, List<Event> nextEvents)
            throws IOException
    {
        getDocumentData.incrementNumDocuments(1);

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

        getDocumentData.updateMaxContentSize(contentSize);
        getDocumentData.updateMinContentSize(contentSize);
        getDocumentData.incrementTotalContentSize(contentSize);
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
        super.suspendTimer();

        DBObject dbObject = (DBObject) event.getData();
        GetDocumentData getContentDataData = GetDocumentData
                .fromDBObject(dbObject);
        String username = getContentDataData.getUsername();
        Session session = cmisSessionFactory.getCMISSession(username);
        String documentId = getContentDataData.getDocumentId();

        List<Event> nextEvents = new LinkedList<Event>();

        try
        {
            Document document = (Document) session.getObject(documentId);

            super.resumeTimer();

            getDocument(document, getContentDataData, nextEvents);

            super.suspendTimer();

            return new EventResult(getContentDataData.toDBObject(), nextEvents,
                    true);
        }
        catch (Exception e)
        {
            logger.error("Exception occurred during event processing", e);
            throw e;
        }
    }
}

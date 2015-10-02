package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.data.CollectStatsBatchData;
import org.alfresco.bm.devicesync.data.SubscriberBatchData;
import org.alfresco.bm.devicesync.data.SubscriptionBatchData;
import org.alfresco.bm.devicesync.data.SyncBatchData;
import org.alfresco.bm.devicesync.data.TreeWalkBatchData;
import org.alfresco.bm.devicesync.data.UploadFileBatchData;
import org.alfresco.bm.devicesync.util.Util;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class BatchProcessor extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(BatchProcessor.class);

    private final String eventNameSubscribersBatch;
    private final String eventNameSubscriptionsBatch;
    private final String eventNameSyncBatch;
    private final String eventNameCollectStatsBatch;
    private final String eventNameUploadFileBatch;
    private final String eventNameUploadAndSyncBatch;
    private final String eventNameTreeWalkBatch;

    /**
     * Constructor 
     * 
     * @param siteDataService_p             Site Data service to retrieve site information from Mongo
     * @param userDataService_p             User Data service to retrieve user information from Mongo
     * @param desktopSyncClientRegistry_p   Registry to create the clients 
     * @param numberOfClients_p             Number of clients to create
     * @param nextEventId_p                 ID of the next event
     */
    public BatchProcessor(String eventNameSubscribersBatch, String eventNameSubscriptionsBatch,
    		String eventNameSyncBatch, String eventNameCollectStatsBatch, String eventNameUploadFileBatch,
    		String eventNameUploadAndSyncBatch, String eventNameTreeWalkBatch)
    {
    	this.eventNameSubscribersBatch = eventNameSubscribersBatch;
        this.eventNameSubscriptionsBatch = eventNameSubscriptionsBatch;
        this.eventNameSyncBatch = eventNameSyncBatch;
        this.eventNameCollectStatsBatch = eventNameCollectStatsBatch;
        this.eventNameUploadFileBatch = eventNameUploadFileBatch;
        this.eventNameUploadAndSyncBatch = eventNameUploadAndSyncBatch;
        this.eventNameTreeWalkBatch = eventNameTreeWalkBatch;

        // validate arguments
        Util.checkArgumentNotNull(eventNameSubscribersBatch, "eventNameSubscribersBatch");
        Util.checkArgumentNotNull(eventNameSubscriptionsBatch, "eventNameSubscriptionsBatch");
        Util.checkArgumentNotNull(eventNameSyncBatch, "eventNameSyncBatch");
        Util.checkArgumentNotNull(eventNameUploadAndSyncBatch, "eventNameUploadAndSyncBatch");
        Util.checkArgumentNotNull(eventNameCollectStatsBatch, "eventNameCollectStatsBatch");
        Util.checkArgumentNotNull(eventNameTreeWalkBatch, "eventNameTreeWalkBatch");
    }

    @Override
    protected EventResult processEvent(Event event_p) throws Exception
    {
        try
        {
            List<Event> nextEvents = new LinkedList<>();

            long time = System.currentTimeMillis();

            {
	            SubscriberBatchData data = new SubscriberBatchData(0);
	            Event nextEvent = new Event(eventNameSubscribersBatch, time, data.toDBObject());
	            nextEvents.add(nextEvent);
            }

            {
	            SubscriptionBatchData data = new SubscriptionBatchData(0);
	            Event nextEvent = new Event(eventNameSubscriptionsBatch, time, data.toDBObject());
	            nextEvents.add(nextEvent);
            }

            {
	            SyncBatchData data = new SyncBatchData(0);
	            Event nextEvent = new Event(eventNameSyncBatch, time, data.toDBObject());
	            nextEvents.add(nextEvent);
            }

            {
	            SyncBatchData data = new SyncBatchData(0);
	            Event nextEvent = new Event(eventNameUploadAndSyncBatch, time, data.toDBObject());
	            nextEvents.add(nextEvent);
            }

            {
	            UploadFileBatchData data = new UploadFileBatchData(0);
	            Event nextEvent = new Event(eventNameUploadFileBatch, time, data.toDBObject());
	            nextEvents.add(nextEvent);
            }

            {
	            CollectStatsBatchData data = new CollectStatsBatchData(0);
	            Event nextEvent = new Event(eventNameCollectStatsBatch, time, data.toDBObject());
	            nextEvents.add(nextEvent);
            }

            {
                TreeWalkBatchData data = new TreeWalkBatchData(0);
                Event nextEvent = new Event(eventNameTreeWalkBatch, time, data.toDBObject());
                nextEvents.add(nextEvent);
            }

            String msg = "Created batch";

            EventResult result = new EventResult(msg, nextEvents);

            if (logger.isDebugEnabled())
            {
                logger.debug(msg);
            }

            return result;
        }
        catch (Exception e)
        {
            logger.error("Error creating subscribers. ", e);
            throw e;
        }
    }

}

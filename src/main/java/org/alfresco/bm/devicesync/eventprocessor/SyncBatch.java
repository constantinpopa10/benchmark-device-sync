package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.devicesync.data.SyncBatchData;
import org.alfresco.bm.devicesync.data.SyncData;
import org.alfresco.bm.devicesync.util.SiteSampleSelector;
import org.alfresco.bm.devicesync.util.Util;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.DBObject;

/**
 * Prepare as much DesktopSync client data as configured.
 * 
 * In this class the configured values are combined with a site in Alfresco (created by a former run of data load
 * benchmark test) and the user data of the site manager. The user data was created by the initial sign-up benchmark
 * test run.
 * 
 * @author sglover
 * @since 1.0
 */
public class SyncBatch extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(SyncBatch.class);

    private SubscriptionsService subscriptionsService;

    private final int batchSize;
    private final int numBatches;
    private final int waitTimeBetweenBatches; // ms

    private final SiteSampleSelector siteSampleSelector;

    private final String eventNameStartSync;

    /**
     * Constructor 
     * 
     * @param siteDataService_p             Site Data service to retrieve site information from Mongo
     * @param userDataService_p             User Data service to retrieve user information from Mongo
     * @param desktopSyncClientRegistry_p   Registry to create the clients 
     * @param numberOfClients_p             Number of clients to create
     * @param nextEventId_p                 ID of the next event
     */
    public SyncBatch(SubscriptionsService subscriptionsService, SiteSampleSelector siteSampleSelector,
    		int batchSize, int numBatches, int waitTimeBetweenBatches, String eventNameStartSync)
    {
    	this.subscriptionsService = subscriptionsService;
    	this.siteSampleSelector = siteSampleSelector;
    	this.batchSize = batchSize;
        this.numBatches = numBatches;
        this.eventNameStartSync = eventNameStartSync;
        this.waitTimeBetweenBatches = waitTimeBetweenBatches;

        // validate arguments
        Util.checkArgumentNotNull(subscriptionsService, "subscriptionsService");
    }


    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
    	DBObject dbObject = (DBObject)event.getData();
    	SyncBatchData syncBatchData = SyncBatchData.fromDBObject(dbObject);
    	int count = syncBatchData.getCount();
    	List<String> sites = syncBatchData.getSites();

        try
        {
        	String msg = null;
            List<Event> nextEvents = new LinkedList<Event>();

            long numSubscriptions = subscriptionsService.countSubscriptions(DataCreationState.Created);
            if(numSubscriptions == 0)
            {
	            msg = "No subscriptions, stopping.";
            }
            else if(count >= numBatches)
            {
            	msg = "Hit number of batches, stopping.";
            }
            else
            {
            	{
	            	for(int i = 0; i < batchSize; i++)
	            	{
	            		SubscriptionData subscriptionData = siteSampleSelector.getSubscription();

	            		SyncData syncData = new SyncData(subscriptionData.getSiteId(),
	            				subscriptionData.getUsername(),
	            				subscriptionData.getSubscriberId(),
	            				subscriptionData.getSubscriptionId(), null);
	
	                	Event nextEvent = new Event(eventNameStartSync, System.currentTimeMillis(), syncData.toDBObject());
	                	nextEvents.add(nextEvent);
	            	}
            	}

            	{
	            	long scheduledTime = System.currentTimeMillis() + waitTimeBetweenBatches;
	            	SyncBatchData newSyncBatchData = new SyncBatchData(count + 1, sites);
	            	Event nextEvent = new Event(event.getName(), scheduledTime, newSyncBatchData.toDBObject());
	            	nextEvents.add(nextEvent);
            	}

	            msg = "Prepared " + batchSize + " syncs";
            }

            EventResult result = new EventResult(msg, nextEvents);

            if (logger.isDebugEnabled())
            {
                logger.debug(msg);
            }

            return result;
        }
        catch (Exception e)
        {
            logger.error("Error preparing desktop sync clients. ", e);
            throw e;
        }
    }
}

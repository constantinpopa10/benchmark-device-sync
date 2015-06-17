package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.devicesync.util.Util;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public class PrepareSyncs extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(PrepareSyncs.class);

    private SubscriptionsService subscriptionsService;

    private final int numBatches;
    private final int batchSize;
    private final int waitTimeBetweenSyncs; // ms
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
    public PrepareSyncs(SubscriptionsService subscriptionsService, int numBatches, String eventNameStartSync,
    		int waitTimeBetweenSyncs, int batchSize)
    {
    	this.subscriptionsService = subscriptionsService;
        this.numBatches = numBatches;
        this.eventNameStartSync = eventNameStartSync;
        this.waitTimeBetweenSyncs = waitTimeBetweenSyncs;
        this.batchSize = batchSize;

        // validate arguments
        Util.checkArgumentNotNull(subscriptionsService, "subscriptionsService");
    }

    @Override
    protected EventResult processEvent(Event event_p) throws Exception
    {
        try
        {
        	String msg = null;
            List<Event> nextEvents = new LinkedList<Event>();

            long numSubscriptions = subscriptionsService.countSubscriptions(DataCreationState.Created);
            if(numSubscriptions == 0)
            {
	            msg = "No subscriptions, stopping.";
            }
            else
            {
            	long scheduledTime = System.currentTimeMillis();
            	for(int i = 0; i < numBatches; i++)
            	{
            		for(int j = 0; j < batchSize; j++)
            		{
	            		SubscriptionData subscriptionData = subscriptionsService.getRandomSubscription(null);
	
	                	Event event = new Event(eventNameStartSync, scheduledTime, subscriptionData.toDBObject());
	                	nextEvents.add(event);
            		}

            		scheduledTime += waitTimeBetweenSyncs;
            	}

	            msg = "Prepared " + numBatches*batchSize + " syncs in " + numBatches + " batches";
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

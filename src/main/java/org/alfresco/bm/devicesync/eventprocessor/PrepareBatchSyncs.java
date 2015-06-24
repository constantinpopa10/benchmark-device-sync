package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.dao.SyncsService;
import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.devicesync.data.SyncState;
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
public class PrepareBatchSyncs extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(PrepareBatchSyncs.class);

    private SubscriptionsService subscriptionsService;
    
    private SyncsService syncsService;

    private int maxSyncs;
    private String eventNameExecuteSyncs;

    /**
     * Constructor 
     * 
     * @param siteDataService_p             Site Data service to retrieve site information from Mongo
     * @param userDataService_p             User Data service to retrieve user information from Mongo
     * @param desktopSyncClientRegistry_p   Registry to create the clients 
     * @param numberOfClients_p             Number of clients to create
     * @param nextEventId_p                 ID of the next event
     */
    public PrepareBatchSyncs(SubscriptionsService subscriptionsService, SyncsService syncsService, int maxSyncs,
    		String eventNameExecuteSyncs)
    {
    	this.subscriptionsService = subscriptionsService;
    	this.syncsService = syncsService;
        this.maxSyncs = maxSyncs;
        this.eventNameExecuteSyncs = eventNameExecuteSyncs;

        // validate arguments
        Util.checkArgumentNotNull(subscriptionsService, "subscriptionsService");
        Util.checkArgumentNotNull(syncsService, "syncsService");
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
            else if(maxSyncs > numSubscriptions)
            {
	            msg = "Not enough subscriptions, stopping.";
            }
            else
            {
            	for(int i = 0; i < maxSyncs; i++)
            	{
            		SubscriptionData subscriptionData = subscriptionsService.getRandomSubscription(null);
            		syncsService.addSync(subscriptionData.getUsername(),
            				subscriptionData.getSubscriberId(),
            				subscriptionData.getSubscriptionId(), SyncState.NotScheduled);
            	}

//            	subscriptionsService.getSubscriptions(DataCreationState.Created, 0, maxSyncs);
//            	subscriptions.stream().forEach(subscriptionData -> {
//            		syncsService.addSync(subscriptionData.getUsername(),
//            				subscriptionData.getSubscriberId(),
//            				subscriptionData.getSubscriptionId(), SyncState.NotScheduled);
//            	});

            	Event event = new Event(eventNameExecuteSyncs, null);
            	nextEvents.add(event);
	            msg = "Prepared " + maxSyncs + " syncs and raising '" + event.getId()
	                    + "' event.";
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

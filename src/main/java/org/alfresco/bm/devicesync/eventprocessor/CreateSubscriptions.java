package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.devicesync.util.PublicApiFactory;
import org.alfresco.bm.devicesync.util.Util;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.social.alfresco.api.Alfresco;
import org.springframework.social.alfresco.api.entities.Subscription;
import org.springframework.social.alfresco.api.entities.SubscriptionType;


/**
 * 
 * @author sglover
 * @since 1.0
 */
public class CreateSubscriptions extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(CreateSubscriptions.class);

    private final SubscriptionsService subscriptionsService;

    private final String eventNamePrepareSyncs;
    private final String eventNameCreateSubscriptions;

    private final int batchSize;
    
    private final PublicApiFactory publicApiFactory;

    /**
     * Constructor 
     * 
     * @param siteDataService_p             Site Data service to retrieve site information from Mongo
     * @param userDataService_p             User Data service to retrieve user information from Mongo
     * @param desktopSyncClientRegistry_p   Registry to create the clients 
     * @param numberOfClients_p             Number of clients to create
     * @param nextEventId_p                 ID of the next event
     */
    public CreateSubscriptions(SubscriptionsService subscriptionsService, PublicApiFactory publicApiFactory,
    		int batchSize, String eventNamePrepareSyncs, String eventNameCreateSubscriptions)
    {
    	this.subscriptionsService = subscriptionsService;
    	this.publicApiFactory = publicApiFactory;
    	this.batchSize = batchSize;
    	this.eventNameCreateSubscriptions = eventNameCreateSubscriptions;
    	this.eventNamePrepareSyncs = eventNamePrepareSyncs;

        // validate arguments
    	Util.checkArgumentNotNull(subscriptionsService, "subscriptionsService");
        Util.checkArgumentNotNull(publicApiFactory, "publicApiFactory");
        Util.checkArgumentNotNull(eventNameCreateSubscriptions, "eventNameCreateSubscriptions");
        Util.checkArgumentNotNull(eventNamePrepareSyncs, "eventNamePrepareSyncs");
    }

    private Alfresco getAlfresco(String username)
    {
    	Alfresco alfresco = publicApiFactory.getPublicApi(username);
    	return alfresco;
    }

    @Override
    protected EventResult processEvent(Event event_p) throws Exception
    {
        try
        {
        	String msg  = null;
            List<Event> nextEvents = new LinkedList<>();

            long numSubscriptions = subscriptionsService.countSubscriptions(DataCreationState.Scheduled);
            if(numSubscriptions == 0)
            {
                long time = System.currentTimeMillis() + 5000L;
                Event doneEvent = new Event(eventNamePrepareSyncs, time, null);
                nextEvents.add(doneEvent);
                msg = "Created all subscriptions and raising '" + doneEvent.getName()
                        + "' event.";
            }
            else
            {
                List<SubscriptionData> subscriptions = subscriptionsService.getSubscriptions(
                		DataCreationState.Scheduled, 0, batchSize);
                subscriptions.stream().forEach(subscriptionData -> {
                	try
                	{
                		SubscriptionType subscriptionType = SubscriptionType.valueOf(subscriptionData.getSubscriptionType());
                		String username = subscriptionData.getUsername();
                		Alfresco alfresco = getAlfresco(username);
                		Subscription subscription = alfresco.createSubscription("-default-", subscriptionData.getSubscriberId(),
                				subscriptionType, subscriptionData.getPath());
                    	subscriptionsService.updateSubscription(subscriptionData.getObjectId(),
                    			subscription.getId(), DataCreationState.Created);
                	}
                	catch(Exception e)
                	{
                		subscriptionsService.updateSubscription(subscriptionData.getObjectId(), DataCreationState.Failed);
                	}
                });
//                .forEach(subscriptionData -> {
//                	subscriptionsService.updateSubscription(subscriptionData.getObjectId(),
//                			subscriptionData.getSubscriptionId(),
//                			DataCreationState.Created);
//                });

                long time = System.currentTimeMillis() + 5000L;
                Event nextEvent = new Event(eventNameCreateSubscriptions, time, null);
                nextEvents.add(nextEvent);
                msg = "Created " + subscriptions.size() + " subscriptions and raising '" + eventNameCreateSubscriptions
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
            logger.error("Error creating subscriptions. ", e);
            throw e;
        }
    }
}

package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.SubscriptionBatchData;
import org.alfresco.bm.devicesync.util.Util;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 
 * @author sglover
 * @since 1.0
 */
public class PrepareSubscriptions extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(PrepareSubscriptions.class);

    private final SubscriptionsService subscriptionsService;

    private final int maxSubscriptions;

    private final String nextEventName;
    private final String eventNameSubscriptionsBatch;

    /**
     * Constructor 
     * 
     * @param siteDataService_p             Site Data service to retrieve site information from Mongo
     * @param userDataService_p             User Data service to retrieve user information from Mongo
     * @param desktopSyncClientRegistry_p   Registry to create the clients 
     * @param numberOfClients_p             Number of clients to create
     * @param nextEventId_p                 ID of the next event
     */
    public PrepareSubscriptions(SubscriptionsService subscriptionsService,
    		String eventNameSubscriptionsBatch, String nextEventName, int maxSubscriptions)
    {
    	this.subscriptionsService = subscriptionsService;
        this.eventNameSubscriptionsBatch = eventNameSubscriptionsBatch;
        this.nextEventName = nextEventName;
        this.maxSubscriptions = maxSubscriptions;

        // validate arguments
        Util.checkArgumentNotNull(subscriptionsService, "subscriptionsService");
        Util.checkArgumentNotNull(eventNameSubscriptionsBatch, "eventNameSubscriptionsBatch");
        Util.checkArgumentNotNull(maxSubscriptions, "maxSubscriptions");
    }

//    private EventResult prepareNewSubscriptions(int numSubscriptions)
//    {
//        List<Event> nextEvents = new LinkedList<>();
//
//    	List<SubscriberData> subscribers = subscribersService.getSubscribers(
//    			DataCreationState.Created, 0, numSubscriptions);
//    	subscribers.stream().forEach(susbcriberData -> {
//    		List<SiteData> sites = siteDataService.getSites("default", DataCreationState.Created, 0, 1);
//    		if(sites.size() > 0)
//    		{
//    			SiteData siteData = sites.get(0);
//    			String username = susbcriberData.getUsername();
//    			String subscriberId = susbcriberData.getSubscriberId();
//    			String subscriptionType = SubscriptionType.CONTENT.toString();
//    			String sitePath = siteData.getPath();
//    			if(sitePath == null || sitePath.equals(""))
//    			{
//    				sitePath = "/Company Home/Sites/" + siteData.getSiteId() + "/documentLibrary";
//    			}
//                subscriptionsService.addSubscription(username, subscriberId, subscriptionType, sitePath,
//                		DataCreationState.Scheduled);
//    		}
//    	});
//
//    	Event event = new Event(eventNameCreateSubscriptions, null);
//    	nextEvents.add(event);
//        String msg = "Prepared " + numSubscriptions + " subscriptions and raising '" + event.getName()
//                + "' event.";
//
//        if (logger.isDebugEnabled())
//        {
//            logger.debug(msg);
//        }
//
//        EventResult result = new EventResult(msg, nextEvents);
//        return result;
//    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
        try
        {
        	String msg = null;
            List<Event> nextEvents = new LinkedList<>();

            long numSubscriptions = subscriptionsService.countSubscriptions(DataCreationState.Created);
            if(maxSubscriptions <= numSubscriptions)
            {
                msg = "Prepared subscriptions, nothing to do";
            	Event newEvent = new Event(nextEventName, null);
            	nextEvents.add(newEvent);
            }
            else
            {
            	int count = (int)(maxSubscriptions - numSubscriptions); // TODO
            	int batchSize = 20;
            	int numBatches = (int)count/batchSize; // TODO
            	SubscriptionBatchData subscriptionBatchData = new SubscriptionBatchData(0, batchSize, numBatches, null,
            			nextEventName, null);
            	Event newEvent = new Event(eventNameSubscriptionsBatch, subscriptionBatchData.toDBObject());
            	nextEvents.add(newEvent);

//            	List<SubscriberData> subscribers = subscribersService.getSubscribers(
//            			DataCreationState.Created, 0, count);
//            	subscribers.stream().forEach(susbcriberData -> {
//            		List<SiteData> sites = siteDataService.getSites("default", DataCreationState.Created, 0, 1);
//            		if(sites.size() > 0)
//            		{
//            			SiteData siteData = sites.get(0);
//            			String username = susbcriberData.getUsername();
//            			String subscriberId = susbcriberData.getSubscriberId();
//            			String subscriptionType = SubscriptionType.CONTENT.toString();
//            			String sitePath = siteData.getPath();
//            			if(sitePath == null || sitePath.equals(""))
//            			{
//            				sitePath = "/Company Home/Sites/" + siteData.getSiteId() + "/documentLibrary";
//            			}
//    	                subscriptionsService.addSubscription(username, subscriberId, subscriptionType, sitePath,
//    	                		DataCreationState.Scheduled);
//            		}
//            	});
//
//            	Event nextEvent = new Event(eventNameCreateSubscriptions, null);
//            	nextEvents.add(nextEvent);

                msg = "Prepared " + count + " subscriptions.";
            }

            if (logger.isDebugEnabled())
            {
                logger.debug(msg);
            }

            EventResult result = new EventResult(msg, nextEvents);
            return result;
        }
        catch (Exception e)
        {
            logger.error("Error preparing desktop sync clients. ", e);
            throw e;
        }
    }
}

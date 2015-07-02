package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.ExtendedSiteDataService;
import org.alfresco.bm.devicesync.dao.SubscribersService;
import org.alfresco.bm.devicesync.data.SubscriberData;
import org.alfresco.bm.devicesync.data.SubscriptionBatchData;
import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.alfresco.bm.site.SiteMemberData;
import org.alfresco.bm.site.SiteRole;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 * @since 1.0
 */
public class SubscriptionsBatch extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(SubscriptionsBatch.class);

    private final ExtendedSiteDataService siteDataService;
    private final SubscribersService subscribersDataService;

    private final String eventNameCreateSubscription;

    private final int batchSize;
    private final int numBatches;
    private final int waitTimeBetweenBatches; // ms

    /**
     * Constructor 
     * 
     * @param siteDataService_p             Site Data service to retrieve site information from Mongo
     * @param userDataService_p             User Data service to retrieve user information from Mongo
     * @param desktopSyncClientRegistry_p   Registry to create the clients 
     * @param numberOfClients_p             Number of clients to create
     * @param nextEventId_p                 ID of the next event
     */
    public SubscriptionsBatch(SubscribersService subscribersDataService, ExtendedSiteDataService siteDataService,
    		int batchSize, int numBatches, int waitTimeBetweenBatches, String eventNameCreateSubscription)
    {
    	this.subscribersDataService = subscribersDataService;
    	this.siteDataService = siteDataService;
    	this.eventNameCreateSubscription = eventNameCreateSubscription;
    	this.batchSize = batchSize;
    	this.numBatches = numBatches;
    	this.waitTimeBetweenBatches = waitTimeBetweenBatches;
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
    	super.suspendTimer();

    	DBObject dbObject = (DBObject)event.getData();
    	SubscriptionBatchData subscriptionBatchData = SubscriptionBatchData.fromDBObject(dbObject);
    	Integer batchSizeParameter = subscriptionBatchData.getBatchSize();
    	int batchSize = batchSizeParameter != null ? 
    			batchSizeParameter.intValue() : this.batchSize;
    	Integer numBatchesParameter = subscriptionBatchData.getNumBatches();
    	int numBatches = numBatchesParameter != null ? 
    			numBatchesParameter.intValue() : this.numBatches;
    	Integer waitTimeBetweenBatchesParameter = subscriptionBatchData.getWaitTimeBetweenBatches();
    	int waitTimeBetweenBatches = waitTimeBetweenBatchesParameter != null ? 
    			waitTimeBetweenBatchesParameter.intValue() : this.waitTimeBetweenBatches;
    	int count = subscriptionBatchData.getCount();
    	String nextEventName = subscriptionBatchData.getNextEventName();
    	List<String> sites = subscriptionBatchData.getSites();

        try
        {
            List<Event> nextEvents = new LinkedList<>();
            String msg = null;

            if(count >= numBatches)
            {
            	if(nextEventName == null || nextEventName.equals(""))
            	{
                	msg = "Hit number of batches, stopping.";
            	}
            	else
            	{
                	msg = "Hit number of batches, raising next event " + nextEventName;
                	Event nextEvent = new Event(nextEventName, System.currentTimeMillis(), null);
                	nextEvents.add(nextEvent);
            	}
            }
            else
            {
            	int subscriptionsCreated = 0;

        		// we assume that subscribers are a member of at least one site
            	try(Stream<SubscriberData> subscribers = subscribersDataService.randomSubscribers(batchSize))
            	{
            		List<Event> events = subscribers.map(sd -> {
            			String username = sd.getUsername();
            			String subscriberId = sd.getSubscriberId();

                		SiteMemberData sm = siteDataService.randomSiteMember(null, DataCreationState.Created, username,
                				SiteRole.SiteManager.toString(), SiteRole.SiteCollaborator.toString());
                		String siteId = sm.getSiteId();

                		logger.debug("Got site member data " + sm + " for user " + username);

	            		SubscriptionData subscriptionData = new SubscriptionData(siteId, username, subscriberId);
	                	Event nextEvent = new Event(eventNameCreateSubscription, System.currentTimeMillis(),
	                			subscriptionData.toDBObject());
	                	return nextEvent;
            		})
            		.collect(Collectors.toList());

        			subscriptionsCreated = events.size();
                	nextEvents.addAll(events);
            	}

//        		try(Stream<SiteMemberData> siteMembers = siteDataService.randomSiteMembers(DataCreationState.Created,
//        				new String[] {SiteRole.SiteManager.toString(),
//        				SiteRole.SiteCollaborator.toString(),
//        				SiteRole.SiteContributor.toString()},
//        				batchSize))
//				{
//        			List<Event> events = siteMembers.map(sm -> {
//                		String siteId = sm.getSiteId();
//                		String username = sm.getUsername();
//
//                		logger.debug("Getting random subscriber for user " + username);
//
//                		SubscriberData subscriberData = subscribersDataService.getRandomSubscriber(username);
//                		String subscriberId = subscriberData.getSubscriberId();
//
//	            		SubscriptionData subscriptionData = new SubscriptionData(siteId, username, subscriberId);
//	                	Event nextEvent = new Event(eventNameCreateSubscription, System.currentTimeMillis(),
//	                			subscriptionData.toDBObject());
//	                	return nextEvent;
//        			})
//        			.collect(Collectors.toList());
//        			subscriptionsCreated = events.size();
//                	nextEvents.addAll(events);
//				}

	        	{
	            	long scheduledTime = System.currentTimeMillis() + waitTimeBetweenBatches;
	            	SubscriptionBatchData newSubscriptionBatchData = new SubscriptionBatchData(count + 1,
	            			batchSizeParameter, numBatchesParameter, waitTimeBetweenBatchesParameter, nextEventName,
	            			sites);
	            	Event nextEvent = new Event(event.getName(), scheduledTime, newSubscriptionBatchData.toDBObject());
	            	nextEvents.add(nextEvent);
	        	}

	        	msg = "Prepared " + subscriptionsCreated + " subscription creates";
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
            logger.error("Error creating subscribers batch. ", e);
            throw e;
        }
    }
}

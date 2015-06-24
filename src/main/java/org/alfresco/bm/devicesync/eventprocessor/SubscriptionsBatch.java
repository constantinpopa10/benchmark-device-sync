package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscribersService;
import org.alfresco.bm.devicesync.data.SubscriberData;
import org.alfresco.bm.devicesync.data.SubscriptionBatchData;
import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.devicesync.util.SiteSampleSelector;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.alfresco.bm.site.SiteDataService;
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

    private SiteDataService siteDataService;
    private final SubscribersService subscribersDataService;
    private final SiteSampleSelector siteSampleSelector;

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
    public SubscriptionsBatch(SubscribersService subscribersDataService, SiteSampleSelector siteSampleSelector,
    		SiteDataService siteDataService,
    		int batchSize, int numBatches,
    		int waitTimeBetweenBatches, String eventNameCreateSubscription)
    {
    	this.subscribersDataService = subscribersDataService;
    	this.siteDataService = siteDataService;
    	this.siteSampleSelector = siteSampleSelector;
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
	        	{
	            	for(int i = 0; i < batchSize; i++)
	            	{
//	            		String siteId = siteSampleSelector.getSite();

	            		// for a random subscriber...
	            		SubscriberData subscriberData = subscribersDataService.getRandomSubscriber(null);
	            		String subscriberId = subscriberData.getSubscriberId();
	            		String username = subscriberData.getUsername();

	            		// find a site to which they can write content
	            		SiteMemberData siteMemberData = siteDataService.randomSiteMember(null,
	            				DataCreationState.Created, username,
	            				SiteRole.SiteManager.toString(), SiteRole.SiteCollaborator.toString(),
	            				SiteRole.SiteContributor.toString());
	            		String siteId = siteMemberData.getSiteId();

	            		SubscriptionData subscriptionData = new SubscriptionData(siteId, username, subscriberId);
	                	Event nextEvent = new Event(eventNameCreateSubscription, System.currentTimeMillis(),
	                			subscriptionData.toDBObject());
	                	nextEvents.add(nextEvent);
	            	}
	        	}

	        	{
	            	long scheduledTime = System.currentTimeMillis() + waitTimeBetweenBatches;
	            	SubscriptionBatchData newSubscriptionBatchData = new SubscriptionBatchData(count + 1,
	            			batchSizeParameter, numBatchesParameter, waitTimeBetweenBatchesParameter, nextEventName,
	            			sites);
	            	Event nextEvent = new Event(event.getName(), scheduledTime, newSubscriptionBatchData.toDBObject());
	            	nextEvents.add(nextEvent);
	        	}

	        	msg = "Prepared " + batchSize + " subscription creates";
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

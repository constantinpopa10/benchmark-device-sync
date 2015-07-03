package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.ExtendedSiteDataService;
import org.alfresco.bm.devicesync.data.SubscriberBatchData;
import org.alfresco.bm.devicesync.data.SubscriberData;
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
public class SubscribersBatch extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(SubscribersBatch.class);

    private final ExtendedSiteDataService siteDataService;

    private final String eventNameCreateSubscriber;

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
    public SubscribersBatch(ExtendedSiteDataService siteDataService,
    		int batchSize, int numBatches, int waitTimeBetweenBatches,
    		String eventNameCreateSubscriber)
    {
    	this.siteDataService = siteDataService;
    	this.eventNameCreateSubscriber = eventNameCreateSubscriber;
    	this.batchSize = batchSize;
    	this.numBatches = numBatches;
    	this.waitTimeBetweenBatches = waitTimeBetweenBatches; 
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
    	DBObject dbObject = (DBObject)event.getData();
    	SubscriberBatchData subscriberBatchData = SubscriberBatchData.fromDBObject(dbObject);
    	Integer batchSizeParameter = subscriberBatchData.getBatchSize();
    	int batchSize = batchSizeParameter != null ? 
    			batchSizeParameter.intValue() : this.batchSize;
    	Integer numBatchesParameter = subscriberBatchData.getNumBatches();
    	int numBatches = numBatchesParameter != null ? 
    			numBatchesParameter.intValue() : this.numBatches;
    	Integer waitTimeBetweenBatchesParameter = subscriberBatchData.getWaitTimeBetweenBatches();
    	int waitTimeBetweenBatches = waitTimeBetweenBatchesParameter != null ? 
    			waitTimeBetweenBatchesParameter.intValue() : this.waitTimeBetweenBatches;
    	int count = subscriberBatchData.getCount();
    	String nextEventName = subscriberBatchData.getNextEventName();

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
            	int subscribersCreated = 0;
        		final long scheduledTime = System.currentTimeMillis();

        		// we create subscribers based on users who are members of sites...
        		try(Stream<SiteMemberData> siteMembers = siteDataService.randomSiteMembers(DataCreationState.Created,
        				new String[] {SiteRole.SiteManager.toString(),
        				SiteRole.SiteCollaborator.toString(),
        				SiteRole.SiteContributor.toString()},
        				batchSize))
				{
        			List<Event> events = siteMembers.map(sm -> {
                		String username = sm.getUsername();

                		logger.debug("Subscriber, site member " + sm);

	            		SubscriberData subscriberData = new SubscriberData(username);
	                	Event nextEvent = new Event(eventNameCreateSubscriber, scheduledTime,
	                			subscriberData.toDBObject());
	                	return nextEvent;
        			})
        			.collect(Collectors.toList());
        			subscribersCreated = events.size();
                	nextEvents.addAll(events);
				}
	
	        	{
	            	final long nextBatchScheduledTime = System.currentTimeMillis() + waitTimeBetweenBatches;
	            	SubscriberBatchData newSubscriberBatchData = new SubscriberBatchData(count + 1, batchSizeParameter,
	            			numBatchesParameter, waitTimeBetweenBatchesParameter, nextEventName);
	            	Event nextEvent = new Event(event.getName(), nextBatchScheduledTime,
	            			newSubscriberBatchData.toDBObject());
	            	nextEvents.add(nextEvent);
	        	}

	        	msg = "Prepared " + subscribersCreated + " subscriber creates";
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

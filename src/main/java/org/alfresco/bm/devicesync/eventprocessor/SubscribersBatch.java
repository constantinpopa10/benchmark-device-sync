package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.data.SubscriberBatchData;
import org.alfresco.bm.devicesync.data.SubscriberData;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataService;
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

    private final UserDataService userDataService;

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
    public SubscribersBatch(UserDataService userDataService, int batchSize, int numBatches, int waitTimeBetweenBatches,
    		String eventNameCreateSubscriber)
    {
    	this.userDataService = userDataService;
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
	        	{
	            	for(int i = 0; i < batchSize; i++)
	            	{
	            		UserData userData = userDataService.getRandomUser();
	            		String username = userData.getUsername();
	            		SubscriberData subscriberData = new SubscriberData(username);
	                	Event nextEvent = new Event(eventNameCreateSubscriber, System.currentTimeMillis(), subscriberData.toDBObject());
	                	nextEvents.add(nextEvent);
	            	}
	        	}
	
	        	{
	            	long scheduledTime = System.currentTimeMillis() + waitTimeBetweenBatches;
	            	SubscriberBatchData newSubscriberBatchData = new SubscriberBatchData(count + 1, batchSizeParameter,
	            			numBatchesParameter, waitTimeBetweenBatchesParameter, nextEventName);
	            	Event nextEvent = new Event(event.getName(), scheduledTime, newSubscriberBatchData.toDBObject());
	            	nextEvents.add(nextEvent);
	        	}

	        	msg = "Prepared " + batchSize + " subscriber creates";
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

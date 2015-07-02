package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.UploadFileBatchData;
import org.alfresco.bm.devicesync.data.UploadFileData;
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
public class UploadFileBatch extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(UploadFileBatch.class);

    private SubscriptionsService subscriptionsService;
    private final SiteSampleSelector siteSampleSelector;

    private final int batchSize;
    private final int numBatches;
    private final int waitTimeBetweenBatches; // ms

    private final String eventNameUploadFile;

    /**
     * Constructor 
     * 
     * @param siteDataService_p             Site Data service to retrieve site information from Mongo
     * @param userDataService_p             User Data service to retrieve user information from Mongo
     * @param desktopSyncClientRegistry_p   Registry to create the clients 
     * @param numberOfClients_p             Number of clients to create
     * @param nextEventId_p                 ID of the next event
     */
    public UploadFileBatch(SiteSampleSelector siteSampleSelector, SubscriptionsService subscriptionsService,
    		int batchSize, int numBatches,
    		int waitTimeBetweenBatches, String eventNameUploadFile)
    {
    	this.siteSampleSelector = siteSampleSelector;
    	this.subscriptionsService = subscriptionsService;
    	this.batchSize = batchSize;
        this.numBatches = numBatches;
        this.eventNameUploadFile = eventNameUploadFile;
        this.waitTimeBetweenBatches = waitTimeBetweenBatches;

        // validate arguments
        Util.checkArgumentNotNull(subscriptionsService, "subscriptionsService");
    }


    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
    	DBObject dbObject = (DBObject)event.getData();
    	UploadFileBatchData uploadFileBatchData = UploadFileBatchData.fromDBObject(dbObject);
    	int count = uploadFileBatchData.getCount();

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
            		try(Stream<UploadFileData> subscriptions = siteSampleSelector.getSubscriptions(batchSize))
            		{
            			List<Event> events = subscriptions.map(ufd -> {
		                	Event nextEvent = new Event(eventNameUploadFile, System.currentTimeMillis(),
		                			ufd.toDBObject());
		                	return nextEvent;
	            		})
	            		.collect(Collectors.toList());
	            		nextEvents.addAll(events);
            		}
            	}

            	{
	            	long scheduledTime = System.currentTimeMillis() + waitTimeBetweenBatches;
	            	UploadFileBatchData newUploadFileBatchData = new UploadFileBatchData(count + 1);
	            	Event nextEvent = new Event(event.getName(), scheduledTime, newUploadFileBatchData.toDBObject());
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

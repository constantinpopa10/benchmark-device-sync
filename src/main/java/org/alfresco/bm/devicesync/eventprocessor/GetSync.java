package org.alfresco.bm.devicesync.eventprocessor;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.data.SyncData;
import org.alfresco.bm.devicesync.util.PublicApiFactory;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.alfresco.service.synchronization.api.GetChangesResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.social.alfresco.api.Alfresco;

import com.mongodb.DBObject;



/**
 * Execution of desktop sync client.
 * 
 * Randomly does sync operations as local file changes, stop or start desktop sync.
 * 
 * @author sglover
 * @since 1.0
 */
public class GetSync extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(GetSync.class);

    private PublicApiFactory publicApiFactory;
    private int waitTimeMillisBeforeSyncOps;
    private int maxTries;

    /**
     * Constructor
     * 
     * @param fileService_p
     *            (TestFileService) delivers test files to insert locally
     * @param maxFolderDepth_p
     *            (int >= 0) Maximum folder depth to handle during create/move/delete of files
     * @param maxNumberOfFileOperations_p
     *            (int > 0) Maximum number of file operations per event
     * @param waitTimeMillisBetweenEvents_p
     *            (int > 0) Wait time between events
     */
    public GetSync(PublicApiFactory publicApiFactory, int waitTimeMillisBeforeSyncOps, int maxTries)
    {
    	this.publicApiFactory = publicApiFactory;
    	this.waitTimeMillisBeforeSyncOps = waitTimeMillisBeforeSyncOps;
    	this.maxTries = maxTries;
        if (logger.isDebugEnabled())
        {
            logger.debug("Created event processor 'get sync'.");
        }
    }

    private Alfresco getAlfresco(String username)
    {
    	Alfresco alfresco = publicApiFactory.getPublicApi(username);
    	return alfresco;
    }

    private void getSync(int counter, Alfresco alfresco, SyncData syncData) 
    		throws JsonParseException, JsonMappingException, IOException
    {
    	String subscriberId = syncData.getSubscriptionId();
    	String subscriptionId = syncData.getSubscriptionId();
		String syncId = syncData.getSyncId();

		GetChangesResponse response = alfresco.getSync("-default-", subscriberId, subscriptionId, syncId);
		logger.debug("response = " + response);

		String status = response.getStatus();
		switch(status)
		{
		case "not ready":
			if(counter < maxTries)
			{
				syncData.incrementRetries();
				getSync(counter + 1, alfresco, syncData);
			}
			else
			{
				syncData.maxRetriesHit();
			}
			break;
		case "ready":
			syncData.gotResults(response.getChanges());
			break;
		default:
		}
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
    	DBObject dbObject = (DBObject)event.getData();
    	SyncData syncData = SyncData.fromDBObject(dbObject);

        try
        {
            String username = syncData.getUsername();
    		String syncId = syncData.getSyncId();
    	    Alfresco alfresco = getAlfresco(username);

            List<Event> nextEvents = new LinkedList<Event>();

			getSync(0, alfresco, syncData);

			long scheduledTime = System.currentTimeMillis() + waitTimeMillisBeforeSyncOps;
            Event nextEvent = new Event("endSync", scheduledTime, syncData.toDBObject());
        	nextEvents.add(nextEvent);
			String msg = "Get sync " + syncId;

            if (logger.isDebugEnabled())
            {
                logger.debug(msg);
            }

            return new EventResult(msg, nextEvents);
        }
        catch (Exception e)
        {
            String msg = "Exception occurred during event processing: Terminate desktop sync client '";
            logger.error(msg, e);

            return new EventResult(msg, Collections.emptyList());
        }
    }
}
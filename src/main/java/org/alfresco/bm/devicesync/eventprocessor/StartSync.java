package org.alfresco.bm.devicesync.eventprocessor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.data.SyncData;
import org.alfresco.bm.devicesync.util.PublicApiFactory;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.alfresco.bm.session.SessionService;
import org.alfresco.service.synchronization.api.StartSyncRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.social.alfresco.api.Alfresco;
import org.springframework.social.alfresco.api.entities.StartSyncResponse;

import com.mongodb.DBObject;



/**
 * Execution of desktop sync client.
 * 
 * Randomly does sync operations as local file changes, stop or start desktop sync.
 * 
 * @author sglover
 * @since 1.0
 */
public class StartSync extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(StartSync.class);

    private final SessionService sessionService;
    private final PublicApiFactory publicApiFactory;
    private final int timeBetweenSyncOps;

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
    public StartSync(SessionService sessionService, PublicApiFactory publicApiFactory, int timeBetweenSyncOps)
    {
    	this.sessionService = sessionService;
    	this.publicApiFactory = publicApiFactory;
    	this.timeBetweenSyncOps = timeBetweenSyncOps;
        if (logger.isDebugEnabled())
        {
            logger.debug("Created event processor 'start sync'.");
        }
    }

    private Alfresco getAlfresco(String username)
    {
    	Alfresco alfresco = publicApiFactory.getPublicApi(username);
    	return alfresco;
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
    	super.suspendTimer();

    	DBObject dbObject = (DBObject)event.getData();
    	SyncData syncData = SyncData.fromDBObject(dbObject);

        try
        {
            List<Event> nextEvents = new LinkedList<Event>();

        	Long endTime = syncData.getEndTime();
        	if(endTime == null || System.currentTimeMillis() <= endTime)
        	{
	            this.sessionService.startSession(null);

	            String username = syncData.getUsername();
	    		String subscriberId = syncData.getSubscriberId();
	    		String subscriptionId = syncData.getSubscriptionId();
	    	    Alfresco alfresco = getAlfresco(username);

				StartSyncRequest req = new StartSyncRequest(Collections.emptyList());
		    	super.resumeTimer();
				StartSyncResponse response = alfresco.startSync(req, "-default-", subscriberId, subscriptionId);
		    	super.suspendTimer();
				long syncId = Long.valueOf(response.getSyncId());

				logger.debug("response = " + response);

				long scheduledTime = System.currentTimeMillis() + timeBetweenSyncOps;
				syncData = new SyncData(null, syncData.getSiteId(), syncData.getUsername(), syncData.getSubscriberId(),
						syncData.getSubscriptionId(), syncId, -1, -1, false, endTime, "Started sync " + syncId);
	            Event nextEvent = new Event("getSync", scheduledTime, syncData.toDBObject());
	        	nextEvents.add(nextEvent);
        	}
        	else
        	{
				syncData = new SyncData(null, syncData.getSiteId(), syncData.getUsername(), syncData.getSubscriberId(),
						syncData.getSubscriptionId(), null, -1, -1, false, endTime, "Start sync end time reached");
        	}

            return new EventResult(syncData.toDBObject(), nextEvents);
        }
        catch (Exception e)
        {
            logger.error("Exception occurred during start sync.", e);
            throw e;
        }
    }
}
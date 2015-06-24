package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.data.SyncData;
import org.alfresco.bm.devicesync.util.PublicApiFactory;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class EndSync extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(EndSync.class);

    private PublicApiFactory publicApiFactory;

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
    public EndSync(PublicApiFactory publicApiFactory)
    {
    	this.publicApiFactory = publicApiFactory;
        if (logger.isDebugEnabled())
        {
            logger.debug("Created event processor 'end sync'.");
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
            String username = syncData.getUsername();
    		String subscriberId = syncData.getSubscriberId();
    		String subscriptionId = syncData.getSubscriptionId();
    		String syncId = syncData.getSyncId();
    	    Alfresco alfresco = getAlfresco(username);

        	super.resumeTimer();
			alfresco.endSync("-default-", subscriberId, subscriptionId, syncId);
	    	super.suspendTimer();

	    	SyncData data = new SyncData(null, syncData.getSiteId(), syncData.getUsername(), syncData.getSubscriberId(),
	    			syncData.getSubscriptionId(), syncData.getSyncId(), syncData.getNumSyncChanges(),
	    			syncData.getNumRetries(), syncData.isMaximumRetriesHit(), syncData.getEndTime(), "Ended sync " + syncId);

            List<Event> nextEvents = new LinkedList<Event>();

            return new EventResult(data.toDBObject(), nextEvents);
        }
        catch (Exception e)
        {
            logger.error("Exception occurred during event processing", e);
            throw e;
        }
    }
}
package org.alfresco.bm.devicesync.eventprocessor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import org.alfresco.bm.devicesync.dao.SyncsService;
import org.alfresco.bm.devicesync.data.SyncData;
import org.alfresco.bm.devicesync.data.SyncResults;
import org.alfresco.bm.devicesync.data.SyncState;
import org.alfresco.bm.devicesync.data.SyncStateData;
import org.alfresco.bm.devicesync.util.PublicApiFactory;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.alfresco.bm.session.SessionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.social.alfresco.api.Alfresco;

/**
 * Execution of desktop sync client.
 * 
 * Randomly does sync operations as local file changes, stop or start desktop
 * sync.
 * 
 * @author sglover
 * @since 1.0
 */
public class BatchExecuteSyncs extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(BatchExecuteSyncs.class);

    private SyncsService syncsService;
    private SessionService sessionService;
    private PublicApiFactory publicApiFactory;

    private int batchSize;
    private int waitTimeMillisBetweenEvents = 1000;

    /**
     * Constructor
     * 
     * @param fileService_p
     *            (TestFileService) delivers test files to insert locally
     * @param maxFolderDepth_p
     *            (int >= 0) Maximum folder depth to handle during
     *            create/move/delete of files
     * @param maxNumberOfFileOperations_p
     *            (int > 0) Maximum number of file operations per event
     * @param waitTimeMillisBetweenEvents_p
     *            (int > 0) Wait time between events
     */
    public BatchExecuteSyncs(SessionService sessionService,
            SyncsService syncsService, PublicApiFactory publicApiFactory,
            int batchSize, int waitTimeMillisBetweenEvents)
    {
        this.sessionService = sessionService;
        this.syncsService = syncsService;
        this.publicApiFactory = publicApiFactory;
        this.batchSize = batchSize;
        this.waitTimeMillisBetweenEvents = waitTimeMillisBetweenEvents;
        if (logger.isDebugEnabled())
        {
            logger.debug("Created event processor 'execute desktop sync'.");
        }
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
            String msg = null;
            List<Event> nextEvents = new LinkedList<Event>();

            this.sessionService.startSession(null);

            List<SyncData> syncs = syncsService.getSyncs(
                    SyncState.NotScheduled, 0, batchSize);
            if (syncs.size() == 0)
            {
                msg = "No more syncs to execute";
            }
            else
            {
                SyncResults res = syncs
                        .stream()
                        .map(syncData -> {
                            ObjectId objectId = syncData.getObjectId();
                            String subscriberId = syncData.getSubscriberId();
                            String subscriptionId = syncData
                                    .getSubscriptionId();
                            String username = syncData.getUsername();

                            Alfresco alfresco = getAlfresco(username);

                            syncsService.updateSync(objectId,
                                    SyncState.Scheduled, null);

                            SyncStateData syncStateData = new SyncStateData(
                                    alfresco, objectId, username, subscriberId,
                                    subscriptionId);
                            return syncStateData.startSync(syncsService);
                        })
                        .map(syncStateData -> {
                            return syncStateData.getSync(syncsService);
                        })
                        .map(syncStateData -> {
                            return syncStateData.endSync(syncsService);
                        })
                        .reduce(new SyncResults(0),
                                new BiFunction<SyncResults, SyncStateData, SyncResults>()
                                {
                                    @Override
                                    public SyncResults apply(SyncResults t,
                                            SyncStateData u)
                                    {
                                        t.apply(u);
                                        return t;
                                    }
                                }, new BinaryOperator<SyncResults>()
                                {
                                    @Override
                                    public SyncResults apply(SyncResults t,
                                            SyncResults u)
                                    {
                                        return t.combine(u);
                                    }
                                });

                logger.debug("res = " + res);

                // create same event again - delay to next event
                Event nextEvent = new Event(event_p.getName(),
                        System.currentTimeMillis()
                                + this.waitTimeMillisBetweenEvents, null);
                nextEvents.add(nextEvent);

                msg = "Executed " + syncs.size() + " syncs";
            }

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
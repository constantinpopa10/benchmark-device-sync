package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.TreeWalkBatchData;
import org.alfresco.bm.devicesync.data.TreeWalkData;
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
 * 
 * @author sglover
 * @since 1.0
 */
public class TreeWalkBatch extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(TreeWalkBatch.class);

    private SubscriptionsService subscriptionsService;

    private final int batchSize;
    private final int numBatches;
    private final int waitTimeBetweenBatches; // ms

    private final SiteSampleSelector siteSampleSelector;

    private final String eventNameTreeWalk;

    /**
     * Constructor
     * 
     * @param siteDataService_p
     *            Site Data service to retrieve site information from Mongo
     * @param userDataService_p
     *            User Data service to retrieve user information from Mongo
     * @param desktopSyncClientRegistry_p
     *            Registry to create the clients
     * @param numberOfClients_p
     *            Number of clients to create
     * @param nextEventId_p
     *            ID of the next event
     */
    public TreeWalkBatch(SubscriptionsService subscriptionsService,
            SiteSampleSelector siteSampleSelector, int batchSize,
            int numBatches, int waitTimeBetweenBatches,
            String eventNameTreeWalk)
    {
        this.subscriptionsService = subscriptionsService;
        this.siteSampleSelector = siteSampleSelector;
        this.batchSize = batchSize;
        this.numBatches = numBatches;
        this.eventNameTreeWalk = eventNameTreeWalk;
        this.waitTimeBetweenBatches = waitTimeBetweenBatches;

        // validate arguments
        Util.checkArgumentNotNull(subscriptionsService, "subscriptionsService");
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
        DBObject dbObject = (DBObject) event.getData();
        TreeWalkBatchData treeWalkBatchData = TreeWalkBatchData.fromDBObject(dbObject);
        int count = treeWalkBatchData.getCount();
        List<String> sites = treeWalkBatchData.getSites();

        try
        {
            String msg = null;
            List<Event> nextEvents = new LinkedList<Event>();

            long numSubscriptions = subscriptionsService
                    .countSubscriptions(DataCreationState.Created);
            if (numSubscriptions == 0)
            {
                msg = "No subscriptions, stopping.";
            }
            else if (count >= numBatches)
            {
                msg = "Hit number of batches, stopping.";
            }
            else
            {
                long scheduledTime = System.currentTimeMillis();

                // TODO fix up so we get at least batchSize even if
                // siteSampleSelector
                // fails to give us a subscription. Stream?
                try (Stream<UploadFileData> subscriptions = siteSampleSelector
                        .getSubscriptions(batchSize))
                {
                    List<Event> events = subscriptions.map(
                            ufd -> {
                                String username = ufd.getUsername();
                                String siteId = ufd.getSiteId();
                                TreeWalkData treeWalkData = new TreeWalkData(0, 0,
                                        username, siteId);
                                Event nextEvent = new Event(eventNameTreeWalk,
                                        scheduledTime, treeWalkData.toDBObject());
                                return nextEvent;
                            }).collect(Collectors.toList());

                    nextEvents.addAll(events);
                }

                {
                    TreeWalkBatchData newTreeWalkBatchData = new TreeWalkBatchData(
                            count + 1, sites);
                    Event nextEvent = new Event(event.getName(), scheduledTime
                            + waitTimeBetweenBatches,
                            newTreeWalkBatchData.toDBObject());
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

package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.SubscriptionBatchData;
import org.alfresco.bm.devicesync.util.Util;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 * @since 1.0
 */
public class PrepareSubscriptions extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(PrepareSubscriptions.class);

    private final SubscriptionsService subscriptionsService;

    private final int minSubscriptions;

    private final String nextEventName;
    private final String eventNameSubscriptionsBatch;

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
    public PrepareSubscriptions(SubscriptionsService subscriptionsService,
            String eventNameSubscriptionsBatch, String nextEventName,
            int minSubscriptions)
    {
        this.subscriptionsService = subscriptionsService;
        this.eventNameSubscriptionsBatch = eventNameSubscriptionsBatch;
        this.nextEventName = nextEventName;
        this.minSubscriptions = minSubscriptions;

        // validate arguments
        Util.checkArgumentNotNull(subscriptionsService, "subscriptionsService");
        Util.checkArgumentNotNull(eventNameSubscriptionsBatch,
                "eventNameSubscriptionsBatch");
        Util.checkArgumentNotNull(minSubscriptions, "minSubscriptions");
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
        try
        {
            String msg = null;
            List<Event> nextEvents = new LinkedList<>();

            long numSubscriptions = subscriptionsService
                    .countSubscriptions(DataCreationState.Created);
            if (minSubscriptions <= numSubscriptions)
            {
                msg = "Prepared subscriptions, nothing to do";
                Event newEvent = new Event(nextEventName, null);
                nextEvents.add(newEvent);
            }
            else
            {
                int count = (int) (minSubscriptions - numSubscriptions); // TODO
                int batchSize = 20;
                int numBatches = (int) count / batchSize; // TODO
                SubscriptionBatchData subscriptionBatchData = new SubscriptionBatchData(
                        0, batchSize, numBatches, null, nextEventName, null);
                Event newEvent = new Event(eventNameSubscriptionsBatch,
                        subscriptionBatchData.toDBObject());
                nextEvents.add(newEvent);

                msg = "Prepared " + count + " subscriptions.";
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
            logger.error("Error preparing desktop sync clients. ", e);
            throw e;
        }
    }
}

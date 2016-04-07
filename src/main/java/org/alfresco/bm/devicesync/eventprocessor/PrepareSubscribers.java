package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.dao.SubscribersService;
import org.alfresco.bm.devicesync.data.SubscriberBatchData;
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
public class PrepareSubscribers extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(PrepareSubscribers.class);

    private final int minSubscribers;

    private final SubscribersService subscribersService;

    private final String nextEventName;
    private final String eventNameSubscribersBatch;

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
    public PrepareSubscribers(SubscribersService subscribersService,
            String eventNameSubscribersBatch, String nextEventName,
            int minSubscribers)
    {
        this.subscribersService = subscribersService;
        this.eventNameSubscribersBatch = eventNameSubscribersBatch;
        this.nextEventName = nextEventName;
        this.minSubscribers = minSubscribers;

        // validate arguments
        Util.checkArgumentNotNull(subscribersService, "subscribersService");
        Util.checkArgumentNotNull(eventNameSubscribersBatch,
                "eventNameSubscribersBatch");
        Util.checkArgumentNotNull(nextEventName, "nextEventName");
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
        try
        {
            String msg = null;
            List<Event> nextEvents = new LinkedList<>();

            long numSubscribers = subscribersService.countSubscribers(null);
            if (minSubscribers <= numSubscribers)
            {
                msg = "Prepared subscribers, will now prepare subscriptions.";
                Event newEvent = new Event(nextEventName, null);
                nextEvents.add(newEvent);
            }
            else
            {
                long count = minSubscribers - numSubscribers;
                int batchSize = 20;
                int numBatches = (int) count / batchSize; // TODO
                SubscriberBatchData subscriberBatchData = new SubscriberBatchData(
                        0, batchSize, numBatches, null, nextEventName);
                Event newEvent = new Event(eventNameSubscribersBatch,
                        subscriberBatchData.toDBObject());
                nextEvents.add(newEvent);

                msg = "Prepared " + count + " subscribers.";
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

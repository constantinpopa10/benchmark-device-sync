package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.data.CollectStatsBatchData;
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
public class CollectStatsBatch extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(CollectStatsBatch.class);

    private final String eventNameCollectStats;

    private final int numBatches;
    private final int waitTimeBetweenBatches; // ms

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
    public CollectStatsBatch(int numBatches, int waitTimeBetweenBatches,
            String eventNameCollectStats)
    {
        this.eventNameCollectStats = eventNameCollectStats;
        this.numBatches = numBatches;
        this.waitTimeBetweenBatches = waitTimeBetweenBatches;
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
        DBObject dbObject = (DBObject) event.getData();
        CollectStatsBatchData collectStatsBatchData = CollectStatsBatchData
                .fromDBObject(dbObject);
        int count = collectStatsBatchData.getCount();

        try
        {
            List<Event> nextEvents = new LinkedList<>();
            String msg = null;

            if (count >= numBatches)
            {
                msg = "Hit number of batches, stopping.";
            }
            else
            {
                {
                    Event nextEvent = new Event(eventNameCollectStats,
                            System.currentTimeMillis(), null);
                    nextEvents.add(nextEvent);
                }

                {
                    long scheduledTime = System.currentTimeMillis()
                            + waitTimeBetweenBatches;
                    CollectStatsBatchData newCollectStatsBatchData = new CollectStatsBatchData(
                            count + 1);
                    Event nextEvent = new Event(event.getName(), scheduledTime,
                            newCollectStatsBatchData.toDBObject());
                    nextEvents.add(nextEvent);
                }

                msg = "Created collect stats";
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

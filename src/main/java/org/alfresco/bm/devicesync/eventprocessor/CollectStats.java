package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.dao.MetricsService;
import org.alfresco.bm.devicesync.util.ActiveMQMonitor;
import org.alfresco.bm.devicesync.util.ActiveMQMonitor.ActiveMQStats;
import org.alfresco.bm.devicesync.util.ActiveMQMonitor.BrokerStats;
import org.alfresco.bm.devicesync.util.ActiveMQMonitor.DestinationStats;
import org.alfresco.bm.devicesync.util.PublicApiFactory;
import org.alfresco.bm.devicesync.util.Util;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.social.alfresco.api.Alfresco;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * 
 * @author sglover
 * @since 1.0
 */
public class CollectStats extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(CollectStats.class);

    private final PublicApiFactory publicApiFactory;
    private final MetricsService metricsService;
    private final ActiveMQMonitor activeMQMonitor;

    /**
     * Constructor
     * 
     */
    public CollectStats(PublicApiFactory publicApiFactory,
            MetricsService metricsService, ActiveMQMonitor activeMQMonitor)
    {
        this.publicApiFactory = publicApiFactory;
        this.metricsService = metricsService;
        this.activeMQMonitor = activeMQMonitor;

        // validate arguments
        Util.checkArgumentNotNull(publicApiFactory, "publicApiFactory");
        Util.checkArgumentNotNull(metricsService, "metricsService");
        Util.checkArgumentNotNull(activeMQMonitor, "activeMQMonitor");
    }

    private Alfresco getAlfresco(String username)
    {
        Alfresco alfresco = publicApiFactory.getAdminPublicApi();
        return alfresco;
    }

    private DBObject toDBObject(DestinationStats stats)
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder
                .start("destType", stats.getDestinationType())
                .add("destName", stats.getDestinationName())
                .add("avgEnqueueTime", stats.getAverageEnqueueTime())
                .add("enqueueCount", stats.getEnqueueCount())
                .add("dequeueCount", stats.getDequeueCount())
                .add("queueSize", stats.getQueueSize())
                .add("avgBlockedTime", stats.getAverageBlockedTime())
                .add("maxEnqueueTime", stats.getMaxEnqueueTime())
                .add("blockedSends", stats.getBlockedSends())
                .add("dispatchCount", stats.getDispatchCount())
                .add("memoryPercentUsage", stats.getMemoryPercentUsage());
        return builder.get();
    }

    private DBObject toDBObject(BrokerStats stats)
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder
                .start("memPercentUsage", stats.getMemoryPercentUsage())
                .add("storePercentUsage", stats.getStorePercentUsage())
                .add("tempPercentUsage", stats.getTempPercentUsage());
        return builder.get();
    }

    private DBObject toDBObject(ActiveMQStats stats)
    {
        BasicDBObjectBuilder destStatsBuilder = BasicDBObjectBuilder.start();
        BrokerStats brokerStats = stats.getBrokerStats();
        for (DestinationStats destStats : stats.getDestinationStats())
        {
            DBObject destStatsDBObject = toDBObject(destStats);
            String destinationName = destStats.getDestinationName().replaceAll(
                    "\\.", "-");
            destStatsBuilder.add(destinationName, destStatsDBObject);
        }
        DBObject brokerStatsDBObject = toDBObject(brokerStats);
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start(
                "brokerStats", brokerStatsDBObject).add("destinationStats",
                destStatsBuilder.get());
        return builder.get();
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
        super.suspendTimer();

        try
        {
            Alfresco alfresco = getAlfresco("admin");

            super.resumeTimer();
            String metrics = alfresco.metrics("-default-");
            ActiveMQStats activeMQStats = activeMQMonitor.getStats();
            super.suspendTimer();

            DBObject activeMQStatsDBObject = toDBObject(activeMQStats);
            DBObject metricsDBObject = (DBObject) JSON.parse(metrics);

            metricsService.addMetrics(metricsDBObject, activeMQStatsDBObject);

            List<Event> nextEvents = new LinkedList<>();
            String msg = "Got metrics";

            EventResult result = new EventResult(msg, nextEvents);

            if (logger.isDebugEnabled())
            {
                logger.debug(msg);
            }

            return result;
        }
        catch (Exception e)
        {
            logger.error("Error creating subscriber. ", e);
            throw e;
        }
    }
}

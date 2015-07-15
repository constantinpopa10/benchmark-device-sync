package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.dao.MetricsService;
import org.alfresco.bm.devicesync.util.ActiveMQMonitor;
import org.alfresco.bm.devicesync.util.ActiveMQMonitor.ActiveMQStats;
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
    public CollectStats(PublicApiFactory publicApiFactory, MetricsService metricsService, ActiveMQMonitor activeMQMonitor)
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
    		.add("dispatchCount", stats.getDispatchCount());
    	return builder.get();
    }

    private DBObject toDBObject(ActiveMQStats stats)
    {
    	List<DBObject> list = new LinkedList<>();
    	for(DestinationStats destStats : stats.getDestinationStats())
    	{
    		list.add(toDBObject(destStats));
    	}
    	BasicDBObjectBuilder builder = BasicDBObjectBuilder
    			.start("stats", list);
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
    		String syncMetrics = alfresco.syncMetrics("-default-");
    		String subsMetrics = alfresco.subsMetrics("-default-");
    		ActiveMQStats stats = activeMQMonitor.getStats();
    		super.suspendTimer();

    		DBObject activeMQStatsDBObject = toDBObject(stats);
    		DBObject syncDBObject = (DBObject)JSON.parse(syncMetrics);
    		DBObject subsDBObject = (DBObject)JSON.parse(subsMetrics);

    		metricsService.addMetrics(syncDBObject, subsDBObject, activeMQStatsDBObject);

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

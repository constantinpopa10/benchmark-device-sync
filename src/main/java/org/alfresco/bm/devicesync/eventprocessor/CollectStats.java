package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.dao.MetricsService;
import org.alfresco.bm.devicesync.util.PublicApiFactory;
import org.alfresco.bm.devicesync.util.Util;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.social.alfresco.api.Alfresco;

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

    /**
     * Constructor 
     * 
     * @param siteDataService_p             Site Data service to retrieve site information from Mongo
     * @param userDataService_p             User Data service to retrieve user information from Mongo
     * @param desktopSyncClientRegistry_p   Registry to create the clients 
     * @param numberOfClients_p             Number of clients to create
     * @param nextEventId_p                 ID of the next event
     */
    public CollectStats(PublicApiFactory publicApiFactory, MetricsService metricsService)
    {
        this.publicApiFactory = publicApiFactory;
        this.metricsService = metricsService;

        // validate arguments
        Util.checkArgumentNotNull(publicApiFactory, "publicApiFactory");
        Util.checkArgumentNotNull(metricsService, "metricsService");
    }

    private Alfresco getAlfresco(String username)
    {
    	Alfresco alfresco = publicApiFactory.getAdminPublicApi();
    	return alfresco;
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
        try
        {
    		Alfresco alfresco = getAlfresco("admin");

    		String syncMetrics = alfresco.syncMetrics("-default-");
    		String subsMetrics = alfresco.subsMetrics("-default-");
    		DBObject syncDBObject = (DBObject)JSON.parse(syncMetrics);
    		DBObject subsDBObject = (DBObject)JSON.parse(subsMetrics);

    		metricsService.addMetrics(syncDBObject, subsDBObject);

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

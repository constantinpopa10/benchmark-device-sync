package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.dao.SubscribersService;
import org.alfresco.bm.devicesync.data.SubscriberData;
import org.alfresco.bm.devicesync.util.PublicApiFactory;
import org.alfresco.bm.devicesync.util.Util;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.social.alfresco.api.Alfresco;

/**
 * 
 * @author sglover
 * @since 1.0
 */
public class DeleteSubscriber extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(DeleteSubscriber.class);

    private final SubscribersService subscribersService;
    private final PublicApiFactory publicApiFactory;

    /**
     * Constructor 
     * 
     * @param siteDataService_p             Site Data service to retrieve site information from Mongo
     * @param userDataService_p             User Data service to retrieve user information from Mongo
     * @param desktopSyncClientRegistry_p   Registry to create the clients 
     * @param numberOfClients_p             Number of clients to create
     * @param nextEventId_p                 ID of the next event
     */
    public DeleteSubscriber(SubscribersService subscribersService, PublicApiFactory publicApiFactory)
    {
    	this.subscribersService = subscribersService;
        this.publicApiFactory = publicApiFactory;

        // validate arguments
        Util.checkArgumentNotNull(subscribersService, "subscribersService");
        Util.checkArgumentNotNull(publicApiFactory, "publicApiFactory");
    }

    private Alfresco getAlfresco(String username)
    {
    	Alfresco alfresco = publicApiFactory.getPublicApi(username);
    	return alfresco;
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
        try
        {
        	String username = (String)event.getData();

    		Alfresco alfresco = getAlfresco(username);

    		SubscriberData subscriberData = subscribersService.getRandomSubscriber(username);
    		String subscriberId = subscriberData.getSubscriberId();
    		alfresco.removeSubscriber("-default-", subscriberId);
        	subscribersService.removeSubscriber(subscriberId);

            List<Event> nextEvents = new LinkedList<>();
        	String msg = "Removed subscriber " + subscriberData;

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

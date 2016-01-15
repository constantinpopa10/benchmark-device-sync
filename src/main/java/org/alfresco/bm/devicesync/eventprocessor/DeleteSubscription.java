package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.SubscriptionData;
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
public class DeleteSubscription extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(DeleteSubscription.class);

    private final SubscriptionsService subscriptionsService;
    private final PublicApiFactory publicApiFactory;

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
    public DeleteSubscription(SubscriptionsService subscriptionsService,
            PublicApiFactory publicApiFactory)
    {
        this.subscriptionsService = subscriptionsService;
        this.publicApiFactory = publicApiFactory;

        // validate arguments
        Util.checkArgumentNotNull(subscriptionsService, "subscriptionsService");
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
        super.suspendTimer();

        try
        {
            String username = (String) event.getData();
            SubscriptionData subscriptionData = subscriptionsService
                    .getRandomSubscription(username);
            String subscriberId = subscriptionData.getSubscriberId();
            String subscriptionId = subscriptionData.getSubscriptionId();

            Alfresco alfresco = getAlfresco(username);

            super.resumeTimer();
            alfresco.removeSubscription("-default-", subscriberId,
                    subscriptionId);
            super.suspendTimer();

            subscriptionsService.removeSubscription(subscriptionId);

            List<Event> nextEvents = new LinkedList<>();
            String msg = "Removed subscription " + subscriptionData;

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

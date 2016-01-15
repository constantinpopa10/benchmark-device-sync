package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscribersService;
import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.SubscriberData;
import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.devicesync.util.PublicApiFactory;
import org.alfresco.bm.devicesync.util.Util;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.alfresco.bm.site.SiteData;
import org.alfresco.bm.site.SiteDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.social.alfresco.api.Alfresco;
import org.springframework.social.alfresco.api.entities.Subscription;
import org.springframework.social.alfresco.api.entities.SubscriptionType;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 * @since 1.0
 */
public class CreateSubscription extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(CreateSubscription.class);

    private final SiteDataService siteDataService;
    private final SubscribersService subscribersService;
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
    public CreateSubscription(SiteDataService siteDataService,
            SubscribersService subscribersService,
            SubscriptionsService subscriptionsService,
            PublicApiFactory publicApiFactory)
    {
        this.siteDataService = siteDataService;
        this.subscribersService = subscribersService;
        this.subscriptionsService = subscriptionsService;
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
        super.suspendTimer();

        try
        {
            DBObject dbObject = (DBObject) event.getData();
            String username = null;
            String subscriberId = null;
            String siteId = null;
            String targetPath = null;

            if (dbObject != null)
            {
                SubscriptionData subscriptionData = SubscriptionData
                        .fromDBObject(dbObject);
                username = subscriptionData.getUsername();
                subscriberId = subscriptionData.getSubscriberId();
                siteId = subscriptionData.getSiteId();
            }
            else
            {
                SubscriberData subscriberData = subscribersService
                        .getRandomSubscriber(null);
                subscriberId = subscriberData.getSubscriberId();
                username = subscriberData.getUsername();
            }

            Alfresco alfresco = getAlfresco(username);

            if (siteId == null)
            {
                SiteData siteData = siteDataService.randomSite("default",
                        DataCreationState.Created);
                siteId = siteData.getSiteId();
                targetPath = siteData.getPath();
            }

            if (targetPath == null || targetPath.equals(""))
            {
                targetPath = "/Company Home/Sites/" + siteId
                        + "/documentLibrary";
            }

            super.resumeTimer();
            Subscription subscription = alfresco.createSubscription(
                    "-default-", subscriberId, SubscriptionType.BOTH,
                    targetPath);
            super.suspendTimer();
            subscriptionsService.addSubscription(siteId, username,
                    subscriberId, subscription.getId(),
                    SubscriptionType.BOTH.toString(), targetPath,
                    DataCreationState.Created);

            List<Event> nextEvents = new LinkedList<>();
            String msg = "Created subscription " + subscription;

            EventResult result = new EventResult(msg, nextEvents);

            if (logger.isDebugEnabled())
            {
                logger.debug(msg);
            }

            return result;
        }
        catch (Exception e)
        {
            logger.error("Error creating subscription. ", e);
            throw e;
        }
    }
}

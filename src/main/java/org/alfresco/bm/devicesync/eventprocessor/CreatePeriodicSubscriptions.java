package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscribersService;
import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.CreatePeriodicSubscriptionsData;
import org.alfresco.bm.devicesync.data.SubscriberData;
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
public class CreatePeriodicSubscriptions extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory
            .getLog(CreatePeriodicSubscriptions.class);

    /** Stores the site data service */
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
    public CreatePeriodicSubscriptions(SubscribersService subscribersService,
            SubscriptionsService subscriptionsService,
            SiteDataService siteDataService, PublicApiFactory publicApiFactory)
    {
        this.subscribersService = subscribersService;
        this.subscriptionsService = subscriptionsService;
        this.siteDataService = siteDataService;
        this.publicApiFactory = publicApiFactory;

        // validate arguments
        Util.checkArgumentNotNull(subscribersService, "subscribersService");
        Util.checkArgumentNotNull(subscriptionsService, "subscriptionsService");
        Util.checkArgumentNotNull(siteDataService, "siteDataService");
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
        DBObject dbObject = (DBObject) event.getData();
        CreatePeriodicSubscriptionsData preparePeriodicSubscriptionsData = CreatePeriodicSubscriptionsData
                .fromDBObject(dbObject);
        int numSubscriptionsPerBatch = preparePeriodicSubscriptionsData
                .getNumSubscriptionsPerBatch();
        int timeBetweenBatches = preparePeriodicSubscriptionsData
                .getTimeBetweenBatches();
        int max = preparePeriodicSubscriptionsData.getMax();
        int count = preparePeriodicSubscriptionsData.getCount();

        try
        {
            for (long i = 0; i < numSubscriptionsPerBatch; i++)
            {
                SubscriptionType subscriptionType = SubscriptionType.BOTH;
                SubscriberData subscriberData = subscribersService
                        .getRandomSubscriber(null);
                String username = subscriberData.getUsername();
                Alfresco alfresco = getAlfresco(username);
                SiteData siteData = siteDataService.randomSite("default",
                        DataCreationState.Created);
                String path = siteData.getPath();
                if (path == null || path.equals(""))
                {
                    path = "/Company Home/Sites/" + siteData.getSiteId()
                            + "/documentLibrary";
                }
                Subscription subscription = alfresco.createSubscription(
                        "-default-", subscriberData.getSubscriberId(),
                        subscriptionType, path);
                subscriptionsService.addSubscription(username,
                        subscriberData.getSubscriberId(), subscription.getId(),
                        subscriptionType.toString(), path,
                        DataCreationState.Created);
            }

            long scheduledTime = System.currentTimeMillis()
                    + timeBetweenBatches;
            CreatePeriodicSubscriptionsData newPreparePeriodicSubscriptionsData = new CreatePeriodicSubscriptionsData(
                    numSubscriptionsPerBatch, timeBetweenBatches, max,
                    count + 1);
            Event nextEvent = new Event("createPeriodicSubscriptions",
                    scheduledTime, newPreparePeriodicSubscriptionsData);
            List<Event> nextEvents = new LinkedList<>();
            nextEvents.add(nextEvent);
            String msg = "Prepared " + numSubscriptionsPerBatch
                    + " subscriptions and raising '" + event.getName()
                    + "' event.";

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

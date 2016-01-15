package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.data.DataCreationState;
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
import org.springframework.social.alfresco.api.entities.Subscriber;

/**
 * 
 * @author sglover
 * @since 1.0
 */
public class CreateSubscribers extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(CreateSubscribers.class);

    private final SubscribersService subscribersService;

    private final String eventNamePrepareSubscriptions;
    private final String eventNameCreateSubscribers;

    private final int batchSize;

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
    public CreateSubscribers(SubscribersService subscribersService,
            PublicApiFactory publicApiFactory, int batchSize,
            String eventNamePrepareSubscriptions,
            String eventNameCreateSubscribers)
    {
        this.subscribersService = subscribersService;
        this.batchSize = batchSize;
        this.publicApiFactory = publicApiFactory;
        this.eventNamePrepareSubscriptions = eventNamePrepareSubscriptions;
        this.eventNameCreateSubscribers = eventNameCreateSubscribers;

        // validate arguments
        Util.checkArgumentNotNull(subscribersService, "subscribersService");
        Util.checkArgumentNotNull(eventNameCreateSubscribers,
                "eventNameCreateSubscribers");
        Util.checkArgumentNotNull(eventNamePrepareSubscriptions,
                "eventNamePrepareSubscriptions");
        Util.checkArgumentNotNull(publicApiFactory, "publicApiFactory");
    }

    private Alfresco getAlfresco(String username)
    {
        Alfresco alfresco = publicApiFactory.getPublicApi(username);
        return alfresco;
    }

    @Override
    protected EventResult processEvent(Event event_p) throws Exception
    {
        try
        {
            String msg = null;
            List<Event> nextEvents = new LinkedList<>();

            long numSubscribers = subscribersService
                    .countSubscribers(DataCreationState.Scheduled);
            if (numSubscribers == 0)
            {
                long time = System.currentTimeMillis() + 5000L;
                Event nextEvent = new Event(eventNamePrepareSubscriptions,
                        time, null);
                nextEvents.add(nextEvent);
                msg = "Created all subscribers and raising '"
                        + nextEvent.getName() + "' event.";
            }
            else
            {
                List<SubscriberData> subscribers = subscribersService
                        .getSubscribers(DataCreationState.Scheduled, 0,
                                batchSize);
                subscribers.stream().forEach(
                        subscriberData -> {
                            String username = subscriberData.getUsername();
                            Alfresco alfresco = getAlfresco(username);
                            try
                            {
                                Subscriber subscriber = alfresco
                                        .createSubscriber("-default-", "test");
                                subscribersService.updateSubscriber(
                                        subscriberData.getObjectId(),
                                        subscriber.getId(),
                                        DataCreationState.Created);
                            }
                            catch (Exception e)
                            {
                                subscribersService.updateSubscriber(
                                        subscriberData.getObjectId(),
                                        DataCreationState.Failed);
                            }
                        });
                // .forEach(subscriberData -> {
                // subscribersService.updateSubscriber(subscriberData.getObjectId(),
                // subscriberData.getSubscriberId(),
                // DataCreationState.Created);
                // });

                long time = System.currentTimeMillis() + 5000L;
                Event nextEvent = new Event(eventNameCreateSubscribers, time,
                        null);
                nextEvents.add(nextEvent);
                msg = "Created " + subscribers.size()
                        + " subscribers and raising '" + nextEvent.getName()
                        + "' event.";
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
            logger.error("Error creating subscribers. ", e);
            throw e;
        }
    }
}

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
import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.social.alfresco.api.Alfresco;
import org.springframework.social.alfresco.api.entities.Subscriber;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 * @since 1.0
 */
public class CreateSubscriber extends AbstractEventProcessor
{
    /** Logger for the class */
    private static Log logger = LogFactory.getLog(CreateSubscriber.class);

    private final UserDataService userDataService;
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
    public CreateSubscriber(UserDataService userDataService, SubscribersService subscribersService,
    		PublicApiFactory publicApiFactory)
    {
    	this.userDataService = userDataService;
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
    	super.suspendTimer();

        try
        {
        	DBObject dbObject = (DBObject)event.getData();
        	String username = null;

        	if(dbObject != null)
        	{
            	SubscriberData subscriberData = SubscriberData
            			.fromDBObject(dbObject);
            	username = subscriberData.getUsername();
        	}
        	else
        	{
        		UserData userData = userDataService.getRandomUser();
        		username = userData.getUsername();
        	}

    		Alfresco alfresco = getAlfresco(username);

        	super.resumeTimer();
    		Subscriber subscriber = alfresco.createSubscriber("-default-", "test");
        	super.suspendTimer();

        	String subscriberId = subscriber.getId();
    		String syncServiceURI = subscriber.getSyncService().getUri();
        	subscribersService.addSubscriber(username, subscriberId, syncServiceURI,
        			DataCreationState.Created);

            List<Event> nextEvents = new LinkedList<>();
        	String msg = "Created subscriber " + subscriber;

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

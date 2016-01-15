package org.alfresco.bm.devicesync;

import static org.alfresco.bm.devicesync.data.PrepareSubscriptionsData.FIELD_MAX_SUBSCRIPTIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.mongo.MongoSubscribersService;
import org.alfresco.bm.devicesync.dao.mongo.MongoSubscriptionsService;
import org.alfresco.bm.devicesync.dao.mongo.MongoSyncsService;
import org.alfresco.bm.devicesync.data.SyncState;
import org.alfresco.bm.devicesync.eventprocessor.BatchExecuteSyncs;
import org.alfresco.bm.devicesync.eventprocessor.CreateSubscribers;
import org.alfresco.bm.devicesync.eventprocessor.CreateSubscriptions;
import org.alfresco.bm.devicesync.eventprocessor.PrepareBatchSyncs;
import org.alfresco.bm.devicesync.eventprocessor.PrepareSubscribers;
import org.alfresco.bm.devicesync.eventprocessor.PrepareSubscriptions;
import org.alfresco.bm.devicesync.util.BasicAuthPublicApiFactory;
import org.alfresco.bm.devicesync.util.PublicApiFactory;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.alfresco.bm.session.MongoSessionService;
import org.alfresco.bm.site.SiteData;
import org.alfresco.bm.site.SiteDataServiceImpl;
import org.alfresco.bm.site.SiteVisibility;
import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataServiceImpl;
import org.alfresco.service.common.mongo.MongoDbFactory;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.social.alfresco.api.CMISEndpoint;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

/**
 * 
 * @author sglover
 *
 */
public class DeviceSyncTest
{
    private static MongodForTestsFactory mongoFactory;

    private DB db;

    private MongoSessionService sessionService;
    private UserDataServiceImpl userDataService;
    private MongoSubscribersService subscribersService;
    private MongoSubscriptionsService subscriptionsService;
    private PublicApiFactory publicApiFactory;
    private SiteDataServiceImpl siteDataService;
    private MongoSyncsService syncsService;

    private StopWatch stopWatch;

    private int numSubscribers = 100;

    @Before
    public void before() throws Exception
    {
        this.stopWatch = new StopWatch();

        long time = System.currentTimeMillis();

        final MongoDbFactory factory = new MongoDbFactory();
        boolean useEmbeddedMongo = ("true".equals(System
                .getProperty("useEmbeddedMongo")) ? true : false);
        if (useEmbeddedMongo)
        {
            mongoFactory = MongodForTestsFactory.with(Version.Main.PRODUCTION);
            final Mongo mongo = mongoFactory.newMongo();
            factory.setMongo(mongo);
        }
        else
        {
            factory.setMongoURI("mongodb://127.0.0.1:27017");
            factory.setDbName("test");
        }
        this.db = factory.createInstance();

        sessionService = new MongoSessionService(db, "sessions" + time);
        sessionService.start();

        userDataService = new UserDataServiceImpl(db, "users" + time);
        userDataService.afterPropertiesSet();

        subscribersService = new MongoSubscribersService(db, "subscribers"
                + time);
        subscribersService.afterPropertiesSet();

        subscriptionsService = new MongoSubscriptionsService(db,
                "subscriptions" + time);
        subscriptionsService.afterPropertiesSet();

        syncsService = new MongoSyncsService(db, "syncs" + time);
        syncsService.afterPropertiesSet();

        CMISEndpoint cmisEndpoint = new CMISEndpoint(BindingType.BROWSER,
                CmisVersion.CMIS_1_1);
        publicApiFactory = new BasicAuthPublicApiFactory("http", "localhost",
                8080, "https", "localhost", 9090, cmisEndpoint, 10, 5000, 5000,
                5000, userDataService, "alfresco", "api", "service");

        UserData userData = new UserData();
        userData.setDomain("default");
        userData.setUsername("admin");
        userData.setPassword("admin");
        userData.setEmail("steven.glover@alfresco.com");
        userData.setCreationState(DataCreationState.Created);
        userDataService.createNewUser(userData);

        siteDataService = new SiteDataServiceImpl(db, "sites" + time,
                "siteMembers" + time);
        siteDataService.afterPropertiesSet();
        SiteData newSite = new SiteData();
        newSite.setCreationState(DataCreationState.Created);
        newSite.setDescription("");
        newSite.setSiteId("site1");
        newSite.setSitePreset("preset");
        newSite.setTitle("site1");
        newSite.setVisibility(SiteVisibility.getRandomVisibility());
        newSite.setType("{http://www.alfresco.org/model/site/1.0}site");
        newSite.setDomain("default");
        siteDataService.addSite(newSite);

        // Alfresco alfresco = publicApiFactory.getAdminPublicApi();
        // alfresco.

        // for(int i = 0; i < 500; i++)
        // {
        // UserData userData = new UserData();
        // userData.setDomain("default");
        // userData.setUsername("username" + i);
        // userData.setPassword("username" + i);
        // userData.setEmail("username" + i + "@alfresco.com");
        // userData.setCreationState(DataCreationState.Created);
        // userDataService.createNewUser(userData);
        // }
    }

    @After
    public void after() throws Exception
    {
        if (mongoFactory != null)
        {
            mongoFactory.shutdown();
        }
    }

    @Test
    public void test1() throws Exception
    {
        // {
        // CreateUsers createUsers = new CreateUsers(userDataService,
        // numSubscribers);
        //
        // Event event = new Event("createUsers", null);
        // EventResult result = createUsers.processEvent(event, stopWatch);
        // assertTrue(result.isSuccess());
        // assertEquals(numSubscribers, userDataService.countUsers("default",
        // DataCreationState.Created));
        // }
        //
        // stopWatch.reset();
        //
        // {
        // PrepareSites prepareSites = new PrepareSite(userDataService,
        // numSubscribers);
        //
        // Event event = new Event("createUsers", null);
        // EventResult result = createUsers.processEvent(event, stopWatch);
        // assertTrue(result.isSuccess());
        // assertEquals(numSubscribers, userDataService.countUsers("default",
        // DataCreationState.Created));
        // }
        //
        // stopWatch.reset();

        {
            PrepareSubscribers prepareSubscribers = new PrepareSubscribers(
                    subscribersService, "", "", numSubscribers);

            Event event = new Event("prepareSubscribers", null);
            EventResult result = prepareSubscribers.processEvent(event,
                    stopWatch);
            assertTrue(result.isSuccess());
            assertEquals(numSubscribers,
                    subscribersService
                            .countSubscribers(DataCreationState.Scheduled));
        }

        stopWatch.reset();

        {
            CreateSubscribers createSubscribers = new CreateSubscribers(
                    subscribersService, publicApiFactory, 10, "", "");

            Event event = new Event("createSubscribers", null);
            EventResult result = createSubscribers.processEvent(event,
                    stopWatch);
            assertTrue(result.isSuccess());
            assertEquals(10,
                    subscribersService
                            .countSubscribers(DataCreationState.Created));
        }

        stopWatch.reset();

        {
            PrepareSubscribers prepareSubscribers = new PrepareSubscribers(
                    subscribersService, "", "", numSubscribers + 10);

            Event event = new Event("prepareSubscribers1", null);
            EventResult result = prepareSubscribers.processEvent(event,
                    stopWatch);
            assertTrue(result.isSuccess());
            assertEquals(numSubscribers,
                    subscribersService
                            .countSubscribers(DataCreationState.Scheduled));
        }

        stopWatch.reset();

        {
            CreateSubscribers createSubscribers = new CreateSubscribers(
                    subscribersService, publicApiFactory, 10, "", "");

            Event event = new Event("createSubscribers1", null);
            EventResult result = createSubscribers.processEvent(event,
                    stopWatch);
            assertTrue(result.isSuccess());
            assertEquals(20,
                    subscribersService
                            .countSubscribers(DataCreationState.Created));
        }

        stopWatch.reset();

        {
            PrepareSubscriptions prepareSubscriptions = new PrepareSubscriptions(
                    subscriptionsService, "", "", 10); // maxSubscriptions
                                                       // should be ignored in
                                                       // favour of the event
                                                       // parameter

            DBObject dbObject = BasicDBObjectBuilder.start(
                    FIELD_MAX_SUBSCRIPTIONS, numSubscribers + 10).get();
            Event event = new Event("prepareSubscriptions1", dbObject);
            EventResult result = prepareSubscriptions.processEvent(event,
                    stopWatch);
            assertTrue(result.isSuccess());
            assertEquals(10,
                    subscriptionsService
                            .countSubscriptions(DataCreationState.Scheduled));
        }

        stopWatch.reset();

        {
            CreateSubscriptions createSubscriptions = new CreateSubscriptions(
                    subscriptionsService, publicApiFactory, 10, "", "");

            Event event = new Event("createSubscriptions1", null);
            EventResult result = createSubscriptions.processEvent(event,
                    stopWatch);
            assertTrue(result.isSuccess());
            assertEquals(10,
                    subscriptionsService
                            .countSubscriptions(DataCreationState.Created));
        }

        stopWatch.reset();

        {
            PrepareBatchSyncs prepareSyncs = new PrepareBatchSyncs(
                    subscriptionsService, syncsService, 10, "");

            Event event = new Event("prepareSyncs1", null);
            EventResult result = prepareSyncs.processEvent(event, stopWatch);
            assertTrue(result.isSuccess());
            assertEquals(10, syncsService.countSyncs(SyncState.NotScheduled));
        }

        stopWatch.reset();

        {
            BatchExecuteSyncs executeSyncs = new BatchExecuteSyncs(
                    sessionService, syncsService, publicApiFactory, 10, 3000);

            Event event = new Event("executeSyncs1", null);
            EventResult result = executeSyncs.processEvent(event, stopWatch);
            assertTrue(result.isSuccess());
            assertEquals(10, syncsService.countSyncs(SyncState.Ready));
        }
    }
}

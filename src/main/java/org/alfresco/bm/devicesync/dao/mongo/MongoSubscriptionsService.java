package org.alfresco.bm.devicesync.dao.mongo;

import static org.alfresco.bm.devicesync.data.SubscriptionData.*;
import static org.alfresco.bm.devicesync.data.SubscriptionData.FIELD_SITE_ID;
import static org.alfresco.bm.devicesync.data.SubscriptionData.FIELD_STATE;
import static org.alfresco.bm.devicesync.data.SubscriptionData.FIELD_SUBSCRIPTION_ID;
import static org.alfresco.bm.devicesync.data.SubscriptionData.FIELD_USERNAME;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.user.UserDataServiceImpl.Range;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.InitializingBean;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;

/**
 * 
 * @author sglover
 *
 */
public class MongoSubscriptionsService implements SubscriptionsService,
        InitializingBean
{
    /** The collection of users, which can be reused by derived extensions. */
    protected final DBCollection collection;

    public MongoSubscriptionsService(DB db, String collection)
    {
        this.collection = db.getCollection(collection);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        checkIndexes();
    }

    /**
     * Ensure that the MongoDB collection has the required indexes associated
     * with this user bean.
     */
    private void checkIndexes()
    {
        collection.setWriteConcern(WriteConcern.SAFE);

        DBObject uidxUserName = BasicDBObjectBuilder.start(
                FIELD_SUBSCRIPTION_ID, 1).get();
        DBObject optUserName = BasicDBObjectBuilder
                .start("name", "uidxSubscriptionId")
                .add("unique", Boolean.TRUE).add("sparse", Boolean.TRUE).get();
        collection.createIndex(uidxUserName, optUserName);

        DBObject idxState = BasicDBObjectBuilder.start(FIELD_STATE, 1)
                .add(FIELD_RANDOMIZER, 2).add(FIELD_USERNAME, 3).get();
        DBObject optState = BasicDBObjectBuilder.start("name", "idxState")
                .add("unique", Boolean.FALSE).get();
        collection.createIndex(idxState, optState);

        DBObject idxSiteId = BasicDBObjectBuilder.start(FIELD_STATE, 1)
                .add(FIELD_SITE_ID, 2).add(FIELD_RANDOMIZER, 3).get();
        DBObject optSiteId = BasicDBObjectBuilder.start("name", "idxSiteId")
                .add("unique", Boolean.FALSE).get();
        collection.createIndex(idxSiteId, optSiteId);

        DBObject idxDomainRand = BasicDBObjectBuilder
                .start(FIELD_RANDOMIZER, 1).get();
        DBObject optDomainRand = BasicDBObjectBuilder.start("name", "idxRand")
                .add("unique", Boolean.FALSE).get();
        collection.createIndex(idxDomainRand, optDomainRand);
    }

    private Range getRandomizerRange(List<String> sites)
    {
        QueryBuilder queryObjBuilder = QueryBuilder.start();
        if (sites != null && sites.size() > 0)
        {
            queryObjBuilder.and("siteId").in(sites);
        }
        return getRandomizerRange(queryObjBuilder);
    }

    private Range getRandomizerRange(QueryBuilder queryObjBuilder)
    {
        DBObject queryObj = queryObjBuilder.get();

        DBObject fieldsObj = BasicDBObjectBuilder.start()
                .add("randomizer", Boolean.TRUE).get();

        DBObject sortObj = BasicDBObjectBuilder.start().add("randomizer", -1)
                .get();

        // Find max
        DBObject resultObj = collection.findOne(queryObj, fieldsObj, sortObj);
        int maxRandomizer = resultObj == null ? 0 : (Integer) resultObj
                .get("randomizer");

        // Find min
        sortObj.put("randomizer", +1);
        resultObj = collection.findOne(queryObj, fieldsObj, sortObj);
        int minRandomizer = resultObj == null ? 0 : (Integer) resultObj
                .get("randomizer");

        return new Range(minRandomizer, maxRandomizer);
    }

    private Range getRandomizerRange()
    {
        QueryBuilder queryObjBuilder = QueryBuilder.start();
        return getRandomizerRange(queryObjBuilder);
    }

    @Override
    public List<SubscriptionData> getSubscriptions(DataCreationState state,
            int skip, int count)
    {
        List<SubscriptionData> subscriptions = new LinkedList<>();

        DBObject query = QueryBuilder.start(FIELD_STATE).is(state.toString())
                .get();
        DBCursor cursor = collection.find(query).skip(skip).limit(count);
        for (DBObject dbObject : cursor)
        {
            SubscriptionData subscription = SubscriptionData
                    .fromDBObject(dbObject);
            subscriptions.add(subscription);
        }

        return subscriptions;
    }

    // @Override
    // public List<SubscriptionData> getRandomSubscriptions(DataCreationState
    // state, int skip, int count)
    // {
    // List<SubscriptionData> subscriptions = new LinkedList<>();
    //
    // DBObject query = QueryBuilder
    // .start(FIELD_STATE).is(state.toString())
    // .get();
    // DBCursor cursor = collection.find(query).skip(skip).limit(count);
    // for(DBObject dbObject : cursor)
    // {
    // SubscriptionData subscription = fromDBObject(dbObject);
    // subscriptions.add(subscription);
    // }
    //
    // return subscriptions;
    // }

    // private DBObject toDBObject(SubscriptionData subscriptionData)
    // {
    // BasicDBObjectBuilder builder = BasicDBObjectBuilder
    // .start(FIELD_USERNAME, subscriptionData.getUsername())
    // .add(FIELD_SUBSCRIBER_ID, subscriptionData.getSubscriberId())
    // .add(FIELD_SUBSCRIPTION_TYPE, subscriptionData.getSubscriptionType())
    // .add(FIELD_PATH, subscriptionData.getPath())
    // .add(FIELD_RANDOMIZER, subscriptionData.getRandomizer());
    // if(subscriptionData.getSubscriptionId() != null)
    // {
    // builder.add(FIELD_SUBSCRIPTION_ID, subscriptionData.getSubscriptionId());
    // }
    // if(subscriptionData.getState() != null)
    // {
    // builder.add(FIELD_STATE, subscriptionData.getState().toString());
    // }
    // DBObject dbObject = builder.get();
    // return dbObject;
    // }

    // private SubscriptionData fromDBObject(DBObject dbObject)
    // {
    // ObjectId id = (ObjectId)dbObject.get("_id");
    // String username = (String)dbObject.get(FIELD_USERNAME);
    // String subscriberId = (String)dbObject.get(FIELD_SUBSCRIBER_ID);
    // String subscriptionId = (String)dbObject.get(FIELD_SUBSCRIPTION_ID);
    // int randomizer = (Integer)dbObject.get(FIELD_RANDOMIZER);
    // DataCreationState state =
    // DataCreationState.valueOf((String)dbObject.get(FIELD_STATE));
    // String subscriptionType = (String)dbObject.get(FIELD_SUBSCRIPTION_TYPE);
    // String path = (String)dbObject.get(FIELD_PATH);
    // SubscriptionData subscriptionData = new SubscriptionData(id, username,
    // subscriberId, subscriptionId,
    // subscriptionType, path, state, randomizer);
    // return subscriptionData;
    // }

    @Override
    public void addSubscription(String siteId, String username,
            String subscriberId, String subscriptionId,
            String subscriptionType, String path, DataCreationState state)
    {
        SubscriptionData subscriptionData = new SubscriptionData(siteId,
                username, subscriberId, subscriptionId, subscriptionType, path,
                state);
        DBObject insert = subscriptionData.toDBObject();
        collection.insert(insert);
    }

    @Override
    public void addSubscription(String siteId, String username,
            String subscriberId, String subscriptionType, String path,
            DataCreationState state)
    {
        SubscriptionData subscriptionData = new SubscriptionData(siteId,
                username, subscriberId, subscriptionType, path, state);
        DBObject insert = subscriptionData.toDBObject();
        collection.insert(insert);
    }

    @Override
    public SubscriptionData getSubscription(String subscriptionId)
    {
        SubscriptionData subscription = null;

        DBObject query = QueryBuilder.start(FIELD_SUBSCRIPTION_ID)
                .is(subscriptionId).and(FIELD_STATE)
                .is(DataCreationState.Created.toString()).get();
        DBObject dbObject = collection.findOne(query);
        if (dbObject != null)
        {
            subscription = SubscriptionData.fromDBObject(dbObject);
        }

        return subscription;
    }

    @Override
    public void updateSubscription(ObjectId objectId, String subscriptionId,
            DataCreationState state)
    {
        DBObject query = QueryBuilder.start("_id").is(objectId).get();

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start(FIELD_STATE,
                state.toString());
        if (subscriptionId != null)
        {
            builder.add(FIELD_SUBSCRIPTION_ID, subscriptionId);
        }
        DBObject update = BasicDBObjectBuilder.start("$set", builder.get())
                .get();
        collection.update(query, update);
    }

    @Override
    public void updateSubscription(ObjectId objectId, DataCreationState state)
    {
        DBObject query = QueryBuilder.start("_id").is(objectId).get();
        DBObject update = BasicDBObjectBuilder
                .start("$set",
                        BasicDBObjectBuilder.start(FIELD_STATE,
                                state.toString()).get()).get();
        collection.update(query, update);
    }

    @Override
    public long countSubscriptions(DataCreationState state)
    {
        QueryBuilder builder = QueryBuilder.start();
        if (state != null)
        {
            builder.and(FIELD_STATE).is(state.toString());
        }
        DBObject query = builder.get();
        long count = collection.count(query);
        return count;
    }

    @Override
    public SubscriptionData getRandomSubscriptionInSite(String siteId)
    {
        Range range = getRandomizerRange(Arrays.asList(siteId));
        int upper = range.getMax();
        int lower = range.getMin();
        int random = lower + (int) (Math.random() * (double) (upper - lower));

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add(
                FIELD_STATE, DataCreationState.Created.toString());
        if (siteId != null)
        {
            builder.add(FIELD_SITE_ID, siteId);
        }
        builder.push(FIELD_RANDOMIZER).add("$gte", Integer.valueOf(random))
                .pop();
        DBObject queryObj = builder.get();

        DBObject dbObject = collection.findOne(queryObj);
        if (dbObject == null)
        {
            queryObj.put(FIELD_RANDOMIZER, new BasicDBObject("$lt", random));
            dbObject = collection.findOne(queryObj);
        }

        return SubscriptionData.fromDBObject(dbObject);
    }

    @Override
    public SubscriptionData getRandomSubscription(String username)
    {
        Range range = getRandomizerRange();
        int upper = range.getMax();
        int lower = range.getMin();
        int random = lower + (int) (Math.random() * (double) (upper - lower));

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start()
                .add(FIELD_STATE, DataCreationState.Created.toString())
                .push(FIELD_RANDOMIZER).add("$gte", Integer.valueOf(random))
                .pop();
        if (username != null)
        {
            builder.add(FIELD_USERNAME, username);
        }
        DBObject queryObj = builder.get();

        DBObject dbObject = collection.findOne(queryObj);
        if (dbObject == null)
        {
            queryObj.put(FIELD_RANDOMIZER, new BasicDBObject("$lt", random));
            dbObject = collection.findOne(queryObj);
        }

        return SubscriptionData.fromDBObject(dbObject);
    }

    @Override
    public Stream<SubscriptionData> getRandomSubscriptionsByLastSyncTime(String username,
            int limit)
    {
        Range range = getRandomizerRange();
        int upper = range.getMax();
        int lower = range.getMin();
        int random = lower + (int) (Math.random() * (double) (upper - lower));

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start()
                .add(FIELD_STATE, DataCreationState.Created.toString())
                .push(FIELD_RANDOMIZER).add("$gte", Integer.valueOf(random))
                .pop();
        if (username != null)
        {
            builder.add(FIELD_USERNAME, username);
        }
        DBObject queryObj = builder.get();
        DBObject orderBy = BasicDBObjectBuilder
                .start(SubscriptionData.FIELD_LAST_SYNC_MS, 1)
                .add(FIELD_RANDOMIZER, 1)
                .get();
        long count = collection.count(queryObj);
        if (limit > 0 && count < limit)
        {
            BasicDBObjectBuilder builder1 = BasicDBObjectBuilder.start()
                    .push(FIELD_RANDOMIZER)
                    .add("$lte", Integer.valueOf(random)).pop();
            if (username != null)
            {
                builder.add(FIELD_USERNAME, username);
            }
            queryObj = builder1.get();
            count = collection.count(queryObj);
            if (limit > 0 && count < limit)
            {
                throw new RuntimeException(
                        "Not enough subscriptions for limit " + limit);
            }
            orderBy = BasicDBObjectBuilder.start(FIELD_RANDOMIZER, -1).get();
        }

        DBCursor cur = collection.find(queryObj).sort(orderBy).limit(limit);
        Stream<SubscriptionData> stream = StreamSupport
                .stream(cur.spliterator(), false).onClose(() -> cur.close())
                .map(dbo -> SubscriptionData.fromDBObject(dbo)); // need to
                                                                 // close
                                                                 // cursor;
        return stream;
    }

    @Override
    public Stream<SubscriptionData> getRandomSubscriptions(String username, 
            int limit)
    {
        Range range = getRandomizerRange();
        int upper = range.getMax();
        int lower = range.getMin();
        int random = lower + (int) (Math.random() * (double) (upper - lower));

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start()
                .add(FIELD_STATE, DataCreationState.Created.toString())
                .push(FIELD_RANDOMIZER).add("$gte", Integer.valueOf(random))
                .pop();
        if (username != null)
        {
            builder.add(FIELD_USERNAME, username);
        }
        DBObject queryObj = builder.get();
        DBObject orderBy = BasicDBObjectBuilder.start(FIELD_RANDOMIZER, 1)
                .get();
        long count = collection.count(queryObj);
        if (limit > 0 && count < limit)
        {
            BasicDBObjectBuilder builder1 = BasicDBObjectBuilder.start()
                    .push(FIELD_RANDOMIZER)
                    .add("$lte", Integer.valueOf(random)).pop();
            if (username != null)
            {
                builder.add(FIELD_USERNAME, username);
            }
            queryObj = builder1.get();
            count = collection.count(queryObj);
            if (limit > 0 && count < limit)
            {
                throw new RuntimeException(
                        "Not enough subscriptions for limit " + limit);
            }
            orderBy = BasicDBObjectBuilder.start(FIELD_RANDOMIZER, -1).get();
        }

        DBCursor cur = collection.find(queryObj).sort(orderBy).limit(limit);
        Stream<SubscriptionData> stream = StreamSupport
                .stream(cur.spliterator(), false).onClose(() -> cur.close())
                .map(dbo -> SubscriptionData.fromDBObject(dbo)); // need to
                                                                 // close
                                                                 // cursor;
        return stream;
    }

    @Override
    public void removeSubscription(String subscriptionId)
    {
        DBObject query = BasicDBObjectBuilder.start(FIELD_SUBSCRIPTION_ID,
                subscriptionId).get();
        collection.remove(query);
    }

    @Override
    public void updateSubscription(String subscriptionId, long lastSyncMs)
    {
        DBObject query = BasicDBObjectBuilder.start(FIELD_SUBSCRIPTION_ID,
                subscriptionId).get();
        DBObject update = BasicDBObjectBuilder
                .start()
                .push("$inc")
                    .add("quantity", 1).add("numSyncs", 1)
                .pop()
                .add("$set", BasicDBObjectBuilder.start(FIELD_LAST_SYNC_MS, lastSyncMs).get())
                .get();
        collection.update(query, update, false, false);
    }
}

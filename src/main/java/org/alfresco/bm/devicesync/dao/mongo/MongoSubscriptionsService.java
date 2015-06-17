package org.alfresco.bm.devicesync.dao.mongo;

import static org.alfresco.bm.devicesync.data.SubscriptionData.FIELD_RANDOMIZER;
import static org.alfresco.bm.devicesync.data.SubscriptionData.FIELD_STATE;
import static org.alfresco.bm.devicesync.data.SubscriptionData.FIELD_SUBSCRIPTION_ID;
import static org.alfresco.bm.devicesync.data.SubscriptionData.FIELD_USERNAME;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.SubscriptionData;
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
public class MongoSubscriptionsService implements SubscriptionsService, InitializingBean
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
     * Ensure that the MongoDB collection has the required indexes associated with
     * this user bean.
     */
    private void checkIndexes()
    {
        collection.setWriteConcern(WriteConcern.SAFE);
        
        DBObject uidxUserName = BasicDBObjectBuilder
                .start(FIELD_SUBSCRIPTION_ID, 1)
                .get();
        DBObject optUserName = BasicDBObjectBuilder
                .start("name", "uidxSubscriptionId")
                .add("unique", Boolean.TRUE)
                .add("sparse", Boolean.TRUE)
                .get();
        collection.createIndex(uidxUserName, optUserName);

        DBObject idxState = BasicDBObjectBuilder
                .start(FIELD_STATE, 1)
                .get();
        DBObject optState = BasicDBObjectBuilder
                .start("name", "idxState")
                .add("unique", Boolean.FALSE)
                .get();
        collection.createIndex(idxState, optState);

        DBObject idxDomainRand = BasicDBObjectBuilder
                .start(FIELD_RANDOMIZER, 1)
                .get();
        DBObject optDomainRand = BasicDBObjectBuilder
                .start("name", "idxRand")
                .add("unique", Boolean.FALSE)
                .get();
        collection.createIndex(idxDomainRand, optDomainRand);
    }

    @Override
    public List<SubscriptionData> getSubscriptions(DataCreationState state, int skip, int count)
    {
    	List<SubscriptionData> subscriptions = new LinkedList<>();

    	DBObject query = QueryBuilder
    		.start(FIELD_STATE).is(state.toString())
    		.get();
    	DBCursor cursor = collection.find(query).skip(skip).limit(count);
    	for(DBObject dbObject : cursor)
    	{
    		SubscriptionData subscription = SubscriptionData.fromDBObject(dbObject);
    		subscriptions.add(subscription);
    	}

    	return subscriptions;
    }

//    @Override
//    public List<SubscriptionData> getRandomSubscriptions(DataCreationState state, int skip, int count)
//    {
//    	List<SubscriptionData> subscriptions = new LinkedList<>();
//
//    	DBObject query = QueryBuilder
//    		.start(FIELD_STATE).is(state.toString())
//    		.get();
//    	DBCursor cursor = collection.find(query).skip(skip).limit(count);
//    	for(DBObject dbObject : cursor)
//    	{
//    		SubscriptionData subscription = fromDBObject(dbObject);
//    		subscriptions.add(subscription);
//    	}
//
//    	return subscriptions;
//    }

//    private DBObject toDBObject(SubscriptionData subscriptionData)
//    {
//    	BasicDBObjectBuilder builder = BasicDBObjectBuilder
//        		.start(FIELD_USERNAME, subscriptionData.getUsername())
//        		.add(FIELD_SUBSCRIBER_ID, subscriptionData.getSubscriberId())
//        		.add(FIELD_SUBSCRIPTION_TYPE, subscriptionData.getSubscriptionType())
//        		.add(FIELD_PATH, subscriptionData.getPath())
//        		.add(FIELD_RANDOMIZER, subscriptionData.getRandomizer());
//    	if(subscriptionData.getSubscriptionId() != null)
//    	{
//    		builder.add(FIELD_SUBSCRIPTION_ID, subscriptionData.getSubscriptionId());
//    	}
//    	if(subscriptionData.getState() != null)
//    	{
//    		builder.add(FIELD_STATE, subscriptionData.getState().toString());
//    	}
//    	DBObject dbObject = builder.get();
//    	return dbObject;
//    }

//    private SubscriptionData fromDBObject(DBObject dbObject)
//    {
//    	ObjectId id = (ObjectId)dbObject.get("_id");
//    	String username = (String)dbObject.get(FIELD_USERNAME);
//    	String subscriberId = (String)dbObject.get(FIELD_SUBSCRIBER_ID);
//    	String subscriptionId = (String)dbObject.get(FIELD_SUBSCRIPTION_ID);
//    	int randomizer = (Integer)dbObject.get(FIELD_RANDOMIZER);
//    	DataCreationState state = DataCreationState.valueOf((String)dbObject.get(FIELD_STATE));
//    	String subscriptionType = (String)dbObject.get(FIELD_SUBSCRIPTION_TYPE);
//    	String path = (String)dbObject.get(FIELD_PATH);
//    	SubscriptionData subscriptionData = new SubscriptionData(id, username, subscriberId, subscriptionId,
//    			subscriptionType, path, state, randomizer);
//    	return subscriptionData;
//    }

	@Override
    public void addSubscription(String username, String subscriberId,
            String subscriptionId, String subscriptionType, String path,
            DataCreationState state)
    {
    	SubscriptionData subscriptionData = new SubscriptionData(username, subscriberId, subscriptionId,
    			subscriptionType, path, state);
    	DBObject insert = subscriptionData.toDBObject();
    	collection.insert(insert);
    }

    @Override
    public void addSubscription(String username, String subscriberId, String subscriptionType, String path,
    		DataCreationState state)
    {
    	SubscriptionData subscriptionData = new SubscriptionData(username, subscriberId, subscriptionType, path,
    			state);
    	DBObject insert = subscriptionData.toDBObject();
    	collection.insert(insert);
    }

    @Override
    public SubscriptionData getSubscription(String subscriptionId)
    {
    	SubscriptionData subscription = null;

    	DBObject query = QueryBuilder
    			.start(FIELD_SUBSCRIPTION_ID).is(subscriptionId)
    			.and(FIELD_STATE).is(DataCreationState.Created)
    			.get();
    	DBObject dbObject = collection.findOne(query);
    	if(dbObject != null)
    	{
    		subscription = SubscriptionData.fromDBObject(dbObject);
    	}

    	return subscription;
    }

    @Override
    public void updateSubscription(ObjectId objectId, String subscriptionId, DataCreationState state)
    {
    	DBObject query = QueryBuilder
    		.start("_id").is(objectId)
    		.get();

    	BasicDBObjectBuilder builder = BasicDBObjectBuilder
    			.start(FIELD_STATE, state.toString());
    	if(subscriptionId != null)
    	{
    		builder.add(FIELD_SUBSCRIPTION_ID, subscriptionId);
    	}
    	DBObject update = BasicDBObjectBuilder
    			.start("$set", builder.get())
    			.get();
    	collection.update(query, update);
    }

    @Override
    public void updateSubscription(ObjectId objectId, DataCreationState state)
    {
    	DBObject query = QueryBuilder
    		.start("_id").is(objectId)
    		.get();
    	DBObject update = BasicDBObjectBuilder
    			.start("$set", BasicDBObjectBuilder
    					.start(FIELD_STATE, state.toString())
    					.get())
    			.get();
    	collection.update(query, update);
    }

    @Override
    public long countSubscriptions(DataCreationState state)
    {
    	QueryBuilder builder = QueryBuilder
    			.start();
    	if(state != null)
    	{
    		builder.and(FIELD_STATE).is(state.toString());
    	}
    	DBObject query = builder.get();
    	long count = collection.count(query);
    	return count;
    }

    @Override
    public SubscriptionData getRandomSubscription(String username)
    {
        int random = (int) (Math.random() * (double) 1e6);
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start()
                .add(FIELD_STATE, DataCreationState.Created.toString())
                .push(FIELD_RANDOMIZER)
                    .add("$gte", Integer.valueOf(random))
                .pop();
        if(username != null)
        {
        	builder.add(FIELD_USERNAME, username);
        }
        DBObject queryObj = builder.get();

        DBObject dbObject = collection.findOne(queryObj);
        if(dbObject == null)
        {
            queryObj.put(FIELD_RANDOMIZER, new BasicDBObject("$lt", random));
            dbObject = collection.findOne(queryObj);
        }

        return SubscriptionData.fromDBObject(dbObject);
    }

	@Override
    public void removeSubscription(String subscriptionId)
    {
		DBObject query = BasicDBObjectBuilder
				.start(FIELD_SUBSCRIPTION_ID, subscriptionId)
				.get();
		collection.remove(query);
    }
}

package org.alfresco.bm.devicesync.dao.mongo;

import static org.alfresco.bm.devicesync.data.SubscriberData.fromDBObject;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.devicesync.dao.SubscribersService;
import org.alfresco.bm.devicesync.data.SubscriberData;
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
public class MongoSubscribersService implements SubscribersService, InitializingBean
{
	public static String FIELD_USERNAME = "username";
	public static String FIELD_RANDOMIZER = "randomizer";
	public static String FIELD_SUBSCRIBER_ID = "subscriberId";
	public static String FIELD_STATE = "state";

    /** The collection of users, which can be reused by derived extensions. */
    protected final DBCollection collection;
    
    public MongoSubscribersService(DB db, String collection)
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

        DBObject uidxSubscriberId = BasicDBObjectBuilder
                .start(FIELD_SUBSCRIBER_ID, 1)
                .get();
        DBObject optSubscriberId = BasicDBObjectBuilder
                .start("name", "uidxSubscriberId")
                .add("unique", Boolean.TRUE)
                .add("sparse", Boolean.TRUE)
                .get();
        collection.createIndex(uidxSubscriberId, optSubscriberId);

        DBObject idxUserName = BasicDBObjectBuilder
                .start(FIELD_USERNAME, 1)
        		.add(FIELD_STATE, 2)
                .get();
        DBObject optUserName = BasicDBObjectBuilder
                .start("name", "uidxUserName")
                .add("unique", Boolean.FALSE)
                .get();
        collection.createIndex(idxUserName, optUserName);

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
    public void addSubscriber(String username, DataCreationState state)
    {
    	SubscriberData subscriberData = new SubscriberData(username, state);
    	DBObject insert = subscriberData.toDBObject();
    	collection.insert(insert);
    }

	@Override
    public void addSubscriber(String username, String subscriberId,
            DataCreationState state)
    {
    	SubscriberData subscriberData = new SubscriberData(username, subscriberId, state);
    	DBObject insert = subscriberData.toDBObject();
    	collection.insert(insert);
    }

    @Override
    public void updateSubscriber(ObjectId objectId, String subscriberId, DataCreationState state)
    {
    	DBObject query = QueryBuilder
    		.start("_id").is(objectId)
    		.get();
    	BasicDBObjectBuilder builder = BasicDBObjectBuilder
    			.start(FIELD_STATE, state.toString());
    	if(subscriberId != null)
    	{
    		builder.add(FIELD_SUBSCRIBER_ID, subscriberId);
    	}
    	DBObject update = BasicDBObjectBuilder
    			.start("$set", builder.get())
    			.get();
    	collection.update(query, update);
    }


    @Override
    public void updateSubscriber(ObjectId objectId, DataCreationState state)
    {
    	DBObject query = QueryBuilder
    		.start("_id").is(objectId)
    		.get();
    	BasicDBObjectBuilder builder = BasicDBObjectBuilder
    			.start(FIELD_STATE, state.toString());
    	DBObject update = BasicDBObjectBuilder
    			.start("$set", builder.get())
    			.get();
    	collection.update(query, update);
    }

    @Override
    public String getSubscriber(String username)
    {
    	DBObject query = QueryBuilder
    		.start(FIELD_USERNAME).is(username)
    		.get();
    	DBObject dbObject = collection.findOne(query);
    	String subscriberId = (String)dbObject.get(FIELD_SUBSCRIBER_ID);
    	return subscriberId;
    }

    @Override
    public List<SubscriberData> getSubscribers(DataCreationState state, int skip, int count)
    {
    	List<SubscriberData> subscribers = new LinkedList<>();

    	DBObject query = QueryBuilder
    		.start(FIELD_STATE).is(state.toString())
    		.get();
    	DBCursor cursor = collection.find(query).skip(skip).limit(count);
    	for(DBObject dbObject : cursor)
    	{
    		SubscriberData subcriber = fromDBObject(dbObject);
//    		ObjectId objectId = (ObjectId)dbObject.get("_id");
//    		String username = (String)dbObject.get(FIELD_USERNAME);
//    		String subscriberId = (String)dbObject.get(FIELD_SUBSCRIBER_ID);
//    		SubscriberData subcriber = new SubscriberData(objectId, username, subscriberId);
    		subscribers.add(subcriber);
    	}

    	return subscribers;
    }

    @Override
    public long countSubscribers(DataCreationState state)
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
    public SubscriberData getRandomSubscriber(String username)
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

        return fromDBObject(dbObject);
    }

	@Override
    public void removeSubscriber(String subscriberId)
    {
		DBObject query = BasicDBObjectBuilder
				.start(FIELD_SUBSCRIBER_ID, subscriberId)
				.get();
		collection.remove(query);
    }
}

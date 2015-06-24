package org.alfresco.bm.devicesync.dao.mongo;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.dao.SyncsService;
import org.alfresco.bm.devicesync.data.SyncData;
import org.alfresco.bm.devicesync.data.SyncState;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.InitializingBean;

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
public class MongoSyncsService implements SyncsService, InitializingBean
{
	private static String FIELD_SITE_ID = "siteId";
	private static String FIELD_SYNC_ID = "syncId";
	private static String FIELD_USERNAME = "username";
	private static String FIELD_RANDOMIZER = "randomizer";
	private static String FIELD_SUBSCRIBER_ID = "subscriberId";
	private static String FIELD_SUBSCRIPTION_ID = "subscriptionId";
	private static String FIELD_SUBSCRIPTION_TYPE = "subscriptionType";
	private static String FIELD_PATH = "path";
	private static String FIELD_STATE = "state";
	private static String FIELD_MESSAGE = "message";
	private static String FIELD_MAX_RETRIES_HIT = "maxRetriesHit";
	private static String FIELD_NUM_SYNC_CHANGES = "numSyncChanges";
	private static String FIELD_MSG = "msg";
	private static String FIELD_NUM_RETRIES = "numRetries";
	private static String FIELD_END_TIME = "endTime";
	private static String FIELD_COUNT = "count";

    /** The collection of users, which can be reused by derived extensions. */
    protected final DBCollection collection;
    
    public MongoSyncsService(DB db, String collection)
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

        DBObject idxSubscriptionId = BasicDBObjectBuilder
                .start(FIELD_SUBSCRIPTION_ID, 1)
                .add(FIELD_STATE, 2)
                .get();
        DBObject optSubscriptionId = BasicDBObjectBuilder
                .start("name", "idxSubscriptionId")
                .get();
        collection.createIndex(idxSubscriptionId, optSubscriptionId);

        DBObject idxSyncId = BasicDBObjectBuilder
                .start(FIELD_SYNC_ID, 1)
                .add(FIELD_STATE, 2)
                .get();
        DBObject optSyncId = BasicDBObjectBuilder
                .start("name", "idxSyncId")
                .get();
        collection.createIndex(idxSyncId, optSyncId);

        DBObject idxState = BasicDBObjectBuilder
                .start(FIELD_STATE, 1)
                .get();
        DBObject optState = BasicDBObjectBuilder
                .start("name", "idxState")
                .get();
        collection.createIndex(idxState, optState);
    }

    @Override
    public List<SyncData> getSyncs(SyncState state, int skip, int count)
    {
    	List<SyncData> syncs = new LinkedList<>();

    	DBObject query = QueryBuilder
    		.start(FIELD_STATE).is(state.toString())
    		.get();
    	DBCursor cursor = collection.find(query).skip(skip).limit(count);
    	for(DBObject dbObject : cursor)
    	{
    		ObjectId objectId = (ObjectId)dbObject.get("_id");
    		String siteId = (String)dbObject.get(FIELD_SITE_ID);
    		String syncId = (String)dbObject.get(FIELD_SYNC_ID);
    		String username = (String)dbObject.get(FIELD_USERNAME);
    		String subscriberId = (String)dbObject.get(FIELD_SUBSCRIBER_ID);
    		String subscriptionId = (String)dbObject.get(FIELD_SUBSCRIPTION_ID);
    		SyncData subscription = new SyncData(objectId, siteId, username, subscriberId, subscriptionId, syncId,
    				-1, -1, false, null, null);
    		syncs.add(subscription);
    	}

    	return syncs;
    }

    @Override
    public void addSync(String username, String subscriberId, String subscriptionId, SyncState syncState)
    {
    	BasicDBObjectBuilder builder = BasicDBObjectBuilder
    		.start(FIELD_SUBSCRIBER_ID, subscriberId)
    		.add(FIELD_USERNAME, username)
    		.add(FIELD_SUBSCRIPTION_ID, subscriptionId);
    	if(syncState != null)
    	{
    		builder.add(FIELD_STATE, syncState.toString());
    	}
    	DBObject insert = builder.get();
    	collection.insert(insert);
    }

    @Override
    public void updateSync(ObjectId objectId, String syncId, SyncState syncState, String message)
    {
    	DBObject query = QueryBuilder
    		.start("_id").is(objectId)
    		.get();
    	DBObject update = BasicDBObjectBuilder
    			.start("$set", BasicDBObjectBuilder
    					.start(FIELD_SYNC_ID, syncId)
    					.add(FIELD_STATE, syncState.toString())
    					.add(FIELD_MESSAGE, message)
    					.get())
    			.get();
    	collection.update(query, update);
    }

    @Override
    public void updateSync(String syncId, SyncState syncState, String message)
    {
    	DBObject query = QueryBuilder
    		.start(FIELD_SYNC_ID).is(syncId)
    		.get();
    	DBObject update = BasicDBObjectBuilder
    			.start("$set", BasicDBObjectBuilder
    					.start(FIELD_STATE, syncState.toString())
    					.add(FIELD_MESSAGE, message)
    					.get())
    			.get();
    	collection.update(query, update);
    }

	@Override
    public void updateSync(ObjectId objectId, SyncState syncState,
            String message)
    {
    	DBObject query = QueryBuilder
        		.start("_id").is(objectId)
        		.get();
        	DBObject update = BasicDBObjectBuilder
        			.start("$set", BasicDBObjectBuilder
        					.start(FIELD_STATE, syncState.toString())
        					.get())
        			.get();
        	collection.update(query, update);
    }

    @Override
    public long countSyncs(SyncState state)
    {
    	DBObject query = QueryBuilder
        		.start(FIELD_STATE).is(state.toString())
        		.get();
    	long count = collection.count(query);
    	return count;
    }
}

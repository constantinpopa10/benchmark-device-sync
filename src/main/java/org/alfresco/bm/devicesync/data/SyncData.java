package org.alfresco.bm.devicesync.data;

import static org.alfresco.bm.devicesync.dao.mongo.MongoSyncsService.FIELD_END_TIME;
import static org.alfresco.bm.devicesync.dao.mongo.MongoSyncsService.FIELD_MAX_RETRIES_HIT;
import static org.alfresco.bm.devicesync.dao.mongo.MongoSyncsService.FIELD_NUM_RETRIES;
import static org.alfresco.bm.devicesync.dao.mongo.MongoSyncsService.FIELD_NUM_SYNC_CHANGES;
import static org.alfresco.bm.devicesync.dao.mongo.MongoSyncsService.FIELD_SUBSCRIBER_ID;
import static org.alfresco.bm.devicesync.dao.mongo.MongoSyncsService.FIELD_SUBSCRIPTION_ID;
import static org.alfresco.bm.devicesync.dao.mongo.MongoSyncsService.FIELD_SYNC_ID;
import static org.alfresco.bm.devicesync.dao.mongo.MongoSyncsService.FIELD_USERNAME;

import java.io.Serializable;
import java.util.List;

import org.alfresco.service.synchronization.api.Change;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class SyncData implements Serializable
{
	private static final long serialVersionUID = 946578159221599841L;

	private Long endTime;
	private int numSyncChanges = 0;
	private int numRetries = 0;
	private boolean maximumRetriesHit = false;
	private ObjectId objectId;
	private String username;
	private String subscriberId;
	private String subscriptionId;
	private String syncId;
	private String result;

	public SyncData(String username, String subscriberId, String subscriptionId, Long endTime)
    {
	    super();
	    this.username = username;
	    this.subscriberId = subscriberId;
	    this.subscriptionId = subscriptionId;
	    this.endTime = endTime;
    }

	public SyncData(String username, String subscriberId, String subscriptionId, String syncId, Long endTime)
    {
	    super();
	    this.username = username;
	    this.subscriberId = subscriberId;
	    this.subscriptionId = subscriptionId;
	    this.syncId = syncId;
	    this.endTime = endTime;
    }

	public SyncData(ObjectId objectId, String username, String subscriberId, String subscriptionId, String syncId)
    {
	    super();
	    this.objectId = objectId;
	    this.username = username;
	    this.subscriberId = subscriberId;
	    this.subscriptionId = subscriptionId;
	    this.syncId = syncId;
    }

	public SyncData(ObjectId objectId, String username, String subscriberId, String subscriptionId, String syncId,
			int numSyncChanges, int numRetries, boolean maximumRetriesHit, Long endTime)
    {
	    super();
	    this.objectId = objectId;
	    this.username = username;
	    this.subscriberId = subscriberId;
	    this.subscriptionId = subscriptionId;
	    this.syncId = syncId;
	    this.numSyncChanges = numSyncChanges;
	    this.numRetries = numRetries;
	    this.maximumRetriesHit = maximumRetriesHit;
	    this.endTime = endTime;
    }

	public Long getEndTime()
	{
		return endTime;
	}

	public int getNumSyncChanges()
	{
		return numSyncChanges;
	}

	public int getNumRetries()
	{
		return numRetries;
	}

	public boolean isMaximumRetriesHit()
	{
		return maximumRetriesHit;
	}

	public ObjectId getObjectId()
	{
		return objectId;
	}
	
	public String getUsername()
	{
		return username;
	}
	public String getSubscriberId()
	{
		return subscriberId;
	}
	public String getSubscriptionId()
	{
		return subscriptionId;
	}
	public String getSyncId()
	{
		return syncId;
	}
	public String getResult()
	{
		return result;
	}

    public DBObject toDBObject()
    {
    	BasicDBObjectBuilder builder = BasicDBObjectBuilder
        		.start(FIELD_USERNAME, getUsername())
        		.add(FIELD_SUBSCRIBER_ID, getSubscriberId())
        		.add(FIELD_SYNC_ID, getSyncId())
        		.add(FIELD_NUM_RETRIES, getNumRetries())
        		.add(FIELD_MAX_RETRIES_HIT, isMaximumRetriesHit())
        		.add(FIELD_NUM_SYNC_CHANGES, getNumSyncChanges());
    	if(getSubscriptionId() != null)
    	{
    		builder.add(FIELD_SUBSCRIPTION_ID, getSubscriptionId());
    	}
    	DBObject dbObject = builder.get();
    	return dbObject;
    }

    public void gotResults(List<Change> changes)
    {
    	this.numSyncChanges = changes.size();
    }

    public void maxRetriesHit()
    {
    	this.maximumRetriesHit = true;
    }

    public void incrementRetries()
    {
    	numRetries++;
    }

    public static SyncData fromDBObject(DBObject dbObject)
    {
    	ObjectId id = (ObjectId)dbObject.get("_id");
    	String username = (String)dbObject.get(FIELD_USERNAME);
    	String subscriberId = (String)dbObject.get(FIELD_SUBSCRIBER_ID);
    	String subscriptionId = (String)dbObject.get(FIELD_SUBSCRIPTION_ID);
    	String syncId = (String)dbObject.get(FIELD_SYNC_ID);
    	int numSyncChanges = (Integer)dbObject.get(FIELD_NUM_SYNC_CHANGES);
    	int numRetries = (Integer)dbObject.get(FIELD_NUM_RETRIES);
    	boolean maximumRetriesHit = (Boolean)dbObject.get(FIELD_MAX_RETRIES_HIT);
    	Long endTime = (Long)dbObject.get(FIELD_END_TIME);
    	SyncData syncData = new SyncData(id, username, subscriberId, subscriptionId, syncId, numSyncChanges,
    			numRetries, maximumRetriesHit, endTime);
    	return syncData;
    }
}

package org.alfresco.bm.devicesync.data;

import java.io.Serializable;
import java.util.List;

import org.alfresco.service.synchronization.api.Change;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class SyncData implements Serializable
{
	public static String FIELD_SITE_ID = "siteId";
	public static String FIELD_SYNC_ID = "syncId";
	public static String FIELD_USERNAME = "username";
	public static String FIELD_RANDOMIZER = "randomizer";
	public static String FIELD_SUBSCRIBER_ID = "subscriberId";
	public static String FIELD_SUBSCRIPTION_ID = "subscriptionId";
	public static String FIELD_SUBSCRIPTION_TYPE = "subscriptionType";
	public static String FIELD_PATH = "path";
	public static String FIELD_STATE = "state";
	public static String FIELD_MESSAGE = "message";
	public static String FIELD_MAX_RETRIES_HIT = "maxRetriesHit";
	public static String FIELD_NUM_SYNC_CHANGES = "numSyncChanges";
	public static String FIELD_MSG = "msg";
	public static String FIELD_NUM_RETRIES = "numRetries";
	public static String FIELD_END_TIME = "endTime";
	public static String FIELD_COUNT = "count";

	private static final long serialVersionUID = 946578159221599841L;

	private Long endTime;
	private int numSyncChanges = 0;
	private int numRetries = 0;
	private boolean maximumRetriesHit = false;
	private ObjectId objectId;
	private String username;
	private String subscriberId;
	private String subscriptionId;
	private String siteId;
	private String syncId;
	private String result;
	private String msg;
	private List<Change> changes;

	public SyncData(String siteId, String username, String subscriberId, String subscriptionId, Long endTime)
    {
	    this(null, siteId, username, subscriberId, subscriptionId, null, -1, 0, false, endTime, null);
    }

	public SyncData(String siteId, String username, String subscriberId, String subscriptionId,
			String syncId, Long endTime)
    {
	    this(null, siteId, username, subscriberId, subscriptionId, syncId, -1, 0, false, endTime, "Get sync " + syncId);
    }

	public SyncData(ObjectId objectId, String siteId, String username, String subscriberId, String subscriptionId,
			String syncId, int numSyncChanges, int numRetries, boolean maximumRetriesHit, Long endTime, String msg)
    {
	    super();
    	this.msg = msg;
	    this.objectId = objectId;
	    this.siteId = siteId;
	    this.username = username;
	    this.subscriberId = subscriberId;
	    this.subscriptionId = subscriptionId;
	    this.syncId = syncId;
	    this.endTime = endTime;
	    this.numSyncChanges = numSyncChanges;
	    this.numRetries = numRetries;
	    this.maximumRetriesHit = maximumRetriesHit;
    }

	public String getSiteId()
	{
		return siteId;
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
        		.add(FIELD_SITE_ID, getSiteId())
        		.add(FIELD_SYNC_ID, getSyncId())
        		.add(FIELD_NUM_RETRIES, getNumRetries())
        		.add(FIELD_MAX_RETRIES_HIT, isMaximumRetriesHit())
        		.add(FIELD_NUM_SYNC_CHANGES, getNumSyncChanges())
        		.add(FIELD_MSG, msg);
    	if(getSubscriptionId() != null)
    	{
    		builder.add(FIELD_SUBSCRIPTION_ID, getSubscriptionId());
    	}
    	DBObject dbObject = builder.get();
    	return dbObject;
    }

    public void gotResults(List<Change> changes)
    {
    	this.changes = changes;
    	this.numSyncChanges = changes.size();
    	this.msg = "Get sync " + syncId + " successful";
    }

    public void maxRetriesHit()
    {
    	this.maximumRetriesHit = true;
    	this.msg = "Get sync " + syncId + " max retries hit";
    }

    public void incrementRetries()
    {
    	numRetries++;
    }

    public List<Change> getChanges()
	{
		return changes;
	}

	public static SyncData fromDBObject(DBObject dbObject)
    {
    	ObjectId id = (ObjectId)dbObject.get("_id");
    	String username = (String)dbObject.get(FIELD_USERNAME);
    	String subscriberId = (String)dbObject.get(FIELD_SUBSCRIBER_ID);
    	String subscriptionId = (String)dbObject.get(FIELD_SUBSCRIPTION_ID);
    	String siteId = (String)dbObject.get(FIELD_SITE_ID);
    	String syncId = (String)dbObject.get(FIELD_SYNC_ID);
    	int numSyncChanges = (Integer)dbObject.get(FIELD_NUM_SYNC_CHANGES);
    	int numRetries = (Integer)dbObject.get(FIELD_NUM_RETRIES);
    	boolean maximumRetriesHit = (Boolean)dbObject.get(FIELD_MAX_RETRIES_HIT);
    	Long endTime = (Long)dbObject.get(FIELD_END_TIME);
    	String msg = (String)dbObject.get("msg");
    	SyncData syncData = new SyncData(id, siteId, username, subscriberId, subscriptionId, syncId, numSyncChanges,
    			numRetries, maximumRetriesHit, endTime, msg);
    	return syncData;
    }
}

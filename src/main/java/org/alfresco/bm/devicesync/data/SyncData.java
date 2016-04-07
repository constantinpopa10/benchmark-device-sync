package org.alfresco.bm.devicesync.data;

import java.io.Serializable;
import java.util.List;

import org.alfresco.service.synchronization.api.Change;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
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
    public static String FIELD_LAST_SYNC_MS = "lastSyncMs";
    public static String FIELD_NUM_SYNC_CHANGES = "numSyncChanges";
    public static String FIELD_MSG = "msg";
    public static String FIELD_NUM_RETRIES = "numRetries";
    public static String FIELD_FINAL_NUM_RETRIES = "finalNumRetries";
    public static String FIELD_END_TIME = "endTime";
    public static String FIELD_COUNT = "count";
    public static String FIELD_GOT_RESULTS = "gotResults";
    public static String FIELD_RESULT = "result";

    private static final long serialVersionUID = 946578159221599841L;

    private boolean gotResults = false;
    private Long endTime;
    private int numSyncChanges = 0;
    private int numRetries = 0;
    private int finalNumRetries = 0;
    private boolean maximumRetriesHit = false;
    private ObjectId objectId;
    private String username;
    private String subscriberId;
    private String subscriptionId;
    private Long lastSyncMs;
    private String siteId;
    private Long syncId;
    private String result;
    private String msg;
    private List<Change> changes;

    public SyncData(String siteId, String username, String subscriberId,
            String subscriptionId, Long lastSyncMs, Long endTime)
    {
        this(null, siteId, username, subscriberId, subscriptionId, lastSyncMs, null, -1, 0,
                0, false, endTime, null, false);
    }

    public SyncData(ObjectId objectId, String siteId, String username,
            String subscriberId, String subscriptionId, Long lastSyncMs, Long syncId,
            int numSyncChanges, int numRetries, int finalNumRetries,
            boolean maximumRetriesHit, Long endTime, String msg,
            Boolean gotResults)
    {
        super();
        this.msg = msg;
        this.objectId = objectId;
        this.siteId = siteId;
        this.username = username;
        this.subscriberId = subscriberId;
        this.subscriptionId = subscriptionId;
        this.lastSyncMs = lastSyncMs;
        this.syncId = syncId;
        this.endTime = endTime;
        this.numSyncChanges = numSyncChanges;
        this.numRetries = numRetries;
        this.finalNumRetries = finalNumRetries;
        this.maximumRetriesHit = maximumRetriesHit;
        this.gotResults = (gotResults != null ? gotResults.booleanValue()
                : false);
    }

    public Long getLastSyncMs()
    {
        return lastSyncMs;
    }

    public boolean isGotResults()
    {
        return gotResults;
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

    public int getFinalNumRetries()
    {
        return finalNumRetries;
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

    public Long getSyncId()
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
                .add(FIELD_FINAL_NUM_RETRIES, getFinalNumRetries())
                .add(FIELD_MAX_RETRIES_HIT, isMaximumRetriesHit())
                .add(FIELD_NUM_SYNC_CHANGES, getNumSyncChanges())
                .add(FIELD_LAST_SYNC_MS, getLastSyncMs())
                .add(FIELD_GOT_RESULTS, gotResults).add(FIELD_MSG, msg)
                .add(FIELD_RESULT, result);
        if (getSubscriptionId() != null)
        {
            builder.add(FIELD_SUBSCRIPTION_ID, getSubscriptionId());
        }
        DBObject dbObject = builder.get();
        return dbObject;
    }

    public void gotResults(List<Change> changes, String message)
    {
        this.msg = "Get sync " + syncId + ", " + message;
        this.changes = changes;
        this.numSyncChanges = changes == null ? 0 : changes.size();
        this.gotResults = true;
        this.finalNumRetries = numRetries;
    }

    public void maxRetriesHit()
    {
        this.maximumRetriesHit = true;
        this.msg = "Get sync " + syncId + " max retries hit";
    }

    public void incrementRetries(String result)
    {
        numRetries++;
        this.result = result;
        this.msg = "Get sync " + syncId + " not ready, need to retry";
    }

    public List<Change> getChanges()
    {
        return changes;
    }

    public static SyncData fromDBObject(DBObject dbObject)
    {
        ObjectId id = (ObjectId) dbObject.get("_id");
        String username = (String) dbObject.get(FIELD_USERNAME);
        String subscriberId = (String) dbObject.get(FIELD_SUBSCRIBER_ID);
        String subscriptionId = (String) dbObject.get(FIELD_SUBSCRIPTION_ID);
        String siteId = (String) dbObject.get(FIELD_SITE_ID);
        Long syncId = (Long) dbObject.get(FIELD_SYNC_ID);
        int numSyncChanges = (Integer) dbObject.get(FIELD_NUM_SYNC_CHANGES);
        int numRetries = (Integer) dbObject.get(FIELD_NUM_RETRIES);
        int finalNumRetries = (Integer) dbObject.get(FIELD_FINAL_NUM_RETRIES);
        boolean maximumRetriesHit = (Boolean) dbObject
                .get(FIELD_MAX_RETRIES_HIT);
        Long endTime = (Long) dbObject.get(FIELD_END_TIME);
        Boolean gotResults = (Boolean) dbObject.get(FIELD_GOT_RESULTS);
        String msg = (String) dbObject.get("msg");
        Long lastSyncMs = (Long) dbObject.get("lastSyncMs");
        SyncData syncData = new SyncData(id, siteId, username, subscriberId,
                subscriptionId, lastSyncMs, syncId, numSyncChanges, numRetries,
                finalNumRetries, maximumRetriesHit, endTime, msg, gotResults);
        return syncData;
    }
}

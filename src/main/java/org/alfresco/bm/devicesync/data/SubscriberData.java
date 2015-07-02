package org.alfresco.bm.devicesync.data;

import org.alfresco.bm.data.DataCreationState;
import org.bson.types.ObjectId;
import org.springframework.social.alfresco.api.entities.Subscriber;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class SubscriberData
{
	public static String FIELD_USERNAME = "username";
	public static String FIELD_RANDOMIZER = "randomizer";
	public static String FIELD_SUBSCRIBER_ID = "subscriberId";
	public static String FIELD_STATE = "state";
	public static String FIELD_SYNC_SERVICE_URI = "syncServiceURI";

	private ObjectId objectId;
	private int randomizer;
	private String username;
	private String subscriberId;
	private DataCreationState state;
	private String syncServiceURI;

	public SubscriberData(String username)
    {
		this.username = username;
    }

	public SubscriberData(String username, DataCreationState state)
    {
		this.username = username;
		this.state = state;
	    this.randomizer = (int)(Math.random() * 1E6);
    }

	public SubscriberData(String username, String subscriberId, String syncServiceURI, DataCreationState state)
    {
		this.username = username;
		this.subscriberId = subscriberId;
		this.state = state;
		this.syncServiceURI = syncServiceURI;
	    this.randomizer = (int)(Math.random() * 1E6);
    }

	public SubscriberData(ObjectId objectId, String username, String subscriberId, String syncServiceURI, 
			DataCreationState state, int randomizer)
    {
		this(objectId, username);
		this.randomizer = randomizer;
		this.state = state;
		this.subscriberId = subscriberId;
		this.syncServiceURI = subscriberId;
    }

	public SubscriberData(ObjectId objectId, String username, int randomizer)
    {
		this(objectId, username);
		this.randomizer = randomizer;
    }

	public SubscriberData(ObjectId objectId, String username)
    {
	    super();
	    this.randomizer = (int)(Math.random() * 1E6);
	    this.objectId = objectId;
	    this.username = username;
    }

	public SubscriberData(ObjectId objectId, String username, String subscriberId)
    {
	    this(objectId, username);
	    this.subscriberId = subscriberId;
    }

	public SubscriberData addSubscriber(Subscriber subscriber)
	{
		SubscriberData subscriberData = new SubscriberData(objectId, username, subscriber.getSubscriberId());
		subscriberData.randomizer = randomizer;
		return subscriberData;
	}

	public String getSyncServiceURI()
	{
		return syncServiceURI;
	}

	public DataCreationState getState()
	{
		return state;
	}

	public int getRandomizer()
	{
		return randomizer;
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

    public DBObject toDBObject()
    {
    	BasicDBObjectBuilder builder = BasicDBObjectBuilder
        		.start(FIELD_USERNAME, getUsername())
        		.add(FIELD_RANDOMIZER, getRandomizer());
    	if(getSubscriberId() != null)
    	{
    		builder.add(FIELD_SUBSCRIBER_ID, getSubscriberId());
    	}
    	if(getState() != null)
    	{
    		builder.add(FIELD_STATE, getState().toString());
    	}
    	if(syncServiceURI != null)
    	{
    		builder.add(FIELD_SYNC_SERVICE_URI, syncServiceURI);
    	}
		DBObject dbObject = builder.get();
    	return dbObject;
    }

    public static SubscriberData fromDBObject(DBObject dbObject)
    {
    	SubscriberData subscriberData = null;
    	if(dbObject != null)
    	{
	    	ObjectId id = (ObjectId)dbObject.get("_id");
	    	String username = (String)dbObject.get(FIELD_USERNAME);
	    	String subscriberId = (String)dbObject.get(FIELD_SUBSCRIBER_ID);
	    	int randomizer = (Integer)dbObject.get(FIELD_RANDOMIZER);
	    	String stateStr = (String)dbObject.get(FIELD_STATE);
	    	String syncServiceURI = (String)dbObject.get(FIELD_SYNC_SERVICE_URI);
	    	DataCreationState state = (stateStr != null ? DataCreationState.valueOf(stateStr) : null);
	    	subscriberData = new SubscriberData(id, username, subscriberId, syncServiceURI, state, randomizer);
    	}
    	return subscriberData;
    }

	@Override
    public String toString()
    {
	    return "SubscriberData [objectId=" + objectId + ", randomizer="
	            + randomizer + ", username=" + username + ", subscriberId="
	            + subscriberId + ", state=" + state + ", syncServiceURI="
	            + syncServiceURI + "]";
    }

}

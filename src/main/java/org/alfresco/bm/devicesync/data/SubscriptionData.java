package org.alfresco.bm.devicesync.data;

import org.alfresco.bm.data.DataCreationState;
import org.bson.types.ObjectId;
import org.springframework.social.alfresco.api.entities.Subscription;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class SubscriptionData
{
	public static String FIELD_USERNAME = "username";
	public static String FIELD_RANDOMIZER = "randomizer";
	public static String FIELD_SUBSCRIBER_ID = "subscriberId";
	public static String FIELD_SUBSCRIPTION_ID = "subscriptionId";
	public static String FIELD_SUBSCRIPTION_TYPE = "subscriptionType";
	public static String FIELD_PATH = "path";
	public static String FIELD_STATE = "state";

	private ObjectId objectId;
	private int randomizer;
	private String username;
	private String subscriberId;
	private String subscriptionId;
	private String subscriptionType;
	private String path;
	private DataCreationState state;

	public SubscriptionData(String username, String subscriberId)
	{
	    this.username = username;
	    this.subscriberId = subscriberId;
	}

	public SubscriptionData(ObjectId objectId, String username, String subscriberId,
			String subscriptionId, String subscriptionType, String path, DataCreationState state,
			int randomizer)
    {
		this(objectId, username, subscriberId, subscriptionId, subscriptionType, path);
		this.randomizer = randomizer;
		this.state = state;
    }

	public SubscriptionData(String username, String subscriberId,
			String subscriptionId, String subscriptionType, String path, DataCreationState state)
    {
		this(null, username, subscriberId, subscriptionId, subscriptionType, path);
		this.state = state;
    }

	public SubscriptionData(String username, String subscriberId, String subscriptionType, String path, DataCreationState state)
    {
	    super();
	    this.randomizer = (int)(Math.random() * 1E6);
	    this.username = username;
	    this.subscriberId = subscriberId;
	    this.subscriptionType = subscriptionType;
	    this.path = path;
	    this.state = state;
    }

	public SubscriptionData(ObjectId objectId, String username, String subscriberId,
			String subscriptionId,  String subscriptionType, String path)
    {
	    super();
	    this.randomizer = (int)(Math.random() * 1E6);
	    this.objectId = objectId;
	    this.username = username;
	    this.subscriberId = subscriberId;
	    this.subscriptionId = subscriptionId;
	    this.subscriptionType = subscriptionType;
	    this.path = path;
    }

	public SubscriptionData addSubscription(Subscription subscription)
	{
		SubscriptionData subscriptionData = new SubscriptionData(objectId, username, subscriberId,
				subscription.getId(), subscriptionType, path);
		subscriptionData.randomizer = randomizer;
		return subscriptionData;
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

	public String getSubscriptionType()
	{
		return subscriptionType;
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
	public String getPath()
	{
		return path;
	}

    public DBObject toDBObject()
    {
    	BasicDBObjectBuilder builder = BasicDBObjectBuilder
        		.start(FIELD_USERNAME, getUsername())
        		.add(FIELD_SUBSCRIBER_ID, getSubscriberId())
        		.add(FIELD_SUBSCRIPTION_TYPE, getSubscriptionType())
        		.add(FIELD_PATH, getPath())
        		.add(FIELD_RANDOMIZER, getRandomizer());
    	if(getSubscriptionId() != null)
    	{
    		builder.add(FIELD_SUBSCRIPTION_ID, getSubscriptionId());
    	}
    	if(getState() != null)
    	{
    		builder.add(FIELD_STATE, getState().toString());
    	}
    	DBObject dbObject = builder.get();
    	return dbObject;
    }

    public static SubscriptionData fromDBObject(DBObject dbObject)
    {
    	ObjectId id = (ObjectId)dbObject.get("_id");
    	String username = (String)dbObject.get(FIELD_USERNAME);
    	String subscriberId = (String)dbObject.get(FIELD_SUBSCRIBER_ID);
    	String subscriptionId = (String)dbObject.get(FIELD_SUBSCRIPTION_ID);
    	int randomizer = (Integer)dbObject.get(FIELD_RANDOMIZER);
    	String stateStr = (String)dbObject.get(FIELD_STATE);
    	DataCreationState state = (stateStr != null ? DataCreationState.valueOf(stateStr) : null);
    	String subscriptionType = (String)dbObject.get(FIELD_SUBSCRIPTION_TYPE);
    	String path = (String)dbObject.get(FIELD_PATH);
    	SubscriptionData subscriptionData = new SubscriptionData(id, username, subscriberId, subscriptionId,
    			subscriptionType, path, state, randomizer);
    	return subscriptionData;
    }
}

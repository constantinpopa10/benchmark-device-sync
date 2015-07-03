package org.alfresco.bm.devicesync.data;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class UploadFileData
{
	private String username;
	private String subscriberId;
	private String subscriptionId;
	private String siteId;
	private String siteRole;
	private String path;
	private Integer numChildren;
	private Integer numChildFolders;

	public UploadFileData(String username, String subscriberId, String subscriptionId, String siteId,
			String siteRole, String path, Integer numChildren, Integer numChildFolders)
    {
	    super();
	    this.username = username;
	    this.subscriberId = subscriberId;
	    this.subscriptionId = subscriptionId;
	    this.siteId = siteId;
	    this.siteRole = siteRole;
	    this.path = path;
	    this.numChildren = numChildren;
	    this.numChildFolders = numChildFolders;
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

	public String getSiteId()
	{
		return siteId;
	}

	public String getSiteRole()
	{
		return siteRole;
	}

	public String getPath()
	{
		return path;
	}

	public Integer getNumChildren()
	{
		return numChildren;
	}

	public Integer getNumChildFolders()
	{
		return numChildFolders;
	}

	public DBObject toDBObject()
    {
    	BasicDBObjectBuilder builder = BasicDBObjectBuilder
    			.start("username", username)
        		.add("subscriberId", subscriberId)
        		.add("subscriptionId", subscriptionId)
        		.add("siteId", siteId)
        		.add("siteRole", siteRole)
        		.add("path", path)
        		.add("numChildren", numChildren)
        		.add("numChildFolders", numChildFolders);
    	DBObject dbObject = builder.get();
    	return dbObject;
    }

    public static UploadFileData fromDBObject(DBObject dbObject)
    {
    	String username = (String)dbObject.get("username");
    	String subscriberId = (String)dbObject.get("subscriberId");
    	String subscriptionId = (String)dbObject.get("subscriptionId");
    	String siteId = (String)dbObject.get("siteId");
    	String siteRole = (String)dbObject.get("siteRole");
    	String path = (String)dbObject.get("path");
    	Integer numChildren = (Integer)dbObject.get("numChildren");
    	Integer numChildFolders = (Integer)dbObject.get("numChildFolders");
    	UploadFileData uploadFileData = new UploadFileData(username, subscriberId, subscriptionId, siteId, siteRole,
    			path, numChildren, numChildFolders);
    	return uploadFileData;
    }

	@Override
    public String toString()
    {
	    return "UploadFileData [username=" + username + ", subscriberId="
	            + subscriberId + ", subscriptionId=" + subscriptionId
	            + ", siteId=" + siteId + ", siteRole=" + siteRole + ", path="
	            + path + ", numChildren=" + numChildren + ", numChildFolders="
	            + numChildFolders + "]";
    }

    
}

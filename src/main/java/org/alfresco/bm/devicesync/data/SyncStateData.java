package org.alfresco.bm.devicesync.data;

import java.util.Collections;

import org.alfresco.bm.devicesync.dao.SyncsService;
import org.alfresco.service.synchronization.api.GetChangesResponse;
import org.alfresco.service.synchronization.api.StartSyncRequest;
import org.bson.types.ObjectId;
import org.springframework.social.alfresco.api.Alfresco;
import org.springframework.social.alfresco.api.entities.StartSyncResponse;

/**
 * 
 * @author sglover
 *
 */
public class SyncStateData
{
	private Alfresco alfresco;
	private Exception exception;
	private ObjectId objectId;
	private String subscriberId;
	private String subscriptionId;
	private String syncId;
	private String username;
	private GetChangesResponse response;
	private SyncState syncState;

	public SyncStateData(Alfresco alfresco, ObjectId objectId, String username, String subscriberId, String subscriptionId)
	{
		this.objectId = objectId;
		this.alfresco = alfresco;
		this.username = username;
		this.subscriberId = subscriberId;
		this.subscriptionId = subscriptionId;
		this.syncState = SyncState.NotScheduled;
	}

	private SyncStateData syncId(SyncsService syncsService, String syncId)
	{
		SyncStateData syncStateData = new SyncStateData(alfresco, objectId, username, subscriberId, subscriptionId);
		syncStateData.syncId = syncId;
		syncStateData.syncState = SyncState.Started;
		syncsService.updateSync(objectId, syncId, SyncState.Started, null);
		return syncStateData;
	}

	private SyncStateData ready(SyncsService syncsService)
	{
		SyncStateData syncStateData = new SyncStateData(alfresco, objectId, username, subscriberId, subscriptionId);
		syncStateData.syncState = SyncState.Ready;
		syncsService.updateSync(syncId, SyncState.Ready, null);
		return syncStateData;
	}

	private SyncStateData notReady(SyncsService syncsService)
	{
		SyncStateData syncStateData = new SyncStateData(alfresco, objectId, username, subscriberId, subscriptionId);
		syncStateData.syncState = SyncState.NotReady;
		syncsService.updateSync(syncId, SyncState.NotReady, null);
		return syncStateData;
	}

	private SyncStateData exception(SyncsService syncsService, Exception e)
	{
		SyncStateData syncStateData = new SyncStateData(alfresco, objectId, username, subscriberId, subscriptionId);
		syncStateData.exception = e;
		syncStateData.syncState = SyncState.Exception;
		syncsService.updateSync(getSyncId(), SyncState.Exception, e.toString());
		return syncStateData;
	}

	private SyncStateData error(SyncsService syncsService, String message)
	{
		SyncStateData syncStateData = new SyncStateData(alfresco, objectId, username, subscriberId, subscriptionId);
		syncStateData.syncState = SyncState.Error;
		syncsService.updateSync(getSyncId(), SyncState.Error, message);
		return syncStateData;
	}

	private SyncStateData cancelled(SyncsService syncsService)
	{
		SyncStateData syncStateData = new SyncStateData(alfresco, objectId, username, subscriberId, subscriptionId);
		syncStateData.syncState = SyncState.Cancelled;
		syncsService.updateSync(getSyncId(), SyncState.Cancelled, null);
		return syncStateData;
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

	public Exception getException()
	{
		return exception;
	}

	public ObjectId getObjectId()
	{
		return objectId;
	}

	public SyncState getSyncState()
	{
		return syncState;
	}

	public SyncStateData startSync(SyncsService syncsService)
	{
		SyncStateData ret = null;

		try
		{
			StartSyncRequest req = new StartSyncRequest(Collections.emptyList());
			StartSyncResponse response = alfresco.startSync(req, "-default-", subscriberId, subscriptionId);
    		ret = syncId(syncsService, response.getSyncId());
		}
		catch(Exception e)
		{
			ret = exception(syncsService, e);
		}

		return ret;
	}

	public SyncStateData getSync(SyncsService syncsService)
	{
		SyncStateData ret = null;

		try
		{
			if(syncId != null)
			{
				GetChangesResponse response = alfresco.getSync("-default-", getSubscriberId(),
						getSubscriptionId(), syncId);
	
				String status = response.getStatus();
				switch(status)
				{
				case "not ready":
					ret = notReady(syncsService);
					break;
				case "ready":
					ret = ready(syncsService);
					break;
				case "error":
					ret = error(syncsService, response.getMessage());
					break;
				case "cancelled":
					ret = cancelled(syncsService);
					break;
				default:
					// TODO
					ret = this;
				}
			}
			else
			{
				// TODO
			}
		}
		catch(Exception e)
		{
			ret = exception(syncsService, e);
		}

		return ret;
	}

	public SyncStateData endSync(SyncsService syncsService)
	{
		SyncStateData ret = null;

		try
		{
			if(syncId != null)
			{
				alfresco.endSync("-default-", subscriberId, subscriptionId, syncId);
			}
			ret = this;
		}
		catch(Exception e)
		{
			ret = exception(syncsService, e);
		}

		return ret;
	}
}

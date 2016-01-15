package org.alfresco.bm.devicesync.dao;

import java.util.List;

import org.alfresco.bm.devicesync.data.SyncData;
import org.alfresco.bm.devicesync.data.SyncState;
import org.bson.types.ObjectId;

/**
 * 
 * @author sglover
 *
 */
public interface SyncsService
{
    List<SyncData> getSyncs(SyncState state, int skip, int count);

    void addSync(String username, String subscriberId, String subscriptionId,
            SyncState syncState);

    void updateSync(ObjectId objectId, SyncState syncState, String message);

    void updateSync(String syncId, SyncState syncState, String message);

    void updateSync(ObjectId objectId, String syncId, SyncState syncState,
            String message);

    long countSyncs(SyncState state);
}

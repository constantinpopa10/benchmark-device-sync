package org.alfresco.bm.devicesync.data;

/**
 * 
 * @author sglover
 *
 */
public enum SyncState
{
	NotScheduled, Scheduled, Started, NotReady, Ready, Cancelled, Exception, Error, Done;
}

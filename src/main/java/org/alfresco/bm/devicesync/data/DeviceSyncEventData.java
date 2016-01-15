package org.alfresco.bm.devicesync.data;

/**
 * 
 * @author sglover
 *
 */
public class DeviceSyncEventData
{
    private final String sessionId;

    public DeviceSyncEventData(String sessionId)
    {
        super();
        this.sessionId = sessionId;
    }

    public String getSessionId()
    {
        return sessionId;
    }

}

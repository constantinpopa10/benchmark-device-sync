package org.alfresco.bm.devicesync.eventprocessor;

import org.alfresco.bm.devicesync.util.Util;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wrapper for DesktopSync Client Data to exchange between {@link AbstractEventProcessor} classes to execute the
 * benchmark test.
 * 
 * Notes:
 * 
 * 1. this class is NOT serializable to ensure the benchmark test runs on the creating server only. 2. The number of
 * operations is only a field here and will not be altered by any "internal" operation. Please use
 * {@link increaseNumberOfOperations} instead.
 * 
 * @author Frank Becker
 * @since 1.0
 */
public class DeviceSyncEventData
{
    /** Logger for the class */
    private static final Log logger = LogFactory.getLog(DeviceSyncEventData.class);

    /** stores the session ID */
    private final String sessionId;

    /**
     * counts number of operations on the client described by the create data of this
     * 
     * Note: this value will NOT be altered by any operation in this class. Please call
     * {@link increaseNumberOfOperations} instead.
     */
    private int numberOfOperations = 0;

    /**
     * Constructor.
     * 
     * Note: the client itself must be created by a separate call to {@link createClient}.
     * 
     * @param clientRegistry_p
     *            (DesktopSyncClientRegistry) registry to create clients.
     * @param createData_p
     *            (DesktopSyncCreateData) data to create the sync client.
     * @param sessionId_p
     *            (String) session ID.
     */
    public DeviceSyncEventData(String sessionId_p)
    {
        this.sessionId = sessionId_p;

        // verify arguments
        Util.checkStringNotNullOrEmpty(sessionId_p, "sessionId_p");

        if (logger.isDebugEnabled())
        {
            logger.debug("Event data object created for session '" + this.sessionId + "'");
        }
    }

    /**
     * @return number of operations on the client described by the create data of this
     */
    public int getNumberOfOperations()
    {
        return numberOfOperations;
    }

    /**
     * Increases the number of operations on the client described by the create data of this
     */
    public void increaseNumberOfOperations()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("increaseNumberOfOperations: " + sessionId);
        }
        
        this.numberOfOperations++;
    }

    /**
     * @return the sessionId
     */
    public String getSessionId()
    {
        return sessionId;
    }
}

package org.alfresco.bm.devicesync.util;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class UploadFileException extends RuntimeException
{
    private static final long serialVersionUID = 2617467259706902689L;

    private Exception e;
    private DBObject data;

    public UploadFileException(Exception e, DBObject data)
    {
        super();
        this.e = e;
        this.data = data;
    }

    public Exception getE()
    {
        return e;
    }

    public DBObject getData()
    {
        return data;
    }
}

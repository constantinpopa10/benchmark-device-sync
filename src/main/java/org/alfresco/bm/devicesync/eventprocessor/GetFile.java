/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.bm.devicesync.eventprocessor;

import org.alfresco.bm.devicesync.data.CMISEventData;
import org.alfresco.bm.devicesync.util.DownloadFileHelper;
import org.alfresco.bm.devicesync.util.UploadFileException;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * Upload a new file
 * 
 * <h1>Input</h1>
 * 
 * A {@link CMISEventData data object } containing an existing folder.
 * 
 * <h1>Actions</h1>
 * 
 * Upload a random file to the current folder
 * 
 * <h1>Output</h1>
 * 
 * {@link #EVENT_NAME_FILE_UPLOADED}: The {@link CMISEventData data object} with the new file<br/>
 * 
 * @author sglover
 * @since 1.0
 */
public class GetFile extends AbstractCMISEventProcessor
{
    public static final String EVENT_NAME_FILE_DOWNLOADED = "fileDownloaded";

    private String eventNameFileDownloaded;
    private final DownloadFileHelper downloadFileHelper;

    /**
     * @param testFileService               service to provide sample files for upload
     */
    public GetFile(DownloadFileHelper downloadFileHelper)
    {
        super();
        this.downloadFileHelper = downloadFileHelper;
        this.eventNameFileDownloaded = EVENT_NAME_FILE_DOWNLOADED;
    }

    /**
     * Override the {@link #EVENT_NAME_FILE_UPLOADED default} event name for 'file uploaded'.
     */
    public void setEventNameFileDownloaded(String eventNameFileDownloaded)
    {
        this.eventNameFileDownloaded = eventNameFileDownloaded;
    }

    @Override
    protected EventResult processCMISEvent(Event event) throws Exception
    {
        super.suspendTimer();                               // Timer control

        DBObject dbObject = (DBObject)event.getData();
        String username = (String)dbObject.get("username");
        String path = (String)dbObject.get("path");

        try
        {
            super.resumeTimer();                               // Timer control
        	DBObject downloadResult = downloadFileHelper.download(username, path);
            super.suspendTimer();                               // Timer control

            // Done
            Event doneEvent = new Event(eventNameFileDownloaded, downloadResult);
            EventResult result = new EventResult(
                    BasicDBObjectBuilder
                        .start()
                        .append("msg", "Successfully downloaded document.")
                        .append("result", downloadResult)
                        .get(),
                    doneEvent);
            
            // Done
            return result;
        }
        catch(UploadFileException e)
        {
        	e.printStackTrace();
            return new EventResult(
            		BasicDBObjectBuilder
                    	.start()
                    	.append("msg", "Exception downloading document.")
                    	.append("exception", e.getE().getMessage())
                    	.append("document", e.getData())
                    	.get(),
                    false);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
            return new EventResult(
            		BasicDBObjectBuilder
                	.start()
                	.append("msg", "Exception downloading document.")
                	.append("exception", e.getMessage())
                	.get(), false);
        }
    }
}

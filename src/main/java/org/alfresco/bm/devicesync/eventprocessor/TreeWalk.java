package org.alfresco.bm.devicesync.eventprocessor;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.bm.devicesync.data.TreeWalkData;
import org.alfresco.bm.devicesync.util.TreeWalkHelper;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class TreeWalk extends AbstractEventProcessor
{
    private TreeWalkHelper treeWalkHelper;

    public TreeWalk(TreeWalkHelper treeWalkHelper)
    {
        this.treeWalkHelper = treeWalkHelper;
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
        super.suspendTimer();

        DBObject dbObject = (DBObject) event.getData();
        TreeWalkData treeWalkData = TreeWalkData.fromDBObject(dbObject);

        List<Event> nextEvents = new LinkedList<Event>();

        try
        {
            super.resumeTimer();

            treeWalkHelper.treeWalk(treeWalkData, nextEvents);

            super.suspendTimer();

            return new EventResult(treeWalkData.toDBObject(), nextEvents, true);
        }
        catch (Exception e)
        {
            logger.error("Exception occurred during event processing", e);
            throw e;
        }
    }
}

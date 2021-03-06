/**************************************************************************************
 * Copyright (C) 2006-2015 EsperTech Inc. All rights reserved.                        *
 * http://www.espertech.com/esper                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core.service;

import com.espertech.esper.client.EPStatement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.sf.cglib.reflect.FastMethod;
import net.sf.cglib.reflect.FastClass;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.event.NaturalEventBean;
import com.espertech.esper.collection.UniformPair;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Array;

/**
 * A result delivery strategy that uses an "update" method that accepts a underlying array
 * for use in wildcard selection.
 */
public class ResultDeliveryStrategyTypeArr implements ResultDeliveryStrategy
{
    private static Log log = LogFactory.getLog(ResultDeliveryStrategyImpl.class);
    protected final EPStatement statement;
    protected final Object subscriber;
    protected final FastMethod fastMethod;
    protected final Class componentType;

    /**
     * Ctor.
     * @param subscriber is the receiver to method invocations
     * @param method is the method to deliver to
     */
    public ResultDeliveryStrategyTypeArr(EPStatement statement, Object subscriber, Method method, Class componentType)
    {
        this.statement = statement;
        this.subscriber = subscriber;
        FastClass fastClass = FastClass.create(Thread.currentThread().getContextClassLoader(), subscriber.getClass());
        this.fastMethod = fastClass.getMethod(method);
        this.componentType = componentType;
    }

    public void execute(UniformPair<EventBean[]> result)
    {
        Object newData;
        Object oldData;

        if (result == null) {
            newData = null;
            oldData = null;
        }
        else {
            newData = convert(result.getFirst());
            oldData = convert(result.getSecond());
        }

        Object parameters[] = new Object[] {newData, oldData};
        try {
            fastMethod.invoke(subscriber, parameters);
        }
        catch (InvocationTargetException e) {
            ResultDeliveryStrategyImpl.handle(statement.getName(), log, e, parameters, subscriber, fastMethod);
        }
    }

    protected Object convert(EventBean[] events)
    {
        if ((events == null) || (events.length == 0))
        {
            return null;
        }

        Object array = Array.newInstance(componentType, events.length);
        int length = 0;
        for (int i = 0; i < events.length; i++)
        {
            if (events[i] instanceof NaturalEventBean)
            {
                NaturalEventBean natural = (NaturalEventBean) events[i];
                Array.set(array, length, natural.getNatural()[0]);
                length++;
            }
        }

        if (length == 0)
        {
            return null;
        }
        if (length != events.length)
        {
            Object reduced = Array.newInstance(componentType, events.length);
            System.arraycopy(array, 0, reduced, 0, length);
            array = reduced;
        }
        return array;
    }
}

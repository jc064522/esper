/*
 * *************************************************************************************
 *  Copyright (C) 2006-2015 EsperTech, Inc. All rights reserved.                       *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 * *************************************************************************************
 */

package com.espertech.esper.epl.variable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VariableVersionCoord
{
    private static final Log log = LogFactory.getLog(VariableVersionCoord.class);
    private final VariableService variableService;
    private int currentMark;

    public VariableVersionCoord(VariableService variableService)
    {
        this.variableService = variableService;
    }

    public synchronized int setVersionGetMark()
    {
        currentMark++;
        variableService.setLocalVersion();
        log.debug(".setVersionGetMark Thread " + Thread.currentThread().getId() + " *** mark=" + currentMark + " ***");
        return currentMark;
    }

    public synchronized int incMark()
    {
        currentMark++;
        return currentMark;
    }

}

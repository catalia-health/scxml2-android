/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.scxml2.env.javascript;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.ErrorReporter;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.EventDispatcher;
import org.apache.commons.scxml2.SCInstance;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.model.Action;
import org.apache.commons.scxml2.model.CustomAction;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.TransitionTarget;

/**
 * SCXML application for the example JavaScript scripts.
 *
 */
public class JSExampleTest extends TestCase {

    public JSExampleTest(String name) {
        super(name);
    }

    // Test data
    private URL example01;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() {
        example01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/javascript/example-01.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        example01 = null;
    }

    // TEST METHODS

    public void testExample01Sample() throws Exception {

        List<CustomAction> actions  = new ArrayList<CustomAction>();        
        actions.add(new CustomAction("http://my.custom-actions.domain",
            "eventdatamaptest", EventDataMapTest.class));

        SCXML scxml = SCXMLTestHelper.parse(example01,actions);
        Evaluator evaluator = new JSEvaluator();
        Context context = new JSContext();
        exec = SCXMLTestHelper.getExecutor(scxml, context, evaluator);

        assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("end", ((State)currentStates.iterator().
            next()).getId());
    }

    // INNER CLASSES
    
    public static class EventDataMapTest extends Action {
        private static final long serialVersionUID = 1L;

        @Override
        public void execute(EventDispatcher dispatcher, ErrorReporter reporter,
                SCInstance instance, Log log, Collection<TriggerEvent> events)
        throws ModelException,SCXMLExpressionException { 
            events.add(new TriggerEvent("ok",TriggerEvent.SIGNAL_EVENT,"and its ok with me to"));
        }
    }

}


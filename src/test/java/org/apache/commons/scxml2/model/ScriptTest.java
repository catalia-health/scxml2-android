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
package org.apache.commons.scxml2.model;

import java.net.URL;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.env.javascript.JSEvaluator;

public class ScriptTest extends TestCase {

    private URL script01jexl, script01js;
    
    public ScriptTest(String testName) {
        super(testName);
    }

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() {
        script01jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/script-01.xml");
        script01js = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/javascript/script-01.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        script01jexl = script01js = null;
    }

    /**
     * Test JEXL script execution.
     */
    public void testJexlScriptExecution() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor(script01jexl);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("end", currentStates.iterator().next().getId());
    }

    public void testJavaScriptExecution() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor(script01js, new JSEvaluator());
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("end", currentStates.iterator().next().getId());
    }

}

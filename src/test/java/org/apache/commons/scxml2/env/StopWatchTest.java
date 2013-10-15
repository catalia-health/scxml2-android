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
package org.apache.commons.scxml2.env;


import junit.framework.TestCase;

public class StopWatchTest extends TestCase {

    public StopWatchTest(String testName) {
        super(testName);
    }

    private StopWatch stopWatch;

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() {
        stopWatch = new StopWatch();
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        stopWatch = null;
    }

    public void testStopWatch() {
        assertEquals("reset", stopWatch.getCurrentState());
        stopWatch.fireEvent(StopWatch.EVENT_START);
        assertEquals("running", stopWatch.getCurrentState());
        stopWatch.fireEvent(StopWatch.EVENT_SPLIT);
        assertEquals("paused", stopWatch.getCurrentState());
        stopWatch.fireEvent(StopWatch.EVENT_UNSPLIT);
        assertEquals("running", stopWatch.getCurrentState());
        stopWatch.fireEvent(StopWatch.EVENT_STOP);
        assertEquals("stopped", stopWatch.getCurrentState());
        stopWatch.fireEvent(StopWatch.EVENT_RESET);
        assertEquals("reset", stopWatch.getCurrentState());
    }

}


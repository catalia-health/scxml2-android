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
import java.util.List;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;

import junit.framework.TestCase;

public class StateTest extends TestCase {

    public StateTest(String testName) {
        super(testName);
    }

    // Test data
    private State state;
    private URL state01;
    private SCXMLExecutor exec;

    @Override
    public void setUp() {
        state = new State();
        state01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/model/state-01.xml");
    }

    @Override
    public void tearDown() {
        state01 = null;
        exec = null;
    }

    public void testGetTransitionsListNull() {
        assertNull(state.getTransitionsList("event"));
    }
    
    public void testGetTransitionsList() {
        
        state.getTransitionsList().add(new Transition());
        
        assertNotNull(state.getTransitionsList(null));
    }
    
    public void testAddTransitionDoesNotContainKey() {
        Transition transition = new Transition();
        transition.setEvent("event");
        
        state.addTransition(transition);
        
        List<Transition> events = state.getTransitionsList("event");
        
        assertEquals(1, events.size());
        assertEquals("event", events.get(0).getEvent());
    }
    
    public void testAddTransitionContainKey() {
        Transition transition1 = new Transition();
        transition1.setEvent("event");

        Transition transition2 = new Transition();
        transition2.setEvent("event");

        state.addTransition(transition1);
        state.addTransition(transition2);
        
        List<Transition> events = state.getTransitionsList("event");
        
        assertEquals(2, events.size());
    }
    
    public void testGetTransitionList() {
        Transition transition1 = new Transition();
        transition1.setEvent("event");

        Transition transition2 = new Transition();
        transition2.setEvent("event");

        state.addTransition(transition1);
        state.addTransition(transition2);
        
        List<Transition> events = state.getTransitionsList();
        
        assertEquals(2, events.size());
    }
    
    public void testHasHistoryEmpty() {
        assertFalse(state.hasHistory());
    }

    public void testHasHistory() {
        History history = new History();
        
        state.addHistory(history);
        
        assertTrue(state.hasHistory());
    }
    
    public void testIsSimple() {
        assertTrue(state.isSimple());
    }
    
    public void testIsSimpleHasChildren() {
        State state1 = new State();
        
        // redundant cast to remove deprecation warning
        // could be removed in v1.0
        state.addChild(state1);
        
        assertFalse(state.isSimple());
    }
    
    public void testIsCompositeFalse() {
        assertFalse(state.isComposite());
    }
    
    public void testIsCompositeParallel() {
        State child = new State();
        
        state.addChild(child);
        
        assertTrue(state.isComposite());
    }
    
    public void testIsCompositeHasChildren() {
        State state1 = new State();
        
        // redundant cast to remove deprecation warning
        // could be removed in v1.0
        state.addChild(state1);
        
        assertTrue(state.isComposite());
    }
    
    public void testIsRegion() {
        state.setParent(new Parallel());
        
        assertTrue(state.isRegion());
    }
    
    public void testIsRegionNotParallel() {
        state.setParent(new State());
        
        assertFalse(state.isRegion());
    }

    public void testInitialAttribute() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(state01);
        assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        assertNotNull(exec);
        assertEquals("s11", exec.getCurrentStatus().getStates().iterator().next().getId());
    }

}

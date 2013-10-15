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
package org.apache.commons.scxml2;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.scxml2.env.SimpleContext;
import org.apache.commons.scxml2.env.jexl.JexlEvaluator;
import org.apache.commons.scxml2.model.History;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.TransitionTarget;

import junit.framework.TestCase;

public class SCInstanceTest extends TestCase {

    public SCInstanceTest(String testName) {
        super(testName);
    }

    private SCInstance instance;
    
    @Override
    public void setUp() {
        instance = new SCInstance(null);
    }
    
    public void testGetRootContextNull() {
        assertNull(instance.getRootContext());
    }
    
    public void testGetRootContext() {
        Context context = new SimpleContext();
        context.set("name", "value");
        
        instance.setRootContext(context);
        assertEquals("value", instance.getRootContext().get("name"));
    }
    
    public void testGetRootContextEvaluator() {
        Evaluator evaluator = new JexlEvaluator();
        
        instance.setEvaluator(evaluator);
        
        assertTrue(instance.getRootContext() instanceof JexlContext);
    }
    
    public void testGetContext() {
        TransitionTarget target = new State();
        target.setId("1");
        
        Context context = new SimpleContext();
        context.set("name", "value");
        
        instance.setContext(target, context);
        
        assertEquals("value", instance.getContext(target).get("name"));
    }
    
    public void testGetContextNullParent() {
        TransitionTarget target = new State();
        target.setId("1");

        Context context = new SimpleContext();
        context.set("name", "value");
        instance.setRootContext(context);

        Evaluator evaluator = new JexlEvaluator();
        instance.setEvaluator(evaluator);

        assertEquals("value", instance.getContext(target).get("name"));
        assertEquals("value", instance.lookupContext(target).get("name"));
    }

    public void testGetContextParent() {
        TransitionTarget target = new State();
        target.setId("1");
        
        State parent = new State();
        parent.setId("parent");
        
        target.setParent(parent);

        Context context = new SimpleContext();
        context.set("name", "value");
        instance.setRootContext(context);

        Evaluator evaluator = new JexlEvaluator();
        instance.setEvaluator(evaluator);

        assertEquals("value", instance.getContext(target).get("name"));
        assertEquals("value", instance.lookupContext(target).get("name"));
    }

    public void testGetLastConfigurationNull() {
        History history = new History();
        
        Set<TransitionTarget> returnConfiguration = instance.getLastConfiguration(history);
        
        assertEquals(0, returnConfiguration.size());
    }


    public void testGetLastConfiguration() {
        History history = new History();
        history.setId("1");
        
        Set<TransitionTarget> configuration = new HashSet<TransitionTarget>();
        TransitionTarget tt1 = new State();
        TransitionTarget tt2 = new State();
        configuration.add(tt1);
        configuration.add(tt2);
        
        instance.setLastConfiguration(history, configuration);  
        
        Set<TransitionTarget> returnConfiguration = instance.getLastConfiguration(history);
        
        assertEquals(2, returnConfiguration.size());
        assertTrue(returnConfiguration.contains(tt1));
        assertTrue(returnConfiguration.contains(tt2));
    }
    
    public void testIsEmpty() {
        assertTrue(instance.isEmpty(new History()));
    }
    
    public void testIsEmptyFalse() {
        History history = new History();
        history.setId("1");
        
        Set<TransitionTarget> configuration = new HashSet<TransitionTarget>();
        TransitionTarget tt1 = new State();
        configuration.add(tt1);
        
        instance.setLastConfiguration(history, configuration);  

        assertFalse(instance.isEmpty(history));
    }
    
    public void testReset() {
        History history = new History();
        history.setId("1");
        
        Set<TransitionTarget> configuration = new HashSet<TransitionTarget>();
        TransitionTarget tt1 = new State();
        configuration.add(tt1);
        
        instance.setLastConfiguration(history, configuration);  

        instance.reset(history);
        
        assertTrue(instance.isEmpty(history));
    }
    
}

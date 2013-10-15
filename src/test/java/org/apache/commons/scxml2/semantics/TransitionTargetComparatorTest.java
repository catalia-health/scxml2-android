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
package org.apache.commons.scxml2.semantics;

import java.util.Comparator;

import junit.framework.TestCase;
import org.apache.commons.scxml2.model.Parallel;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.TransitionTarget;

public class TransitionTargetComparatorTest extends TestCase {

    public TransitionTargetComparatorTest(String testName) {
        super(testName);
    }

    private Comparator<TransitionTarget> comparator;
    
    @Override
    public void setUp() {
        comparator = new TransitionTargetComparator<TransitionTarget>();
    }
    
    public void testComparatorEquals() {
        TransitionTarget target = new State();
        
        assertEquals(0, comparator.compare(target, target));
    }
    
    public void testComparatorNegative() {
        TransitionTarget target1 = new State();
        TransitionTarget target2 = new State();
        
        target1.setParent(target2);
        
        assertEquals(-1, comparator.compare(target1, target2));
    }
    
    public void testComparatorPositive() {
        TransitionTarget target1 = new State();
        TransitionTarget target2 = new State();
        
        target2.setParent(target1);
        
        assertEquals(1, comparator.compare(target1, target2));
    }
    
    public void testComparatorFirstMoreParents() {
        TransitionTarget target1 = new State();
        TransitionTarget parent1 = new State();
        TransitionTarget parent2 = new State();

        parent1.setParent(parent2);
        target1.setParent(parent1);
        
        TransitionTarget target2 = new State();
        TransitionTarget parent3 = new State();
        
        target2.setParent(parent3);
        
        assertEquals(-1, comparator.compare(target1, target2));
    }
    
    public void testComparatorSecondMoreParents() {
        TransitionTarget target1 = new State();
        TransitionTarget parent1 = new State();
        TransitionTarget parent2 = new State();

        parent1.setParent(parent2);
        target1.setParent(parent1);
        
        TransitionTarget target2 = new State();
        TransitionTarget parent3 = new State();
        
        target2.setParent(parent3);
        
        assertEquals(1, comparator.compare(target2, target1)); // reversed
    }
    
    public void testComparatorSameParent() {
        State target1 = new State();
        Parallel parent = new Parallel();
        target1.setParent(parent);
        parent.addChild(target1);
        
        State target2 = new State();
        target2.setParent(parent);
        parent.addChild(target2);
        
        assertEquals(1, comparator.compare(target1, target2));
    }
}

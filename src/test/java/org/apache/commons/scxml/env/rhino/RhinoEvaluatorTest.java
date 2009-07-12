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
package org.apache.commons.scxml.env.rhino;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.env.rhino.RhinoContext;
import org.apache.commons.scxml.env.rhino.RhinoEvaluator;

public class RhinoEvaluatorTest extends TestCase {

    private String BAD_EXPRESSION = ">";
    private Context ctx = new RhinoContext();

    public RhinoEvaluatorTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(RhinoEvaluatorTest.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = {RhinoEvaluatorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    public void testPristine() throws SCXMLExpressionException {
        Evaluator eval = new RhinoEvaluator();
        assertNotNull(eval);
        assertTrue(eval.evalCond(ctx, "1+1 == 2").booleanValue());
    }

    public void testErrorMessage() {
        Evaluator eval = new RhinoEvaluator();
        assertNotNull(eval);
        try {
            eval.eval(ctx, BAD_EXPRESSION);
            fail("RhinoEvaluator should throw SCXMLExpressionException");
        } catch (SCXMLExpressionException e) {
            assertTrue("RhinoEvaluator: Incorrect error message",
                e.getMessage().startsWith("eval('" + BAD_EXPRESSION + "'):"));
        }
    }

}


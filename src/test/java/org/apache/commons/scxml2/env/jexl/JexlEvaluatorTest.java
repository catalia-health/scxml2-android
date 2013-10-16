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
package org.apache.commons.scxml2.env.jexl;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.junit.Assert;
import org.junit.Test;

public class JexlEvaluatorTest {

    private String BAD_EXPRESSION = ">";
    private Context ctx = new JexlContext();

    @Test
    public void testPristine() throws SCXMLExpressionException {
        Evaluator eval = new JexlEvaluator();
        Assert.assertNotNull(eval);
        Assert.assertTrue(((Boolean) eval.eval(ctx, "1+1 eq 2")).booleanValue());
    }
    
    @Test
    public void testErrorMessage() {
        Evaluator eval = new JexlEvaluator();
        Assert.assertNotNull(eval);
        try {
            eval.eval(ctx, BAD_EXPRESSION);
            Assert.fail("JexlEvaluator should throw SCXMLExpressionException");
        } catch (SCXMLExpressionException e) {
            Assert.assertTrue("JexlEvaluator: Incorrect error message",
                e.getMessage().startsWith("eval('" + BAD_EXPRESSION + "'):"));
        }
    }

}

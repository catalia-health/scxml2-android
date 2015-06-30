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

import java.io.Serializable;

import android.util.Log;
import org.apache.commons.scxml2.SCXMLListener;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.Transition;
import org.apache.commons.scxml2.model.TransitionTarget;


/**
 * Simple SCXML Listener that logs execution.
 */
public class SimpleSCXMLListener implements SCXMLListener, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    private static final String TAG = "SimpleSCXMLListener";

    /**
     * @see SCXMLListener#onEntry(EnterableState)
     */
    public void onEntry(final EnterableState state) {
        Log.i(TAG, "enter " + LogUtils.getTTPath(state));
    }

    /**
     * @see SCXMLListener#onExit(EnterableState)
     */
    public void onExit(final EnterableState state) {
        Log.i(TAG, "exit " + LogUtils.getTTPath(state));
    }

    /**
* @see SCXMLListener#onTransition(TransitionTarget,TransitionTarget,Transition,String)
     */
    public void onTransition(final TransitionTarget from,
            final TransitionTarget to, final Transition transition, String event) {
        Log.i(TAG, "transition " + LogUtils.transToString(from, to, transition, event));
    }

}


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
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Custom error handler that logs the parsing errors in the
 * SCXML document.
 */
public class SimpleErrorHandler implements ErrorHandler, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Message prefix. */
    private static final String MSG_PREFIX = "SCXML SAX Parsing: ";
    /** Message postfix. */
    private static final String MSG_POSTFIX = " Correct the SCXML document.";
    private static final String TAG = "SimpleErrorHandler";

    /**
     * Constructor.
     */
    public SimpleErrorHandler() {
        super();
    }

    /**
     * @see ErrorHandler#error(SAXParseException)
     */
    public void error(final SAXParseException exception) {
        Log.e(TAG, MSG_PREFIX + exception.getMessage() + MSG_POSTFIX,
                exception);
    }

    /**
     * @see ErrorHandler#fatalError(SAXParseException)
     */
    public void fatalError(final SAXParseException exception) {
        Log.e(TAG, MSG_PREFIX + exception.getMessage() + MSG_POSTFIX,
            exception);
    }

    /**
     * @see ErrorHandler#warning(SAXParseException)
     */
    public void warning(final SAXParseException exception) {
        Log.w(TAG, MSG_PREFIX + exception.getMessage() + MSG_POSTFIX,
            exception);
    }
}


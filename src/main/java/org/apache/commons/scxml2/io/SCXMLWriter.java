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
package org.apache.commons.scxml2.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.scxml2.model.Action;
import org.apache.commons.scxml2.model.Assign;
import org.apache.commons.scxml2.model.Cancel;
import org.apache.commons.scxml2.model.Content;
import org.apache.commons.scxml2.model.Data;
import org.apache.commons.scxml2.model.Datamodel;
import org.apache.commons.scxml2.model.Else;
import org.apache.commons.scxml2.model.ElseIf;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.Raise;
import org.apache.commons.scxml2.model.ExternalContent;
import org.apache.commons.scxml2.model.Final;
import org.apache.commons.scxml2.model.Finalize;
import org.apache.commons.scxml2.model.Foreach;
import org.apache.commons.scxml2.model.History;
import org.apache.commons.scxml2.model.If;
import org.apache.commons.scxml2.model.Initial;
import org.apache.commons.scxml2.model.Invoke;
import org.apache.commons.scxml2.model.Log;
import org.apache.commons.scxml2.model.OnEntry;
import org.apache.commons.scxml2.model.OnExit;
import org.apache.commons.scxml2.model.Parallel;
import org.apache.commons.scxml2.model.Param;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.Script;
import org.apache.commons.scxml2.model.Send;
import org.apache.commons.scxml2.model.SimpleTransition;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.Transition;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.apache.commons.scxml2.model.Var;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

/**
 * <p>Utility class for serializing the Commons SCXML Java object
 * model. Class uses the visitor pattern to trace through the
 * object heirarchy. Used primarily for testing, debugging and
 * visual verification.</p>
 *
 * <b>NOTE:</b> This writer makes the following assumptions about the
 * original SCXML document(s) parsed to create the object model:
 * <ul>
 *  <li>The default document namespace is the SCXML namespace:
 *      <i>http://www.w3.org/2005/07/scxml</i></li>
 *  <li>The Commons SCXML namespace
 *      ( <i>http://commons.apache.org/scxml</i> ), if needed, uses the
 *      &quot;<i>cs</i>&quot; prefix</li>
 *  <li>All namespace prefixes needed throughout the document are
 *      declared on the document root element (&lt;scxml&gt;)</li>
 * </ul>
 *
 * @since 1.0
 */
public class SCXMLWriter {

    //---------------------- PRIVATE CONSTANTS ----------------------//
    //---- NAMESPACES ----//
    /**
     * The SCXML namespace.
     */
    private static final String XMLNS_SCXML = "http://www.w3.org/2005/07/scxml";

    /**
     * The Commons SCXML namespace.
     */
    private static final String XMLNS_COMMONS_SCXML = "http://commons.apache.org/scxml";

    //---- ERROR MESSAGES ----//
    /**
     * Null OutputStream passed as argument.
     */
    private static final String ERR_NULL_OSTR = "Cannot write to null OutputStream";

    /**
     * Null Writer passed as argument.
     */
    private static final String ERR_NULL_WRIT = "Cannot write to null Writer";

    /**
     * Null Result passed as argument.
     */
    private static final String ERR_NULL_RES = "Cannot parse null Result";

    //--------------------------- XML VOCABULARY ---------------------------//
    //---- ELEMENT NAMES ----//
    private static final String ELEM_ASSIGN = "assign";
    private static final String ELEM_CANCEL = "cancel";
    private static final String ELEM_CONTENT = "content";
    private static final String ELEM_DATA = "data";
    private static final String ELEM_DATAMODEL = "datamodel";
    private static final String ELEM_ELSE = "else";
    private static final String ELEM_ELSEIF = "elseif";
    private static final String ELEM_RAISE = "raise";
    private static final String ELEM_FINAL = "final";
    private static final String ELEM_FINALIZE = "finalize";
    private static final String ELEM_HISTORY = "history";
    private static final String ELEM_IF = "if";
    private static final String ELEM_INITIAL = "initial";
    private static final String ELEM_INVOKE = "invoke";
    private static final String ELEM_FOREACH = "foreach";
    private static final String ELEM_LOG = "log";
    private static final String ELEM_ONENTRY = "onentry";
    private static final String ELEM_ONEXIT = "onexit";
    private static final String ELEM_PARALLEL = "parallel";
    private static final String ELEM_PARAM = "param";
    private static final String ELEM_SCRIPT = "script";
    private static final String ELEM_SCXML = "scxml";
    private static final String ELEM_SEND = "send";
    private static final String ELEM_STATE = "state";
    private static final String ELEM_TRANSITION = "transition";
    private static final String ELEM_VAR = "var";

    //---- ATTRIBUTE NAMES ----//
    private static final String ATTR_ARRAY = "array";
    private static final String ATTR_ATTR = "attr";
    private static final String ATTR_AUTOFORWARD = "autoforward";
    private static final String ATTR_COND = "cond";
    private static final String ATTR_DATAMODEL = "datamodel";
    private static final String ATTR_DELAY = "delay";
    private static final String ATTR_DELAYEXPR = "delayexpr";
    private static final String ATTR_EVENT = "event";
    private static final String ATTR_EVENTEXPR = "eventexpr";
    private static final String ATTR_EXMODE = "exmode";
    private static final String ATTR_EXPR = "expr";
    private static final String ATTR_HINTS = "hints";
    private static final String ATTR_ID = "id";
    private static final String ATTR_IDLOCATION = "idlocation";
    private static final String ATTR_INDEX = "index";
    private static final String ATTR_INITIAL = "initial";
    private static final String ATTR_ITEM = "item";
    private static final String ATTR_LABEL = "label";
    private static final String ATTR_LOCATION = "location";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_NAMELIST = "namelist";
    private static final String ATTR_PROFILE = "profile";
    private static final String ATTR_SENDID = "sendid";
    private static final String ATTR_SRC = "src";
    private static final String ATTR_SRCEXPR = "srcexpr";
    private static final String ATTR_TARGET = "target";
    private static final String ATTR_TARGETEXPR = "targetexpr";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_TYPEEXPR = "typeexpr";
    private static final String ATTR_VERSION = "version";

    //------------------------- STATIC MEMBERS -------------------------//
    /**
     * The JAXP transformer.
     */
    private static final Transformer XFORMER = getTransformer();
    private static final String TAG = "SCXMLWriter";

    //------------------------- PUBLIC API METHODS -------------------------//
    /**
     * Write out the Commons SCXML object model as an SCXML document (used
     * primarily for testing, debugging and visual verification), returned as
     * a string.
     *
     * @param scxml The object model to serialize.
     *
     * @return The corresponding SCXML document as a string.
     *
     * @throws IOException An IO error during serialization.
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    public static String write(final SCXML scxml)
            throws IOException, XmlPullParserException {

        return write(scxml, new Configuration(true, true));
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document (used
     * primarily for testing, debugging and visual verification) using the
     * supplied {@link Configuration}, and return as a string.
     *
     * @param scxml The object model to serialize.
     * @param configuration The {@link Configuration} to use while serializing.
     *
     * @return The corresponding SCXML document as a string.
     *
     * @throws IOException An IO error during serialization.
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    public static String write(final SCXML scxml, final Configuration configuration)
            throws IOException, XmlPullParserException {

        // Must be true since we want to return a string
        configuration.writeToString = true;
        writeInternal(scxml, configuration, null, null, null);
        if (configuration.usePrettyPrint) {
            return configuration.prettyPrintOutput;
        } else {
            configuration.internalWriter.flush();
            return configuration.internalWriter.toString();
        }
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document to the
     * supplied {@link OutputStream}.
     *
     * @param scxml The object model to write out.
     * @param scxmlStream The {@link OutputStream} to write to.
     *
     * @throws IOException An IO error during serialization.
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    public static void write(final SCXML scxml, final OutputStream scxmlStream)
            throws IOException, XmlPullParserException {

        write(scxml, scxmlStream, new Configuration());
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document to the
     * supplied {@link OutputStream} using the given {@link Configuration}.
     *
     * @param scxml The object model to write out.
     * @param scxmlStream The {@link OutputStream} to write to.
     * @param configuration The {@link Configuration} to use.
     *
     * @throws IOException An IO error during serialization.
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    public static void write(final SCXML scxml, final OutputStream scxmlStream, final Configuration configuration)
            throws IOException, XmlPullParserException {

        if (scxmlStream == null) {
            throw new IllegalArgumentException(ERR_NULL_OSTR);
        }
        writeInternal(scxml, configuration, scxmlStream, null, null);
        if (configuration.closeUnderlyingWhenDone) {
            scxmlStream.flush();
            scxmlStream.close();
        }
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document to the
     * supplied {@link Writer}.
     *
     * @param scxml The object model to write out.
     * @param scxmlWriter The {@link Writer} to write to.
     *
     * @throws IOException An IO error during serialization.
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    public static void write(final SCXML scxml, final Writer scxmlWriter)
            throws IOException, XmlPullParserException {

        write(scxml, scxmlWriter, new Configuration());
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document to the
     * supplied {@link Writer} using the given {@link Configuration}.
     *
     * @param scxml The object model to write out.
     * @param scxmlWriter The {@link Writer} to write to.
     * @param configuration The {@link Configuration} to use.
     *
     * @throws IOException An IO error during serialization.
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    public static void write(final SCXML scxml, final Writer scxmlWriter, final Configuration configuration)
            throws IOException, XmlPullParserException {

        if (scxmlWriter == null) {
            throw new IllegalArgumentException(ERR_NULL_WRIT);
        }
        writeInternal(scxml, configuration, null, scxmlWriter, null);
        if (configuration.closeUnderlyingWhenDone) {
            scxmlWriter.flush();
            scxmlWriter.close();
        }
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document to the
     * supplied {@link Result}.
     *
     * @param scxml The object model to write out.
     * @param scxmlResult The {@link Result} to write to.
     *
     * @throws IOException An IO error during serialization.
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    public static void write(final SCXML scxml, final Result scxmlResult)
            throws IOException, XmlPullParserException {

        write(scxml, scxmlResult, new Configuration());
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document to the
     * supplied {@link Result} using the given {@link Configuration}.
     *
     * @param scxml The object model to write out.
     * @param scxmlResult The {@link Result} to write to.
     * @param configuration The {@link Configuration} to use.
     *
     * @throws IOException An IO error during serialization.
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    public static void write(final SCXML scxml, final Result scxmlResult, final Configuration configuration)
            throws IOException, XmlPullParserException {

        if (scxmlResult == null) {
            throw new IllegalArgumentException(ERR_NULL_RES);
        }
        writeInternal(scxml, configuration, null, null, scxmlResult);
    }

    //---------------------- PRIVATE UTILITY METHODS ----------------------//

    /**
     * Escape XML strings for serialization.
     * The basic algorithm is taken from Commons Lang (see oacl.Entities.java)
     *
     * @param str A string to be escaped
     * @return The escaped string
     */
    private static String escapeXML(final String str) {
        if (str == null) {
            return null;
        }

        // Make the writer an arbitrary bit larger than the source string
        int len = str.length();
        StringWriter stringWriter = new StringWriter(len + 8);

        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            String entityName = null; // Look for XML 1.0 predefined entities
            switch (c) {
                case '"':
                    entityName = "quot";
                    break;
                case '&':
                    entityName = "amp";
                    break;
                case '<':
                    entityName = "lt";
                    break;
                case '>':
                    entityName = "gt";
                    break;
                default:
            }
            if (entityName == null) {
                if (c > 0x7F) {
                    stringWriter.write("&#");
                    stringWriter.write(Integer.toString(c));
                    stringWriter.write(';');
                } else {
                    stringWriter.write(c);
                }
            } else {
                stringWriter.write('&');
                stringWriter.write(entityName);
                stringWriter.write(';');
            }
        }

        return stringWriter.toString();
    }

    /**
     * Write out the Commons SCXML object model using the supplied {@link Configuration}.
     * Exactly one of the stream, writer or result parameters must be provided.
     *
     * @param scxml The object model to write out.
     * @param configuration The {@link Configuration} to use.
     * @param scxmlStream The optional {@link OutputStream} to write to.
     * @param scxmlWriter The optional {@link Writer} to write to.
     * @param scxmlResult The optional {@link Result} to write to.
     *
     * @throws IOException An IO error during serialization.
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeInternal(final SCXML scxml, final Configuration configuration,
                                      final OutputStream scxmlStream, final Writer scxmlWriter, final Result scxmlResult)
            throws IOException, XmlPullParserException {

        XmlSerializer writer = getWriter(configuration, scxmlStream, scxmlWriter);
        writeDocument(writer, configuration, scxml);
        writer.flush();
        if (configuration.internalWriter != null) {
            configuration.internalWriter.flush();
        }
        if (configuration.usePrettyPrint) {
            Writer prettyPrintWriter = (scxmlWriter != null ? scxmlWriter : new StringWriter());
            writePretty(configuration, scxmlStream, prettyPrintWriter, scxmlResult);
            if (configuration.writeToString) {
                prettyPrintWriter.flush();
                configuration.prettyPrintOutput = prettyPrintWriter.toString();
            }
        }
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document using the supplied {@link Configuration}.
     * This method tackles the XML document level concerns.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param configuration The {@link Configuration} in use.
     * @param scxml The root of the object model to write out.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeDocument(final XmlSerializer writer, final Configuration configuration,
                                      final SCXML scxml)
            throws XmlPullParserException, IOException {

        String encoding = "UTF-8";
        if (configuration.encoding != null) {
            encoding = configuration.encoding;
        }
        writer.startDocument(encoding, false);
        writeSCXML(writer, scxml);
        writer.endDocument();
    }

    /**
     * Write out this {@link SCXML} object into its serialization as the corresponding &lt;scxml&gt; element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param scxml The root of the object model to write out.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeSCXML(final XmlSerializer writer, final SCXML scxml)
            throws XmlPullParserException, IOException {

        // Start
        writer.startTag(XMLNS_SCXML, ELEM_SCXML);

        // Namespaces
/*
        writer.writeNamespace(null, XMLNS_SCXML);
        writer.writeNamespace("cs", XMLNS_COMMONS_SCXML);
        for (Map.Entry<String, String> entry : scxml.getNamespaces().entrySet()) {
            String key = entry.getKey();
            if (key != null && key.trim().length() > 0 && !key.equals("cs")) { // TODO Remove reserved prefixes
                writer.writeNamespace(key, entry.getValue());
            }
        }
*/

        // Attributes
        writeAV(writer, ATTR_VERSION, scxml.getVersion());
        writeAV(writer, ATTR_INITIAL, scxml.getInitial());
        writeAV(writer, ATTR_DATAMODEL, scxml.getDatamodelName());
        writeAV(writer, ATTR_NAME, scxml.getName());
        writeAV(writer, ATTR_PROFILE, scxml.getProfile());
        writeAV(writer, ATTR_EXMODE, scxml.getExmode());

        // Marker to indicate generated document
        writer.comment(XMLNS_COMMONS_SCXML);

        // Write global script if defined
        if (scxml.getGlobalScript() != null) {
            Script s = scxml.getGlobalScript();
            writer.startTag(XMLNS_SCXML, ELEM_SCRIPT);
            writer.cdsect(s.getScript());
            writer.endTag(XMLNS_SCXML, ELEM_SCRIPT);
        }

        // Children
        writeDatamodel(writer, scxml.getDatamodel());
        for (EnterableState es : scxml.getChildren()) {
            if (es instanceof Final) {
                writeFinal(writer, (Final) es);
            } else if (es instanceof State) {
                writeState(writer, (State) es);
            } else if (es instanceof Parallel) {
                writeParallel(writer, (Parallel) es);
            }
        }

        // End
        writer.endTag(XMLNS_SCXML, ELEM_SCXML);
    }

    /**
     * Write out this {@link Datamodel} object into its serialization as the corresponding &lt;datamodel&gt; element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param datamodel The {@link Datamodel} to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeDatamodel(final XmlSerializer writer, final Datamodel datamodel)
            throws XmlPullParserException, IOException {

        if (datamodel == null) {
            return;
        }

        writer.startTag(null, ELEM_DATAMODEL);
        if (datamodel.getData().size() > 0 && XFORMER == null) {
            writer.comment("Datamodel was not serialized");
        } else {
            for (Data d : datamodel.getData()) {
                Node n = d.getNode();
                if (n != null) {
                    writeNode(writer, n);
                } else {
                    writer.startTag(null, ELEM_DATA);
                    writeAV(writer, ATTR_ID, d.getId());
                    writeAV(writer, ATTR_SRC, escapeXML(d.getSrc()));
                    writeAV(writer, ATTR_EXPR, escapeXML(d.getExpr()));
                    writer.endTag(null, ELEM_DATA);
                }
            }
        }
        writer.endTag(null, ELEM_DATAMODEL);
    }

    /**
     * Write out the TransitionTarget id attribute unless it was auto-generated
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param tt The {@link TransitionTarget} for which to write the id attribute.
     * @throws XmlPullParserException
     */
    private static void writeTransitionTargetId(final XmlSerializer writer, final TransitionTarget tt)
            throws XmlPullParserException, IOException {
        if (!tt.getId().startsWith(SCXML.GENERATED_TT_ID_PREFIX)) {
            writeAV(writer, ATTR_ID, tt.getId());
        }
    }

    /**
     * Write out this {@link State} object into its serialization as the corresponding &lt;state&gt; element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param state The {@link State} to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeState(final XmlSerializer writer, final State state)
            throws XmlPullParserException, IOException {

        writer.startTag(null, ELEM_STATE);
        writeTransitionTargetId(writer, state);
        writeAV(writer, ATTR_INITIAL, state.getFirst());
        writeInitial(writer, state.getInitial());
        writeDatamodel(writer, state.getDatamodel());
        writeHistory(writer, state.getHistory());
        for (OnEntry onentry : state.getOnEntries()) {
            writeOnEntry(writer, onentry);
        }

        for (Transition t : state.getTransitionsList()) {
            writeTransition(writer, t);
        }

        for (Invoke inv : state.getInvokes()) {
            writeInvoke(writer, inv);
        }

        for (EnterableState es : state.getChildren()) {
            if (es instanceof Final) {
                writeFinal(writer, (Final) es);
            } else if (es instanceof State) {
                writeState(writer, (State) es);
            } else if (es instanceof Parallel) {
                writeParallel(writer, (Parallel) es);
            }
        }

        for (OnExit onexit : state.getOnExits()) {
            writeOnExit(writer, onexit);
        }
        writer.endTag(null, ELEM_STATE);
    }

    /**
     * Write out this {@link Parallel} object into its serialization as the corresponding &lt;parallel&gt; element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param parallel The {@link Parallel} to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeParallel(final XmlSerializer writer, final Parallel parallel)
            throws XmlPullParserException, IOException {

        writer.startTag(null, ELEM_PARALLEL);
        writeTransitionTargetId(writer, parallel);

        writeDatamodel(writer, parallel.getDatamodel());
        writeHistory(writer, parallel.getHistory());
        for (OnEntry onentry : parallel.getOnEntries()) {
            writeOnEntry(writer, onentry);
        }

        for (Transition t : parallel.getTransitionsList()) {
            writeTransition(writer, t);
        }

        for (Invoke inv : parallel.getInvokes()) {
            writeInvoke(writer, inv);
        }

        for (EnterableState es : parallel.getChildren()) {
            if (es instanceof Final) {
                writeFinal(writer, (Final) es);
            } else if (es instanceof State) {
                writeState(writer, (State) es);
            } else if (es instanceof Parallel) {
                writeParallel(writer, (Parallel) es);
            }
        }

        for (OnExit onexit : parallel.getOnExits()) {
            writeOnExit(writer, onexit);
        }
        writer.endTag(null, ELEM_PARALLEL);
    }

    /**
     * Write out this {@link Final} object into its serialization as the corresponding &lt;final&gt; element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param end The {@link Final} to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeFinal(final XmlSerializer writer, final Final end)
            throws XmlPullParserException, IOException {

        writer.startTag(null, ELEM_FINAL);
        writeTransitionTargetId(writer, end);
        for (OnEntry onentry : end.getOnEntries()) {
            writeOnEntry(writer, onentry);
        }
        for (OnExit onexit : end.getOnExits()) {
            writeOnExit(writer, onexit);
        }
        writer.endTag(null, ELEM_FINAL);
    }

    /**
     * Write out this {@link Initial} object into its serialization as the corresponding &lt;initial&gt; element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param initial The {@link Initial} to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeInitial(final XmlSerializer writer, final Initial initial)
            throws XmlPullParserException, IOException {

        if (initial == null || initial.isGenerated()) {
            return;
        }

        writer.startTag(null, ELEM_INITIAL);
        writeTransition(writer, initial.getTransition());
        writer.endTag(null, ELEM_INITIAL);
    }

    /**
     * Write out this {@link History} list into its serialization as the corresponding set of &lt;history&gt;
     * elements.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param history The {@link History} list to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeHistory(final XmlSerializer writer, final List<History> history)
            throws XmlPullParserException, IOException {

        if (history == null) {
            return;
        }

        for (History h : history) {
            writer.startTag(null, ELEM_HISTORY);
            writeTransitionTargetId(writer, h);
            if (h.isDeep()) {
                writeAV(writer, ATTR_TYPE, "deep");
            } else {
                writeAV(writer, ATTR_TYPE, "shallow");
            }
            writeTransition(writer, h.getTransition());
            writer.endTag(null, ELEM_HISTORY);
        }
    }

    /**
     * Write out this {@link OnEntry} object into its serialization as the corresponding &lt;onentry&gt; element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param onentry The {@link OnEntry} to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeOnEntry(final XmlSerializer writer, final OnEntry onentry)
            throws XmlPullParserException, IOException {

        if (onentry != null && (onentry.isRaiseEvent() || onentry.getActions().size() > 0 )) {
            writer.startTag(null, ELEM_ONENTRY);
            writeAV(writer, ATTR_EVENT, onentry.getRaiseEvent());
            writeExecutableContent(writer, onentry.getActions());
            writer.endTag(null, ELEM_ONENTRY);
        }
    }

    /**
     * Write out this {@link OnExit} object into its serialization as the corresponding &lt;onexit&gt; element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param onexit The {@link OnExit} to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeOnExit(final XmlSerializer writer, final OnExit onexit)
            throws XmlPullParserException, IOException {

        if (onexit != null && (onexit.isRaiseEvent() || onexit.getActions().size() > 0)) {
            writer.startTag(null, ELEM_ONEXIT);
            writeAV(writer, ATTR_EVENT, onexit.getRaiseEvent());
            writeExecutableContent(writer, onexit.getActions());
            writer.endTag(null, ELEM_ONEXIT);
        }
    }

    /**
     * Write out this {@link Transition} object into its serialization as the corresponding &lt;transition&gt; element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param transition The {@link Transition} to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeTransition(final XmlSerializer writer, final SimpleTransition transition)
            throws XmlPullParserException, IOException {

        writer.startTag(null, ELEM_TRANSITION);
        if (transition instanceof Transition) {
            writeAV(writer, ATTR_EVENT, ((Transition)transition).getEvent());
            writeAV(writer, ATTR_COND, escapeXML(((Transition)transition).getCond()));
        }

        writeAV(writer, ATTR_TARGET, transition.getNext());
        if (transition.getType() != null) {
            writeAV(writer, ATTR_TYPE, transition.getType().name());
        }
        writeExecutableContent(writer, transition.getActions());
        writer.endTag(null, ELEM_TRANSITION);
    }

    /**
     * Write out this {@link Invoke} object into its serialization as the corresponding &lt;invoke&gt; element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param invoke The {@link Invoke} to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeInvoke(final XmlSerializer writer, final Invoke invoke)
            throws XmlPullParserException, IOException {

        writer.startTag(null, ELEM_INVOKE);
        writeAV(writer, ATTR_ID, invoke.getId());
        writeAV(writer, ATTR_SRC, invoke.getSrc());
        writeAV(writer, ATTR_SRCEXPR, invoke.getSrcexpr());
        writeAV(writer, ATTR_TYPE, invoke.getType());
        writeAV(writer, ATTR_AUTOFORWARD, invoke.getAutoForward());

        for (Param p : invoke.getParams()) {
            writer.startTag(null, ELEM_PARAM);
            writeAV(writer, ATTR_NAME, p.getName());
            writeAV(writer, ATTR_LOCATION, p.getLocation());
            writeAV(writer, ATTR_EXPR, escapeXML(p.getExpr()));
            writer.endTag(null, ELEM_PARAM);
        }
        writeFinalize(writer, invoke.getFinalize());
        writeContent(writer, invoke.getContent());

        writer.endTag(null, ELEM_INVOKE);
    }

    /**
     * Write out this {@link Finalize} object into its serialization as the corresponding &lt;finalize&gt; element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param finalize The {@link Finalize} to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeFinalize(final XmlSerializer writer, final Finalize finalize)
            throws XmlPullParserException, IOException {

        if (finalize != null && finalize.getActions().size() > 0) {
            writer.startTag(null, ELEM_FINALIZE);
            writeExecutableContent(writer, finalize.getActions());
            writer.endTag(null, ELEM_FINALIZE);
        }
    }

    /**
     * Write out this executable content (list of actions) into its serialization as the corresponding set of action
     * elements. Custom actions aren't serialized.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param actions The list of actions to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeExecutableContent(final XmlSerializer writer, final List<Action> actions)
            throws XmlPullParserException, IOException {

        if (actions == null) {
            return;
        }
        for (Action a : actions) {
            if (a instanceof Assign) {
                Assign asn = (Assign) a;
                writer.startTag(XMLNS_SCXML, ELEM_ASSIGN);
                writeAV(writer, ATTR_LOCATION, asn.getLocation());
                if (asn.getType() != null) {
                    writeAV(writer, ATTR_TYPE, asn.getType().value());
                }
                writeAV(writer, ATTR_ATTR, asn.getAttr());
                writeAV(writer, ATTR_SRC, asn.getSrc());
                writeAV(writer, ATTR_EXPR, escapeXML(asn.getExpr()));
                writer.endTag(XMLNS_SCXML, ELEM_ASSIGN);
            } else if (a instanceof Send) {
                writeSend(writer, (Send) a);
            } else if (a instanceof Cancel) {
                Cancel c = (Cancel) a;
                writer.startTag(XMLNS_SCXML, ELEM_CANCEL);
                writeAV(writer, ATTR_SENDID, c.getSendid());
                writer.endTag(XMLNS_SCXML, ELEM_CANCEL);
            } else if (a instanceof Foreach) {
                writeForeach(writer, (Foreach) a);
            } else if (a instanceof Log) {
                Log lg = (Log) a;
                writer.startTag(XMLNS_SCXML, ELEM_LOG);
                writeAV(writer, ATTR_LABEL, lg.getLabel());
                writeAV(writer, ATTR_EXPR, escapeXML(lg.getExpr()));
                writer.endTag(XMLNS_SCXML, ELEM_LOG);
            } else if (a instanceof Raise) {
                Raise e = (Raise) a;
                writer.startTag(XMLNS_SCXML, ELEM_RAISE);
                writeAV(writer, ATTR_EVENT, e.getEvent());
                writer.endTag(XMLNS_SCXML, ELEM_RAISE);
            } else if (a instanceof Script) {
                Script s = (Script) a;
                writer.startTag(XMLNS_SCXML, ELEM_SCRIPT);
                writer.cdsect(s.getScript());
                writer.endTag(XMLNS_SCXML, ELEM_SCRIPT);
            } else if (a instanceof If) {
                writeIf(writer, (If) a);
            } else if (a instanceof Else) {
                //writer.writeEmptyElement(ELEM_ELSE);
            } else if (a instanceof ElseIf) {
                ElseIf eif = (ElseIf) a;
                writer.startTag(XMLNS_SCXML, ELEM_ELSEIF);
                writeAV(writer, ATTR_COND, escapeXML(eif.getCond()));
                writer.endTag(XMLNS_SCXML, ELEM_ELSEIF);
            } else if (a instanceof Var) {
                Var v = (Var) a;
                writer.startTag(XMLNS_COMMONS_SCXML, ELEM_VAR);
                writeAV(writer, ATTR_NAME, v.getName());
                writeAV(writer, ATTR_EXPR, escapeXML(v.getExpr()));
                writer.endTag(XMLNS_COMMONS_SCXML, ELEM_VAR);
            } else {
                writer.comment("Custom action with class name '" + a.getClass().getName() + "' not serialized");
            }
        }
    }

    /**
     * Write out this {@link Send} object into its serialization as the corresponding &lt;send&gt; element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param send The {@link Send} to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeSend(final XmlSerializer writer, final Send send)
            throws XmlPullParserException, IOException {

        writer.startTag(XMLNS_SCXML, ELEM_SEND);
        writeAV(writer, ATTR_ID, send.getId());
        writeAV(writer, ATTR_IDLOCATION, send.getIdlocation());
        writeAV(writer, ATTR_EVENT, send.getEvent());
        writeAV(writer, ATTR_EVENTEXPR, send.getEventexpr());
        writeAV(writer, ATTR_TARGET, send.getTarget());
        writeAV(writer, ATTR_TARGETEXPR, send.getTargetexpr());
        writeAV(writer, ATTR_TYPE, send.getType());
        writeAV(writer, ATTR_TYPEEXPR, send.getTypeexpr());
        writeAV(writer, ATTR_DELAY, send.getDelay());
        writeAV(writer, ATTR_DELAYEXPR, send.getDelayexpr());
        writeAV(writer, ATTR_NAMELIST, send.getNamelist());
        writeAV(writer, ATTR_HINTS, send.getHints());

        for (Param p : send.getParams()) {
            writer.startTag(null, ELEM_PARAM);
            writeAV(writer, ATTR_NAME, p.getName());
            writeAV(writer, ATTR_LOCATION, p.getLocation());
            writeAV(writer, ATTR_EXPR, escapeXML(p.getExpr()));
            writer.endTag(null, ELEM_PARAM);
        }
        writeContent(writer, send.getContent());

        writer.endTag(XMLNS_SCXML, ELEM_SEND);
    }

    /**
     * Write out this {@link If} object into its serialization as the corresponding &lt;if&gt; element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param iff The {@link If} to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeIf(final XmlSerializer writer, final If iff)
            throws XmlPullParserException, IOException {

        writer.startTag(null, ELEM_IF);
        writeAV(writer, ATTR_COND, escapeXML(iff.getCond()));
        writeExecutableContent(writer, iff.getActions());
        writer.endTag(null, ELEM_IF);
    }

    /**
     * Write out this {@link Foreach} object into its serialization as the corresponding &lt;foreach&gt; element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param foreach The {@link If} to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeForeach(final XmlSerializer writer, final Foreach foreach)
            throws XmlPullParserException, IOException {

        writer.startTag(null, ELEM_FOREACH);
        writeAV(writer, ATTR_ITEM, foreach.getItem());
        writeAV(writer, ATTR_INDEX, foreach.getIndex());
        writeAV(writer, ATTR_ARRAY, escapeXML(foreach.getArray()));
        writeExecutableContent(writer, foreach.getActions());
        writer.endTag(null, ELEM_FOREACH);
    }

    /**
     * Write the {@link Content} element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param content The content element to write.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeContent(final XmlSerializer writer, final Content content)
            throws XmlPullParserException, IOException {

        if (content != null) {
            writer.startTag(null, ELEM_CONTENT);
            writeAV(writer, ATTR_EXPR, content.getExpr());
            if (content.getBody() != null) {
                if (content.getBody() instanceof Node) {
                    NodeList nodeList = ((Node)content.getBody()).getChildNodes();
                    if (nodeList.getLength() > 0 && XFORMER == null) {
                        writer.comment("External content was not serialized");
                    }
                    else {
                        for (int i = 0, size = nodeList.getLength(); i < size; i++) {
                            writeNode(writer, nodeList.item(i));
                        }
                    }
                }
                else {
                    writer.text(content.getBody().toString());
                }
            }
            writer.endTag(null, ELEM_CONTENT);
        }
    }

    /**
     * Write the serialized body of this {@link ExternalContent} element.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param externalContent The model element containing the external body content.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeExternalContent(final XmlSerializer writer,
                                             final ExternalContent externalContent)
            throws XmlPullParserException, IOException {

        List<Node> externalNodes = externalContent.getExternalNodes();

        if (externalNodes.size() > 0 && XFORMER == null) {
            writer.comment("External content was not serialized");
        } else {
            for (Node n : externalNodes) {
                writeNode(writer, n);
            }
        }
    }

    /**
     * Write out this {@link Node} object into its serialization.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param node The {@link Node} to serialize.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeNode(final XmlSerializer writer, final Node node)
            throws XmlPullParserException, IOException {

        Source input = new DOMSource(node);
        StringWriter out = new StringWriter();
        Result output = new StreamResult(out);
        try {
            XFORMER.transform(input, output);
        } catch (TransformerException te) {
            android.util.Log.e(TAG, te.getMessage(), te);
            writer.comment("TransformerException: Node was not serialized");
        }
        writer.text(out.toString());
    }

    /**
     * Write out this attribute, if the value is not <code>null</code>.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param localName The local name of the attribute.
     * @param value The attribute value.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeAV(final XmlSerializer writer, final String localName, final String value)
            throws XmlPullParserException, IOException {
        if (value != null) {
            writer.attribute(null, localName, value);
        }
    }

    /**
     * Write out this attribute, if the value is not <code>null</code>.
     *
     * @param writer The {@link XmlSerializer} in use for the serialization.
     * @param localName The local name of the attribute.
     * @param value The attribute value.
     *
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writeAV(final XmlSerializer writer, final String localName, final Boolean value)
            throws XmlPullParserException, IOException {
        if (value != null) {
            writer.attribute(null, localName, value.toString());
        }
    }

    /**
     * Write the serialized SCXML document while making attempts to make the serialization human readable. This
     * includes using new-lines and indentation as appropriate, where possible. Exactly one of the stream, writer
     * or result parameters must be provided.
     *
     * @param configuration The {@link Configuration} to use.
     * @param scxmlStream The optional {@link OutputStream} to write to.
     * @param scxmlWriter The optional {@link Writer} to write to.
     * @param scxmlResult The optional {@link Result} to write to.
     *
     * @throws IOException An IO error during serialization.
     * @throws XmlPullParserException An exception processing the underlying {@link XmlSerializer}.
     */
    private static void writePretty(final Configuration configuration, final OutputStream scxmlStream,
                                    final Writer scxmlWriter, final Result scxmlResult)
            throws IOException, XmlPullParserException {

        // There isn't any portable way to write pretty using the JDK 1.6 StAX API
        configuration.internalWriter.flush();
        Source prettyPrintSource = new StreamSource(new StringReader(configuration.internalWriter.toString()));
        Result prettyPrintResult = null;
        if (scxmlStream != null) {
            prettyPrintResult = new StreamResult(scxmlStream);
        } else if (scxmlWriter != null) {
            prettyPrintResult = new StreamResult(scxmlWriter);
        } else if (scxmlResult != null) {
            prettyPrintResult = scxmlResult;
        }

        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            Transformer transformer = factory.newTransformer();
            if (configuration.encoding != null) {
                transformer.setOutputProperty(OutputKeys.ENCODING, configuration.encoding);
            }
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(prettyPrintSource, prettyPrintResult);
        } catch (TransformerException te) {
            throw new XmlPullParserException("TransformerException while pretty printing SCXML", null, te);
        }
    }

    /**
     * Use the supplied {@link Configuration} to create an appropriate {@link XmlSerializer} for this
     * {@link SCXMLWriter}. Exactly one of the stream, writer or result parameters must be provided.
     *
     * @param configuration The {@link Configuration} to use.
     * @param stream The optional {@link OutputStream} to write to.
     * @param writer The optional {@link Writer} to write to.
     *
     * @return The appropriately configured {@link XmlSerializer}.
     *
     * @throws XmlPullParserException A problem with the XML stream creation.
     */
    private static XmlSerializer getWriter(final Configuration configuration, final OutputStream stream,
                                             final Writer writer)
            throws XmlPullParserException, IOException {

        // Instantiate the XmlPullParserFactory
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

        XmlSerializer xsw = factory.newSerializer();
        if (configuration.usePrettyPrint || configuration.writeToString) {
            xsw.setOutput(configuration.internalWriter);
        } else if (stream != null) {
            if (configuration.encoding != null) {
                xsw.setOutput(stream, configuration.encoding);
            } else {
                xsw.setOutput(stream, null);
            }
        } else if (writer != null) {
            xsw.setOutput(writer);
        }
        return xsw;
    }

    /**
     * Get a {@link Transformer} instance that pretty prints the output.
     *
     * @return Transformer The indenting {@link Transformer} instance.
     */
    private static Transformer getTransformer() {
        Transformer transformer;
        Properties outputProps = new Properties();
        outputProps.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        outputProps.put(OutputKeys.STANDALONE, "no");
        outputProps.put(OutputKeys.INDENT, "yes");
        try {
            TransformerFactory tfFactory = TransformerFactory.newInstance();
            transformer = tfFactory.newTransformer();
            transformer.setOutputProperties(outputProps);
        } catch (TransformerFactoryConfigurationError t) {
            android.util.Log.e(TAG, t.getMessage(), t);
            return null;
        } catch (TransformerConfigurationException e) {
            android.util.Log.e(TAG, e.getMessage(), e);
            return null;
        }
        return transformer;
    }

    /**
     * Discourage instantiation since this is a utility class.
     */
    private SCXMLWriter() {
        super();
    }

    //------------------------- CONFIGURATION CLASS -------------------------//
    /**
     * <p>
     * Configuration for the {@link SCXMLWriter}. The configuration properties necessary for the following are
     * covered:
     * </p>
     *
     * <ul>
     *   <li>{@link XmlPullParserFactory} configuration properties such as <code>factoryId</code> or any properties</li>
     *   <li>{@link XmlSerializer} configuration properties such as target {@link Writer} or {@link OutputStream}
     *   and the <code>encoding</code></li>
     * </ul>
     */
    public static class Configuration {

        /*
         * Configuration properties for this {@link SCXMLWriter}.
         */
        // XmlPullParserFactory configuration properties.
        /**
         * The <code>factoryId</code> to use for the {@link XmlPullParserFactory}.
         */
        final String factoryId;

        /**
         * The {@link ClassLoader} to use for the {@link XmlPullParserFactory} instance to create.
         */
        final ClassLoader factoryClassLoader;

        /**
         * The map of properties (keys are property name strings, values are object property values) for the
         * {@link XmlPullParserFactory}.
         */
        final Map<String, Object> properties;

        // XmlSerializer configuration properties.
        /**
         * The <code>encoding</code> to use for the {@link XmlSerializer}.
         */
        final String encoding;

        /**
         * Whether to use a pretty print style that makes the output much more human readable.
         */
        final boolean usePrettyPrint;

        /**
         * The intermediate writer that will hold the output to be pretty printed, given the lack of a standard
         * StAX property for the {@link XmlPullParserFactory} in this regard. The contents will get transformed using
         * the transformation API.
         */
        final Writer internalWriter;

        // Underlying stream or writer close
        /**
         * Whether to close the underlying stream or writer passed by the caller.
         */
        final boolean closeUnderlyingWhenDone;

        /**
         * Whether to maintain an internal writer to return the serialization as a string.
         */
        boolean writeToString;

        /**
         * The pretty print output as a string.
         */
        String prettyPrintOutput;

        /*
         * Public constructors
         */
        /**
         * Default constructor.
         */
        public Configuration() {

            this(null, null, null, null, false, false, false);
        }

        /**
         * All-purpose constructor. Any of the parameters passed in can be <code>null</code> (booleans should default
         * to <code>false</code>). At the moment, the <code>factoryId</code> and <code>factoryClassLoader</code>
         * arguments are effectively ignored due to a bug in the underlying StAX {@link XmlPullParserFactory} API.
         *
         * @param factoryId The <code>factoryId</code> to use.
         * @param factoryClassLoader The {@link ClassLoader} to use for the {@link XmlPullParserFactory} instance to
         *                           create.
         * @param properties The map of properties (keys are property name strings, values are object property values)
         *                   for the {@link XmlPullParserFactory}.
         * @param encoding The <code>encoding</code> to use for the {@link XmlSerializer}
         * @param usePrettyPrint Whether to make the output human readable as far as possible. Since StAX does not
         *                       provide a portable way to do this in JDK 1.6, choosing the pretty print option
         *                       is currently not very efficient.
         * @param closeUnderlyingWhenDone Whether to close the underlying stream or writer passed by the caller.
         */
        public Configuration(final String factoryId, final ClassLoader factoryClassLoader,
                             final Map<String, Object> properties, final String encoding, final boolean usePrettyPrint,
                             final boolean closeUnderlyingWhenDone) {

            this(factoryId, factoryClassLoader, properties, encoding, usePrettyPrint, closeUnderlyingWhenDone, false);
        }

        /*
         * Package access constructors
         */
        /**
         * Convenience package access constructor.
         *
         * @param writeToString Whether we will be returning the serialization as a string.
         * @param usePrettyPrint Whether we will attempt to make the output human readable as far as possible.
         */
        public Configuration(final boolean writeToString, final boolean usePrettyPrint) {

            this(null, null, null, null, usePrettyPrint, false, writeToString);
        }

        /**
         * All-purpose package access constructor.
         *
         * @param factoryId The <code>factoryId</code> to use.
         * @param factoryClassLoader The {@link ClassLoader} to use for the {@link XmlPullParserFactory} instance to
         *                           create.
         * @param properties The map of properties (keys are property name strings, values are object property values)
         *                   for the {@link XmlPullParserFactory}.
         * @param encoding The <code>encoding</code> to use for the {@link XmlSerializer}
         * @param usePrettyPrint Whether to make the output human readable as far as possible. Since StAX does not
         *                       provide a portable way to do this in JDK 1.6, choosing the pretty print option
         *                       is currently not very efficient.
         * @param closeUnderlyingWhenDone Whether to close the underlying stream or writer passed by the caller.
         * @param writeToString Whether to maintain an internal writer to return the serialization as a string.
         */
        Configuration(final String factoryId, final ClassLoader factoryClassLoader,
                      final Map<String, Object> properties, final String encoding, final boolean usePrettyPrint,
                      final boolean closeUnderlyingWhenDone, final boolean writeToString) {

            this.factoryId = factoryId;
            this.factoryClassLoader = factoryClassLoader;
            this.properties = (properties == null ? new HashMap<String, Object>() : properties);
            this.encoding = encoding;
            this.usePrettyPrint = usePrettyPrint;
            this.closeUnderlyingWhenDone = closeUnderlyingWhenDone;
            this.writeToString = writeToString;
            if (this.usePrettyPrint || this.writeToString) {
                this.internalWriter = new StringWriter();
            } else {
                this.internalWriter = null;
            }
        }
    }
}

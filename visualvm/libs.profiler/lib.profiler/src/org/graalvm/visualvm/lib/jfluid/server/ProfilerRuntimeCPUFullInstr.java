/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997, 2018, Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.graalvm.visualvm.lib.jfluid.server;

/**
 * This class contains the actual methods for full instrumentation recursive CPU profiling, calls to which are injected
 * into the target application (TA) bytecodes when they are instrumented.
 *
 * @author Tomas Hurka
 * @author Misha Dmitriev
 */
public class ProfilerRuntimeCPUFullInstr extends ProfilerRuntimeCPU {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static void enableProfiling(boolean v) {
        if (v) {
            createNewDataStructures();
            ProfilerRuntimeCPU.enableProfiling(v);
        } else {
            ProfilerRuntimeCPU.enableProfiling(v);
            clearDataStructures();
        }
    }

    // ---------------------------------- Profile Data Acquisition --------------------------------------
    /** Called upon entry into a special root method used for */
    public static void markerMethodEntry(char methodId) {
        if (recursiveInstrumentationDisabled) {
            return; // See the comment at the recursiveInstrumentationDisabled variable declaration
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (ti.inProfilingRuntimeMethod > 0) {
            return;
        }

        //if (instrMethodClasses != null && methodId < instrMethodClasses.length) System.out.println("++++++Marker methodEntry for " + instrMethodClasses[methodId] + "." + instrMethodNames[methodId] + ", thread = " + Thread.currentThread());
        //else System.out.println("++++++Marker methodEntry for methodId = " + (int)methodId + ", thread = " + Thread.currentThread());
        if (!ti.isInitialized()) {
            if ((nProfiledThreadsAllowed > 0) && !ThreadInfo.isCurrentThreadProfilerServerThread()) {
                ti.initialize();
                ti.useEventBuffer();

                synchronized (eventBuffer) { // Make this happen atomically wrt. other operations on eventBuffer, such as reset collectors
                    nProfiledThreadsAllowed--;

                    ti.inProfilingRuntimeMethod++;
                    ti.inCallGraph = true;
                    writeThreadCreationEvent(ti);
                }
            } else {
                return;
            }
        } else {
            ti.inProfilingRuntimeMethod++;
            ti.inCallGraph = true;
        }

        if (ti.stackDepth <= stackDepthLimit) {
            // when methodId > 64K/2 is passed here using our instrumentation's sipush command at the call site, 
            // it's treated here as a signed integer. Thus without
            // the below fix we can get e.g. an ArrayIndexOutOfBoundsException(-32768) when methodId == 32768 (***)
            int methodIdInt = methodId&0xff;
            methodIdInt |= methodId&0xff00;

            if (!instrMethodInvoked[methodIdInt]) {
                instrMethodInvoked[methodIdInt] = true; // Mark this method as invoked
                if (ti.rootMethodStackDepth > 0) { // marker method under root method - perform instrumentation of nearest callees
                    firstTimeMethodInvoke(ti, methodId);
                }
            }

            ProfilerServer.notifyClientOnResultsAvailability();
            writeParametersEvent(ti);
            writeTimeStampedEvent(MARKER_ENTRY, ti, methodId);
        }
        ti.stackDepth++; //= 1;  // This is the logical stack depth
        ti.inProfilingRuntimeMethod--;
    }

    /** Called upon exit from the marker method. */
    public static void markerMethodExit(char methodId) {
        markerMethodExit(NO_RET_VALUE, methodId);
    }
    
    /** Called upon exit from the marker method. */
    public static void markerMethodExit(Object ret, char methodId) {
        if (recursiveInstrumentationDisabled) {
            return;
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (ti.isInitialized() && ti.inCallGraph) { // ti == null may happen if instrumentation has been removed or data collectors reset

            if (ti.inProfilingRuntimeMethod > 0) {
                return;
            }

            ti.inProfilingRuntimeMethod++;

            //System.out.println("------markerMethodExit for " + instrMethodClasses[methodId] + "." + instrMethodNames[methodId] + ", depth = " + ti.stackDepth + ", id = " + (int) methodId);
            ti.stackDepth--;

            if (ti.stackDepth < 1) {
                ti.inCallGraph = false; // We are exiting the marker method of our call subgraph
            }
            if (ti.stackDepth <= stackDepthLimit) {
                writeRetValue(ret, ti);
                writeTimeStampedEvent(MARKER_EXIT, ti, methodId);
            }
            ti.inProfilingRuntimeMethod--;
        }
    }

    /** Called upon entry into a non-root target application method */
    public static void methodEntry(char methodId) {
        if (recursiveInstrumentationDisabled) {
            return; // See the comment at the recursiveInstrumentationDisabled variable declaration
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (ti.isInitialized() && ti.inCallGraph && (ti.rootMethodStackDepth > 0)) {
            if (ti.inProfilingRuntimeMethod > 0) {
                return;
            }

            ti.inProfilingRuntimeMethod++;
            //System.out.println("++++++methodEntry, depth = " + ti.stackDepth + ", id = " + (int) methodId);

            if (ti.stackDepth <= stackDepthLimit) {
                // See comment marked with (***)
                int methodIdInt = methodId&0xff;
                methodIdInt |= methodId&0xff00;

                // Now check if it's the first invocation of this method, and if so, perform instrumentation of nearest callees
                if (!instrMethodInvoked[methodIdInt]) {
                    instrMethodInvoked[methodIdInt] = true; // Mark this method as invoked
                    firstTimeMethodInvoke(ti, methodId);
                }

                writeTimeStampedEvent(METHOD_ENTRY, ti, methodId);
            }
            ti.stackDepth++;
            ti.inProfilingRuntimeMethod--;
        }
    }

    /** Called upon exit from the method. */
    public static void methodExit(char methodId) {
        if (recursiveInstrumentationDisabled) {
            return; // See the comment at the recursiveInstrumentationDisabled variable declaration
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (ti.isInitialized() && ti.inCallGraph && (ti.rootMethodStackDepth > 0)) { // ti == null may happen if instrumentation has been removed or data collectors reset

            if (ti.inProfilingRuntimeMethod > 0) {
                return;
            }

            ti.inProfilingRuntimeMethod++;

            //System.out.println("------methodExit, depth = " + ti.stackDepth + ", id = " + (int) methodId);
            if (ti.rootMethodStackDepth == ti.stackDepth) {
                ti.rootMethodStackDepth = 0;
            }

            ti.stackDepth--;

            if (ti.stackDepth < 1) {
                ti.inCallGraph = false; // We are exiting the root method of our call subgraph
                writeTimeStampedEvent(ROOT_EXIT, ti, methodId);
            } else if (ti.rootMethodStackDepth == 0) { // We are exiting the root method, which was under marker method
                writeTimeStampedEvent(ROOT_EXIT, ti, methodId);
            } else if (ti.stackDepth <= stackDepthLimit) {
                writeTimeStampedEvent(METHOD_EXIT, ti, methodId);
            }

            ti.inProfilingRuntimeMethod--;
        }
    }

    public static void resumeActiveTimers() {
        writeTimeStampedEvent(THREADS_RESUMED, null, (char) 0); // FIXME: see above
        changeAllThreadsInProfRuntimeMethodStatus(-1); // See the comment in suspendActiveTimers()
        recursiveInstrumentationDisabled = false;
    }

    /** Called upon entry into a root target application method */
    public static void rootMethodEntry(char methodId) {
        if (recursiveInstrumentationDisabled) {
            return; // See the comment at the recursiveInstrumentationDisabled variable declaration
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (ti.inProfilingRuntimeMethod > 0) {
            return;
        }

        ProfilerServer.notifyClientOnResultsAvailability();

        if (ti.isInitialized() && !ti.inCallGraph && (ti.stackDepth > 0)) {
            ti.inCallGraph = true;
            methodEntry(methodId);
            ti.inCallGraph = false;

            return;
        }

        if (ti.isInitialized() && ti.inCallGraph && (ti.rootMethodStackDepth > 0)) {
            methodEntry(methodId);
        } else { // Entered the root method from outside this call subgraph
                 //if (instrMethodClasses != null && methodId < instrMethodClasses.length) System.out.println("++++++Root methodEntry for " + instrMethodClasses[methodId] + "." + instrMethodNames[methodId] + ", thread = " + Thread.currentThread());
                 //else System.out.println("++++++Root methodEntry for methodId = " + (int)methodId + ", thread = " + Thread.currentThread());

            if (!ti.isInitialized()) {
                if ((nProfiledThreadsAllowed > 0) && !ThreadInfo.isCurrentThreadProfilerServerThread()) {
                    ti.initialize();
                    ti.useEventBuffer();

                    synchronized (eventBuffer) { // Make this happen atomically wrt. other operations on eventBuffer, such as reset collectors
                        nProfiledThreadsAllowed--;
                        ti.inProfilingRuntimeMethod++;

                        if (!ProfilerServer.startProfilingPointsActive()) {
                            ti.inCallGraph = true;
                        }

                        writeThreadCreationEvent(ti);
                    }
                } else {
                    return;
                }
            } else {
                ti.inProfilingRuntimeMethod++;

                if (ti.stackDepth == 0 && !ProfilerServer.startProfilingPointsActive()) {
                    ti.inCallGraph = true;
                }
            }

            // See comment marked with (***)
            int methodIdInt = methodId&0xff;
            methodIdInt |= methodId&0xff00;
            
            // Check if it's the first invocation of this method, and if so, perform instrumentation of its immediate callees
            if (!instrMethodInvoked[methodIdInt]) {
                instrMethodInvoked[methodIdInt] = true;
                if (enableFirstTimeMethodInvoke) externalActionsHandler.handleFirstTimeMethodInvoke(methodId);
            }

            ti.stackDepth++; //= 1;  // This is the logical stack depth
            writeTimeStampedEvent(ROOT_ENTRY, ti, methodId);

            ti.rootMethodStackDepth = ti.stackDepth;
            ti.inProfilingRuntimeMethod--;
        }
    }

    public static void suspendActiveTimers() {
        changeAllThreadsInProfRuntimeMethodStatus(+1); // In case any instrumented method is called while we perform profiler work on behalf of this thread.
        recursiveInstrumentationDisabled = true;
        writeTimeStampedEvent(THREADS_SUSPENDED, null, (char) 0); // FIXME: need a special event writing method or something
    }

    protected static void clearDataStructures() {
        ProfilerRuntimeCPU.clearDataStructures();
    }
}

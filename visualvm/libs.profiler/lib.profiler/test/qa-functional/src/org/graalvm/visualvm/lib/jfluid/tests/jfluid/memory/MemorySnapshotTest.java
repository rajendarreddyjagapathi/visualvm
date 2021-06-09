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

package org.graalvm.visualvm.lib.jfluid.tests.jfluid.memory;

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.junit.NbModuleSuite;
import org.graalvm.visualvm.lib.jfluid.ProfilerEngineSettings;
import org.graalvm.visualvm.lib.jfluid.global.CommonConstants;


/**
 *
 * @author ehucka
 */
public class MemorySnapshotTest extends MemorySnapshotTestCase {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of BasicTest */
    public MemorySnapshotTest(String name) {
        super(name);
    }
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(MemorySnapshotTest.class).addTest(
            "testSettingsAllocations",
            "testSettingsAllocationsServer",
            "testSettingsAllocationsStackTraces",
            "testSettingsAllocationsStackTracesServer",
            "testSettingsLiveness",
            "testSettingsLivenessServer",
            "testSettingsLivenessStackTraces",
            "testSettingsLivenessStackTracesServer").enableModules(".*").clusters(".*").gui(false));
    }
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void testSettingsAllocations() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(0);
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_ALLOCATIONS, new String[] { "simple" },
                                "simple.memory.Bean");
    }

    public void testSettingsAllocationsServer() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        addJVMArgs(settings, "-server");
        settings.setAllocStackTraceLimit(0);
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_ALLOCATIONS, new String[] { "simple" },
                                "simple.memory.Bean");
    }

    public void testSettingsAllocationsStackTraces() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(-1);
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_ALLOCATIONS, new String[] { "simple" },
                                "simple.memory.Bean");
    }

    public void testSettingsAllocationsStackTracesServer() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(-1);
        addJVMArgs(settings, "-server");
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_ALLOCATIONS, new String[] { "simple" },
                                "simple.memory.Bean");
    }

    public void testSettingsLiveness() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(0);
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_LIVENESS, new String[] { "simple" }, "simple.memory.Bean");
    }

    public void testSettingsLivenessServer() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(0);
        addJVMArgs(settings, "-server");
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_LIVENESS, new String[] { "simple" }, "simple.memory.Bean");
    }

    public void testSettingsLivenessStackTraces() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(-1);
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_LIVENESS, new String[] { "simple" }, "simple.memory.Bean");
    }

    public void testSettingsLivenessStackTracesServer() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(-1);
        addJVMArgs(settings, "-server");
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_LIVENESS, new String[] { "simple" }, "simple.memory.Bean");
    }
}

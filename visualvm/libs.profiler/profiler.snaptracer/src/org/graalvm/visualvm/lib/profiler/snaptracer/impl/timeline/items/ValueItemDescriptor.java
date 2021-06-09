/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2018, Oracle and/or its affiliates. All rights reserved.
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

package org.graalvm.visualvm.lib.profiler.snaptracer.impl.timeline.items;

import org.graalvm.visualvm.lib.profiler.snaptracer.ItemValueFormatter;
import org.graalvm.visualvm.lib.profiler.snaptracer.ProbeItemDescriptor;

/**
 * 
 * @author Jiri Sedlacek
 */
public abstract class ValueItemDescriptor extends ProbeItemDescriptor {

    private final ItemValueFormatter formatter;
    private final double dataFactor;
    private final long minValue;
    private final long maxValue;


    ValueItemDescriptor(String name, String description,
                        ItemValueFormatter formatter, double dataFactor,
                        long minValue, long maxValue) {

        super(name, description);
        if (formatter == null) {
            throw new IllegalArgumentException("formatter cannot be null"); // NOI18N
        }
        if (dataFactor == 0) {
            throw new IllegalArgumentException("dataFactor cannot be 0"); // NOI18N
        }
        this.formatter = formatter;
        this.dataFactor = dataFactor;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }


    public final String getValueString(long value, int format) {
        return formatter.formatValue(value, format);
    }

    public final String getUnitsString(int format) {
        return formatter.getUnits(format);
    }

    public final double getDataFactor() {
        return dataFactor;
    }

    public final long getMinValue() {
        return minValue;
    }

    public final long getMaxValue() {
        return maxValue;
    }
    
}

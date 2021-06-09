/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.graalvm.visualvm.lib.profiler.heapwalk.details.jdk;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.graalvm.visualvm.lib.jfluid.heap.Instance;
import org.graalvm.visualvm.lib.profiler.heapwalk.details.spi.DetailsProvider;
import org.graalvm.visualvm.lib.profiler.heapwalk.details.spi.DetailsUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Hurka
 */
@ServiceProvider(service=DetailsProvider.class)
public final class MathDetailsProvider extends DetailsProvider.Basic {
    
    private static final String BIG_INTEGRER_MASK = "java.math.BigInteger"; // NOI18N
    private static final String BIG_DECIMAL_MASK = "java.math.BigDecimal";  // NOI18N
    private static final String FD_BIG_INTEGRER_MASK = "jdk.internal.math.FDBigInteger";  // NOI18N
    
    public MathDetailsProvider() {
        super(BIG_INTEGRER_MASK,BIG_DECIMAL_MASK, FD_BIG_INTEGRER_MASK);
    }
    
    public String getDetailsString(String className, Instance instance) {
        if (BIG_INTEGRER_MASK.equals(className)) {
            BigInteger bint = getBigInteger(instance);
            
            if (bint != null) {
                return bint.toString();
            }
        } else if (BIG_DECIMAL_MASK.equals(className)) {
            String val = DetailsUtils.getInstanceFieldString(instance, "stringCache");   // NOI18N
            if (val == null) {
                int scale = DetailsUtils.getIntFieldValue(instance, "scale", 0);    // NOI18N
                long intCompact = DetailsUtils.getLongFieldValue(instance, "intCompact", Long.MIN_VALUE);   // NOI18N
                
                if (intCompact != Long.MIN_VALUE) {
                    return BigDecimal.valueOf(intCompact, scale).toString();
                } else {
                    Object bintInstace = instance.getValueOfField("intVal");    // NOI18N
                    if (bintInstace instanceof Instance) {
                        BigInteger bint = getBigInteger((Instance)bintInstace);
                        
                        if (bint != null) {
                            return new BigDecimal(bint, scale).toString();
                        }
                    }
                }
            } else {
                return val;
            }
        }
        if (FD_BIG_INTEGRER_MASK.equals(className)) {
            Integer nWords = (Integer) instance.getValueOfField("nWords");      // NOI18N
            Integer offset = (Integer) instance.getValueOfField("offset");      // NOI18N
            int[] data = DetailsUtils.getIntArray(DetailsUtils.getPrimitiveArrayFieldValues(instance, "data"));   // NOI18N
            if (nWords != null && offset != null && data != null) {
                byte[] magnitude = new byte[nWords * 4 + 1];
                for (int i = 0; i < nWords; i++) {
                    int w = data[i];
                    magnitude[magnitude.length - 4 * i - 1] = (byte) w;
                    magnitude[magnitude.length - 4 * i - 2] = (byte) (w >> 8);
                    magnitude[magnitude.length - 4 * i - 3] = (byte) (w >> 16);
                    magnitude[magnitude.length - 4 * i - 4] = (byte) (w >> 24);
                }
                return new BigInteger(magnitude).shiftLeft(offset * 32).toString();
            }
        }
        return null;
    }

    private BigInteger getBigInteger(final Instance instance) {
        int sig = DetailsUtils.getIntFieldValue(instance, "signum", Integer.MAX_VALUE);     // NOI18N
        int[] mag = DetailsUtils.getIntArray(DetailsUtils.getPrimitiveArrayFieldValues(instance, "mag"));   // NOI18N
        if (mag != null && sig != Integer.MAX_VALUE) {
            ByteBuffer buffer = ByteBuffer.allocate(mag.length * 4);
            IntBuffer intBuffer = buffer.asIntBuffer();
            intBuffer.put(mag);
            
            return new BigInteger(sig, buffer.array());
        }
        return null;
    }
    
}

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

package org.graalvm.visualvm.lib.charts.xy.synchronous;

import org.graalvm.visualvm.lib.charts.swing.Utils;
import org.graalvm.visualvm.lib.charts.ChartContext;
import org.graalvm.visualvm.lib.charts.ChartItem;
import org.graalvm.visualvm.lib.charts.ChartItemChange;
import org.graalvm.visualvm.lib.charts.ItemSelection;
import org.graalvm.visualvm.lib.charts.swing.LongRect;
import org.graalvm.visualvm.lib.charts.xy.XYItem;
import org.graalvm.visualvm.lib.charts.xy.XYItemChange;
import org.graalvm.visualvm.lib.charts.xy.XYItemPainter;
import org.graalvm.visualvm.lib.charts.xy.XYItemSelection;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.List;

/**
 *
 * @author Jiri Sedlacek
 */
public class SynchronousXYItemMarker extends XYItemPainter.Abstract {

    protected final int markRadius;
    protected final int line1Width;
    protected final Color line1Color;
    protected final int line2Width;
    protected final Color line2Color;
    protected final Color fillColor;

    protected final Stroke line1Stroke;
    protected final Stroke line2Stroke;

    protected final int decorationRadius;

    protected final int type;
    protected final int maxValueOffset;


    // --- Constructor ---------------------------------------------------------

    public static SynchronousXYItemMarker absolutePainter(int markRadius,
                                                       float line1Width,
                                                       Color line1Color,
                                                       float line2Width,
                                                       Color line2Color,
                                                       Color fillColor) {

        return new SynchronousXYItemMarker(markRadius, line1Width, line1Color,
                                        line2Width, line2Color, fillColor,
                                        TYPE_ABSOLUTE, 0);
    }

    public static SynchronousXYItemMarker relativePainter(int markRadius,
                                                       float line1Width,
                                                       Color line1Color,
                                                       float line2Width,
                                                       Color line2Color,
                                                       Color fillColor,
                                                       int maxOffset) {

        return new SynchronousXYItemMarker(markRadius, line1Width, line1Color,
                                        line2Width, line2Color, fillColor,
                                        TYPE_RELATIVE, maxOffset);
    }


    public SynchronousXYItemMarker(int markRadius, float line1Width, Color line1Color,
                                 float line2Width, Color line2Color, Color fillColor,
                                 int type, int maxValueOffset) {

        if (line1Color == null && line2Color == null && fillColor == null)
            throw new IllegalArgumentException("No parameters defined"); // NOI18N

        this.markRadius = markRadius;
        this.line1Width = (int)Math.ceil(line1Width);
        this.line1Color = Utils.checkedColor(line1Color);
        this.line2Width = (int)Math.ceil(line2Width);
        this.line2Color = Utils.checkedColor(line2Color);
        this.fillColor = Utils.checkedColor(fillColor);

        this.line1Stroke = line1Color == null ? null :
                           new BasicStroke(line1Width, BasicStroke.CAP_ROUND,
                                           BasicStroke.JOIN_ROUND);
        this.line2Stroke = line2Color == null ? null :
                           new BasicStroke(line2Width, BasicStroke.CAP_ROUND,
                                         BasicStroke.JOIN_ROUND);


        decorationRadius = markRadius + this.line1Width + this.line2Width;

        this.type = type;
        this.maxValueOffset = maxValueOffset;
    }


    // --- ItemPainter implementation ------------------------------------------
    
    public LongRect getItemBounds(ChartItem item) {
//        if (!(item instanceof ProfilerXYItem))
//            throw new UnsupportedOperationException("Unsupported item: " + item); // NOI18N

        SynchronousXYItem xyItem = (SynchronousXYItem)item;
        if (type == TYPE_ABSOLUTE) {
            return xyItem.getBounds();
        } else {
            LongRect itemBounds1 = new LongRect(xyItem.getBounds());
            itemBounds1.y = 0;
            itemBounds1.height = 0;
            return itemBounds1;
        }
    }

    public LongRect getItemBounds(ChartItem item, ChartContext context) {
//        if (!(item instanceof ProfilerXYItem))
//            throw new UnsupportedOperationException("Unsupported item: " + item); // NOI18N

        SynchronousXYItem xyItem = (SynchronousXYItem)item;
        return getViewBounds(xyItem, null, context);
    }


    public boolean isBoundsChange(ChartItemChange itemChange) {
//        if (!(itemChange instanceof XYItemChange))
//            throw new UnsupportedOperationException("Unsupported itemChange: " + itemChange);

        // Items can only be added => always bounds change
        XYItemChange change = (XYItemChange)itemChange;
        return !LongRect.equals(change.getOldValuesBounds(),
                                change.getNewValuesBounds());
    }

    public boolean isAppearanceChange(ChartItemChange itemChange) {
//        if (!(itemChange instanceof XYItemChange))
//            throw new UnsupportedOperationException("Unsupported itemChange: " + itemChange);
        
        // Items can only be added => always appearance change
        XYItemChange change = (XYItemChange)itemChange;
        LongRect dirtyBounds = change.getDirtyValuesBounds();
        return dirtyBounds.width != 0 || dirtyBounds.height != 0;
    }

    public LongRect getDirtyBounds(ChartItemChange itemChange, ChartContext context) {
//        if (!(itemChange instanceof XYItemChange))
//            throw new UnsupportedOperationException("Unsupported itemChange: " + itemChange);
        
        // Items can only be added => always dirty bounds for last value
        XYItemChange change = (XYItemChange)itemChange;
        return getViewBounds(change.getItem(), change.getValuesIndexes(), context);
    }


    public boolean supportsHovering(ChartItem item) {
        return true;
    }

    public boolean supportsSelecting(ChartItem item) {
        return true;
    }

    public LongRect getSelectionBounds(ItemSelection selection, ChartContext context) {
//        if (!(selection instanceof XYItemSelection))
//            throw new UnsupportedOperationException("Unsupported selection: " + selection); // NOI18N

        XYItemSelection sel = (XYItemSelection)selection;
        XYItem item  = sel.getItem();
        int selectedValueIndex = sel.getValueIndex();

        if (selectedValueIndex == -1 ||
            selectedValueIndex >= item.getValuesCount())
            // This happens on reset - bounds of the selection are unknown, let's clear whole area
            return new LongRect(0, 0, context.getViewportWidth(),
                                context.getViewportHeight());
        else
            return getViewBounds(item, new int[] { sel.getValueIndex() }, context);
    }

    public XYItemSelection getClosestSelection(ChartItem item, int viewX,
                                               int viewY, ChartContext context) {
//        if (!(item instanceof ProfilerXYItem))
//            throw new UnsupportedOperationException("Unsupported item: " + item); // NOI18N
//        if (!(context instanceof ProfilerXYChartComponent.Context))
//            throw new UnsupportedOperationException("Unsupported context: " + context);

        SynchronousXYChartContext contx = (SynchronousXYChartContext)context;

        int nearestTimestampIndex = contx.getNearestTimestampIndex(viewX, viewY);
        if (nearestTimestampIndex == -1) return null; // item not visible

        SynchronousXYItem xyItem = (SynchronousXYItem)item;
        return new XYItemSelection.Default(xyItem, nearestTimestampIndex);
    }

    public void paintItem(ChartItem item, List<ItemSelection> highlighted,
                          List<ItemSelection> selected, Graphics2D g,
                          Rectangle dirtyArea, ChartContext context) {
//        if (!(item instanceof ProfilerXYItem))
//            throw new UnsupportedOperationException("Unsupported item: " + item); // NOI18N
//        if (!(context instanceof ProfilerXYChartComponent.Context))
//            throw new UnsupportedOperationException("Unsupported context: " + context);
        
        paint((SynchronousXYItem)item, highlighted, selected, g, dirtyArea,
              (SynchronousXYChartContext)context);
    }


    // --- Private implementation ----------------------------------------------

    private LongRect getViewBoundsRelative(LongRect dataBounds, XYItem item,
                                           ChartContext context) {
        LongRect itemBounds = item.getBounds();

        double itemValueFactor = getItemValueFactor(context, maxValueOffset,
                                                    itemBounds.height);

        // TODO: fix the math!!!
        double value1 = context.getDataOffsetY() + itemValueFactor *
                      (double)(dataBounds.y - itemBounds.y);
        double value2 = context.getDataOffsetY() + itemValueFactor *
                      (double)(dataBounds.y + dataBounds.height - itemBounds.y);

        long viewX = (long)Math.ceil(context.getViewX(dataBounds.x));
        long viewWidth = (long)Math.ceil(context.getViewWidth(dataBounds.width));
        if (context.isRightBased()) viewX -= viewWidth;

        long viewY1 = (long)Math.ceil(context.getViewY(value1));
        long viewY2 = (long)Math.ceil(context.getViewY(value2));
        long viewHeight = context.isBottomBased() ? viewY1 - viewY2 :
                                                    viewY2 - viewY1;
        if (!context.isBottomBased()) viewY2 -= viewHeight;

        LongRect viewBounds =  new LongRect(viewX, viewY2, viewWidth, viewHeight);
        LongRect.addBorder(viewBounds, decorationRadius);

        return viewBounds;
    }

    private LongRect getViewBounds(XYItem item, int[] valuesIndexes, ChartContext context) {
        
        LongRect dataBounds = new LongRect();

        if (valuesIndexes == null) {
            LongRect.set(dataBounds, item.getBounds());
        } else {
            boolean firstPoint = true;
            for (int valueIndex : valuesIndexes) {
                if (valueIndex == -1) continue;
                long xValue = item.getXValue(valueIndex);
                long yValue = item.getYValue(valueIndex);
                if (firstPoint) {
                    LongRect.set(dataBounds, xValue, yValue, 0, 0);
                    firstPoint = false;
                } else {
                    LongRect.add(dataBounds, xValue, yValue);
                }
            }
        }

//        if (type == TYPE_RELATIVE)
//            LongRect.set(dataBounds, getRelativeDataBounds(dataBounds, item,
//                                                           context, maxOffset));

        if (type == TYPE_RELATIVE) {

            return getViewBoundsRelative(dataBounds, item, context);

        } else {

            LongRect viewBounds = context.getViewRect(dataBounds);
            LongRect.addBorder(viewBounds, decorationRadius);
            return viewBounds;

        }
    }

    
    private void paint(SynchronousXYItem item, List<ItemSelection> highlighted,
                       List<ItemSelection> selected, Graphics2D g,
                       Rectangle dirtyArea, SynchronousXYChartContext context) {

        if (highlighted.isEmpty()) return;
        if (item.getValuesCount() < 1) return;
        if (context.getViewWidth() == 0 || context.getViewHeight() == 0) return;

        double itemValueFactor = type == TYPE_RELATIVE ?
                                         getItemValueFactor(context, maxValueOffset,
                                         item.getBounds().height) : 0;

        for (ItemSelection selection : highlighted) {

            XYItemSelection sel = (XYItemSelection)selection;
            int valueIndex = sel.getValueIndex();
            if (valueIndex == -1) continue;

            int itemX = Utils.checkedInt(context.getViewX(
                                                 item.getXValue(valueIndex)));
            int itemY = Utils.checkedInt(getYValue(item, valueIndex,
                                                 type, context, itemValueFactor));

            if (fillColor != null) {
                g.setPaint(fillColor);
                g.fillOval(itemX - markRadius, itemY - markRadius,
                           markRadius * 2, markRadius * 2);
            }

            if (line2Color != null) {
                g.setPaint(line2Color);
                g.setStroke(line2Stroke);
                g.drawOval(itemX - markRadius, itemY - markRadius,
                           markRadius * 2, markRadius * 2);
            }

            if (line1Color != null) {
                int radius = markRadius + line2Width / 2;
                g.setPaint(line1Color);
                g.setStroke(line1Stroke);
                g.drawOval(itemX - radius, itemY - radius,
                           radius * 2, radius * 2);
            }

        }

//        System.err.println(">>> paintItem, dirtyArea: " + dirtyArea);
        
    }

    private static double getYValue(XYItem item, int valueIndex,
                                  int type, ChartContext context, double itemValueFactor) {
        if (type == TYPE_ABSOLUTE) {
            return context.getViewY(item.getYValue(valueIndex));
        } else {
            return context.getViewY(context.getDataOffsetY() + (itemValueFactor *
                        (item.getYValue(valueIndex) - item.getBounds().y)));
        }
    }

    private static double getItemValueFactor(ChartContext context,
                                             double maxValueOffset,
                                             double itemHeight) {
        return ((double)context.getDataHeight() -
               context.getDataHeight(maxValueOffset)) / itemHeight;
    }

}

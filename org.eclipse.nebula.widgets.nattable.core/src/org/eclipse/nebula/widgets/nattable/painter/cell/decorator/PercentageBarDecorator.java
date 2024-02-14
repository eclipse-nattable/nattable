/*******************************************************************************
 * Copyright (c) 2012, 2024 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell.decorator;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;

/**
 * Draws a rectangular bar in cell proportional to the value of the cell.
 */
public class PercentageBarDecorator extends CellPainterWrapper {

    public static final ConfigAttribute<Color> PERCENTAGE_BAR_COMPLETE_REGION_START_COLOR = new ConfigAttribute<>();
    public static final ConfigAttribute<Color> PERCENTAGE_BAR_COMPLETE_REGION_END_COLOR = new ConfigAttribute<>();
    public static final ConfigAttribute<Color> PERCENTAGE_BAR_INCOMPLETE_REGION_COLOR = new ConfigAttribute<>();

    private static final Color DEFAULT_COMPLETE_REGION_START_COLOR = GUIHelper.getColor(new RGB(187, 216, 254));
    private static final Color DEFAULT_COMPLETE_REGION_END_COLOR = GUIHelper.getColor(new RGB(255, 255, 255));

    public PercentageBarDecorator(ICellPainter interiorPainter) {
        super(interiorPainter);
    }

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
        Pattern originalBackgroundPattern = gc.getBackgroundPattern();

        double factor = Math.min(1.0, convertDataType(cell, configRegistry).doubleValue());
        factor = Math.max(0.0, factor);

        Rectangle bar = new Rectangle(
                rectangle.x,
                rectangle.y,
                (int) (rectangle.width * factor),
                rectangle.height);

        Color color1 = CellStyleUtil
                .getCellStyle(cell, configRegistry)
                .getAttributeValue(PERCENTAGE_BAR_COMPLETE_REGION_START_COLOR);
        Color color2 = CellStyleUtil
                .getCellStyle(cell, configRegistry)
                .getAttributeValue(PERCENTAGE_BAR_COMPLETE_REGION_END_COLOR);
        if (color1 == null) {
            color1 = DEFAULT_COMPLETE_REGION_START_COLOR;
        }
        if (color2 == null) {
            color2 = DEFAULT_COMPLETE_REGION_END_COLOR;
        }

        Pattern pattern = new Pattern(
                Display.getCurrent(),
                rectangle.x,
                rectangle.y,
                (float) rectangle.x + (float) rectangle.width,
                (float) rectangle.y + (float) rectangle.height,
                color1,
                color2);
        gc.setBackgroundPattern(pattern);
        gc.fillRectangle(bar);

        gc.setBackgroundPattern(originalBackgroundPattern);
        pattern.dispose();

        Color incompleteRegionColor = CellStyleUtil
                .getCellStyle(cell, configRegistry)
                .getAttributeValue(PERCENTAGE_BAR_INCOMPLETE_REGION_COLOR);
        if (incompleteRegionColor != null) {
            Region incompleteRegion = new Region();

            incompleteRegion.add(rectangle);
            incompleteRegion.subtract(bar);
            Color originalBackgroundColor = gc.getBackground();
            gc.setBackground(incompleteRegionColor);
            gc.fillRectangle(incompleteRegion.getBounds());
            gc.setBackground(originalBackgroundColor);

            incompleteRegion.dispose();
        }

        super.paintCell(cell, gc, rectangle, configRegistry);
    }

    /**
     * Converts the data type to a {@link Number}. If the value is not a
     * {@link Number} it will simply return 0.
     *
     * @param cell
     *            The cell for which the {@link Number} value is requested.
     * @param configRegistry
     *            The {@link IConfigRegistry} to retrieve the
     *            {@link IDisplayConverter} for the cell.
     * @return The {@link Number} value to show. Should be a factor value &lt;=
     *         1.0 to be interpreted as a percentage.
     * @since 2.3
     */
    protected Number convertDataType(ILayerCell cell, IConfigRegistry configRegistry) {
        if (cell.getDataValue() instanceof Number) {
            return (Number) cell.getDataValue();
        }
        return Double.valueOf(0);
    }

}

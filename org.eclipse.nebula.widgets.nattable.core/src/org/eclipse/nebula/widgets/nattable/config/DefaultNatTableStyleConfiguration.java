/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.config;

import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.LineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

//fields are public by design to make it easy for adapters to customize styling
@SuppressWarnings("java:S1104")
public class DefaultNatTableStyleConfiguration extends AbstractRegistryConfiguration {

    public Color bgColor = GUIHelper.COLOR_WHITE;
    public Color fgColor = GUIHelper.COLOR_BLACK;
    public Color gradientBgColor = GUIHelper.COLOR_WHITE;
    public Color gradientFgColor = GUIHelper.getColor(136, 212, 215);
    public Font font = GUIHelper.DEFAULT_FONT;
    public HorizontalAlignmentEnum hAlign = HorizontalAlignmentEnum.CENTER;
    public VerticalAlignmentEnum vAlign = VerticalAlignmentEnum.MIDDLE;
    public BorderStyle borderStyle = null;

    public ICellPainter cellPainter = new LineBorderDecorator(new TextPainter());

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER,
                this.cellPainter);

        Style cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.bgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.fgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.gradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.gradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.font);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.hAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.vAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.borderStyle);

        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                cellStyle);

        configRegistry.registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                new DefaultDisplayConverter());
    }
}

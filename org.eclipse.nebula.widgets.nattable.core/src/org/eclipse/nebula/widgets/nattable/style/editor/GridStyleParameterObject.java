/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style.editor;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class GridStyleParameterObject {

    public Font tableFont;
    public Color evenRowColor;
    public Color oddRowColor;
    public Color selectionColor;

    public IStyle evenRowStyle;
    public IStyle oddRowStyle;
    public IStyle selectionStyle;
    public IStyle tableStyle;

    private final IConfigRegistry configRegistry;

    public GridStyleParameterObject(IConfigRegistry configRegistry) {
        this.configRegistry = configRegistry;
        init(configRegistry);
    }

    private void init(IConfigRegistry configRegistry) {
        this.evenRowStyle = configRegistry.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
        this.evenRowColor = this.evenRowStyle
                .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);

        this.oddRowStyle = configRegistry.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);
        this.oddRowColor = this.oddRowStyle
                .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);

        this.selectionStyle = configRegistry.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT);
        this.selectionColor = this.selectionStyle
                .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);

        this.tableStyle = configRegistry.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL);
        this.tableFont = this.tableStyle.getAttributeValue(CellStyleAttributes.FONT);
    }

    public IConfigRegistry getConfigRegistry() {
        return this.configRegistry;
    }

}

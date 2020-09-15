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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColumnHeaderCheckBoxPainter extends ImagePainter {

    private static final Logger LOG = LoggerFactory.getLogger(ColumnHeaderCheckBoxPainter.class);

    private final Image checkedImg;
    private final Image semicheckedImg;
    private final Image uncheckedImg;

    private final IUniqueIndexLayer columnDataLayer;

    public ColumnHeaderCheckBoxPainter(IUniqueIndexLayer columnDataLayer) {
        this(
                columnDataLayer,
                GUIHelper.getImage("checked"), //$NON-NLS-1$
                GUIHelper.getImage("semichecked"), //$NON-NLS-1$
                GUIHelper.getImage("unchecked") //$NON-NLS-1$
        );
    }

    public ColumnHeaderCheckBoxPainter(IUniqueIndexLayer columnLayer,
            Image checkedImg, Image semicheckedImage, Image uncheckedImg) {
        this.columnDataLayer = columnLayer;
        this.checkedImg = checkedImg;
        this.semicheckedImg = semicheckedImage;
        this.uncheckedImg = uncheckedImg;
    }

    public int getPreferredWidth(boolean checked) {
        return getImage(checked).getBounds().width;
    }

    public int getPreferredHeight(boolean checked) {
        return getImage(checked).getBounds().height;
    }

    public void paintIconImage(GC gc, Rectangle rectangle, int yOffset, boolean checked) {
        Image checkBoxImage = getImage(checked);

        // Center image
        int x = rectangle.x + (rectangle.width / 2) - (checkBoxImage.getBounds().width / 2);

        gc.drawImage(checkBoxImage, x, rectangle.y + yOffset);
    }

    public Image getImage(boolean checked) {
        return checked ? this.checkedImg : this.uncheckedImg;
    }

    @Override
    protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
        int columnPosition = LayerUtil.convertColumnPosition(
                cell.getLayer(), cell.getColumnPosition(), this.columnDataLayer);

        int checkedCellsCount = getCheckedCellsCount(columnPosition, configRegistry);

        Image result = null;
        if (checkedCellsCount > 0) {
            if (checkedCellsCount == this.columnDataLayer.getRowCount()) {
                result = this.checkedImg;
            } else {
                result = this.semicheckedImg;
            }
        } else {
            result = this.uncheckedImg;
        }

        return result;
    }

    public int getCheckedCellsCount(int columnPosition, IConfigRegistry configRegistry) {
        int checkedCellsCount = 0;

        for (int rowPosition = 0; rowPosition < this.columnDataLayer.getRowCount(); rowPosition++) {
            ILayerCell columnCell = this.columnDataLayer.getCellByPosition(columnPosition, rowPosition);
            if (isChecked(columnCell, configRegistry)) {
                checkedCellsCount++;
            }
        }
        return checkedCellsCount;
    }

    protected boolean isChecked(ILayerCell cell, IConfigRegistry configRegistry) {
        return convertDataType(cell, configRegistry).booleanValue();
    }

    protected Boolean convertDataType(ILayerCell cell, IConfigRegistry configRegistry) {
        if (cell.getDataValue() instanceof Boolean) {
            return (Boolean) cell.getDataValue();
        }
        IDisplayConverter displayConverter = configRegistry.getConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                cell.getDisplayMode(),
                cell.getConfigLabels());
        Boolean convertedValue = null;
        if (displayConverter != null) {
            try {
                convertedValue =
                        (Boolean) displayConverter.canonicalToDisplayValue(
                                cell, configRegistry, cell.getDataValue());
            } catch (Exception e) {
                LOG.warn("Conversion failed", e); //$NON-NLS-1$
            }
        }
        if (convertedValue == null) {
            convertedValue = Boolean.FALSE;
        }
        return convertedValue;
    }

}

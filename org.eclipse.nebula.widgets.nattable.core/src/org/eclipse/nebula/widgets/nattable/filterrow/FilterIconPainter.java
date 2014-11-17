/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.graphics.Image;

/**
 * {@link ImagePainter} that is responsible for painting the icon into the
 * filter row cells. It will handle 3 different icons, where two of them are
 * mandatory:
 * <ul>
 * <li>removeFilterImage (mandatory) - Image that is rendered if a filter is
 * applied for that cell.</li>
 * <li>comboImage (mandatory) - Image that is rendered if no filter is applied
 * for that cell, but the filter cell editor is of type
 * {@link ComboBoxCellEditor}.</li>
 * <li>filterImage (optional) - Image that is rendered if no filter is applied
 * for that cell.</li>
 * </ul>
 */
public class FilterIconPainter extends ImagePainter {

    /**
     * Icon that will be rendered if no filter is applied for a cell in a filter
     * row. This can be <code>null</code> which will leave the cell blank. If
     * set it will indicate the filter purpose of the cell to a user and will
     * override the combo box icon, as both icons do not fit into the cell.
     */
    private Image filterImage;
    /**
     * Icon that will be rendered if a filter is applied for a cell in a filter
     * row. This overrides any other icon configuration.
     */
    private Image removeFilterImage;
    /**
     * Icon that is rendered if no filter is applied for a cell in a filter row,
     * and the configured cell editor for that cell is a
     * {@link ComboBoxCellEditor}. This image will be overriden in case a
     * filterImage is applied.
     */
    private Image comboImage;

    /**
     * Creates a {@link FilterIconPainter} that uses the default images for
     * removeFilter and combo box icons. No image for indicating the filter row
     * will be applied.
     */
    public FilterIconPainter() {
        this(null, null, null);
    }

    /**
     * Creates a {@link FilterIconPainter} that uses the default images for
     * removeFilter and combo box icons. Will apply the given image for
     * indicating the filter row to a user.
     *
     * @param filterImage
     *            Icon that will be rendered if no filter is applied for a cell
     *            in a filter row.
     */
    public FilterIconPainter(Image filterImage) {
        this(filterImage, null, null);
    }

    /**
     * Creates a {@link FilterIconPainter} that uses the given images for the
     * icons in the filter row cells.
     *
     * @param filterImage
     *            Icon that will be rendered if no filter is applied for a cell
     *            in a filter row.
     * @param removeFilterImage
     *            Icon that will be rendered if a filter is applied for a cell
     *            in a filter row.
     * @param comboImage
     *            Icon that is rendered if no filter is applied for a cell in a
     *            filter row, and the configured cell editor for that cell is a
     *            {@link ComboBoxCellEditor}.
     */
    public FilterIconPainter(Image filterImage, Image removeFilterImage, Image comboImage) {
        if (filterImage != null) {
            this.filterImage = filterImage;
        }

        this.removeFilterImage = removeFilterImage != null ? removeFilterImage
                : GUIHelper.getImage("remove_filter"); //$NON-NLS-1$
        this.comboImage = comboImage != null ? comboImage : GUIHelper
                .getImage("down_2"); //$NON-NLS-1$
    }

    @Override
    protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
        // If a filter value is present draw the remove filter icon
        if (ObjectUtils.isNotNull(cell.getDataValue())) {
            return this.removeFilterImage;
        }

        // If no filter value is present and a filter icon is specified,
        // draw the filter icon to indicate the cell as a filter cell
        if (this.filterImage != null) {
            return this.filterImage;
        }

        ICellEditor cellEditor = configRegistry.getConfigAttribute(
                EditConfigAttributes.CELL_EDITOR,
                cell.getDisplayMode(),
                cell.getConfigLabels().getLabels());

        // If a combo box is specified as the editor, draw the combo box arrow
        if (ObjectUtils.isNotNull(cellEditor)
                && cellEditor instanceof ComboBoxCellEditor) {
            return this.comboImage;
        }

        return null;
    }
}
/*******************************************************************************
 * Copyright (c) 2013, 2023 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Image;

/**
 * {@link ImagePainter} that is responsible for painting the icon into the
 * filter row cells that are configured to show Excel like filter comboboxes. It
 * will handle 2 different icons:
 * <ul>
 * <li>removeFilterImage - Image that is rendered if a filter is applied for
 * that cell.</li>
 * <li>comboImage - Image that is rendered if no filter is applied for that cell
 * </li>
 * </ul>
 */
public class ComboBoxFilterIconPainter extends ImagePainter {

    /**
     * The IComboBoxDataProvider that is used to fill the filter comboboxes.
     * Needed here to determine whether a filter is applied or not. This is
     * because if all items in the combo are selected, this means there is no
     * filter applied.
     */
    private final IComboBoxDataProvider comboBoxDataProvider;
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
     * @param comboBoxDataProvider
     *            The IComboBoxDataProvider that is used to fill the filter
     *            comboboxes.
     */
    public ComboBoxFilterIconPainter(IComboBoxDataProvider comboBoxDataProvider) {
        this(comboBoxDataProvider, null, null);
    }

    /**
     * @param comboBoxDataProvider
     *            The IComboBoxDataProvider that is used to fill the filter
     *            comboboxes.
     * @param removeFilterImage
     *            Icon that will be rendered if a filter is applied for a cell
     *            in a filter row.
     * @param comboImage
     *            Icon that is rendered if no filter is applied for a cell in a
     *            filter row, and the configured cell editor for that cell is a
     *            {@link ComboBoxCellEditor}.
     */
    public ComboBoxFilterIconPainter(
            IComboBoxDataProvider comboBoxDataProvider,
            Image removeFilterImage,
            Image comboImage) {
        this.comboBoxDataProvider = comboBoxDataProvider;
        this.removeFilterImage = removeFilterImage != null
                ? removeFilterImage
                : GUIHelper.getImage("remove_filter"); //$NON-NLS-1$
        this.comboImage = comboImage != null
                ? comboImage
                : GUIHelper.getImage("down_2"); //$NON-NLS-1$
    }

    @Override
    protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
        Image result = null;
        if (ComboBoxFilterUtils.isAllSelected(cell.getColumnIndex(), cell.getDataValue(), this.comboBoxDataProvider)) {
            result = this.comboImage;
        } else {
            result = this.removeFilterImage;
        }
        return result;
    }
}

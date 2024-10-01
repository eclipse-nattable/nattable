/*******************************************************************************
 * Copyright (c) 2023, 2024 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.EditConstants;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

/**
 * Helper class for Excel like combo box filters.
 *
 * @since 2.1
 */
public final class ComboBoxFilterUtils {

    private ComboBoxFilterUtils() {
        // private default constructor for helper class
    }

    /**
     * Check if all values of the combo box are selected, which actually means
     * "no filter".
     *
     * @param columnIndex
     *            The column index of the filter to check. Needed to retrieve
     *            the values in the combobox.
     * @param cellData
     *            The filter value to check.
     * @param comboBoxDataProvider
     *            The {@link IComboBoxDataProvider} that provides the entries
     *            available in the filter combo.
     * @return <code>true</code> if all values are selected and therefore no
     *         filter is applied, <code>false</code> if not.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean isAllSelected(int columnIndex, Object cellData, IComboBoxDataProvider comboBoxDataProvider) {
        if (EditConstants.SELECT_ALL_ITEMS_VALUE.equals(cellData)) {
            return true;
        }

        if (cellData instanceof Collection) {
            Collection dataCollection = (Collection) cellData;
            List<?> dataProviderList;

            if (comboBoxDataProvider instanceof FilterRowComboBoxDataProvider) {
                dataProviderList = ((FilterRowComboBoxDataProvider) comboBoxDataProvider).getAllValues(columnIndex);
            } else {
                dataProviderList = comboBoxDataProvider.getValues(columnIndex, 0);
            }

            return new HashSet<>(dataCollection).containsAll(dataProviderList);
        }

        return false;
    }

    /**
     * Check if the editor registered for the given column is a
     * {@link FilterRowComboBoxCellEditor}.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve the configured
     *            cell editor.
     * @param columnIndex
     *            The column index for which the filter editor should be
     *            inspected.
     * @return <code>true</code> if the filter editor configured for the given
     *         column index is of type {@link FilterRowComboBoxCellEditor}.
     */
    public static boolean isFilterRowComboBoxCellEditor(IConfigRegistry configRegistry, int columnIndex) {
        ICellEditor cellEditor = configRegistry.getConfigAttribute(
                EditConfigAttributes.CELL_EDITOR,
                DisplayMode.NORMAL,
                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex, GridRegion.FILTER_ROW);
        return (cellEditor instanceof FilterRowComboBoxCellEditor);
    }

    /**
     * Checks if a filter is active. Handles default editors and combobox filter
     * editors.
     *
     * @param <T>
     *            The type of the underlying row objects.
     * @param filterRowDataLayer
     *            The {@link FilterRowDataLayer} needed to access the filter
     *            data.
     * @param comboBoxDataProvider
     *            The {@link IComboBoxDataProvider} that provides the entries
     *            available in the filter combo.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve the configured
     *            cell editor.
     * @return <code>true</code> if any type of filter is currently applied on
     *         the table, <code>false</code> if not.
     *
     * @since 2.2
     */
    public static <T> boolean isFilterActive(
            FilterRowDataLayer<T> filterRowDataLayer,
            IComboBoxDataProvider comboBoxDataProvider,
            IConfigRegistry configRegistry) {

        for (int column = 0; column < filterRowDataLayer.getFilterRowDataProvider().getColumnCount(); column++) {
            Object filterValue = filterRowDataLayer.getDataValue(column, 0);
            if (ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(configRegistry, column)) {
                // if combobox filter, check if all is selected
                if (!ComboBoxFilterUtils.isAllSelected(column, filterValue, comboBoxDataProvider)) {
                    return true;
                }
            } else {
                if (filterValue != null && !filterValue.toString().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

}

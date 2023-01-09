/*******************************************************************************
 * Copyright (c) 2023 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowPainter;
import org.eclipse.nebula.widgets.nattable.filterrow.event.ClearFilterIconMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.events.MouseEvent;

/**
 * Specialization of a {@link ClearFilterIconMouseEventMatcher} that only
 * matches for the filter row region if a filter is applied in the clicked cell
 * and the click was executed on the painted icon in that cell (usually the
 * clear filter icon) if the filter row painter is used with the Excel like
 * combo box filters.
 *
 * @since 2.1
 */
public class ComboBoxClearFilterIconMouseEventMatcher extends CellPainterMouseEventMatcher {

    /**
     * The {@link IComboBoxDataProvider} that is used to fill the filter
     * comboboxes. Needed here to determine whether a filter is applied or not.
     * This is because if all items in the combo are selected, this means there
     * is no filter applied.
     */
    private final IComboBoxDataProvider comboBoxDataProvider;

    /**
     * Create a new {@link ComboBoxClearFilterIconMouseEventMatcher} for the
     * given {@link FilterRowPainter}
     *
     * @param filterRowPainter
     *            The {@link FilterRowPainter} needed to determine the filter
     *            icon painter.
     * @param comboBoxDataProvider
     *            The {@link IComboBoxDataProvider} that is used to fill the
     *            filter comboboxes. Needed here to determine whether a filter
     *            is applied or not.
     */
    public ComboBoxClearFilterIconMouseEventMatcher(FilterRowPainter filterRowPainter, IComboBoxDataProvider comboBoxDataProvider) {
        super(GridRegion.FILTER_ROW,
                MouseEventMatcher.LEFT_BUTTON,
                filterRowPainter.getFilterIconPainter().getClass());
        this.comboBoxDataProvider = comboBoxDataProvider;
    }

    @Override
    public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
        boolean matches = super.matches(natTable, event, regionLabels);
        if (matches) {
            ILayerCell cell = natTable.getCellByPosition(
                    natTable.getColumnPositionByX(event.x),
                    natTable.getRowPositionByY(event.y));

            matches = !ComboBoxFilterUtils.isAllSelected(cell.getColumnIndex(), cell.getDataValue(), this.comboBoxDataProvider);
        }
        return matches;
    }
}

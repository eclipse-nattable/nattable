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
 *     Roman Flueckiger <roman.flueckiger@mac.com> - Bug 451486
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.columnChooser.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnChooser;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

public class DisplayColumnChooserCommandHandler extends AbstractLayerCommandHandler<DisplayColumnChooserCommand> {

    private final ColumnHideShowLayer columnHideShowLayer;
    private final ColumnGroupHeaderLayer columnGroupHeaderLayer;
    private final ColumnGroupModel columnGroupModel;
    private final SelectionLayer selectionLayer;
    private final DataLayer columnHeaderDataLayer;
    private final ColumnHeaderLayer columnHeaderLayer;
    private final boolean sortAvailableColumns;
    private final boolean preventHidingAllColumns;
    private IDialogSettings dialogSettings;
    private List<Integer> nonModifiableColumns = new ArrayList<>();

    private final org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer cghLayer;

    public DisplayColumnChooserCommandHandler(SelectionLayer selectionLayer,
            ColumnHideShowLayer columnHideShowLayer,
            ColumnHeaderLayer columnHeaderLayer,
            DataLayer columnHeaderDataLayer,
            ColumnGroupHeaderLayer cgHeader,
            ColumnGroupModel columnGroupModel) {

        this(selectionLayer, columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer, cgHeader, columnGroupModel, false, false);
    }

    public DisplayColumnChooserCommandHandler(
            SelectionLayer selectionLayer,
            ColumnHideShowLayer columnHideShowLayer,
            ColumnHeaderLayer columnHeaderLayer,
            DataLayer columnHeaderDataLayer,
            ColumnGroupHeaderLayer cgHeader,
            ColumnGroupModel columnGroupModel,
            boolean sortAvailableColumns) {

        this(selectionLayer, columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer, cgHeader, columnGroupModel, sortAvailableColumns, false);
    }

    public DisplayColumnChooserCommandHandler(
            SelectionLayer selectionLayer,
            ColumnHideShowLayer columnHideShowLayer,
            ColumnHeaderLayer columnHeaderLayer,
            DataLayer columnHeaderDataLayer,
            ColumnGroupHeaderLayer cgHeader,
            ColumnGroupModel columnGroupModel,
            boolean sortAvalableColumns,
            boolean preventHidingAllColumns) {

        this.selectionLayer = selectionLayer;
        this.columnHideShowLayer = columnHideShowLayer;
        this.columnHeaderLayer = columnHeaderLayer;
        this.columnHeaderDataLayer = columnHeaderDataLayer;
        this.columnGroupHeaderLayer = cgHeader;
        this.columnGroupModel = columnGroupModel;
        this.sortAvailableColumns = sortAvalableColumns;
        this.preventHidingAllColumns = preventHidingAllColumns;
        this.cghLayer = null;
    }

    /**
     * Create the {@link DisplayColumnChooserCommandHandler} for the new
     * performance column grouping feature showing the columns in the available
     * tree unsorted.
     *
     * @param columnHideShowLayer
     *            The {@link ColumnHideShowLayer} for hide/show support.
     * @param columnHeaderLayer
     *            The {@link ColumnHeaderLayer} for retrieving column header
     *            information.
     * @param columnHeaderDataLayer
     *            The {@link DataLayer} of the column header region for
     *            retrieving column header information.
     * @param cgHeader
     *            The new performance
     *            {@link org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer}
     *            to support column grouping. Cannot be <code>null</code>.
     * @throws IllegalArgumentException
     *             if cgHeader is null
     *
     * @since 1.6
     */
    public DisplayColumnChooserCommandHandler(
            ColumnHideShowLayer columnHideShowLayer,
            ColumnHeaderLayer columnHeaderLayer,
            DataLayer columnHeaderDataLayer,
            org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer cgHeader) {

        this(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer, cgHeader, false);
    }

    /**
     * Create the {@link DisplayColumnChooserCommandHandler} for the new
     * performance column grouping feature.
     *
     * @param columnHideShowLayer
     *            The {@link ColumnHideShowLayer} for hide/show support.
     * @param columnHeaderLayer
     *            The {@link ColumnHeaderLayer} for retrieving column header
     *            information.
     * @param columnHeaderDataLayer
     *            The {@link DataLayer} of the column header region for
     *            retrieving column header information.
     * @param cgHeader
     *            The new performance
     *            {@link org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer}
     *            to support column grouping. Cannot be <code>null</code>.
     * @param sortAvailableColumns
     *            Flag to configure if entries in the available tree should be
     *            displayed in sorted order.
     * @throws IllegalArgumentException
     *             if cgHeader is null
     *
     * @since 1.6
     */
    public DisplayColumnChooserCommandHandler(
            ColumnHideShowLayer columnHideShowLayer,
            ColumnHeaderLayer columnHeaderLayer,
            DataLayer columnHeaderDataLayer,
            org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer cgHeader,
            boolean sortAvailableColumns) {

        if (cgHeader == null) {
            throw new IllegalArgumentException("cgHeader cannot be null"); //$NON-NLS-1$
        }

        this.selectionLayer = null;
        this.columnHideShowLayer = columnHideShowLayer;
        this.columnHeaderLayer = columnHeaderLayer;
        this.columnHeaderDataLayer = columnHeaderDataLayer;
        this.columnGroupHeaderLayer = null;
        this.columnGroupModel = null;
        this.sortAvailableColumns = sortAvailableColumns;
        this.preventHidingAllColumns = false;
        this.cghLayer = cgHeader;
    }

    @Override
    public boolean doCommand(DisplayColumnChooserCommand command) {
        ColumnChooser columnChooser = this.cghLayer == null
                ? new ColumnChooser(
                        command.getNatTable().getShell(),
                        this.selectionLayer,
                        this.columnHideShowLayer,
                        this.columnHeaderLayer,
                        this.columnHeaderDataLayer,
                        this.columnGroupHeaderLayer,
                        this.columnGroupModel,
                        this.sortAvailableColumns,
                        this.preventHidingAllColumns)
                : new ColumnChooser(
                        command.getNatTable().getShell(),
                        this.columnHideShowLayer,
                        this.columnHeaderLayer,
                        this.columnHeaderDataLayer,
                        this.cghLayer,
                        this.sortAvailableColumns);

        columnChooser.setDialogSettings(this.dialogSettings);
        columnChooser.addNonModifiableColumn(this.nonModifiableColumns.toArray(new Integer[] {}));
        columnChooser.openDialog();
        return true;
    }

    public void setDialogSettings(IDialogSettings dialogSettings) {
        this.dialogSettings = dialogSettings;
    }

    public void addNonModifiableColumn(Integer... columnIndexes) {
        for (Integer column : columnIndexes) {
            this.nonModifiableColumns.add(column);
        }
    }

    public void removeNonModifiableColumn(Integer... columnIndexes) {
        for (Integer column : columnIndexes) {
            this.nonModifiableColumns.remove(column);
        }
    }

    @Override
    public Class<DisplayColumnChooserCommand> getCommandClass() {
        return DisplayColumnChooserCommand.class;
    }
}

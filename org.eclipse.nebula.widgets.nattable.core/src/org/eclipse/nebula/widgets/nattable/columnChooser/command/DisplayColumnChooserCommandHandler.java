/*******************************************************************************
 * Copyright (c) 2012, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    private List<Integer> nonModifiableColumns = new ArrayList<Integer>();

    public DisplayColumnChooserCommandHandler(SelectionLayer selectionLayer,
            ColumnHideShowLayer columnHideShowLayer,
            ColumnHeaderLayer columnHeaderLayer,
            DataLayer columnHeaderDataLayer,
            ColumnGroupHeaderLayer cgHeader,
            ColumnGroupModel columnGroupModel) {

        this(selectionLayer, columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer, cgHeader, columnGroupModel, false, false);
    }

    public DisplayColumnChooserCommandHandler(SelectionLayer selectionLayer,
            ColumnHideShowLayer columnHideShowLayer,
            ColumnHeaderLayer columnHeaderLayer,
            DataLayer columnHeaderDataLayer, ColumnGroupHeaderLayer cgHeader,
            ColumnGroupModel columnGroupModel, boolean sortAvailableColumns) {

        this(selectionLayer, columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer, cgHeader, columnGroupModel, sortAvailableColumns, false);
    }

    public DisplayColumnChooserCommandHandler(SelectionLayer selectionLayer,
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
    }

    @Override
    public boolean doCommand(DisplayColumnChooserCommand command) {
        ColumnChooser columnChooser = new ColumnChooser(command.getNatTable()
                .getShell(), this.selectionLayer, this.columnHideShowLayer,
                this.columnHeaderLayer, this.columnHeaderDataLayer,
                this.columnGroupHeaderLayer, this.columnGroupModel,
                this.sortAvailableColumns, this.preventHidingAllColumns);

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

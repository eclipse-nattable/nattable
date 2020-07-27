/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Roman Flueckiger <roman.flueckiger@mac.com> - Bug 451486
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.columnChooser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.columnChooser.gui.ColumnChooserDialog;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.command.ColumnGroupExpandCollapseCommand;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnsAndGroupsCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.swt.widgets.Shell;

public class ColumnChooser {

    protected final ColumnChooserDialog columnChooserDialog;
    protected final ColumnHideShowLayer columnHideShowLayer;
    protected final DataLayer columnHeaderDataLayer;
    protected final ColumnHeaderLayer columnHeaderLayer;
    protected List<ColumnEntry> hiddenColumnEntries;
    protected List<ColumnEntry> visibleColumnsEntries;
    protected final ColumnGroupModel columnGroupModel;
    protected final SelectionLayer selectionLayer;
    protected final boolean sortAvailableColumns;
    protected final boolean preventHidingAllColumns;

    private final org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer columnGroupHeaderLayer;

    List<Integer> nonModifiableColumns = new ArrayList<Integer>();

    /**
     * Constructor to be used with the old column grouping feature.
     *
     * @param shell
     *            The parent shell to be used for creating the
     *            {@link ColumnChooserDialog}.
     * @param selectionLayer
     *            The {@link SelectionLayer} needed for
     *            position-index-transformation and to execute commands.
     * @param columnHideShowLayer
     *            The {@link ColumnHideShowLayer} for hide/show support.
     * @param columnHeaderLayer
     *            The {@link ColumnHeaderLayer} for retrieving column header
     *            information.
     * @param columnHeaderDataLayer
     *            The {@link DataLayer} of the column header region for
     *            retrieving column header information.
     * @param columnGroupHeaderLayer
     *            The old {@link ColumnGroupHeaderLayer}, currently unused.
     * @param columnGroupModel
     *            The old {@link ColumnGroupModel} used to support column
     *            grouping with the old column grouping feature, can be
     *            <code>null</code> if column grouping is not supported.
     * @param sortAvailableColumns
     *            Flag to configure if entries in the available tree should be
     *            displayed in sorted order.
     */
    public ColumnChooser(Shell shell,
            SelectionLayer selectionLayer,
            ColumnHideShowLayer columnHideShowLayer,
            ColumnHeaderLayer columnHeaderLayer,
            DataLayer columnHeaderDataLayer,
            ColumnGroupHeaderLayer columnGroupHeaderLayer,
            ColumnGroupModel columnGroupModel,
            boolean sortAvailableColumns) {

        this(shell, selectionLayer, columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer, columnGroupHeaderLayer, columnGroupModel, sortAvailableColumns, false);
    }

    /**
     * Constructor to be used with the old column grouping feature.
     *
     * @param shell
     *            The parent shell to be used for creating the
     *            {@link ColumnChooserDialog}.
     * @param selectionLayer
     *            The {@link SelectionLayer} needed for
     *            position-index-transformation and to execute commands.
     * @param columnHideShowLayer
     *            The {@link ColumnHideShowLayer} for hide/show support.
     * @param columnHeaderLayer
     *            The {@link ColumnHeaderLayer} for retrieving column header
     *            information.
     * @param columnHeaderDataLayer
     *            The {@link DataLayer} of the column header region for
     *            retrieving column header information.
     * @param columnGroupHeaderLayer
     *            The old {@link ColumnGroupHeaderLayer}, currently unused.
     * @param columnGroupModel
     *            The old {@link ColumnGroupModel} used to support column
     *            grouping with the old column grouping feature, can be
     *            <code>null</code> if column grouping is not supported.
     * @param sortAvailableColumns
     *            Flag to configure if entries in the available tree should be
     *            displayed in sorted order.
     * @param preventHidingAllColumns
     *            Flag to prevent hiding all columns, currently without
     *            function.
     */
    public ColumnChooser(Shell shell,
            SelectionLayer selectionLayer,
            ColumnHideShowLayer columnHideShowLayer,
            ColumnHeaderLayer columnHeaderLayer,
            DataLayer columnHeaderDataLayer,
            ColumnGroupHeaderLayer columnGroupHeaderLayer,
            ColumnGroupModel columnGroupModel,
            boolean sortAvailableColumns,
            boolean preventHidingAllColumns) {

        this.selectionLayer = selectionLayer;
        this.columnHideShowLayer = columnHideShowLayer;
        this.columnHeaderLayer = columnHeaderLayer;
        this.columnHeaderDataLayer = columnHeaderDataLayer;
        this.columnGroupModel = columnGroupModel;
        this.sortAvailableColumns = sortAvailableColumns;
        this.preventHidingAllColumns = preventHidingAllColumns;
        this.columnGroupHeaderLayer = null;

        this.columnChooserDialog = new ColumnChooserDialog(shell, Messages.getString("ColumnChooser.availableColumns"), Messages.getString("ColumnChooser.selectedColumns")); //$NON-NLS-1$ //$NON-NLS-2$
        this.columnChooserDialog.setPreventHidingAllColumns(preventHidingAllColumns);
    }

    /**
     * Constructor to be used with the new performance column grouping feature.
     *
     * @param shell
     *            The parent shell to be used for creating the
     *            {@link ColumnChooserDialog}.
     * @param columnHideShowLayer
     *            The {@link ColumnHideShowLayer} for hide/show support.
     * @param columnHeaderLayer
     *            The {@link ColumnHeaderLayer} for retrieving column header
     *            information.
     * @param columnHeaderDataLayer
     *            The {@link DataLayer} of the column header region for
     *            retrieving column header information.
     * @param columnGroupHeaderLayer
     *            The new performance
     *            {@link org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer}
     *            to support column grouping. <code>null</code> is not allowed.
     * @param sortAvailableColumns
     *            Flag to configure if entries in the available tree should be
     *            displayed in sorted order.
     * @throws IllegalArgumentException
     *             if columnGroupHeaderLayer is null
     * @since 1.6
     */
    public ColumnChooser(Shell shell,
            ColumnHideShowLayer columnHideShowLayer,
            ColumnHeaderLayer columnHeaderLayer,
            DataLayer columnHeaderDataLayer,
            org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer columnGroupHeaderLayer,
            boolean sortAvailableColumns) {

        if (columnGroupHeaderLayer == null) {
            throw new IllegalArgumentException("columnGroupHeaderLayer cannot be null"); //$NON-NLS-1$
        }

        this.selectionLayer = null;
        this.columnHideShowLayer = columnHideShowLayer;
        this.columnHeaderLayer = columnHeaderLayer;
        this.columnHeaderDataLayer = columnHeaderDataLayer;
        this.columnGroupModel = null;
        this.sortAvailableColumns = sortAvailableColumns;
        this.preventHidingAllColumns = false;
        this.columnGroupHeaderLayer = columnGroupHeaderLayer;

        this.columnChooserDialog = new ColumnChooserDialog(shell, Messages.getString("ColumnChooser.availableColumns"), Messages.getString("ColumnChooser.selectedColumns")); //$NON-NLS-1$ //$NON-NLS-2$
        this.columnChooserDialog.setPreventHidingAllColumns(this.preventHidingAllColumns);
    }

    public void setDialogSettings(IDialogSettings dialogSettings) {
        this.columnChooserDialog.setDialogSettings(dialogSettings);
    }

    public void openDialog() {
        this.columnChooserDialog.create();

        this.hiddenColumnEntries = getHiddenColumnEntries();
        if (this.columnGroupHeaderLayer != null) {
            this.columnChooserDialog.populateAvailableTree(this.hiddenColumnEntries, this.columnGroupHeaderLayer, this.sortAvailableColumns);
        } else {
            this.columnChooserDialog.populateAvailableTree(this.hiddenColumnEntries, this.columnGroupModel, this.sortAvailableColumns);
        }

        this.visibleColumnsEntries = getVisibleColumnEntries();
        if (this.columnGroupHeaderLayer != null) {
            this.columnChooserDialog.populateSelectedTree(this.visibleColumnsEntries, this.columnGroupHeaderLayer);
        } else {
            this.columnChooserDialog.populateSelectedTree(this.visibleColumnsEntries, this.columnGroupModel);
        }

        this.columnChooserDialog.expandAllLeaves();

        addListenersOnColumnChooserDialog();
        this.columnChooserDialog.open();
    }

    private void addListenersOnColumnChooserDialog() {

        this.columnChooserDialog.addListener(new ISelectionTreeListener() {

            @Override
            public void itemsRemoved(List<ColumnEntry> removedItems) {
                ColumnChooserUtils.hideColumnEntries(removedItems, ColumnChooser.this.columnHideShowLayer);
                refreshColumnChooserDialog();
            }

            @Override
            public void itemsSelected(List<ColumnEntry> addedItems) {
                ColumnChooserUtils.showColumnEntries(addedItems, ColumnChooser.this.columnHideShowLayer);
                refreshColumnChooserDialog();
                ColumnChooser.this.columnChooserDialog
                        .setSelectionIncludingNested(ColumnChooserUtils.getColumnEntryIndexes(addedItems));
            }

            @Override
            public void itemsMoved(MoveDirectionEnum direction,
                    List<ColumnGroupEntry> movedColumnGroupEntries,
                    List<ColumnEntry> movedColumnEntries,
                    List<List<Integer>> fromPositions,
                    List<Integer> toPositions) {

                moveItems(direction, movedColumnGroupEntries, movedColumnEntries, fromPositions, toPositions);
            }

            /**
             * Fire appropriate commands depending on the events received from
             * the column chooser dialog
             *
             * @param direction
             * @param movedColumnGroupEntries
             * @param movedColumnEntries
             * @param fromPositions
             * @param toPositions
             */
            private void moveItems(MoveDirectionEnum direction,
                    List<ColumnGroupEntry> movedColumnGroupEntries,
                    List<ColumnEntry> movedColumnEntries,
                    List<List<Integer>> fromPositions,
                    List<Integer> toPositions) {

                for (int i = 0; i < fromPositions.size(); i++) {
                    boolean columnGroupMoved = columnGroupMoved(fromPositions.get(i), movedColumnGroupEntries);
                    boolean multipleColumnsMoved = fromPositions.get(i).size() > 1;

                    ILayerCommand command = null;
                    if (!columnGroupMoved && !multipleColumnsMoved) {
                        int fromPosition = fromPositions.get(i).get(0).intValue();
                        int toPosition = adjustToPosition(direction, toPositions.get(i));
                        command = new ColumnReorderCommand(ColumnChooser.this.columnHideShowLayer, fromPosition, toPosition);
                    } else if (columnGroupMoved && multipleColumnsMoved) {
                        command = new ReorderColumnsAndGroupsCommand(ColumnChooser.this.columnHideShowLayer, fromPositions.get(i),
                                adjustToPosition(direction, toPositions.get(i)));
                    } else if (!columnGroupMoved && multipleColumnsMoved) {
                        command = new MultiColumnReorderCommand(ColumnChooser.this.columnHideShowLayer, fromPositions.get(i),
                                adjustToPosition(direction, toPositions.get(i)));
                    } else if (columnGroupMoved && !multipleColumnsMoved) {
                        command = new ReorderColumnGroupCommand(ColumnChooser.this.columnHideShowLayer, fromPositions.get(i)
                                .get(0), adjustToPosition(direction, toPositions.get(i)));
                    }
                    ColumnChooser.this.columnHideShowLayer.doCommand(command);
                }

                refreshColumnChooserDialog();
                ColumnChooser.this.columnChooserDialog.setSelectionIncludingNested(ColumnChooserUtils.getColumnEntryIndexes(movedColumnEntries));
            }

            private int adjustToPosition(MoveDirectionEnum direction, Integer toColumnPosition) {
                if (MoveDirectionEnum.DOWN == direction) {
                    return toColumnPosition + 1;
                } else {
                    return toColumnPosition;
                }
            }

            private boolean columnGroupMoved(List<Integer> fromPositions, List<ColumnGroupEntry> movedColumnGroupEntries) {
                for (ColumnGroupEntry columnGroupEntry : movedColumnGroupEntries) {
                    if (fromPositions.contains(columnGroupEntry.getFirstElementPosition())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void itemsCollapsed(ColumnGroupEntry columnGroupEntry) {
                if (ColumnChooser.this.columnGroupHeaderLayer != null) {
                    int index = columnGroupEntry.getFirstElementIndex();
                    int position = ColumnChooser.this.columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(index);
                    ColumnChooser.this.columnGroupHeaderLayer.doCommand(new ColumnGroupExpandCollapseCommand(ColumnChooser.this.columnGroupHeaderLayer.getPositionLayer(), position));
                } else {
                    int index = columnGroupEntry.getFirstElementIndex();
                    int position = ColumnChooser.this.selectionLayer.getColumnPositionByIndex(index);
                    ColumnChooser.this.selectionLayer.doCommand(new ColumnGroupExpandCollapseCommand(ColumnChooser.this.selectionLayer, position));
                }
            }

            @Override
            public void itemsExpanded(ColumnGroupEntry columnGroupEntry) {
                if (ColumnChooser.this.columnGroupHeaderLayer != null) {
                    int index = columnGroupEntry.getFirstElementIndex();
                    int position = ColumnChooser.this.columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(index);
                    ColumnChooser.this.columnGroupHeaderLayer.doCommand(new ColumnGroupExpandCollapseCommand(ColumnChooser.this.columnGroupHeaderLayer.getPositionLayer(), position));
                } else {
                    int index = columnGroupEntry.getFirstElementIndex();
                    int position = ColumnChooser.this.selectionLayer.getColumnPositionByIndex(index);
                    ColumnChooser.this.selectionLayer.doCommand(new ColumnGroupExpandCollapseCommand(ColumnChooser.this.selectionLayer, position));
                }
            }
        });
    }

    private void refreshColumnChooserDialog() {
        this.hiddenColumnEntries = getHiddenColumnEntries();
        this.visibleColumnsEntries = getVisibleColumnEntries();

        this.columnChooserDialog.removeAllLeaves();

        if (this.columnGroupHeaderLayer != null) {
            this.columnChooserDialog.populateSelectedTree(this.visibleColumnsEntries, this.columnGroupHeaderLayer);
            this.columnChooserDialog.populateAvailableTree(this.hiddenColumnEntries, this.columnGroupHeaderLayer, this.sortAvailableColumns);
        } else {
            this.columnChooserDialog.populateSelectedTree(this.visibleColumnsEntries, this.columnGroupModel);
            this.columnChooserDialog.populateAvailableTree(this.hiddenColumnEntries, this.columnGroupModel, this.sortAvailableColumns);
        }
        this.columnChooserDialog.expandAllLeaves();
    }

    protected List<ColumnEntry> getHiddenColumnEntries() {
        List<ColumnEntry> columnEntries = ColumnChooserUtils.getHiddenColumnEntries(this.columnHideShowLayer, this.columnHeaderLayer, this.columnHeaderDataLayer);

        if (!this.nonModifiableColumns.isEmpty()) {
            for (Iterator<ColumnEntry> it = columnEntries.iterator(); it.hasNext();) {
                ColumnEntry entry = it.next();
                if (this.nonModifiableColumns.contains(entry.getIndex())) {
                    it.remove();
                }
            }
        }

        return columnEntries;
    }

    private List<ColumnEntry> getVisibleColumnEntries() {
        List<ColumnEntry> columnEntries = ColumnChooserUtils.getVisibleColumnsEntries(this.columnHideShowLayer, this.columnHeaderLayer, this.columnHeaderDataLayer);

        if (!this.nonModifiableColumns.isEmpty()) {
            for (Iterator<ColumnEntry> it = columnEntries.iterator(); it.hasNext();) {
                ColumnEntry entry = it.next();
                if (this.nonModifiableColumns.contains(entry.getIndex())) {
                    it.remove();
                }
            }
        }

        return columnEntries;
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

}

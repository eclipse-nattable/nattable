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
package org.eclipse.nebula.widgets.nattable.group.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.columnRename.ColumnRenameDialog;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.event.GroupColumnsEvent;
import org.eclipse.nebula.widgets.nattable.group.event.UngroupColumnsEvent;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class ColumnGroupsCommandHandler extends
        AbstractLayerCommandHandler<IColumnGroupCommand> {

    private final ColumnGroupModel model;
    private final SelectionLayer selectionLayer;
    private final ColumnGroupHeaderLayer contextLayer;
    private Map<Integer, Integer> columnIndexesToPositionsMap;

    public ColumnGroupsCommandHandler(ColumnGroupModel model,
            SelectionLayer selectionLayer, ColumnGroupHeaderLayer contextLayer) {
        this.model = model;
        this.selectionLayer = selectionLayer;
        this.contextLayer = contextLayer;
    }

    @Override
    public boolean doCommand(IColumnGroupCommand command) {
        if (command instanceof CreateColumnGroupCommand) {
            if (this.columnIndexesToPositionsMap.size() > 0) {
                handleGroupColumnsCommand(((CreateColumnGroupCommand) command)
                        .getColumnGroupName());
                this.columnIndexesToPositionsMap.clear();
                return true;
            }
        } else if (command instanceof OpenCreateColumnGroupDialog) {
            OpenCreateColumnGroupDialog openDialogCommand = (OpenCreateColumnGroupDialog) command;
            loadSelectedColumnsIndexesWithPositions();
            if (this.selectionLayer.getFullySelectedColumnPositions().length > 0
                    && this.columnIndexesToPositionsMap.size() > 0) {
                openDialogCommand.openDialog(this.contextLayer);
            } else {
                openDialogCommand.openErrorBox(Messages
                        .getString("ColumnGroups.selectNonGroupedColumns")); //$NON-NLS-1$
            }
            return true;
        } else if (command instanceof UngroupColumnCommand) {
            handleUngroupCommand();
            return true;
        } else if (command instanceof RemoveColumnGroupCommand) {
            RemoveColumnGroupCommand removeColumnGroupCommand = (RemoveColumnGroupCommand) command;
            int columnIndex = removeColumnGroupCommand.getColumnIndex();
            handleRemoveColumnGroupCommand(columnIndex);
            return true;
        } else if (command instanceof DisplayColumnGroupRenameDialogCommand) {
            return displayColumnGroupRenameDialog((DisplayColumnGroupRenameDialogCommand) command);
        }
        return false;
    }

    private boolean displayColumnGroupRenameDialog(
            DisplayColumnGroupRenameDialogCommand command) {
        int columnPosition = command.getColumnPosition();

        ColumnRenameDialog dialog = new ColumnRenameDialog(Display.getDefault()
                .getActiveShell(), null, null);
        Rectangle colHeaderBounds = this.contextLayer.getBoundsByPosition(
                columnPosition, 0);
        Point point = new Point(colHeaderBounds.x, colHeaderBounds.y
                + colHeaderBounds.height);
        dialog.setLocation(command.toDisplayCoordinates(point));
        dialog.open();

        if (!dialog.isCancelPressed()) {
            int columnIndex = this.contextLayer
                    .getColumnIndexByPosition(columnPosition);
            ColumnGroup columnGroup = this.model.getColumnGroupByIndex(columnIndex);
            columnGroup.setName(dialog.getNewColumnLabel());
        }

        return true;
    }

    @Override
    public Class<IColumnGroupCommand> getCommandClass() {
        return IColumnGroupCommand.class;
    }

    protected void loadSelectedColumnsIndexesWithPositions() {
        this.columnIndexesToPositionsMap = new LinkedHashMap<Integer, Integer>();
        int[] fullySelectedColumns = this.selectionLayer
                .getFullySelectedColumnPositions();

        if (fullySelectedColumns.length > 0) {
            for (int index = 0; index < fullySelectedColumns.length; index++) {
                final int columnPosition = fullySelectedColumns[index];
                int columnIndex = this.selectionLayer
                        .getColumnIndexByPosition(columnPosition);
                if (this.model.isPartOfAGroup(columnIndex)) {
                    this.columnIndexesToPositionsMap.clear();
                    break;
                }
                this.columnIndexesToPositionsMap.put(Integer.valueOf(columnIndex),
                        Integer.valueOf(columnPosition));
            }

        }
    }

    public void handleGroupColumnsCommand(String columnGroupName) {

        try {
            List<Integer> selectedPositions = new ArrayList<Integer>();
            int[] fullySelectedColumns = new int[this.columnIndexesToPositionsMap
                    .size()];
            int count = 0;
            for (Integer columnIndex : this.columnIndexesToPositionsMap.keySet()) {
                fullySelectedColumns[count++] = columnIndex.intValue();
                selectedPositions.add(this.columnIndexesToPositionsMap
                        .get(columnIndex));
            }
            this.model.addColumnsIndexesToGroup(columnGroupName,
                    fullySelectedColumns);
            this.selectionLayer.doCommand(new MultiColumnReorderCommand(
                    this.selectionLayer, selectedPositions, selectedPositions.get(0)
                            .intValue()));
            this.selectionLayer.clear();
        } catch (Throwable t) {}
        this.contextLayer.fireLayerEvent(new GroupColumnsEvent(this.contextLayer));
    }

    public void handleUngroupCommand() {
        // Grab fully selected column positions
        int[] fullySelectedColumns = this.selectionLayer
                .getFullySelectedColumnPositions();
        Map<String, Integer> toColumnPositions = new HashMap<String, Integer>();
        if (fullySelectedColumns.length > 0) {

            // Pick the ones which belong to a group and remove them from the
            // group
            for (int index = 0; index < fullySelectedColumns.length; index++) {
                final int columnPosition = fullySelectedColumns[index];
                int columnIndex = this.selectionLayer
                        .getColumnIndexByPosition(columnPosition);
                if (this.model.isPartOfAGroup(columnIndex)
                        && !this.model.isPartOfAnUnbreakableGroup(columnIndex)) {
                    handleRemovalFromGroup(toColumnPositions, columnIndex);
                }
            }
            // The groups which were affected should be reordered to the start
            // position, this should group all columns together
            Collection<Integer> values = toColumnPositions.values();
            final Iterator<Integer> toColumnPositionsIterator = values
                    .iterator();
            while (toColumnPositionsIterator.hasNext()) {
                Integer toColumnPosition = toColumnPositionsIterator.next();
                this.selectionLayer.doCommand(new ReorderColumnGroupCommand(
                        this.selectionLayer, toColumnPosition.intValue(),
                        toColumnPosition.intValue()));
            }
            this.selectionLayer.clear();
        }

        this.contextLayer.fireLayerEvent(new UngroupColumnsEvent(this.contextLayer));
    }

    private void handleRemovalFromGroup(Map<String, Integer> toColumnPositions,
            int columnIndex) {
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(columnIndex);

        final String columnGroupName = columnGroup.getName();
        final List<Integer> columnIndexesInGroup = columnGroup.getMembers();
        final int columnGroupSize = columnIndexesInGroup.size();
        if (!toColumnPositions.containsKey(columnGroupName)) {
            for (int colGroupIndex : columnIndexesInGroup) {
                if (ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(
                        colGroupIndex, this.contextLayer, this.selectionLayer, this.model)) {
                    int toPosition = this.selectionLayer
                            .getColumnPositionByIndex(colGroupIndex);
                    if (colGroupIndex == columnIndex) {
                        if (columnGroupSize == 1) {
                            break;
                        } else {
                            toPosition++;
                        }
                    }
                    toColumnPositions.put(columnGroupName,
                            Integer.valueOf(toPosition));
                    break;
                }
            }
        } else {
            if (columnGroupSize - 1 <= 0) {
                toColumnPositions.remove(columnGroupName);
            }
        }
        columnGroup.removeColumn(columnIndex);
    }

    private void handleRemoveColumnGroupCommand(int columnIndex) {
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(columnIndex);
        this.model.removeColumnGroup(columnGroup);
    }

}

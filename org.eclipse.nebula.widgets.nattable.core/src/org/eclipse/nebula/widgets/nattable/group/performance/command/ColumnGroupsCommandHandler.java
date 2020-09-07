/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance.command;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.command.CreateColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.DisplayColumnGroupRenameDialogCommand;
import org.eclipse.nebula.widgets.nattable.group.command.IColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.RemoveColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.UngroupColumnCommand;
import org.eclipse.nebula.widgets.nattable.group.event.GroupColumnsEvent;
import org.eclipse.nebula.widgets.nattable.group.event.UngroupColumnsEvent;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.rename.HeaderRenameDialog;
import org.eclipse.nebula.widgets.nattable.ui.rename.HeaderRenameDialog.RenameDialogLabels;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

/**
 * Command handler for handling {@link IColumnGroupCommand}s to create, remove
 * and rename column groups.
 *
 * @since 1.6
 */
public class ColumnGroupsCommandHandler extends AbstractLayerCommandHandler<IColumnGroupCommand> {

    private final ColumnGroupHeaderLayer contextLayer;
    private final SelectionLayer selectionLayer;

    public ColumnGroupsCommandHandler(ColumnGroupHeaderLayer contextLayer, SelectionLayer selectionLayer) {
        this.contextLayer = contextLayer;
        this.selectionLayer = selectionLayer;
    }

    @Override
    public boolean doCommand(IColumnGroupCommand command) {
        if (command instanceof CreateColumnGroupCommand) {
            CreateColumnGroupCommand createCommand = ((CreateColumnGroupCommand) command);
            if (!handleCreateColumnGroupCommand(createCommand.getColumnGroupName())) {
                MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(), SWT.INHERIT_DEFAULT | SWT.ICON_ERROR | SWT.OK);
                messageBox.setText(Messages.getString("ErrorDialog.title")); //$NON-NLS-1$
                messageBox.setMessage(Messages.getString("ColumnGroups.selectNonGroupedColumns")); //$NON-NLS-1$
                messageBox.open();
            }
            return true;
        } else if (command instanceof RemoveColumnGroupCommand) {
            RemoveColumnGroupCommand removeColumnGroupCommand = (RemoveColumnGroupCommand) command;
            int columnIndex = removeColumnGroupCommand.getColumnIndex();
            handleRemoveColumnGroupCommand(columnIndex);
            return true;
        } else if (command instanceof UngroupColumnCommand) {
            handleUngroupCommand();
            return true;
        } else if (command instanceof DisplayColumnGroupRenameDialogCommand) {
            return displayColumnGroupRenameDialog((DisplayColumnGroupRenameDialogCommand) command);
        }
        return false;
    }

    /**
     * Creates a new column group with the given name out of the currently fully
     * selected column positions. If a selected column is part of an existing
     * group, the existing group will be removed and all columns belonging to
     * that group will be also part of the new group.
     *
     * @param columnGroupName
     *            The name of the new column group.
     * @return <code>true</code> if the column group could be created,
     *         <code>false</code> if there are no columns fully selected.
     */
    protected boolean handleCreateColumnGroupCommand(String columnGroupName) {
        int[] fullySelectedColumns = this.selectionLayer.getFullySelectedColumnPositions();

        // we operate on the GroupModel directly to avoid the position
        // transformation
        GroupModel model = this.contextLayer.getGroupModel();

        MutableIntList positionsToGroup = IntLists.mutable.empty();
        if (fullySelectedColumns != null && fullySelectedColumns.length > 0) {
            for (int column : fullySelectedColumns) {
                // convert to position layer
                // needed because the group model takes the positions based on
                // the position layer
                int converted = LayerUtil.convertColumnPosition(this.selectionLayer, column, this.contextLayer.getPositionLayer());
                if (converted > -1) {
                    positionsToGroup.add(converted);
                }
            }

            HashSet<Group> existingGroups = new HashSet<Group>();
            for (MutableIntIterator it = positionsToGroup.intIterator(); it.hasNext();) {
                int column = it.next();
                Group group = model.getGroupByPosition(column);
                if (group != null) {
                    if (!group.isUnbreakable()) {
                        existingGroups.add(group);
                    } else {
                        // if a position of an unbreakable group was found, we
                        // ignore that position
                        it.remove();
                    }
                }
            }

            if (!existingGroups.isEmpty()) {
                // expand those groups
                this.contextLayer.doCommand(new ColumnGroupExpandCommand(model, existingGroups));
                // get all positions from the other groups
                for (Group group : existingGroups) {
                    positionsToGroup.addAll(group.getVisiblePositions());
                    // remove existing group and create a new one
                    this.contextLayer.removeGroup(group);
                }
            }

            // bring it to the correct order and remove duplicates
            positionsToGroup.sortThis();
            MutableIntList selectedPositions = IntLists.mutable.ofAll(positionsToGroup.distinct());

            if (selectedPositions.size() > 1) {
                // if a group is created for more than one column, reorder so
                // the positions are consecutive which is necessary for grouping
                this.selectionLayer.doCommand(
                        new MultiColumnReorderCommand(this.selectionLayer, selectedPositions.toArray(), selectedPositions.get(0)));
            }

            // create the column group
            this.contextLayer.addGroup(columnGroupName, this.selectionLayer.getColumnIndexByPosition(selectedPositions.get(0)), selectedPositions.size());

            this.selectionLayer.clear();

            this.contextLayer.fireLayerEvent(new GroupColumnsEvent(this.contextLayer));

            return true;
        }
        return false;
    }

    /**
     * Remove the column group at the given column index.
     *
     * @param columnIndex
     *            The column index to retrieve the column group to remove.
     */
    protected void handleRemoveColumnGroupCommand(int columnIndex) {
        int selectedPosition = this.selectionLayer.getColumnPositionByIndex(columnIndex);
        int converted = LayerUtil.convertColumnPosition(this.selectionLayer, selectedPosition, this.contextLayer.getPositionLayer());
        GroupModel model = this.contextLayer.getGroupModel();
        Group group = model.getGroupByPosition(converted);
        if (group != null && !group.isUnbreakable()) {
            if (group.isCollapsed()) {
                this.contextLayer.doCommand(new ColumnGroupExpandCommand(model, group));
            }
            model.removeGroup(group);

            this.contextLayer.fireLayerEvent(new GroupColumnsEvent(this.contextLayer));
        }
    }

    /**
     * Remove the currently fully selected columns from their corresponding
     * groups. Will also trigger a reorder to ensure a consistent group
     * rendering
     */
    protected void handleUngroupCommand() {
        // Grab fully selected column positions
        int[] fullySelectedColumns = this.selectionLayer.getFullySelectedColumnPositions();

        if (fullySelectedColumns != null && fullySelectedColumns.length > 0) {
            MutableIntList positionsToUngroup = IntLists.mutable.empty();
            for (int column : fullySelectedColumns) {
                // convert to position layer
                // needed because the group model takes the positions based on
                // the position layer
                int converted = LayerUtil.convertColumnPosition(this.selectionLayer, column, this.contextLayer.getPositionLayer());
                if (converted > -1) {
                    positionsToUngroup.add(converted);
                }
            }

            // we operate on the GroupModel directly to avoid the position
            // transformation
            GroupModel model = this.contextLayer.getGroupModel();
            HashMap<Group, MutableIntList> toRemove = new HashMap<>();
            positionsToUngroup.forEach(pos -> {
                Group group = model.getGroupByPosition(pos);
                if (group != null) {
                    int endPos = group.getVisibleStartPosition() + group.getVisibleSpan();
                    if (pos < endPos && !group.isGroupStart(pos)) {
                        // remember position to remove
                        MutableIntList remove = toRemove.get(group);
                        if (remove == null) {
                            remove = IntLists.mutable.empty();
                            toRemove.put(group, remove);
                        }
                        remove.add(pos);
                    } else {
                        model.removePositionsFromGroup(group, pos);
                    }
                }
            });

            if (!toRemove.isEmpty()) {
                toRemove.entrySet().forEach(entry -> {
                    Group group = entry.getKey();
                    int endPos = group.getVisibleStartPosition() + group.getVisibleSpan();

                    this.selectionLayer.doCommand(new MultiColumnReorderCommand(this.selectionLayer, entry.getValue().toArray(), endPos));

                    MutableIntList value = entry.getValue();
                    int start = endPos - value.size();
                    int[] positionsToRemove = new int[value.size()];
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        positionsToRemove[i] = start + i;
                    }

                    model.removePositionsFromGroup(group, positionsToRemove);
                });
            }

            this.selectionLayer.clear();

            this.contextLayer.fireLayerEvent(new UngroupColumnsEvent(this.contextLayer));
        }
    }

    // TODO NatTable 2.0 - Dialog should not be opened by the command handler
    protected boolean displayColumnGroupRenameDialog(DisplayColumnGroupRenameDialogCommand command) {
        int columnPosition = command.getColumnPosition();

        HeaderRenameDialog dialog = new HeaderRenameDialog(Display.getDefault().getActiveShell(), null, null, RenameDialogLabels.COLUMN_RENAME);
        Rectangle colHeaderBounds = this.contextLayer.getBoundsByPosition(columnPosition, 0);
        Point point = new Point(colHeaderBounds.x, colHeaderBounds.y + colHeaderBounds.height);
        dialog.setLocation(command.toDisplayCoordinates(point));
        dialog.open();

        if (!dialog.isCancelPressed()) {
            Group columnGroup = this.contextLayer.getGroupByPosition(columnPosition);
            columnGroup.setName(dialog.getNewLabel());
            this.contextLayer.fireLayerEvent(new VisualRefreshEvent(this.contextLayer));
        }

        return true;
    }

    @Override
    public Class<IColumnGroupCommand> getCommandClass() {
        return IColumnGroupCommand.class;
    }

}

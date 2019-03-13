/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.columnRename.ColumnRenameDialog;
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
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
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

        Set<Integer> positionsToGroup = new TreeSet<Integer>();
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

            Set<Group> existingGroups = new HashSet<Group>();
            for (Iterator<Integer> it = positionsToGroup.iterator(); it.hasNext();) {
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

            List<Integer> selectedPositions = new ArrayList<Integer>(positionsToGroup);

            // reorder so the positions are consecutive which is necessary for
            // grouping
            this.selectionLayer.doCommand(
                    new MultiColumnReorderCommand(this.selectionLayer, selectedPositions, selectedPositions.get(0)));

            // create the column group
            this.contextLayer.addGroup(columnGroupName, this.selectionLayer.getColumnIndexByPosition(selectedPositions.get(0)), positionsToGroup.size());

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
            Set<Integer> positionsToUngroup = new TreeSet<Integer>();
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
            Map<Group, List<Integer>> toRemove = new HashMap<Group, List<Integer>>();
            for (int pos : positionsToUngroup) {
                Group group = model.getGroupByPosition(pos);
                if (group != null) {
                    int endPos = group.getVisibleStartPosition() + group.getVisibleSpan();
                    if (pos < endPos && !group.isLeftEdge(pos)) {
                        // remember position to remove
                        List<Integer> remove = toRemove.get(group);
                        if (remove == null) {
                            remove = new ArrayList<Integer>();
                            toRemove.put(group, remove);
                        }
                        remove.add(pos);
                    } else {
                        model.removePositionsFromGroup(group, pos);
                    }
                }
            }

            if (!toRemove.isEmpty()) {
                for (Map.Entry<Group, List<Integer>> entry : toRemove.entrySet()) {
                    Group group = entry.getKey();
                    int endPos = group.getVisibleStartPosition() + group.getVisibleSpan();

                    this.selectionLayer.doCommand(new MultiColumnReorderCommand(this.selectionLayer, entry.getValue(), endPos));

                    List<Integer> value = entry.getValue();
                    int start = endPos - value.size();
                    int[] positionsToRemove = new int[value.size()];
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        positionsToRemove[i] = start + i;
                    }

                    model.removePositionsFromGroup(group, positionsToRemove);
                }
            }

            this.selectionLayer.clear();

            this.contextLayer.fireLayerEvent(new UngroupColumnsEvent(this.contextLayer));
        }
    }

    // TODO NatTable 2.0 - Dialog should not be opened by the command handler
    protected boolean displayColumnGroupRenameDialog(DisplayColumnGroupRenameDialogCommand command) {
        int columnPosition = command.getColumnPosition();

        ColumnRenameDialog dialog = new ColumnRenameDialog(Display.getDefault().getActiveShell(), null, null);
        Rectangle colHeaderBounds = this.contextLayer.getBoundsByPosition(columnPosition, 0);
        Point point = new Point(colHeaderBounds.x, colHeaderBounds.y + colHeaderBounds.height);
        dialog.setLocation(command.toDisplayCoordinates(point));
        dialog.open();

        if (!dialog.isCancelPressed()) {
            Group columnGroup = this.contextLayer.getGroupByPosition(columnPosition);
            columnGroup.setName(dialog.getNewColumnLabel());
        }

        return true;
    }

    @Override
    public Class<IColumnGroupCommand> getCommandClass() {
        return IColumnGroupCommand.class;
    }

}

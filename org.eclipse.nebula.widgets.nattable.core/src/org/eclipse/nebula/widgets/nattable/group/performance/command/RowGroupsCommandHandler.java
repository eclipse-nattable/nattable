/*******************************************************************************
 * Copyright (c) 2019, 2023 Dirk Fauth.
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
import org.eclipse.nebula.widgets.nattable.group.command.CreateRowGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.DisplayRowGroupRenameDialogCommand;
import org.eclipse.nebula.widgets.nattable.group.command.IRowGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.RemoveRowGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.UngroupRowCommand;
import org.eclipse.nebula.widgets.nattable.group.event.GroupRowsEvent;
import org.eclipse.nebula.widgets.nattable.group.event.UngroupRowsEvent;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiRowReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.rename.HeaderRenameDialog;
import org.eclipse.nebula.widgets.nattable.ui.rename.HeaderRenameDialog.RenameDialogLabels;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

/**
 * Command handler for handling {@link IRowGroupCommand}s to create, remove and
 * rename row groups.
 *
 * @since 1.6
 */
public class RowGroupsCommandHandler extends AbstractLayerCommandHandler<IRowGroupCommand> {

    private final RowGroupHeaderLayer contextLayer;
    private final SelectionLayer selectionLayer;

    private boolean clearSelection = true;

    /**
     *
     * @param contextLayer
     *            The {@link RowGroupHeaderLayer} this command handler is
     *            connected to.
     * @param selectionLayer
     *            The {@link SelectionLayer} needed to get the selection and
     *            perform reordering tasks if necessary.
     */
    public RowGroupsCommandHandler(RowGroupHeaderLayer contextLayer, SelectionLayer selectionLayer) {
        this(contextLayer, selectionLayer, true);
    }

    /**
     *
     * @param contextLayer
     *            The {@link RowGroupHeaderLayer} this command handler is
     *            connected to.
     * @param selectionLayer
     *            The {@link SelectionLayer} needed to get the selection and
     *            perform reordering tasks if necessary.
     * @param clearSelection
     *            <code>true</code> if the selection should be cleared after
     *            group/ungroup, <code>false</code> if the selection should stay
     *            unchanged.
     * @since 2.3
     */
    public RowGroupsCommandHandler(RowGroupHeaderLayer contextLayer, SelectionLayer selectionLayer, boolean clearSelection) {
        this.contextLayer = contextLayer;
        this.selectionLayer = selectionLayer;
        this.clearSelection = clearSelection;
    }

    @Override
    public boolean doCommand(IRowGroupCommand command) {
        if (command instanceof CreateRowGroupCommand) {
            CreateRowGroupCommand createCommand = ((CreateRowGroupCommand) command);
            if (!handleCreateRowGroupCommand(createCommand.getRowGroupName())) {
                MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(), SWT.INHERIT_DEFAULT | SWT.ICON_ERROR | SWT.OK);
                messageBox.setText(Messages.getString("ErrorDialog.title")); //$NON-NLS-1$
                messageBox.setMessage(Messages.getString("RowGroups.selectNonGroupedRows")); //$NON-NLS-1$
                messageBox.open();
            }
            return true;
        } else if (command instanceof RemoveRowGroupCommand) {
            RemoveRowGroupCommand removeRowGroupCommand = (RemoveRowGroupCommand) command;
            int rowIndex = removeRowGroupCommand.getRowIndex();
            handleRemoveRowGroupCommand(rowIndex);
            return true;
        } else if (command instanceof UngroupRowCommand) {
            handleUngroupCommand();
            return true;
        } else if (command instanceof DisplayRowGroupRenameDialogCommand) {
            return displayRowGroupRenameDialog((DisplayRowGroupRenameDialogCommand) command);
        }
        return false;
    }

    /**
     * Creates a new row group with the given name out of the currently fully
     * selected row positions. If a selected row is part of an existing group,
     * the existing group will be removed and all rows belonging to that group
     * will be also part of the new group.
     *
     * @param rowGroupName
     *            The name of the new row group.
     * @return <code>true</code> if the row group could be created,
     *         <code>false</code> if there are no rows fully selected.
     */
    protected boolean handleCreateRowGroupCommand(String rowGroupName) {

        // we operate on the GroupModel directly to avoid the position
        // transformation
        GroupModel model = this.contextLayer.getGroupModel();

        MutableIntList positionsToGroup = getPositionsToProcess();
        if (!positionsToGroup.isEmpty()) {
            HashSet<Group> existingGroups = new HashSet<>();
            for (MutableIntIterator it = positionsToGroup.intIterator(); it.hasNext();) {
                int row = it.next();
                Group group = model.getGroupByPosition(row);
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
                this.contextLayer.doCommand(new RowGroupExpandCommand(model, existingGroups));
                // get all positions from the other groups
                for (Group group : existingGroups) {
                    positionsToGroup.addAll(group.getVisiblePositions());
                    // remove existing group and create a new one
                    this.contextLayer.removeGroup(group);
                }
            }

            positionsToGroup.sortThis();
            MutableIntList selectedPositions = IntLists.mutable.ofAll(positionsToGroup.distinct());

            if (selectedPositions.size() > 1) {
                // if a group is created for more than one row, reorder so
                // the positions are consecutive which is necessary for grouping
                this.selectionLayer.doCommand(
                        new MultiRowReorderCommand(this.selectionLayer, selectedPositions.toArray(), selectedPositions.get(0)));
            }

            // create the row group
            this.contextLayer.addGroup(rowGroupName, this.selectionLayer.getRowIndexByPosition(selectedPositions.get(0)), selectedPositions.size());

            if (this.clearSelection) {
                this.selectionLayer.clear();
            }

            this.contextLayer.fireLayerEvent(new GroupRowsEvent(this.contextLayer));

            return true;
        }
        return false;
    }

    /**
     * Remove the row group at the given row index.
     *
     * @param rowIndex
     *            The row index to retrieve the row group to remove.
     */
    protected void handleRemoveRowGroupCommand(int rowIndex) {
        int selectedPosition = this.selectionLayer.getRowPositionByIndex(rowIndex);
        int converted = LayerUtil.convertRowPosition(this.selectionLayer, selectedPosition, this.contextLayer.getPositionLayer());
        GroupModel model = this.contextLayer.getGroupModel();
        Group group = model.getGroupByPosition(converted);
        if (group != null && !group.isUnbreakable()) {
            if (group.isCollapsed()) {
                this.contextLayer.doCommand(new RowGroupExpandCommand(model, group));
            }
            model.removeGroup(group);

            this.contextLayer.fireLayerEvent(new GroupRowsEvent(this.contextLayer));
        }
    }

    /**
     * Remove the currently fully selected rows from their corresponding groups.
     * Will also trigger a reorder to ensure a consistent group rendering
     */
    protected void handleUngroupCommand() {
        MutableIntList positionsToUngroup = getPositionsToProcess();
        if (!positionsToUngroup.isEmpty()) {

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

                    this.selectionLayer.doCommand(new MultiRowReorderCommand(this.selectionLayer, entry.getValue().toArray(), endPos));

                    MutableIntList value = entry.getValue();
                    int start = endPos - value.size();
                    int[] positionsToRemove = new int[value.size()];
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        positionsToRemove[i] = start + i;
                    }

                    model.removePositionsFromGroup(group, positionsToRemove);
                });
            }

            if (this.clearSelection) {
                this.selectionLayer.clear();
            }

            this.contextLayer.fireLayerEvent(new UngroupRowsEvent(this.contextLayer));
        }
    }

    /**
     * Return the collection of row positions related to the
     * {@link RowGroupHeaderLayer#getPositionLayer()} that should be added to a
     * row group.
     * <p>
     * The default implementation uses the {@link SelectionLayer} to retrieve
     * the fully selected row positions.
     *
     * @return The collection of row positions related to the
     *         {@link RowGroupHeaderLayer#getPositionLayer()} that should be
     *         added to a row group.
     * @since 2.3
     */
    protected MutableIntList getPositionsToProcess() {
        MutableIntList positionsToProcess = IntLists.mutable.empty();

        // Grab fully selected row positions
        int[] fullySelectedRows = this.selectionLayer.getFullySelectedRowPositions();
        if (fullySelectedRows != null && fullySelectedRows.length > 0) {
            for (int row : fullySelectedRows) {
                // convert to position layer
                // needed because the group model takes the positions based on
                // the position layer
                int converted = LayerUtil.convertRowPosition(this.selectionLayer, row, this.contextLayer.getPositionLayer());
                if (converted > -1) {
                    positionsToProcess.add(converted);
                }
            }
        }
        return positionsToProcess;
    }

    // TODO Dialog should not be opened by the command handler
    protected boolean displayRowGroupRenameDialog(DisplayRowGroupRenameDialogCommand command) {
        int rowPosition = command.getRowPosition();

        HeaderRenameDialog dialog = new HeaderRenameDialog(Display.getDefault().getActiveShell(), null, null, RenameDialogLabels.ROW_RENAME);
        Rectangle rowHeaderBounds = this.contextLayer.getBoundsByPosition(rowPosition, 0);
        Point point = new Point(rowHeaderBounds.x, rowHeaderBounds.y + rowHeaderBounds.height);
        dialog.setLocation(command.toDisplayCoordinates(point));
        dialog.open();

        if (!dialog.isCancelPressed()) {
            Group rowGroup = this.contextLayer.getGroupByPosition(rowPosition);
            rowGroup.setName(dialog.getNewLabel());
            this.contextLayer.fireLayerEvent(new VisualRefreshEvent(this.contextLayer));
        }

        return true;
    }

    @Override
    public Class<IRowGroupCommand> getCommandClass() {
        return IRowGroupCommand.class;
    }

}

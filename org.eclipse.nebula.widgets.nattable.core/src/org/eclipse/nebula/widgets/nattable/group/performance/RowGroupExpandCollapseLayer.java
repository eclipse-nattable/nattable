/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.group.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupCollapseCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupExpandCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.UpdateRowGroupCollapseCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.AbstractRowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

/**
 * Layer that is used in combination with the performance
 * {@link RowGroupHeaderLayer} to support expand/collapse of row groups.
 *
 * @since 1.6
 */
public class RowGroupExpandCollapseLayer extends AbstractRowHideShowLayer {

    private final MutableMap<Group, MutableIntSet> hidden = Maps.mutable.empty();

    public RowGroupExpandCollapseLayer(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer);
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof RowGroupExpandCommand) {
            List<Group> groups = ((RowGroupExpandCommand) command).getGroups();

            MutableIntSet shownIndexes = IntSets.mutable.empty();

            for (Group group : groups) {
                // if group is not collapseable return without any further
                // operation
                if (group == null || !group.isCollapseable()) {
                    continue;
                }

                if (group.isCollapsed()) {
                    group.setCollapsed(false);
                    MutableIntSet rowIndexes = this.hidden.get(group);
                    this.hidden.remove(group);
                    shownIndexes.addAll(rowIndexes);
                }
            }

            if (!shownIndexes.isEmpty()) {
                invalidateCache();
                fireLayerEvent(new ShowRowPositionsEvent(this, getRowPositionsByIndexes(shownIndexes.toArray())));
            } else {
                fireLayerEvent(new VisualRefreshEvent(this));
            }

            return true;
        } else if (command instanceof RowGroupCollapseCommand) {
            GroupModel groupModel = ((RowGroupCollapseCommand) command).getGroupModel();
            List<Group> groups = ((RowGroupCollapseCommand) command).getGroups();
            Collections.sort(groups, (Group o1, Group o2) -> (o2.getVisibleStartPosition() - o1.getVisibleStartPosition()));

            MutableIntSet hiddenPositions = IntSets.mutable.empty();
            MutableIntSet hiddenIndexes = IntSets.mutable.empty();

            for (Group group : groups) {
                // if group is not collapseable return without any further
                // operation
                if (group == null || !group.isCollapseable()) {
                    continue;
                }

                MutableIntSet rowIndexes = IntSets.mutable.empty();
                if (!group.isCollapsed()) {
                    rowIndexes.addAll(group.getVisibleIndexes());
                    group.setCollapsed(true);
                } else if (!this.hidden.containsKey(group)) {
                    for (int member : group.getMembers()) {
                        int pos = groupModel.getPositionByIndex(member);
                        if (pos > -1) {
                            rowIndexes.add(pos);
                        }
                    }
                }

                modifyForVisible(group, rowIndexes);
                this.hidden.put(group, rowIndexes);

                hiddenPositions.addAll(getRowPositionsByIndexes(rowIndexes.toArray()));
                hiddenIndexes.addAll(rowIndexes);
            }

            if (!hiddenPositions.isEmpty()) {
                invalidateCache();
                fireLayerEvent(new HideRowPositionsEvent(this, hiddenPositions.toArray(), hiddenIndexes.toArray()));
            } else {
                fireLayerEvent(new VisualRefreshEvent(this));
            }

            return true;
        } else if (command instanceof UpdateRowGroupCollapseCommand) {
            UpdateRowGroupCollapseCommand cmd = (UpdateRowGroupCollapseCommand) command;
            Group group = cmd.getGroup();
            MutableIntSet hiddenRowIndexes = this.hidden.get(group);
            if (group.getVisibleIndexes().length + hiddenRowIndexes.size() <= group.getOriginalSpan()) {
                MutableIntSet indexesToHide = IntSets.mutable.of(cmd.getIndexesToHide());
                MutableIntSet indexesToShow = IntSets.mutable.of(cmd.getIndexesToShow());

                // remove already hidden indexes
                indexesToHide.removeAll(hiddenRowIndexes);

                // remove static indexes
                modifyForVisible(group, indexesToHide);

                int[] hiddenPositions = getRowPositionsByIndexes(indexesToHide.toArray());

                hiddenRowIndexes.addAll(indexesToHide);
                hiddenRowIndexes.removeAll(indexesToShow);

                invalidateCache();

                fireLayerEvent(new HideRowPositionsEvent(this, hiddenPositions, indexesToHide.toArray()));
            }

            return true;
        }
        return super.doCommand(command);
    }

    /**
     * Ensure that a group is never hidden completely via collapse operations.
     * Removes either the configured static indexes of the group or the first
     * visible row in a group from the given collection of indexes for this.
     *
     * @param group
     *            The group to check.
     * @param rowIndexes
     *            The collection of indexes that should be hidden.
     */
    private void modifyForVisible(Group group, MutableIntSet rowIndexes) {
        int[] staticIndexes = group.getStaticIndexes();
        if (staticIndexes.length == 0) {
            // keep the first row
            rowIndexes.remove(group.getVisibleStartIndex());
        } else {
            // do not hide static indexes
            rowIndexes.removeAll(staticIndexes);
        }
    }

    @Override
    public boolean isRowIndexHidden(int rowIndex) {
        MutableIntSet found = this.hidden.detect(indexes -> indexes.contains(rowIndex));
        return found != null;
    }

    @Override
    public Collection<Integer> getHiddenRowIndexes() {
        MutableIntList hiddenRowIndexes = IntLists.mutable.empty();
        for (MutableIntSet indexes : this.hidden.values()) {
            hiddenRowIndexes.addAll(indexes);
        }
        return ArrayUtil.asIntegerList(hiddenRowIndexes.distinct().toSortedArray());
    }

    @Override
    public int[] getHiddenRowIndexesArray() {
        MutableIntList hiddenRowIndexes = IntLists.mutable.empty();
        for (MutableIntSet indexes : this.hidden.values()) {
            hiddenRowIndexes.addAll(indexes);
        }
        return hiddenRowIndexes.distinct().toSortedArray();
    }

    @Override
    public boolean hasHiddenRows() {
        return !this.hidden.isEmpty();
    }
}

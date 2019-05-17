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
package org.eclipse.nebula.widgets.nattable.group.performance;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupCollapseCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupExpandCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.UpdateColumnGroupCollapseCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.AbstractColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;

/**
 * Layer that is used in combination with the performance
 * {@link ColumnGroupHeaderLayer} to support expand/collapse of column groups.
 *
 * @since 1.6
 */
public class ColumnGroupExpandCollapseLayer extends AbstractColumnHideShowLayer {

    private final Map<Group, Collection<Integer>> hidden = new HashMap<Group, Collection<Integer>>();

    public ColumnGroupExpandCollapseLayer(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer);
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof ColumnGroupExpandCommand) {
            List<Group> groups = ((ColumnGroupExpandCommand) command).getGroups();

            Set<Integer> shownIndexes = new TreeSet<Integer>();

            for (Group group : groups) {
                // if group is not collapseable return without any further
                // operation
                if (group == null || !group.isCollapseable()) {
                    continue;
                }

                if (group.isCollapsed()) {
                    group.setCollapsed(false);
                    Collection<Integer> columnIndexes = this.hidden.get(group);
                    this.hidden.remove(group);
                    shownIndexes.addAll(columnIndexes);
                }
            }

            if (!shownIndexes.isEmpty()) {
                invalidateCache();
                fireLayerEvent(new ShowColumnPositionsEvent(this, getColumnPositionsByIndexes(shownIndexes)));
            } else {
                fireLayerEvent(new VisualRefreshEvent(this));
            }

            return true;
        } else if (command instanceof ColumnGroupCollapseCommand) {
            GroupModel groupModel = ((ColumnGroupCollapseCommand) command).getGroupModel();
            List<Group> groups = ((ColumnGroupCollapseCommand) command).getGroups();
            Collections.sort(groups, new Comparator<Group>() {

                @Override
                public int compare(Group o1, Group o2) {
                    return o2.getVisibleStartPosition() - o1.getVisibleStartPosition();
                }
            });

            Set<Integer> hiddenPositions = new TreeSet<Integer>();
            Set<Integer> hiddenIndexes = new TreeSet<Integer>();

            for (Group group : groups) {
                // if group is not collapseable return without any further
                // operation
                if (group == null || !group.isCollapseable()) {
                    continue;
                }

                Set<Integer> columnIndexes = new TreeSet<Integer>();
                if (!group.isCollapsed()) {
                    columnIndexes.addAll(group.getVisibleIndexes());
                    group.setCollapsed(true);
                } else if (!this.hidden.containsKey(group)) {
                    for (int member : group.getMembers()) {
                        int pos = groupModel.getPositionByIndex(member);
                        if (pos > -1) {
                            columnIndexes.add(pos);
                        }
                    }
                }

                modifyForVisible(group, columnIndexes);
                this.hidden.put(group, columnIndexes);

                hiddenPositions.addAll(getColumnPositionsByIndexes(columnIndexes));
                hiddenIndexes.addAll(columnIndexes);
            }

            if (!hiddenPositions.isEmpty()) {
                invalidateCache();
                fireLayerEvent(new HideColumnPositionsEvent(this, hiddenPositions, hiddenIndexes));
            } else {
                fireLayerEvent(new VisualRefreshEvent(this));
            }

            return true;
        } else if (command instanceof UpdateColumnGroupCollapseCommand) {
            UpdateColumnGroupCollapseCommand cmd = (UpdateColumnGroupCollapseCommand) command;
            Group group = cmd.getGroup();
            Collection<Integer> hiddenColumnIndexes = this.hidden.get(group);
            if (group.getVisibleIndexes().size() + hiddenColumnIndexes.size() <= group.getOriginalSpan()) {
                Collection<Integer> indexesToHide = cmd.getIndexesToHide();
                Collection<Integer> indexesToShow = cmd.getIndexesToShow();

                // remove already hidden indexes
                indexesToHide.removeAll(hiddenColumnIndexes);

                // remove static indexes
                modifyForVisible(group, indexesToHide);

                Collection<Integer> hiddenPositions = getColumnPositionsByIndexes(indexesToHide);

                hiddenColumnIndexes.addAll(indexesToHide);
                hiddenColumnIndexes.removeAll(indexesToShow);

                invalidateCache();

                fireLayerEvent(new HideColumnPositionsEvent(this, hiddenPositions, indexesToHide));
            }

            return true;
        }
        return super.doCommand(command);
    }

    /**
     * Ensure that a group is never hidden completely via collapse operations.
     * Removes either the configured static indexes of the group or the first
     * visible column in a group from the given collection of indexes for this.
     *
     * @param group
     *            The group to check.
     * @param columnIndexes
     *            The collection of indexes that should be hidden.
     */
    private void modifyForVisible(Group group, Collection<Integer> columnIndexes) {
        Collection<Integer> staticIndexes = group.getStaticIndexes();
        if (staticIndexes.isEmpty()) {
            // keep the first column
            columnIndexes.remove(group.getVisibleStartIndex());
        } else {
            // do not hide static indexes
            columnIndexes.removeAll(staticIndexes);
        }
    }

    @Override
    public boolean isColumnIndexHidden(int columnIndex) {
        for (Collection<Integer> indexes : this.hidden.values()) {
            if (indexes.contains(Integer.valueOf(columnIndex))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<Integer> getHiddenColumnIndexes() {
        Set<Integer> hiddenColumnIndexes = new TreeSet<Integer>();
        for (Collection<Integer> indexes : this.hidden.values()) {
            hiddenColumnIndexes.addAll(indexes);
        }
        return hiddenColumnIndexes;
    }

}

/*******************************************************************************
 * Copyright (c) 2012, 2013, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 460052
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;

/**
 * Tracks: Columns (by index) in a column Group. Does not keep the column
 * indexes in any defined order. Expand/collapse state of the Group. Name of the
 * Column Group (CG)
 */
public class ColumnGroupModel implements IPersistable {

    private static final String PERSISTENCE_KEY_COLUMN_GROUPS = ".columnGroups"; //$NON-NLS-1$

    /** Column group header name to column indexes */

    private final List<ColumnGroup> columnGroups = new LinkedList<ColumnGroup>();

    private final Collection<IColumnGroupModelListener> listeners = new HashSet<IColumnGroupModelListener>();

    public void registerColumnGroupModelListener(IColumnGroupModelListener listener) {
        this.listeners.add(listener);
    }

    public void unregisterColumnGroupModelListener(IColumnGroupModelListener listener) {
        this.listeners.remove(listener);
    }

    public void notifyListeners() {
        for (IColumnGroupModelListener listener : this.listeners) {
            listener.columnGroupModelChanged();
        }
    }

    @Override
    public void saveState(String prefix, Properties properties) {
        StringBuilder strBuilder = new StringBuilder();

        for (ColumnGroup columnGroup : this.columnGroups) {
            String columnGroupName = columnGroup.getName();

            // if this columnGroup has members, continue without saving state.
            // A group can haven no members if groups are used to organize
            // columns on a higher abstraction level ...
            if (columnGroup.members.size() == 0) {
                continue;
            }

            strBuilder.append(columnGroupName);
            strBuilder.append('=');

            strBuilder.append(columnGroup.collapsed ? "collapsed" : "expanded"); //$NON-NLS-1$ //$NON-NLS-2$
            strBuilder.append(':');

            strBuilder.append(columnGroup.collapseable ? "collapseable" : "uncollapseable"); //$NON-NLS-1$ //$NON-NLS-2$
            strBuilder.append(':');

            strBuilder.append(columnGroup.unbreakable ? "unbreakable" : "breakable"); //$NON-NLS-1$ //$NON-NLS-2$
            strBuilder.append(':');

            for (Integer member : columnGroup.members) {
                strBuilder.append(member);
                strBuilder.append(',');
            }

            if (!columnGroup.staticColumnIndexes.isEmpty()) {
                strBuilder.append(':');
                for (Integer member : columnGroup.staticColumnIndexes) {
                    strBuilder.append(member);
                    strBuilder.append(',');
                }
            }

            strBuilder.append('|');
        }

        properties.setProperty(prefix + PERSISTENCE_KEY_COLUMN_GROUPS, strBuilder.toString());
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        String property = properties.getProperty(prefix + PERSISTENCE_KEY_COLUMN_GROUPS);
        if (property != null) {
            clear();

            StringTokenizer columnGroupTokenizer = new StringTokenizer(property, "|"); //$NON-NLS-1$
            while (columnGroupTokenizer.hasMoreTokens()) {
                String columnGroupToken = columnGroupTokenizer.nextToken();

                int separatorIndex = columnGroupToken.indexOf('=');

                // Column group name
                String columnGroupName = columnGroupToken.substring(0, separatorIndex);
                ColumnGroup columnGroup = new ColumnGroup(columnGroupName);
                this.columnGroups.add(columnGroup);

                String[] columnGroupProperties = columnGroupToken.substring(separatorIndex + 1).split(":"); //$NON-NLS-1$

                // Expanded/collapsed
                String state = columnGroupProperties[0];
                if ("collapsed".equals(state)) { //$NON-NLS-1$
                    columnGroup.collapsed = true;
                } else if ("expanded".equals(state)) { //$NON-NLS-1$
                    columnGroup.collapsed = false;
                } else {
                    throw new IllegalArgumentException(state + " not one of 'expanded' or 'collapsed'"); //$NON-NLS-1$
                }

                // collapseble / uncollapseable
                state = columnGroupProperties[1];
                if ("collapseable".equals(state)) { //$NON-NLS-1$
                    columnGroup.collapseable = true;
                } else if ("uncollapseable".equals(state)) { //$NON-NLS-1$
                    columnGroup.collapseable = false;
                } else {
                    throw new IllegalArgumentException(state + " not one of 'uncollapseable' or 'collapseable'"); //$NON-NLS-1$
                }

                // breakable / unbreakable
                state = columnGroupProperties[2];
                if ("breakable".equals(state)) { //$NON-NLS-1$
                    columnGroup.unbreakable = false;
                } else if ("unbreakable".equals(state)) { //$NON-NLS-1$
                    columnGroup.unbreakable = true;
                } else {
                    throw new IllegalArgumentException(state + " not one of 'breakable' or 'unbreakable'"); //$NON-NLS-1$
                }

                // Indexes
                String indexes = columnGroupProperties[3];
                StringTokenizer indexTokenizer = new StringTokenizer(indexes, ","); //$NON-NLS-1$
                while (indexTokenizer.hasMoreTokens()) {
                    Integer index = Integer.valueOf(indexTokenizer.nextToken());
                    columnGroup.members.add(index);
                }

                if (columnGroupProperties.length == 5) {
                    String statics = columnGroupProperties[4];
                    StringTokenizer staticTokenizer = new StringTokenizer(statics, ","); //$NON-NLS-1$
                    while (staticTokenizer.hasMoreTokens()) {
                        Integer index = Integer.valueOf(staticTokenizer.nextToken());
                        columnGroup.staticColumnIndexes.add(index);
                    }
                }
            }
        }
    }

    /**
     * Creates the column group if one does not exist with the given name and
     * adds the column indexes to it.
     *
     * @see #insertColumnIndexes(String, int[])
     */
    public void addColumnsIndexesToGroup(String colGroupName, int... bodyColumnIndexs) {
        if (getColumnGroupByName(colGroupName) == null) {
            ColumnGroup group = new ColumnGroup(colGroupName);
            this.columnGroups.add(group);
        }
        insertColumnIndexes(colGroupName, bodyColumnIndexs);
        notifyListeners();
    }

    /**
     * This method will add column indexes to an existing group.
     *
     * @param colGroupName
     *            The name of the column group to which the column indexes
     *            should be added to
     * @param columnIndexesToInsert
     *            The column indexes to insert.
     * @return FALSE if: The column group is frozen Index is already s part of a
     *         column group
     */
    public boolean insertColumnIndexes(String colGroupName, int... columnIndexesToInsert) {
        LinkedList<Integer> members = new LinkedList<Integer>();

        ColumnGroup columnGroup = getColumnGroupByName(colGroupName);
        if (columnGroup.unbreakable) {
            return false;
        }

        // Check if any of the indexes belong to existing groups
        boolean memberAdded = false;
        for (int columnIndexToInsert : columnIndexesToInsert) {
            final Integer index = Integer.valueOf(columnIndexToInsert);
            ColumnGroup group = getColumnGroupByIndex(columnIndexToInsert);
            if (group != null && !group.getName().equals(colGroupName)) {
                return false;
            }
            else if (group != null && group.getName().equals(colGroupName)) {
                // column is already part of the group
                continue;
            }
            members.add(index);
        }

        if (!members.isEmpty()) {
            columnGroup.members.addAll(members);
            memberAdded = true;
            notifyListeners();
        }
        return memberAdded;
    }

    /**
     * This method will remove column indexes from an existing group.
     *
     * @param colGroupName
     *            The name of the column group from which the column indexes
     *            should be removed.
     * @param columnIndexesToRemove
     *            The column indexes to remove.
     * @return <code>true</code> if at least one column was removed from the
     *         column group.
     * @since 1.3
     */
    public boolean removeColumnIndexes(String colGroupName, int... columnIndexesToRemove) {
        ColumnGroup columnGroup = getColumnGroupByName(colGroupName);
        if (columnGroup.unbreakable) {
            return false;
        }

        // only remove members that belong to the given group
        boolean removed = false;
        for (int colIdx : columnIndexesToRemove) {
            if (columnGroup.members.contains(colIdx)) {
                if (columnGroup.removeColumn(colIdx) && !removed) {
                    removed = true;
                }
            }
        }

        notifyListeners();
        return removed;
    }

    /**
     * Add static columns identified by <code>staticColumnIndexes</code> to the
     * given columnGroup <code>colGroupName</code>. Static columns remains
     * visible when a column group is collapsed.
     *
     * @param colGroupName
     *            to add the indexes to
     * @param staticColumnIndexes
     */
    public void setStaticColumnIndexesByGroup(String colGroupName, int[] staticColumnIndexes) {
        if (getColumnGroupByName(colGroupName) == null) {
            ColumnGroup group = new ColumnGroup(colGroupName);
            this.columnGroups.add(group);
        }

        insertStaticColumnIndexes(colGroupName, staticColumnIndexes);

        notifyListeners();
    }

    /**
     * This method will add static column index(s) to an existing group
     *
     * @param colGroupName
     *            to add the indexes to
     * @param columnIndexesToInsert
     */
    public void insertStaticColumnIndexes(String colGroupName, int... columnIndexesToInsert) {

        LinkedList<Integer> staticColumnIndexes = new LinkedList<Integer>();
        ColumnGroup columnGroup = getColumnGroupByName(colGroupName);

        // Check if any of the indexes belong to existing groups
        for (int columnIndexToInsert : columnIndexesToInsert) {
            final Integer index = Integer.valueOf(columnIndexToInsert);
            staticColumnIndexes.add(index);
        }

        columnGroup.staticColumnIndexes.addAll(staticColumnIndexes);

        notifyListeners();
    }

    // Getters

    public ColumnGroup getColumnGroupByName(String groupName) {
        for (ColumnGroup columnGroup : this.columnGroups) {
            if (columnGroup.getName().equals(groupName)) {
                return columnGroup;
            }
        }

        return null;
    }

    public ColumnGroup getColumnGroupByIndex(int columnIndex) {
        for (ColumnGroup columnGroup : this.columnGroups) {
            if (columnGroup.getMembers().contains(Integer.valueOf(columnIndex))) {
                return columnGroup;
            }
        }

        return null;
    }

    public void addColumnGroup(ColumnGroup columnGroup) {
        this.columnGroups.add(columnGroup);
        notifyListeners();
    }

    public void removeColumnGroup(ColumnGroup columnGroup) {
        this.columnGroups.remove(columnGroup);
        notifyListeners();
    }

    public boolean isPartOfAGroup(int bodyColumnIndex) {
        for (ColumnGroup columnGroup : this.columnGroups) {
            if (columnGroup.members.contains(bodyColumnIndex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return TRUE if a group by this name exists
     */
    public boolean isAGroup(String cellValue) {
        for (ColumnGroup columnGroup : this.columnGroups) {
            if (columnGroup.getName().equals(cellValue)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        this.columnGroups.clear();
    }

    /**
     * @return Number of column Groups in the model.
     */
    public int size() {
        return this.columnGroups.size();
    }

    /**
     * @return TRUE if no column groups exist
     */
    public boolean isEmpty() {
        return this.columnGroups.size() == 0;
    }

    /**
     * @return all the indexes which belong to groups
     */
    public List<Integer> getAllIndexesInGroups() {
        List<Integer> indexes = new LinkedList<Integer>();
        for (ColumnGroup columnGroup : this.columnGroups) {
            indexes.addAll(columnGroup.members);
        }
        return indexes;
    }

    /**
     * @return TRUE if <code>bodyColumnIndex</code> is contained in the list of
     *         static columns of the column group this index belongs to
     */
    public boolean isStaticColumn(int bodyColumnIndex) {
        if (isPartOfAGroup(bodyColumnIndex)) {
            return getColumnGroupByIndex(bodyColumnIndex).staticColumnIndexes.contains(bodyColumnIndex);
        }
        return false;
    }

    /**
     * @return Total number of columns hidden for all the collapsed columns.
     */
    public int getCollapsedColumnCount() {
        int count = 0;

        for (ColumnGroup columnGroup : this.columnGroups) {
            if (columnGroup.collapsed) {
                int staticColumnIndexesCount = columnGroup.getStaticColumnIndexes().size();
                count = count + columnGroup.getMembers().size() - Math.max(staticColumnIndexesCount, 1);
            }
        }
        return count;
    }

    /**
     * @param bodyColumnIndex
     * @return The position of the index within the column group
     */
    public int getColumnGroupPositionFromIndex(int bodyColumnIndex) {
        if (isPartOfAGroup(bodyColumnIndex)) {
            ColumnGroup columnGroup = getColumnGroupByIndex(bodyColumnIndex);
            return columnGroup.members.indexOf(Integer.valueOf(bodyColumnIndex));
        }
        return -1;
    }

    /**
     * Check if the column at the specified column index belongs to a
     * {@link ColumnGroup} and if this {@link ColumnGroup} is collabseable.
     *
     * @param columnIndex
     *            The column index used to retrieve the corresponding column
     *            group
     * @return <code>true</code> if the column at the specified column index
     *         belongs to a {@link ColumnGroup} and this {@link ColumnGroup} is
     *         collabseable, <code>false</code> if not.
     */
    public boolean isPartOfACollapseableGroup(int columnIndex) {
        if (isPartOfAGroup(columnIndex)) {
            return getColumnGroupByIndex(columnIndex).isCollapseable();
        }
        return false;
    }

    /**
     * Set the {@link ColumnGroup} to which the column and the specified column
     * index belongs to, to be collapseable or not.
     *
     * @param columnIndex
     *            The column index used to retrieve the corresponding column
     *            group
     * @param collabseable
     *            <code>true</code> to set the column group collapseable,
     *            <code>false</code> to set it not to be collapseable.
     */
    public void setColumnGroupCollapseable(int columnIndex, boolean collabseable) {
        if (isPartOfAGroup(columnIndex)) {
            getColumnGroupByIndex(columnIndex).setCollapseable(collabseable);
        }
    }

    /**
     * Check if the column at the specified column index belongs to a
     * {@link ColumnGroup} and if this {@link ColumnGroup} is unbreakable.
     *
     * @param columnIndex
     *            The column index used to retrieve the corresponding column
     *            group
     * @return <code>true</code> if the column at the specified column index
     *         belongs to a {@link ColumnGroup} and this {@link ColumnGroup} is
     *         unbreakable, <code>false</code> if not.
     */
    public boolean isPartOfAnUnbreakableGroup(int columnIndex) {
        if (isPartOfAGroup(columnIndex)) {
            return getColumnGroupByIndex(columnIndex).isUnbreakable();
        }
        return false;
    }

    /**
     * Set the {@link ColumnGroup} to which the column and the specified column
     * index belongs to, to be unbreakable/breakable.
     *
     * @param columnIndex
     *            The column index used to retrieve the corresponding column
     *            group
     * @param unbreakable
     *            <code>true</code> to set the column group unbreakable,
     *            <code>false</code> to remove the unbreakable state.
     */
    public void setColumnGroupUnbreakable(int columnIndex, boolean unbreakable) {
        if (isPartOfAGroup(columnIndex)) {
            getColumnGroupByIndex(columnIndex).setUnbreakable(unbreakable);
        }
    }

    // *** Column Group ***

    public class ColumnGroup {

        /** Body column indexes */
        private final LinkedList<Integer> members = new LinkedList<Integer>();

        /** column indexes which remain visible when collapsing this group */
        private LinkedList<Integer> staticColumnIndexes = new LinkedList<Integer>();

        private String name;

        private boolean collapsed = false;
        private boolean collapseable = true;

        /**
         * If a group is marked as unbreakable, the composition of the group
         * cannot be changed. Columns cannot be added or removed from the group.
         * Columns may be reorder within the group.
         *
         * @see NTBL 393
         */
        private boolean unbreakable = false;

        ColumnGroup(String groupName) {
            this.name = groupName;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
            notifyListeners();
        }

        public boolean isCollapsed() {
            return this.collapsed;
        }

        public void setCollapsed(boolean collapsed) {
            this.collapsed = collapsed;
            notifyListeners();
        }

        public void toggleCollapsed() {
            setCollapsed(!this.collapsed);
        }

        public boolean isCollapseable() {
            return this.collapseable;
        }

        public void setCollapseable(boolean collapseable) {
            this.collapseable = collapseable;
            notifyListeners();
        }

        public boolean isUnbreakable() {
            return this.unbreakable;
        }

        public void setUnbreakable(boolean unbreakable) {
            this.unbreakable = unbreakable;
            notifyListeners();
        }

        public List<Integer> getMembers() {
            return this.members;
        }

        public List<Integer> getMembersSorted() {

            List<Integer> sortedMembers = new LinkedList<Integer>(this.members);
            Collections.sort(sortedMembers);

            return sortedMembers;
        }

        /**
         * @return the column indexes which remains visible when collapsing this
         *         group
         */
        public LinkedList<Integer> getStaticColumnIndexes() {
            return this.staticColumnIndexes;
        }

        public int getSize() {
            return this.members.size();
        }

        /**
         * @return TRUE if index successfully removed from its group.
         */
        public boolean removeColumn(int bodyColumnIndex) {
            if (this.members.contains(bodyColumnIndex) && !this.unbreakable) {
                this.members.remove(Integer.valueOf(bodyColumnIndex));
                if (this.members.size() == 0) {
                    ColumnGroupModel.this.columnGroups.remove(this);
                }
                notifyListeners();
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Column Group:\n\t name: " + this.name //$NON-NLS-1$
                    + "\n\t collapsed: " + this.collapsed //$NON-NLS-1$
                    + "\n\t unbreakable: " + this.unbreakable //$NON-NLS-1$
                    + "\n\t members: " + ObjectUtils.toString(this.members) + "\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "\n\t staticColumns: " + ObjectUtils.toString(this.staticColumnIndexes) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Column Group Model:\n"); //$NON-NLS-1$

        for (ColumnGroup columnGroup : this.columnGroups) {
            buffer.append(columnGroup);
        }
        return buffer.toString();
    }

}

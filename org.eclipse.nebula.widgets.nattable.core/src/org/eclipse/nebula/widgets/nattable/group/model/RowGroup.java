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
package org.eclipse.nebula.widgets.nattable.group.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link IRowGroup}.
 *
 * @author Stefan Bolton
 *
 * @param <T>
 */
public class RowGroup<T> implements IRowGroup<T> {

    private String groupName;
    private boolean collapsed;
    private boolean collapseable;

    // The model is needed to a) notify of any changes made (so it can notify
    // IRowGroupModelListeners) and b) it keeps a 'master-map' of all the row
    // position-to-row objects added into any of the row groups.
    private final RowGroupModel<T> rowGroupModel;

    // The rows and static rows in this group.
    private List<T> rowMembers;;
    private List<T> staticRowMembers;

    // A list of child groups.
    private List<IRowGroup<T>> childGroups;

    // Null for top-level groups.
    private IRowGroup<T> parentGroup;

    // Arbitrary data tagged onto a group.
    private Map<String, Object> data;

    private static final String DEFAULT_DATA_KEY = "defaultKey"; //$NON-NLS-1$

    public RowGroup(final RowGroupModel<T> rowGroupModel, final String groupName) {
        this.rowGroupModel = rowGroupModel;
        init(groupName, false);
        this.collapseable = true;
    }

    public RowGroup(final RowGroupModel<T> rowGroupModel,
            final String groupName, final boolean collapsed) {
        this.rowGroupModel = rowGroupModel;
        init(groupName, collapsed);
    }

    private void init(final String groupName, final boolean collapsed) {
        this.groupName = groupName;
        this.collapsed = collapsed;
        this.collapseable = true;
        this.rowMembers = Collections.synchronizedList(new ArrayList<T>());
        this.staticRowMembers = Collections
                .synchronizedList(new ArrayList<T>());
        this.childGroups = Collections
                .synchronizedList(new ArrayList<IRowGroup<T>>());
        this.data = new HashMap<String, Object>();
    }

    @Override
    public String getGroupName() {
        return this.groupName;
    }

    @Override
    public Object getData() {
        return this.data.get(DEFAULT_DATA_KEY);
    }

    @Override
    public Object getData(String key) {
        return this.data.get(key);
    }

    @Override
    public void setData(Object data) {
        this.data.put(DEFAULT_DATA_KEY, data);
    }

    @Override
    public void setData(String key, Object data) {
        this.data.put(key, data);
    }

    @Override
    public boolean isCollapsed() {
        return this.collapsed;
    }

    @Override
    public boolean isCollapseable() {
        return this.collapseable;
    }

    public void setCollapseable(boolean collapseable) {
        this.collapseable = collapseable;
    }

    @Override
    public void collapse() {
        if (isCollapseable() && !isCollapsed()) {
            this.collapsed = true;
            this.rowGroupModel.notifyListeners();
        }
    }

    @Override
    public void expand() {
        if (isCollapseable() && isCollapsed()) {
            this.collapsed = false;
            this.rowGroupModel.notifyListeners();
        }
    }

    private void addMemberRowAndCache(final List<T> rows, final T row) {
        rows.add(row);
        this.rowGroupModel.addMemberRow(row, this);
    }

    @Override
    public void addMemberRow(final T row) {
        addMemberRowAndCache(this.rowMembers, row);
        this.rowGroupModel.notifyListeners();
    }

    @Override
    public void addStaticMemberRow(final T row) {
        addMemberRowAndCache(this.staticRowMembers, row);
        this.rowGroupModel.notifyListeners();
    }

    @Override
    public void addMemberRows(final List<T> rows) {
        for (T row : rows) {
            addMemberRowAndCache(this.rowMembers, row);
        }
        this.rowGroupModel.notifyListeners();
    }

    private boolean removeMemberRowFromCache(final T row) {
        boolean removed = false;

        // Remove the row from our group.
        if (row != null) {
            removed = this.rowMembers.remove(row);

            // Try removing a static row instead.
            if (!removed) {
                removed = this.staticRowMembers.remove(row);
            }

            if (removed) {
                // Bump row positions to compensate.
                this.rowGroupModel.removeMemberRow(row);

                if ((getOwnMemberRows(false).size() == 0)
                        && (getRowGroups().size() == 0)) {
                    // If there are no more member rows, then clean-up any
                    // static rows and remove the group from the model.
                    for (T staticRow : this.getOwnStaticMemberRows()) {
                        this.rowGroupModel.removeMemberRow(staticRow);
                    }

                    this.staticRowMembers.clear();
                    this.rowGroupModel.removeRowGroup(this);

                    if (this.parentGroup != null) {
                        this.parentGroup.removeRowGroup(this);
                    }
                }

            } else {
                // Try sub-groups.
                synchronized (this.childGroups) {
                    for (final IRowGroup<T> rowGroup : this.childGroups) {
                        removed = ((RowGroup<T>) rowGroup).removeMemberRow(row);

                        if (removed) {
                            // Remove empty child groups from the model.
                            if (rowGroup.getOwnMemberRows(false).size() == 0) {
                                this.childGroups.remove(rowGroup);
                            }
                            break;
                        }
                    }
                }
            }
        }

        return removed;
    }

    @Override
    public boolean removeMemberRow(final T row) {

        boolean removed = removeMemberRowFromCache(row);

        if (removed) {
            this.rowGroupModel.notifyListeners();
        }

        return removed;
    }

    @Override
    public void removeMemberRows(final List<T> rows) {
        boolean removed = false;

        for (T row : rows) {
            removed |= removeMemberRowFromCache(row);
        }

        if (removed) {
            this.rowGroupModel.notifyListeners();
        }
    }

    @Override
    public IRowGroup<T> getParentGroup() {
        return this.parentGroup;
    }

    @Override
    public void setParentGroup(IRowGroup<T> parentGroup) {
        this.parentGroup = parentGroup;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.gresham.darwin.ui.widgets.grid.data.rowgroups.IRowGroup#addMemberRowGroup
     * (com.gresham.darwin.ui.widgets.grid.data.rowgroups.IRowGroup)
     */
    @Override
    public void addRowGroup(final IRowGroup<T> rowGroup) {
        rowGroup.setParentGroup(this);
        this.childGroups.add(rowGroup);
        this.rowGroupModel.notifyListeners();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.gresham.darwin.ui.widgets.grid.data.rowgroups.IRowGroup#
     * removeMemberRowGroup
     * (com.gresham.darwin.ui.widgets.grid.data.rowgroups.IRowGroup)
     */
    @Override
    public boolean removeRowGroup(final IRowGroup<T> rowGroup) {
        // Remove all members in the group.
        rowGroup.setParentGroup(null);
        rowGroup.clear();
        boolean removed = this.childGroups.remove(rowGroup);
        this.rowGroupModel.notifyListeners();
        return removed;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.gresham.darwin.ui.widgets.grid.data.rowgroups.IRowGroup#
     * getMemnerRowGroups()
     */
    @Override
    public List<IRowGroup<T>> getRowGroups() {
        return Collections.unmodifiableList(this.childGroups);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.gresham.darwin.ui.widgets.grid.data.rowgroups.IRowGroup#getOwnMemberRows
     * ()
     */
    @Override
    public List<T> getOwnMemberRows(final boolean includeStaticRows) {
        List<T> rows = new ArrayList<T>(this.rowMembers);

        if (includeStaticRows) {
            rows.addAll(this.staticRowMembers);
        }

        return Collections.unmodifiableList(rows);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.gresham.darwin.ui.widgets.grid.data.rowgroups.IRowGroup#
     * getOwnStaticMemberRows()
     */
    @Override
    public List<T> getOwnStaticMemberRows() {
        return Collections.unmodifiableList(this.staticRowMembers);
    }

    @Override
    public void clear() {
        synchronized (this.childGroups) {
            for (final IRowGroup<T> rowGroup : this.childGroups) {
                rowGroup.clear();
            }
        }

        synchronized (this.rowMembers) {
            for (T row : new ArrayList<T>(this.rowMembers)) {
                removeMemberRow(row);
            }
        }

        synchronized (this.staticRowMembers) {
            for (T row : new ArrayList<T>(this.staticRowMembers)) {
                removeMemberRow(row);
            }
        }
    }

    @Override
    public List<T> getMemberRows(final boolean includeStaticRows) {
        final List<T> memberRows = new ArrayList<T>();

        // Return all the member rows from nested groups.
        synchronized (this.childGroups) {
            for (final IRowGroup<T> rowGroup : this.childGroups) {
                memberRows.addAll(rowGroup.getMemberRows(includeStaticRows));
            }
        }

        // Add all of our immediate child rows.
        memberRows.addAll(getOwnMemberRows(includeStaticRows));

        return Collections.unmodifiableList(memberRows);
    }

    @Override
    public List<T> getStaticMemberRows() {
        final List<T> staticMemberRows = new ArrayList<T>();

        // Return all the member rows from nested groups.
        synchronized (this.childGroups) {
            for (final IRowGroup<T> rowGroup : this.childGroups) {
                staticMemberRows.addAll(rowGroup.getStaticMemberRows());
            }
        }

        // Add all of our immediate child rows.
        staticMemberRows.addAll(getOwnStaticMemberRows());

        return Collections.unmodifiableList(staticMemberRows);
    }

    @Override
    public IRowGroup<T> getRowGroupForRow(final T row) {
        IRowGroup<T> group = null;

        if (getOwnMemberRows(true).contains(row)) {
            group = this;

        } else {
            synchronized (this.childGroups) {
                for (final IRowGroup<T> rowGroup : this.childGroups) {
                    group = rowGroup.getRowGroupForRow(row);

                    if (group != null) {
                        break;
                    }
                }
            }
        }

        return group;
    }

    @Override
    public boolean isEmpty() {

        boolean empty = true;

        synchronized (this.childGroups) {
            for (final IRowGroup<T> rowGroup : this.childGroups) {
                empty &= rowGroup.isEmpty();
            }
        }

        return (empty && ((this.rowMembers.size() + this.staticRowMembers.size()) == 0));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Name      : %s\n", getGroupName())); //$NON-NLS-1$
        sb.append(String.format("Collapsed : %s\n", isCollapsed())); //$NON-NLS-1$
        sb.append("Members   : \n"); //$NON-NLS-1$

        for (T row : getOwnMemberRows(false)) {
            sb.append(String.format("%s", row.toString())); //$NON-NLS-1$
        }

        for (T row : getOwnStaticMemberRows()) {
            sb.append(String.format("*%s", row.toString())); //$NON-NLS-1$
        }

        if (this.childGroups.size() > 0) {
            sb.append(String.format("Start Child Groups for [%s] :- \n", getGroupName())); //$NON-NLS-1$
            for (final IRowGroup<T> rowGroup : this.childGroups) {
                sb.append(rowGroup.toString());
            }
            sb.append(String.format("End Child Groups for [%s]\n", getGroupName())); //$NON-NLS-1$
        }

        return sb.toString();
    }
}

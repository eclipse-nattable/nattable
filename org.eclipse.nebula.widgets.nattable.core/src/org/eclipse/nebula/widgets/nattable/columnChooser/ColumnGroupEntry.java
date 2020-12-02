/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.columnChooser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.columnChooser.gui.ColumnChooserDialog;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Object representation of a column group in the SWT tree.
 * <p>
 * <b>Note:</b> This is set as the SWT data on the {@link TreeItem}.
 * </p>
 *
 * @see ColumnChooserDialog
 */
public class ColumnGroupEntry {
    private String label;
    private Integer firstElementPosition;
    private Integer firstElementIndex;
    private boolean collapsed;

    private Group group;

    /**
     * Creates a {@link ColumnGroupEntry} for the old column grouping mechanism.
     *
     * @param label
     *            The label to be shown in the tree.
     * @param firstElementPosition
     *            The position of the first item in the group.
     * @param firstElementIndex
     *            The index of the first item in the group.
     * @param collapsed
     *            <code>true</code> if the group should be shown collapsed,
     *            <code>false</code> if not.
     */
    public ColumnGroupEntry(String label, Integer firstElementPosition, Integer firstElementIndex, boolean collapsed) {
        this.label = label;
        this.firstElementPosition = firstElementPosition;
        this.firstElementIndex = firstElementIndex;
        this.collapsed = collapsed;
    }

    /**
     * Creates a ColumnGroupEntry for the new performance column grouping, using
     * the Group object as underlying data reference.
     *
     * @param group
     *            The {@link Group} that should be wrapped.
     * @since 1.6
     */
    public ColumnGroupEntry(Group group) {
        this.group = group;
    }

    public String getLabel() {
        if (this.group != null) {
            return this.group.getName();
        }
        return this.label;
    }

    public Integer getFirstElementPosition() {
        if (this.group != null) {
            return this.group.getVisibleStartPosition();
        }
        return this.firstElementPosition;
    }

    public Integer getFirstElementIndex() {
        if (this.group != null) {
            return this.group.getVisibleStartIndex();
        }
        return this.firstElementIndex;
    }

    public boolean isCollapsed() {
        if (this.group != null) {
            return this.group.isCollapsed();
        }
        return this.collapsed;
    }

    /**
     * @return The {@link Group} carried by this {@link ColumnGroupEntry} or
     *         <code>null</code> if it is configured for the old column grouping
     *         mechanism.
     * @since 1.6
     */
    public Group getGroup() {
        return this.group;
    }

    public static List<Integer> getColumnGroupEntryPositions(List<ColumnGroupEntry> columnEntries) {
        List<Integer> columnGroupEntryPositions = new ArrayList<>();
        for (ColumnGroupEntry columnGroupEntry : columnEntries) {
            columnGroupEntryPositions.add(columnGroupEntry.getFirstElementPosition());
        }
        return columnGroupEntryPositions;
    }

    @Override
    public String toString() {
        return "ColumnGroupEntry (" + //$NON-NLS-1$
                "Label: " + getLabel() + //$NON-NLS-1$
                ", firstElementPosition: " + getFirstElementPosition() + //$NON-NLS-1$
                ", firstElementIndex: " + getFirstElementIndex() + //$NON-NLS-1$
                ", collapsed: " + isCollapsed() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ColumnGroupEntry other = (ColumnGroupEntry) obj;
        if (this.group == null) {
            if (this.firstElementIndex == null) {
                if (other.firstElementIndex != null)
                    return false;
            } else if (!this.firstElementIndex.equals(other.firstElementIndex))
                return false;
            if (this.firstElementPosition == null) {
                if (other.firstElementPosition != null)
                    return false;
            } else if (!this.firstElementPosition.equals(other.firstElementPosition))
                return false;
            if (this.collapsed != other.collapsed)
                return false;
            if (this.label == null) {
                if (other.label != null)
                    return false;
            } else if (!this.label.equals(other.label))
                return false;
        } else {
            return this.group.equals(other.group);
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (this.group == null) {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.firstElementIndex == null) ? 0 : this.firstElementIndex.hashCode());
            result = prime * result + ((this.firstElementPosition == null) ? 0 : this.firstElementPosition.hashCode());
            result = prime * result + (this.collapsed ? 1231 : 1237);
            result = prime * result + ((this.label == null) ? 0 : this.label.hashCode());
            return result;
        } else {
            return this.group.hashCode();
        }
    }
}

/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.columnChooser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.columnChooser.gui.ColumnChooserDialog;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Object representation of a ColumnGroup in the SWT tree. NOTE: this is set as
 * the SWT data on the {@link TreeItem}.
 *
 * @see ColumnChooserDialog#populateModel
 */
public class ColumnGroupEntry {
    private final String label;
    private final Integer firstElementPosition;
    private final Integer firstElementIndex;
    private final boolean isCollapsed;

    public ColumnGroupEntry(String label, Integer firstElementPosition,
            Integer firstElementIndex, boolean isCollapsed) {
        super();
        this.label = label;
        this.firstElementPosition = firstElementPosition;
        this.firstElementIndex = firstElementIndex;
        this.isCollapsed = isCollapsed;
    }

    public String getLabel() {
        return this.label;
    }

    public Integer getFirstElementPosition() {
        return this.firstElementPosition;
    }

    public Integer getFirstElementIndex() {
        return this.firstElementIndex;
    }

    public boolean isCollapsed() {
        return this.isCollapsed;
    }

    public static List<Integer> getColumnGroupEntryPositions(
            List<ColumnGroupEntry> columnEntries) {
        List<Integer> columnGroupEntryPositions = new ArrayList<Integer>();
        for (ColumnGroupEntry ColumnGroupEntry : columnEntries) {
            columnGroupEntryPositions.add(ColumnGroupEntry
                    .getFirstElementPosition());
        }
        return columnGroupEntryPositions;
    }

    @Override
    public String toString() {
        return "ColumnGroupEntry (" + //$NON-NLS-1$
                "Label: " + this.label + //$NON-NLS-1$
                ", firstElementPosition: " + this.firstElementPosition + //$NON-NLS-1$
                ", firstElementIndex: " + this.firstElementIndex + //$NON-NLS-1$
                ", collapsed: " + this.isCollapsed + ")"; //$NON-NLS-1$ //$NON-NLS-2$
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
        if (this.isCollapsed != other.isCollapsed)
            return false;
        if (this.label == null) {
            if (other.label != null)
                return false;
        } else if (!this.label.equals(other.label))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.firstElementIndex == null) ? 0 : this.firstElementIndex.hashCode());
        result = prime * result + ((this.firstElementPosition == null) ? 0 : this.firstElementPosition.hashCode());
        result = prime * result + (this.isCollapsed ? 1231 : 1237);
        result = prime * result + ((this.label == null) ? 0 : this.label.hashCode());
        return result;
    }
}

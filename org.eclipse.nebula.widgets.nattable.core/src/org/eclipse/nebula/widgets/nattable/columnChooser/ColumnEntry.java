/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.columnChooser;

import org.eclipse.nebula.widgets.nattable.Messages;

/**
 * Object representation of a NatTable Column. This is used in the Column
 * chooser dialogs as a mechanism of preserving meta data on the columns in the
 * dialog.
 *
 * @see ColumnChooserUtils
 */
public class ColumnEntry {

    private final String label;
    private final int index;
    private int position;

    /**
     *
     * @param label
     *            The label of the column.
     * @param index
     *            The index of the column.
     * @param position
     *            The position of the column.
     *
     * @since 2.0
     */
    public ColumnEntry(String label, int index, int position) {
        this.label = label;
        this.index = index;
        this.position = position;
    }

    /**
     *
     * @return The position of the column.
     *
     * @since 2.0
     */
    public int getPosition() {
        return this.position;
    }

    /**
     *
     * @param position
     *            The new position of the column.
     *
     * @since 2.0
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     *
     * @return The index of the column.
     *
     * @since 2.0
     */
    public int getIndex() {
        return this.index;
    }

    /**
     *
     * @return The label of the column.
     */
    public String getLabel() {
        return toString();
    }

    @Override
    public String toString() {
        return this.label != null ? this.label : Messages.getString("ColumnEntry.0"); //$NON-NLS-1$
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ColumnEntry other = (ColumnEntry) obj;
        if (this.index != other.index)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.index;
        return result;
    }
}

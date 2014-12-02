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
    private final Integer index;
    private Integer position;

    public ColumnEntry(String label, Integer index, Integer position) {
        this.label = label;
        this.index = index;
        this.position = position;
    }

    @Override
    public String toString() {
        return this.label != null ? this.label : Messages.getString("ColumnEntry.0"); //$NON-NLS-1$
    }

    public Integer getPosition() {
        return this.position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getIndex() {
        return this.index;
    }

    public String getLabel() {
        return toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ColumnEntry) {
            ColumnEntry that = (ColumnEntry) obj;
            return this.index.intValue() == that.index.intValue();
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.index.hashCode();
    }
}

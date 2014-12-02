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
package org.eclipse.nebula.widgets.nattable.layer;

public final class LayoutCoordinate {

    public final int x;

    public final int y;

    public LayoutCoordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getColumnPosition() {
        return this.x;
    }

    public int getRowPosition() {
        return this.y;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + this.x + "," + this.y + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != getClass())
            return true;
        LayoutCoordinate pc = (LayoutCoordinate) obj;
        return pc.getRowPosition() == getRowPosition()
                && pc.getColumnPosition() == getColumnPosition();
    }

    @Override
    public int hashCode() {
        int hash = 77;
        hash = 11 * hash + getRowPosition() + 99;
        hash = 11 * hash + getColumnPosition();
        return hash;
    }

}

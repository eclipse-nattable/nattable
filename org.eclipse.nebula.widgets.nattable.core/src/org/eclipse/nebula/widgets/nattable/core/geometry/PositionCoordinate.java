/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth, Edwin Park.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com>   - initial API and implementation
 *     Edwin Park <esp1@cornell.edu>            - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.core.geometry;

import java.math.BigInteger;

/**
 * Instances of this class represent places on the (x, y) coordinate plane. It
 * is used to identify a layer coordinate (column/row) in NatTable.
 * <p>
 * Note that the x and y values are final.
 * </p>
 */
public class PositionCoordinate {

    /**
     * the x coordinate of the point
     */
    public final BigInteger x;

    /**
     * the y coordinate of the point
     */
    public final BigInteger y;

    /**
     * Constructs a new position coordinate with the given x and y coordinates.
     *
     * @param x
     *            the x coordinate of the new point
     * @param y
     *            the y coordinate of the new point
     */
    public PositionCoordinate(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.x == null) ? 0 : this.x.hashCode());
        result = prime * result + ((this.y == null) ? 0 : this.y.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PositionCoordinate other = (PositionCoordinate) obj;
        if (this.x == null) {
            if (other.x != null)
                return false;
        } else if (!this.x.equals(other.x))
            return false;
        if (this.y == null) {
            if (other.y != null)
                return false;
        } else if (!this.y.equals(other.y))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PositionCoordinate {" + this.x + ", " + this.y + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}

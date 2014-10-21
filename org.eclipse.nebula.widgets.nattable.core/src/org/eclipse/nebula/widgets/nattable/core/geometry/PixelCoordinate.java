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

/**
 * Instances of this class represent places on the (x, y) coordinate plane. It
 * is used to identify a pixel coordinate for drawing operations via graphics
 * context. The values for x and y are stored as double values because newer UI
 * toolkits use doubles to identify coordinates to support scaling.
 * <p>
 * Note that the x and y values are final.
 * </p>
 * <p>
 * There might be an implementation for this in every UI toolkit, like
 * org.eclipse.swt.graphics.Point or java.awt.Point, but they are dependent to
 * the UI toolkit because of their package and inheritance. That's why we
 * re-implemented it again to make use of it in NatTable core without any
 * further dependencies.
 * </p>
 */
public class PixelCoordinate {

    /**
     * the x coordinate of the point
     */
    public final double x;

    /**
     * the y coordinate of the point
     */
    public final double y;

    /**
     * Constructs a new pixel coordinate with the given x and y coordinates.
     *
     * @param x
     *            the x coordinate of the new point
     * @param y
     *            the y coordinate of the new point
     */
    public PixelCoordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(this.x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        PixelCoordinate other = (PixelCoordinate) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PixelCoordinate {" + this.x + ", " + this.y + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}

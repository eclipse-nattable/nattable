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
 * Instances of this class represent rectangular areas in an (x, y) coordinate
 * system. The top left corner of the rectangle is specified by its x and y
 * values, and the extent of the rectangle is specified by its width and height.
 * The values are stored as double values because newer UI toolkits use doubles
 * to identify coordinates to support scaling.
 * <p>
 * There might be an implementation for this in every UI toolkit, like
 * org.eclipse.swt.graphics.Rectangle or java.awt.Rectangle, but they are
 * dependent to the UI toolkit because of their package and inheritance. That's
 * why we re-implemented it again to make use of it in NatTable core without any
 * further dependencies.
 * </p>
 */
public class PixelRectangle {

    /**
     * the x coordinate of the rectangle
     */
    public double x;

    /**
     * the y coordinate of the rectangle
     */
    public double y;

    /**
     * the width of the rectangle
     */
    public double width;

    /**
     * the height of the rectangle
     */
    public double height;

    /**
     * Construct a new instance of this class given the x, y, width and height
     * values.
     *
     * @param x
     *            the x coordinate of the origin of the rectangle
     * @param y
     *            the y coordinate of the origin of the rectangle
     * @param width
     *            the width of the rectangle
     * @param height
     *            the height of the rectangle
     */
    public PixelRectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Destructively replaces the x, y, width and height values in the receiver
     * with ones which represent the union of the rectangles specified by the
     * receiver and the given rectangle.
     * <p>
     * The union of two rectangles is the smallest single rectangle that
     * completely covers both of the areas covered by the two given rectangles.
     * </p>
     *
     * @param rect
     *            the rectangle to merge with the receiver
     *
     * @throws IllegalArgumentException
     *             if the specified rectangle is <code>null</code>
     */
    public void add(PixelRectangle rect) {
        if (rect == null)
            throw new IllegalArgumentException("rectangle can not be null"); //$NON-NLS-1$
        double left = this.x < rect.x ? this.x : rect.x;
        double top = this.y < rect.y ? this.y : rect.y;
        double lhs = this.x + this.width;
        double rhs = rect.x + rect.width;
        double right = lhs > rhs ? lhs : rhs;
        lhs = this.y + this.height;
        rhs = rect.y + rect.height;
        double bottom = lhs > rhs ? lhs : rhs;
        this.x = left;
        this.y = top;
        this.width = right - left;
        this.height = bottom - top;
    }

    /**
     * Returns <code>true</code> if the point specified by the arguments is
     * inside the area specified by the receiver, and <code>false</code>
     * otherwise.
     *
     * @param x
     *            the x coordinate of the point to test for containment
     * @param y
     *            the y coordinate of the point to test for containment
     * @return <code>true</code> if the rectangle contains the point and
     *         <code>false</code> otherwise
     */
    public boolean contains(double x, double y) {
        return (x >= this.x) && (y >= this.y) && x < (this.x + this.width) && y < (this.y + this.height);
    }

    /**
     * Returns <code>true</code> if the given pixel coordinate is inside the
     * area specified by the receiver, and <code>false</code> otherwise.
     *
     * @param pt
     *            the point to test for containment
     * @return <code>true</code> if the rectangle contains the point and
     *         <code>false</code> otherwise
     *
     * @throws IllegalArgumentException
     *             if the specified point is <code>null</code>
     */
    public boolean contains(PixelCoordinate pt) {
        if (pt == null)
            throw new IllegalArgumentException("pixel coordinate can not be null"); //$NON-NLS-1$
        return contains(pt.x, pt.y);
    }

    /**
     * Returns <code>true</code> if the receiver does not cover any area in the
     * (x, y) coordinate plane, and <code>false</code> if the receiver does
     * cover some area in the plane.
     * <p>
     * A rectangle is considered to <em>cover area</em> in the (x, y) coordinate
     * plane if both its width and height are non-zero.
     * </p>
     *
     * @return <code>true</code> if the receiver is empty, and
     *         <code>false</code> otherwise
     */
    public boolean isEmpty() {
        return (this.width <= 0) || (this.height <= 0);
    }

    /**
     * Destructively replaces the x, y, width and height values in the receiver
     * with ones which represent the intersection of the rectangles specified by
     * the receiver and the given rectangle.
     *
     * @param rect
     *            the rectangle to intersect with the receiver
     *
     * @throws IllegalArgumentException
     *             if the specified rectangle is <code>null</code>
     */
    public void intersect(PixelRectangle rect) {
        if (rect == null)
            throw new IllegalArgumentException("rectangle can not be null"); //$NON-NLS-1$
        if (this == rect)
            return;
        double left = this.x > rect.x ? this.x : rect.x;
        double top = this.y > rect.y ? this.y : rect.y;
        double lhs = this.x + this.width;
        double rhs = rect.x + rect.width;
        double right = lhs < rhs ? lhs : rhs;
        lhs = this.y + this.height;
        rhs = rect.y + rect.height;
        double bottom = lhs < rhs ? lhs : rhs;
        this.x = right < left ? 0 : left;
        this.y = bottom < top ? 0 : top;
        this.width = right < left ? 0 : right - left;
        this.height = bottom < top ? 0 : bottom - top;
    }

    /**
     * Returns a new rectangle which represents the intersection of the receiver
     * and the given rectangle.
     * <p>
     * The intersection of two rectangles is the rectangle that covers the area
     * which is contained within both rectangles.
     * </p>
     *
     * @param rect
     *            the rectangle to intersect with the receiver
     * @return the intersection of the receiver and the argument
     *
     * @throws IllegalArgumentException
     *             if the specified rectangle is <code>null</code>
     */
    public PixelRectangle intersection(PixelRectangle rect) {
        if (rect == null)
            throw new IllegalArgumentException("rectangle can not be null"); //$NON-NLS-1$
        if (this == rect)
            return new PixelRectangle(this.x, this.y, this.width, this.height);
        double left = this.x > rect.x ? this.x : rect.x;
        double top = this.y > rect.y ? this.y : rect.y;
        double lhs = this.x + this.width;
        double rhs = rect.x + rect.width;
        double right = lhs < rhs ? lhs : rhs;
        lhs = this.y + this.height;
        rhs = rect.y + rect.height;
        double bottom = lhs < rhs ? lhs : rhs;
        return new PixelRectangle(
                right < left ? 0 : left,
                        bottom < top ? 0 : top,
                                right < left ? 0 : right - left,
                                        bottom < top ? 0 : bottom - top);
    }

    /**
     * Returns <code>true</code> if the rectangle described by the arguments
     * intersects with the receiver and <code>false</code> otherwise.
     * <p>
     * Two rectangles intersect if the area of the rectangle representing their
     * intersection is not empty.
     * </p>
     *
     * @param x
     *            the x coordinate of the origin of the rectangle
     * @param y
     *            the y coordinate of the origin of the rectangle
     * @param width
     *            the width of the rectangle
     * @param height
     *            the height of the rectangle
     * @return <code>true</code> if the rectangle intersects with the receiver,
     *         and <code>false</code> otherwise
     *
     * @see #intersection(PixelRectangle)
     * @see #isEmpty()
     */
    public boolean intersects(double x, double y, double width, double height) {
        return (x < this.x + this.width) && (y < this.y + this.height) &&
                (x + width > this.x) && (y + height > this.y);
    }

    /**
     * Returns <code>true</code> if the given rectangle intersects with the
     * receiver and <code>false</code> otherwise.
     * <p>
     * Two rectangles intersect if the area of the rectangle representing their
     * intersection is not empty.
     * </p>
     *
     * @param rect
     *            the rectangle to test for intersection
     * @return <code>true</code> if the rectangle intersects with the receiver,
     *         and <code>false</code> otherwise
     *
     * @throws IllegalArgumentException
     *             if the specified rectangle is <code>null</code>
     *
     * @see #intersection(PixelRectangle)
     * @see #isEmpty()
     */
    public boolean intersects(PixelRectangle rect) {
        if (rect == null)
            throw new IllegalArgumentException("rectangle can not be null"); //$NON-NLS-1$
        return rect == this || intersects(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Returns a new rectangle which represents the union of the receiver and
     * the given rectangle.
     * <p>
     * The union of two rectangles is the smallest single rectangle that
     * completely covers both of the areas covered by the two given rectangles.
     * </p>
     *
     * @param rect
     *            the rectangle to perform union with
     * @return the union of the receiver and the argument
     *
     * @throws IllegalArgumentException
     *             if the specified rectangle is <code>null</code>
     *
     * @see #add(PixelRectangle)
     */
    public PixelRectangle union(PixelRectangle rect) {
        if (rect == null)
            throw new IllegalArgumentException("rectangle can not be null"); //$NON-NLS-1$
        double left = this.x < rect.x ? this.x : rect.x;
        double top = this.y < rect.y ? this.y : rect.y;
        double lhs = this.x + this.width;
        double rhs = rect.x + rect.width;
        double right = lhs > rhs ? lhs : rhs;
        lhs = this.y + this.height;
        rhs = rect.y + rect.height;
        double bottom = lhs > rhs ? lhs : rhs;
        return new PixelRectangle(left, top, right - left, bottom - top);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(this.height);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.width);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        PixelRectangle other = (PixelRectangle) obj;
        if (Double.doubleToLongBits(this.height) != Double.doubleToLongBits(other.height))
            return false;
        if (Double.doubleToLongBits(this.width) != Double.doubleToLongBits(other.width))
            return false;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PixelRectangle {" + this.x + ", " + this.y + ", " + this.width + ", " + this.height + "}"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

}

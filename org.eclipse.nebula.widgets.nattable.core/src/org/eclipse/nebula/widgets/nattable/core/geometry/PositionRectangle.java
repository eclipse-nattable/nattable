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
 * Instances of this class represent rectangular areas in an (x, y) coordinate
 * system. The top left corner of the rectangle is specified by its x and y
 * values, and the extent of the rectangle is specified by its width and height.
 * The values are stored as BigInteger values because newer UI toolkits use
 * BigIntegers to identify coordinates to support scaling.
 * <p>
 * There might be an implementation for this in every UI toolkit, like
 * org.eclipse.swt.graphics.Rectangle or java.awt.Rectangle, but they are
 * dependent to the UI toolkit because of their package and inheritance. That's
 * why we re-implemented it again to make use of it in NatTable core without any
 * further dependencies.
 * </p>
 */
public class PositionRectangle {

    private static final BigInteger NONE = BigInteger.valueOf(-1);

    /**
     * the x coordinate of the rectangle
     */
    public BigInteger x;

    /**
     * the y coordinate of the rectangle
     */
    public BigInteger y;

    /**
     * the width of the rectangle
     */
    public BigInteger width;

    /**
     * the height of the rectangle
     */
    public BigInteger height;

    /**
     * Construct a new instance of this class given the x, y, width and height
     * values.
     * <p>
     * This constructor is used for backwards compatibility because the layers
     * are not yet updated for huge data sets and still use <code>int</code> to
     * identify coordinates.
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
     */
    public PositionRectangle(Integer x, Integer y, Integer width, Integer height) {
        this.x = x != null ? BigInteger.valueOf(x) : NONE;
        this.y = y != null ? BigInteger.valueOf(y) : NONE;
        this.width = width != null ? BigInteger.valueOf(width) : NONE;
        this.height = height != null ? BigInteger.valueOf(height) : NONE;
    }

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
    public PositionRectangle(BigInteger x, BigInteger y, BigInteger width, BigInteger height) {
        this.x = x != null ? x : NONE;
        this.y = y != null ? y : NONE;
        this.width = width != null ? width : NONE;
        this.height = height != null ? height : NONE;
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
    public void add(PositionRectangle rect) {
        if (rect == null)
            throw new IllegalArgumentException("rectangle can not be null"); //$NON-NLS-1$
        BigInteger left = this.x.min(rect.x);
        BigInteger top = this.y.min(rect.y);
        BigInteger lhs = this.x.add(this.width);
        BigInteger rhs = rect.x.add(rect.width);
        BigInteger right = lhs.max(rhs);
        lhs = this.y.add(this.height);
        rhs = rect.y.add(rect.height);
        BigInteger bottom = lhs.max(rhs);
        this.x = left;
        this.y = top;
        this.width = right.subtract(left);
        this.height = bottom.subtract(top);
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
     *
     * @throws IllegalArgumentException
     *             if on of the specified coordinates is <code>null</code>
     */
    public boolean contains(BigInteger x, BigInteger y) {
        if (x == null || y == null)
            throw new IllegalArgumentException("coordinates can not be null"); //$NON-NLS-1$
        return (x.compareTo(this.x) >= 0)
                && (y.compareTo(this.y) >= 0)
                && (x.compareTo(this.x.add(this.width)) < 0)
                && (y.compareTo(this.y.add(this.height)) < 0);
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
    public boolean contains(PositionCoordinate pt) {
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
        return (this.width.compareTo(BigInteger.ZERO) <= 0)
                || (this.height.compareTo(BigInteger.ZERO) <= 0);
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
    public void intersect(PositionRectangle rect) {
        if (rect == null)
            throw new IllegalArgumentException("rectangle can not be null"); //$NON-NLS-1$
        if (this == rect)
            return;
        BigInteger left = this.x.max(rect.x);
        BigInteger top = this.y.max(rect.y);
        BigInteger lhs = this.x.add(this.width);
        BigInteger rhs = rect.x.add(rect.width);
        BigInteger right = lhs.min(rhs);
        lhs = this.y.add(this.height);
        rhs = rect.y.add(rect.height);
        BigInteger bottom = lhs.min(rhs);
        this.x = right.compareTo(left) < 0 ? BigInteger.ZERO : left;
        this.y = bottom.compareTo(top) < 0 ? BigInteger.ZERO : top;
        this.width = right.compareTo(left) < 0 ? BigInteger.ZERO : right.subtract(left);
        this.height = bottom.compareTo(top) < 0 ? BigInteger.ZERO : bottom.subtract(top);
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
    public PositionRectangle intersection(PositionRectangle rect) {
        if (rect == null)
            throw new IllegalArgumentException("rectangle can not be null"); //$NON-NLS-1$
        if (this == rect)
            return new PositionRectangle(this.x, this.y, this.width, this.height);
        BigInteger left = this.x.max(rect.x);
        BigInteger top = this.y.max(rect.y);
        BigInteger lhs = this.x.add(this.width);
        BigInteger rhs = rect.x.add(rect.width);
        BigInteger right = lhs.min(rhs);
        lhs = this.y.add(this.height);
        rhs = rect.y.add(rect.height);
        BigInteger bottom = lhs.min(rhs);
        return new PositionRectangle(
                right.compareTo(left) < 0 ? BigInteger.ZERO : left,
                        bottom.compareTo(top) < 0 ? BigInteger.ZERO : top,
                                right.compareTo(left) < 0 ? BigInteger.ZERO : right.subtract(left),
                                        bottom.compareTo(top) < 0 ? BigInteger.ZERO : bottom.subtract(top));
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
     * @see #intersection(PositionRectangle)
     * @see #isEmpty()
     *
     * @throws IllegalArgumentException
     *             if on of the specified values is <code>null</code>
     */
    public boolean intersects(BigInteger x, BigInteger y, BigInteger width, BigInteger height) {
        if (x == null || y == null || width == null || height == null)
            throw new IllegalArgumentException("none of the values can be null"); //$NON-NLS-1$
        return (x.compareTo(this.x.add(this.width)) < 0) && (y.compareTo(this.y.add(this.height)) < 0) &&
                (x.add(width).compareTo(this.x) > 0) && (y.add(height).compareTo(this.y) > 0);
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
     * @see #intersection(PositionRectangle)
     * @see #isEmpty()
     */
    public boolean intersects(PositionRectangle rect) {
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
     * @see #add(PositionRectangle)
     */
    public PositionRectangle union(PositionRectangle rect) {
        if (rect == null)
            throw new IllegalArgumentException("rectangle can not be null"); //$NON-NLS-1$
        BigInteger left = this.x.min(rect.x);
        BigInteger top = this.y.min(rect.y);
        BigInteger lhs = this.x.add(this.width);
        BigInteger rhs = rect.x.add(rect.width);
        BigInteger right = lhs.max(rhs);
        lhs = this.y.add(this.height);
        rhs = rect.y.add(rect.height);
        BigInteger bottom = lhs.max(rhs);
        return new PositionRectangle(left, top, right.subtract(left), bottom.subtract(top));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.height == null) ? 0 : this.height.hashCode());
        result = prime * result + ((this.width == null) ? 0 : this.width.hashCode());
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
        PositionRectangle other = (PositionRectangle) obj;
        if (this.height == null) {
            if (other.height != null)
                return false;
        } else if (!this.height.equals(other.height))
            return false;
        if (this.width == null) {
            if (other.width != null)
                return false;
        } else if (!this.width.equals(other.width))
            return false;
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
        return "PositionRectangle {" + this.x + ", " + this.y + ", " + this.width + ", " + this.height + "}"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

}

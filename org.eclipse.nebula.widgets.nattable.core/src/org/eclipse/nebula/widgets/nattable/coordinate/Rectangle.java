/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.nebula.widgets.nattable.coordinate;


/**
 * Instances of this class represent rectangular areas in an (x, y) coordinate system. The top left
 * corner of the rectangle is specified by its x and y values, and the extent of the rectangle is 
 * specified by its width and height.
 * <p>
 * The coordinate space for rectangles and points is considered to have increasing values downward
 * and to the right from its origin making this the normal, computer graphics oriented notion
 * of (x, y) coordinates rather than the strict mathematical one.
 * </p>
 * <p>
 * The hashCode() method in this class uses the values of the public fields to compute the hash 
 * value. When storing instances of the class in hashed collections, do not modify these fields 
 * after the object has been inserted.
 * </p>
 */
public final class Rectangle {
	
	/**
	 * the x coordinate of the rectangle
	 */
	public int x;
	
	/**
	 * the y coordinate of the rectangle
	 */
	public int y;
	
	/**
	 * the width of the rectangle
	 */
	public int width;
	
	/**
	 * the height of the rectangle
	 */
	public int height;
	
	
	/**
	 * Construct a new instance of this class given the x, y, width and height values.
	 * 
	 * @param x the x coordinate of the origin of the rectangle
	 * @param y the y coordinate of the origin of the rectangle
	 * @param width the width of the rectangle
	 * @param height the height of the rectangle
	 */
	public Rectangle(final int x, final int y, final int width, final int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	
	/**
	 * Returns <code>true</code> if the receiver does not cover any area in the (x, y) coordinate 
	 * plane, and <code>false</code> if the receiver does cover some area in the plane.
	 * <p>
	 * A rectangle is considered to <em>cover area</em> in the (x, y) coordinate plane if both its 
	 * width and height are non-zero.
	 * </p>
	 * 
	 * @return <code>true</code> if the receiver is empty, and <code>false</code> otherwise
	 */
	public boolean isEmpty() {
		return ((this.width <= 0) || (this.height <= 0));
	}
	
	/**
	 * Returns <code>true</code> if the point specified by the arguments is inside the area
	 * specified by the receiver, and <code>false</code> otherwise.
	 * 
	 * @param x the x coordinate of the point to test for containment
	 * @param y the y coordinate of the point to test for containment
	 * @return <code>true</code> if the rectangle contains the point and <code>false</code> otherwise
	 */
	public boolean contains(final int x, final int y) {
		return ((x >= this.x) && (y >= this.y)
				&& x < (this.x + this.width) && y < (this.y + this.height) );
	}
	
	/**
	 * Returns <code>true</code> if the rectangle specified by the arguments is completely inside
	 * the area specified by the receiver, and <code>false</code> otherwise.
	 * 
	 * @param x the x coordinate of the point to test for containment
	 * @param y the y coordinate of the point to test for containment
	 * @return <code>true</code> if the rectangle contains the point and <code>false</code> 
	 *     otherwise
	 * 
	 * @exception NullPointerException if the argument is <code>null</code>
	 */
	public boolean contains(/*@NonNull*/ final Rectangle rect) {
		if (rect == null) {
			new NullPointerException("rect"); //$NON-NLS-1$
		}
		return ((rect.x >= this.x) && (rect.y >= this.y)
				&& (rect.x + rect.width <= this.x + this.width) && (rect.y + rect.height <= this.y + this.height) );
	}
	
	
//	/**
//	 * Returns <code>true</code> if the given point is inside the area specified by the receiver, 
//	 * and <code>false</code> otherwise.
//	 * 
//	 * @param pt the point to test for containment
//	 * @return <code>true</code> if the rectangle contains the point and <code>false</code> 
//	 *     otherwise
//	 * 
//	 * @exception NullPointerException if the argument is <code>null</code>
//	 */
//	public boolean contains(/*@NonNull*/ final Point pt) {
//		if (pt == null) {
//			throw new NullPointerException("pt"); //$NON-NLS-1$
//		}
//		return contains(pt.x, pt.y);
//	}
	
	/**
	 * Returns <code>true</code> if the rectangle described by the arguments intersects with the
	 * receiver and <code>false</code> otherwise.
	 * <p>
	 * Two rectangles intersect if the area of the rectangle representing their intersection is not
	 * empty.
	 * </p>
	 * 
	 * @param x the x coordinate of the origin of the rectangle
	 * @param y the y coordinate of the origin of the rectangle
	 * @param width the width of the rectangle
	 * @param height the height of the rectangle
	 * @return <code>true</code> if the rectangle intersects with the receiver, and 
	 *     <code>false</code> otherwise
	 * 
	 * @see #intersection(Rectangle)
	 * @see #isEmpty()
	 */
	public boolean intersects(final int x, final int y, final int width, final int height) {
		return (x < this.x + this.width) && (y < this.y + this.height)
				&& (x + width > this.x) && (y + height > this.y);
	}
	
	/**
	 * Returns <code>true</code> if the given rectangle intersects with the receiver and 
	 * <code>false</code> otherwise.
	 * <p>
	 * Two rectangles intersect if the area of the rectangle representing their intersection is not
	 * empty.
	 * </p>
	 * 
	 * @param rect the rectangle to test for intersection
	 * @return <code>true</code> if the rectangle intersects with the receiver, and 
	 *     <code>false</code> otherwise
	 * 
	 * @exception NullPointerException if the argument is <code>null</code>
	 * 
	 * @see #intersection(Rectangle)
	 * @see #isEmpty()
	 */
	public boolean intersects(/*@NonNull*/ final Rectangle rect) {
		if (rect == null) {
			new NullPointerException("rect"); //$NON-NLS-1$
		}
		return (rect == this || intersects(rect.x, rect.y, rect.width, rect.height));
	}
	
	
	/**
	 * Destructively replaces the x, y, width and height values in the receiver with ones which 
	 * represent the union of the rectangles specified by the receiver and the given rectangle.
	 * <p>
	 * The union of two rectangles is the smallest single rectangle that completely covers both of
	 * the areas covered by the two given rectangles.
	 * </p>
	 * 
	 * @param rect the rectangle to merge with the receiver
	 * 
	 * @exception NullPointerException if the argument is <code>null</code>
	 */
	public void add(/*@NonNull*/ final Rectangle rect) {
		if (rect == null) {
			throw new NullPointerException("rect"); //$NON-NLS-1$
		}
		final int left = (this.x < rect.x) ? this.x : rect.x;
		final int top = (this.y < rect.y) ? this.y : rect.y;
		int lhs = this.x + this.width;
		int rhs = rect.x + rect.width;
		final int right = (lhs > rhs) ? lhs : rhs;
		lhs = this.y + this.height;
		rhs = rect.y + rect.height;
		final int bottom = (lhs > rhs) ? lhs : rhs;
		this.x = left;
		this.y = top;
		this.width = right - left;
		this.height = bottom - top;
	}
	
	/**
	 * Destructively replaces the x, y, width and height values in the receiver with ones which 
	 * represent the intersection of the rectangles specified by the receiver and the given
	 * rectangle.
	 * 
	 * @param rect the rectangle to intersect with the receiver
	 * 
	 * @exception NullPointerException if the argument is <code>null</code>
	 */
	public void intersect(/*@NonNull*/ final Rectangle rect) {
		if (rect == null) {
			throw new NullPointerException("rect"); //$NON-NLS-1$
		}
		if (this == rect) {
			return;
		}
		final int left = (this.x > rect.x) ? this.x : rect.x;
		final int top = (this.y > rect.y) ? this.y : rect.y;
		int lhs = this.x + this.width;
		int rhs = rect.x + rect.width;
		final int right = (lhs < rhs) ? lhs : rhs;
		lhs = this.y + this.height;
		rhs = rect.y + rect.height;
		final int bottom = (lhs < rhs) ? lhs : rhs;
		this.x = (right < left) ? 0 : left;
		this.y = (bottom < top) ? 0 : top;
		this.width = (right < left) ? 0 : right - left;
		this.height = (bottom < top) ? 0 : bottom - top;
	}
	
	
	/**
	 * Returns a new rectangle which represents the intersection of the receiver and the given
	 * rectangle. 
	 * <p>
	 * The intersection of two rectangles is the rectangle that covers the area which is contained
	 * within both rectangles.
	 * </p>
	 * 
	 * @param rect the rectangle to intersect with the receiver 
	 * @return the intersection of the receiver and the argument
	 * 
	 * @exception NullPointerException if the argument is <code>null</code>
	 */
	public Rectangle intersection(/*@NonNull*/ final Rectangle rect) {
		if (rect == null) {
			throw new NullPointerException("rect"); //$NON-NLS-1$
		}
		if (this == rect) {
			return new Rectangle(this.x, this.y, this.width, this.height);
		}
		final int left = (this.x > rect.x) ? this.x : rect.x;
		final int top = (this.y > rect.y) ? this.y : rect.y;
		int lhs = this.x + this.width;
		int rhs = rect.x + rect.width;
		final int right = (lhs < rhs) ? lhs : rhs;
		lhs = this.y + this.height;
		rhs = rect.y + rect.height;
		final int bottom = (lhs < rhs) ? lhs : rhs;
		return new Rectangle(
				(right < left) ? 0 : left,
				(bottom < top) ? 0 : top,
				(right < left) ? 0 : right - left,
				(bottom < top) ? 0 : bottom - top );
	}
	
	/**
	 * Returns a new rectangle which represents the union of the receiver and the given rectangle.
	 * <p>
	 * The union of two rectangles is the smallest single rectangle that completely covers both of
	 * the areas covered by the two given rectangles.
	 * </p>
	 * 
	 * @param rect the rectangle to perform union with
	 * @return the union of the receiver and the argument
	 * 
	 * @exception NullPointerException if the argument is <code>null</code>
	 * 
	 * @see #add(Rectangle)
	 */
	public Rectangle union(/*@NonNull*/ final Rectangle rect) {
		if (rect == null) {
			throw new NullPointerException("rect"); //$NON-NLS-1$
		}
		final int left = (this.x < rect.x) ? this.x : rect.x;
		final int top = (this.y < rect.y) ? this.y : rect.y;
		int lhs = this.x + this.width;
		int rhs = rect.x + rect.width;
		final int right = (lhs > rhs) ? lhs : rhs;
		lhs = this.y + this.height;
		rhs = rect.y + rect.height;
		final int bottom = (lhs > rhs) ? lhs : rhs;
		return new Rectangle(left, top, right - left, bottom - top);
	}
	
	
	/**
	 * Returns an integer hash code for the receiver. Any two objects that return <code>true</code>
	 * when passed to <code>equals</code> must return the same value for this method.
	 * 
	 * @return the receiver's hash
	 * 
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {
		return this.x ^ this.y ^ this.width ^ this.height;
	}
	
	/**
	 * Compares the argument to the receiver, and returns true if they represent the <em>same</em>
	 * object using a class specific comparison.
	 * 
	 * @param object the object to compare with this object
	 * @return <code>true</code> if the object is the same as this object and <code>false</code>
	 *     otherwise
	 * 
	 * @see #hashCode()
	 */
	@Override
	public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof Rectangle)) {
			return false;
		}
		final Rectangle other = (Rectangle) object;
		return ((other.x == this.x) && (other.y == this.y)
				&& (other.width == this.width) && (other.height == this.height) );
	}
	
	/**
	 * Returns a string containing a concise, human-readable description of the receiver.
	 * 
	 * @return a string representation of the rectangle
	 */
	@Override
	public String toString() {
		return "Rectangle {" + this.x + ", " + this.y + ", " + this.width + ", " + this.height + "}"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
	
}

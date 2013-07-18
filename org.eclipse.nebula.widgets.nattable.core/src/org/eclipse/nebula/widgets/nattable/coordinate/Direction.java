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

package org.eclipse.nebula.widgets.nattable.coordinate;

import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.HORIZONTAL;
import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.VERTICAL;


public enum Direction {
	
	
	LEFT (HORIZONTAL),
	RIGHT (HORIZONTAL),
	UP (VERTICAL),
	DOWN (VERTICAL);
	
	
	public static Direction backward(final Orientation orientation) {
		if (orientation == null) {
			throw new NullPointerException("orientation"); //$NON-NLS-1$
		}
		return (orientation == HORIZONTAL) ? LEFT : UP;
	}
	
	public static Direction forward(final Orientation orientation) {
		if (orientation == null) {
			throw new NullPointerException("orientation"); //$NON-NLS-1$
		}
		return (orientation == HORIZONTAL) ? RIGHT : LEFT;
	}
	
	
	private final Orientation orientation;
	
	
	private Direction(final Orientation orientation) {
		this.orientation = orientation;
	}
	
	
	public Orientation getOrientation() {
		return this.orientation;
	}
	
	public boolean isBackward() {
		switch (this) {
		case LEFT:
		case UP:
			return true;
		case RIGHT:
		case DOWN:
		default:
			return false;
		}
	}
	
	public boolean isForward() {
		switch (this) {
		case RIGHT:
		case DOWN:
			return true;
		case LEFT:
		case UP:
		default:
			return false;
		}
	}
	
	public Direction getOpposite() {
		switch (this) {
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		case UP:
			return DOWN;
		case DOWN:
		default:
			return UP;
		}
	}
	
}

/*******************************************************************************
 * Copyright (c) 2013 Stephan Wahlbrink and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 ******************************************************************************/

package org.eclipse.nebula.widgets.nattable.swt;

import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.HORIZONTAL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;

import org.eclipse.nebula.widgets.nattable.coordinate.Orientation;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;

public class SWTUtil {
	
	
	public static final int getMouseWheelEventType(/*@NonNull*/ final Orientation orientation) {
		if (orientation == null) {
			throw new NullPointerException("orientation"); //$NON-NLS-1$
		}
		return (orientation == HORIZONTAL) ?
				SWT.MouseHorizontalWheel :
				SWT.MouseVerticalWheel;
	}
	
	public static final ScrollBar getScrollBar(/*@NonNull*/ final Scrollable control,
			/*@NonNull*/ final Orientation orientation) {
		if (control == null) {
			throw new NullPointerException("control"); //$NON-NLS-1$
		}
		if (orientation == null) {
			throw new NullPointerException("orientation"); //$NON-NLS-1$
		}
		return (orientation == HORIZONTAL) ?
				control.getHorizontalBar() :
				control.getVerticalBar();
	}
	
	public static final Range getRange(/*@NonNull*/ final Rectangle rectangle,
			/*@NonNull*/ final Orientation orientation) {
		if (rectangle == null) {
			throw new NullPointerException("rectangle"); //$NON-NLS-1$
		}
		if (orientation == null) {
			throw new NullPointerException("orientation"); //$NON-NLS-1$
		}
		return (orientation == HORIZONTAL) ?
				new Range(rectangle.x, rectangle.x + rectangle.width) :
				new Range(rectangle.y, rectangle.y + rectangle.height);
	}
	
}

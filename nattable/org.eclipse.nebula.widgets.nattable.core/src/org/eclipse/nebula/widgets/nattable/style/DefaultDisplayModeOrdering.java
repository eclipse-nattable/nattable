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
package org.eclipse.nebula.widgets.nattable.style;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultDisplayModeOrdering implements IDisplayModeOrdering {
	
	private static final List<String> NORMAL_ORDERING = Arrays.asList(DisplayMode.NORMAL);
	
	private static final List<String> SELECT_ORDERING = Arrays.asList(DisplayMode.SELECT, DisplayMode.NORMAL);

	private static final List<String> EDIT_ORDERING = Arrays.asList(DisplayMode.EDIT, DisplayMode.NORMAL);
	
	private static final List<String> EMPTY_ORDERING = Collections.emptyList();
	
	/**
	 * @see DefaultDisplayModeOrderingTest
	 */
	public List<String> getDisplayModeOrdering(String targetDisplayMode) {
		if (DisplayMode.NORMAL.equals(targetDisplayMode)) {
			return NORMAL_ORDERING;
		} else if (DisplayMode.SELECT.equals(targetDisplayMode)) {
			return SELECT_ORDERING;
		} else if (DisplayMode.EDIT.equals(targetDisplayMode)) {
			return EDIT_ORDERING;
		} else {
			return EMPTY_ORDERING;
		}
	}
}

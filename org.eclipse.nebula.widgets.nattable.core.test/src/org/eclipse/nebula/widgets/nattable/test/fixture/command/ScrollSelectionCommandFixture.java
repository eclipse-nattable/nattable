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
package org.eclipse.nebula.widgets.nattable.test.fixture.command;

import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.ScrollSelectionCommand;

public class ScrollSelectionCommandFixture extends ScrollSelectionCommand {

	public static final MoveDirectionEnum DEFAULT_DIRECTION = MoveDirectionEnum.DOWN;
	public static final boolean DEFAULT_SHIFT_MASK = false;
	public static final boolean DEFAULT_CTRL_MASK = false;
	
	public ScrollSelectionCommandFixture() {
		super(DEFAULT_DIRECTION, DEFAULT_SHIFT_MASK, DEFAULT_CTRL_MASK);
	}

	public ScrollSelectionCommandFixture(MoveDirectionEnum direction) {
		super(direction, DEFAULT_SHIFT_MASK, DEFAULT_CTRL_MASK);
	}
}

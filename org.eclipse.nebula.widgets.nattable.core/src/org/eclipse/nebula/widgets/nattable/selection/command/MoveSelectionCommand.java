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
package org.eclipse.nebula.widgets.nattable.selection.command;

import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

public class MoveSelectionCommand extends AbstractSelectionCommand {

	private final MoveDirectionEnum direction;
	private final int stepSize;

	public MoveSelectionCommand(MoveDirectionEnum direction, boolean shiftMask, boolean controlMask) {
		this(direction, 0, shiftMask, controlMask);
	}

	public MoveSelectionCommand(MoveDirectionEnum direction, int stepSize, boolean shiftMask, boolean controlMask) {
		super(shiftMask, controlMask);
		this.direction = direction;
		this.stepSize = stepSize;
	}

	public MoveDirectionEnum getDirection() {
		return direction;
	}

	public int getStepSize() {
		return stepSize;
	}


}

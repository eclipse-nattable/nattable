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

public class ScrollSelectionCommand extends AbstractSelectionCommand {

    private final MoveDirectionEnum direction;

    public ScrollSelectionCommand(MoveDirectionEnum direction,
            boolean shiftMask, boolean controlMask) {
        super(shiftMask, controlMask);
        this.direction = direction;
    }

    public MoveDirectionEnum getDirection() {
        return this.direction;
    }

}

/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.command;

import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

public class ScrollSelectionCommand extends AbstractSelectionCommand {

    private final MoveDirectionEnum direction;

    public ScrollSelectionCommand(MoveDirectionEnum direction, boolean shiftMask, boolean controlMask) {
        super(shiftMask, controlMask);
        this.direction = direction;
    }

    public MoveDirectionEnum getDirection() {
        return this.direction;
    }

}

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

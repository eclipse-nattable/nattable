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
package org.eclipse.nebula.widgets.nattable.viewport.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractPositionCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class ShowCellInViewportCommand extends AbstractPositionCommand {

    public ShowCellInViewportCommand(ILayer layer, int columnPosition,
            int rowPosition) {
        super(layer, columnPosition, rowPosition);
    }

    protected ShowCellInViewportCommand(ShowCellInViewportCommand command) {
        super(command);
    }

    @Override
    public ShowCellInViewportCommand cloneCommand() {
        return new ShowCellInViewportCommand(this);
    }

}

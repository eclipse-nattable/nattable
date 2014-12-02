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

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class ShowRowInViewportCommand extends AbstractRowCommand {

    public ShowRowInViewportCommand(ILayer layer, int rowPosition) {
        super(layer, rowPosition);
    }

    protected ShowRowInViewportCommand(ShowRowInViewportCommand command) {
        super(command);
    }

    @Override
    public ShowRowInViewportCommand cloneCommand() {
        return new ShowRowInViewportCommand(this);
    }

}

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

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class ShowColumnInViewportCommand extends AbstractColumnCommand {

    public ShowColumnInViewportCommand(ILayer layer, int columnPosition) {
        super(layer, columnPosition);
    }

    protected ShowColumnInViewportCommand(ShowColumnInViewportCommand command) {
        super(command);
    }

    @Override
    public ShowColumnInViewportCommand cloneCommand() {
        return new ShowColumnInViewportCommand(this);
    }

}

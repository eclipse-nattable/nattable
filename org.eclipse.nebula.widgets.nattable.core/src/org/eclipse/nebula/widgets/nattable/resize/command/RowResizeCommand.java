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
package org.eclipse.nebula.widgets.nattable.resize.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Event indicating that a row has been resized.
 */
public class RowResizeCommand extends AbstractRowCommand {

    private int newHeight;

    public RowResizeCommand(ILayer layer, int rowPosition, int newHeight) {
        super(layer, rowPosition);
        this.newHeight = newHeight;
    }

    protected RowResizeCommand(RowResizeCommand command) {
        super(command);
        this.newHeight = command.newHeight;
    }

    public int getNewHeight() {
        return this.newHeight;
    }

    @Override
    public RowResizeCommand cloneCommand() {
        return new RowResizeCommand(this);
    }

}

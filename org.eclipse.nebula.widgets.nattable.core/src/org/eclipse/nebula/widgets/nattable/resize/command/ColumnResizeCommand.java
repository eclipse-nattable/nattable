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

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Event indicating that a column has been resized.
 */
public class ColumnResizeCommand extends AbstractColumnCommand {

    private int newColumnWidth;

    public ColumnResizeCommand(ILayer layer, int columnPosition, int newWidth) {
        super(layer, columnPosition);
        this.newColumnWidth = newWidth;
    }

    protected ColumnResizeCommand(ColumnResizeCommand command) {
        super(command);
        this.newColumnWidth = command.newColumnWidth;
    }

    public int getNewColumnWidth() {
        return this.newColumnWidth;
    }

    @Override
    public ColumnResizeCommand cloneCommand() {
        return new ColumnResizeCommand(this);
    }

}

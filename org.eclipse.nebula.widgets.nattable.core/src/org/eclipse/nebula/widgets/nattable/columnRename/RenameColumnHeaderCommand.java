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
package org.eclipse.nebula.widgets.nattable.columnRename;

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command fired to rename a column header
 *
 * @see RenameColumnHeaderCommandHandler
 */
public class RenameColumnHeaderCommand extends AbstractColumnCommand {

    private final String customColumnName;

    public RenameColumnHeaderCommand(ILayer layer, int columnPosition,
            String customColumnName) {
        super(layer, columnPosition);
        this.customColumnName = customColumnName;
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new RenameColumnHeaderCommand(getLayer(), getColumnPosition(),
                this.customColumnName);
    }

    public String getCustomColumnName() {
        return this.customColumnName;
    }

}

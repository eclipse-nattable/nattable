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
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;

public class ReorderColumnGroupCommand extends ColumnReorderCommand {

    public ReorderColumnGroupCommand(ILayer layer, int fromColumnPosition,
            int toColumnPosition) {
        super(layer, fromColumnPosition, toColumnPosition);
    }

    public ReorderColumnGroupCommand(ReorderColumnGroupCommand command) {
        super(command);
    }

    @Override
    public ReorderColumnGroupCommand cloneCommand() {
        return new ReorderColumnGroupCommand(this);
    }

}

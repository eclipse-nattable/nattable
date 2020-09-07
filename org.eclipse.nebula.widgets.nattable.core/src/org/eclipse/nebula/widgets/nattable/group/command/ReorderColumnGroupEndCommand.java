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
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderEndCommand;

public class ReorderColumnGroupEndCommand extends ColumnReorderEndCommand {

    public ReorderColumnGroupEndCommand(ILayer layer, int toColumnPosition) {
        super(layer, toColumnPosition);
    }

    public ReorderColumnGroupEndCommand(ReorderColumnGroupEndCommand command) {
        super(command);
    }

    @Override
    public ReorderColumnGroupEndCommand cloneCommand() {
        return new ReorderColumnGroupEndCommand(this);
    }

}

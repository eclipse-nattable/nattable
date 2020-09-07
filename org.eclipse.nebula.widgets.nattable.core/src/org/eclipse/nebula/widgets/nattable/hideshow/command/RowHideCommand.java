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
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class RowHideCommand extends AbstractRowCommand {

    public RowHideCommand(ILayer layer, int rowPosition) {
        super(layer, rowPosition);
    }

    protected RowHideCommand(RowHideCommand command) {
        super(command);
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new RowHideCommand(this);
    }

}

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

import org.eclipse.nebula.widgets.nattable.command.AbstractMultiColumnCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class MultiColumnHideCommand extends AbstractMultiColumnCommand {

    public MultiColumnHideCommand(ILayer layer, int columnPosition) {
        this(layer, new int[] { columnPosition });
    }

    public MultiColumnHideCommand(ILayer layer, int... columnPositions) {
        super(layer, columnPositions);
    }

    protected MultiColumnHideCommand(MultiColumnHideCommand command) {
        super(command);
    }

    @Override
    public MultiColumnHideCommand cloneCommand() {
        return new MultiColumnHideCommand(this);
    }

}

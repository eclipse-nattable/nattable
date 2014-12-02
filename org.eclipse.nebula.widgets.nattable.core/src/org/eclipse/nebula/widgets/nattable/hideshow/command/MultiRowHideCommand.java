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
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractMultiRowCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class MultiRowHideCommand extends AbstractMultiRowCommand {

    public MultiRowHideCommand(ILayer layer, int rowPosition) {
        this(layer, new int[] { rowPosition });
    }

    public MultiRowHideCommand(ILayer layer, int[] rowPositions) {
        super(layer, rowPositions);
    }

    protected MultiRowHideCommand(MultiRowHideCommand command) {
        super(command);
    }

    @Override
    public MultiRowHideCommand cloneCommand() {
        return new MultiRowHideCommand(this);
    }

}

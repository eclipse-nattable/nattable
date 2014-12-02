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
package org.eclipse.nebula.widgets.nattable.tree.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandCollapseCommand;

public class TreeExpandCollapseCommand extends AbstractContextFreeCommand {

    private final int parentIndex;

    public TreeExpandCollapseCommand(int parentIndex) {
        this.parentIndex = parentIndex;
    }

    protected TreeExpandCollapseCommand(TreeExpandCollapseCommand command) {
        this.parentIndex = command.parentIndex;
    }

    public int getParentIndex() {
        return this.parentIndex;
    }

}

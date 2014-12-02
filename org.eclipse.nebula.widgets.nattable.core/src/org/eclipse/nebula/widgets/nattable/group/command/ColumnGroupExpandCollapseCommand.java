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

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class ColumnGroupExpandCollapseCommand extends AbstractColumnCommand {

    /**
     * We carry the row position in here as separate member instead of making
     * this command a AbstractPositionCommand. The reason for this is that
     * otherwise the position transformation will fail and the command will not
     * get executed.
     */
    private int rowPosition = 0;

    public ColumnGroupExpandCollapseCommand(ILayer layer, int columnPosition) {
        this(layer, columnPosition, 0);
    }

    public ColumnGroupExpandCollapseCommand(ILayer layer, int columnPosition,
            int rowPosition) {
        super(layer, columnPosition);
        this.rowPosition = rowPosition;
    }

    protected ColumnGroupExpandCollapseCommand(
            ColumnGroupExpandCollapseCommand command) {
        super(command);
        this.rowPosition = command.getRowPosition();
    }

    @Override
    public ColumnGroupExpandCollapseCommand cloneCommand() {
        return new ColumnGroupExpandCollapseCommand(this);
    }

    /**
     * @return the rowPosition
     */
    public int getRowPosition() {
        return this.rowPosition;
    }

}

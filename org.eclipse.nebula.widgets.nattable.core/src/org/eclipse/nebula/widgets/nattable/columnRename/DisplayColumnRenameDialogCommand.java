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

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.swt.graphics.Point;

/**
 * Fire this command to pop-up the rename column dialog.
 */
public class DisplayColumnRenameDialogCommand extends AbstractColumnCommand {

    private final NatTable natTable;

    /**
     * @param columnPosition
     *            of the column to be renamed
     */
    public DisplayColumnRenameDialogCommand(NatTable natTable,
            int columnPosition) {
        super(natTable, columnPosition);
        this.natTable = natTable;
    }

    public Point toDisplayCoordinates(Point point) {
        return this.natTable.toDisplay(point);
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new DisplayColumnRenameDialogCommand((NatTable) getLayer(),
                getColumnPosition());
    }

}

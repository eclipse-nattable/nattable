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
    public DisplayColumnRenameDialogCommand(NatTable natTable, int columnPosition) {
        super(natTable, columnPosition);
        this.natTable = natTable;
    }

    public Point toDisplayCoordinates(Point point) {
        return this.natTable.toDisplay(point);
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new DisplayColumnRenameDialogCommand((NatTable) getLayer(), getColumnPosition());
    }

}

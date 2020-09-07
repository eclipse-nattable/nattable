/*******************************************************************************
 * Copyright (c) 2012, 2020 Edwin Park and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Edwin Park - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.swt.graphics.Point;

public class DisplayColumnGroupRenameDialogCommand extends AbstractColumnCommand implements IColumnGroupCommand {

    private final NatTable natTable;

    /**
     * @param columnPosition
     *            of the column group to be renamed
     */
    public DisplayColumnGroupRenameDialogCommand(NatTable natTable, int columnPosition) {
        super(natTable, columnPosition);
        this.natTable = natTable;
    }

    public Point toDisplayCoordinates(Point point) {
        return this.natTable.toDisplay(point);
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new DisplayColumnGroupRenameDialogCommand((NatTable) getLayer(), getColumnPosition());
    }

}

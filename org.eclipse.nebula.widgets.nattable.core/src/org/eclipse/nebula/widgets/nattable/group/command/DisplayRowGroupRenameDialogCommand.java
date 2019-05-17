/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.swt.graphics.Point;

/**
 * Command to show the row group rename dialog and a rename of a row group if
 * executed.
 *
 * @since 1.6
 */
public class DisplayRowGroupRenameDialogCommand extends AbstractRowCommand implements IRowGroupCommand {

    private final NatTable natTable;

    /**
     * @param rowPosition
     *            the row position of the row group that should be renamed.
     */
    public DisplayRowGroupRenameDialogCommand(NatTable natTable, int rowPosition) {
        super(natTable, rowPosition);
        this.natTable = natTable;
    }

    public Point toDisplayCoordinates(Point point) {
        return this.natTable.toDisplay(point);
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new DisplayRowGroupRenameDialogCommand((NatTable) getLayer(), getRowPosition());
    }

}

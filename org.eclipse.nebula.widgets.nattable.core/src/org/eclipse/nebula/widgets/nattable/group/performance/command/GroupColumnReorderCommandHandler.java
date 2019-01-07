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
package org.eclipse.nebula.widgets.nattable.group.performance.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;

/**
 * Command handler for the {@link ColumnReorderCommand} that is registered on
 * the positionLayer of the {@link ColumnGroupHeaderLayer} to avoid handling in
 * case the reordering would break an unbreakable group.
 *
 * @since 1.6
 */
public class GroupColumnReorderCommandHandler extends AbstractLayerCommandHandler<ColumnReorderCommand> {

    private final ColumnGroupHeaderLayer columnGroupHeaderLayer;

    public GroupColumnReorderCommandHandler(ColumnGroupHeaderLayer columnGroupHeaderLayer) {
        this.columnGroupHeaderLayer = columnGroupHeaderLayer;
    }

    @Override
    protected boolean doCommand(ColumnReorderCommand command) {
        int fromColumnPosition = command.getFromColumnPosition();
        int toColumnPosition = command.getToColumnPosition();

        // return false means process further and do not consume
        return !ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, fromColumnPosition, toColumnPosition);
    }

    @Override
    public Class<ColumnReorderCommand> getCommandClass() {
        return ColumnReorderCommand.class;
    }
}
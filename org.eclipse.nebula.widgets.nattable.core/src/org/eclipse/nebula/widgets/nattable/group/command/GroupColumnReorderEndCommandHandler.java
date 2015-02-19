/*******************************************************************************
 * Copyright (c) 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderEndCommand;

/**
 * @since 1.3
 */
public class GroupColumnReorderEndCommandHandler extends AbstractLayerCommandHandler<ColumnReorderEndCommand> {

    private final ColumnGroupReorderLayer columnGroupReorderLayer;

    public GroupColumnReorderEndCommandHandler(ColumnGroupReorderLayer columnReorderLayer) {
        this.columnGroupReorderLayer = columnReorderLayer;
    }

    @Override
    public Class<ColumnReorderEndCommand> getCommandClass() {
        return ColumnReorderEndCommand.class;
    }

    @Override
    protected boolean doCommand(ColumnReorderEndCommand command) {
        int toColumnPosition = command.getToColumnPosition();
        boolean reorderToLeftEdge = command.isReorderToLeftEdge();

        this.columnGroupReorderLayer.updateColumnGroupModel(
                this.columnGroupReorderLayer.getReorderFromColumnPosition(),
                toColumnPosition, reorderToLeftEdge);

        // we only update the ColumnGroupModel, the reordering is done in the
        // ColumnReorderLayer
        return false;
    }

}

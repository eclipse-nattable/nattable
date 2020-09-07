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
package org.eclipse.nebula.widgets.nattable.edit.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.painter.cell.ColumnHeaderCheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.events.MouseEvent;

public class ToggleCheckBoxColumnAction implements IMouseAction {

    private final ColumnHeaderCheckBoxPainter columnHeaderCheckBoxPainter;
    private final IUniqueIndexLayer bodyDataLayer;

    public ToggleCheckBoxColumnAction(
            ColumnHeaderCheckBoxPainter columnHeaderCheckBoxPainter,
            IUniqueIndexLayer bodyDataLayer) {
        this.columnHeaderCheckBoxPainter = columnHeaderCheckBoxPainter;
        this.bodyDataLayer = bodyDataLayer;
    }

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        int sourceColumnPosition = natTable.getColumnPositionByX(event.x);
        int columnPosition = LayerUtil.convertColumnPosition(natTable, sourceColumnPosition, this.bodyDataLayer);

        int checkedCellsCount = this.columnHeaderCheckBoxPainter
                .getCheckedCellsCount(columnPosition, natTable.getConfigRegistry());
        boolean targetState = checkedCellsCount < this.bodyDataLayer.getRowCount();

        for (int rowPosition = 0; rowPosition < this.bodyDataLayer.getRowCount(); rowPosition++) {
            this.bodyDataLayer.doCommand(
                    new UpdateDataCommand(this.bodyDataLayer, columnPosition, rowPosition, targetState));
        }
    }

}

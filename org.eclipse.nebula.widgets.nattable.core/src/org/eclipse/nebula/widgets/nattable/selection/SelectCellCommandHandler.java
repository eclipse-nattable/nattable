/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Jonas Hugo <Jonas.Hugo@jeppesen.com>,
 *       Markus Wahl <Markus.Wahl@jeppesen.com> - Use getters and setters for
 *         the markers of SelectionLayer instead of the fields.
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 453851
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.isControlOnly;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.swt.graphics.Rectangle;

public class SelectCellCommandHandler implements ILayerCommandHandler<SelectCellCommand> {

    private final SelectionLayer selectionLayer;

    public SelectCellCommandHandler(SelectionLayer selectionLayer) {
        this.selectionLayer = selectionLayer;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, SelectCellCommand command) {
        if (command.convertToTargetLayer(this.selectionLayer)) {
            toggleCell(
                    command.getColumnPosition(),
                    command.getRowPosition(),
                    command.isShiftMask(),
                    command.isControlMask(),
                    command.isForcingEntireCellIntoViewport());
            this.selectionLayer.fireCellSelectionEvent(
                    command.getColumnPosition(),
                    command.getRowPosition(),
                    command.isForcingEntireCellIntoViewport(),
                    command.isShiftMask(),
                    command.isControlMask());
            return true;
        }
        return false;
    }

    /**
     * Toggles the selection state of the given row and column.
     */
    protected void toggleCell(
            int columnPosition, int rowPosition,
            boolean withShiftMask, boolean withControlMask,
            boolean forcingEntireCellIntoViewport) {
        boolean selectCell = true;
        if (isControlOnly(withShiftMask, withControlMask)) {
            if (this.selectionLayer.isCellPositionSelected(columnPosition, rowPosition)) {
                ILayerCell cell = this.selectionLayer.getCellByPosition(columnPosition, rowPosition);
                Rectangle cellRect = new Rectangle(
                        cell.getOriginColumnPosition(),
                        cell.getOriginRowPosition(),
                        cell.getColumnSpan(),
                        cell.getRowSpan());
                this.selectionLayer.clearSelection(cellRect);
                selectCell = false;
            }
        }
        if (selectCell) {
            selectCell(columnPosition, rowPosition, withShiftMask, withControlMask);
        }
    }

    /**
     * Selects a cell, optionally clearing current selection
     */
    public void selectCell(int columnPosition, int rowPosition,
            boolean withShiftMask, boolean withControlMask) {

        if (!withShiftMask && !withControlMask) {
            this.selectionLayer.clear(false);
        }

        ILayerCell cell = this.selectionLayer.getCellByPosition(columnPosition,
                rowPosition);

        if (cell != null) {
            this.selectionLayer.setLastSelectedCell(cell.getOriginColumnPosition(),
                    cell.getOriginRowPosition());

            // Shift pressed + row selected
            if (this.selectionLayer.getSelectionModel().isMultipleSelectionAllowed()
                    && withShiftMask
                    && this.selectionLayer.getLastSelectedRegion() != null
                    && this.selectionLayer.hasRowSelection()
                    && (this.selectionLayer.getSelectionAnchor().rowPosition != SelectionLayer.NO_SELECTION)
                    && (this.selectionLayer.getSelectionAnchor().columnPosition != SelectionLayer.NO_SELECTION)) {
                // if cell.rowPosition > getSelectionAnchor().rowPositon, then
                // use cell.rowPosition + span - 1 (maxRowPosition)
                // else use cell.originRowPosition (minRowPosition)
                // and compare with selectionAnchor.rowPosition
                Rectangle lastSelected = new Rectangle(
                        this.selectionLayer.getLastSelectedRegion().x,
                        this.selectionLayer.getLastSelectedRegion().y,
                        this.selectionLayer.getLastSelectedRegion().width,
                        this.selectionLayer.getLastSelectedRegion().height);

                if (cell.getRowPosition() > this.selectionLayer.getSelectionAnchor().rowPosition) {
                    int maxRowPosition = cell.getOriginRowPosition() + cell.getRowSpan() - 1;
                    lastSelected.height = Math.abs(
                            this.selectionLayer.getSelectionAnchor().rowPosition - maxRowPosition) + 1;
                } else {
                    int minRowPosition = cell.getOriginRowPosition();
                    lastSelected.height = Math.abs(
                            this.selectionLayer.getSelectionAnchor().rowPosition - minRowPosition) + 1;
                }
                lastSelected.y = Math.min(
                        this.selectionLayer.getSelectionAnchor().rowPosition,
                        cell.getOriginRowPosition());

                if (cell.getColumnPosition() > this.selectionLayer.getSelectionAnchor().columnPosition) {
                    int maxColumnPosition = cell.getOriginColumnPosition() + cell.getColumnSpan() - 1;
                    lastSelected.width = Math.abs(
                            this.selectionLayer.getSelectionAnchor().columnPosition - maxColumnPosition) + 1;
                } else {
                    int minColumnPosition = cell.getOriginColumnPosition();
                    lastSelected.width = Math.abs(
                            this.selectionLayer.getSelectionAnchor().columnPosition - minColumnPosition) + 1;
                }
                lastSelected.x = Math.min(
                        this.selectionLayer.getSelectionAnchor().columnPosition,
                        cell.getOriginColumnPosition());

                this.selectionLayer.setLastSelectedRegion(lastSelected);
                this.selectionLayer.addSelection(this.selectionLayer.getLastSelectedRegion());
            } else {
                this.selectionLayer.setLastSelectedRegion(null);
                Rectangle selection = new Rectangle(
                        cell.getOriginColumnPosition(),
                        cell.getOriginRowPosition(),
                        cell.getColumnSpan(),
                        cell.getRowSpan());

                this.selectionLayer.addSelection(selection);
            }
        }
    }

    @Override
    public Class<SelectCellCommand> getCommandClass() {
        return SelectCellCommand.class;
    }

}

/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
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
 *     Vincent Lorenzo <vincent.lorenzo@cea.fr> - Bug 478622
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.bothShiftAndControl;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.isControlOnly;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.isShiftOnly;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.noShiftOrControl;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.ColumnSelectionEvent;
import org.eclipse.swt.graphics.Rectangle;

public class SelectColumnCommandHandler implements
        ILayerCommandHandler<SelectColumnCommand> {

    private final SelectionLayer selectionLayer;

    public SelectColumnCommandHandler(SelectionLayer selectionLayer) {
        this.selectionLayer = selectionLayer;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, SelectColumnCommand command) {
        if (command.convertToTargetLayer(this.selectionLayer)) {
            selectColumn(command.getColumnPosition(), command.getRowPosition(),
                    command.isWithShiftMask(), command.isWithControlMask());
            return true;
        }
        return false;
    }

    protected void selectColumn(int columnPosition, int rowPosition,
            boolean withShiftMask, boolean withControlMask) {
        if (noShiftOrControl(withShiftMask, withControlMask)) {
            this.selectionLayer.clear(false);
            this.selectionLayer.selectCell(columnPosition, 0, false, false);
            this.selectionLayer
                    .selectRegion(columnPosition, 0, 1, Integer.MAX_VALUE);
            this.selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
        } else if (bothShiftAndControl(withShiftMask, withControlMask)) {
            selectColumnWithShiftKey(columnPosition);
        } else if (isShiftOnly(withShiftMask, withControlMask)) {
            selectColumnWithShiftKey(columnPosition);
        } else if (isControlOnly(withShiftMask, withControlMask)) {
            selectColumnWithCtrlKey(columnPosition, rowPosition);
        }

        // Set last selected column position to the recently clicked column
        this.selectionLayer.setLastSelectedCell(columnPosition, rowPosition);

        this.selectionLayer.fireLayerEvent(new ColumnSelectionEvent(this.selectionLayer,
                columnPosition, withShiftMask, withControlMask));
    }

    private void selectColumnWithCtrlKey(int columnPosition, int rowPosition) {
        Rectangle selectedColumnRectangle = new Rectangle(columnPosition, 0, 1,
                Integer.MAX_VALUE);

        if (this.selectionLayer.isColumnPositionFullySelected(columnPosition)) {
            this.selectionLayer.clearSelection(selectedColumnRectangle);
            if (this.selectionLayer.getLastSelectedRegion() != null
                    && this.selectionLayer.getLastSelectedRegion().equals(
                            selectedColumnRectangle)) {
                this.selectionLayer.setLastSelectedRegion(null);
            }
        } else {
            if (this.selectionLayer.getLastSelectedRegion() != null) {
                this.selectionLayer.selectionModel.addSelection(new Rectangle(
                        this.selectionLayer.getLastSelectedRegion().x,
                        this.selectionLayer.getLastSelectedRegion().y,
                        this.selectionLayer.getLastSelectedRegion().width,
                        this.selectionLayer.getLastSelectedRegion().height));
            }
            this.selectionLayer
                    .selectRegion(columnPosition, 0, 1, Integer.MAX_VALUE);
            this.selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
        }
    }

    private void selectColumnWithShiftKey(int columnPosition) {
        int numOfColumnsToIncludeInRegion = 1;
        int startColumnPosition = columnPosition;

        // if multiple selection is disabled, we need to ensure to only select
        // the current columnPosition
        // modifying the selection anchor here ensures that the anchor also
        // moves
        if (!this.selectionLayer.getSelectionModel().isMultipleSelectionAllowed()) {
            this.selectionLayer.getSelectionAnchor().columnPosition = columnPosition;
        }

        if (this.selectionLayer.getLastSelectedRegion() != null) {

            // Negative when we move left, but we are only concerned with the
            // num. of columns
            numOfColumnsToIncludeInRegion = Math.abs(this.selectionLayer
                    .getSelectionAnchor().columnPosition - columnPosition) + 1;

            // Select to the Left
            if (columnPosition < this.selectionLayer.getSelectionAnchor().columnPosition) {
                startColumnPosition = columnPosition;
            } else {
                startColumnPosition = this.selectionLayer.getSelectionAnchor().columnPosition;
            }
        }
        this.selectionLayer.selectRegion(startColumnPosition, 0,
                numOfColumnsToIncludeInRegion, Integer.MAX_VALUE);
    }

    @Override
    public Class<SelectColumnCommand> getCommandClass() {
        return SelectColumnCommand.class;
    }

}

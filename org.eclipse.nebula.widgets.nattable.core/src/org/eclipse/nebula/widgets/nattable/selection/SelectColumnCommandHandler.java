/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
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

public class SelectColumnCommandHandler implements ILayerCommandHandler<SelectColumnCommand> {

    /**
     * @since 1.6
     */
    protected final SelectionLayer selectionLayer;

    public SelectColumnCommandHandler(SelectionLayer selectionLayer) {
        this.selectionLayer = selectionLayer;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, SelectColumnCommand command) {
        if (command.convertToTargetLayer(this.selectionLayer)) {
            selectColumn(
                    command.getColumnPosition(),
                    command.getRowPosition(),
                    command.isWithShiftMask(),
                    command.isWithControlMask());
            return true;
        }
        return false;
    }

    protected void selectColumn(int columnPosition, int rowPosition,
            boolean withShiftMask, boolean withControlMask) {
        if (noShiftOrControl(withShiftMask, withControlMask)) {
            this.selectionLayer.clear(false);
            this.selectionLayer.selectCell(columnPosition, 0, false, false);
            this.selectionLayer.selectRegion(columnPosition, 0, 1, Integer.MAX_VALUE);
            this.selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
        } else if (bothShiftAndControl(withShiftMask, withControlMask)) {
            selectColumnWithShiftKey(columnPosition);
        } else if (isShiftOnly(withShiftMask, withControlMask)) {
            selectColumnWithShiftKey(columnPosition);
        } else if (isControlOnly(withShiftMask, withControlMask)) {
            selectColumnWithCtrlKey(columnPosition, rowPosition);
        }

        // Set last selected column position to the recently clicked column
        this.selectionLayer.setLastSelectedCell(columnPosition, this.selectionLayer.getRowCount() - 1);

        this.selectionLayer.fireLayerEvent(
                new ColumnSelectionEvent(
                        this.selectionLayer,
                        columnPosition,
                        withShiftMask,
                        withControlMask));
    }

    private void selectColumnWithCtrlKey(int columnPosition, int rowPosition) {
        Rectangle selectedColumnRectangle = new Rectangle(columnPosition, 0, 1, Integer.MAX_VALUE);

        if (this.selectionLayer.isColumnPositionFullySelected(columnPosition)) {
            this.selectionLayer.clearSelection(selectedColumnRectangle);
            this.selectionLayer.setLastSelectedRegion(null);

            // if there is still a column selected but no selection anchor, we
            // need to set one for a consistent state
            int[] selectedColumns = this.selectionLayer.getFullySelectedColumnPositions();
            if (selectedColumns.length > 0
                    && this.selectionLayer.getSelectionAnchor().columnPosition == SelectionLayer.NO_SELECTION) {

                // determine column to move the anchor to
                int toPos = selectedColumns[0];
                for (int i = 0; i < selectedColumns.length; i++) {
                    if (selectedColumns[i] < columnPosition) {
                        toPos = selectedColumns[i];
                    } else {
                        break;
                    }
                }
                this.selectionLayer.moveSelectionAnchor(toPos, rowPosition);
            }

        } else {
            if (this.selectionLayer.getLastSelectedRegion() != null) {
                // Preserve last selected region
                this.selectionLayer.selectRegion(
                        this.selectionLayer.getLastSelectedRegion().x,
                        this.selectionLayer.getLastSelectedRegion().y,
                        this.selectionLayer.getLastSelectedRegion().width,
                        this.selectionLayer.getLastSelectedRegion().height);
            }
            this.selectionLayer.selectRegion(columnPosition, 0, 1, Integer.MAX_VALUE);
            this.selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
        }
    }

    private void selectColumnWithShiftKey(int columnPosition) {
        int numOfColumnsToInclude = 1;
        int startColumnPosition = columnPosition;

        // This method selects the range based on the selection anchor and the
        // clicked position. Therefore the selection prior adding the newly
        // calculated selection needs to be cleared in advance.
        Rectangle lastSelectedRegion = this.selectionLayer.getLastSelectedRegion();
        if (lastSelectedRegion != null) {
            this.selectionLayer.getSelectionModel().clearSelection(lastSelectedRegion);
        } else {
            this.selectionLayer.getSelectionModel().clearSelection();
        }

        // move the selection anchor if multiple selection is disabled or there
        // is no selection anchor active already
        if (!this.selectionLayer.getSelectionModel().isMultipleSelectionAllowed()
                || this.selectionLayer.getSelectionAnchor().columnPosition == SelectionLayer.NO_SELECTION) {
            this.selectionLayer.moveSelectionAnchor(columnPosition, 0);
        }

        if (this.selectionLayer.getSelectionAnchor().columnPosition != SelectionLayer.NO_SELECTION) {
            numOfColumnsToInclude = Math.abs(this.selectionLayer.getSelectionAnchor().columnPosition - columnPosition) + 1;
            if (this.selectionLayer.getSelectionAnchor().columnPosition < columnPosition) {
                startColumnPosition = this.selectionLayer.getSelectionAnchor().columnPosition;
            }
        }
        this.selectionLayer.selectRegion(startColumnPosition, 0, numOfColumnsToInclude, Integer.MAX_VALUE);
    }

    @Override
    public Class<SelectColumnCommand> getCommandClass() {
        return SelectColumnCommand.class;
    }

}

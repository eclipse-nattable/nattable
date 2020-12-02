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
 *     Jonas Hugo <Jonas.Hugo@jeppesen.com>,
 *       Markus Wahl <Markus.Wahl@jeppesen.com> - Use getters and setters for
 *         the markers of SelectionLayer instead of the fields.
 *     Vincent Lorenzo <vincent.lorenzo@cea.fr> - Bug 478622
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.isControlOnly;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.isShiftOnly;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.noShiftOrControl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.IntStream;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.group.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.RowGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroupModel;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowGroupsCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.swt.graphics.Rectangle;

public class SelectRowGroupCommandHandler<T> extends AbstractLayerCommandHandler<SelectRowGroupsCommand> {

    private final IRowGroupModel<T> model;
    private final RowGroupHeaderLayer<T> rowGroupHeaderLayer;
    private final SelectionLayer selectionLayer;

    public SelectRowGroupCommandHandler(
            IRowGroupModel<T> model,
            SelectionLayer selectionLayer,
            RowGroupHeaderLayer<T> rowGroupHeaderLayer) {

        this.model = model;
        this.selectionLayer = selectionLayer;
        this.rowGroupHeaderLayer = rowGroupHeaderLayer;
    }

    @Override
    public Class<SelectRowGroupsCommand> getCommandClass() {
        return SelectRowGroupsCommand.class;
    }

    @Override
    protected boolean doCommand(SelectRowGroupsCommand command) {
        int[] rowIndexes = RowGroupUtils.getRowIndexesInGroupAsArray(
                this.model,
                this.rowGroupHeaderLayer.getRowIndexByPosition(command.getRowPosition()));
        int[] rowPositions = RowGroupUtils.getRowPositionsInGroup(
                this.selectionLayer,
                rowIndexes);
        selectRows(
                command.getColumnPosition(),
                rowPositions,
                command.isWithShiftMask(),
                command.isWithControlMask(),
                command.getRowPositionToMoveIntoViewport(),
                command.isMoveAnchorToTopOfGroup());
        return true;
    }

    /**
     * Trigger the selection of rows.
     *
     * @param columnPosition
     *            column position needed for the selection anchor.
     * @param rowPositions
     *            the row to select.
     * @param withShiftMask
     *            if shift mask selection behavior is enabled or not.
     * @param withControlMask
     *            if control mask selection behavior is enabled or not.
     * @param rowPositionToMoveIntoViewport
     *            <code>true</code> if the selected row should be moved into the
     *            viewport to make it visible.
     * @param moveAnchorToTopOfGroup
     *            <code>true</code> if the selection anchor should be moved to
     *            the first row in the group.
     *
     * @since 2.0
     */
    protected void selectRows(
            int columnPosition,
            int[] rowPositions,
            boolean withShiftMask,
            boolean withControlMask,
            int rowPositionToMoveIntoViewport,
            boolean moveAnchorToTopOfGroup) {

        HashSet<Range> changedRowRanges = new HashSet<>();

        if (rowPositions.length > 0) {
            changedRowRanges.addAll(internalSelectRow(
                    columnPosition,
                    rowPositions[0],
                    rowPositions.length,
                    withShiftMask,
                    withControlMask,
                    moveAnchorToTopOfGroup));
        }

        int[] changedRows = changedRowRanges.stream()
                .flatMapToInt(range -> IntStream.range(range.start, range.end))
                .toArray();
        this.selectionLayer.fireLayerEvent(
                new RowSelectionEvent(
                        this.selectionLayer,
                        changedRows,
                        rowPositionToMoveIntoViewport,
                        withShiftMask,
                        withControlMask));
    }

    private HashSet<Range> internalSelectRow(
            int columnPosition,
            int rowPosition,
            int rowCount,
            boolean withShiftMask,
            boolean withControlMask,
            boolean moveAnchorToTopOfGroup) {

        HashSet<Range> changedRowRanges = new HashSet<>();

        if (noShiftOrControl(withShiftMask, withControlMask)) {
            changedRowRanges.addAll(this.selectionLayer.getSelectedRowPositions());
            this.selectionLayer.clear(false);
            this.selectionLayer.selectCell(0, rowPosition, withShiftMask, withControlMask);
            this.selectionLayer.selectRegion(
                    0,
                    rowPosition,
                    this.selectionLayer.getColumnCount(),
                    rowCount);
            changedRowRanges.add(new Range(rowPosition, rowPosition + rowCount));
        } else if (isControlOnly(withShiftMask, withControlMask)) {
            changedRowRanges.add(selectRowWithCtrlKey(columnPosition, rowPosition, rowCount));
        } else if (isShiftOnly(withShiftMask, withControlMask)) {
            changedRowRanges.add(selectRowWithShiftKey(columnPosition, rowPosition, rowCount));
        }
        if (moveAnchorToTopOfGroup) {
            this.selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
        }
        this.selectionLayer.getLastSelectedCellPosition().columnPosition = this.selectionLayer.getColumnCount() - 1;
        this.selectionLayer.getLastSelectedCellPosition().rowPosition = rowPosition;

        return changedRowRanges;
    }

    private Range selectRowWithCtrlKey(int columnPosition, int rowPosition, int rowCount) {
        Rectangle selectedRowRectangle = new Rectangle(0, rowPosition, this.selectionLayer.getColumnCount(), rowCount);

        if (this.selectionLayer.isRowPositionFullySelected(rowPosition)) {
            this.selectionLayer.clearSelection(selectedRowRectangle);
            if (this.selectionLayer.getLastSelectedRegion() != null
                    && this.selectionLayer.getLastSelectedRegion().equals(selectedRowRectangle)) {
                this.selectionLayer.setLastSelectedRegion(null);
            }
        } else {
            // Preserve last selected region
            if (this.selectionLayer.getLastSelectedRegion() != null) {
                this.selectionLayer.selectionModel.addSelection(new Rectangle(
                        this.selectionLayer.getLastSelectedRegion().x,
                        this.selectionLayer.getLastSelectedRegion().y,
                        this.selectionLayer.getLastSelectedRegion().width,
                        this.selectionLayer.getLastSelectedRegion().height));
            }
            this.selectionLayer.selectRegion(
                    0,
                    rowPosition,
                    this.selectionLayer.getColumnCount(),
                    rowCount);
        }

        return new Range(rowPosition, rowPosition + 1);
    }

    private Range selectRowWithShiftKey(int columnPosition, int rowPosition, int rowCount) {
        if (this.selectionLayer.getLastSelectedRegion() != null) {
            int start = Math.min(this.selectionLayer.getLastSelectedRegion().y, rowPosition);
            int end = Math.max(this.selectionLayer.getLastSelectedRegion().y, rowPosition);

            for (int i = start; i <= end; i++) {
                int index = this.selectionLayer.getRowIndexByPosition(i);
                if (RowGroupUtils.isPartOfAGroup(this.model, index)
                        && !this.selectionLayer.isRowPositionFullySelected(i)) {
                    int[] rowPositions = RowGroupUtils.getRowPositionsInGroup(
                            this.selectionLayer,
                            RowGroupUtils.getRowIndexesInGroupAsArray(this.model, index));
                    Arrays.sort(rowPositions);
                    this.selectionLayer.selectRegion(
                            0,
                            rowPositions[0],
                            this.selectionLayer.getColumnCount(),
                            rowPositions.length);
                    i = rowPositions[rowPositions.length - 1];
                }
            }

        }
        return new Range(rowPosition, rowPosition + 1);
    }

}

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
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 447259, 447261
 *     Vincent Lorenzo <vincent.lorenzo@cea.fr> - Bug 478622
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.bothShiftAndControl;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.isControlOnly;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.isShiftOnly;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.noShiftOrControl;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.IntStream;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.swt.graphics.Rectangle;

public class SelectRowCommandHandler implements ILayerCommandHandler<SelectRowsCommand> {

    /**
     * @since 1.6
     */
    protected final SelectionLayer selectionLayer;

    public SelectRowCommandHandler(SelectionLayer selectionLayer) {
        this.selectionLayer = selectionLayer;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, SelectRowsCommand command) {
        if (command.convertToTargetLayer(this.selectionLayer)) {
            selectRows(
                    command.getColumnPosition(),
                    command.getRowPositionsArray(),
                    command.isWithShiftMask(),
                    command.isWithControlMask(),
                    command.getRowPositionToMoveIntoViewport());
            return true;
        }
        return false;
    }

    /**
     * Performs row selection based on the given informations and fires a
     * {@link RowSelectionEvent} for the changed selection.
     *
     * @param columnPosition
     *            The column position of the {@link SelectRowsCommand}.
     * @param rowPositions
     *            The row position of the {@link SelectRowsCommand}.
     * @param withShiftMask
     *            The shift mask information of the {@link SelectRowsCommand}.
     * @param withControlMask
     *            The control mask information of the {@link SelectRowsCommand}.
     * @param rowPositionToMoveIntoViewport
     *            Information which row should be moved to the viewport,
     *            transported by the {@link SelectRowsCommand}.
     *
     * @deprecated Use {@link #selectRows(int, int[], boolean, boolean, int)}
     *             with primitive values.
     */
    @Deprecated
    protected void selectRows(
            int columnPosition, Collection<Integer> rowPositions,
            boolean withShiftMask, boolean withControlMask,
            int rowPositionToMoveIntoViewport) {

        selectRows(
                columnPosition,
                rowPositions.stream().mapToInt(Integer::intValue).toArray(),
                withShiftMask,
                withControlMask,
                rowPositionToMoveIntoViewport);
    }

    /**
     * Performs row selection based on the given informations and fires a
     * {@link RowSelectionEvent} for the changed selection.
     *
     * @param columnPosition
     *            The column position of the {@link SelectRowsCommand}.
     * @param rowPositions
     *            The row position of the {@link SelectRowsCommand}.
     * @param withShiftMask
     *            The shift mask information of the {@link SelectRowsCommand}.
     * @param withControlMask
     *            The control mask information of the {@link SelectRowsCommand}.
     * @param rowPositionToMoveIntoViewport
     *            Information which row should be moved to the viewport,
     *            transported by the {@link SelectRowsCommand}.
     *
     * @since 2.0
     */
    protected void selectRows(
            int columnPosition,
            int[] rowPositions,
            boolean withShiftMask,
            boolean withControlMask,
            int rowPositionToMoveIntoViewport) {

        HashSet<Range> changedRowRanges = new HashSet<Range>();

        for (int rowPosition : rowPositions) {
            changedRowRanges.addAll(
                    internalSelectRow(
                            columnPosition, rowPosition,
                            withShiftMask, withControlMask));
        }

        int[] changedRows = changedRowRanges.stream()
                .flatMapToInt(range -> IntStream.range(range.start, range.end))
                .toArray();

        this.selectionLayer.fireLayerEvent(
                new RowSelectionEvent(
                        this.selectionLayer, changedRows, rowPositionToMoveIntoViewport, withShiftMask, withControlMask));
    }

    /**
     * Delegates the selection operations to execute regarding the state
     * modifier keys.
     *
     * @param columnPosition
     *            The column position of the {@link SelectRowsCommand}.
     * @param rowPositions
     *            The row position of the {@link SelectRowsCommand}.
     * @param withShiftMask
     *            The shift mask information of the {@link SelectRowsCommand}.
     * @param withControlMask
     *            The control mask information of the {@link SelectRowsCommand}.
     * @return The changed selection.
     */
    private HashSet<Range> internalSelectRow(
            int columnPosition, int rowPosition,
            boolean withShiftMask, boolean withControlMask) {

        HashSet<Range> changedRowRanges = new HashSet<Range>();

        if (noShiftOrControl(withShiftMask, withControlMask)) {
            changedRowRanges.addAll(this.selectionLayer.getSelectedRowPositions());
            this.selectionLayer.clear(false);
            this.selectionLayer.selectCell(0, rowPosition, withShiftMask, withControlMask);
            this.selectionLayer.selectRegion(0, rowPosition, Integer.MAX_VALUE, 1);
            this.selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
            changedRowRanges.add(new Range(rowPosition, rowPosition + 1));
        } else if (bothShiftAndControl(withShiftMask, withControlMask)) {
            changedRowRanges.add(selectRowWithShiftKey(columnPosition, rowPosition));
        } else if (isShiftOnly(withShiftMask, withControlMask)) {
            changedRowRanges.add(selectRowWithShiftKey(columnPosition, rowPosition));
        } else if (isControlOnly(withShiftMask, withControlMask)) {
            changedRowRanges.add(selectRowWithCtrlKey(columnPosition, rowPosition));
        }

        this.selectionLayer.setLastSelectedCell(this.selectionLayer.getColumnCount() - 1, rowPosition);

        return changedRowRanges;
    }

    /**
     * Performs selection operations with pressed CTRL modifier.
     *
     * @param columnPosition
     *            The column position of the {@link SelectRowsCommand}. Needed
     *            to move the selection anchor.
     * @param rowPositions
     *            The row position of the {@link SelectRowsCommand}.
     * @return The changed selection.
     */
    private Range selectRowWithCtrlKey(int columnPosition, int rowPosition) {
        Rectangle selectedRowRectangle = new Rectangle(0, rowPosition, Integer.MAX_VALUE, 1);

        if (this.selectionLayer.isRowPositionFullySelected(rowPosition)) {
            this.selectionLayer.clearSelection(selectedRowRectangle);
            this.selectionLayer.setLastSelectedRegion(null);

            // if there is still a row selected but no selection anchor, we
            // need to set one for a consistent state
            int[] selectedRows = this.selectionLayer.getFullySelectedRowPositions();
            if (selectedRows.length > 0
                    && this.selectionLayer.getSelectionAnchor().rowPosition == SelectionLayer.NO_SELECTION) {

                // determine row to move the anchor to
                int toPos = selectedRows[0];
                for (int i = 0; i < selectedRows.length; i++) {
                    if (selectedRows[i] < rowPosition) {
                        toPos = selectedRows[i];
                    } else {
                        break;
                    }
                }
                this.selectionLayer.moveSelectionAnchor(columnPosition, toPos);
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
            this.selectionLayer.selectRegion(0, rowPosition, Integer.MAX_VALUE, 1);
            this.selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
        }

        return new Range(rowPosition, rowPosition + 1);
    }

    /**
     * Performs selection operations with pressed SHIFT modifier.
     *
     * @param columnPosition
     *            The column position of the {@link SelectRowsCommand}. Needed
     *            to move the selection anchor.
     * @param rowPositions
     *            The row position of the {@link SelectRowsCommand}.
     * @return The changed selection.
     */
    private Range selectRowWithShiftKey(int columnPosition, int rowPosition) {
        int numOfRowsToInclude = 1;
        int startRowPosition = rowPosition;

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
                || this.selectionLayer.getSelectionAnchor().rowPosition == SelectionLayer.NO_SELECTION) {
            this.selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
        }

        if (this.selectionLayer.getSelectionAnchor().rowPosition != SelectionLayer.NO_SELECTION) {
            numOfRowsToInclude = Math.abs(this.selectionLayer.getSelectionAnchor().rowPosition - rowPosition) + 1;
            if (this.selectionLayer.getSelectionAnchor().rowPosition < startRowPosition) {
                startRowPosition = this.selectionLayer.getSelectionAnchor().rowPosition;
            }
        }
        this.selectionLayer.selectRegion(0, startRowPosition, Integer.MAX_VALUE, numOfRowsToInclude);

        return new Range(startRowPosition, startRowPosition + numOfRowsToInclude);
    }

    @Override
    public Class<SelectRowsCommand> getCommandClass() {
        return SelectRowsCommand.class;
    }

}

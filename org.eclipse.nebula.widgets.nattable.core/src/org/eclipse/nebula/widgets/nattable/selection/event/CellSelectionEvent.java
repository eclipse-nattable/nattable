/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.selection.event;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

public class CellSelectionEvent extends CellVisualChangeEvent implements ISelectionEvent {

    private final SelectionLayer selectionLayer;

    // The state of the keys when the event was raised
    private boolean withShiftMask = false;
    private boolean withControlMask = false;

    private boolean forcingEntireCellIntoViewport = true;

    /**
     * Creates a {@link CellSelectionEvent} that will move a selected cell into
     * the viewport if it is currently not visible.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer}.
     * @param columnPosition
     *            The column position of the selected cell.
     * @param rowPosition
     *            The row position of the selected cell.
     * @param withShiftMask
     *            <code>true</code> if the SHIFT key was pressed.
     * @param withControlMask
     *            <code>true</code> if the CTRL key was pressed.
     */
    public CellSelectionEvent(SelectionLayer selectionLayer,
            int columnPosition, int rowPosition,
            boolean withShiftMask, boolean withControlMask) {
        super(selectionLayer, columnPosition, rowPosition);
        this.selectionLayer = selectionLayer;
        this.withControlMask = withControlMask;
        this.withShiftMask = withShiftMask;
    }

    /**
     * Creates a {@link CellSelectionEvent}.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer}.
     * @param columnPosition
     *            The column position of the selected cell.
     * @param rowPosition
     *            The row position of the selected cell.
     * @param withShiftMask
     *            <code>true</code> if the SHIFT key was pressed.
     * @param withControlMask
     *            <code>true</code> if the CTRL key was pressed.
     * @param forcingEntireCellIntoViewport
     *            <code>true</code> if the selected cell should be moved into
     *            the viewport, <code>false</code> if not.
     *
     * @since 2.1
     */
    public CellSelectionEvent(SelectionLayer selectionLayer,
            int columnPosition, int rowPosition,
            boolean withShiftMask, boolean withControlMask,
            boolean forcingEntireCellIntoViewport) {
        super(selectionLayer, columnPosition, rowPosition);
        this.selectionLayer = selectionLayer;
        this.withControlMask = withControlMask;
        this.withShiftMask = withShiftMask;
        this.forcingEntireCellIntoViewport = forcingEntireCellIntoViewport;
    }

    // Copy constructor
    protected CellSelectionEvent(CellSelectionEvent event) {
        super(event);
        this.selectionLayer = event.selectionLayer;
        this.withControlMask = event.withControlMask;
        this.withShiftMask = event.withShiftMask;
        this.forcingEntireCellIntoViewport = event.forcingEntireCellIntoViewport;
    }

    @Override
    public SelectionLayer getSelectionLayer() {
        return this.selectionLayer;
    }

    @Override
    public CellSelectionEvent cloneEvent() {
        return new CellSelectionEvent(this);
    }

    /**
     *
     * @return <code>true</code> if the SHIFT key was pressed.
     */
    public boolean isWithShiftMask() {
        return this.withShiftMask;
    }

    /**
     *
     * @return <code>true</code> if the CTRL key was pressed.
     */
    public boolean isWithControlMask() {
        return this.withControlMask;
    }

    /**
     *
     * @return <code>true</code> if the selected cell should be moved into the
     *         viewport, <code>false</code> if not.
     *
     * @since 2.1
     */
    public boolean isForcingEntireCellIntoViewport() {
        return this.forcingEntireCellIntoViewport;
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        if (this.columnPosition == SelectionLayer.NO_SELECTION
                || this.rowPosition == SelectionLayer.NO_SELECTION) {
            return true;
        }
        this.columnPosition = localLayer.underlyingToLocalColumnPosition(getLayer(),
                this.columnPosition);
        this.rowPosition = localLayer.underlyingToLocalRowPosition(getLayer(),
                this.rowPosition);

        this.layer = localLayer;

        return this.columnPosition >= 0 && this.rowPosition >= 0
                && this.columnPosition < this.layer.getColumnCount()
                && this.rowPosition < this.layer.getRowCount();
    }

}

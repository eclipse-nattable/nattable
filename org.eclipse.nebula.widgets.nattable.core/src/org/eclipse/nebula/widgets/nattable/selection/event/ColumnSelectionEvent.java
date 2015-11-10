/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Vincent Lorenzo <vincent.lorenzo@cea.fr> - Bug 478622
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.event;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

public class ColumnSelectionEvent extends ColumnVisualChangeEvent implements ISelectionEvent {

    private final SelectionLayer selectionLayer;

    /**
     * The shift mask used.
     */
    private boolean withShiftMask = false;

    /**
     * The control mask used.
     */
    private boolean withControlMask = false;

    /**
     * Constructor.
     *
     * @param selectionLayer
     *            The selection layer.
     * @param columnPosition
     *            The column position.
     * @deprecated Replaced by
     *             {@link #ColumnSelectionEvent(SelectionLayer, int, boolean, boolean)}
     */
    @Deprecated
    public ColumnSelectionEvent(SelectionLayer selectionLayer, int columnPosition) {
        super(selectionLayer, new Range(columnPosition, columnPosition + 1));
        this.selectionLayer = selectionLayer;
    }

    /**
     * Constructor.
     *
     * @param selectionLayer
     *            The selection layer.
     * @param columnPosition
     *            The column position.
     * @param withShiftMask
     *            Boolean to determinate if the shift mask is used.
     * @param withControlMask
     *            Boolean to determinate if the control mask is used.
     * @since 1.4
     */
    public ColumnSelectionEvent(SelectionLayer selectionLayer, int columnPosition,
            boolean withShiftMask, boolean withControlMask) {
        super(selectionLayer, new Range(columnPosition, columnPosition + 1));
        this.selectionLayer = selectionLayer;
        this.withShiftMask = withShiftMask;
        this.withControlMask = withControlMask;
    }

    protected ColumnSelectionEvent(ColumnSelectionEvent event) {
        super(event);
        this.selectionLayer = event.selectionLayer;
        this.withShiftMask = event.withShiftMask;
        this.withControlMask = event.withControlMask;
    }

    @Override
    public SelectionLayer getSelectionLayer() {
        return this.selectionLayer;
    }

    /**
     * Returns if the shift mask is used.
     *
     * @return <code>true</code> if the shift mask is used, <code>false</code>
     *         otherwise.
     * @since 1.4
     */
    public boolean isWithShiftMask() {
        return this.withShiftMask;
    }

    /**
     * Returns if the control mask is used.
     *
     * @return <code>true</code> if the control mask is used, <code>false</code>
     *         otherwise.
     * @since 1.4
     */
    public boolean isWithControlMask() {
        return this.withControlMask;
    }

    @Override
    public ColumnSelectionEvent cloneEvent() {
        return new ColumnSelectionEvent(this);
    }

}

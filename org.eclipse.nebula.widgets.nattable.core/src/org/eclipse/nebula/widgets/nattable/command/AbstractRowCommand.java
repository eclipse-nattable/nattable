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
package org.eclipse.nebula.widgets.nattable.command;

import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * {@link ILayerCommand} that is executed for a row position. Transports a
 * {@link RowPositionCoordinate} that is transformed while transported down the
 * layer stack.
 */
public abstract class AbstractRowCommand implements ILayerCommand {

    private RowPositionCoordinate rowPositionCoordinate;

    /**
     *
     * @param layer
     *            The {@link ILayer} to which the column and row position
     *            correlate.
     * @param rowPosition
     *            The row position for which the command should be processed.
     */
    protected AbstractRowCommand(ILayer layer, int rowPosition) {
        this.rowPositionCoordinate = new RowPositionCoordinate(layer, rowPosition);
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected AbstractRowCommand(AbstractRowCommand command) {
        this.rowPositionCoordinate = command.rowPositionCoordinate;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        RowPositionCoordinate targetRowPositionCoordinate =
                LayerCommandUtil.convertRowPositionToTargetContext(this.rowPositionCoordinate, targetLayer);
        if (targetRowPositionCoordinate != null) {
            this.rowPositionCoordinate = targetRowPositionCoordinate;
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return The {@link ILayer} to which the column and row position
     *         correlate.
     *
     * @since 1.6
     */
    public ILayer getLayer() {
        return this.rowPositionCoordinate.getLayer();
    }

    /**
     *
     * @return The row position for which the command should be processed.
     */
    public int getRowPosition() {
        return this.rowPositionCoordinate.getRowPosition();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + " [" + this.rowPositionCoordinate.getLayer() //$NON-NLS-1$
                + " rowPosition=" + this.rowPositionCoordinate.getRowPosition() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}

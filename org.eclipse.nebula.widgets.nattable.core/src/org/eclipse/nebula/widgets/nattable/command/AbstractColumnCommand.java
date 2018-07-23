/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.command;

import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * {@link ILayerCommand} that is executed for a column position. Transports a
 * {@link ColumnPositionCoordinate} that is transformed while transported down
 * the layer stack.
 */
public abstract class AbstractColumnCommand implements ILayerCommand {

    private ColumnPositionCoordinate columnPositionCoordinate;

    /**
     *
     * @param layer
     *            The {@link ILayer} to which the column and row position
     *            correlate.
     * @param columnPosition
     *            The column position for which the command should be processed.
     */
    protected AbstractColumnCommand(ILayer layer, int columnPosition) {
        this.columnPositionCoordinate = new ColumnPositionCoordinate(layer, columnPosition);
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected AbstractColumnCommand(AbstractColumnCommand command) {
        this.columnPositionCoordinate = command.columnPositionCoordinate;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        ColumnPositionCoordinate targetColumnPositionCoordinate =
                LayerCommandUtil.convertColumnPositionToTargetContext(this.columnPositionCoordinate, targetLayer);
        if (targetColumnPositionCoordinate != null) {
            this.columnPositionCoordinate = targetColumnPositionCoordinate;
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return The {@link ILayer} to which the column and row position
     *         correlate.
     */
    public ILayer getLayer() {
        return this.columnPositionCoordinate.getLayer();
    }

    /**
     *
     * @return The column position for which the command should be processed.
     */
    public int getColumnPosition() {
        return this.columnPositionCoordinate.getColumnPosition();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + " [" + this.columnPositionCoordinate.getLayer() //$NON-NLS-1$
                + " columnPosition=" + this.columnPositionCoordinate.getColumnPosition() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}

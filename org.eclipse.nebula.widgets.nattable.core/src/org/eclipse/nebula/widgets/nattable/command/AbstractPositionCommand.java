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

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * {@link ILayerCommand} that is executed for a cell position. Transports a
 * {@link PositionCoordinate} that is transformed while transported down the
 * layer stack.
 */
public abstract class AbstractPositionCommand implements ILayerCommand {

    private PositionCoordinate positionCoordinate;

    /**
     *
     * @param layer
     *            The {@link ILayer} to which the column and row position
     *            correlate.
     * @param columnPosition
     *            The column position for which the command should be processed.
     * @param rowPosition
     *            The row position for which the command should be processed.
     */
    protected AbstractPositionCommand(ILayer layer, int columnPosition, int rowPosition) {
        this.positionCoordinate = new PositionCoordinate(layer, columnPosition, rowPosition);
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected AbstractPositionCommand(AbstractPositionCommand command) {
        this.positionCoordinate = command.positionCoordinate;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        PositionCoordinate targetPositionCoordinate =
                LayerCommandUtil.convertPositionToTargetContext(this.positionCoordinate, targetLayer);
        if (targetPositionCoordinate != null) {
            this.positionCoordinate = targetPositionCoordinate;
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
        return this.positionCoordinate.getLayer();
    }

    /**
     *
     * @return The column position for which the command should be processed.
     */
    public int getColumnPosition() {
        return this.positionCoordinate.getColumnPosition();
    }

    /**
     *
     * @return The row position for which the command should be processed.
     */
    public int getRowPosition() {
        return this.positionCoordinate.getRowPosition();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + " [" + this.positionCoordinate.getLayer() //$NON-NLS-1$
                + " columnPosition=" + this.positionCoordinate.getColumnPosition() //$NON-NLS-1$
                + ", rowPosition=" + this.positionCoordinate.getRowPosition() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}

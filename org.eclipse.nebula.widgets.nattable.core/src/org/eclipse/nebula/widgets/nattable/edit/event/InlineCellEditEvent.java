/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit.event;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * This event is used to activate an editor inline. It is used internally if a
 * single cell should be edited by selection, e.g. on pressing the F2 key on the
 * current selected cell.
 * <p>
 * This event is needed to do the translation of the coordinates and bounds
 * needed for inline editing. On activating an editor via selection these
 * informations are not known and need to be evaluated through the layer event
 * handlers.
 */
public class InlineCellEditEvent implements ILayerEvent {

    /**
     * The layer the cellCoordinates rely on. The layer will change on event
     * processing to always match the translated coordinates.
     */
    private ILayer layer;
    /**
     * The coordinates of the cell to edit for the set layer.
     */
    private final PositionCoordinate cellCoordinate;
    /**
     * The parent Composite, needed for the creation of the editor control.
     */
    private final Composite parent;
    /**
     * The {@link IConfigRegistry} containing the configuration of the current
     * NatTable instance the command should be executed for. This is necessary
     * because the edit controllers in the current architecture are not aware of
     * the instance they are running in.
     */
    private final IConfigRegistry configRegistry;
    /**
     * The value that should be put to the activated editor control.
     */
    private final Object initialValue;

    /**
     *
     * @param layer
     *            The layer the cellCoordinates rely on.
     * @param cellCoordinate
     *            The coordinates of the cell to edit for the set layer.
     * @param parent
     *            The parent Composite, needed for the creation of the editor
     *            control.
     * @param configRegistry
     *            The {@link IConfigRegistry} containing the configuration of
     *            the current NatTable instance the command should be executed
     *            for. This is necessary because the edit controllers in the
     *            current architecture are not aware of the instance they are
     *            running in.
     * @param initialValue
     *            The value that should be put to the activated editor control.
     */
    public InlineCellEditEvent(ILayer layer, PositionCoordinate cellCoordinate,
            Composite parent, IConfigRegistry configRegistry,
            Object initialValue) {

        this.layer = layer;
        this.cellCoordinate = cellCoordinate;
        this.parent = parent;
        this.configRegistry = configRegistry;
        this.initialValue = initialValue;
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        this.cellCoordinate.columnPosition = localLayer
                .underlyingToLocalColumnPosition(this.layer,
                        this.cellCoordinate.columnPosition);
        if (this.cellCoordinate.columnPosition < 0
                || this.cellCoordinate.columnPosition >= localLayer.getColumnCount()) {
            return false;
        }

        this.cellCoordinate.rowPosition = localLayer.underlyingToLocalRowPosition(
                this.layer, this.cellCoordinate.rowPosition);
        if (this.cellCoordinate.rowPosition < 0
                || this.cellCoordinate.rowPosition >= localLayer.getRowCount()) {
            return false;
        }

        this.layer = localLayer;
        return true;
    }

    /**
     * @return The column position of the cell to edit.
     */
    public int getColumnPosition() {
        return this.cellCoordinate.columnPosition;
    }

    /**
     * @return The row position of the cell to edit.
     */
    public int getRowPosition() {
        return this.cellCoordinate.rowPosition;
    }

    /**
     * @return The parent Composite, needed for the creation of the editor
     *         control.
     */
    public Composite getParent() {
        return this.parent;
    }

    /**
     * @return The {@link IConfigRegistry} containing the configuration of the
     *         current NatTable instance the command should be executed for.
     *         This is necessary because the edit controllers in the current
     *         architecture are not aware of the instance they are running in.
     */
    public IConfigRegistry getConfigRegistry() {
        return this.configRegistry;
    }

    /**
     * @return The value that should be put to the activated editor control.
     */
    public Object getInitialValue() {
        return this.initialValue;
    }

    @Override
    public InlineCellEditEvent cloneEvent() {
        return new InlineCellEditEvent(this.layer, new PositionCoordinate(
                this.cellCoordinate), this.parent, this.configRegistry, this.initialValue);
    }

}

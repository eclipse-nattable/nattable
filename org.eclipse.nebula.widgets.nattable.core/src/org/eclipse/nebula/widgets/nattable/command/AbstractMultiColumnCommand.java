/*******************************************************************************
 * Copyright (c) 2012, 2019 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.command;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Abstract implementation for commands that should process multiple columns.
 */
public abstract class AbstractMultiColumnCommand implements ILayerCommand {

    protected Collection<ColumnPositionCoordinate> columnPositionCoordinates;

    /**
     *
     * @param layer
     *            The {@link ILayer} to which the positions match.
     * @param columnPositions
     *            The positions that should be processed by this command.
     */
    protected AbstractMultiColumnCommand(ILayer layer, int... columnPositions) {
        setColumnPositions(layer, columnPositions);
    }

    /**
     * Clone constructor
     *
     * @param command
     *            The command to clone.
     */
    protected AbstractMultiColumnCommand(AbstractMultiColumnCommand command) {
        this.columnPositionCoordinates = new HashSet<>(command.columnPositionCoordinates);
    }

    /**
     *
     * @return The unique column positions that should be processed by this
     *         command.
     */
    public Collection<Integer> getColumnPositions() {
        return this.columnPositionCoordinates.stream()
                .map(ColumnPositionCoordinate::getColumnPosition)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     *
     * @param layer
     *            The {@link ILayer} to which the positions match.
     * @param columnPositions
     *            The positions that should be processed by this command.
     */
    protected final void setColumnPositions(ILayer layer, int... columnPositions) {
        this.columnPositionCoordinates = new HashSet<>();
        for (int columnPosition : columnPositions) {
            this.columnPositionCoordinates.add(new ColumnPositionCoordinate(layer, columnPosition));
        }
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        Collection<ColumnPositionCoordinate> converted = this.columnPositionCoordinates.stream()
                .map(coord -> LayerCommandUtil.convertColumnPositionToTargetContext(coord, targetLayer))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        if (converted.size() > 0) {
            this.columnPositionCoordinates = converted;
            return true;
        } else {
            return false;
        }
    }

}

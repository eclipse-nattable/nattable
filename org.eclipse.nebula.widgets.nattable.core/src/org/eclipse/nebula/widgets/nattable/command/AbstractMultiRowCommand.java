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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Abstract implementation for commands that should process multiple rows.
 */
public abstract class AbstractMultiRowCommand implements ILayerCommand {

    private Collection<RowPositionCoordinate> rowPositionCoordinates;

    /**
     *
     * @param layer
     *            The {@link ILayer} to which the positions match.
     * @param rowPositions
     *            The positions that should be processed by this command.
     */
    protected AbstractMultiRowCommand(ILayer layer, int... rowPositions) {
        setRowPositions(layer, rowPositions);
    }

    /**
     * Clone constructor
     *
     * @param command
     *            The command to clone.
     */
    protected AbstractMultiRowCommand(AbstractMultiRowCommand command) {
        this.rowPositionCoordinates = new HashSet<>(command.rowPositionCoordinates);
    }

    /**
     *
     * @return The unique row positions that should be processed by this
     *         command.
     */
    public Collection<Integer> getRowPositions() {
        return this.rowPositionCoordinates.stream()
                .map(RowPositionCoordinate::getRowPosition)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     *
     * @return The unique row positions that should be processed by this
     *         command.
     *
     * @since 2.0
     */
    public int[] getRowPositionsArray() {
        return this.rowPositionCoordinates.stream()
                .mapToInt(RowPositionCoordinate::getRowPosition)
                .sorted()
                .toArray();
    }

    /**
     *
     * @param layer
     *            The {@link ILayer} to which the positions match.
     * @param rowPositions
     *            The positions that should be processed by this command.
     */
    protected final void setRowPositions(ILayer layer, int... rowPositions) {
        this.rowPositionCoordinates = new HashSet<>();
        for (int rowPosition : rowPositions) {
            this.rowPositionCoordinates.add(new RowPositionCoordinate(layer, rowPosition));
        }
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        Collection<RowPositionCoordinate> converted = this.rowPositionCoordinates.stream()
                .map(coord -> LayerCommandUtil.convertRowPositionToTargetContext(coord, targetLayer))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        if (converted.size() > 0) {
            this.rowPositionCoordinates = converted;
            return true;
        } else {
            return false;
        }
    }

}

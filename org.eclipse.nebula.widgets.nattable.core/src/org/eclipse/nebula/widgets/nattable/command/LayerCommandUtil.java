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

import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Helper class for converting coordinates. Does only support conversions down
 * the layer stack, e.g. for transporting commands.
 */
public class LayerCommandUtil {

    private LayerCommandUtil() {
        // private default constructor for helper class
    }

    /**
     * Convert the given {@link PositionCoordinate} to the given target
     * {@link ILayer}.
     *
     * @param positionCoordinate
     *            The {@link PositionCoordinate} that should be converted.
     * @param targetLayer
     *            The {@link ILayer} to which the {@link PositionCoordinate}
     *            should be converted to.
     * @return The converted {@link PositionCoordinate} or <code>null</code> if
     *         a conversion is not possible.
     */
    public static PositionCoordinate convertPositionToTargetContext(
            PositionCoordinate positionCoordinate, ILayer targetLayer) {
        return convertPositionToTargetContext(positionCoordinate, targetLayer, false);
    }

    /**
     * Convert the given {@link PositionCoordinate} to the given target
     * {@link ILayer}.
     *
     * @param positionCoordinate
     *            The {@link PositionCoordinate} that should be converted.
     * @param targetLayer
     *            The {@link ILayer} to which the {@link PositionCoordinate}
     *            should be converted to.
     * @param acceptNegativePosition
     *            <code>true</code> if a negative position value should be
     *            accepted, <code>false</code> if not.
     *            <p>
     *            <b>Note:</b> Accepting a negative position value could cause
     *            serious issues as a negative position typically means the
     *            position does not exist. Only in cases where the ViewportLayer
     *            and spanning is involved a negative position could make sense
     *            as the position could be for example not visible at the
     *            moment.
     *            </p>
     * @return The converted {@link PositionCoordinate} or <code>null</code> if
     *         a conversion is not possible.
     *
     * @since 1.6
     */
    public static PositionCoordinate convertPositionToTargetContext(
            PositionCoordinate positionCoordinate,
            ILayer targetLayer,
            boolean acceptNegativePosition) {

        ILayer layer = positionCoordinate.getLayer();

        if (layer == targetLayer) {
            return positionCoordinate;
        }

        int columnPosition = positionCoordinate.getColumnPosition();
        int underlyingColumnPosition = layer.localToUnderlyingColumnPosition(columnPosition);
        if (!acceptNegativePosition && underlyingColumnPosition < 0) {
            return null;
        }

        int rowPosition = positionCoordinate.getRowPosition();
        int underlyingRowPosition = layer.localToUnderlyingRowPosition(rowPosition);
        if (!acceptNegativePosition && underlyingRowPosition < 0) {
            return null;
        }

        ILayer underlyingLayer = layer.getUnderlyingLayerByPosition(columnPosition, rowPosition);
        if (underlyingLayer == null) {
            return null;
        }

        return convertPositionToTargetContext(
                new PositionCoordinate(underlyingLayer, underlyingColumnPosition, underlyingRowPosition),
                targetLayer,
                acceptNegativePosition);
    }

    /**
     * Convert the given {@link ColumnPositionCoordinate} to the given target
     * {@link ILayer}.
     *
     * @param columnPositionCoordinate
     *            The {@link ColumnPositionCoordinate} that should be converted.
     * @param targetLayer
     *            The {@link ILayer} to which the
     *            {@link ColumnPositionCoordinate} should be converted to.
     * @return The converted {@link ColumnPositionCoordinate} or
     *         <code>null</code> if a conversion is not possible.
     */
    public static ColumnPositionCoordinate convertColumnPositionToTargetContext(
            ColumnPositionCoordinate columnPositionCoordinate, ILayer targetLayer) {
        return convertColumnPositionToTargetContext(columnPositionCoordinate, targetLayer, false);
    }

    /**
     * Convert the given {@link ColumnPositionCoordinate} to the given target
     * {@link ILayer}.
     *
     * @param columnPositionCoordinate
     *            The {@link ColumnPositionCoordinate} that should be converted.
     * @param targetLayer
     *            The {@link ILayer} to which the
     *            {@link ColumnPositionCoordinate} should be converted to.
     * @param acceptNegativePosition
     *            <code>true</code> if a negative column position should be
     *            accepted, <code>false</code> if not.
     *            <p>
     *            <b>Note:</b> Accepting a negative position could cause serious
     *            issues as a negative position typically means the position
     *            does not exist. Only in cases where the ViewportLayer and
     *            spanning is involved a negative position could make sense as
     *            the position could be for example not visible at the moment.
     *            </p>
     * @return The converted {@link ColumnPositionCoordinate} or
     *         <code>null</code> if a conversion is not possible.
     *
     * @since 1.6
     */
    public static ColumnPositionCoordinate convertColumnPositionToTargetContext(
            ColumnPositionCoordinate columnPositionCoordinate,
            ILayer targetLayer,
            boolean acceptNegativePosition) {

        if (columnPositionCoordinate != null) {
            ILayer layer = columnPositionCoordinate.getLayer();

            if (layer == targetLayer) {
                return columnPositionCoordinate;
            }

            int columnPosition = columnPositionCoordinate.getColumnPosition();
            int underlyingColumnPosition = layer.localToUnderlyingColumnPosition(columnPosition);
            if (!acceptNegativePosition && underlyingColumnPosition < 0) {
                return null;
            }

            Collection<ILayer> underlyingLayers = layer.getUnderlyingLayersByColumnPosition(columnPosition);
            if (underlyingLayers != null) {
                for (ILayer underlyingLayer : underlyingLayers) {
                    if (underlyingLayer != null) {
                        ColumnPositionCoordinate convertedColumnPositionCoordinate =
                                convertColumnPositionToTargetContext(
                                        new ColumnPositionCoordinate(underlyingLayer, underlyingColumnPosition),
                                        targetLayer,
                                        acceptNegativePosition);
                        if (convertedColumnPositionCoordinate != null) {
                            return convertedColumnPositionCoordinate;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Convert the given {@link RowPositionCoordinate} to the given target
     * {@link ILayer}.
     *
     * @param rowPositionCoordinate
     *            The {@link RowPositionCoordinate} that should be converted.
     * @param targetLayer
     *            The {@link ILayer} to which the {@link RowPositionCoordinate}
     *            should be converted to.
     * @return The converted {@link RowPositionCoordinate} or <code>null</code>
     *         if a conversion is not possible.
     */
    public static RowPositionCoordinate convertRowPositionToTargetContext(
            RowPositionCoordinate rowPositionCoordinate, ILayer targetLayer) {
        return convertRowPositionToTargetContext(rowPositionCoordinate, targetLayer, false);
    }

    /**
     * Convert the given {@link RowPositionCoordinate} to the given target
     * {@link ILayer}.
     *
     * @param rowPositionCoordinate
     *            The {@link RowPositionCoordinate} that should be converted.
     * @param targetLayer
     *            The {@link ILayer} to which the {@link RowPositionCoordinate}
     *            should be converted to.
     * @param acceptNegativePosition
     *            <code>true</code> if a negative row position should be
     *            accepted, <code>false</code> if not.
     *            <p>
     *            <b>Note:</b> Accepting a negative position could cause serious
     *            issues as a negative position typically means the position
     *            does not exist. Only in cases where the ViewportLayer and
     *            spanning is involved a negative position could make sense as
     *            the position could be for example not visible at the moment.
     *            </p>
     * @return The converted {@link RowPositionCoordinate} or <code>null</code>
     *         if a conversion is not possible.
     *
     * @since 1.6
     */
    public static RowPositionCoordinate convertRowPositionToTargetContext(
            RowPositionCoordinate rowPositionCoordinate,
            ILayer targetLayer,
            boolean acceptNegativePosition) {

        if (rowPositionCoordinate != null) {
            ILayer layer = rowPositionCoordinate.getLayer();

            if (layer == targetLayer) {
                return rowPositionCoordinate;
            }

            int rowPosition = rowPositionCoordinate.getRowPosition();
            int underlyingRowPosition = layer.localToUnderlyingRowPosition(rowPosition);
            if (!acceptNegativePosition && underlyingRowPosition < 0) {
                return null;
            }

            Collection<ILayer> underlyingLayers = layer.getUnderlyingLayersByRowPosition(rowPosition);
            if (underlyingLayers != null) {
                for (ILayer underlyingLayer : underlyingLayers) {
                    if (underlyingLayer != null) {
                        RowPositionCoordinate convertedRowPositionCoordinate =
                                convertRowPositionToTargetContext(
                                        new RowPositionCoordinate(underlyingLayer, underlyingRowPosition),
                                        targetLayer,
                                        acceptNegativePosition);
                        if (convertedRowPositionCoordinate != null) {
                            return convertedRowPositionCoordinate;
                        }
                    }
                }
            }
        }
        return null;
    }

}

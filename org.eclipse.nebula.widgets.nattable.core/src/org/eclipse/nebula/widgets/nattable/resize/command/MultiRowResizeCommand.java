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
package org.eclipse.nebula.widgets.nattable.resize.command;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.command.AbstractMultiRowCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to trigger resizing of multiple rows.
 */
public class MultiRowResizeCommand extends AbstractMultiRowCommand {

    private int commonRowHeight = -1;
    protected Map<RowPositionCoordinate, Integer> rowPositionToHeight = new HashMap<>();
    private final boolean downScale;

    /**
     * Create a {@link MultiRowResizeCommand} to resize multiple rows, where
     * each row is resized to the same height, e.g. during a drag resize. The
     * given row height will be taken as is without scaling.
     *
     * @param layer
     *            The {@link ILayer} to which the row positions correlate.
     * @param rowPositions
     *            The positions of the rows that should be resized.
     * @param commonRowHeight
     *            The row height that should be applied to all given rows.
     */
    public MultiRowResizeCommand(ILayer layer, int[] rowPositions, int commonRowHeight) {
        this(layer, rowPositions, commonRowHeight, false);
    }

    /**
     * Create a {@link MultiRowResizeCommand} to resize multiple rows, where
     * each row is resized to the same height, e.g. during a drag resize.
     *
     * @param layer
     *            The {@link ILayer} to which the row positions correlate.
     * @param rowPositions
     *            The positions of the rows that should be resized.
     * @param commonRowHeight
     *            The row height that should be applied to all given rows.
     * @param downScale
     *            <code>true</code> if the commonRowHeight value should be down
     *            scaled according to the scaling level, <code>false</code> if
     *            the value should be taken as is.
     *
     * @since 1.6
     */
    public MultiRowResizeCommand(ILayer layer, int[] rowPositions, int commonRowHeight, boolean downScale) {
        super(layer, rowPositions);
        this.commonRowHeight = commonRowHeight;
        this.downScale = downScale;
    }

    /**
     * Create a {@link MultiRowResizeCommand} to resize multiple rows, where
     * each row can be resized to a different size, e.g. during auto resize. The
     * given row heights will be taken as is without scaling.
     *
     * @param layer
     *            The {@link ILayer} to which the row positions correlate.
     * @param rowPositions
     *            The positions of the rows that should be resized.
     * @param rowHeights
     *            The new heights that should be applied to the given rows. The
     *            indexes in the arrays need to correlate.
     */
    public MultiRowResizeCommand(ILayer layer, int[] rowPositions, int[] rowHeights) {
        this(layer, rowPositions, rowHeights, false);
    }

    /**
     * Create a {@link MultiRowResizeCommand} to resize multiple rows, where
     * each row can be resized to a different size, e.g. during auto resize.
     *
     * @param layer
     *            The {@link ILayer} to which the row positions correlate.
     * @param rowPositions
     *            The positions of the rows that should be resized.
     * @param rowHeights
     *            The new heights that should be applied to the given rows. The
     *            indexes in the arrays need to correlate.
     * @param downScale
     *            <code>true</code> if the rowHeights values should be down
     *            scaled according to the scaling level, <code>false</code> if
     *            the value should be taken as is.
     *
     * @since 1.6
     */
    public MultiRowResizeCommand(ILayer layer, int[] rowPositions, int[] rowHeights, boolean downScale) {
        super(layer, rowPositions);
        for (int i = 0; i < rowPositions.length; i++) {
            this.rowPositionToHeight.put(
                    new RowPositionCoordinate(layer, rowPositions[i]),
                    Integer.valueOf(rowHeights[i]));
        }
        this.downScale = downScale;
    }

    /**
     * Constructor used to clone the given command.
     *
     * @param command
     *            The command to clone.
     */
    protected MultiRowResizeCommand(MultiRowResizeCommand command) {
        super(command);
        this.commonRowHeight = command.commonRowHeight;
        this.rowPositionToHeight = new HashMap<>(command.rowPositionToHeight);
        this.downScale = command.downScale;
    }

    /**
     *
     * @return The common row height if all rows should be resized to the same
     *         height, or -1 if the rows should be resized to different values.
     */
    public int getCommonRowHeight() {
        return this.commonRowHeight;
    }

    /**
     *
     * @param rowPosition
     *            The row position for which the new height is requested.
     * @return The new row height for the requested position.
     */
    public int getRowHeight(int rowPosition) {
        for (RowPositionCoordinate rowPositionCoordinate : this.rowPositionToHeight.keySet()) {
            if (rowPositionCoordinate.getRowPosition() == rowPosition) {
                return this.rowPositionToHeight.get(rowPositionCoordinate).intValue();
            }
        }
        return this.commonRowHeight;
    }

    /**
     *
     * @return <code>true</code> if the row height value should be down scaled
     *         according to the scaling level, <code>false</code> if the value
     *         should be taken as is.
     *
     * @since 1.6
     */
    public boolean downScaleValue() {
        return this.downScale;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        Map<RowPositionCoordinate, Integer> newRowPositionToHeight = new HashMap<>();

        for (RowPositionCoordinate rowPositionCoordinate : this.rowPositionToHeight.keySet()) {
            RowPositionCoordinate convertedRowPositionCoordinate =
                    LayerCommandUtil.convertRowPositionToTargetContext(rowPositionCoordinate, targetLayer);
            if (convertedRowPositionCoordinate != null) {
                newRowPositionToHeight.put(
                        convertedRowPositionCoordinate,
                        this.rowPositionToHeight.get(rowPositionCoordinate));
            }
        }

        if (super.convertToTargetLayer(targetLayer)) {
            this.rowPositionToHeight = newRowPositionToHeight;
            return true;
        }
        return false;
    }

    @Override
    public MultiRowResizeCommand cloneCommand() {
        return new MultiRowResizeCommand(this);
    }

}

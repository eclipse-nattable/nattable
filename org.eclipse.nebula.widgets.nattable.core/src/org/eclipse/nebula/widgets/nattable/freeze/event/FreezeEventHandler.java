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
package org.eclipse.nebula.widgets.nattable.freeze.event;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;

public class FreezeEventHandler implements
        ILayerEventHandler<IStructuralChangeEvent> {

    private final FreezeLayer freezeLayer;

    public FreezeEventHandler(FreezeLayer freezeLayer) {
        this.freezeLayer = freezeLayer;
    }

    @Override
    public Class<IStructuralChangeEvent> getLayerEventClass() {
        return IStructuralChangeEvent.class;
    }

    @Override
    public void handleLayerEvent(IStructuralChangeEvent event) {
        PositionCoordinate topLeftPosition = this.freezeLayer.getTopLeftPosition();
        PositionCoordinate bottomRightPosition = this.freezeLayer
                .getBottomRightPosition();

        Collection<StructuralDiff> columnDiffs = event.getColumnDiffs();
        if (columnDiffs != null) {
            int leftOffset = 0;
            int rightOffset = 0;

            for (StructuralDiff columnDiff : columnDiffs) {
                DiffTypeEnum diffType = columnDiff.getDiffType();
                if (diffType == DiffTypeEnum.ADD) {
                    Range afterPositionRange = columnDiff
                            .getAfterPositionRange();
                    if (afterPositionRange.start < topLeftPosition.columnPosition) {
                        leftOffset += afterPositionRange.size();
                    }
                    if (afterPositionRange.start <= bottomRightPosition.columnPosition) {
                        rightOffset += afterPositionRange.size();
                    }
                } else if (diffType == DiffTypeEnum.DELETE) {
                    Range beforePositionRange = columnDiff
                            .getBeforePositionRange();
                    if (beforePositionRange.start < topLeftPosition.columnPosition) {
                        leftOffset -= Math.min(beforePositionRange.end,
                                topLeftPosition.columnPosition + 1)
                                - beforePositionRange.start;
                    }
                    if (beforePositionRange.start <= bottomRightPosition.columnPosition) {
                        rightOffset -= Math.min(beforePositionRange.end,
                                bottomRightPosition.columnPosition + 1)
                                - beforePositionRange.start;
                    }
                }
            }

            topLeftPosition.columnPosition += leftOffset;
            bottomRightPosition.columnPosition += rightOffset;
        }

        Collection<StructuralDiff> rowDiffs = event.getRowDiffs();
        if (rowDiffs != null) {
            int leftOffset = 0;
            int rightOffset = 0;

            for (StructuralDiff rowDiff : rowDiffs) {
                DiffTypeEnum diffType = rowDiff.getDiffType();
                if (diffType == DiffTypeEnum.ADD) {
                    Range afterPositionRange = rowDiff
                            .getAfterPositionRange();
                    if (afterPositionRange.start < topLeftPosition.rowPosition) {
                        leftOffset += afterPositionRange.size();
                    }
                    if (afterPositionRange.start <= bottomRightPosition.rowPosition) {
                        rightOffset += afterPositionRange.size();
                    }
                } else if (diffType == DiffTypeEnum.DELETE) {
                    Range beforePositionRange = rowDiff
                            .getBeforePositionRange();
                    if (beforePositionRange.start < topLeftPosition.rowPosition) {
                        leftOffset -= Math.min(beforePositionRange.end,
                                topLeftPosition.rowPosition + 1)
                                - beforePositionRange.start;
                    }
                    if (beforePositionRange.start <= bottomRightPosition.rowPosition) {
                        rightOffset -= Math.min(beforePositionRange.end,
                                bottomRightPosition.rowPosition + 1)
                                - beforePositionRange.start;
                    }
                }
            }

            topLeftPosition.rowPosition += leftOffset;
            bottomRightPosition.rowPosition += rightOffset;
        }
    }

}

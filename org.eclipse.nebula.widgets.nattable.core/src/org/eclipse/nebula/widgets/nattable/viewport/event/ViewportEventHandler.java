/*******************************************************************************
 * Copyright (c) 2012, 2024 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.viewport.event;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class ViewportEventHandler implements ILayerEventHandler<IStructuralChangeEvent> {

    private final ViewportLayer viewportLayer;

    public ViewportEventHandler(ViewportLayer viewportLayer) {
        this.viewportLayer = viewportLayer;
    }

    @Override
    public Class<IStructuralChangeEvent> getLayerEventClass() {
        return IStructuralChangeEvent.class;
    }

    @Override
    public void handleLayerEvent(IStructuralChangeEvent event) {
        IUniqueIndexLayer scrollableLayer = this.viewportLayer.getScrollableLayer();

        if (event.isHorizontalStructureChanged()) {
            this.viewportLayer.invalidateHorizontalStructure();

            int columnOffset = 0;
            int minimumOriginColumnPosition = this.viewportLayer.getMinimumOriginColumnPosition();

            Collection<StructuralDiff> columnDiffs = event.getColumnDiffs();
            if (columnDiffs != null) {
                if (minimumOriginColumnPosition < 0) {
                    // this is for handling of hide/show behaviour
                    // the value can only be -1 in case the column for which the
                    // minimum origin was set before
                    // was hidden, so we try to determine the correct value now
                    // if it is shown again
                    minimumOriginColumnPosition = scrollableLayer
                            .getColumnPositionByX(this.viewportLayer.getMinimumOrigin().getX());
                }
                for (StructuralDiff columnDiff : columnDiffs) {
                    DiffTypeEnum diffType = columnDiff.getDiffType();
                    if (diffType == DiffTypeEnum.ADD) {
                        Range afterPositionRange = columnDiff.getAfterPositionRange();
                        if (minimumOriginColumnPosition > 0) {
                            int mocp = minimumOriginColumnPosition;
                            for (int i = afterPositionRange.start; i < afterPositionRange.end; i++) {
                                if (i < mocp) {
                                    mocp++;
                                    columnOffset += 1;
                                }
                            }
                        }
                    } else if (diffType == DiffTypeEnum.DELETE) {
                        Range beforePositionRange = columnDiff.getBeforePositionRange();
                        if (minimumOriginColumnPosition > 0) {
                            for (int i = beforePositionRange.start; i < beforePositionRange.end; i++) {
                                if (i < minimumOriginColumnPosition) {
                                    columnOffset -= 1;
                                }
                            }
                        }
                    }
                }
            }

            int minimumOriginColumn = minimumOriginColumnPosition + columnOffset;

            // in case of split viewports we use the min column position instead
            // of the calculated value
            if (this.viewportLayer.getMinColumnPosition() >= 0) {
                minimumOriginColumn = this.viewportLayer.getMinColumnPosition();
            }

            // if the new origin is out of range (e.g. the last column in the
            // viewport is moved
            // to the frozen region, the minimum origin need to be updated in
            // another way
            int startX = scrollableLayer.getStartXOfColumnPosition(minimumOriginColumn);
            if (startX < 0 && minimumOriginColumnPosition > 0) {
                int columnCount = scrollableLayer.getColumnCount();
                if (columnCount == 0) {
                    // special case when all columns are hidden
                    startX = 0;
                } else {
                    startX = scrollableLayer.getStartXOfColumnPosition(columnCount - 1)
                            + scrollableLayer.getColumnWidthByPosition(columnCount - 1);
                }
            }

            this.viewportLayer.setMinimumOriginX(startX);
        }

        if (event.isVerticalStructureChanged()) {
            this.viewportLayer.invalidateVerticalStructure();

            int rowOffset = 0;
            int minimumOriginRowPosition = this.viewportLayer.getMinimumOriginRowPosition();

            Collection<StructuralDiff> rowDiffs = event.getRowDiffs();
            if (rowDiffs != null) {
                if (minimumOriginRowPosition < 0) {
                    // this is for handling of hide/show behaviour
                    // the value can only be -1 in case the row for which the
                    // minimum origin was set before
                    // was hidden, so we try to determine the correct value now
                    // if it is shown again
                    minimumOriginRowPosition = scrollableLayer
                            .getRowPositionByY(this.viewportLayer.getMinimumOrigin().getY());
                }
                for (StructuralDiff rowDiff : rowDiffs) {
                    DiffTypeEnum diffType = rowDiff.getDiffType();
                    if (diffType == DiffTypeEnum.ADD) {
                        Range afterPositionRange = rowDiff.getAfterPositionRange();
                        if (minimumOriginRowPosition > 0) {
                            int morp = minimumOriginRowPosition;
                            for (int i = afterPositionRange.start; i < afterPositionRange.end; i++) {
                                if (i < morp) {
                                    morp++;
                                    rowOffset += 1;
                                }
                            }
                        }
                    } else if (diffType == DiffTypeEnum.DELETE) {
                        Range beforePositionRange = rowDiff.getBeforePositionRange();
                        if (minimumOriginRowPosition > 0) {
                            for (int i = beforePositionRange.start; i < beforePositionRange.end; i++) {
                                if (i < minimumOriginRowPosition) {
                                    rowOffset -= 1;
                                }
                            }
                        }
                    }
                }
            }

            int minimumOriginRow = minimumOriginRowPosition + rowOffset;

            // in case of split viewports we use the min row position instead of
            // the calculated value
            if (this.viewportLayer.getMinRowPosition() >= 0) {
                minimumOriginRow = this.viewportLayer.getMinRowPosition();
            }

            // if the new origin is out of range (e.g. the last row in the
            // viewport is moved
            // to the frozen region, the minimum origin need to be updated in
            // another way
            int startY = scrollableLayer.getStartYOfRowPosition(minimumOriginRow);
            if (startY < 0 && minimumOriginRowPosition > 0) {
                int rowCount = scrollableLayer.getRowCount();
                if (rowCount == 0) {
                    // special case when all rows are hidden
                    startY = 0;
                } else {
                    startY = scrollableLayer.getStartYOfRowPosition(rowCount - 1)
                            + scrollableLayer.getRowHeightByPosition(rowCount - 1);
                }
            }
            this.viewportLayer.setMinimumOriginY(startY);
        }
    }

}

/*******************************************************************************
 * Copyright (c) 2012, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 446275 - deprecated because ISelectionModel
 *                                              is not itself the event handler
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.event;

import java.util.Collection;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.selection.ISelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @deprecated ISelectionModel is now itself a ILayerEventHandler
 */
@Deprecated
public class SelectionLayerStructuralChangeEventHandler implements ILayerEventHandler<IStructuralChangeEvent> {

    private final SelectionLayer selectionLayer;

    /**
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} this handler is operating on.
     *            Needed to clear selections and retrieve selection states while
     *            handling {@link IStructuralChangeEvent}s
     */
    public SelectionLayerStructuralChangeEventHandler(SelectionLayer selectionLayer) {
        this.selectionLayer = selectionLayer;
    }

    /**
     *
     * @param selectionLayer
     * @param selectionModel
     * @deprecated This handler doesn't make use of the ISelectionModel anymore,
     *             therefore setting another ISelectionModel will have no effect
     */
    @Deprecated
    public SelectionLayerStructuralChangeEventHandler(
            SelectionLayer selectionLayer, ISelectionModel selectionModel) {
        this.selectionLayer = selectionLayer;
    }

    @Override
    public Class<IStructuralChangeEvent> getLayerEventClass() {
        return IStructuralChangeEvent.class;
    }

    @Override
    public void handleLayerEvent(IStructuralChangeEvent event) {
        if (event.isHorizontalStructureChanged()) {
            // TODO handle column deletion
        }

        if (event.isVerticalStructureChanged()) {
            // if there are no row diffs, it seems to be a complete refresh
            if (event.getRowDiffs() == null) {
                Collection<Rectangle> rectangles = event
                        .getChangedPositionRectangles();
                for (Rectangle rectangle : rectangles) {
                    Range changedRange = new Range(rectangle.y, rectangle.y
                            + rectangle.height);
                    if (selectedRowModified(changedRange)) {
                        this.selectionLayer.clear();
                        break;
                    }
                }
            } else {
                // there are row diffs so we try to determine the diffs to
                // process
                for (StructuralDiff diff : event.getRowDiffs()) {
                    // DiffTypeEnum.CHANGE is used for resizing and shouldn't
                    // result in clearing the selection
                    if (diff.getDiffType() != DiffTypeEnum.CHANGE) {
                        if (selectedRowModified(diff.getBeforePositionRange())) {
                            this.selectionLayer.clear();
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean selectedRowModified(Range changedRange) {
        Set<Range> selectedRows = this.selectionLayer.getSelectedRowPositions();
        for (Range rowRange : selectedRows) {
            if (rowRange.overlap(changedRange)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param selectionModel
     * @deprecated This handler doesn't make use of the ISelectionModel anymore,
     *             therefore setting another ISelectionModel will have no effect
     */
    @Deprecated
    public void setSelectionModel(ISelectionModel selectionModel) {}

}

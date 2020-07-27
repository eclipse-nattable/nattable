/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.RowGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.RowGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroup;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroupModel;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;

public class RowGroupExpandCollapseCommandHandler<T> extends AbstractLayerCommandHandler<RowGroupExpandCollapseCommand> {

    private final RowGroupExpandCollapseLayer<T> rowGroupExpandCollapseLayer;

    public RowGroupExpandCollapseCommandHandler(RowGroupExpandCollapseLayer<T> rowGroupExpandCollapseLayer) {
        this.rowGroupExpandCollapseLayer = rowGroupExpandCollapseLayer;
    }

    @Override
    public Class<RowGroupExpandCollapseCommand> getCommandClass() {
        return RowGroupExpandCollapseCommand.class;
    }

    @Override
    protected boolean doCommand(RowGroupExpandCollapseCommand command) {

        int rowIndex = this.rowGroupExpandCollapseLayer.getRowIndexByPosition(command.getRowPosition());
        IRowGroupModel<T> model = this.rowGroupExpandCollapseLayer.getModel();
        IRowGroup<T> group = RowGroupUtils.getTopMostParentGroup(RowGroupUtils.getRowGroupForRowIndex(model, rowIndex));

        // if group of rowIndex is not collapseable return without any
        // further operation ...
        if (group != null && group.isCollapseable()) {
            boolean wasCollapsed = group.isCollapsed();

            if (wasCollapsed) {
                group.expand();
            } else {
                group.collapse();
            }

            int[] rowIndexes = RowGroupUtils.getRowIndexesInGroupAsArray(model, rowIndex);
            int[] rowPositions = RowGroupUtils.getRowPositionsInGroup(this.rowGroupExpandCollapseLayer, rowIndexes);

            ILayerEvent event;
            if (wasCollapsed) {
                event = new ShowRowPositionsEvent(this.rowGroupExpandCollapseLayer, rowPositions);
            } else {
                event = new HideRowPositionsEvent(this.rowGroupExpandCollapseLayer, rowPositions);
            }

            this.rowGroupExpandCollapseLayer.fireLayerEvent(event);
        }

        return true;
    }
}

/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.reorder.action.ColumnReorderDragMode;

/**
 * Specialization of {@link ColumnReorderDragMode} that avoids rendering of the
 * column drag lines in case the mouse moves over the groupBy region.
 */
public class GroupByColumnReorderDragMode extends ColumnReorderDragMode {

    @Override
    protected boolean isValidTargetColumnPosition(ILayer natLayer, int dragFromGridColumnPosition, int dragToGridColumnPosition) {
        // Suppress reorder if cursor is over the group by region
        LabelStack regionLabels = natLayer.getRegionLabelsByXY(this.currentEvent.x, this.currentEvent.y);
        if (regionLabels != null && !regionLabels.hasLabel(GroupByHeaderLayer.GROUP_BY_REGION)) {
            return super.isValidTargetColumnPosition(natLayer, dragFromGridColumnPosition, dragToGridColumnPosition);
        }
        return false;
    }

}

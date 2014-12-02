/*******************************************************************************
 * Copyright (c) 2012, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 446476
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByHeaderLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.command.GroupByColumnIndexCommand;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.swt.events.MouseEvent;

/**
 * IDragMode for enabling the groupBy feature. This IDragMode supports dragging
 * of a column to the groupBy region and executes the
 * {@link GroupByColumnIndexCommand} on mouseUp.
 * <p>
 * It is typically registered to the column header region.
 * </p>
 */
public class GroupByDragMode implements IDragMode {

    private int selectedColumnIndex = -1;

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        int columnPosition = natTable.getColumnPositionByX(event.x);
        this.selectedColumnIndex = natTable
                .getColumnIndexByPosition(columnPosition);
    }

    @Override
    public void mouseMove(NatTable natTable, MouseEvent event) {}

    @Override
    public void mouseUp(NatTable natTable, MouseEvent event) {
        LabelStack regionLabels = natTable
                .getRegionLabelsByXY(event.x, event.y);
        if (regionLabels != null
                && regionLabels.hasLabel(GroupByHeaderLayer.GROUP_BY_REGION)
                && this.selectedColumnIndex != -1) {
            natTable.doCommand(new GroupByColumnIndexCommand(
                    this.selectedColumnIndex));
            this.selectedColumnIndex = -1;
        }
    }

}

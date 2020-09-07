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
package org.eclipse.nebula.widgets.nattable.ui;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.swt.events.MouseEvent;

public class NatEventData {

    private MouseEvent originalEvent;
    private final NatTable natTable;
    private final LabelStack regionLabels;
    int columnPosition;
    int rowPosition;

    public static NatEventData createInstanceFromEvent(MouseEvent event) {
        NatTable natTable = (NatTable) event.widget;

        int columnPosition = natTable.getColumnPositionByX(event.x);
        int rowPosition = natTable.getRowPositionByY(event.y);

        return new NatEventData(natTable, natTable.getRegionLabelsByXY(event.x,
                event.y), columnPosition, rowPosition, event);
    }

    public NatEventData(NatTable natTable, LabelStack regionLabels,
            int columnPosition, int rowPosition, MouseEvent originalEvent) {
        this.natTable = natTable;
        this.regionLabels = regionLabels;
        this.columnPosition = columnPosition;
        this.rowPosition = rowPosition;
        this.originalEvent = originalEvent;
    }

    public NatTable getNatTable() {
        return this.natTable;
    }

    public LabelStack getRegionLabels() {
        return this.regionLabels;
    }

    public int getColumnPosition() {
        return this.columnPosition;
    }

    public int getRowPosition() {
        return this.rowPosition;
    }

    public MouseEvent getOriginalEvent() {
        return this.originalEvent;
    }

}

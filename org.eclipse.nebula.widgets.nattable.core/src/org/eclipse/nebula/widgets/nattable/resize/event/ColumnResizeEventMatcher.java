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
package org.eclipse.nebula.widgets.nattable.resize.event;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

public class ColumnResizeEventMatcher extends MouseEventMatcher {

    public ColumnResizeEventMatcher(int stateMask, String eventRegion, int button) {
        super(stateMask, eventRegion, button);
    }

    @Override
    public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
        return super.matches(natTable, event, regionLabels) && isColumnResizable(natTable, event);
    }

    private boolean isColumnResizable(ILayer natLayer, MouseEvent event) {
        int columnPosition = CellEdgeDetectUtil.getColumnPositionToResize(natLayer, new Point(event.x, event.y));

        if (columnPosition < 0) {
            return false;
        } else {
            return natLayer.isColumnPositionResizable(columnPosition);
        }
    }

}

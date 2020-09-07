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
package org.eclipse.nebula.widgets.nattable.filterrow.event;

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotNull;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IMouseEventMatcher;
import org.eclipse.swt.events.MouseEvent;

public class FilterRowMouseEventMatcher implements IMouseEventMatcher {

    @Override
    public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
        NatEventData eventData = NatEventData.createInstanceFromEvent(event);
        LabelStack labels = eventData.getRegionLabels();

        if (isNotNull(labels)) {
            return labels.contains(GridRegion.FILTER_ROW);
        }
        return false;
    }
}

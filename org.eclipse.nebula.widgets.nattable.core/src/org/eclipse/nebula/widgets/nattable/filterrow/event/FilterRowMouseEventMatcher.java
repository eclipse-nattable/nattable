/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.events.MouseEvent;

public class FilterRowMouseEventMatcher implements IMouseEventMatcher {

    private final int button;

    /**
     * Creates a {@link FilterRowMouseEventMatcher} that checks for the
     * {@link GridRegion#FILTER_ROW} in the region labels.
     */
    public FilterRowMouseEventMatcher() {
        this(0);
    }

    /**
     * Creates a {@link FilterRowMouseEventMatcher} that checks for the
     * {@link GridRegion#FILTER_ROW} in the region labels and additionally
     * checks for the given mouse button.
     *
     * @param button
     *            The mouse button that should be additionally checked, e.g.
     *            {@link MouseEventMatcher#LEFT_BUTTON}
     *
     * @since 2.2
     */
    public FilterRowMouseEventMatcher(int button) {
        this.button = button;
    }

    @Override
    public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
        NatEventData eventData = NatEventData.createInstanceFromEvent(event);
        LabelStack labels = eventData.getRegionLabels();

        if (isNotNull(labels) && (this.button == 0 || event.button == this.button)) {
            return labels.contains(GridRegion.FILTER_ROW);
        }
        return false;
    }
}

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

import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;

/**
 * Special {@link RowStructuralRefreshEvent} that is fired in case the filter
 * changes via {@link FilterRowDataProvider}. This includes clearing a filter.
 */
public class FilterAppliedEvent extends RowStructuralRefreshEvent {

    public FilterAppliedEvent(ILayer layer) {
        super(layer);
    }

}

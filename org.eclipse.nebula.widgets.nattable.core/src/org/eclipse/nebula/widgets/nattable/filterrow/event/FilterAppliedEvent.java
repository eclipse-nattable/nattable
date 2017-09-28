/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

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
package org.eclipse.nebula.widgets.nattable.hover.config;

import org.eclipse.nebula.widgets.nattable.config.AggregateConfiguration;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultColumnHeaderStyleConfiguration;

/**
 * Setup for the column header area to support column resizing and hover
 * styling.
 */
public class ColumnHeaderHoverLayerConfiguration extends AggregateConfiguration {

    public ColumnHeaderHoverLayerConfiguration(HoverLayer layer) {
        addColumnHeaderStyleConfig();
        addColumnHeaderUIBindings(layer);
    }

    protected void addColumnHeaderStyleConfig() {
        addConfiguration(new DefaultColumnHeaderStyleConfiguration());
    }

    protected void addColumnHeaderUIBindings(HoverLayer layer) {
        addConfiguration(new ColumnHeaderResizeHoverBindings(layer));
    }
}

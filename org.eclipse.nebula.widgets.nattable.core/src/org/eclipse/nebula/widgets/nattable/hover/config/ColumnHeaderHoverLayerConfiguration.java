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
package org.eclipse.nebula.widgets.nattable.hover.config;

import org.eclipse.nebula.widgets.nattable.config.AggregateConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultColumnHeaderStyleConfiguration;

/**
 * Setup for the column header area to support column resizing and hover
 * styling. Needs to be registered with the {@link ColumnHeaderLayer} instead of
 * the default configuration to work correctly.
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

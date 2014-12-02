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
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderStyleConfiguration;

/**
 * Setup for the row header area to support row resizing and hover styling.
 */
public class RowHeaderHoverLayerConfiguration extends AggregateConfiguration {

    public RowHeaderHoverLayerConfiguration(HoverLayer layer) {
        addRowHeaderStyleConfig();
        addRowHeaderUIBindings(layer);
    }

    protected void addRowHeaderStyleConfig() {
        addConfiguration(new DefaultRowHeaderStyleConfiguration());
    }

    protected void addRowHeaderUIBindings(HoverLayer layer) {
        addConfiguration(new RowHeaderResizeHoverBindings(layer));
    }

}

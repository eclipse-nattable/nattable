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
package org.eclipse.nebula.widgets.nattable.layer.config;

import org.eclipse.nebula.widgets.nattable.config.AggregateConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.resize.config.DefaultColumnResizeBindings;

/**
 * Sets up Column header styling and resize bindings. Added by the
 * {@link ColumnHeaderLayer}
 */
public class DefaultColumnHeaderLayerConfiguration extends
        AggregateConfiguration {

    public DefaultColumnHeaderLayerConfiguration() {
        addColumnHeaderStyleConfig();
        addColumnHeaderUIBindings();
    }

    protected void addColumnHeaderUIBindings() {
        addConfiguration(new DefaultColumnResizeBindings());
    }

    protected void addColumnHeaderStyleConfig() {
        addConfiguration(new DefaultColumnHeaderStyleConfiguration());
    }

}

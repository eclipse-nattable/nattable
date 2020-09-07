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
package org.eclipse.nebula.widgets.nattable.layer.config;

import org.eclipse.nebula.widgets.nattable.config.AggregateConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.resize.config.DefaultColumnResizeBindings;

/**
 * Sets up Column header styling and resize bindings. Added by the
 * {@link ColumnHeaderLayer}
 */
public class DefaultColumnHeaderLayerConfiguration extends AggregateConfiguration {

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

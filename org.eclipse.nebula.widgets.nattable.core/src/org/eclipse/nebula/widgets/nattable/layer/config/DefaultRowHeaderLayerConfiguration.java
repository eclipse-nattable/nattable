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
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.resize.config.DefaultRowResizeBindings;

/**
 * Default setup for the Row header area. Added by the {@link RowHeaderLayer}
 * Override the methods in this class to customize style / UI bindings.
 *
 * @see GridRegion
 */
public class DefaultRowHeaderLayerConfiguration extends AggregateConfiguration {

    public DefaultRowHeaderLayerConfiguration() {
        addRowHeaderStyleConfig();
        addRowHeaderUIBindings();
    }

    protected void addRowHeaderStyleConfig() {
        addConfiguration(new DefaultRowHeaderStyleConfiguration());
    }

    protected void addRowHeaderUIBindings() {
        addConfiguration(new DefaultRowResizeBindings());
    }

}

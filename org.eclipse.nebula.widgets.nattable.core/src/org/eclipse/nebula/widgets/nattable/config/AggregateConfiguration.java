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
package org.eclipse.nebula.widgets.nattable.config;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;

/**
 * Aggregates {@link IConfiguration} objects and invokes configure methods on
 * all its members.
 */
public class AggregateConfiguration implements IConfiguration {

    private final Collection<IConfiguration> configurations = new LinkedList<IConfiguration>();

    public void addConfiguration(IConfiguration configuration) {
        this.configurations.add(configuration);
    }

    @Override
    public void configureLayer(ILayer layer) {
        for (IConfiguration configuration : this.configurations) {
            configuration.configureLayer(layer);
        }
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        for (IConfiguration configuration : this.configurations) {
            configuration.configureRegistry(configRegistry);
        }
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        for (IConfiguration configuration : this.configurations) {
            configuration.configureUiBindings(uiBindingRegistry);
        }
    }

}

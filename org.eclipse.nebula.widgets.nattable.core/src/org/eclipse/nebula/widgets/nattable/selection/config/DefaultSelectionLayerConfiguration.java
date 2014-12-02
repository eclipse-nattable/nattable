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
package org.eclipse.nebula.widgets.nattable.selection.config;

import org.eclipse.nebula.widgets.nattable.config.AggregateConfiguration;
import org.eclipse.nebula.widgets.nattable.search.config.DefaultSearchBindings;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.tickupdate.config.DefaultTickUpdateConfiguration;

/**
 * Sets up default styling and UI bindings. Override the methods in here to
 * customize behavior. Added by the {@link SelectionLayer}
 */
public class DefaultSelectionLayerConfiguration extends AggregateConfiguration {

    public DefaultSelectionLayerConfiguration() {
        addSelectionStyleConfig();
        addSelectionUIBindings();
        addSearchUIBindings();
        addTickUpdateConfig();
        addMoveSelectionConfig();
    }

    protected void addSelectionStyleConfig() {
        addConfiguration(new DefaultSelectionStyleConfiguration());
    }

    protected void addSelectionUIBindings() {
        addConfiguration(new DefaultSelectionBindings());
    }

    protected void addSearchUIBindings() {
        addConfiguration(new DefaultSearchBindings());
    }

    protected void addTickUpdateConfig() {
        addConfiguration(new DefaultTickUpdateConfiguration());
    }

    protected void addMoveSelectionConfig() {
        addConfiguration(new DefaultMoveSelectionConfiguration());
    }
}

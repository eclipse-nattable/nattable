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
package org.eclipse.nebula.widgets.nattable.config;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;

/**
 * Casts the layer to be to the type parameter for convenience.
 *
 * @param <L>
 *            type of the layer being configured
 */
public abstract class AbstractLayerConfiguration<L extends ILayer> implements
        IConfiguration {

    @Override
    @SuppressWarnings("unchecked")
    public void configureLayer(ILayer layer) {
        configureTypedLayer((L) layer);
    }

    public abstract void configureTypedLayer(L layer);

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {}

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {}

}

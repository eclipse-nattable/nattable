/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.config;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;

/**
 * Configurations can be added to NatTable/ILayer to modify default behavior.
 * These will be processed when {@link NatTable#configure()} is invoked.
 *
 * Default configurations are added to most layers
 * {@link AbstractLayer#addConfiguration(IConfiguration)}. You can turn off
 * default configuration for an {@link ILayer} by setting auto configure to
 * false in the constructor.
 */
public interface IConfiguration {

    public void configureLayer(ILayer layer);

    /**
     * Configure NatTable's {@link IConfigRegistry} upon receiving this call
     * back. A mechanism to plug-in custom {@link ICellPainter},
     * {@link IDataValidator} etc.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} instance to register configuration
     *            values to.
     */
    public void configureRegistry(IConfigRegistry configRegistry);

    /**
     * Configure NatTable's {@link IConfigRegistry} upon receiving this call
     * back A mechanism to customize key/mouse bindings.
     *
     * @param uiBindingRegistry
     *            The {@link UiBindingRegistry} instance to register ui bindings
     *            to.
     */
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry);

}

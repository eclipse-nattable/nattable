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
 * {@link AbstractLayer#addConfiguration(IConfiguration)}. Typically you can
 * turn off default configurations for an {@link ILayer} by setting
 * autoconfigure to <code>false</code> in the constructor.
 */
public interface IConfiguration {

    /**
     * Perform configurations on the provided layer.
     *
     * @param layer
     *            The {@link ILayer} to configure.
     */
    public void configureLayer(ILayer layer);

    /**
     * Configure NatTable's {@link IConfigRegistry} upon receiving this
     * callback. A mechanism to plug-in custom {@link ICellPainter},
     * {@link IDataValidator} etc.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} instance to register configuration
     *            values to.
     */
    public void configureRegistry(IConfigRegistry configRegistry);

    /**
     * Configure NatTable's {@link UiBindingRegistry} upon receiving this
     * callback. A mechanism to customize key/mouse bindings.
     *
     * @param uiBindingRegistry
     *            The {@link UiBindingRegistry} instance to register ui bindings
     *            to.
     */
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry);

}

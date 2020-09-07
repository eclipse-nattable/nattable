/*******************************************************************************
 * Copyright (c) 2017, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thanh Liem PHAN (ALL4TEC) <thanhliem.phan@all4tec.net> - Bug 509361
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.export.image.config;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.export.ExportConfigAttributes;
import org.eclipse.nebula.widgets.nattable.export.action.ExportTableAction;
import org.eclipse.nebula.widgets.nattable.export.command.ExportTableCommandHandler;
import org.eclipse.nebula.widgets.nattable.export.image.ImageExporter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.swt.SWT;

/**
 * Configuration for binding image export function.
 *
 * @since 1.5
 */
public class DefaultImageExportBindings implements IConfiguration {

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // Bind the key event CTRL+i to the ImageExportAction
        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, 'i'),
                new ExportTableAction());
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(
                ExportConfigAttributes.TABLE_EXPORTER,
                new ImageExporter());
    }

    @Override
    public void configureLayer(ILayer layer) {
        layer.registerCommandHandler(new ExportTableCommandHandler(layer));
    }
}

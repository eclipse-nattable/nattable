/*******************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.datachange.config;

import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.datachange.DataChangeLayer;
import org.eclipse.nebula.widgets.nattable.datachange.command.DiscardDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.datachange.command.SaveDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;

/**
 * Default configuration for data changes to register key bindings for data
 * change operations like discard or save and a default style to highlight data
 * changes.
 *
 * @since 1.6
 */
public class DefaultDataChangeConfiguration extends AbstractUiBindingConfiguration {

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        Style style = new Style();
        style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_BLUE);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                style,
                DisplayMode.NORMAL,
                DataChangeLayer.DIRTY);
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, 's'),
                (natTable, event) -> natTable.doCommand(new SaveDataChangesCommand()));
        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, 'd'),
                (natTable, event) -> natTable.doCommand(new DiscardDataChangesCommand()));

    }

}

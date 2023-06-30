/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.filterrow.config;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.action.MouseEditAction;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowPainter;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.action.ClearFilterAction;
import org.eclipse.nebula.widgets.nattable.filterrow.action.ToggleFilterRowAction;
import org.eclipse.nebula.widgets.nattable.filterrow.event.ClearFilterIconMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.filterrow.event.FilterRowMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.viewport.action.ShowColumnInViewportAction;
import org.eclipse.swt.SWT;

//fields are public by design to make it easy for adapters to customize configuration
@SuppressWarnings("java:S1104")
public class DefaultFilterRowConfiguration extends AbstractRegistryConfiguration {

    public FilterRowPainter cellPainter = new FilterRowPainter();
    public TextMatchingMode textMatchingMode = TextMatchingMode.CONTAINS;
    public int showHideKeyConstant = SWT.F3;

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        // Plug in custom painter
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER,
                new PaddingDecorator(this.cellPainter, 0, 0, 0, 5),
                DisplayMode.NORMAL,
                GridRegion.FILTER_ROW);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.RENDER_GRID_LINES,
                Boolean.TRUE,
                DisplayMode.NORMAL,
                GridRegion.FILTER_ROW);

        // Make cells editable
        configRegistry.registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                IEditableRule.ALWAYS_EDITABLE,
                DisplayMode.NORMAL,
                GridRegion.FILTER_ROW);

        // Default text matching mode
        configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                this.textMatchingMode);

        // Default display converter. Used to convert the values typed into the
        // text boxes into String objects.
        configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                new DefaultDisplayConverter());

        // Default comparator. Used to compare objects in the column during
        // threshold matching.
        configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.FILTER_COMPARATOR,
                DefaultComparator.getInstance());
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        uiBindingRegistry.registerFirstMouseDownBinding(
                new FilterRowMouseEventMatcher(MouseEventMatcher.LEFT_BUTTON),
                new ShowColumnInViewportAction());
        uiBindingRegistry.registerFirstSingleClickBinding(
                new FilterRowMouseEventMatcher(MouseEventMatcher.LEFT_BUTTON),
                new MouseEditAction());
        uiBindingRegistry.registerFirstSingleClickBinding(
                new ClearFilterIconMouseEventMatcher(this.cellPainter),
                new ClearFilterAction());
        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(this.showHideKeyConstant),
                new ToggleFilterRowAction());
    }
}

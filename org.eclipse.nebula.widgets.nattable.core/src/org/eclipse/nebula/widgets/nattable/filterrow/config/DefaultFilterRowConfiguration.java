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
package org.eclipse.nebula.widgets.nattable.filterrow.config;

import static org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes.CELL_PAINTER;
import static org.eclipse.nebula.widgets.nattable.config.IEditableRule.ALWAYS_EDITABLE;
import static org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes.CELL_EDITABLE_RULE;
import static org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode.CONTAINS;
import static org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes.FILTER_COMPARATOR;
import static org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER;
import static org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes.TEXT_MATCHING_MODE;
import static org.eclipse.nebula.widgets.nattable.grid.GridRegion.FILTER_ROW;
import static org.eclipse.nebula.widgets.nattable.style.DisplayMode.NORMAL;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.action.MouseEditAction;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowPainter;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.action.ClearFilterAction;
import org.eclipse.nebula.widgets.nattable.filterrow.action.ToggleFilterRowAction;
import org.eclipse.nebula.widgets.nattable.filterrow.event.ClearFilterIconMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.filterrow.event.FilterRowMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.swt.SWT;

public class DefaultFilterRowConfiguration extends AbstractRegistryConfiguration {

	public FilterRowPainter cellPainter = new FilterRowPainter();
	public TextMatchingMode textMatchingMode = CONTAINS;
	public int showHideKeyConstant = SWT.F3;

	public void configureRegistry(IConfigRegistry configRegistry) {
		// Plug in custom painter
		configRegistry.registerConfigAttribute(CELL_PAINTER, cellPainter, NORMAL, FILTER_ROW);

		// Make cells editable
		configRegistry.registerConfigAttribute(CELL_EDITABLE_RULE, ALWAYS_EDITABLE, NORMAL, FILTER_ROW);

		// Default text matching mode
		configRegistry.registerConfigAttribute(TEXT_MATCHING_MODE, textMatchingMode);

		// Default display converter. Used to convert the values typed into the text boxes into String objects.
		configRegistry.registerConfigAttribute(FILTER_DISPLAY_CONVERTER, new DefaultDisplayConverter());

		// Default comparator. Used to compare objects in the column during threshold matching.
		configRegistry.registerConfigAttribute(FILTER_COMPARATOR, DefaultComparator.getInstance());
	}

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerFirstSingleClickBinding(new FilterRowMouseEventMatcher(), new MouseEditAction());
		uiBindingRegistry.registerFirstSingleClickBinding(new ClearFilterIconMouseEventMatcher(cellPainter), new ClearFilterAction());
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(showHideKeyConstant), new ToggleFilterRowAction());
	}
}

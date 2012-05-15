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
package org.eclipse.nebula.widgets.nattable.extension.builder.configuration;


import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.action.SortColumnAction;
import org.eclipse.nebula.widgets.nattable.sort.event.ColumnHeaderClickEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

public class SortConfiguration extends AbstractUiBindingConfiguration {

	/**
	 * Remove the original key bindings and implement new ones.
	 */
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		// Register single click bindings
		uiBindingRegistry.registerSingleClickBinding(
              new ColumnHeaderClickEventMatcher(SWT.NONE, 1), new SortColumnAction(false));

		uiBindingRegistry.registerSingleClickBinding(
             MouseEventMatcher.columnHeaderLeftClick(SWT.ALT), new SortColumnAction(true));
	}
}

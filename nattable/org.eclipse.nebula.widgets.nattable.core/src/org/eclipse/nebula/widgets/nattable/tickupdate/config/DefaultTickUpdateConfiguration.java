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
package org.eclipse.nebula.widgets.nattable.tickupdate.config;

import org.eclipse.nebula.widgets.nattable.config.AbstractLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.tickupdate.ITickUpdateHandler;
import org.eclipse.nebula.widgets.nattable.tickupdate.TickUpdateConfigAttributes;
import org.eclipse.nebula.widgets.nattable.tickupdate.action.TickUpdateAction;
import org.eclipse.nebula.widgets.nattable.tickupdate.command.TickUpdateCommandHandler;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.swt.SWT;


public class DefaultTickUpdateConfiguration extends AbstractLayerConfiguration<SelectionLayer> {
	
	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(TickUpdateConfigAttributes.UPDATE_HANDLER, ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER);
	}

	@Override
	public void configureTypedLayer(SelectionLayer selectionLayer) {
		selectionLayer.registerCommandHandler(new TickUpdateCommandHandler(selectionLayer));
	}
	
	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerKeyBinding(
				new KeyEventMatcher(SWT.NONE, SWT.KEYPAD_ADD), 
				new TickUpdateAction(true));

		uiBindingRegistry.registerKeyBinding(
				new KeyEventMatcher(SWT.NONE, SWT.KEYPAD_SUBTRACT), 
				new TickUpdateAction(false));
	}
	
}

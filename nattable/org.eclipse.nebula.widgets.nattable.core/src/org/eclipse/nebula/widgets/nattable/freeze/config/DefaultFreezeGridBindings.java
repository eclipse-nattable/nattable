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
package org.eclipse.nebula.widgets.nattable.freeze.config;


import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.freeze.action.FreezeGridAction;
import org.eclipse.nebula.widgets.nattable.freeze.action.UnFreezeGridAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.swt.SWT;

public class DefaultFreezeGridBindings extends AbstractUiBindingConfiguration {

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL | SWT.SHIFT, 'f'), new FreezeGridAction());
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL | SWT.SHIFT, 'u'), new UnFreezeGridAction());
	}
}

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
package org.eclipse.nebula.widgets.nattable.extension.builder.configuration;


import org.eclipse.nebula.widgets.nattable.selection.action.MoveToFirstRowAction;
import org.eclipse.nebula.widgets.nattable.selection.action.MoveToLastRowAction;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionBindings;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

public class RowSelectionUIBindings extends RowOnlySelectionBindings {

	@Override
	protected void configureMoveDownBindings(UiBindingRegistry uiBindingRegistry, IKeyAction action) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_DOWN), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_DOWN), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.MOD1, SWT.ARROW_DOWN), new MoveToLastRowAction());
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.MOD1, SWT.ARROW_DOWN), new MoveToLastRowAction());
	}

	@Override
	protected void configureMoveUpBindings(UiBindingRegistry uiBindingRegistry,	IKeyAction action) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_UP), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_UP), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.MOD1, SWT.ARROW_UP), new MoveToFirstRowAction());
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.MOD1, SWT.ARROW_UP), new MoveToFirstRowAction());
	}

	@Override
	protected void configureBodyMouseDragMode(UiBindingRegistry uiBindingRegistry) {
		IDragMode dragMode = new SingleRowSelectionDragMode();
		uiBindingRegistry.registerFirstMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.NONE), dragMode);
		uiBindingRegistry.registerFirstMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.SHIFT), dragMode);
		uiBindingRegistry.registerFirstMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.MOD1), dragMode);
		uiBindingRegistry.registerFirstMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.SHIFT | SWT.MOD1), dragMode);
	}

}

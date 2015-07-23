/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 447259, 459029
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.config;

import org.eclipse.nebula.widgets.nattable.selection.action.RowSelectionDragMode;
import org.eclipse.nebula.widgets.nattable.selection.action.SelectRowAction;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

public class RowOnlySelectionBindings extends DefaultSelectionBindings {

    @Override
    protected void configureBodyMouseClickBindings(UiBindingRegistry uiBindingRegistry) {
        IMouseAction action = new SelectRowAction();
        uiBindingRegistry.registerFirstMouseDownBinding(
                MouseEventMatcher.bodyLeftClick(SWT.NONE), action);
        uiBindingRegistry.registerFirstMouseDownBinding(
                MouseEventMatcher.bodyLeftClick(SWT.MOD2), action);
        uiBindingRegistry.registerFirstMouseDownBinding(
                MouseEventMatcher.bodyLeftClick(SWT.MOD1), action);
        uiBindingRegistry.registerFirstMouseDownBinding(
                MouseEventMatcher.bodyLeftClick(SWT.MOD2 | SWT.MOD1), action);
    }

    @Override
    protected void configureBodyMouseDragMode(UiBindingRegistry uiBindingRegistry) {
        IDragMode dragMode = new RowSelectionDragMode();
        uiBindingRegistry.registerFirstMouseDragMode(
                MouseEventMatcher.bodyLeftClick(SWT.NONE), dragMode);
        uiBindingRegistry.registerFirstMouseDragMode(
                MouseEventMatcher.bodyLeftClick(SWT.MOD2), dragMode);
        uiBindingRegistry.registerFirstMouseDragMode(
                MouseEventMatcher.bodyLeftClick(SWT.MOD1), dragMode);
        uiBindingRegistry.registerFirstMouseDragMode(
                MouseEventMatcher.bodyLeftClick(SWT.MOD2 | SWT.MOD1), dragMode);
    }
}

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
package org.eclipse.nebula.widgets.nattable.resize.config;

import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.resize.action.AutoResizeRowAction;
import org.eclipse.nebula.widgets.nattable.resize.action.RowResizeCursorAction;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEventMatcher;
import org.eclipse.nebula.widgets.nattable.resize.mode.RowResizeDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.ClearCursorAction;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

public class DefaultRowResizeBindings extends AbstractUiBindingConfiguration {

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // Mouse move - Show resize cursor
        uiBindingRegistry.registerFirstMouseMoveBinding(
                new RowResizeEventMatcher(SWT.NONE, 0),
                new RowResizeCursorAction());
        uiBindingRegistry.registerMouseMoveBinding(
                new MouseEventMatcher(),
                new ClearCursorAction());

        // Row resize
        uiBindingRegistry.registerFirstMouseDragMode(
                new RowResizeEventMatcher(SWT.NONE, 1),
                new RowResizeDragMode());

        uiBindingRegistry.registerDoubleClickBinding(
                new RowResizeEventMatcher(SWT.NONE, 1),
                new AutoResizeRowAction());
        uiBindingRegistry.registerSingleClickBinding(
                new RowResizeEventMatcher(SWT.NONE, 1),
                new NoOpMouseAction());
    }

}

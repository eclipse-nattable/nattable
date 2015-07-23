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
package org.eclipse.nebula.widgets.nattable.edit.config;

import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.action.CellEditDragMode;
import org.eclipse.nebula.widgets.nattable.edit.action.KeyEditAction;
import org.eclipse.nebula.widgets.nattable.edit.action.MouseEditAction;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellEditorMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.LetterOrDigitKeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

/**
 * Default configuration for edit related bindings. Adds bindings that support
 * opening cell editors via keypress and mouse click.
 * <p>
 * By default {@link GridRegion#BODY} is used for the matchers to evaluate if an
 * editor should be activated. By doing this only the editing in the body layer
 * stack of a grid is enabled.
 * </p>
 * <p>
 * Note: For typical {@link ICellEditor}s there is no special registering
 * necessary like it was previous to 1.0.0. Only {@link ICellEditor}s that
 * return <code>false</code> for {@link ICellEditor#activateAtAnyPosition()}
 * need to register a custom {@link IMouseEventMatcher} to determine whether to
 * activate the editor or not, regarding the correct position. This is for
 * example necessary for the {@link CheckboxCellEditor} that is configured with
 * the corresponding {@link CheckBoxPainter}, so the editor is only activated if
 * the checkbox item is clicked, not any other position in the cell.
 * </p>
 */
public class DefaultEditBindings extends AbstractUiBindingConfiguration {

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // configure the space key to activate a cell editor via keyboard
        // this is especially useful for changing the value for a checkbox
        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.NONE, 32),
                new KeyEditAction());
        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.NONE, SWT.F2),
                new KeyEditAction());
        uiBindingRegistry.registerKeyBinding(
                new LetterOrDigitKeyEventMatcher(),
                new KeyEditAction());
        uiBindingRegistry.registerKeyBinding(
                new LetterOrDigitKeyEventMatcher(SWT.MOD2),
                new KeyEditAction());

        uiBindingRegistry.registerSingleClickBinding(
                new CellEditorMouseEventMatcher(GridRegion.BODY),
                new MouseEditAction());

        uiBindingRegistry.registerMouseDragMode(
                new CellEditorMouseEventMatcher(GridRegion.BODY),
                new CellEditDragMode());

        uiBindingRegistry.registerFirstSingleClickBinding(
                new CellPainterMouseEventMatcher(GridRegion.BODY, MouseEventMatcher.LEFT_BUTTON, CheckBoxPainter.class),
                new MouseEditAction());

        uiBindingRegistry.registerFirstMouseDragMode(
                new CellPainterMouseEventMatcher(GridRegion.BODY, MouseEventMatcher.LEFT_BUTTON, CheckBoxPainter.class),
                new CellEditDragMode());

    }

}

/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.fillhandle.config;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.copy.command.InternalCopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.fillhandle.FillHandleLayerPainter;
import org.eclipse.nebula.widgets.nattable.fillhandle.action.FillHandleCursorAction;
import org.eclipse.nebula.widgets.nattable.fillhandle.action.FillHandleDragMode;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommandHandler;
import org.eclipse.nebula.widgets.nattable.fillhandle.event.FillHandleEventMatcher;
import org.eclipse.nebula.widgets.nattable.fillhandle.event.FillHandleMarkupListener;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.action.ClearCursorAction;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;

/**
 * Default configuration for fill handle functionality. Registers the
 * corresponding painter, command handler and ui bindings.
 *
 * @since 1.4
 */
public class FillHandleConfiguration extends AbstractLayerConfiguration<NatTable> {

    protected SelectionLayer selectionLayer;

    protected FillHandleLayerPainter painter;

    protected InternalCellClipboard clipboard;

    /**
     * Create the FillHandleConfiguration for a NatTable.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} needed to determine the current
     *            selection on which the fill handle will be rendered. Can not
     *            be <code>null</code>.
     */
    public FillHandleConfiguration(SelectionLayer selectionLayer) {
        if (selectionLayer == null) {
            throw new IllegalArgumentException("SelectionLayer can not be null"); //$NON-NLS-1$
        }
        this.selectionLayer = selectionLayer;
    }

    @Override
    public void configureTypedLayer(NatTable natTable) {
        // initialization works here because configureLayer() is executed before
        // configureUiBindings()
        this.clipboard = natTable.getInternalCellClipboard();

        this.painter = new FillHandleLayerPainter();
        this.selectionLayer.setLayerPainter(this.painter);

        this.selectionLayer.addLayerListener(new FillHandleMarkupListener(this.selectionLayer));

        this.selectionLayer.registerCommandHandler(
                new InternalCopyDataCommandHandler(this.selectionLayer, this.clipboard));
        this.selectionLayer.registerCommandHandler(
                new FillHandlePasteCommandHandler(this.selectionLayer, this.clipboard));
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        FillHandleEventMatcher matcher = new FillHandleEventMatcher(this.painter);

        // Mouse move
        // Show fill handle cursor
        uiBindingRegistry.registerFirstMouseMoveBinding(
                matcher,
                new FillHandleCursorAction());
        uiBindingRegistry.registerMouseMoveBinding(
                new MouseEventMatcher(),
                new ClearCursorAction());

        // Mouse drag
        // trigger the handle drag operations
        uiBindingRegistry.registerFirstMouseDragMode(
                matcher,
                new FillHandleDragMode(this.selectionLayer, this.clipboard));

        // Mouse click
        // ensure no selection is triggered on mouse down on the handle
        uiBindingRegistry.registerFirstMouseDownBinding(
                matcher,
                new NoOpMouseAction());
    }

}

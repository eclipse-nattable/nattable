/*****************************************************************************
 * Copyright (c) 2015, 2024 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.fillhandle.config;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.copy.command.InternalCopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.fillhandle.FillHandleBoundsProvider;
import org.eclipse.nebula.widgets.nattable.fillhandle.FillHandleLayerPainter;
import org.eclipse.nebula.widgets.nattable.fillhandle.action.FillHandleColumnAction;
import org.eclipse.nebula.widgets.nattable.fillhandle.action.FillHandleCursorAction;
import org.eclipse.nebula.widgets.nattable.fillhandle.action.FillHandleDragMode;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommandHandler;
import org.eclipse.nebula.widgets.nattable.fillhandle.event.FillHandleEventMatcher;
import org.eclipse.nebula.widgets.nattable.fillhandle.event.FillHandleMarkupListener;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.action.ClearCursorAction;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;

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

    private FillHandleBoundsProvider fillHandleBoundsProvider;

    /**
     * Create the FillHandleConfiguration for a NatTable.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} needed to determine the current
     *            selection on which the fill handle will be rendered. Can not
     *            be <code>null</code>.
     */
    public FillHandleConfiguration(SelectionLayer selectionLayer) {
        this(selectionLayer, null);
    }

    /**
     * Create the FillHandleConfiguration for a NatTable.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} needed to determine the current
     *            selection on which the fill handle will be rendered. Can not
     *            be <code>null</code>.
     * @param boundsProvider
     *            The {@link FillHandleBoundsProvider} that is used to determine
     *            the fill handle bounds. Can be <code>null</code> in which case
     *            the {@link FillHandleLayerPainter} is used that is created in
     *            {@link #configureTypedLayer(NatTable)}
     * @since 2.5
     */
    public FillHandleConfiguration(SelectionLayer selectionLayer, FillHandleBoundsProvider boundsProvider) {
        if (selectionLayer == null) {
            throw new IllegalArgumentException("SelectionLayer can not be null"); //$NON-NLS-1$
        }
        this.selectionLayer = selectionLayer;
        this.fillHandleBoundsProvider = boundsProvider;
    }

    @Override
    public void configureTypedLayer(NatTable natTable) {
        // initialization works here because configureLayer() is executed before
        // configureUiBindings()
        this.clipboard = natTable.getInternalCellClipboard();

        this.painter = new FillHandleLayerPainter(this.clipboard);
        this.selectionLayer.setLayerPainter(this.painter);

        this.selectionLayer.addLayerListener(new FillHandleMarkupListener(this.selectionLayer));

        this.selectionLayer.registerCommandHandler(
                new InternalCopyDataCommandHandler(this.selectionLayer, this.clipboard));
        this.selectionLayer.registerCommandHandler(
                new FillHandlePasteCommandHandler(this.selectionLayer, this.clipboard));
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        FillHandleEventMatcher matcher = this.fillHandleBoundsProvider != null
                ? new FillHandleEventMatcher(this.fillHandleBoundsProvider)
                : new FillHandleEventMatcher((FillHandleBoundsProvider) this.painter);

        // Mouse move
        // Show fill handle cursor
        uiBindingRegistry.registerFirstMouseMoveBinding(
                matcher,
                new FillHandleCursorAction(),
                new ClearCursorAction());

        // Mouse drag
        // trigger the handle drag operations
        uiBindingRegistry.registerFirstMouseDragMode(
                matcher,
                new FillHandleDragMode(this.selectionLayer, this.clipboard));

        // Mouse double click
        // trigger the handle double click operation
        uiBindingRegistry.registerDoubleClickBinding(
                matcher,
                new FillHandleColumnAction(this.selectionLayer, this.clipboard));

        // Mouse click
        // ensure no selection is triggered on mouse down on the handle
        uiBindingRegistry.registerFirstMouseDownBinding(
                matcher,
                new NoOpMouseAction());
    }

}

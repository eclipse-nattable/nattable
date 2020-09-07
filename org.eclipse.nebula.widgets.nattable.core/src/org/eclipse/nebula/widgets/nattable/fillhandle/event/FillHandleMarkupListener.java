/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
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
package org.eclipse.nebula.widgets.nattable.fillhandle.event;

import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.event.ISelectionEvent;

/**
 * {@link ILayerListener} that will trigger the markup in the
 * {@link SelectionLayer} of the cell that will hold the fill handle.
 *
 * @since 1.4
 */
public class FillHandleMarkupListener implements ILayerListener {

    private final SelectionLayer selectionLayer;

    public FillHandleMarkupListener(SelectionLayer selectionLayer) {
        this.selectionLayer = selectionLayer;
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof ISelectionEvent) {
            FillHandleMarkupListener.this.selectionLayer.markFillHandleCell();
        }
    }

}

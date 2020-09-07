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
package org.eclipse.nebula.widgets.nattable.copy;

import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEvent;

/**
 * {@link ILayerListener} that clears the {@link InternalCellClipboard} if an
 * {@link IStructuralChangeEvent} happens.
 *
 * @since 1.4
 */
public class InternalClipboardStructuralChangeListener implements ILayerListener {

    private InternalCellClipboard clipboard;

    public InternalClipboardStructuralChangeListener(InternalCellClipboard clipboard) {
        this.clipboard = clipboard;
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof IStructuralChangeEvent
                && !(event instanceof RowResizeEvent)
                && !(event instanceof ColumnResizeEvent)) {
            this.clipboard.clear();
        }
    }

}

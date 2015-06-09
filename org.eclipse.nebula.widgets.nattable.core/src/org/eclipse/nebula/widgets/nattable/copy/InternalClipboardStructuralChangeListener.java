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
package org.eclipse.nebula.widgets.nattable.copy;

import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;

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
        if (event instanceof IStructuralChangeEvent) {
            this.clipboard.clear();
        }
    }

}

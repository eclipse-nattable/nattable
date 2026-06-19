/*******************************************************************************
 * Copyright (c) 2026 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.util.PlatformHelper;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Default implementation of an {@link IDpiConverter} that provides the DPI and
 * scale factor based on the given zoom level and autoscale property.
 *
 * @since 2.7
 */
public class DefaultZoomDpiConverter extends AbstractDpiConverter {

    private Composite parent;
    private int zoom;

    public DefaultZoomDpiConverter(Composite parent) {
        this.parent = parent;
        Display.getDefault().syncExec(() -> {
            Object zoomRetrievalObject = null;
            if (parent != null) {
                zoomRetrievalObject = parent.getShell();
            } else {
                zoomRetrievalObject = Display.getDefault().getPrimaryMonitor();
            }
            Object zoomObject = PlatformHelper.callGetter(zoomRetrievalObject, "getZoom"); //$NON-NLS-1$
            DefaultZoomDpiConverter.this.zoom = zoomObject != null ? (int) zoomObject : 100;
        });

    }

    @Override
    protected void readDpiFromDisplay() {
        int autoscaledZoom = GUIHelper.getAutoScaleZoom(this.parent, this.zoom);
        this.scaleFactor = Float.valueOf(this.zoom) / Float.valueOf(autoscaledZoom);
        this.dpi = (int) (this.scaleFactor * 96);
    }

    @Override
    public int getDpi() {
        if (this.dpi < 0) {
            readDpiFromDisplay();
        }
        return this.dpi;
    }

    @Override
    public float getCurrentDpiFactor() {
        if (this.scaleFactor < 0) {
            readDpiFromDisplay();
        }
        return this.scaleFactor;
    }

}
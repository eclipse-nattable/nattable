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

/**
 * Default implementation of an {@link IDpiConverter} that provides the DPI
 * based on the given zoom level.
 *
 * @since 2.7
 */
public class DefaultZoomDpiConverter extends AbstractDpiConverter {

    private Object zoom;

    public DefaultZoomDpiConverter(Object zoom) {
        this.zoom = zoom;
    }

    @Override
    protected void readDpiFromDisplay() {
        String updateOnRuntime = System.getProperty("swt.autoScale.updateOnRuntime", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        if (Boolean.parseBoolean(updateOnRuntime)) {
            this.dpi = 96;
        } else {
            this.dpi = GUIHelper.getZoomBasedDpi(this.zoom);
        }
    }
}
/*******************************************************************************
 * Copyright (c) 2020 Dirk Fauth.
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
 * Default implementation of an {@link IDpiConverter} that scales according to
 * the set dpi.
 *
 * @since 2.0
 */
public class FixedScalingDpiConverter extends AbstractDpiConverter {

    public FixedScalingDpiConverter(int dpi) {
        this.dpi = dpi;
        this.scaleFactor = GUIHelper.getDpiFactor(dpi);
    }

    @Override
    protected void readDpiFromDisplay() {
        // do nothing
    }

}

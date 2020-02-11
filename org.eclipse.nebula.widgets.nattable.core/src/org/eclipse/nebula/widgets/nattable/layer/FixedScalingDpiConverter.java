/*******************************************************************************
 * Copyright (c) 2020 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

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

import org.eclipse.swt.widgets.Display;

/**
 * Default implementation of an {@link IDpiConverter} that provides the vertical
 * dots per inch from the default display via {@link Display#getDPI()}.
 *
 * @since 2.0
 */
public class DefaultVerticalDpiConverter extends AbstractDpiConverter {

    @Override
    protected void readDpiFromDisplay() {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                DefaultVerticalDpiConverter.this.dpi = Display.getDefault().getDPI().y;
            }
        });
    }

}

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
        Display.getDefault().syncExec(() -> DefaultVerticalDpiConverter.this.dpi = Display.getDefault().getDPI().y);
    }

}

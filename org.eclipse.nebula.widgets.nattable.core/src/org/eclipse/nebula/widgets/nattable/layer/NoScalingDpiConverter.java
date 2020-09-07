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

/**
 * Default implementation of an {@link IDpiConverter} that returns a scaling
 * factor of 1.0f with 96 dots per inch.
 *
 * @since 2.0
 */
public class NoScalingDpiConverter extends AbstractDpiConverter {

    public NoScalingDpiConverter() {
        this.dpi = 96;
        this.scaleFactor = 1.0f;
    }

    @Override
    protected void readDpiFromDisplay() {
        // do nothing
    }

}

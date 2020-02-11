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

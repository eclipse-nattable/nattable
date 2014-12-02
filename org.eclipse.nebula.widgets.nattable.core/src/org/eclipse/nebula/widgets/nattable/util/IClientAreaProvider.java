/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.util;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Specifies the rectangular area available to an {@link ILayer} Note: All
 * layers get the client area from {@link NatTable} which implements this
 * interface.
 *
 * @see ILayer#getClientAreaProvider()
 */
public interface IClientAreaProvider {

    IClientAreaProvider DEFAULT = new IClientAreaProvider() {
        @Override
        public Rectangle getClientArea() {
            return new Rectangle(0, 0, 0, 0);
        }
    };

    public Rectangle getClientArea();
}

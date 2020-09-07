/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

    IClientAreaProvider DEFAULT = () -> new Rectangle(0, 0, 0, 0);

    public Rectangle getClientArea();
}

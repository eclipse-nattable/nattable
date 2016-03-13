/*****************************************************************************
 * Copyright (c) 2016 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.config;

/**
 * Enumeration that is used to configure direction attributes. It is used for
 * example
 * <ul>
 * <li>to configure which directions are allowed for using the fill drag handle
 * </li>
 * <li>to configure whether a table should be scaled to fit on a page</li>
 * </ul>
 *
 * @since 1.4
 */
public enum Direction {
    NONE, HORIZONTAL, VERTICAL, BOTH
}

/*******************************************************************************
 * Copyright (c) 2024 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.fillhandle;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Interface for providers of the selection handle bounds of the fill handle.
 *
 * @since 2.5
 */
public interface FillHandleBoundsProvider {

    /**
     *
     * @return The bounds of the current visible selection handle or
     *         <code>null</code> if no fill handle is currently rendered.
     */
    Rectangle getSelectionHandleBounds();
}

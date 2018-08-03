/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.hideshow;

/**
 * Interface to define a layer that is able to deal with hidden rows.
 *
 * @since 1.6
 */
public interface IRowHideShowLayer {

    /**
     * Show the row(s) that are hidden next to the given row position.
     *
     * @param rowPosition
     *            The row position whose neighbors should be shown again.
     * @param showToTop
     *            Whether the row positions to the top or the bottom of the
     *            given row position should be shown again.
     * @param showAll
     *            Whether all hidden adjacent rows should be shown again or only
     *            the single direct adjacent row.
     */
    void showRowPosition(int rowPosition, boolean showToTop, boolean showAll);

}

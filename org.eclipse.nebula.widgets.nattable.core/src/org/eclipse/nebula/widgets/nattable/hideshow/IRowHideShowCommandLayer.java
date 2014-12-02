/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow;

import java.util.Collection;

/**
 * Interface for creating a layer for row hide/show behaviour. The main idea
 * behind it is that a layer that implements this interface is intended to
 * handle the row hide/show commands, so it isn't necessary to create new
 * commands for every layer.
 *
 * @author Dirk Fauth
 *
 */
public interface IRowHideShowCommandLayer {

    /**
     * Hide the rows at the given positions.
     *
     * @param rowPositions
     *            The positions of the rows to hide.
     */
    void hideRowPositions(Collection<Integer> rowPositions);

    /**
     * Hide the rows with the given indexes.
     *
     * @param rowIndexes
     *            The indexes of the rows to hide.
     */
    void hideRowIndexes(Collection<Integer> rowIndexes);

    /**
     * Show the rows with the given indexes again.
     *
     * @param rowIndexes
     *            The indexes of the rows that should be showed again.
     */
    void showRowIndexes(Collection<Integer> rowIndexes);

    /**
     * Show all rows that where previously hidden.
     */
    void showAllRows();
}

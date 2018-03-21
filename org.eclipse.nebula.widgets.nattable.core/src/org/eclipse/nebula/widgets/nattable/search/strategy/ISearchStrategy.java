/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.search.strategy;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.search.command.SearchGridCellsCommandHandler;

/**
 * Interface for the strategy implementation that should be used to perform a
 * search action.
 *
 * @see SearchGridCellsCommandHandler
 */
public interface ISearchStrategy {

    /**
     * Label that can be added to cells to avoid that they are included in the
     * search result. Helpful for example if columns that show checkboxes should be
     * excluded from searching.
     *
     * @since 1.6
     */
    String SKIP_SEARCH_RESULT_LABEL = "SKIP_SEARCH_RESULT"; //$NON-NLS-1$

    /**
     * Search for the given value and return the coordinates of the matching cell.
     *
     * @param valueToMatch
     *            The value to search for.
     * @return The coordinates of the cell that contains the given value.
     */
    PositionCoordinate executeSearch(Object valueToMatch);

}

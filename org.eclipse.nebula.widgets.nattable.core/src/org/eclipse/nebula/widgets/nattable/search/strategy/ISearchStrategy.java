/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
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
     * Search for the given value and return the coordinates of the matching
     * cell.
     *
     * @param valueToMatch
     *            The value to search for.
     * @return The coordinates of the cell that contains the given value.
     */
    PositionCoordinate executeSearch(Object valueToMatch);

}

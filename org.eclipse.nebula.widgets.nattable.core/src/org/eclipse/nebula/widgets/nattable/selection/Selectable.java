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
package org.eclipse.nebula.widgets.nattable.selection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Indicates an {@link ILayer} that supports the selection of individual cells.
 * Classes should implement this interface if they need to customize selection
 * logic.
 */
public interface Selectable {

    /**
     * Determine if a cell at a given position is selected.
     *
     * @param p
     *            cell to query
     * @return <code>true</code> if the given cell is selected
     */
    public boolean isSelected(PositionCoordinate p);
}

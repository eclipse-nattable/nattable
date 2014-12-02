/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

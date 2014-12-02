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
package org.eclipse.nebula.widgets.nattable.columnChooser;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

public interface ISelectionTreeListener {

    void itemsSelected(List<ColumnEntry> addedItems);

    void itemsRemoved(List<ColumnEntry> removedItems);

    /**
     * If columns moved are adjacent to each other, they are grouped together.
     *
     * @param direction
     * @param selectedColumnGroupEntries
     */
    void itemsMoved(MoveDirectionEnum direction,
            List<ColumnGroupEntry> selectedColumnGroupEntries,
            List<ColumnEntry> movedColumnEntries,
            List<List<Integer>> fromPositions, List<Integer> toPositions);

    void itemsExpanded(ColumnGroupEntry columnGroupEntry);

    void itemsCollapsed(ColumnGroupEntry columnGroupEntry);
}

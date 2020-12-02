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
package org.eclipse.nebula.widgets.nattable.columnChooser;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

public interface ISelectionTreeListener {

    void itemsSelected(List<ColumnEntry> addedItems);

    void itemsRemoved(List<ColumnEntry> removedItems);

    void itemsMoved(MoveDirectionEnum direction,
            List<ColumnGroupEntry> selectedColumnGroupEntries,
            List<ColumnEntry> movedColumnEntries,
            List<List<Integer>> fromPositions, List<Integer> toPositions);

    void itemsExpanded(ColumnGroupEntry columnGroupEntry);

    void itemsCollapsed(ColumnGroupEntry columnGroupEntry);
}

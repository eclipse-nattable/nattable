/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.hierarchical.command;

import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.copy.command.RowSpanningCopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * Handler class for copying selected data on a {@link HierarchicalTreeLayer} to
 * the clipboard. Will treat cells with row spanning as a single cell and will
 * not create gaps for rows with no cell to copy. Will also not copy cells of
 * collapsed nodes if the labels {@link HierarchicalTreeLayer#COLLAPSED_CHILD}
 * or {@link HierarchicalTreeLayer#NO_OBJECT_IN_LEVEL} are applied.
 *
 * @since 1.6
 *
 * @see HierarchicalTreePasteDataCommandHandler
 */
public class HierarchicalTreeCopyDataCommandHandler extends RowSpanningCopyDataCommandHandler {

    /**
     * Creates an instance that only checks the {@link SelectionLayer} for data
     * to add to the clipboard.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} within the NatTable. Can not be
     *            <code>null</code>.
     * @param treeLayer
     *            The {@link HierarchicalTreeLayer} that will be used as
     *            copyLayer from which the cells are identified to copy.
     * @param clipboard
     *            The {@link InternalCellClipboard} that should be used for
     *            copy/paste operations within a NatTable instance.
     */
    public HierarchicalTreeCopyDataCommandHandler(
            SelectionLayer selectionLayer, HierarchicalTreeLayer treeLayer, InternalCellClipboard clipboard) {
        this(selectionLayer, treeLayer, null, clipboard);
    }

    /**
     * Creates an instance that checks the {@link SelectionLayer} and the column
     * header layer if given for data to add to the clipboard.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} within the NatTable. Can not be
     *            <code>null</code>.
     * @param treeLayer
     *            The {@link HierarchicalTreeLayer} that will be used as
     *            copyLayer from which the cells are identified to copy.
     * @param columnHeaderLayer
     *            The column header layer within the NatTable grid. Can be
     *            <code>null</code>.
     * @param clipboard
     *            The {@link InternalCellClipboard} that should be used for
     *            copy/paste operations within a NatTable instance.
     */
    public HierarchicalTreeCopyDataCommandHandler(
            SelectionLayer selectionLayer, HierarchicalTreeLayer treeLayer, ILayer columnHeaderLayer, InternalCellClipboard clipboard) {
        super(selectionLayer, columnHeaderLayer, clipboard);
        setCopyLayer(treeLayer);
    }

    @Override
    protected boolean isCopyAllowed(ILayerCell cellToCopy) {
        LabelStack configLabels = cellToCopy.getConfigLabels();
        if (configLabels.hasLabel(HierarchicalTreeLayer.COLLAPSED_CHILD)
                || configLabels.hasLabel(HierarchicalTreeLayer.NO_OBJECT_IN_LEVEL)) {
            return false;
        }
        return super.isCopyAllowed(cellToCopy);
    }
}

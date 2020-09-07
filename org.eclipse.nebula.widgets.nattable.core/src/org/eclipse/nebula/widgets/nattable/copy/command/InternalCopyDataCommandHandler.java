/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.copy.command;

import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionUtils;

/**
 * Specialized {@link CopyDataCommandHandler} that stores the copied cells in
 * the {@link InternalCellClipboard} so it can be pasted within NatTable.
 *
 * @since 1.4
 */
public class InternalCopyDataCommandHandler extends CopyDataCommandHandler {

    protected InternalCellClipboard clipboard;

    /**
     * Creates an instance that only checks the {@link SelectionLayer} for the
     * data to add to the system clipboard and the given
     * {@link InternalCellClipboard}.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} within the NatTable. Can not be
     *            <code>null</code>.
     * @param clipboard
     *            The {@link InternalCellClipboard} that should be used for
     *            copy/paste operations within a NatTable instance.
     */
    public InternalCopyDataCommandHandler(SelectionLayer selectionLayer, InternalCellClipboard clipboard) {
        super(selectionLayer);
        this.clipboard = clipboard;
    }

    @Override
    protected void internalDoCommand(CopyDataToClipboardCommand command, ILayerCell[][] assembledCopiedDataStructure) {
        // copy to clipboard
        super.internalDoCommand(command, assembledCopiedDataStructure);

        // only copy if contiguous cells
        if (SelectionUtils.hasConsecutiveSelection(this.selectionLayer)) {
            preInternalCopy();

            // remember cells to copy to support paste
            // we need to re-assemble the data structure to copy because
            // preInternalCopy() could have changed the data, e.g. for formula
            // resolution
            this.clipboard.setCopiedCells(assembleCopiedDataStructure());

            postInternalCopy();
        }
    }

    /**
     * Perform actions prior copying values to the internal clipboard. E.g.
     * disabling formula evaluation.
     */
    protected void preInternalCopy() {
    }

    /**
     * Perform actions after copying values to the internal clipboard. E.g.
     * enabling formula evaluation.
     */
    protected void postInternalCopy() {
        this.selectionLayer.fireLayerEvent(new VisualRefreshEvent(this.selectionLayer));
    }
}

/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
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
package org.eclipse.nebula.widgets.nattable.copy.command;

import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
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
    public boolean doCommand(CopyDataToClipboardCommand command) {
        // copy to clipboard
        super.doCommand(command);

        // only copy if contiguous cells
        if (SelectionUtils.hasConsecutiveSelection(this.selectionLayer)) {
            preInternalCopy();

            // remember cells to copy to support paste
            this.clipboard.setCopiedCells(assembleCopiedDataStructure());

            postInternalCopy();
        }

        return true;
    }

    /**
     * Perform actions prior copying values to the internal clipboard. E.g.
     * disabling formula evaluation.
     */
    protected void preInternalCopy() {}

    /**
     * Perform actions after copying values to the internal clipboard. E.g.
     * enabling formula evaluation.
     */
    protected void postInternalCopy() {}
}

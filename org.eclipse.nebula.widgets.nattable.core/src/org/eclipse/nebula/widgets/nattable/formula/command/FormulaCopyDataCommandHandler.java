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
package org.eclipse.nebula.widgets.nattable.formula.command;

import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * Specialized {@link CopyDataCommandHandler} that stores the copied cells in
 * the {@link InternalCellClipboard} so it can be pasted within NatTable.
 *
 * @since 1.4
 */
public class FormulaCopyDataCommandHandler extends CopyDataCommandHandler {

    private InternalCellClipboard clipboard;

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
    public FormulaCopyDataCommandHandler(SelectionLayer selectionLayer, InternalCellClipboard clipboard) {
        super(selectionLayer);
        this.clipboard = clipboard;
    }

    @Override
    public boolean doCommand(CopyDataToClipboardCommand command) {
        // copy to clipboard
        super.doCommand(command);

        // only copy if contiguous cells
        ILayerCell[][] cells = assembleCopiedDataStructure();
        if (!isDiscontiguousSelection(cells)) {
            this.selectionLayer.doCommand(new DisableFormulaEvaluationCommand());

            // remember cells to copy to support paste of formulas
            this.clipboard.setCopiedCells(assembleCopiedDataStructure());

            this.selectionLayer.doCommand(new EnableFormulaEvaluationCommand());
        }

        return true;
    }

    /**
     * Performs a check whether the selection contains discontinuous cells. In
     * such a case copy/paste operations are not possible.
     *
     * @param selectedCells
     *            The selection to check.
     * @return <code>true</code> if the selection contains discontinuous cells,
     *         therefore copy/paste operations are not possible,
     *         <code>false</code> if a valid selection with continuous cells is
     *         checked
     */
    protected boolean isDiscontiguousSelection(ILayerCell[][] selectedCells) {
        int previousColumnIndex = -1;
        int previousRowIndex = -1;
        for (ILayerCell[] cells : selectedCells) {
            for (ILayerCell cell : cells) {
                if (cell != null) {
                    if (previousColumnIndex >= 0) {
                        int diff = cell.getColumnIndex() - previousColumnIndex;
                        if (diff > 1) {
                            return true;
                        }
                    }
                    previousColumnIndex = cell.getColumnIndex();
                }
            }

            if (previousRowIndex >= 0) {
                int diff = cells[0].getRowIndex() - previousRowIndex;
                if (diff > 1) {
                    return true;
                }
            }
            previousRowIndex = cells[0].getRowIndex();
        }
        return false;
    }

}

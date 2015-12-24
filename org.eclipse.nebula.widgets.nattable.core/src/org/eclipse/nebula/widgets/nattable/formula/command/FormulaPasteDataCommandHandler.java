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

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.copy.command.InternalPasteDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.copy.command.PasteDataCommand;
import org.eclipse.nebula.widgets.nattable.formula.FormulaDataProvider;
import org.eclipse.nebula.widgets.nattable.formula.function.FunctionException;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * {@link ILayerCommandHandler} for handling {@link PasteDataCommand}s. Uses the
 * {@link InternalCellClipboard} and transforms formulas to match the new
 * position.
 *
 * @since 1.4
 */
public class FormulaPasteDataCommandHandler extends InternalPasteDataCommandHandler {

    protected FormulaDataProvider dataProvider;

    /**
     *
     * @param selectionLayer
     *            {@link SelectionLayer} that is needed to determine the
     *            position to paste the values to.
     * @param dataProvider
     *            the {@link FormulaDataProvider} that is needed to perform
     *            formula related functions on pasting data.
     * @param clipboard
     *            The {@link InternalCellClipboard} that contains the values
     *            that should be pasted.
     */
    public FormulaPasteDataCommandHandler(
            SelectionLayer selectionLayer,
            InternalCellClipboard clipboard,
            FormulaDataProvider dataProvider) {

        super(selectionLayer, clipboard);
        this.dataProvider = dataProvider;
    }

    @Override
    protected Object getPasteValue(ILayerCell cell, int pasteColumn, int pasteRow) {
        Object cellValue = cell.getDataValue();
        if (cellValue != null && this.dataProvider.getFormulaParser().isFunction(cellValue.toString())) {
            try {
                cellValue = this.dataProvider.getFormulaParser().updateReferences(
                        cellValue.toString(), cell.getColumnPosition(), cell.getRowPosition(), pasteColumn, pasteRow);
            } catch (FunctionException e) {
                if (this.dataProvider.getErrorReporter() != null) {
                    this.dataProvider.getErrorReporter().addFormulaError(pasteColumn, pasteRow, e.getLocalizedMessage());
                }
                cellValue = e.getErrorMarkup();
            }
        }
        return cellValue;
    }

    @Override
    protected void preInternalPaste() {
        this.selectionLayer.doCommand(new DisableFormulaEvaluationCommand());
    }

    @Override
    protected void postInternalPaste() {
        this.selectionLayer.doCommand(new EnableFormulaEvaluationCommand());
    }

}

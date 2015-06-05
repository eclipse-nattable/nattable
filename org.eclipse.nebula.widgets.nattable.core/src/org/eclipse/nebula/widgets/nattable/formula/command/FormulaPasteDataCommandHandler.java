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

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.edit.command.EditUtils;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.formula.FormulaDataProvider;
import org.eclipse.nebula.widgets.nattable.formula.function.FunctionException;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * {@link ILayerCommandHandler} for handling {@link FormulaPasteDataCommand}s.
 * Uses the {@link InternalCellClipboard} and transforms formulas to match the
 * new position.
 *
 * @since 1.4
 */
public class FormulaPasteDataCommandHandler extends AbstractLayerCommandHandler<FormulaPasteDataCommand> {

    protected SelectionLayer selectionLayer;
    protected FormulaDataProvider dataProvider;
    protected InternalCellClipboard clipboard;

    public FormulaPasteDataCommandHandler(
            SelectionLayer selectionLayer,
            FormulaDataProvider dataProvider,
            InternalCellClipboard clipboard) {

        this.selectionLayer = selectionLayer;
        this.dataProvider = dataProvider;
        this.clipboard = clipboard;
    }

    @Override
    protected boolean doCommand(FormulaPasteDataCommand command) {
        if (this.clipboard.getCopiedCells() != null) {
            // in case there are no cached data information held in the copied
            // cells, ensure that formulas are not evaluated on paste
            this.selectionLayer.doCommand(new DisableFormulaEvaluationCommand());

            PositionCoordinate coord = this.selectionLayer.getSelectionAnchor();
            int pasteColumn = coord.getColumnPosition();
            int pasteRow = coord.getRowPosition();

            for (ILayerCell[] cells : this.clipboard.getCopiedCells()) {
                for (ILayerCell cell : cells) {
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

                    if (EditUtils.isCellEditable(
                            this.selectionLayer,
                            command.getConfigRegistry(),
                            new PositionCoordinate(this.selectionLayer, pasteColumn, pasteRow))) {

                        this.selectionLayer.doCommand(new UpdateDataCommand(this.selectionLayer, pasteColumn, pasteRow, cellValue));
                    }

                    pasteColumn++;

                    if (pasteColumn >= this.selectionLayer.getColumnCount()) {
                        break;
                    }
                }
                pasteRow++;
                pasteColumn = coord.getColumnPosition();
            }

            this.selectionLayer.doCommand(new EnableFormulaEvaluationCommand());
        }
        return true;
    }

    @Override
    public Class<FormulaPasteDataCommand> getCommandClass() {
        return FormulaPasteDataCommand.class;
    }

}

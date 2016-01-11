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

import java.math.BigDecimal;

import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommandHandler;
import org.eclipse.nebula.widgets.nattable.formula.FormulaDataProvider;
import org.eclipse.nebula.widgets.nattable.formula.function.FunctionException;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * Specialized command handler for {@link FillHandlePasteCommand}s that is able
 * to deal with formulas.
 *
 * @since 1.4
 */
public class FormulaFillHandlePasteCommandHandler extends FillHandlePasteCommandHandler {

    protected FormulaDataProvider dataProvider;

    /**
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} needed to determine the fill handle
     *            region and perform the update command.
     * @param clipboard
     *            The internal clipboard that carries the cells for the copy
     *            &amp; paste operation triggered by using the fill handle.
     * @param dataProvider
     *            The {@link FormulaDataProvider} that is needed to copy &amp;
     *            paste formulas.
     */
    public FormulaFillHandlePasteCommandHandler(
            SelectionLayer selectionLayer,
            InternalCellClipboard clipboard,
            FormulaDataProvider dataProvider) {

        super(selectionLayer, clipboard);
        this.dataProvider = dataProvider;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, FillHandlePasteCommand command) {
        if (this.clipboard.getCopiedCells() != null) {
            // in case there are no cached data information held in the copied
            // cells, ensure that formulas are not evaluated on paste
            this.selectionLayer.doCommand(new DisableFormulaEvaluationCommand());

            super.doCommand(targetLayer, command);

            this.selectionLayer.doCommand(new EnableFormulaEvaluationCommand());
        }
        return true;
    }

    @Override
    protected Object getPasteValue(ILayerCell cell, FillHandlePasteCommand command, int toColumn, int toRow) {
        Object cellValue = cell.getDataValue();
        if (cellValue != null && this.dataProvider.getFormulaParser().isFunction(cellValue.toString())) {
            try {
                cellValue = this.dataProvider.getFormulaParser().updateReferences(
                        cellValue.toString(), cell.getColumnPosition(), cell.getRowPosition(), toColumn, toRow);
            } catch (FunctionException e) {
                if (this.dataProvider.getErrorReporter() != null) {
                    this.dataProvider.getErrorReporter().addFormulaError(toColumn, toRow, e.getLocalizedMessage());
                }
                cellValue = e.getErrorMarkup();
            }
        } else if (cellValue != null
                && cellValue instanceof String
                && this.dataProvider.getFormulaParser().isNumber((String) cellValue)) {
            final BigDecimal converted = this.dataProvider.getFormulaParser().convertToBigDecimal((String) cellValue);
            ILayerCell temp = new LayerCell(cell.getLayer(),
                    cell.getOriginColumnPosition(), cell.getOriginRowPosition(),
                    cell.getColumnPosition(), cell.getRowPosition(),
                    cell.getColumnSpan(), cell.getRowSpan()) {

                @Override
                public Object getDataValue() {
                    return converted;
                }
            };
            Object calculated = super.getPasteValue(temp, command, toColumn, toRow);
            cellValue = (calculated != null) ? calculated.toString() : calculated;
        } else {
            cellValue = super.getPasteValue(cell, command, toColumn, toRow);
        }
        return cellValue;
    }

    @Override
    protected BigDecimal calculateBigDecimalDiff(ILayerCell c1, ILayerCell c2) {
        BigDecimal result = null;
        if (c1.getDataValue() != null && c2.getDataValue() != null) {
            BigDecimal v1 = null;
            if (c1.getDataValue() instanceof BigDecimal) {
                v1 = (BigDecimal) c1.getDataValue();
            } else if (c1.getDataValue() instanceof String
                    && this.dataProvider.getFormulaParser().isNumber((String) c1.getDataValue())) {
                v1 = this.dataProvider.getFormulaParser().convertToBigDecimal((String) c1.getDataValue());
            }

            BigDecimal v2 = null;
            if (c2.getDataValue() instanceof BigDecimal) {
                v2 = (BigDecimal) c2.getDataValue();
            } else if (c2.getDataValue() instanceof String
                    && this.dataProvider.getFormulaParser().isNumber((String) c2.getDataValue())) {
                v2 = this.dataProvider.getFormulaParser().convertToBigDecimal((String) c2.getDataValue());
            }

            if (v1 != null && v2 != null) {
                result = v1.subtract(v2);
            }
        }
        return result;
    }

}

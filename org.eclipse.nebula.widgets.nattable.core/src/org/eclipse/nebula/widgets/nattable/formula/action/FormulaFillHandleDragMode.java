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
package org.eclipse.nebula.widgets.nattable.formula.action;

import java.math.BigDecimal;
import java.util.Date;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.fillhandle.action.FillHandleDragMode;
import org.eclipse.nebula.widgets.nattable.formula.FormulaDataProvider;
import org.eclipse.nebula.widgets.nattable.formula.command.DisableFormulaEvaluationCommand;
import org.eclipse.nebula.widgets.nattable.formula.command.EnableFormulaEvaluationCommand;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * Specialized {@link FillHandleDragMode} that also opens the dialog in case of
 * String values that can be converted to {@link BigDecimal} values using the
 * {@link FormulaDataProvider}.
 *
 * @since 1.4
 */
public class FormulaFillHandleDragMode extends FillHandleDragMode {

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
     *            The {@link FormulaDataProvider} that is needed to determine
     *            whether a value is a number value.
     */
    public FormulaFillHandleDragMode(SelectionLayer selectionLayer, InternalCellClipboard clipboard,
            FormulaDataProvider dataProvider) {
        super(selectionLayer, clipboard);
        this.dataProvider = dataProvider;
    }

    @Override
    protected boolean showMenu(final NatTable natTable) {
        natTable.doCommand(new DisableFormulaEvaluationCommand());

        try {
            Class<?> type = null;
            for (ILayerCell[] cells : this.clipboard.getCopiedCells()) {
                for (ILayerCell cell : cells) {
                    if (cell.getDataValue() == null) {
                        return false;
                    } else {
                        if (type == null) {
                            type = cell.getDataValue().getClass();
                            if (!Number.class.isAssignableFrom(type)
                                    && !Date.class.isAssignableFrom(type)
                                    && (String.class.isAssignableFrom(type)
                                            && !this.dataProvider.getFormulaParser().isNumber((String) cell.getDataValue()))) {
                                return false;
                            }
                        } else if (type != cell.getDataValue().getClass()) {
                            return false;
                        }
                    }
                }
            }
        } finally {
            natTable.doCommand(new EnableFormulaEvaluationCommand());
        }

        return true;
    }
}

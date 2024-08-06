/*******************************************************************************
 * Copyright (c) 2024 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.formula.action;

import java.util.Date;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.formula.FormulaDataProvider;
import org.eclipse.nebula.widgets.nattable.formula.command.DisableFormulaEvaluationCommand;
import org.eclipse.nebula.widgets.nattable.formula.command.EnableFormulaEvaluationCommand;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

/**
 * Helper class for formula fill handle actions.
 *
 * @since 2.5
 */
public final class FormulaFillHandleActionHelper {

    private FormulaFillHandleActionHelper() {
        // private default constructor for helper class
    }

    /**
     * Check if the menu should be shown for selecting copy or series fill
     * operation. Uses the {@link FormulaDataProvider} to use the data type for
     * number evaluations.
     *
     * @param natTable
     *            The NatTable instance on which the operation is performed.
     * @param clipboard
     *            The internal clipboard that carries the cells for the copy
     *            &amp; paste operation triggered by using the fill handle.
     * @param dataProvider
     *            The {@link FormulaDataProvider} that is needed to determine
     *            whether a value is a number value.
     * @return <code>true</code> if the menu should be shown, <code>false</code>
     *         if not.
     */
    public static boolean showMenu(NatTable natTable, InternalCellClipboard clipboard, FormulaDataProvider dataProvider) {
        natTable.doCommand(new DisableFormulaEvaluationCommand());

        try {
            Class<?> type = null;
            for (ILayerCell[] cells : clipboard.getCopiedCells()) {
                for (ILayerCell cell : cells) {
                    if (cell != null) {
                        if (cell.getDataValue() == null) {
                            return false;
                        } else {
                            if (type == null) {
                                type = cell.getDataValue().getClass();
                                if (!Number.class.isAssignableFrom(type)
                                        && !Date.class.isAssignableFrom(type)
                                        && (String.class.isAssignableFrom(type)
                                                && !dataProvider.getFormulaParser().isNumber((String) cell.getDataValue()))) {
                                    return false;
                                }
                            } else if (type != cell.getDataValue().getClass()) {
                                return false;
                            }
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

/*****************************************************************************
 * Copyright (c) 2015, 2024 CEA LIST.
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
package org.eclipse.nebula.widgets.nattable.formula.action;

import java.math.BigDecimal;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.fillhandle.action.FillHandleDragMode;
import org.eclipse.nebula.widgets.nattable.formula.FormulaDataProvider;
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
        return FormulaFillHandleActionHelper.showMenu(natTable, this.clipboard, this.dataProvider);
    }
}

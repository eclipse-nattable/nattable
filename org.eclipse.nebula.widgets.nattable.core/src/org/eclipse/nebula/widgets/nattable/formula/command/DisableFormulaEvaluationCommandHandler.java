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
package org.eclipse.nebula.widgets.nattable.formula.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.formula.FormulaDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;

/**
 * Disables the formula evaluation in the {@link FormulaDataProvider}.
 *
 * @see DisableFormulaEvaluationCommand
 *
 * @since 1.4
 */
public class DisableFormulaEvaluationCommandHandler implements ILayerCommandHandler<DisableFormulaEvaluationCommand> {

    private FormulaDataProvider formulaDataProvider;

    public DisableFormulaEvaluationCommandHandler(FormulaDataProvider formulaDataProvider) {
        this.formulaDataProvider = formulaDataProvider;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, DisableFormulaEvaluationCommand command) {
        this.formulaDataProvider.setFormulaEvaluationEnabled(false);
        targetLayer.fireLayerEvent(new VisualRefreshEvent(targetLayer));
        return true;
    }

    @Override
    public Class<DisableFormulaEvaluationCommand> getCommandClass() {
        return DisableFormulaEvaluationCommand.class;
    }

}

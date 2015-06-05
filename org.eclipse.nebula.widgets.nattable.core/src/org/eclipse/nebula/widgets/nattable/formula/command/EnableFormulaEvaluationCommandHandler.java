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
import org.eclipse.nebula.widgets.nattable.formula.FormulaDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;

/**
 * Enables the formula evaluation in the {@link FormulaDataProvider}.
 *
 * @see EnableFormulaEvaluationCommand
 *
 * @since 1.4
 */
public class EnableFormulaEvaluationCommandHandler implements ILayerCommandHandler<EnableFormulaEvaluationCommand> {

    private FormulaDataProvider formulaDataProvider;

    public EnableFormulaEvaluationCommandHandler(FormulaDataProvider formulaDataProvider) {
        this.formulaDataProvider = formulaDataProvider;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, EnableFormulaEvaluationCommand command) {
        this.formulaDataProvider.setFormulaEvaluationEnabled(true);
        targetLayer.fireLayerEvent(new VisualRefreshEvent(targetLayer));
        return true;
    }

    @Override
    public Class<EnableFormulaEvaluationCommand> getCommandClass() {
        return EnableFormulaEvaluationCommand.class;
    }

}

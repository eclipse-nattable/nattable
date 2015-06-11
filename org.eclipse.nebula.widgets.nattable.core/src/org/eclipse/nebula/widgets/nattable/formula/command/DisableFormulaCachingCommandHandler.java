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

/**
 * Disables the formula caching and background processing of formula parsing and
 * calculation in the {@link FormulaDataProvider}.
 *
 * @see DisableFormulaCachingCommand
 *
 * @since 1.4
 */
public class DisableFormulaCachingCommandHandler implements ILayerCommandHandler<DisableFormulaCachingCommand> {

    private FormulaDataProvider formulaDataProvider;

    public DisableFormulaCachingCommandHandler(FormulaDataProvider formulaDataProvider) {
        this.formulaDataProvider = formulaDataProvider;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, DisableFormulaCachingCommand command) {
        this.formulaDataProvider.setFormulaCachingEnabled(false);
        return true;
    }

    @Override
    public Class<DisableFormulaCachingCommand> getCommandClass() {
        return DisableFormulaCachingCommand.class;
    }

}

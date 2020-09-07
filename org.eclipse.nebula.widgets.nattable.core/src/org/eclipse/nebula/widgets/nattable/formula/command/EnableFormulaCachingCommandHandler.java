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

/**
 * Enables the formula caching and background processing of formula parsing and
 * calculation in the {@link FormulaDataProvider}.
 *
 * @see EnableFormulaCachingCommand
 *
 * @since 1.4
 */
public class EnableFormulaCachingCommandHandler implements ILayerCommandHandler<EnableFormulaCachingCommand> {

    private FormulaDataProvider formulaDataProvider;

    public EnableFormulaCachingCommandHandler(FormulaDataProvider formulaDataProvider) {
        this.formulaDataProvider = formulaDataProvider;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, EnableFormulaCachingCommand command) {
        this.formulaDataProvider.setFormulaCachingEnabled(true);
        return true;
    }

    @Override
    public Class<EnableFormulaCachingCommand> getCommandClass() {
        return EnableFormulaCachingCommand.class;
    }

}

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

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.formula.FormulaDataProvider;

/**
 * {@link ILayerCommand} that is used to disable formula result caching in the
 * {@link FormulaDataProvider}.
 * <p>
 * Disabling formula result caching means that the parsing and calculation
 * processing is performed in the current thread and the processing is performed
 * always.
 * </p>
 *
 * @see DisableFormulaCachingCommandHandler
 *
 * @since 1.4
 */
public class DisableFormulaCachingCommand extends AbstractContextFreeCommand {
}
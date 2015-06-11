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

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.formula.FormulaDataProvider;

/**
 * {@link ILayerCommand} that is used to enable formula result caching in the
 * {@link FormulaDataProvider}.
 * <p>
 * Enabling formula result caching means that the parsing and calculation
 * processing is performed in a background thread and the processing is
 * performed only in case values in the {@link IDataProvider} have changed.
 * </p>
 *
 * @see EnableFormulaCachingCommandHandler
 *
 * @since 1.4
 */
public class EnableFormulaCachingCommand extends AbstractContextFreeCommand {}
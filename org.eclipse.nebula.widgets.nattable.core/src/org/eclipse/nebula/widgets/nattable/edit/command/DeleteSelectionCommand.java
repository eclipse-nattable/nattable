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
package org.eclipse.nebula.widgets.nattable.edit.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;

/**
 * {@link ILayerCommand} that is used to delete the values in the current
 * selected cells.
 *
 * @see DeleteSelectionCommandHandler
 *
 * @since 1.4
 */
public class DeleteSelectionCommand extends AbstractContextFreeCommand {

    private final IConfigRegistry configRegistry;

    public DeleteSelectionCommand(IConfigRegistry configRegistry) {
        this.configRegistry = configRegistry;
    }

    public IConfigRegistry getConfigRegistry() {
        return this.configRegistry;
    }
}
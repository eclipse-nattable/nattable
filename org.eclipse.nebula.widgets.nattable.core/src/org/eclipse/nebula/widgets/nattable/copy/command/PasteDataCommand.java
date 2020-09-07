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
package org.eclipse.nebula.widgets.nattable.copy.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;

/**
 * Command to trigger pasting data.
 *
 * @since 1.4
 *
 * @see InternalPasteDataCommandHandler
 */
public class PasteDataCommand extends AbstractContextFreeCommand {

    public final IConfigRegistry configRegistry;

    public PasteDataCommand(IConfigRegistry configRegistry) {
        this.configRegistry = configRegistry;
    }
}

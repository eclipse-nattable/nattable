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

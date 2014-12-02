/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.columnChooser.command;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

public class DisplayColumnChooserCommand extends AbstractContextFreeCommand {

    private final NatTable natTable;

    public DisplayColumnChooserCommand(NatTable natTable) {
        this.natTable = natTable;
    }

    public NatTable getNatTable() {
        return this.natTable;
    }

}

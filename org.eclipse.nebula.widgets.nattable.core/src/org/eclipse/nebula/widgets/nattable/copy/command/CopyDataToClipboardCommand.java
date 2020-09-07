/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.copy.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;

public class CopyDataToClipboardCommand extends AbstractContextFreeCommand {

    private final String cellDelimeter;
    private final String rowDelimeter;
    private final IConfigRegistry configRegistry;

    public CopyDataToClipboardCommand(String cellDelimeter, String rowDelimeter, IConfigRegistry configRegistry) {
        this.cellDelimeter = cellDelimeter;
        this.rowDelimeter = rowDelimeter;
        this.configRegistry = configRegistry;
    }

    public String getCellDelimeter() {
        return this.cellDelimeter;
    }

    public String getRowDelimeter() {
        return this.rowDelimeter;
    }

    public IConfigRegistry getConfigRegistry() {
        return this.configRegistry;
    }
}

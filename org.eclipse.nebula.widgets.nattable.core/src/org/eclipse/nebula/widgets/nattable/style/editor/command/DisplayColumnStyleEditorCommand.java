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
package org.eclipse.nebula.widgets.nattable.style.editor.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class DisplayColumnStyleEditorCommand extends AbstractContextFreeCommand {

    public final int columnPosition;
    public final int rowPosition;
    private final ILayer layer;
    private final IConfigRegistry configRegistry;

    public DisplayColumnStyleEditorCommand(ILayer natLayer,
            IConfigRegistry configRegistry, int columnPosition, int rowPosition) {
        this.layer = natLayer;
        this.configRegistry = configRegistry;
        this.columnPosition = columnPosition;
        this.rowPosition = rowPosition;
    }

    public ILayer getNattableLayer() {
        return this.layer;
    }

    public IConfigRegistry getConfigRegistry() {
        return this.configRegistry;
    }
}

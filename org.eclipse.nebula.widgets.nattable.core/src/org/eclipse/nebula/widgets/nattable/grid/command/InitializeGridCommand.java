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
package org.eclipse.nebula.widgets.nattable.grid.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.swt.widgets.Composite;

/**
 * Command that is propagated when NatTable starts up. This gives every layer a
 * chance to initialize itself and compute its structural caches.
 */
public class InitializeGridCommand extends AbstractContextFreeCommand {

    private final Composite tableComposite;

    public InitializeGridCommand(Composite tableComposite) {
        this.tableComposite = tableComposite;
    }

    public Composite getTableComposite() {
        return this.tableComposite;
    }

}

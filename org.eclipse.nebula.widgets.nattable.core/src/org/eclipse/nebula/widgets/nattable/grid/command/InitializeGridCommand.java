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

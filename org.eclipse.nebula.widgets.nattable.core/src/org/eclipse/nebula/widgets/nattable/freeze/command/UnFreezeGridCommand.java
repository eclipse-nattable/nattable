/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.freeze.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

/**
 * Simple command to unfreeze a frozen state.
 */
public class UnFreezeGridCommand extends AbstractContextFreeCommand implements
        IFreezeCommand {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.nebula.widgets.nattable.freeze.command.IFreezeCommand#isToggle
     * ()
     */
    @Override
    public boolean isToggle() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.nebula.widgets.nattable.freeze.command.IFreezeCommand#
     * isOverrideFreeze()
     */
    @Override
    public boolean isOverrideFreeze() {
        return false;
    }
}

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

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;

/**
 * Interface to mark commands as freeze related commands that are handled by the
 * {@link FreezeCommandHandler} or any command handler that handles
 * {@link ILayerCommand}s of this type.
 */
public interface IFreezeCommand extends ILayerCommand {

    /**
     * Indicates whether this command should toggle the frozen state between
     * frozen and unfrozen, or if it should always result in a frozen state.
     *
     * @return <code>true</code> if a frozen state should be unfrozen when it is
     *         tried to freeze again
     */
    boolean isToggle();

    /**
     * Indicates whether this command should override a current frozen state or
     * if it should be skipped if a frozen state is already applied.
     *
     * @return <code>true</code> if a current freeze state should be overriden,
     *         <code>false</code> if a requested freeze should be skipped on a
     *         already frozen state.
     */
    boolean isOverrideFreeze();
}

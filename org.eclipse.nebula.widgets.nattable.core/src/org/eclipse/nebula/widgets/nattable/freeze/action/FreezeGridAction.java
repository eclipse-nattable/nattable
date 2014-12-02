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
package org.eclipse.nebula.widgets.nattable.freeze.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeSelectionCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.events.KeyEvent;

/**
 * {@link IKeyAction} that will execute a {@link FreezeSelectionCommand} with
 * the specified parameters.
 */
public class FreezeGridAction implements IKeyAction {

    /**
     * Indicates whether this command should toggle the frozen state between
     * frozen and unfrozen, or if it should always result in a frozen state.
     */
    private boolean toggle;

    /**
     * Indicates whether this command should override a current frozen state or
     * if it should be skipped if a frozen state is already applied. Setting
     * this value to <code>true</code> will override the toggle behaviour.
     */
    private boolean overrideFreeze;

    /**
     * Creates a simple FreezeGridAction that doesn't toggle or override a
     * current frozen state.
     */
    public FreezeGridAction() {
        this(false);
    }

    /**
     * Creates a FreezeGridAction that doesn't override a current frozen state.
     * If it should toggle the current frozen state can be specified by
     * parameter.
     *
     * @param toggle
     *            whether this command should toggle the frozen state between
     *            frozen and unfrozen, or if it should always result in a frozen
     *            state.
     */
    public FreezeGridAction(boolean toggle) {
        this(toggle, false);
    }

    /**
     * Creates a FreezeGridAction. If it should toggle or override the current
     * frozen state can be specified by parameter.
     *
     * @param toggle
     *            whether this command should toggle the frozen state between
     *            frozen and unfrozen, or if it should always result in a frozen
     *            state.
     * @param overrideFreeze
     *            whether this command should override a current frozen state or
     *            if it should be skipped if a frozen state is already applied.
     *            <b>Note: Setting this value to <code>true</code> will override
     *            the toggle behaviour.</b>
     */
    public FreezeGridAction(boolean toggle, boolean overrideFreeze) {
        this.toggle = toggle;
        this.overrideFreeze = overrideFreeze;
    }

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        natTable.doCommand(new FreezeSelectionCommand(this.toggle,
                this.overrideFreeze));
    }

}

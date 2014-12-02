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

import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Will inform the handler to use the selection layer for its freeze
 * coordinates.
 *
 */
public class FreezeSelectionCommand implements IFreezeCommand {

    /**
     * Indicates whether this command should toggle the frozen state between
     * frozen and unfrozen, or if it should always result in a frozen state.
     */
    private boolean toggle;

    /**
     * Indicates whether this command should override a current frozen state or
     * if it should be skipped if a frozen state is already applied.
     */
    private boolean overrideFreeze;

    /**
     * Creates a simple FreezeSelectionCommand that doesn't toggle or override a
     * current frozen state.
     */
    public FreezeSelectionCommand() {
        this(false);
    }

    /**
     * Creates a FreezeSelectionCommand that doesn't override a current frozen
     * state. If it should toggle the current frozen state can be specified by
     * parameter.
     *
     * @param toggle
     *            whether this command should toggle the frozen state between
     *            frozen and unfrozen, or if it should always result in a frozen
     *            state.
     */
    public FreezeSelectionCommand(boolean toggle) {
        this(toggle, false);
    }

    /**
     * Creates a FreezeSelectionCommand. If it should toggle or override the
     * current frozen state can be specified by parameter.
     *
     * @param toggle
     *            whether this command should toggle the frozen state between
     *            frozen and unfrozen, or if it should always result in a frozen
     *            state.
     * @param overrideFreeze
     *            whether this command should override a current frozen state or
     *            if it should be skipped if a frozen state is already applied.
     */
    public FreezeSelectionCommand(boolean toggle, boolean overrideFreeze) {
        this.toggle = toggle;
        this.overrideFreeze = overrideFreeze;
    }

    @Override
    public boolean isToggle() {
        return this.toggle;
    }

    @Override
    public boolean isOverrideFreeze() {
        return this.overrideFreeze;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        return true;
    }

    @Override
    public FreezeSelectionCommand cloneCommand() {
        return this;
    }

}

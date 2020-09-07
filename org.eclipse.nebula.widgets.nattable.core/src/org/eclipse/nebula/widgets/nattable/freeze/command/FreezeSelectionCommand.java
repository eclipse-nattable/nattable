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
    private final boolean toggle;

    /**
     * Indicates whether this command should override a current frozen state or
     * if it should be skipped if a frozen state is already applied.
     */
    private final boolean overrideFreeze;

    /**
     * Indicates whether this command should include the selected cell to the
     * frozen region or not.
     */
    private final boolean include;

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
        this(toggle, overrideFreeze, false);
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
     * @param include
     *            whether the selected cell should be included in the freeze
     *            region or not. Include means the freeze borders will be to the
     *            right and bottom, while exclude means the freeze borders are
     *            to the left and top. Default is <code>false</code>.
     * @since 1.6
     */
    public FreezeSelectionCommand(boolean toggle, boolean overrideFreeze, boolean include) {
        this.toggle = toggle;
        this.overrideFreeze = overrideFreeze;
        this.include = include;
    }

    @Override
    public boolean isToggle() {
        return this.toggle;
    }

    @Override
    public boolean isOverrideFreeze() {
        return this.overrideFreeze;
    }

    /**
     *
     * @return Whether the selected cell should be included to the frozen region
     *         or not. Included means the freeze borders will be to the right
     *         and bottom, exclude means the freeze borders are to the top and
     *         left. Default is <code>false</code>.
     *
     * @since 1.6
     */
    public boolean isInclude() {
        return this.include;
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

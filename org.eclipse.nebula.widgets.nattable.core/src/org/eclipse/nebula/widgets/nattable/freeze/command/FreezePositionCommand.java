/*******************************************************************************
 * Copyright (c) 2012, 2019 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.freeze.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractPositionCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to freeze at the specified position.
 */
public class FreezePositionCommand extends AbstractPositionCommand implements IFreezeCommand {

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
     * Indicates whether this command should include the selected cell to the
     * frozen region or not.
     */
    private final boolean include;

    /**
     * Creates a FreezePositionCommand for the given column and row positions
     * related to the given layer, that doesn't toggle or override a current
     * frozen state.
     *
     * @param layer
     *            The layer to which the position coordinates match.
     * @param columnPosition
     *            The column position that will be the right most column in the
     *            frozen part.
     * @param rowPosition
     *            The row position that will be the bottom row in the frozen
     *            part.
     */
    public FreezePositionCommand(ILayer layer, int columnPosition, int rowPosition) {
        this(layer, columnPosition, rowPosition, false);
    }

    /**
     * Creates a FreezePositionCommand for the given column and row positions
     * related to the given layer, that doesn't override a current frozen
     * state.. If it should toggle the current frozen state can be specified by
     * parameter.
     *
     * @param layer
     *            The layer to which the position coordinates match.
     * @param columnPosition
     *            The column position that will be the right most column in the
     *            frozen part.
     * @param rowPosition
     *            The row position that will be the bottom row in the frozen
     *            part.
     * @param toggle
     *            whether this command should toggle the frozen state between
     *            frozen and unfrozen, or if it should always result in a frozen
     *            state.
     */
    public FreezePositionCommand(ILayer layer, int columnPosition, int rowPosition, boolean toggle) {
        this(layer, columnPosition, rowPosition, toggle, false);
    }

    /**
     * Creates a FreezePositionCommand for the given column and row positions
     * related to the given layer. If it should toggle or override the current
     * frozen state can be specified by parameter.
     *
     * @param layer
     *            The layer to which the position coordinates match.
     * @param columnPosition
     *            The column position that will be the right most column in the
     *            frozen part.
     * @param rowPosition
     *            The row position that will be the bottom row in the frozen
     *            part.
     * @param toggle
     *            whether this command should toggle the frozen state between
     *            frozen and unfrozen, or if it should always result in a frozen
     *            state.
     * @param overrideFreeze
     *            whether this command should override a current frozen state or
     *            if it should be skipped if a frozen state is already applied.
     */
    public FreezePositionCommand(ILayer layer, int columnPosition, int rowPosition, boolean toggle, boolean overrideFreeze) {
        this(layer, columnPosition, rowPosition, toggle, overrideFreeze, false);
    }

    /**
     * Creates a FreezePositionCommand for the given column and row positions
     * related to the given layer. If it should toggle or override the current
     * frozen state can be specified by parameter.
     *
     * @param layer
     *            The layer to which the position coordinates match.
     * @param columnPosition
     *            The column position that will be the right most column in the
     *            frozen part.
     * @param rowPosition
     *            The row position that will be the bottom row in the frozen
     *            part.
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
     *
     * @since 1.6
     */
    public FreezePositionCommand(ILayer layer, int columnPosition, int rowPosition, boolean toggle, boolean overrideFreeze, boolean include) {
        super(layer, columnPosition, rowPosition);
        this.toggle = toggle;
        this.overrideFreeze = overrideFreeze;
        this.include = include;
    }

    /**
     * Constructor used for cloning the command.
     *
     * @param command
     *            The command which is the base for the new cloned instance.
     */
    protected FreezePositionCommand(FreezePositionCommand command) {
        super(command);
        this.toggle = command.toggle;
        this.overrideFreeze = command.overrideFreeze;
        this.include = command.include;
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
    public ILayerCommand cloneCommand() {
        return new FreezePositionCommand(this);
    }

}

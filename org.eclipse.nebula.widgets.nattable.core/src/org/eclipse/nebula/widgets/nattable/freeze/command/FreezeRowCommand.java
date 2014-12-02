/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.freeze.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command that can be used to freeze a grid for whole rows.
 *
 * @author Dirk Fauth
 */
public class FreezeRowCommand extends AbstractRowCommand implements
        IFreezeCommand {

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
     * Creates a FreezeRowCommand for the given row position related to the
     * given layer, that doesn't toggle or override a current frozen state.
     *
     * @param layer
     *            The layer to which the row position matches.
     * @param rowPosition
     *            The row position that will be the bottom row in the frozen
     *            part.
     */
    public FreezeRowCommand(ILayer layer, int rowPosition) {
        this(layer, rowPosition, false);
    }

    /**
     * Creates a FreezeRowCommand for the given row position related to the
     * given layer, that doesn't override a current frozen state. If it should
     * toggle the current frozen state can be specified by parameter.
     *
     * @param layer
     *            The layer to which the row position matches.
     * @param rowPosition
     *            The row position that will be the bottom row in the frozen
     *            part.
     * @param toggle
     *            whether this command should toggle the frozen state between
     *            frozen and unfrozen, or if it should always result in a frozen
     *            state.
     */
    public FreezeRowCommand(ILayer layer, int rowPosition, boolean toggle) {
        this(layer, rowPosition, toggle, false);
    }

    /**
     * Creates a FreezeRowCommand for the given row position related to the
     * given layer. If it should toggle or override the current frozen state can
     * be specified by parameter.
     *
     * @param layer
     *            The layer to which the row position matches.
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
    public FreezeRowCommand(ILayer layer, int rowPosition, boolean toggle,
            boolean overrideFreeze) {
        super(layer, rowPosition);
        this.toggle = toggle;
        this.overrideFreeze = overrideFreeze;
    }

    /**
     * Constructor used for cloning the command.
     *
     * @param command
     *            The command which is the base for the new cloned instance.
     */
    protected FreezeRowCommand(FreezeRowCommand command) {
        super(command);
        this.toggle = command.toggle;
        this.overrideFreeze = command.overrideFreeze;
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
    public ILayerCommand cloneCommand() {
        return new FreezeRowCommand(this);
    }

}

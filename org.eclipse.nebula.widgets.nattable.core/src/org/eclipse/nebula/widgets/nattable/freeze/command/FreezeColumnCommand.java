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

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command that can be used to freeze a grid for whole columns.
 */
public class FreezeColumnCommand extends AbstractColumnCommand implements
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
     * Creates a FreezeColumnCommand for the given column related to the given
     * layer, that doesn't toggle or override a current frozen state.
     *
     * @param layer
     *            The layer to which the column position matches.
     * @param columnPosition
     *            The column position that will be the right most column in the
     *            frozen part.
     */
    public FreezeColumnCommand(ILayer layer, int columnPosition) {
        this(layer, columnPosition, false);
    }

    /**
     * Creates a FreezeColumnCommand for the given column related to the given
     * layer, that doesn't override a current frozen state. If it should toggle
     * the current frozen state can be specified by parameter.
     *
     * @param layer
     *            The layer to which the column position matches.
     * @param columnPosition
     *            The column position that will be the right most column in the
     *            frozen part.
     * @param toggle
     *            whether this command should toggle the frozen state between
     *            frozen and unfrozen, or if it should always result in a frozen
     *            state.
     */
    public FreezeColumnCommand(ILayer layer, int columnPosition, boolean toggle) {
        this(layer, columnPosition, toggle, false);
    }

    /**
     * Creates a FreezeColumnCommand for the given column related to the given
     * layer. If it should toggle or override the current frozen state can be
     * specified by parameter.
     *
     * @param layer
     *            The layer to which the column position matches.
     * @param columnPosition
     *            The column position that will be the right most column in the
     *            frozen part.
     * @param toggle
     *            whether this command should toggle the frozen state between
     *            frozen and unfrozen, or if it should always result in a frozen
     *            state.
     * @param overrideFreeze
     *            whether this command should override a current frozen state or
     *            if it should be skipped if a frozen state is already applied.
     */
    public FreezeColumnCommand(ILayer layer, int columnPosition,
            boolean toggle, boolean overrideFreeze) {
        super(layer, columnPosition);
        this.toggle = toggle;
        this.overrideFreeze = overrideFreeze;
    }

    /**
     * Constructor used for cloning the command.
     *
     * @param command
     *            The command which is the base for the new cloned instance.
     */
    protected FreezeColumnCommand(FreezeColumnCommand command) {
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
        return new FreezeColumnCommand(this);
    }

}

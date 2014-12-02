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
package org.eclipse.nebula.widgets.nattable.edit.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.widgets.Composite;

/**
 * Command that will trigger activating the edit mode for the specified cell.
 * The corresponding command handler is responsible for determining if
 * activation should proceed because of race conditions like e.g. the cell is
 * configured to be editable or another editor is active containing an invalid
 * data.
 */
public class EditCellCommand extends AbstractContextFreeCommand {

    /**
     * The {@link IConfigRegistry} containing the configuration of the current
     * NatTable instance the command should be executed for.
     * <p>
     * This is necessary because the edit controllers in the current
     * architecture are not aware of the instance they are running in.
     */
    private final IConfigRegistry configRegistry;
    /**
     * The parent Composite, needed for the creation of the editor control.
     */
    private final Composite parent;
    /**
     * The cell that should be put in edit mode.
     */
    private ILayerCell cell;

    /**
     *
     * @param parent
     *            The parent Composite, needed for the creation of the editor
     *            control.
     * @param configRegistry
     *            The {@link IConfigRegistry} containing the configuration of
     *            the current NatTable instance the command should be executed
     *            for. This is necessary because the edit controllers in the
     *            current architecture are not aware of the instance they are
     *            running in.
     * @param cell
     *            The cell that should be put into the edit mode.
     */
    public EditCellCommand(Composite parent, IConfigRegistry configRegistry,
            ILayerCell cell) {
        this.configRegistry = configRegistry;
        this.parent = parent;
        this.cell = cell;
    }

    /**
     * @return The {@link IConfigRegistry} containing the configuration of the
     *         current NatTable instance the command should be executed for.
     */
    public IConfigRegistry getConfigRegistry() {
        return this.configRegistry;
    }

    /**
     * @return The parent Composite, needed for the creation of the editor
     *         control.
     */
    public Composite getParent() {
        return this.parent;
    }

    /**
     * @return The cell that should be put in edit mode.
     */
    public ILayerCell getCell() {
        return this.cell;
    }

}

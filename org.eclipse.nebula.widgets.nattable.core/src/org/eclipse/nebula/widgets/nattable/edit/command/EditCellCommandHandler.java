/*******************************************************************************
 * Copyright (c) 2012, 2013, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 459991
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.EditController;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.swt.widgets.Composite;

/**
 * Command handler for handling {@link EditCellCommand}s. Will first check if
 * putting the cell into edit mode is allowed. If it is allowed it will call the
 * {@link EditController} for activation of the edit mode.
 */
public class EditCellCommandHandler extends AbstractLayerCommandHandler<EditCellCommand> {

    @Override
    public Class<EditCellCommand> getCommandClass() {
        return EditCellCommand.class;
    }

    @Override
    public boolean doCommand(EditCellCommand command) {
        ILayerCell cell = command.getCell();
        Composite parent = command.getParent();
        IConfigRegistry configRegistry = command.getConfigRegistry();
        if (cell != null && configRegistry != null) {

            // check if the cell is editable
            IEditableRule rule = configRegistry.getConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE,
                    DisplayMode.EDIT,
                    cell.getConfigLabels().getLabels());

            if (rule.isEditable(cell, configRegistry)) {
                EditController.editCell(
                        cell, parent, cell.getDataValue(), configRegistry);
            }
        }

        // as commands by default are intended to be consumed by the handler,
        // always return true, whether the activation of the edit mode was
        // successful or not
        return true;
    }

}

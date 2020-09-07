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
package org.eclipse.nebula.widgets.nattable.edit.command;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.edit.EditController;
import org.eclipse.nebula.widgets.nattable.edit.event.InlineCellEditEvent;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.widgets.Composite;

/**
 * Command handler for handling {@link EditSelectionCommand}s. Will first check
 * if all selected cells are editable and if they have the same editor
 * configured. Will call the {@link EditController} for activation of the edit
 * mode if these checks succeed.
 */
public class EditSelectionCommandHandler extends AbstractLayerCommandHandler<EditSelectionCommand> {

    private SelectionLayer selectionLayer;
    private IUniqueIndexLayer upperLayer;

    /**
     * Creates a command handler that performs the edit checks on the
     * {@link SelectionLayer}.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selection.
     */
    public EditSelectionCommandHandler(SelectionLayer selectionLayer) {
        this(selectionLayer, null);
    }

    /**
     * Creates a command handler that performs the edit checks based on the
     * given upper layer. Needed for example if the upper layer adds information
     * that is needed for checks, e.g. a tree layer.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selection.
     * @param upperLayer
     *            The layer on top of the given {@link SelectionLayer} to which
     *            the selection should be converted to. Can be <code>null</code>
     *            which causes the resulting selected cells to be related to the
     *            {@link SelectionLayer}.
     *
     * @since 1.6
     */
    public EditSelectionCommandHandler(SelectionLayer selectionLayer, IUniqueIndexLayer upperLayer) {
        this.selectionLayer = selectionLayer;
        this.upperLayer = upperLayer;
    }

    @Override
    public Class<EditSelectionCommand> getCommandClass() {
        return EditSelectionCommand.class;
    }

    @Override
    public boolean doCommand(EditSelectionCommand command) {
        Composite parent = command.getParent();
        IConfigRegistry configRegistry = command.getConfigRegistry();
        Character initialValue = command.getCharacter();

        if (EditUtils.allCellsEditable(this.selectionLayer, this.upperLayer, configRegistry)
                && EditUtils.isEditorSame(this.selectionLayer, this.upperLayer, configRegistry)
                && EditUtils.isConverterSame(this.selectionLayer, this.upperLayer, configRegistry)
                && EditUtils.activateLastSelectedCellEditor(this.selectionLayer, configRegistry, command.isByTraversal())) {

            // check how many cells are selected
            Collection<ILayerCell> selectedCells = EditUtils.getSelectedCellsForEditing(this.selectionLayer, this.upperLayer);
            if (selectedCells.size() == 1) {
                // editing is triggered by key for a single cell
                // we need to fire the InlineCellEditEvent here because we
                // don't know the correct bounds of the cell to edit inline
                // corresponding to the NatTable.
                // On firing the event, a translation process is triggered,
                // converting the information to the correct values
                // needed for inline editing
                ILayerCell cell = selectedCells.iterator().next();
                this.selectionLayer.fireLayerEvent(
                        new InlineCellEditEvent(
                                new PositionCoordinate(this.selectionLayer, cell.getOriginColumnPosition(), cell.getOriginRowPosition()),
                                parent,
                                configRegistry,
                                (initialValue != null ? initialValue : cell.getDataValue())));
            } else if (selectedCells.size() > 1) {
                // determine the initial value
                Object initialEditValue = initialValue;
                if (initialValue == null
                        && EditUtils.isValueSame(this.selectionLayer, this.upperLayer)) {
                    ILayerCell cell = selectedCells.iterator().next();
                    initialEditValue = this.selectionLayer.getDataValueByPosition(
                            cell.getColumnPosition(),
                            cell.getRowPosition());
                }

                EditController.editCells(selectedCells, parent, initialEditValue, configRegistry);
            }
        }

        // as commands by default are intended to be consumed by the handler,
        // always return true, whether the activation of the edit mode was
        // successful or not
        return true;
    }

}

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
package org.eclipse.nebula.widgets.nattable.columnCategories;

import static org.eclipse.nebula.widgets.nattable.columnChooser.ColumnChooserUtils.getHiddenColumnEntries;
import static org.eclipse.nebula.widgets.nattable.columnChooser.ColumnChooserUtils.getVisibleColumnsEntries;
import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.columnCategories.gui.ColumnCategoriesDialog;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnChooserUtils;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;

public class ChooseColumnsFromCategoriesCommandHandler extends
        AbstractLayerCommandHandler<ChooseColumnsFromCategoriesCommand>
        implements IColumnCategoriesDialogListener {

    private final ColumnHideShowLayer columnHideShowLayer;
    private final ColumnHeaderLayer columnHeaderLayer;
    private final DataLayer columnHeaderDataLayer;
    private final ColumnCategoriesModel model;
    private ColumnCategoriesDialog dialog;

    public ChooseColumnsFromCategoriesCommandHandler(
            ColumnHideShowLayer columnHideShowLayer,
            ColumnHeaderLayer columnHeaderLayer,
            DataLayer columnHeaderDataLayer, ColumnCategoriesModel model) {
        super();
        this.columnHideShowLayer = columnHideShowLayer;
        this.columnHeaderLayer = columnHeaderLayer;
        this.columnHeaderDataLayer = columnHeaderDataLayer;
        this.model = model;
    }

    @Override
    protected boolean doCommand(ChooseColumnsFromCategoriesCommand command) {
        this.dialog = new ColumnCategoriesDialog(command.getShell(), this.model,
                getHiddenColumnEntries(this.columnHideShowLayer, this.columnHeaderLayer,
                        this.columnHeaderDataLayer), getVisibleColumnsEntries(
                        this.columnHideShowLayer, this.columnHeaderLayer,
                        this.columnHeaderDataLayer));

        this.dialog.addListener(this);
        this.dialog.open();
        return true;
    }

    @Override
    public Class<ChooseColumnsFromCategoriesCommand> getCommandClass() {
        return ChooseColumnsFromCategoriesCommand.class;
    }

    // Listen and respond to the dialog events

    @Override
    public void itemsRemoved(List<Integer> removedColumnPositions) {
        ColumnChooserUtils.hideColumnPositions(removedColumnPositions,
                this.columnHideShowLayer);
        refreshDialog();
    }

    @Override
    public void itemsSelected(List<Integer> addedColumnIndexes) {
        ColumnChooserUtils.showColumnIndexes(addedColumnIndexes,
                this.columnHideShowLayer);
        refreshDialog();
    }

    /**
     * Moves the columns up or down by firing commands on the dialog.
     *
     * Individual columns are moved using the {@link ColumnReorderCommand}
     * Contiguously selected columns are moved using the
     * {@link MultiColumnReorderCommand}
     *
     * @param direction
     *            the direction to move
     * @param selectedPositions
     *            the column positions to move
     */
    @Override
    public void itemsMoved(MoveDirectionEnum direction,
            List<Integer> selectedPositions) {
        List<List<Integer>> fromPositions = PositionUtil
                .getGroupedByContiguous(selectedPositions);
        List<Integer> toPositions = getDestinationPositions(direction,
                fromPositions);

        for (int i = 0; i < fromPositions.size(); i++) {
            boolean multipleColumnsMoved = fromPositions.get(i).size() > 1;

            ILayerCommand command = null;
            if (!multipleColumnsMoved) {
                int fromPosition = fromPositions.get(i).get(0).intValue();
                int toPosition = toPositions.get(i);
                command = new ColumnReorderCommand(this.columnHideShowLayer,
                        fromPosition, toPosition);
            } else if (multipleColumnsMoved) {
                command = new MultiColumnReorderCommand(this.columnHideShowLayer,
                        fromPositions.get(i), toPositions.get(i));
            }
            this.columnHideShowLayer.doCommand(command);
        }

        refreshDialog();
    }

    /**
     * Calculates the destination positions taking into account the move
     * direction and single/contiguous selection.
     *
     * @param direction
     *            the direction to move
     * @param selectedPositions
     *            grouped together if they are contiguous.
     *            <p>
     *            Example: if 2,3,4, 9, 12 are selected, they are grouped as
     *            [[2, 3, 4], 9, 12]
     *            <ul>
     *            <li>While moving up the destination position for [2, 3, 4] is
     *            1</li>
     *            <li>While moving up the destination position for [2, 3, 4] is
     *            6</li>
     *            </ul>
     * @return a List of destination positions
     */
    protected List<Integer> getDestinationPositions(
            MoveDirectionEnum direction, List<List<Integer>> selectedPositions) {
        List<Integer> destinationPositions = new ArrayList<Integer>();
        for (List<Integer> contiguousPositions : selectedPositions) {
            switch (direction) {
                case UP:
                    destinationPositions.add(ObjectUtils
                            .getFirstElement(contiguousPositions) - 1);
                    break;
                case DOWN:
                    destinationPositions.add(ObjectUtils
                            .getLastElement(contiguousPositions) + 2);
                default:
                    break;
            }
        }
        return destinationPositions;
    }

    private void refreshDialog() {
        if (isNotNull(this.dialog)) {
            this.dialog.refresh(ColumnChooserUtils.getHiddenColumnEntries(
                    this.columnHideShowLayer, this.columnHeaderLayer,
                    this.columnHeaderDataLayer), ColumnChooserUtils
                    .getVisibleColumnsEntries(this.columnHideShowLayer,
                            this.columnHeaderLayer, this.columnHeaderDataLayer));
        }
    }

}

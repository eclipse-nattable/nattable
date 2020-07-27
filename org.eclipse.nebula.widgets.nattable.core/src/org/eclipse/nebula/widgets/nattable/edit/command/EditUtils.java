/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.IRowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

/**
 * Helper class for retrieving information regarding editing of selected cells.
 */
public final class EditUtils {

    private EditUtils() {
        // empty default constructor for helper class
    }

    /**
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selection
     *            from.
     * @return The last cell of the current selection in the specified
     *         {@link SelectionLayer}. Will return <code>null</code> if there is
     *         no selection.
     */
    public static ILayerCell getLastSelectedCell(SelectionLayer selectionLayer) {
        PositionCoordinate lastSelectedCell = selectionLayer.getLastSelectedCellPosition();
        return (lastSelectedCell != null)
                ? selectionLayer.getCellByPosition(lastSelectedCell.columnPosition, lastSelectedCell.rowPosition)
                : null;
    }

    /**
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selectio
     *            from.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to access the configured
     *            {@link ICellEditor}.
     * @return The {@link ICellEditor} of the last cell of the current selection
     *         in the specified {@link SelectionLayer}. Will return
     *         <code>null</code> if there is no selection.
     */
    public static ICellEditor getLastSelectedCellEditor(SelectionLayer selectionLayer, IConfigRegistry configRegistry) {
        ILayerCell lastSelectedCell = EditUtils.getLastSelectedCell(selectionLayer);
        if (lastSelectedCell != null) {
            final List<String> lastSelectedCellLabelsArray = lastSelectedCell.getConfigLabels();
            return configRegistry.getConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    DisplayMode.EDIT,
                    lastSelectedCellLabelsArray);
        }
        return null;
    }

    /**
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selection.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to access the configured
     *            {@link ICellEditor}.
     * @param byTraversal
     *            <code>true</code> if the activation is triggered by traversal,
     *            <code>false</code> if not
     * @return <code>true</code> if the current selected cell contains an editor
     *         that should be activated, <code>false</code> if not
     */
    public static boolean activateLastSelectedCellEditor(SelectionLayer selectionLayer, IConfigRegistry configRegistry, boolean byTraversal) {
        ILayerCell lastSelectedCell = EditUtils.getLastSelectedCell(selectionLayer);
        if (lastSelectedCell != null) {
            final List<String> lastSelectedCellLabelsArray = lastSelectedCell.getConfigLabels();
            ICellEditor editor = configRegistry.getConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    DisplayMode.EDIT,
                    lastSelectedCellLabelsArray);
            if (editor != null) {
                return (!byTraversal || editor.activateOnTraversal(configRegistry, lastSelectedCellLabelsArray));
            }
        }
        return false;
    }

    /**
     * For every cell that is selected it is checked whether the cell is
     * editable or not.
     *
     * <p>
     * In case a {@link IRowSelectionModel} is in use, only the selection anchor
     * is checked.
     * </p>
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selection.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to access the configured
     *            {@link IEditableRule}s.
     * @return <code>true</code> if all selected cells are editable,
     *         <code>false</code> if at least one cell is not editable.
     */
    public static boolean allCellsEditable(SelectionLayer selectionLayer, IConfigRegistry configRegistry) {
        return allCellsEditable(getSelectedCellsForEditing(selectionLayer), configRegistry);
    }

    /**
     * For every cell that is selected it is checked whether the cell is
     * editable or not.
     *
     * <p>
     * In case a {@link IRowSelectionModel} is in use, only the selection anchor
     * is checked.
     * </p>
     *
     * <p>
     * <b>Note:</b><br>
     * Converts the {@link SelectionLayer} based cells to the given upperLayer
     * to be able to inspect the cells if that layer on top of the
     * {@link SelectionLayer} adds information that needs to be inspected for
     * editing checks, e.g. a tree layer.
     * </p>
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selection.
     * @param upperLayer
     *            The layer on top of the given {@link SelectionLayer} to which
     *            the selection should be converted to. Can be <code>null</code>
     *            which causes the resulting selected cells to be related to the
     *            {@link SelectionLayer}.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to access the configured
     *            {@link IEditableRule}s.
     * @return <code>true</code> if all selected cells are editable,
     *         <code>false</code> if at least one cell is not editable.
     *
     * @since 1.6
     */
    public static boolean allCellsEditable(SelectionLayer selectionLayer, IUniqueIndexLayer upperLayer, IConfigRegistry configRegistry) {
        return allCellsEditable(getSelectedCellsForEditing(selectionLayer, upperLayer), configRegistry);
    }

    /**
     * For every selected cell it is checked whether the cell is editable or
     * not. If the collection of selected cells is <code>null</code> or empty,
     * this method will also return <code>true</code>.
     *
     * @param selectedCells
     *            The collection of selected cells that should be checked.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to access the configured
     *            {@link IEditableRule}s.
     * @return <code>true</code> if all selected cells are editable,
     *         <code>false</code> if at least one cell is not editable.
     */
    public static boolean allCellsEditable(Collection<ILayerCell> selectedCells, IConfigRegistry configRegistry) {
        if (selectedCells != null) {
            for (ILayerCell layerCell : selectedCells) {
                LabelStack labelStack = layerCell.getConfigLabels();
                IEditableRule editableRule = configRegistry.getConfigAttribute(
                        EditConfigAttributes.CELL_EDITABLE_RULE,
                        DisplayMode.EDIT,
                        labelStack);

                if (editableRule == null
                        || !editableRule.isEditable(layerCell, configRegistry)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if the cell at the specified coordinates is editable or not.
     *
     * @param cellCoords
     *            The coordinates of the cell to check the editable state,
     *            related to the given {@link ILayer}
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to access the configured
     *            {@link IEditableRule}s.
     * @return <code>true</code> if the cell is editable, <code>false</code> if
     *         not
     *
     * @since 1.6
     */
    public static boolean isCellEditable(PositionCoordinate cellCoords, IConfigRegistry configRegistry) {
        ILayerCell layerCell = cellCoords.getLayer().getCellByPosition(cellCoords.columnPosition, cellCoords.rowPosition);
        if (layerCell != null) {
            LabelStack labelStack = layerCell.getConfigLabels();

            IEditableRule editableRule = configRegistry.getConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE,
                    DisplayMode.EDIT,
                    labelStack);

            if (editableRule != null) {
                return editableRule.isEditable(layerCell, configRegistry);
            }
        }
        return false;
    }

    /**
     * Checks if all selected cells have the same {@link ICellEditor}
     * configured. This is needed for the multi edit feature to determine if a
     * multi edit is possible.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selection.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to access the configured
     *            {@link ICellEditor}s.
     * @return <code>true</code> if all selected cells have the same
     *         {@link ICellEditor} configured, <code>false</code> if at least
     *         one cell has another {@link ICellEditor} configured.
     */
    public static boolean isEditorSame(SelectionLayer selectionLayer, IConfigRegistry configRegistry) {
        return isEditorSame(getSelectedCellsForEditing(selectionLayer), configRegistry);
    }

    /**
     * Checks if all selected cells have the same {@link ICellEditor}
     * configured. This is needed for the multi edit feature to determine if a
     * multi edit is possible.
     * <p>
     * <b>Note:</b><br>
     * Converts the {@link SelectionLayer} based cells to the given upperLayer
     * to be able to inspect the cells if that layer on top of the
     * {@link SelectionLayer} adds information that needs to be inspected for
     * editing checks, e.g. a tree layer.
     * </p>
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selection.
     * @param upperLayer
     *            The layer on top of the given {@link SelectionLayer} to which
     *            the selection should be converted to. Can be <code>null</code>
     *            which causes the resulting selected cells to be related to the
     *            {@link SelectionLayer}.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to access the configured
     *            {@link ICellEditor}s.
     * @return <code>true</code> if all selected cells have the same
     *         {@link ICellEditor} configured, <code>false</code> if at least
     *         one cell has another {@link ICellEditor} configured.
     *
     * @since 1.6
     */
    public static boolean isEditorSame(SelectionLayer selectionLayer, IUniqueIndexLayer upperLayer, IConfigRegistry configRegistry) {
        return isEditorSame(getSelectedCellsForEditing(selectionLayer, upperLayer), configRegistry);
    }

    /**
     * Checks if all selected cells have the same {@link ICellEditor}
     * configured. This is needed for the multi edit feature to determine if a
     * multi edit is possible. If the collection of selected cells is
     * <code>null</code> or empty, this method will also return
     * <code>true</code>.
     *
     * @param selectedCells
     *            The collection of selected cells that should be checked.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to access the configured
     *            {@link ICellEditor}s.
     * @return <code>true</code> if all selected cells have the same
     *         {@link ICellEditor} configured, <code>false</code> if at least
     *         one cell has another {@link ICellEditor} configured.
     */
    public static boolean isEditorSame(Collection<ILayerCell> selectedCells, IConfigRegistry configRegistry) {
        if (selectedCells != null) {
            ICellEditor lastSelectedCellEditor = null;
            for (ILayerCell selectedCell : selectedCells) {
                LabelStack labelStack = selectedCell.getConfigLabels();
                ICellEditor cellEditor = configRegistry.getConfigAttribute(
                        EditConfigAttributes.CELL_EDITOR,
                        DisplayMode.EDIT,
                        labelStack);

                // The first time we get here we need to remember the editor so
                // further checks can use it.
                // Getting the editor before by getLastSelectedCellEditor()
                // might cause issues in case there is no active selection
                // anchor
                if (lastSelectedCellEditor == null) {
                    lastSelectedCellEditor = cellEditor;
                }
                if (cellEditor != lastSelectedCellEditor) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if all selected cells have the same {@link IDisplayConverter}
     * configured. This is needed for the multi edit feature to determine if a
     * multi edit is possible.
     * <p>
     * Let's assume there are two columns, one containing an Integer, the other
     * a Date. Both have a TextCellEditor configured, so if only the editor is
     * checked, the multi edit dialog would open. On committing a changed value
     * an error would occur because of wrong conversion.
     * </p>
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selection.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to access the configured
     *            {@link IDisplayConverter}s.
     * @return <code>true</code> if all selected cells have the same
     *         {@link IDisplayConverter} configured, <code>false</code> if at
     *         least one cell has another {@link IDisplayConverter} configured.
     */
    public static boolean isConverterSame(SelectionLayer selectionLayer, IConfigRegistry configRegistry) {
        return isConverterSame(getSelectedCellsForEditing(selectionLayer), configRegistry);
    }

    /**
     * Checks if all selected cells have the same {@link IDisplayConverter}
     * configured. This is needed for the multi edit feature to determine if a
     * multi edit is possible.
     * <p>
     * Let's assume there are two columns, one containing an Integer, the other
     * a Date. Both have a TextCellEditor configured, so if only the editor is
     * checked, the multi edit dialog would open. On committing a changed value
     * an error would occur because of wrong conversion.
     * </p>
     * <p>
     * <b>Note:</b><br>
     * Converts the {@link SelectionLayer} based cells to the given upperLayer
     * to be able to inspect the cells if that layer on top of the
     * {@link SelectionLayer} adds information that needs to be inspected for
     * editing checks, e.g. a tree layer.
     * </p>
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selection.
     * @param upperLayer
     *            The layer on top of the given {@link SelectionLayer} to which
     *            the selection should be converted to. Can be <code>null</code>
     *            which causes the resulting selected cells to be related to the
     *            {@link SelectionLayer}.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to access the configured
     *            {@link IDisplayConverter}s.
     * @return <code>true</code> if all selected cells have the same
     *         {@link IDisplayConverter} configured, <code>false</code> if at
     *         least one cell has another {@link IDisplayConverter} configured.
     *
     * @since 1.6
     */
    public static boolean isConverterSame(SelectionLayer selectionLayer, IUniqueIndexLayer upperLayer, IConfigRegistry configRegistry) {
        return isConverterSame(getSelectedCellsForEditing(selectionLayer, upperLayer), configRegistry);
    }

    /**
     * Checks if all selected cells have the same {@link IDisplayConverter}
     * configured. This is needed for the multi edit feature to determine if a
     * multi edit is possible. If the collection of selected cells is
     * <code>null</code> or empty, this method will also return
     * <code>true</code>.
     * <p>
     * Let's assume there are two columns, one containing an Integer, the other
     * a Date. Both have a TextCellEditor configured, so if only the editor is
     * checked, the multi edit dialog would open. On committing a changed value
     * an error would occur because of wrong conversion.
     * </p>
     *
     * @param selectedCells
     *            The collection of selected cells that should be checked.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to access the configured
     *            {@link IDisplayConverter}s.
     * @return <code>true</code> if all selected cells have the same
     *         {@link IDisplayConverter} configured, <code>false</code> if at
     *         least one cell has another {@link IDisplayConverter} configured.
     */
    public static boolean isConverterSame(Collection<ILayerCell> selectedCells, IConfigRegistry configRegistry) {
        if (selectedCells != null) {
            Set<Class<?>> converterSet = new HashSet<>();

            for (ILayerCell selectedCell : selectedCells) {
                LabelStack labelStack = selectedCell.getConfigLabels();
                IDisplayConverter dataTypeConverter = configRegistry.getConfigAttribute(
                        CellConfigAttributes.DISPLAY_CONVERTER,
                        DisplayMode.EDIT,
                        labelStack);
                if (dataTypeConverter != null) {
                    converterSet.add(dataTypeConverter.getClass());
                }
                if (converterSet.size() > 1)
                    return false;
            }
        }
        return true;
    }

    /**
     * Checks if all selected cells contain the same canonical value. This is
     * needed for multi edit to know if the editor should be initialised with
     * the value that is shared amongst all cells.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selection.
     * @return <code>true</code> if all cells contain the same value,
     *         <code>false</code> if at least one cell contains another value.
     */
    public static boolean isValueSame(SelectionLayer selectionLayer) {
        return isValueSame(getSelectedCellsForEditing(selectionLayer));
    }

    /**
     * Checks if all selected cells contain the same canonical value. This is
     * needed for multi edit to know if the editor should be initialised with
     * the value that is shared amongst all cells.
     * <p>
     * <b>Note:</b><br>
     * Converts the {@link SelectionLayer} based cells to the given upperLayer
     * to be able to inspect the cells if that layer on top of the
     * {@link SelectionLayer} adds information that needs to be inspected for
     * editing checks, e.g. a tree layer.
     * </p>
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selection.
     * @param upperLayer
     *            The layer on top of the given {@link SelectionLayer} to which
     *            the selection should be converted to. Can be <code>null</code>
     *            which causes the resulting selected cells to be related to the
     *            {@link SelectionLayer}.
     * @return <code>true</code> if all cells contain the same value,
     *         <code>false</code> if at least one cell contains another value.
     *
     * @since 1.6
     */
    public static boolean isValueSame(SelectionLayer selectionLayer, IUniqueIndexLayer upperLayer) {
        return isValueSame(getSelectedCellsForEditing(selectionLayer, upperLayer));
    }

    /**
     * Checks if all selected cells contain the same canonical value. This is
     * needed for multi edit to know if the editor should be initialized with
     * the value that is shared amongst all cells. If the collection of selected
     * cells is <code>null</code> or empty, this method will also return
     * <code>true</code>.
     *
     * @param selectedCells
     *            The collection of selected cells that should be checked.
     * @return <code>true</code> if all cells contain the same value,
     *         <code>false</code> if at least one cell contains another value.
     */
    public static boolean isValueSame(Collection<ILayerCell> selectedCells) {
        if (selectedCells != null) {
            Object lastSelectedValue = null;
            for (ILayerCell layerCell : selectedCells) {
                Object cellValue = layerCell.getDataValue();
                if (lastSelectedValue == null) {
                    lastSelectedValue = cellValue;
                }
                if ((cellValue != null && !cellValue.equals(lastSelectedValue))
                        || cellValue == null && lastSelectedValue != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the collection of selected {@link ILayerCell}s that are eligible
     * for editing. This method is used for multi edit support, to ensure the
     * editing also with row selection.
     * <p>
     * In case of cell selection, simply all selected cells are returned.
     * </p>
     * <p>
     * In case a {@link IRowSelectionModel} is configured, the selected cells in
     * correlation to the selection anchor are returned. This means, in case
     * only one row is selected, the selection anchor is returned. In case
     * multiple rows are selected, the cells at the column position of the
     * selection anchor for all selected rows are returned.
     * </p>
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selection.
     * @return The selected {@link ILayerCell}s that are eligible for editing.
     */
    public static Collection<ILayerCell> getSelectedCellsForEditing(SelectionLayer selectionLayer) {
        Collection<ILayerCell> selectedCells = null;
        if (selectionLayer.getSelectionModel() instanceof IRowSelectionModel) {
            selectedCells = new ArrayList<>();

            if (selectionLayer.getSelectionModel().getSelectedRowCount() == 1) {
                selectedCells.add(getLastSelectedCell(selectionLayer));
            } else {
                ILayerCell anchor = getLastSelectedCell(selectionLayer);
                int columnPosition = anchor != null ? anchor.getColumnPosition() : -1;
                for (ILayerCell cell : selectionLayer.getSelectedCells()) {
                    if (cell.getColumnPosition() == columnPosition) {
                        selectedCells.add(cell);
                    }
                }
            }
        } else {
            selectedCells = selectionLayer.getSelectedCells();
        }
        return selectedCells;
    }

    /**
     * Returns the collection of selected {@link ILayerCell}s that are eligible
     * for editing.
     * <p>
     * <b>Note:</b><br>
     * Converts the {@link SelectionLayer} based cells to the given upperLayer
     * to be able to inspect the cells if that layer on top of the
     * {@link SelectionLayer} adds information that needs to be inspected for
     * editing checks, e.g. a tree layer.
     * </p>
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the current selection.
     * @param upperLayer
     *            The layer on top of the given {@link SelectionLayer} to which
     *            the selection should be converted to. Can be <code>null</code>
     *            which causes the resulting selected cells to be related to the
     *            {@link SelectionLayer}.
     * @return The selected {@link ILayerCell}s that are eligible for editing,
     *         related to the given upperLayer.
     *
     * @since 1.6
     *
     * @see #getSelectedCellsForEditing(SelectionLayer)
     */
    public static Collection<ILayerCell> getSelectedCellsForEditing(SelectionLayer selectionLayer, IUniqueIndexLayer upperLayer) {
        Collection<ILayerCell> selectedCells = getSelectedCellsForEditing(selectionLayer);
        if (upperLayer != null) {
            ArrayList<ILayerCell> convertedCells = new ArrayList<>();
            for (ILayerCell cell : selectedCells) {
                int convertedColPos = LayerUtil.convertColumnPosition(cell.getLayer(), cell.getColumnPosition(), upperLayer);
                int convertedRowPos = LayerUtil.convertRowPosition(cell.getLayer(), cell.getRowPosition(), upperLayer);
                convertedCells.add(upperLayer.getCellByPosition(convertedColPos, convertedRowPos));
            }
            return convertedCells;
        }
        return selectedCells;
    }

}

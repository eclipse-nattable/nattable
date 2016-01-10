/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Added scaling
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.group.config.DefaultRowGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroup;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroupModel;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.SizeConfig;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.TransformedLayerCell;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectRowGroupCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;

/**
 * Adds the Row grouping functionality to the row headers. Also persists the
 * state of the row groups when {@link NatTable#saveState(String, Properties)}
 * is invoked.
 *
 * Internally uses the {@link IRowGroupModel} to track the row groups.
 * <p>
 * See RowGroupGridExample
 */
public class RowGroupHeaderLayer<T> extends AbstractLayerTransform {

    private final SizeConfig columnWidthConfig = new SizeConfig(DataLayer.DEFAULT_COLUMN_WIDTH);
    private final IRowGroupModel<T> model;
    private final SelectionLayer selectionLayer;
    private final ILayer rowHeaderLayer;

    public RowGroupHeaderLayer(
            ILayer rowHeaderLayer,
            SelectionLayer selectionLayer,
            IRowGroupModel<T> rowGroupModel) {

        this(rowHeaderLayer, selectionLayer, rowGroupModel, true);
    }

    public RowGroupHeaderLayer(
            ILayer rowHeaderLayer,
            SelectionLayer selectionLayer,
            IRowGroupModel<T> rowGroupModel,
            boolean useDefaultConfiguration) {

        super(rowHeaderLayer);
        this.rowHeaderLayer = rowHeaderLayer;
        this.selectionLayer = selectionLayer;
        this.model = rowGroupModel;

        registerCommandHandlers();

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultRowGroupHeaderLayerConfiguration<T>());
        }
    }

    public IRowGroupModel<T> getModel() {
        return this.model;
    }

    // Persistence

    @Override
    public void loadState(String prefix, Properties properties) {
        super.loadState(prefix, properties);
        this.model.loadState(prefix, properties);
        fireLayerEvent(new RowStructuralRefreshEvent(this));
    }

    @Override
    public void saveState(String prefix, Properties properties) {
        super.saveState(prefix, properties);
        this.model.saveState(prefix, properties);
    }

    // Configuration

    @Override
    protected void registerCommandHandlers() {
        registerCommandHandler(new SelectRowGroupCommandHandler<T>(this.model, this.selectionLayer, this));
        registerCommandHandler(new ConfigureScalingCommandHandler(this.columnWidthConfig, null));
    }

    // Horizontal features

    // Columns

    @Override
    public int getColumnCount() {
        return this.rowHeaderLayer.getColumnCount() + 1;
    }

    @Override
    public int getPreferredColumnCount() {
        return this.rowHeaderLayer.getPreferredColumnCount() + 1;
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        if (columnPosition == 0) {
            return columnPosition;
        } else {
            return this.rowHeaderLayer.getColumnIndexByPosition(columnPosition - 1);
        }
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        if (localColumnPosition == 0) {
            return localColumnPosition;
        }
        return localColumnPosition - 1;
    }

    // Width

    @Override
    public int getWidth() {
        return this.columnWidthConfig.getAggregateSize(1)
                + this.rowHeaderLayer.getWidth();
    }

    @Override
    public int getPreferredWidth() {
        return this.columnWidthConfig.getAggregateSize(1)
                + this.rowHeaderLayer.getPreferredWidth();
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        if (columnPosition == 0) {
            return this.columnWidthConfig.getSize(columnPosition);
        } else {
            return this.rowHeaderLayer.getColumnWidthByPosition(columnPosition - 1);
        }
    }

    public void setColumnWidth(int columnWidth) {
        this.columnWidthConfig.setSize(0, columnWidth);
    }

    // Column resize

    @Override
    public boolean isColumnPositionResizable(int columnPosition) {
        if (columnPosition == 0) {
            return this.columnWidthConfig.isPositionResizable(columnPosition);
        } else {
            return this.rowHeaderLayer.isRowPositionResizable(columnPosition - 1);
        }
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        int col0Width = getColumnWidthByPosition(0);
        if (x < col0Width) {
            return 0;
        } else {
            return 1 + this.rowHeaderLayer.getColumnPositionByX(x - col0Width);
        }
    }

    @Override
    public int getStartXOfColumnPosition(int columnPosition) {
        if (columnPosition == 0) {
            return this.columnWidthConfig.getAggregateSize(columnPosition);
        } else {
            return getColumnWidthByPosition(0)
                    + this.rowHeaderLayer.getStartXOfColumnPosition(columnPosition - 1);
        }
    }

    // Cell features

    /**
     * If a cell belongs to a column group: column position - set to the start
     * position of the group span - set to the width/size of the row group
     *
     * NOTE: gc.setClip() is used in the CompositeLayerPainter to ensure that
     * partially visible Column group header cells are rendered properly.
     */
    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        int bodyRowIndex = getRowIndexByPosition(rowPosition);

        // Row group header cell
        if (RowGroupUtils.isPartOfAGroup(this.model, bodyRowIndex)) {
            if (columnPosition == 0) {
                return new LayerCell(
                        this,
                        columnPosition,
                        getStartPositionOfGroup(rowPosition),
                        columnPosition,
                        rowPosition,
                        1,
                        getRowSpan(rowPosition));
            } else {
                return new LayerCell(this, columnPosition, rowPosition);
            }
        } else {
            // render row header w/ columnspan = 2
            // as in this case we ask the row header layer for the cell position
            // and the row header layer asks his data provider for the column
            // count which should always return 1, we ask for row position 0
            ILayerCell cell = this.rowHeaderLayer.getCellByPosition(0, rowPosition);
            if (cell != null) {
                cell = new TransformedLayerCell(cell) {
                    @Override
                    public ILayer getLayer() {
                        return RowGroupHeaderLayer.this;
                    }

                    @Override
                    public int getColumnSpan() {
                        return 2;
                    }
                };
            }
            return cell;
        }
    }

    /**
     * Calculates the span of a cell in a Row Group. Takes into account
     * collapsing and hidden rows in the group.
     *
     * @param rowPosition
     *            position of any row belonging to the group
     */
    protected int getRowSpan(int rowPosition) {
        int rowIndex = getRowIndexByPosition(rowPosition);

        // Get the row and the group from our cache and model.
        final IRowGroup<T> rowGroup =
                RowGroupUtils.getRowGroupForRowIndex(this.model, rowIndex);

        int sizeOfGroup = RowGroupUtils.sizeOfGroup(this.model, rowIndex);

        if (RowGroupUtils.isCollapsed(this.model, rowGroup)) {
            int sizeOfStaticRows = rowGroup.getOwnStaticMemberRows().size();
            if (sizeOfStaticRows == 0) {
                return 1;
            } else {
                sizeOfGroup = sizeOfStaticRows;
            }
        }

        int startPositionOfGroup = getStartPositionOfGroup(rowPosition);
        int endPositionOfGroup = startPositionOfGroup + sizeOfGroup;
        List<Integer> rowIndexesInGroup =
                RowGroupUtils.getRowIndexesInGroup(this.model, rowIndex);

        for (int i = startPositionOfGroup; i < endPositionOfGroup; i++) {
            int index = getRowIndexByPosition(i);
            if (!rowIndexesInGroup.contains(Integer.valueOf(index))) {
                sizeOfGroup--;
            }
        }

        return sizeOfGroup;
    }

    /**
     * Figures out the start position of the group.
     *
     * @param selectionLayerColumnPosition
     *            of any column belonging to the group
     * @return first position of the column group
     */
    private int getStartPositionOfGroup(int rowPosition) {
        int bodyRowIndex = getRowIndexByPosition(rowPosition);
        int leastPossibleStartPositionOfGroup =
                Math.max(0, (rowPosition - RowGroupUtils.sizeOfGroup(this.model, bodyRowIndex)));
        int i = 0;
        for (i = leastPossibleStartPositionOfGroup; i < rowPosition; i++) {
            if (RowGroupUtils.isInTheSameGroup(
                    getRowIndexByPosition(i),
                    bodyRowIndex,
                    this.model)) {
                break;
            }
        }
        return i;
    }

    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
        int rowIndex = getRowIndexByPosition(rowPosition);
        if (columnPosition == 0
                && RowGroupUtils.isPartOfAGroup(this.model, rowIndex)) {
            return DisplayMode.NORMAL;
        } else {
            return this.rowHeaderLayer.getDisplayModeByPosition(columnPosition - 1, rowPosition);
        }
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        int rowIndex = getRowIndexByPosition(rowPosition);
        if (columnPosition == 0
                && RowGroupUtils.isPartOfAGroup(this.model, rowIndex)) {
            LabelStack stack = new LabelStack(GridRegion.ROW_GROUP_HEADER);

            IRowGroup<T> group = RowGroupUtils.getRowGroupForRowIndex(this.model, rowIndex);
            if (RowGroupUtils.isCollapsed(this.model, group)) {
                stack.addLabelOnTop(DefaultRowGroupHeaderLayerConfiguration.GROUP_COLLAPSED_CONFIG_TYPE);
            } else {
                stack.addLabelOnTop(DefaultRowGroupHeaderLayerConfiguration.GROUP_EXPANDED_CONFIG_TYPE);
            }

            List<Integer> selectedRowIndexes =
                    convertToRowIndexes(this.selectionLayer.getFullySelectedRowPositions());
            if (selectedRowIndexes.contains(rowIndex)) {
                stack.addLabelOnTop(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE);
            }

            return stack;
        } else {
            return this.rowHeaderLayer.getConfigLabelsByPosition(columnPosition - 1, rowPosition);
        }
    }

    private List<Integer> convertToRowIndexes(final int[] rowPositions) {
        final List<Integer> rowIndexes = new ArrayList<Integer>();
        for (final Integer rowPosition : rowPositions) {
            rowIndexes.add(this.selectionLayer.getRowIndexByPosition(rowPosition));
        }
        return rowIndexes;
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        int rowIndex = getRowIndexByPosition(rowPosition);
        if (columnPosition == 0
                && RowGroupUtils.isPartOfAGroup(this.model, rowIndex)) {
            return RowGroupUtils.getRowGroupNameForIndex(this.model, rowIndex);
        } else {
            return this.rowHeaderLayer.getDataValueByPosition(columnPosition - 1, rowPosition);
        }
    }

    @Override
    public LabelStack getRegionLabelsByXY(int x, int y) {
        int rowIndex = getRowIndexByPosition(getRowPositionByY(y));
        if (RowGroupUtils.isPartOfAGroup(this.model, rowIndex)
                && x < getColumnWidthByPosition(0)) {
            return new LabelStack(GridRegion.ROW_GROUP_HEADER);
        } else {
            return this.rowHeaderLayer.getRegionLabelsByXY(x - getColumnWidthByPosition(0), y);
        }
    }

    public void collapseRowGroupByIndex(int rowIndex) {
        RowGroupUtils.getRowGroupForRowIndex(this.model, rowIndex).collapse();
    }

    public void clearAllGroups() {
        this.model.clear();
    }

    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> labels = super.getProvidedLabels();

        labels.add(GridRegion.ROW_GROUP_HEADER);
        labels.add(DefaultRowGroupHeaderLayerConfiguration.GROUP_COLLAPSED_CONFIG_TYPE);
        labels.add(DefaultRowGroupHeaderLayerConfiguration.GROUP_EXPANDED_CONFIG_TYPE);

        return labels;
    }
}

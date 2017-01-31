/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
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

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.group.command.ColumnGroupsCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.config.DefaultColumnGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.SizeConfig;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.TransformedLayerCell;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

/**
 * Adds the Column grouping functionality to the column headers. Also persists
 * the state of the column groups when
 * {@link NatTable#saveState(String, Properties)} is invoked.
 *
 * Internally uses the {@link ColumnGroupModel} to track the column groups. See
 * ColumnGroupGridExample
 */
public class ColumnGroupHeaderLayer extends AbstractLayerTransform {

    private final SizeConfig rowHeightConfig = new SizeConfig(DataLayer.DEFAULT_ROW_HEIGHT);
    private final ColumnGroupModel model;
    private final ILayer columnHeaderLayer;

    /**
     * Flag which is used to tell the ColumnGroupHeaderLayer whether to
     * calculate the height of the layer dependent on column group configuration
     * or not. If it is set to <code>true</code> the column header will check if
     * column groups are configured and if not, the height of the column header
     * will not show the double height for showing column groups.
     */
    private boolean calculateHeight = false;

    /**
     * Listener that will fire a RowStructuralRefreshEvent in case the
     * ColumnGroupModel changes. Is only needed in case the dynamic height
     * calculation is enabled.
     */
    private IColumnGroupModelListener modelChangeListener;

    public ColumnGroupHeaderLayer(
            ILayer columnHeaderLayer,
            SelectionLayer selectionLayer,
            ColumnGroupModel columnGroupModel) {

        this(columnHeaderLayer, selectionLayer, columnGroupModel, true);
    }

    public ColumnGroupHeaderLayer(
            final ILayer columnHeaderLayer,
            SelectionLayer selectionLayer,
            ColumnGroupModel columnGroupModel,
            boolean useDefaultConfiguration) {

        super(columnHeaderLayer);

        this.columnHeaderLayer = columnHeaderLayer;
        this.model = columnGroupModel;

        registerCommandHandler(new ColumnGroupsCommandHandler(this.model, selectionLayer, this));
        registerCommandHandler(new ConfigureScalingCommandHandler(null, this.rowHeightConfig));

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultColumnGroupHeaderLayerConfiguration(columnGroupModel));
        }

        this.modelChangeListener = new IColumnGroupModelListener() {
            @Override
            public void columnGroupModelChanged() {
                fireLayerEvent(new RowStructuralRefreshEvent(columnHeaderLayer));
            }
        };

        this.model.registerColumnGroupModelListener(this.modelChangeListener);
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        super.saveState(prefix, properties);
        this.model.saveState(prefix, properties);
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        super.loadState(prefix, properties);
        this.model.loadState(prefix, properties);
        fireLayerEvent(new ColumnStructuralRefreshEvent(this));
    }

    // Vertical features

    // Rows

    @Override
    public int getRowCount() {
        return this.columnHeaderLayer.getRowCount() + getGroupRowCount();
    }

    private int getGroupRowCount() {
        return isGroupRowIncluded() ? 1 : 0;
    }

    private int getGroupRowHeight() {
        return isGroupRowIncluded() ? this.rowHeightConfig.getAggregateSize(1) : 0;
    }

    @Override
    public int getPreferredRowCount() {
        return this.columnHeaderLayer.getPreferredRowCount() + getGroupRowCount();
    }

    @Override
    public int getRowIndexByPosition(int rowPosition) {
        if (rowPosition == 0 && isGroupRowIncluded()) {
            return rowPosition;
        } else {
            return this.columnHeaderLayer.getRowIndexByPosition(rowPosition - this.getGroupRowCount());
        }
    }

    @Override
    public int localToUnderlyingRowPosition(int localRowPosition) {
        if (localRowPosition == 0 && isGroupRowIncluded()) {
            return localRowPosition;
        } else {
            return localRowPosition - getGroupRowCount();
        }

    }

    // Height

    @Override
    public int getHeight() {
        return this.getGroupRowHeight()
                + this.columnHeaderLayer.getHeight();
    }

    private boolean isGroupRowIncluded() {
        return !this.calculateHeight
                || (this.model.getAllIndexesInGroups() != null
                        && !this.model.getAllIndexesInGroups().isEmpty());
    }

    @Override
    public int getPreferredHeight() {
        return getGroupRowHeight()
                + this.columnHeaderLayer.getPreferredHeight();
    }

    @Override
    public int getRowHeightByPosition(int rowPosition) {
        if (rowPosition == 0 && isGroupRowIncluded()) {
            return this.rowHeightConfig.getSize(rowPosition);
        } else {
            return this.columnHeaderLayer.getRowHeightByPosition(rowPosition - getGroupRowCount());
        }
    }

    public void setRowHeight(int rowHeight) {
        this.rowHeightConfig.setSize(0, rowHeight);
    }

    // Row resize

    @Override
    public boolean isRowPositionResizable(int rowPosition) {
        if (rowPosition == 0 && isGroupRowIncluded()) {
            return this.rowHeightConfig.isPositionResizable(rowPosition);
        } else {
            return this.columnHeaderLayer.isRowPositionResizable(rowPosition - getGroupRowCount());
        }
    }

    // Y

    @Override
    public int getRowPositionByY(int y) {
        if (this.isGroupRowIncluded()) {
            int row0Height = getRowHeightByPosition(0);
            if (y < row0Height) {
                return 0;
            } else {
                return 1 + this.columnHeaderLayer.getRowPositionByY(y - row0Height);
            }
        } else {
            return this.columnHeaderLayer.getRowPositionByY(y);
        }
    }

    @Override
    public int getStartYOfRowPosition(int rowPosition) {
        if (isGroupRowIncluded()) {
            if (rowPosition == 0) {
                return this.rowHeightConfig.getAggregateSize(rowPosition);
            } else {
                return getRowHeightByPosition(0)
                        + this.columnHeaderLayer.getStartYOfRowPosition(rowPosition - 1);
            }
        } else {
            return this.columnHeaderLayer.getStartYOfRowPosition(rowPosition);
        }
    }

    // Cell features

    /**
     * If a cell belongs to a column group: column position - set to the start
     * position of the group span - set to the width/size of the column group
     *
     * NOTE: gc.setClip() is used in the CompositeLayerPainter to ensure that
     * partially visible Column group header cells are rendered properly.
     */
    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        int bodyColumnIndex = getColumnIndexByPosition(columnPosition);

        // Column group header cell
        if (this.model.isPartOfAGroup(bodyColumnIndex)) {
            if (rowPosition == 0) {
                return new LayerCell(
                        this,
                        getStartPositionOfGroup(columnPosition),
                        rowPosition,
                        columnPosition,
                        rowPosition,
                        getColumnSpan(columnPosition),
                        1);
            } else {
                return new LayerCell(this, columnPosition, rowPosition);
            }
        } else {
            // render column header w/ rowspan = 2
            // as in this case we ask the column header layer for the cell
            // position and the column header layer asks his data provider for
            // the row count which should always return 1, we ask for row
            // position 0 instead of using getGroupHeaderRowPosition(), if we
            // would use getGroupHeaderRowPosition() the
            // ColumnGroupGroupHeaderLayer wouldn't work anymore
            ILayerCell cell = this.columnHeaderLayer.getCellByPosition(columnPosition, 0);
            if (cell != null) {
                final int rowSpan;

                if (this.calculateHeight && this.model.size() == 0) {
                    rowSpan = 1;
                } else {
                    rowSpan = 2;
                }

                cell = new TransformedLayerCell(cell) {
                    @Override
                    public ILayer getLayer() {
                        return ColumnGroupHeaderLayer.this;
                    }

                    @Override
                    public int getRowSpan() {
                        return rowSpan;
                    }
                };
            }
            return cell;
        }
    }

    /**
     * Calculates the span of a cell in a Column Group. Takes into account
     * collapsing and hidden columns in the group.
     *
     * @param columnPosition
     *            position of any column belonging to the group
     */
    protected int getColumnSpan(int columnPosition) {
        int columnIndex = getColumnIndexByPosition(columnPosition);
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(columnIndex);

        int sizeOfGroup = columnGroup.getSize();

        if (columnGroup.isCollapsed()) {
            int sizeOfStaticColumns = columnGroup.getStaticColumnIndexes().size();
            if (sizeOfStaticColumns == 0) {
                return 1;
            } else {
                sizeOfGroup = sizeOfStaticColumns;
            }
        }

        int startPositionOfGroup = getStartPositionOfGroup(columnPosition);
        int endPositionOfGroup = startPositionOfGroup + sizeOfGroup;
        List<Integer> columnIndexesInGroup = columnGroup.getMembers();

        for (int i = startPositionOfGroup; i < endPositionOfGroup; i++) {
            int index = getColumnIndexByPosition(i);
            if (!columnIndexesInGroup.contains(Integer.valueOf(index))) {
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
    private int getStartPositionOfGroup(int columnPosition) {
        int bodyColumnIndex = getColumnIndexByPosition(columnPosition);
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(bodyColumnIndex);

        int leastPossibleStartPositionOfGroup = columnPosition - (columnGroup.getSize() - 1);
        int i = 0;
        for (i = leastPossibleStartPositionOfGroup; i < columnPosition; i++) {
            if (ColumnGroupUtils.isInTheSameGroup(
                    getColumnIndexByPosition(i),
                    bodyColumnIndex,
                    this.model)) {
                break;
            }
        }
        return i;
    }

    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
        int columnIndex = getColumnIndexByPosition(columnPosition);
        if (rowPosition == 0 && this.model.isPartOfAGroup(columnIndex)) {
            return DisplayMode.NORMAL;
        } else {
            return this.columnHeaderLayer.getDisplayModeByPosition(columnPosition, rowPosition);
        }
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        int columnIndex = getColumnIndexByPosition(columnPosition);
        if (rowPosition == 0 && this.model.isPartOfAGroup(columnIndex)) {
            LabelStack stack = new LabelStack();
            if (getConfigLabelAccumulator() != null) {
                getConfigLabelAccumulator().accumulateConfigLabels(stack, columnPosition, rowPosition);
            }
            stack.addLabel(GridRegion.COLUMN_GROUP_HEADER);

            if (this.model.isPartOfACollapseableGroup(columnIndex)) {
                ColumnGroup group = this.model.getColumnGroupByIndex(columnIndex);
                if (group.isCollapsed()) {
                    stack.addLabelOnTop(DefaultColumnGroupHeaderLayerConfiguration.GROUP_COLLAPSED_CONFIG_TYPE);
                } else {
                    stack.addLabelOnTop(DefaultColumnGroupHeaderLayerConfiguration.GROUP_EXPANDED_CONFIG_TYPE);
                }
            }

            return stack;
        } else {
            return this.columnHeaderLayer.getConfigLabelsByPosition(columnPosition, rowPosition);
        }
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        int columnIndex = getColumnIndexByPosition(columnPosition);
        if (rowPosition == 0 && this.model.isPartOfAGroup(columnIndex)) {
            return this.model.getColumnGroupByIndex(columnIndex).getName();
        } else {
            return this.columnHeaderLayer.getDataValueByPosition(columnPosition, 0);
        }
    }

    @Override
    public LabelStack getRegionLabelsByXY(int x, int y) {
        int columnIndex = getColumnIndexByPosition(getColumnPositionByX(x));
        if (this.model.isPartOfAGroup(columnIndex) && y < getRowHeightByPosition(0)) {
            return new LabelStack(GridRegion.COLUMN_GROUP_HEADER);
        } else {
            return this.columnHeaderLayer.getRegionLabelsByXY(x, y - getRowHeightByPosition(0));
        }
    }

    // ColumnGroupModel delegates

    public void addColumnsIndexesToGroup(String colGroupName, int... colIndexes) {
        this.model.addColumnsIndexesToGroup(colGroupName, colIndexes);
    }

    public void clearAllGroups() {
        this.model.clear();
    }

    public void setStaticColumnIndexesByGroup(String colGroupName, int... staticColumnIndexes) {
        this.model.setStaticColumnIndexesByGroup(colGroupName, staticColumnIndexes);
    }

    public boolean isColumnInGroup(int bodyColumnIndex) {
        return this.model.isPartOfAGroup(bodyColumnIndex);
    }

    /**
     * @see ColumnGroup#setUnbreakable(boolean)
     */
    public void setGroupUnbreakable(int columnIndex) {
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(columnIndex);
        columnGroup.setUnbreakable(true);
    }

    public void setGroupAsCollapsed(int columnIndex) {
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(columnIndex);
        columnGroup.setCollapsed(true);
    }

    public boolean isCalculateHeight() {
        return this.calculateHeight;
    }

    public void setCalculateHeight(boolean calculateHeight) {
        boolean changed = calculateHeight != this.calculateHeight;
        this.calculateHeight = calculateHeight;
        if (changed) {
            if (calculateHeight) {
                this.model.registerColumnGroupModelListener(this.modelChangeListener);
            } else {
                this.model.unregisterColumnGroupModelListener(this.modelChangeListener);
            }
            this.fireLayerEvent(new RowStructuralRefreshEvent(this));
        }
    }

    /**
     * @since 1.4
     */
    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> labels = super.getProvidedLabels();

        labels.add(GridRegion.COLUMN_GROUP_HEADER);
        labels.add(DefaultColumnGroupHeaderLayerConfiguration.GROUP_COLLAPSED_CONFIG_TYPE);
        labels.add(DefaultColumnGroupHeaderLayerConfiguration.GROUP_EXPANDED_CONFIG_TYPE);

        // add the labels configured via IConfigLabelAccumulator
        if (getConfigLabelAccumulator() != null
                && getConfigLabelAccumulator() instanceof IConfigLabelProvider) {
            labels.addAll(((IConfigLabelProvider) getConfigLabelAccumulator()).getProvidedLabels());
        }

        return labels;
    }
}

/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.group.config.DefaultColumnGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.SizeConfig;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

/**
 * Adds the Column grouping functionality to the column headers. Also persists
 * the state of the column groups when
 * {@link NatTable#saveState(String, Properties)} is invoked.
 *
 * Internally uses the {@link ColumnGroupModel} to track the column groups.
 * <p>
 * See ColumnGroupGridExample
 */
public class ColumnGroupGroupHeaderLayer extends AbstractLayerTransform {

    private final SizeConfig rowHeightConfig = new SizeConfig(DataLayer.DEFAULT_ROW_HEIGHT);
    private final ColumnGroupModel model;
    private final ColumnGroupHeaderLayer columnGroupHeaderLayer;

    public ColumnGroupGroupHeaderLayer(
            ColumnGroupHeaderLayer columnGroupHeaderLayer,
            SelectionLayer selectionLayer,
            ColumnGroupModel columnGroupModel) {
        this(columnGroupHeaderLayer, selectionLayer, columnGroupModel, true);
    }

    public ColumnGroupGroupHeaderLayer(
            ColumnGroupHeaderLayer columnGroupHeaderLayer,
            SelectionLayer selectionLayer,
            ColumnGroupModel columnGroupModel,
            boolean useDefaultConfiguration) {
        super(columnGroupHeaderLayer);
        this.columnGroupHeaderLayer = columnGroupHeaderLayer;
        this.model = columnGroupModel;

        registerCommandHandler(new ConfigureScalingCommandHandler(null, this.rowHeightConfig));

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultColumnGroupHeaderLayerConfiguration(columnGroupModel));
        }
    }

    // Vertical features

    // Rows

    @Override
    public int getRowCount() {
        return this.columnGroupHeaderLayer.getRowCount() + getGroupRowCount();
    }

    private int getGroupRowCount() {
        return isGroupRowIncluded() ? 1 : 0;
    }

    @Override
    public int getPreferredRowCount() {
        return this.columnGroupHeaderLayer.getPreferredRowCount() + getGroupRowCount();
    }

    @Override
    public int getRowIndexByPosition(int rowPosition) {
        if (rowPosition == 0 && isGroupRowIncluded()) {
            return rowPosition;
        } else {
            return this.columnGroupHeaderLayer.getRowIndexByPosition(rowPosition - getGroupRowCount());
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
        return this.rowHeightConfig.getAggregateSize(1)
                + this.columnGroupHeaderLayer.getHeight();
    }

    private boolean isGroupRowIncluded() {
        return (this.model.getAllIndexesInGroups() != null
                && !this.model.getAllIndexesInGroups().isEmpty());
    }

    @Override
    public int getPreferredHeight() {
        return this.rowHeightConfig.getAggregateSize(1)
                + this.columnGroupHeaderLayer.getPreferredHeight();
    }

    @Override
    public int getRowHeightByPosition(int rowPosition) {
        if (rowPosition == 0 && isGroupRowIncluded()) {
            return this.rowHeightConfig.getSize(rowPosition);
        } else {
            return this.columnGroupHeaderLayer.getRowHeightByPosition(rowPosition - getGroupRowCount());
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
            return this.columnGroupHeaderLayer.isRowPositionResizable(rowPosition - getGroupRowCount());
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
                return 1 + this.columnGroupHeaderLayer.getRowPositionByY(y - row0Height);
            }
        } else {
            return this.columnGroupHeaderLayer.getRowPositionByY(y);
        }
    }

    @Override
    public int getStartYOfRowPosition(int rowPosition) {
        if (this.isGroupRowIncluded()) {
            if (rowPosition == 0) {
                return this.rowHeightConfig.getAggregateSize(rowPosition);
            } else {
                return getRowHeightByPosition(0)
                        + this.columnGroupHeaderLayer.getStartYOfRowPosition(rowPosition - 1);
            }
        } else {
            return this.columnGroupHeaderLayer.getStartYOfRowPosition(rowPosition);
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

        if (rowPosition == 0) {
            if (this.model.isPartOfAGroup(bodyColumnIndex)) {
                return new LayerCell(this,
                        getStartPositionOfGroup(columnPosition),
                        rowPosition,
                        columnPosition,
                        rowPosition,
                        getColumnSpan(columnPosition), 1);
            } else {
                ILayerCell underlyingCell =
                        this.columnGroupHeaderLayer.getCellByPosition(columnPosition, rowPosition);
                return new LayerCell(this,
                        underlyingCell.getOriginColumnPosition(),
                        underlyingCell.getOriginRowPosition(),
                        columnPosition,
                        rowPosition,
                        underlyingCell.getColumnSpan(),
                        underlyingCell.getRowSpan() + 1);
            }
        } else if (rowPosition == 1) {
            ILayerCell underlyingCell =
                    this.columnGroupHeaderLayer.getCellByPosition(columnPosition, rowPosition - 1);
            boolean partOfAGroup = this.model.isPartOfAGroup(bodyColumnIndex);
            return new LayerCell(this,
                    underlyingCell.getOriginColumnPosition(),
                    underlyingCell.getOriginRowPosition() + (partOfAGroup ? 1 : 0),
                    columnPosition,
                    rowPosition,
                    underlyingCell.getColumnSpan(),
                    underlyingCell.getRowSpan() + (partOfAGroup ? 0 : 1));
        } else if (rowPosition == 2) {
            ILayerCell underlyingCell =
                    this.columnGroupHeaderLayer.getCellByPosition(columnPosition, rowPosition - 1);
            boolean partOfAGroup = this.model.isPartOfAGroup(bodyColumnIndex)
                    || this.columnGroupHeaderLayer.isColumnInGroup(bodyColumnIndex);
            return new LayerCell(this,
                    underlyingCell.getOriginColumnPosition(),
                    underlyingCell.getOriginRowPosition() + (partOfAGroup ? 1 : 0),
                    columnPosition,
                    rowPosition,
                    underlyingCell.getColumnSpan(),
                    underlyingCell.getRowSpan() + (partOfAGroup ? 0 : 1));
        }
        return null;
    }

    /**
     * Calculates the span of a cell in a Column Group. Takes into account
     * collapsing and hidden columns in the group.
     *
     * @param columnPosition
     *            position of any column belonging to the group
     *
     * @since 1.6
     */
    public int getColumnSpan(int columnPosition) {
        int columnIndex = getColumnIndexByPosition(columnPosition);
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(columnIndex);

        if (columnGroup == null) {
            // this can happen in case the column group in the lower level is
            // not part of a group in this level
            return this.columnGroupHeaderLayer.getColumnSpan(columnPosition);
        }

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
     * @param columnPosition
     *            of any column belonging to the group
     * @return first position of the column group
     *
     * @since 1.6
     */
    public int getStartPositionOfGroup(int columnPosition) {
        int bodyColumnIndex = getColumnIndexByPosition(columnPosition);
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(bodyColumnIndex);

        if (columnGroup == null) {
            // this can happen in case the column group in the lower level is
            // not part of a group in this level
            return this.columnGroupHeaderLayer.getStartPositionOfGroup(columnPosition);
        }

        int leastPossibleStartPositionOfGroup = columnPosition - columnGroup.getSize();
        int i = 0;
        for (i = columnPosition; i >= leastPossibleStartPositionOfGroup; i--) {
            if (!ColumnGroupUtils.isInTheSameGroup(
                    getColumnIndexByPosition(i),
                    bodyColumnIndex,
                    this.model)) {
                // the previous position was the last index in the group
                i++;
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
        } else if (rowPosition == 1 && this.columnGroupHeaderLayer.isColumnInGroup(columnIndex)) {
            return DisplayMode.NORMAL;
        } else {
            return this.columnGroupHeaderLayer.getDisplayModeByPosition(columnPosition, rowPosition);
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
            if (rowPosition != 0) {
                rowPosition--;
            }
            return this.columnGroupHeaderLayer.getConfigLabelsByPosition(columnPosition, rowPosition);
        }
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        int columnIndex = getColumnIndexByPosition(columnPosition);
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(columnIndex);

        if (rowPosition == 0) {
            if (this.model.isPartOfAGroup(columnIndex)) {
                return columnGroup.getName();
            }
        } else {
            rowPosition--;
        }

        return this.columnGroupHeaderLayer.getDataValueByPosition(columnPosition, rowPosition);
    }

    @Override
    public LabelStack getRegionLabelsByXY(int x, int y) {
        int columnIndex = getColumnIndexByPosition(getColumnPositionByX(x));
        if (this.model.isPartOfAGroup(columnIndex) && y < getRowHeightByPosition(0)) {
            return new LabelStack(GridRegion.COLUMN_GROUP_HEADER);
        } else {
            return this.columnGroupHeaderLayer.getRegionLabelsByXY(x, y - getRowHeightByPosition(0));
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

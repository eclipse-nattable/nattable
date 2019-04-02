/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.DimensionallyDependentIndexLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DimensionallyDependentLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.command.ColumnGroupExpandCollapseCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.IndexPositionConverter;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupCollapseCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupExpandCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupReorderEndCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupReorderStartCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupsCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.GroupColumnReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.GroupColumnReorderEndCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.GroupColumnReorderStartCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.GroupMultiColumnReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.UpdateColumnGroupCollapseCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.config.DefaultColumnGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.group.performance.painter.ColumnGroupHeaderGridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.SizeConfig;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.TransformedLayerCell;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.event.ColumnReorderEvent;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiRowResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Adds the column grouping functionality to the column header. Also persists
 * the state of the column groups when
 * {@link NatTable#saveState(String, Properties)} is invoked.
 * <p>
 * Internally uses a collection of {@link GroupModel} to track the column groups
 * on multiple levels.
 * </p>
 * <p>
 * It supports multiple column grouping levels. The levels are 0 based and
 * configured bottom up. That means if 3 levels of column groups are defined,
 * the first level==0 is the bottom most rowPosition==2, and the top most
 * level==2 is on rowPosition==0.
 * </p>
 *
 * @since 1.6
 */
public class ColumnGroupHeaderLayer extends AbstractLayerTransform {

    private static final Log LOG = LogFactory.getLog(ColumnGroupHeaderLayer.class);

    private static final String PERSISTENCE_KEY_COLUMN_GROUPS = ".columnGroups"; //$NON-NLS-1$

    private final List<GroupModel> model;

    /**
     * The {@link ILayerPainter} that is used by this layer. Typically the
     * {@link ColumnGroupHeaderGridLineCellLayerPainter} to support rendering of
     * huge column group cells by inspecting the {@link #showAlwaysGroupNames}
     * attribute.
     */
    private ILayerPainter layerPainter;

    /**
     * {@link SizeConfig} instance for the row height configuration.
     */
    private final SizeConfig rowHeightConfig = new SizeConfig(DataLayer.DEFAULT_ROW_HEIGHT);

    /**
     * Flag which is used to tell the {@link ColumnGroupHeaderLayer} whether to
     * calculate the height of the layer dependent on column group configuration
     * or not. If it is set to <code>true</code> the column header will check if
     * column groups are configured and if not, the height of the column header
     * will not show the double height for showing column groups.
     */
    private boolean calculateHeight = false;

    /**
     * Flag to configure whether group names should be always visible on
     * rendering, e.g. on scrolling, or if the group names should scroll with
     * the cell. Default is <code>false</code>.
     */
    private boolean showAlwaysGroupNames = false;

    /**
     * The layer to which the positions in the group should match. Typically
     * this is the {@link SelectionLayer}.
     */
    private IUniqueIndexLayer positionLayer;

    /**
     * The converter that is used to perform the index-position conversion in a
     * {@link GroupModel}.
     */
    private IndexPositionConverter indexPositionConverter;

    /**
     * The path of {@link ILayer} from {@link #positionLayer} to this layer.
     * Needed to be able to convert the column position based on the
     * {@link #positionLayer} to a position in this layer.
     */
    private List<ILayer> layerPath;

    /**
     * Position that is tracked on column group reorder via dragging. Needed
     * because on drag operations the viewport could scroll and therefore the
     * from position is not the original one anymore.
     */
    private int reorderFromColumnPosition;

    /**
     * Map in which it is stored if reordering is supported per level.
     */
    private Map<Integer, Boolean> reorderSupportedOnLevel = new HashMap<Integer, Boolean>();

    /**
     * The {@link CompositeFreezeLayer} in case it is part of the layer
     * composition. Needed to deal with groups in frozen state as column
     * positions could get ambiguous on scrolling.
     */
    private CompositeFreezeLayer compositeFreezeLayer;

    /**
     * Creates a {@link ColumnGroupHeaderLayer} with the specified
     * configurations and one grouping level. Uses the SelectionLayer as
     * positionLayer and the default configuration.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the ColumnHeaderLayer.
     * @param selectionLayer
     *            The SelectionLayer needed for command handlers that inspect
     *            the selection on handling.
     */
    public ColumnGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            SelectionLayer selectionLayer) {

        this(underlyingHeaderLayer, selectionLayer, selectionLayer, 1, true);
    }

    /**
     * Creates a {@link ColumnGroupHeaderLayer} with the specified
     * configurations. Uses the SelectionLayer as positionLayer and the default
     * configuration.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the ColumnHeaderLayer.
     * @param selectionLayer
     *            The SelectionLayer needed for command handlers that inspect
     *            the selection on handling.
     * @param numberOfGroupLevels
     *            The number of group levels that should be supported.
     *            Additional levels can also be added via
     *            {@link #addGroupingLevel()}.
     */
    public ColumnGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            SelectionLayer selectionLayer,
            int numberOfGroupLevels) {

        this(underlyingHeaderLayer, selectionLayer, selectionLayer, numberOfGroupLevels, true);
    }

    /**
     * Creates a {@link ColumnGroupHeaderLayer} with the specified
     * configurations and one grouping level. Uses the default configuration.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the ColumnHeaderLayer.
     * @param positionLayer
     *            The positionLayer to which this layer should be mapped to,
     *            needed to handle column position transformations without
     *            taking the viewport into account. Typically the
     *            SelectionLayer.
     * @param selectionLayer
     *            The SelectionLayer needed for command handlers that inspect
     *            the selection on handling.
     */
    public ColumnGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            IUniqueIndexLayer positionLayer,
            SelectionLayer selectionLayer) {

        this(underlyingHeaderLayer, positionLayer, selectionLayer, 1, true);
    }

    /**
     * Creates a {@link ColumnGroupHeaderLayer} with the specified
     * configurations. Uses the default configuration.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the ColumnHeaderLayer.
     * @param positionLayer
     *            The positionLayer to which this layer should be mapped to,
     *            needed to handle column position transformations without
     *            taking the viewport into account. Typically the
     *            SelectionLayer.
     * @param selectionLayer
     *            The SelectionLayer needed for command handlers that inspect
     *            the selection on handling.
     * @param numberOfGroupLevels
     *            The number of group levels that should be supported.
     *            Additional levels can also be added via
     *            {@link #addGroupingLevel()}.
     */
    public ColumnGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            IUniqueIndexLayer positionLayer,
            SelectionLayer selectionLayer,
            int numberOfGroupLevels) {

        this(underlyingHeaderLayer, positionLayer, selectionLayer, numberOfGroupLevels, true);
    }

    /**
     * Creates a {@link ColumnGroupHeaderLayer} with the specified
     * configurations. Takes the {@link SelectionLayer} as positionLayer.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the ColumnHeaderLayer.
     * @param selectionLayer
     *            The SelectionLayer needed for command handlers that inspect
     *            the selection on handling.
     * @param numberOfGroupLevels
     *            The number of group levels that should be supported.
     *            Additional levels can also be added via
     *            {@link #addGroupingLevel()}.
     * @param useDefaultConfiguration
     *            <code>true</code> if the default configuration should be
     *            applied, <code>false</code> if a custom configuration will be
     *            applied afterwards.
     */
    public ColumnGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            SelectionLayer selectionLayer,
            int numberOfGroupLevels,
            boolean useDefaultConfiguration) {

        this(underlyingHeaderLayer, selectionLayer, selectionLayer, numberOfGroupLevels, useDefaultConfiguration);
    }

    /**
     * Creates a {@link ColumnGroupHeaderLayer} with one grouping level and the
     * specified configurations. Takes the {@link SelectionLayer} as
     * positionLayer.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the ColumnHeaderLayer.
     * @param selectionLayer
     *            The SelectionLayer needed for command handlers that inspect
     *            the selection on handling.
     * @param useDefaultConfiguration
     *            <code>true</code> if the default configuration should be
     *            applied, <code>false</code> if a custom configuration will be
     *            applied afterwards.
     */
    public ColumnGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            SelectionLayer selectionLayer,
            boolean useDefaultConfiguration) {

        this(underlyingHeaderLayer, selectionLayer, selectionLayer, 1, useDefaultConfiguration);
    }

    /**
     * Creates a {@link ColumnGroupHeaderLayer} with one grouping level and the
     * specified configurations.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the ColumnHeaderLayer.
     * @param positionLayer
     *            The positionLayer to which this layer should be mapped to,
     *            needed to handle column position transformations without
     *            taking the viewport into account. Typically the
     *            SelectionLayer.
     * @param selectionLayer
     *            The SelectionLayer needed for command handlers that inspect
     *            the selection on handling.
     * @param useDefaultConfiguration
     *            <code>true</code> if the default configuration should be
     *            applied, <code>false</code> if a custom configuration will be
     *            applied afterwards.
     */
    public ColumnGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            IUniqueIndexLayer positionLayer,
            SelectionLayer selectionLayer,
            boolean useDefaultConfiguration) {
        this(underlyingHeaderLayer, selectionLayer, selectionLayer, 1, useDefaultConfiguration);
    }

    /**
     * Creates a {@link ColumnGroupHeaderLayer} with the specified
     * configurations.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the ColumnHeaderLayer.
     * @param positionLayer
     *            The positionLayer to which this layer should be mapped to,
     *            needed to handle column position transformations without
     *            taking the viewport into account. Typically the
     *            SelectionLayer.
     * @param selectionLayer
     *            The SelectionLayer needed for command handlers that inspect
     *            the selection on handling.
     * @param numberOfGroupLevels
     *            The number of group levels that should be supported.
     *            Additional levels can also be added via
     *            {@link #addGroupingLevel()}.
     * @param useDefaultConfiguration
     *            <code>true</code> if the default configuration should be
     *            applied, <code>false</code> if a custom configuration will be
     *            applied afterwards.
     */
    public ColumnGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            IUniqueIndexLayer positionLayer,
            SelectionLayer selectionLayer,
            int numberOfGroupLevels,
            boolean useDefaultConfiguration) {

        super(underlyingHeaderLayer);

        this.positionLayer = positionLayer;
        this.indexPositionConverter = new GroupModel.IndexPositionConverter() {

            @Override
            public int convertPositionToIndex(IUniqueIndexLayer positionLayer, int position) {
                return positionLayer.getColumnIndexByPosition(position);
            }

            @Override
            public int convertIndexToPosition(IUniqueIndexLayer positionLayer, int index) {
                return positionLayer.getColumnPositionByIndex(index);
            }
        };

        this.model = new ArrayList<GroupModel>(numberOfGroupLevels);
        for (int i = 0; i < numberOfGroupLevels; i++) {
            GroupModel groupModel = new GroupModel();
            groupModel.setPositionLayer(this.positionLayer, this.indexPositionConverter);
            this.model.add(groupModel);
            this.reorderSupportedOnLevel.put(i, Boolean.TRUE);
        }

        this.layerPath = findLayerPath(this, 0);
        this.layerPainter = new ColumnGroupHeaderGridLineCellLayerPainter(this);

        // add listener on dependent layer to be notified about structural
        // changes
        positionLayer.addLayerListener(new StructuralChangeLayerListener());

        registerCommandHandlers(selectionLayer);

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultColumnGroupHeaderLayerConfiguration(false));
        }
    }

    @Override
    public ILayerPainter getLayerPainter() {
        return this.layerPainter;
    }

    @Override
    public void setLayerPainter(ILayerPainter layerPainter) {
        this.layerPainter = layerPainter;
    }

    /**
     * Register command handlers for this layer.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} needed for handling selections on
     *            grouping/ungrouping.
     */
    protected void registerCommandHandlers(SelectionLayer selectionLayer) {
        registerCommandHandler(new ColumnGroupsCommandHandler(this, selectionLayer));
        registerCommandHandler(new ConfigureScalingCommandHandler(null, this.rowHeightConfig));

        // group reordering
        registerCommandHandler(new ColumnGroupReorderCommandHandler(this));
        registerCommandHandler(new ColumnGroupReorderStartCommandHandler(this));
        registerCommandHandler(new ColumnGroupReorderEndCommandHandler(this));
        // ReorderColumnsAndGroupsCommand ???

        // register command handlers to add checks if a reordering is valid in
        // case of unbreakable groups
        getPositionLayer().registerCommandHandler(new GroupColumnReorderCommandHandler(this));
        getPositionLayer().registerCommandHandler(new GroupColumnReorderStartCommandHandler(this));
        getPositionLayer().registerCommandHandler(new GroupColumnReorderEndCommandHandler(this));
        getPositionLayer().registerCommandHandler(new GroupMultiColumnReorderCommandHandler(this));
    }

    /**
     * Convenience method to get the {@link GroupModel} on level 0. Useful for
     * single level column grouping.
     *
     * @return The {@link GroupModel} for level 0.
     */
    public GroupModel getGroupModel() {
        return getGroupModel(0);
    }

    /**
     * Return the {@link GroupModel} for the given grouping level. Note that the
     * levels are bottom up, so level 0 is the bottom most grouping level.
     *
     * @param level
     *            The grouping level. Value is bottom up.
     * @return The {@link GroupModel} for the corresponding level.
     */
    public GroupModel getGroupModel(int level) {
        if (level >= this.model.size()) {
            LOG.warn("tried to add a group on a non-existent level"); //$NON-NLS-1$
            return null;
        }
        return this.model.get(level);
    }

    /**
     * Adds a new grouping level on top.
     */
    public void addGroupingLevel() {
        GroupModel groupModel = new GroupModel();
        groupModel.setPositionLayer(getPositionLayer(), this.indexPositionConverter);
        this.model.add(groupModel);
        this.reorderSupportedOnLevel.put(this.model.size() - 1, Boolean.TRUE);
    }

    /**
     *
     * @return The number of grouping levels configured in this layer.
     */
    public int getLevelCount() {
        return this.model.size();
    }

    /**
     *
     * @return The layer to which the positions in the group should match.
     *         Typically this is the {@link SelectionLayer}.
     */
    public IUniqueIndexLayer getPositionLayer() {
        return this.positionLayer;
    }

    /**
     * Calculates the path of {@link ILayer} from {@link #positionLayer} to the
     * given layer.
     *
     * @param layer
     *            The {@link ILayer} for which the path is requested.
     * @return The path of {@link ILayer} from the {@link #positionLayer} to the
     *         given {@link ILayer} or <code>null</code> if a direct path is not
     *         available.
     */
    List<ILayer> findLayerPath(ILayer layer, int columnPosition) {

        if (layer == getPositionLayer()) {
            List<ILayer> result = new ArrayList<ILayer>();
            result.add(layer);
            return result;
        }

        if (this.compositeFreezeLayer == null && layer instanceof CompositeFreezeLayer) {
            this.compositeFreezeLayer = (CompositeFreezeLayer) layer;
        }

        // handle collection
        List<ILayer> result = null;
        Collection<ILayer> underlyingLayers = layer.getUnderlyingLayersByColumnPosition(columnPosition);
        if (underlyingLayers != null) {
            for (ILayer underlyingLayer : underlyingLayers) {
                if (underlyingLayer != null) {
                    result = findLayerPath(underlyingLayer, columnPosition);
                }
            }
        }

        // handle horizontal dependency
        if (result == null && layer instanceof DimensionallyDependentLayer) {
            result = findLayerPath(((DimensionallyDependentLayer) layer).getHorizontalLayerDependency(), columnPosition);
        }
        if (result == null && this.underlyingLayer instanceof DimensionallyDependentIndexLayer) {
            result = findLayerPath(((DimensionallyDependentIndexLayer) layer).getHorizontalLayerDependency(), columnPosition);
        }

        if (result != null) {
            result.add(layer);
        }

        return result;
    }

    /**
     * Converts the given column position the {@link #layerPath} upwards.
     *
     * @param columnPosition
     *            The column position to convert.
     * @return The upwards converted column position.
     */
    protected int convertColumnPositionUpwards(int columnPosition) {
        int converted = columnPosition;

        // This could be for example when the CompositeFreezeLayer is in the
        // composition. At creation time the underlying layers would be empty
        // because the width is not yet calculated.
        List<ILayer> path = this.layerPath;
        if (path == null) {
            path = findLayerPath(this, columnPosition);
        }

        if (path != null) {
            for (int i = 0; i < path.size() - 1; i++) {
                ILayer underlying = path.get(i);
                ILayer upper = path.get(i + 1);
                converted = upper.underlyingToLocalColumnPosition(underlying, converted);
            }
        }
        return converted;
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof ColumnGroupExpandCollapseCommand
                && command.convertToTargetLayer(getPositionLayer())) {
            // only ColumnGroupExpandCollapseCommand needs to be converted to
            // positionLayer so also currently not visible column groups can be
            // expanded/collapsed, e.g. via ColumnChooser
            ColumnGroupExpandCollapseCommand cmd = (ColumnGroupExpandCollapseCommand) command;
            int rowPosition = cmd.getLocalRowPosition(this);
            int level = getLevelForRowPosition(rowPosition);
            int columnPosition = cmd.getColumnPosition();

            GroupModel groupModel = getGroupModel(level);
            if (groupModel != null) {
                Group group = groupModel.getGroupByPosition(columnPosition);
                if (group != null && group.isCollapsed()) {
                    expandGroup(groupModel, group);
                } else if (group != null && !group.isCollapsed()) {
                    collapseGroup(groupModel, group);
                }
            }
            return true;
        } else if (command instanceof RowResizeCommand && command.convertToTargetLayer(this)) {
            RowResizeCommand rowResizeCommand = (RowResizeCommand) command;
            int newRowHeight = rowResizeCommand.downScaleValue()
                    ? this.rowHeightConfig.downScale(rowResizeCommand.getNewHeight())
                    : rowResizeCommand.getNewHeight();

            setRowHeight(rowResizeCommand.getRowPosition(), newRowHeight);
            fireLayerEvent(new RowResizeEvent(this, rowResizeCommand.getRowPosition()));
            return true;
        } else if (command instanceof MultiRowResizeCommand && command.convertToTargetLayer(this)) {
            MultiRowResizeCommand rowResizeCommand = (MultiRowResizeCommand) command;
            for (int row : rowResizeCommand.getRowPositions()) {
                int newRowHeight = rowResizeCommand.downScaleValue()
                        ? this.rowHeightConfig.downScale(rowResizeCommand.getRowHeight(row))
                        : rowResizeCommand.getRowHeight(row);

                setRowHeight(row, newRowHeight);
                fireLayerEvent(new RowResizeEvent(this, row));
                // do not consume as additional rows might need to get updated
                // too
            }
        }
        return super.doCommand(command);
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        super.saveState(prefix, properties);
        int level = 0;
        for (GroupModel groupModel : this.model) {
            groupModel.saveState(prefix + PERSISTENCE_KEY_COLUMN_GROUPS + "_" + level, properties); //$NON-NLS-1$
            level++;
        }
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        super.loadState(prefix, properties);
        // expand all currently collapsed groups
        expandAllGroups();
        int level = 0;
        for (GroupModel groupModel : this.model) {
            // load the group model
            groupModel.loadState(prefix + PERSISTENCE_KEY_COLUMN_GROUPS + "_" + level, properties); //$NON-NLS-1$
            // trigger real collapse of collapsed groups in model
            List<Group> collapsedGroups = new ArrayList<Group>();
            for (Group group : groupModel.getGroups()) {
                if (group.isCollapsed()) {
                    collapsedGroups.add(group);
                }
            }
            if (!collapsedGroups.isEmpty()) {
                doCommand(new ColumnGroupCollapseCommand(groupModel, collapsedGroups));
            }
            level++;
        }

        fireLayerEvent(new ColumnStructuralRefreshEvent(this));
    }

    // Vertical features

    // Rows

    @Override
    public int getRowCount() {
        return this.underlyingLayer.getRowCount() + this.model.size();
    }

    @Override
    public int getPreferredRowCount() {
        return this.underlyingLayer.getPreferredRowCount() + this.model.size();
    }

    @Override
    public int getRowIndexByPosition(int rowPosition) {
        int rowCount = this.model.size();
        if (rowPosition < rowCount) {
            return rowPosition;
        } else {
            return this.underlyingLayer.getRowIndexByPosition(rowPosition - rowCount);
        }
    }

    @Override
    public int localToUnderlyingRowPosition(int localRowPosition) {
        int rowCount = this.model.size();
        if (localRowPosition < rowCount) {
            return localRowPosition;
        } else {
            return localRowPosition - rowCount;
        }
    }

    // Height

    private int getGroupingHeight() {
        if (!this.calculateHeight) {
            return this.rowHeightConfig.getAggregateSize(this.model.size());
        }

        int height = 0;
        for (int i = 0; i < this.model.size(); i++) {
            GroupModel groupModel = this.model.get(i);
            if (!groupModel.isEmpty()) {
                height += this.rowHeightConfig.getSize(getRowPositionForLevel(i));
            }
        }
        return height;
    }

    @Override
    public int getHeight() {
        return getGroupingHeight() + this.underlyingLayer.getHeight();
    }

    @Override
    public int getPreferredHeight() {
        return getGroupingHeight() + this.underlyingLayer.getPreferredHeight();
    }

    @Override
    public int getRowHeightByPosition(int rowPosition) {
        int rowCount = this.model.size();
        if (rowPosition < rowCount) {
            if (!this.calculateHeight) {
                return this.rowHeightConfig.getSize(rowPosition);
            } else {
                int level = getLevelForRowPosition(rowPosition);
                return getGroupModel(level).isEmpty() ? 0 : this.rowHeightConfig.getSize(rowPosition);
            }
        } else {
            return this.underlyingLayer.getRowHeightByPosition(rowPosition - rowCount);
        }
    }

    /**
     * Set the row height for grouping level 0.
     *
     * @param rowHeight
     *            The height to set for grouping level 0.
     */
    public void setRowHeight(int rowHeight) {
        setRowHeight(getRowPositionForLevel(0), rowHeight);
    }

    /**
     * Set the row height for the given row in this layer.
     * <p>
     * <b>Note: </b> Use {@link #getLevelForRowPosition(int)} if the row
     * position for a level needs to be determined.
     * </p>
     *
     * @param row
     *            The row whose height should be set.
     * @param rowHeight
     *            The height to set for the given row position.
     */
    public void setRowHeight(int row, int rowHeight) {
        this.rowHeightConfig.setSize(row, rowHeight);
    }

    /**
     *
     * @param level
     *            The level for which the row position is requested.
     * @return The row positions for the given grouping level.
     */
    public int getRowPositionForLevel(int level) {
        return this.model.size() - level - 1;
    }

    /**
     *
     * @param rowPosition
     *            The row position for which the level is requested.
     * @return The level for the given row position.
     */
    public int getLevelForRowPosition(int rowPosition) {
        return this.model.size() - rowPosition - 1;
    }

    // Row resize

    @Override
    public boolean isRowPositionResizable(int rowPosition) {
        int rowCount = this.model.size();
        if (rowPosition < rowCount) {
            return this.rowHeightConfig.isPositionResizable(rowPosition);
        } else {
            return this.underlyingLayer.isRowPositionResizable(rowPosition - rowCount);
        }
    }

    // Y

    @Override
    public int getRowPositionByY(int y) {
        int groupHeight = getGroupingHeight();
        if (y <= groupHeight) {
            return LayerUtil.getRowPositionByY(this, y);
        } else {
            return this.model.size() + this.underlyingLayer.getRowPositionByY(y - groupHeight);
        }
    }

    @Override
    public int getStartYOfRowPosition(int rowPosition) {
        int rowCount = this.model.size();
        if (rowPosition < rowCount) {
            if (!this.calculateHeight) {
                return this.rowHeightConfig.getAggregateSize(rowPosition);
            } else {
                int startY = 0;
                for (int i = 0; i < rowPosition; i++) {
                    GroupModel groupModel = this.model.get(i);
                    if (!groupModel.isEmpty()) {
                        startY += this.rowHeightConfig.getSize(getRowPositionForLevel(i));
                    }
                }
                return startY;
            }
        } else {
            return getGroupingHeight()
                    + this.underlyingLayer.getStartYOfRowPosition(rowPosition - this.model.size());
        }
    }

    // Cell features

    @Override
    public ILayerCell getCellByPosition(final int columnPosition, final int rowPosition) {
        // Column group header cell
        if (rowPosition < this.model.size()) {
            int level = getLevelForRowPosition(rowPosition);
            Group group = getGroupByPosition(level, columnPosition);
            if (group != null) {
                int start = convertColumnPositionUpwards(getPositionLayer().getColumnPositionByIndex(group.getVisibleStartIndex()));

                // check if there is a level above that does not have a group
                int row = rowPosition;
                int rowSpan = 1;
                while (level < (this.model.size() - 1)) {
                    level++;
                    Group upperGroup = getGroupByPosition(level, columnPosition);
                    if (upperGroup == null) {
                        row--;
                        rowSpan++;
                    } else {
                        break;
                    }
                }

                // if the header should be shown always, e.g. because of
                // huge column groups, the start will not below 0 and the
                // end not below column count
                int columnSpan = getColumnSpan(group);
                if (this.showAlwaysGroupNames) {
                    if (start < 0) {
                        columnSpan += start;
                        start = 0;
                    }

                    if (start + columnSpan > getColumnCount()) {
                        columnSpan = getColumnCount() - start;
                    }
                }

                return new LayerCell(
                        this,
                        start,
                        row,
                        columnPosition,
                        rowPosition,
                        columnSpan,
                        rowSpan);
            } else {
                // for the level there is no group, check if the level below has
                // a group to calculate the row spanning
                int rowSpan = 2;
                Group subGroup = null;
                while (level > 0) {
                    level--;
                    group = getGroupByPosition(level, columnPosition);
                    if (group == null) {
                        rowSpan++;
                    } else {
                        subGroup = group;
                    }
                }

                if (subGroup != null) {
                    int start = convertColumnPositionUpwards(getPositionLayer().getColumnPositionByIndex(subGroup.getVisibleStartIndex()));
                    int columnSpan = getColumnSpan(subGroup);

                    // if the header should be shown always, e.g. because of
                    // huge column groups, the start will not below 0 and the
                    // end not below column count
                    if (this.showAlwaysGroupNames) {
                        if (start < 0) {
                            columnSpan += start;
                            start = 0;
                        }

                        if (start + columnSpan > getColumnCount()) {
                            columnSpan = getColumnCount() - start;
                        }
                    }

                    return new LayerCell(
                            this,
                            start,
                            rowPosition,
                            columnPosition,
                            rowPosition,
                            columnSpan,
                            rowSpan);
                } else {
                    // get the cell from the underlying layer
                    final int span = rowSpan;
                    ILayerCell cell = this.underlyingLayer.getCellByPosition(columnPosition, 0);
                    if (cell != null) {
                        cell = new TransformedLayerCell(cell) {
                            @Override
                            public ILayer getLayer() {
                                return ColumnGroupHeaderLayer.this;
                            }

                            @Override
                            public int getRowSpan() {
                                return span;
                            }

                            @Override
                            public int getRowPosition() {
                                return rowPosition;
                            }

                            @Override
                            public int getOriginRowPosition() {
                                return rowPosition;
                            }
                        };
                    }
                    return cell;
                }
            }
        } else {
            // check if one row above has a group
            int level = getLevelForRowPosition(rowPosition - 1);

            Group group = null;
            int rowSpan = 1;
            while (level < this.model.size()) {
                group = getGroupByPosition(level, columnPosition);
                if (group == null) {
                    rowSpan++;
                } else {
                    break;
                }
                level++;
            }

            final int span = rowSpan;
            ILayerCell cell = this.underlyingLayer.getCellByPosition(columnPosition, 0);
            if (cell != null) {
                cell = new TransformedLayerCell(cell) {
                    @Override
                    public ILayer getLayer() {
                        return ColumnGroupHeaderLayer.this;
                    }

                    @Override
                    public int getRowSpan() {
                        return span;
                    }

                    @Override
                    public int getRowPosition() {
                        return rowPosition;
                    }

                    @Override
                    public int getOriginRowPosition() {
                        return rowPosition - (span - 1);
                    }
                };
            }
            return cell;
        }
    }

    @Override
    public Rectangle getBoundsByPosition(int columnPosition, int rowPosition) {
        Rectangle bounds = super.getBoundsByPosition(columnPosition, rowPosition);
        if (this.compositeFreezeLayer != null && this.compositeFreezeLayer.isFrozen()) {
            // if we are have a composition with freeze and there is a freeze
            // region active, we need to perform some special bound calculation
            // because the origin column positions of a spanned cell could be
            // ambiguous on scrolling
            ILayerCell cell = getCellByPosition(columnPosition, rowPosition);
            int[] columnBounds = this.compositeFreezeLayer.getColumnBounds(
                    columnPosition,
                    cell.getOriginColumnPosition(),
                    cell.getOriginColumnPosition() + cell.getColumnSpan() - 1);
            bounds.x = columnBounds[0];
            bounds.width = columnBounds[1];
        }

        return bounds;
    }

    /**
     * Get the {@link Group} for the column at the given column position for
     * level 0. Will transform the given column position to a position matching
     * the position layer for correct resolution.
     *
     * @param columnPosition
     *            The column position related to this layer.
     * @return The {@link Group} at the given column position or
     *         <code>null</code> if there is no {@link Group} at this position.
     */
    public Group getGroupByPosition(int columnPosition) {
        return getGroupByPosition(0, columnPosition);
    }

    /**
     * Get the {@link Group} for the column at the given column position for the
     * given grouping level. Will transform the given column position to a
     * position matching the position layer for correct resolution.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param columnPosition
     *            The column position related to this layer.
     * @return The {@link Group} at the given column position or
     *         <code>null</code> if there is no {@link Group} at this position.
     */
    public Group getGroupByPosition(int level, int columnPosition) {
        // calculate the position matching the position layer
        int posColumn = LayerUtil.convertColumnPosition(this, columnPosition, getPositionLayer());
        if (posColumn > -1) {
            GroupModel groupModel = getGroupModel(level);
            if (groupModel != null) {
                Group group = groupModel.getGroupByPosition(posColumn);
                return group;
            }
        }
        return null;
    }

    /**
     * Checks if there is a {@link Group} configured for the given column
     * position at any level.
     *
     * @param columnPosition
     *            The column position related to this layer.
     * @return <code>true</code> if there is a {@link Group} at the given column
     *         position, <code>false</code> if not.
     */
    public boolean isPartOfAGroup(int columnPosition) {
        for (int level = 0; level < this.model.size(); level++) {
            if (isPartOfAGroup(level, columnPosition)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is a {@link Group} configured for the given column
     * position at the given level.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param columnPosition
     *            The column position related to this layer.
     * @return <code>true</code> if there is a {@link Group} at the given column
     *         position, <code>false</code> if not.
     */
    public boolean isPartOfAGroup(int level, int columnPosition) {
        Group group = getGroupByPosition(level, columnPosition);
        return group != null;
    }

    /**
     * Check if the specified position belongs to a {@link Group} and if this
     * {@link Group} is unbreakable. Convenience method for checks on level 0.
     *
     * @param columnPosition
     *            The position used to retrieve the corresponding group related
     *            to this layer.
     * @return <code>true</code> if the specified position belongs to a
     *         {@link Group} and this {@link Group} is unbreakable,
     *         <code>false</code> if not.
     */
    public boolean isPartOfAnUnbreakableGroup(int columnPosition) {
        return isPartOfAnUnbreakableGroup(0, columnPosition);
    }

    /**
     * Check if the specified position belongs to a {@link Group} at the
     * specified level and if this {@link Group} is unbreakable.
     *
     * @param level
     *            The level for which the check should be performed.
     * @param columnPosition
     *            The position used to retrieve the corresponding group related
     *            to this layer.
     * @return <code>true</code> if the specified position belongs to a
     *         {@link Group} at the specified level and this {@link Group} is
     *         unbreakable, <code>false</code> if not.
     */
    public boolean isPartOfAnUnbreakableGroup(int level, int columnPosition) {
        Group group = getGroupByPosition(level, columnPosition);
        if (group != null) {
            return group.isUnbreakable();
        }
        return false;
    }

    /**
     * Calculates the span of a cell in a group. Takes into account collapsed
     * and hidden columns in the group.
     *
     * @param group
     *            the group for which the span should be calculated.
     */
    public int getColumnSpan(Group group) {
        int sizeOfGroup = group.getVisibleSpan();

        if (group.isCollapsed()) {
            int sizeOfStaticColumns = group.getStaticIndexes().size();
            if (sizeOfStaticColumns == 0) {
                return 1;
            } else {
                int staticSize = 0;
                for (int index : group.getStaticIndexes()) {
                    if (getPositionLayer().getColumnPositionByIndex(index) >= 0) {
                        staticSize++;
                    }
                }
                sizeOfGroup = staticSize;
            }
        }

        return sizeOfGroup;
    }

    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
        if (rowPosition < this.model.size() && isPartOfAGroup(getLevelForRowPosition(rowPosition), columnPosition)) {
            return DisplayMode.NORMAL;
        } else {
            return this.underlyingLayer.getDisplayModeByPosition(columnPosition, rowPosition);
        }
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        if (rowPosition < this.model.size() && isPartOfAGroup(getLevelForRowPosition(rowPosition), columnPosition)) {
            LabelStack stack = new LabelStack();
            if (getConfigLabelAccumulator() != null) {
                getConfigLabelAccumulator().accumulateConfigLabels(stack, columnPosition, rowPosition);
            }
            stack.addLabel(GridRegion.COLUMN_GROUP_HEADER);

            Group group = getGroupByPosition(getLevelForRowPosition(rowPosition), columnPosition);
            if (group != null && group.isCollapseable()) {
                if (group.isCollapsed()) {
                    stack.addLabelOnTop(DefaultColumnGroupHeaderLayerConfiguration.GROUP_COLLAPSED_CONFIG_TYPE);
                } else {
                    stack.addLabelOnTop(DefaultColumnGroupHeaderLayerConfiguration.GROUP_EXPANDED_CONFIG_TYPE);
                }
            }

            return stack;
        } else {
            return this.underlyingLayer.getConfigLabelsByPosition(columnPosition, rowPosition);
        }
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        if (rowPosition < this.model.size()) {
            int level = getLevelForRowPosition(rowPosition);
            Group group = getGroupByPosition(level, columnPosition);
            while (group == null && level > 0) {
                level--;
                group = getGroupByPosition(level, columnPosition);
            }

            if (group != null) {
                return group.getName();
            }
        }

        return this.underlyingLayer.getDataValueByPosition(columnPosition, 0);
    }

    @Override
    public LabelStack getRegionLabelsByXY(int x, int y) {
        if (y < getGroupingHeight()) {
            for (int i = 0; i < this.model.size(); i++) {
                if (isPartOfAGroup(i, getColumnPositionByX(x))) {
                    return new LabelStack(GridRegion.COLUMN_GROUP_HEADER);
                }
            }
        }

        return this.underlyingLayer.getRegionLabelsByXY(x, y - getGroupingHeight());
    }

    // GroupModel delegates

    /**
     * Returns the {@link Group} for the given name at level 0.
     *
     * @param name
     *            The name of the requested group.
     * @return The group with the given group name or <code>null</code> if there
     *         is no group with such a name.
     */
    public Group getGroupByName(String name) {
        return getGroupByName(0, name);
    }

    /**
     * Returns the {@link Group} for the given name.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param name
     *            The name of the requested group.
     * @return The group with the given group name or <code>null</code> if there
     *         is no group with such a name.
     */
    public Group getGroupByName(int level, String name) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            return groupModel.getGroupByName(name);
        }
        return null;
    }

    /**
     * Adds the given positions to the group with the given name.
     *
     * @param groupName
     *            The name of the group to which the given positions should be
     *            added.
     * @param positions
     *            The positions to add corresponding to this layer.
     */
    public void addPositionsToGroup(String groupName, int... positions) {
        addPositionsToGroup(0, groupName, positions);
    }

    /**
     * Adds the given positions to the group with the given name.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param groupName
     *            The name of the group to which the given positions should be
     *            added.
     * @param positions
     *            The positions to add corresponding to this layer.
     */
    public void addPositionsToGroup(int level, String groupName, int... positions) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            Group group = groupModel.getGroupByName(groupName);
            if (group != null) {
                addPositionsToGroup(group, positions);
            }
        }
    }

    /**
     * Adds the given positions to the group to which the given column position
     * belongs to.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param columnPosition
     *            The column position related to this layer to get the
     *            corresponding group to which the given positions should be
     *            added.
     * @param positions
     *            The positions to add corresponding to this layer.
     */
    public void addPositionsToGroup(int level, int columnPosition, int... positions) {
        Group group = getGroupByPosition(level, columnPosition);
        if (group != null) {
            addPositionsToGroup(group, positions);
        }
    }

    /**
     * Adds the given positions to the given {@link Group}.
     *
     * @param group
     *            The {@link Group} to which the positions should be added.
     * @param positions
     *            The positions to add corresponding to this layer.
     */
    protected void addPositionsToGroup(Group group, int... positions) {
        addPositionsToGroup(0, group, positions);
    }

    /**
     * Adds the given positions to the given {@link Group}.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param group
     *            The {@link Group} to which the positions should be added.
     * @param positions
     *            The positions to add corresponding to this layer.
     */
    protected void addPositionsToGroup(int level, Group group, int... positions) {
        int[] converted = new int[positions.length];
        for (int pos = 0; pos < positions.length; pos++) {
            // calculate the position matching the position layer
            converted[pos] = LayerUtil.convertColumnPosition(this, positions[pos], getPositionLayer());
        }

        if (group.isCollapsed()) {
            getPositionLayer().doCommand(new ColumnGroupExpandCommand(getGroupModel(level), group));
        }

        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.addPositionsToGroup(group, converted);
        }

        fireLayerEvent(new RowStructuralRefreshEvent(ColumnGroupHeaderLayer.this.underlyingLayer));
    }

    /**
     * Removes the given positions from corresponding groups. Only performs an
     * action if the position is part of the group.
     * <p>
     * <b>Note:</b><br>
     * A removal will only happen for columns at the beginning or the end of a
     * group. Removing a position in the middle will cause removal of columns at
     * the end of the group to avoid splitting a group.
     * </p>
     * <p>
     * <b>Note:</b><br>
     * A removal does only work for visible positions. That means removing
     * something from a collapsed group does not work.
     * </p>
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param positions
     *            The positions to remove corresponding to this layer.
     */
    public void removePositionsFromGroup(int level, int... positions) {
        int[] converted = new int[positions.length];
        for (int pos = 0; pos < positions.length; pos++) {
            // calculate the position matching the position layer
            converted[pos] = LayerUtil.convertColumnPosition(this, positions[pos], getPositionLayer());
        }

        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            for (int i = converted.length - 1; i >= 0; i--) {
                int pos = converted[i];
                Group group = groupModel.getGroupByPosition(pos);
                if (group.isCollapsed()) {
                    getPositionLayer().doCommand(new ColumnGroupExpandCommand(groupModel, group));
                }
                groupModel.removePositionsFromGroup(group, pos);
            }
        }

        // fire the event to update column group rendering in case calculate
        // height is enabled
        if (this.calculateHeight) {
            fireLayerEvent(new RowStructuralRefreshEvent(ColumnGroupHeaderLayer.this.underlyingLayer));
        }
    }

    /**
     * Creates and adds a group.
     *
     * @param groupName
     *            The name of the group. Typically used as value in the cell.
     * @param startIndex
     *            The index of the first item in the group.
     * @param span
     *            The configured number of items that belong to this group.
     */
    public void addGroup(String groupName, int startIndex, int span) {
        addGroup(0, groupName, startIndex, span);
    }

    /**
     * Creates and adds a group.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param groupName
     *            The name of the group. Typically used as value in the cell.
     * @param startIndex
     *            The index of the first item in the group.
     * @param span
     *            The configured number of items that belong to this group.
     */
    public void addGroup(int level, String groupName, int startIndex, int span) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.addGroup(groupName, startIndex, span);
        }
    }

    /**
     * Removes the group identified by the given name.
     *
     * @param groupName
     *            The name of the group to remove.
     */
    public void removeGroup(String groupName) {
        removeGroup(0, groupName);
    }

    /**
     * Removes the group identified by the given name.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param groupName
     *            The name of the group to remove.
     */
    public void removeGroup(int level, String groupName) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            Group group = groupModel.removeGroup(groupName);
            if (group != null && group.isCollapsed()) {
                getPositionLayer().doCommand(new ColumnGroupExpandCommand(getGroupModel(level), group));
            }
        }
    }

    /**
     * Removes the group identified by the given column position.
     *
     * @param columnPosition
     *            The group that contains the given column position.
     */
    public void removeGroup(int columnPosition) {
        removeGroup(0, columnPosition);
    }

    /**
     * Removes the group identified by the given column position.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param columnPosition
     *            The group that contains the given column position.
     */
    public void removeGroup(int level, int columnPosition) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            Group group = groupModel.removeGroup(columnPosition);
            if (group != null && group.isCollapsed()) {
                getPositionLayer().doCommand(new ColumnGroupExpandCommand(getGroupModel(level), group));
            }
        }
    }

    /**
     * Removes the given group.
     *
     * @param group
     *            The group to remove.
     */
    public void removeGroup(Group group) {
        removeGroup(0, group);
    }

    /**
     * Removes the given group.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param group
     *            The group to remove.
     */
    public void removeGroup(int level, Group group) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.removeGroup(group);
            if (group != null && group.isCollapsed()) {
                getPositionLayer().doCommand(new ColumnGroupExpandCommand(getGroupModel(level), group));
            }
        }
    }

    /**
     * Removes all groups in all levels.
     */
    public void clearAllGroups() {
        for (GroupModel groupModel : this.model) {
            if (!groupModel.isEmpty()) {
                getPositionLayer().doCommand(new ColumnGroupExpandCommand(groupModel, groupModel.getGroups()));
            }
            groupModel.clear();
        }
    }

    /**
     * Removes all groups in the given level.
     *
     * @param level
     *            The grouping level that should be cleared. The level is zero
     *            based bottom-up.
     */
    public void clearAllGroups(int level) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null && !groupModel.isEmpty()) {
            getPositionLayer().doCommand(new ColumnGroupExpandCommand(groupModel, groupModel.getGroups()));
            groupModel.clear();
        }
    }

    /**
     * Adds the given indexes as static indexes to the group that is identified
     * by the given group name. Static indexes are the indexes that stay visible
     * when the group is collapsed.
     *
     * @param groupName
     *            The name of a group to which the the static indexes should be
     *            added to.
     * @param staticIndexes
     *            The static indexes to add.
     */
    public void addStaticColumnIndexesToGroup(String groupName, int... staticIndexes) {
        addStaticColumnIndexesToGroup(0, groupName, staticIndexes);
    }

    /**
     * Adds the given indexes as static indexes to the group that is identified
     * by the given group name. Static indexes are the indexes that stay visible
     * when the group is collapsed.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param groupName
     *            The name of a group to which the the static indexes should be
     *            added to.
     * @param staticIndexes
     *            The static indexes to add.
     */
    public void addStaticColumnIndexesToGroup(int level, String groupName, int... staticIndexes) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.addStaticIndexesToGroup(groupName, staticIndexes);
        }
    }

    /**
     * Adds the given indexes as static indexes to the group that is identified
     * by the given column position. Static indexes are the indexes that stay
     * visible when the group is collapsed.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param columnPosition
     *            The position of a group to which the the static indexes should
     *            be added to.
     * @param staticIndexes
     *            The static indexes to add.
     */
    public void addStaticColumnIndexesToGroup(int level, int columnPosition, int... staticIndexes) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.addStaticIndexesToGroup(columnPosition, staticIndexes);
        }
    }

    /**
     * Collapses the group with the given name.
     *
     * @param groupName
     *            The name of the group that should be collapsed.
     */
    public void collapseGroup(String groupName) {
        collapseGroup(0, groupName);
    }

    /**
     * Collapses the group with the given name.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param groupName
     *            The name of the group that should be collapsed.
     */
    public void collapseGroup(int level, String groupName) {
        collapseGroup(getGroupModel(level), getGroupByName(groupName));
    }

    /**
     * Collapses the group for the given position, if the column at the
     * specified position belongs to a group.
     *
     * @param position
     *            The position corresponding to this layer whose corresponding
     *            group should be collapsed.
     */
    public void collapseGroup(int position) {
        collapseGroup(0, position);
    }

    /**
     * Collapses the group for the given position, if the column at the
     * specified position belongs to a group.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param position
     *            The position corresponding to this layer whose corresponding
     *            group should be collapsed.
     */
    public void collapseGroup(int level, int position) {
        collapseGroup(getGroupModel(level), getGroupByPosition(level, position));
    }

    /**
     * Collapses the given group of the given group model.
     *
     * @param groupModel
     *            The group model to which the given group belongs to.
     * @param group
     *            The group to collapse.
     */
    public void collapseGroup(GroupModel groupModel, Group group) {
        if (groupModel != null && group != null) {
            getPositionLayer().doCommand(new ColumnGroupCollapseCommand(groupModel, group));
        }
    }

    /**
     * Collapses all groups in all levels.
     */
    public void collapseAllGroups() {
        for (GroupModel groupModel : this.model) {
            if (!groupModel.isEmpty()) {
                getPositionLayer().doCommand(new ColumnGroupCollapseCommand(groupModel, groupModel.getGroups()));
            }
        }
    }

    /**
     * Collapses all groups in the given level.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     */
    public void collapseAllGroups(int level) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null && !groupModel.isEmpty()) {
            getPositionLayer().doCommand(new ColumnGroupCollapseCommand(groupModel, groupModel.getGroups()));
        }
    }

    /**
     * Expands the group with the given name.
     *
     * @param groupName
     *            The name of the group that should be expanded.
     */
    public void expandGroup(String groupName) {
        expandGroup(0, groupName);
    }

    /**
     * Expands the group with the given name.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param groupName
     *            The name of the group that should be expanded.
     */
    public void expandGroup(int level, String groupName) {
        expandGroup(getGroupModel(level), getGroupByName(groupName));
    }

    /**
     * Expands the group for the given position, if the column at the specified
     * position belongs to a group.
     *
     * @param position
     *            The position corresponding to this layer whose corresponding
     *            group should be expanded.
     */
    public void expandGroup(int position) {
        expandGroup(0, position);
    }

    /**
     * Expands the group for the given position, if the column at the specified
     * position belongs to a group.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param position
     *            The position corresponding to this layer whose corresponding
     *            group should be expanded.
     */
    public void expandGroup(int level, int position) {
        expandGroup(getGroupModel(level), getGroupByPosition(level, position));
    }

    /**
     * Expands the given group of the given group model.
     *
     * @param groupModel
     *            The group model to which the given group belongs to.
     * @param group
     *            The group to expand.
     */
    public void expandGroup(GroupModel groupModel, Group group) {
        if (groupModel != null && group != null) {
            getPositionLayer().doCommand(new ColumnGroupExpandCommand(groupModel, group));
        }
    }

    /**
     * Expands all groups in all levels.
     */
    public void expandAllGroups() {
        for (GroupModel groupModel : this.model) {
            if (!groupModel.isEmpty()) {
                getPositionLayer().doCommand(new ColumnGroupExpandCommand(groupModel, groupModel.getGroups()));
            }
        }
    }

    /**
     * Expands all groups in the given level.
     *
     * @param level
     *            The grouping level that should be expanded. The level is zero
     *            based bottom-up.
     */
    public void expandAllGroups(int level) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null && !groupModel.isEmpty()) {
            getPositionLayer().doCommand(new ColumnGroupExpandCommand(groupModel, groupModel.getGroups()));
        }
    }

    /**
     * Sets the default value for the collapseable flag when creating group
     * objects for all group levels.
     *
     * @param defaultCollapseable
     *            the default value for the collapseable flag that should be set
     *            on creating group.
     */
    public void setDefaultCollapseable(boolean defaultCollapseable) {
        for (GroupModel groupModel : this.model) {
            groupModel.setDefaultCollapseable(defaultCollapseable);
        }
    }

    /**
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @return The default value for the collapseable flag of newly created
     *         group objects.
     */
    public boolean isDefaultCollapseable(int level) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            return groupModel.isDefaultCollapseable();
        }
        return false;
    }

    /**
     * Sets the default value for the collapseable flag when creating group
     * objects.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param defaultCollapseable
     *            the default value for the collapseable flag that should be set
     *            on creating group.
     */
    public void setDefaultCollapseable(int level, boolean defaultCollapseable) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.setDefaultCollapseable(defaultCollapseable);
        }
    }

    /**
     * Set the group with the given group name to be collapseable or not.
     *
     * @param groupName
     *            The name of the group that should be modified.
     * @param collabseable
     *            <code>true</code> to set the group collapseable,
     *            <code>false</code> to set it not to be collapseable.
     */
    public void setGroupCollapseable(String groupName, boolean collabseable) {
        setGroupCollapseable(0, groupName, collabseable);
    }

    /**
     * Set the group with the given group name to be collapseable or not.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param groupName
     *            The name of the group that should be modified.
     * @param collabseable
     *            <code>true</code> to set the group collapseable,
     *            <code>false</code> to set it not to be collapseable.
     */
    public void setGroupCollapseable(int level, String groupName, boolean collabseable) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.setGroupCollapseable(groupName, collabseable);
        }
    }

    /**
     * Set the group to which the specified position belongs to, to be
     * collapseable or not.
     *
     * @param position
     *            The position used to retrieve the corresponding group.
     * @param collabseable
     *            <code>true</code> to set the group collapseable,
     *            <code>false</code> to set it not to be collapseable.
     */
    public void setGroupCollapseable(int position, boolean collabseable) {
        setGroupCollapseable(0, position, collabseable);
    }

    /**
     * Set the group to which the specified position belongs to, to be
     * collapseable or not.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param position
     *            The position used to retrieve the corresponding group.
     * @param collabseable
     *            <code>true</code> to set the group collapseable,
     *            <code>false</code> to set it not to be collapseable.
     */
    public void setGroupCollapseable(int level, int position, boolean collabseable) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.setGroupCollapseable(position, collabseable);
        }
    }

    /**
     * Sets the default value for the unbreakable flag when creating group
     * objects for all grouping levels.
     *
     * @param defaultUnbreakable
     *            the default value for the unbreakable flag that should be set
     *            on creating group.
     */
    public void setDefaultUnbreakable(boolean defaultUnbreakable) {
        for (GroupModel groupModel : this.model) {
            groupModel.setDefaultUnbreakable(defaultUnbreakable);
        }
    }

    /**
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @return The default value for the unbreakable flag of newly created group
     *         objects.
     */
    public boolean isDefaultUnbreakable(int level) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            return groupModel.isDefaultUnbreakable();
        }
        return false;
    }

    /**
     * Sets the default value for the unbreakable flag when creating group
     * objects.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param defaultUnbreakable
     *            the default value for the unbreakable flag that should be set
     *            on creating group.
     */
    public void setDefaultUnbreakable(int level, boolean defaultUnbreakable) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.setDefaultUnbreakable(defaultUnbreakable);
        }
    }

    /**
     * Set the group with the given name to unbreakable/breakable.
     *
     * @param groupName
     *            The name of the group that should be modified.
     * @param unbreakable
     *            <code>true</code> to set the group unbreakable,
     *            <code>false</code> to remove the unbreakable state.
     */
    public void setGroupUnbreakable(String groupName, boolean unbreakable) {
        setGroupUnbreakable(0, groupName, unbreakable);
    }

    /**
     * Set the group with the given name to unbreakable/breakable.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param groupName
     *            The name of the group that should be modified.
     * @param unbreakable
     *            <code>true</code> to set the group unbreakable,
     *            <code>false</code> to remove the unbreakable state.
     */
    public void setGroupUnbreakable(int level, String groupName, boolean unbreakable) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.setGroupUnbreakable(groupName, unbreakable);
        }
    }

    /**
     * Set the group to which the position belongs to unbreakable/breakable.
     *
     * @param position
     *            The position used to retrieve the corresponding group.
     * @param unbreakable
     *            <code>true</code> to set the group unbreakable,
     *            <code>false</code> to remove the unbreakable state.
     */
    public void setGroupUnbreakable(int position, boolean unbreakable) {
        setGroupUnbreakable(0, position, unbreakable);
    }

    /**
     * Set the group to which the position belongs to unbreakable/breakable.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param position
     *            The position used to retrieve the corresponding group.
     * @param unbreakable
     *            <code>true</code> to set the group unbreakable,
     *            <code>false</code> to remove the unbreakable state.
     */
    public void setGroupUnbreakable(int level, int position, boolean unbreakable) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.setGroupUnbreakable(position, unbreakable);
        }
    }

    // height

    /**
     *
     * @return <code>true</code> if the a check is performed whether column
     *         groups are configured or not. If not the height of the layer will
     *         not show additional height for showing column groups.
     *         <code>false</code> if the height should be fixed regardless of
     *         existing column group.
     */
    public boolean isCalculateHeight() {
        return this.calculateHeight;
    }

    /**
     * Configure whether the {@link ColumnGroupHeaderLayer} should calculate the
     * height of the layer dependent on column group configuration or not.
     *
     * @param calculateHeight
     *            <code>true</code> if the layer should check if column groups
     *            are configured and if not, the height of the column header
     *            will not show the double height for showing column groups.
     *            <code>false</code> if the height should be fixed regardless of
     *            existing column group.
     */
    public void setCalculateHeight(boolean calculateHeight) {
        boolean changed = calculateHeight != this.calculateHeight;
        this.calculateHeight = calculateHeight;
        if (changed) {
            this.fireLayerEvent(new RowStructuralRefreshEvent(this));
        }
    }

    // group cell/name rendering

    /**
     *
     * @return <code>true</code> if the group names are always visible on
     *         rendering, e.g. on scrolling, <code>false</code> if the group
     *         names stay at the fixed position in the cell and scroll with the
     *         cell. Default is <code>false</code>.
     */
    public boolean isShowAlwaysGroupNames() {
        return this.showAlwaysGroupNames;
    }

    /**
     * Configure whether group names should be always visible on rendering, e.g.
     * on scrolling, or if the group names should scroll with the cell. Setting
     * this value to <code>true</code> is recommended for huge column groups to
     * ensure that the group name is always visible. This also increases the
     * rendering performance as the spanned grouping cells are limited to the
     * visible area.
     *
     * @param showAlwaysGroupNames
     *            <code>true</code> if the group names should be always visible
     *            on rendering, e.g. on scrolling, <code>false</code> if the
     *            group names should stay at the fixed position in the cell and
     *            scroll with the cell. Default is <code>false</code>.
     */
    public void setShowAlwaysGroupNames(boolean showAlwaysGroupNames) {
        this.showAlwaysGroupNames = showAlwaysGroupNames;
    }

    // reorder

    /**
     * Used to support column reordering via drag and drop. Needed because on
     * drag the viewport could scroll and therefore on drag end the initial
     * position could not be determined anymore.
     *
     * @return The position from which a column reorder operation was started.
     *         Position is based on the configured {@link #positionLayer}.
     */
    public int getReorderFromColumnPosition() {
        return this.reorderFromColumnPosition;
    }

    /**
     * Set the position from which a column group drag operation was started.
     * <p>
     * Used to support column reordering via drag and drop. Needed because on
     * drag the viewport could scroll and therefore on drag end the initial
     * position could not be determined anymore.
     *
     * @param fromColumnPosition
     *            The position from which a column reorder operation was
     *            started. Position needs to be based on the configured
     *            {@link #positionLayer}.
     */
    public void setReorderFromColumnPosition(int fromColumnPosition) {
        this.reorderFromColumnPosition = fromColumnPosition;
    }

    /**
     * Check if reordering for the given grouping level is supported or not. By
     * default reordering is supported on all grouping levels.
     *
     * @param level
     *            The level to check.
     * @return <code>true</code> if the given grouping level does support
     *         reordering, <code>false</code> if group reordering is not
     *         supported for the given level.
     */
    public boolean isReorderSupportedOnLevel(int level) {
        Boolean supported = this.reorderSupportedOnLevel.get(level);
        if (supported != null) {
            return supported;
        }
        return true;
    }

    /**
     * Configure whether reordering for a grouping level should be supported or
     * not. By default reordering is enabled for all grouping levels.
     *
     * @param level
     *            The level for which the reorder support should be configured.
     * @param supported
     *            <code>true</code> if the given grouping level should support
     *            reordering, <code>false</code> if group reordering should not
     *            be supported for the given level.
     */
    public void setReorderSupportedOnLevel(int level, boolean supported) {
        if (level < this.model.size()) {
            this.reorderSupportedOnLevel.put(level, supported);
        }
    }

    /**
     * Reorder a column group for the fromColumnPosition at the given level to
     * the specified toColumnPosition.
     *
     * @param level
     *            The group level on which the group reorder should be
     *            performed.
     * @param fromColumnPosition
     *            The column position of a column in the column group that
     *            should be reordered. Based on the configured
     *            {@link #positionLayer}.
     * @param toColumnPosition
     *            The column position to which a column group should be
     *            reordered to. Based on the configured {@link #positionLayer}.
     * @return <code>true</code> if the reorder command was executed and
     *         consumed successfully
     */
    public boolean reorderColumnGroup(int level, int fromColumnPosition, int toColumnPosition) {
        if (!isReorderSupportedOnLevel(level)
                || !ColumnGroupUtils.isBetweenTwoGroups(
                        this,
                        level,
                        toColumnPosition,
                        toColumnPosition < getColumnCount(),
                        ColumnGroupUtils.getMoveDirection(fromColumnPosition, toColumnPosition))) {

            // consume the command and avoid reordering a group into another
            // group
            return true;
        }

        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            Group group = groupModel.getGroupByPosition(fromColumnPosition);
            if (group != null) {
                int toPosition = toColumnPosition;
                if (group.isCollapsed()) {
                    // if a collapsed column group is reordered, it needs to be
                    // expanded first to be able to get all column positions of
                    // that group that need to be reordered
                    int toIndex = getPositionLayer().getColumnIndexByPosition(toColumnPosition);
                    boolean toEnd = false;
                    if (toPosition >= getPositionLayer().getColumnCount()) {
                        toEnd = true;
                    }
                    expandGroup(groupModel, group);
                    if (toEnd) {
                        toPosition = getPositionLayer().getColumnCount();
                    } else {
                        toPosition = getPositionLayer().getColumnPositionByIndex(toIndex);
                    }
                }

                // we need to convert and fire the command on the underlying
                // layer of the positionLayer as otherwise the special command
                // handler is activated
                List<Integer> underlyingFrom = new ArrayList<Integer>();
                for (int from : group.getVisiblePositions()) {
                    underlyingFrom.add(getPositionLayer().localToUnderlyingColumnPosition(from));
                }
                int underlyingTo = getPositionLayer().localToUnderlyingColumnPosition(toPosition);
                return getPositionLayer().getUnderlyingLayerByPosition(0, 0).doCommand(
                        new MultiColumnReorderCommand(getPositionLayer(), underlyingFrom, underlyingTo));
            }
        }
        return false;
    }

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

    /**
     * {@link ILayerListener} implementation to update {@link Group}s according
     * to structural changes. Is registered on the positionLayer, therefore a
     * position conversion is not necessary.
     */
    private final class StructuralChangeLayerListener implements ILayerListener {

        @Override
        public void handleLayerEvent(ILayerEvent event) {
            // special handling of ColumnReorderEvent because that events
            // removes and adds a column at the same time and we need special
            // handling in terms of left edge to ungroup if possible
            if (event instanceof ColumnReorderEvent) {
                // handle reorder event and update model
                updateColumnGroupModel((ColumnReorderEvent) event);
            } else if (event instanceof IStructuralChangeEvent &&
                    ((IStructuralChangeEvent) event).isHorizontalStructureChanged()) {
                IStructuralChangeEvent changeEvent = (IStructuralChangeEvent) event;
                Collection<StructuralDiff> columnDiffs = changeEvent.getColumnDiffs();
                if (columnDiffs != null && !columnDiffs.isEmpty()) {

                    List<Integer> deletedPositions = getDeletedPositions(columnDiffs);
                    if (deletedPositions != null) {
                        // check if the number of positions are the same as the
                        // number of indexes, otherwise trigger a consistency
                        // check. reason is that the ranges are modified to be
                        // always in a valid range, and that could cause a loss
                        // of hidden positions on conversion
                        if (event instanceof ColumnStructuralChangeEvent
                                && ((ColumnStructuralChangeEvent) event).getColumnIndexes().size() > deletedPositions.size()) {
                            // this triggers a consistency check
                            handleDeleteDiffs(new ArrayList<Integer>());
                        } else {
                            handleDeleteDiffs(deletedPositions);
                        }
                    }

                    for (StructuralDiff diff : columnDiffs) {
                        if (diff.getDiffType() == DiffTypeEnum.ADD) {
                            // update visible start positions of all groups
                            updateVisibleStartPositions();

                            for (GroupModel groupModel : ColumnGroupHeaderLayer.this.model) {
                                Map<Group, UpdateColumnGroupCollapseCommand> collapseUpdates = new HashMap<Group, UpdateColumnGroupCollapseCommand>();

                                // find group and update visible span
                                for (int i = diff.getAfterPositionRange().start; i < diff.getAfterPositionRange().end; i++) {
                                    Group group = groupModel.getGroupByPosition(i);
                                    int newStartIndex = getPositionLayer().getColumnIndexByPosition(i);
                                    if (group != null && group.getVisibleStartPosition() <= i) {
                                        if (!group.isCollapsed()) {
                                            group.setVisibleSpan(group.getVisibleSpan() + 1);
                                        } else {
                                            // update collapsed state
                                            UpdateColumnGroupCollapseCommand cmd = collapseUpdates.get(group);
                                            if (cmd == null) {
                                                cmd = new UpdateColumnGroupCollapseCommand(groupModel, group);
                                                collapseUpdates.put(group, cmd);
                                            }
                                            if (!group.getStaticIndexes().contains(Integer.valueOf(newStartIndex))) {
                                                cmd.getIndexesToShow().add(newStartIndex);
                                                cmd.getIndexesToHide().add(getPositionLayer().getColumnIndexByPosition(i) + 1);
                                            }
                                            if (group.getVisibleSpan() == 0) {
                                                group.setVisibleSpan(1);
                                            }
                                        }
                                    } else {
                                        // if we have not found a group or the
                                        // found group starts at the new visible
                                        // position, check to the left to ensure
                                        // correct handling at the end of a
                                        // group
                                        Group leftGroup = groupModel.getGroupByPosition(i - 1);
                                        if (leftGroup != null
                                                && leftGroup.getVisibleSpan() < leftGroup.getOriginalSpan()
                                                && leftGroup.getMembers().contains(Integer.valueOf(newStartIndex))) {
                                            if (!leftGroup.isCollapsed()) {
                                                leftGroup.setVisibleSpan(leftGroup.getVisibleSpan() + 1);
                                            } else {
                                                if (!leftGroup.getStaticIndexes().contains(Integer.valueOf(newStartIndex))) {
                                                    // update collapsed state
                                                    UpdateColumnGroupCollapseCommand cmd = collapseUpdates.get(leftGroup);
                                                    if (cmd == null) {
                                                        cmd = new UpdateColumnGroupCollapseCommand(groupModel, leftGroup);
                                                        collapseUpdates.put(leftGroup, cmd);
                                                    }
                                                    cmd.getIndexesToHide().add(getPositionLayer().getColumnIndexByPosition(i));
                                                } else {
                                                    leftGroup.setVisibleSpan(leftGroup.getVisibleSpan() + 1);
                                                }
                                            }
                                        } else {
                                            // check if there is a group with
                                            // static indexes where the current
                                            // index would belong to
                                            Group g = groupModel.getGroupByStaticIndex(newStartIndex);
                                            if (g != null) {
                                                g.setVisibleStartIndex(newStartIndex);
                                                g.setVisibleSpan(g.getVisibleSpan() + 1);
                                                g.updateVisibleStartPosition();
                                            } else {
                                                // check if there is a group to
                                                // the right and if that group
                                                // has the newStartIndex as
                                                // member
                                                g = groupModel.getGroupByPosition(diff.getAfterPositionRange().end);
                                                if (g != null && g.getMembers().contains(newStartIndex)) {
                                                    g.setStartIndex(newStartIndex);
                                                    g.setVisibleStartIndex(newStartIndex);
                                                    g.setVisibleSpan(g.getVisibleSpan() + 1);
                                                    g.updateVisibleStartPosition();
                                                }
                                            }
                                        }
                                    }
                                }

                                for (UpdateColumnGroupCollapseCommand cmd : collapseUpdates.values()) {
                                    doCommand(cmd);
                                }
                            }
                        }
                    }

                    // update visible start positions of all groups
                    updateVisibleStartPositions();
                } else {
                    // trigger a consistency check as the details of the event
                    // probably where removed on converting the layer stack
                    // upwards (e.g. if the hidden position was at the end of
                    // the table)
                    performConsistencyCheck();
                }
            }
        }

        private List<Integer> getDeletedPositions(Collection<StructuralDiff> columnDiffs) {
            List<Integer> result = new ArrayList<Integer>();
            boolean deleteDiffFound = false;
            for (StructuralDiff diff : columnDiffs) {
                if (diff.getDiffType() == DiffTypeEnum.DELETE) {
                    deleteDiffFound = true;
                    int[] positions = PositionUtil.getPositions(diff.getBeforePositionRange());
                    result.addAll(ArrayUtil.asIntegerList(positions));
                }
            }
            if (deleteDiffFound) {
                // we need to handle the deleted positions backwards
                Collections.sort(result, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o2.compareTo(o1);
                    }
                });
                return result;
            } else {
                // there was no DELETE diff, so we return null
                return null;
            }
        }

        private void handleDeleteDiffs(List<Integer> positionList) {
            if (positionList.size() > 0) {
                for (GroupModel groupModel : ColumnGroupHeaderLayer.this.model) {
                    List<Integer> groupPositionList = new ArrayList<Integer>(positionList);
                    while (!groupPositionList.isEmpty()) {
                        // find group and update visible span
                        // we need to iterate because one could hide the
                        // last column in one group and the first of
                        // another group at once
                        Integer pos = groupPositionList.get(0);
                        Group group = groupModel.getGroupByPosition(pos);
                        if (group != null) {
                            // find all positions belonging to
                            // the group
                            List<Integer> groupPositions = new ArrayList<Integer>(group.getVisiblePositions());

                            // find the positions in the group
                            // that are hidden
                            List<Integer> hiddenGroupPositions = new ArrayList<Integer>();
                            for (int groupPos : groupPositions) {
                                if (groupPositionList.contains(groupPos)) {
                                    hiddenGroupPositions.add(groupPos);
                                }
                            }

                            // update the positionList as we
                            // handled the hidden columns
                            groupPositionList.removeAll(hiddenGroupPositions);

                            groupPositions.removeAll(hiddenGroupPositions);
                            if (groupPositions.size() > 0) {
                                group.setVisibleSpan(groupPositions.size());
                                for (int i : hiddenGroupPositions) {
                                    if (group.getVisibleStartPosition() == i) {
                                        group.setVisibleStartIndex(getPositionLayer().getColumnIndexByPosition(i - groupPositionList.size()));
                                    }
                                }
                            } else {
                                group.setVisibleStartIndex(-1);
                                group.setVisibleSpan(0);
                            }
                        } else {
                            groupPositionList.remove(pos);
                        }
                    }
                }
            } else {
                // trigger a consistency check as the details of
                // the event probably where removed on
                // converting the layer stack upwards (e.g. if
                // the hidden position was at the end of the
                // table)
                performConsistencyCheck();
            }
        }

        private void updateVisibleStartPositions() {
            for (GroupModel groupModel : ColumnGroupHeaderLayer.this.model) {
                groupModel.updateVisibleStartPositions();
            }
        }

        private void performConsistencyCheck() {
            for (GroupModel groupModel : ColumnGroupHeaderLayer.this.model) {
                groupModel.performConsistencyCheck();
            }
        }

        private void updateColumnGroupModel(ColumnReorderEvent reorderEvent) {

            int[] fromColumnPositions = PositionUtil.getPositions(reorderEvent.getBeforeFromColumnPositionRanges());
            int toColumnPosition = reorderEvent.getBeforeToColumnPosition();
            boolean reorderToLeftEdge = reorderEvent.isReorderToLeftEdge();

            int fromColumnPosition = toColumnPosition;

            if (toColumnPosition > fromColumnPositions[fromColumnPositions.length - 1]) {
                // Moving from left to right
                fromColumnPosition = fromColumnPositions[fromColumnPositions.length - 1];
            } else if (toColumnPosition < fromColumnPositions[fromColumnPositions.length - 1]) {
                // Moving from right to left
                fromColumnPosition = fromColumnPositions[0];
            }

            MoveDirectionEnum moveDirection = PositionUtil.getMoveDirection(fromColumnPosition, toColumnPosition);

            if (reorderToLeftEdge
                    && toColumnPosition > 0
                    && MoveDirectionEnum.RIGHT == moveDirection) {
                toColumnPosition--;
            }

            if (fromColumnPosition == -1 || toColumnPosition == -1) {
                LOG.error("Invalid reorder positions, fromPosition: " + fromColumnPosition //$NON-NLS-1$
                        + ", toPosition: " + toColumnPosition); //$NON-NLS-1$
                return;
            }

            for (GroupModel groupModel : ColumnGroupHeaderLayer.this.model) {
                Group toColumnGroup = groupModel.getGroupByPosition(toColumnPosition);

                Group fromColumnGroup = groupModel.getGroupByPosition(fromColumnPosition);

                if (fromColumnGroup != null
                        && toColumnGroup != null
                        && fromColumnGroup.equals(toColumnGroup)) {
                    // movement inside a column group

                    // if from and to position are the same and it is a
                    // column at the edge of a group, remove the
                    // position from the group
                    if (fromColumnPosition == toColumnPosition
                            && (!ColumnGroupUtils.isGroupReordered(fromColumnGroup, fromColumnPositions) || (fromColumnGroup.isCollapsed() && fromColumnGroup.getMembers().size() > 1))
                            && (fromColumnGroup.isLeftEdge(fromColumnPosition)
                                    || fromColumnGroup.isRightEdge(fromColumnPosition))) {
                        if (MoveDirectionEnum.RIGHT == moveDirection) {
                            int pos[] = new int[fromColumnPositions.length];
                            int index = 0;
                            for (int from = fromColumnPositions.length - 1; from >= 0; from--) {
                                pos[index] = fromColumnPositions[from];
                                index++;
                            }
                            removePositionsFromGroup(
                                    groupModel,
                                    fromColumnGroup,
                                    pos,
                                    reorderEvent.getBeforeFromColumnIndexes(),
                                    fromColumnPosition,
                                    moveDirection);
                        } else {
                            removePositionsFromGroup(
                                    groupModel,
                                    fromColumnGroup,
                                    fromColumnPositions,
                                    reorderEvent.getBeforeFromColumnIndexes(),
                                    fromColumnPosition,
                                    moveDirection);
                        }
                        break;
                    }

                    // if we moved the first visible start position or
                    // moved another column to the visible start
                    // position, we need to update the start index
                    if (fromColumnPosition != toColumnPosition
                            && (fromColumnGroup.getVisibleStartPosition() == fromColumnPositions[0]
                                    || toColumnGroup.getVisibleStartPosition() == toColumnPosition)) {
                        int newStartIndex = getPositionLayer().getColumnIndexByPosition(fromColumnGroup.getVisibleStartPosition());
                        fromColumnGroup.setVisibleStartIndex(newStartIndex);
                        if (!fromColumnGroup.isCollapsed()
                                || fromColumnGroup.getStaticIndexes().contains(Integer.valueOf(fromColumnGroup.getStartIndex()))) {
                            // if we move inside a collapsed group, there are
                            // static indexes, and therefore we do not update
                            // the start index, as the start index should be the
                            // same on expand
                            fromColumnGroup.setStartIndex(newStartIndex);
                        }
                    }
                } else if (fromColumnGroup == null
                        && toColumnGroup != null) {
                    addPositionsToGroup(
                            groupModel,
                            toColumnGroup,
                            fromColumnPositions,
                            reorderEvent.getBeforeFromColumnIndexes(),
                            toColumnPosition,
                            moveDirection);
                } else if (fromColumnGroup != null
                        && toColumnGroup == null
                        && (!ColumnGroupUtils.isGroupReordered(fromColumnGroup, fromColumnPositions) || fromColumnGroup.isCollapsed())) {
                    removePositionsFromGroup(
                            groupModel,
                            fromColumnGroup,
                            fromColumnPositions,
                            reorderEvent.getBeforeFromColumnIndexes(),
                            fromColumnPosition,
                            moveDirection);
                } else if (fromColumnGroup == null
                        && toColumnGroup == null
                        && fromColumnPosition == toColumnPosition
                        && fromColumnPositions.length == 1) {
                    // this might happen on drag and drop operations when
                    // trying to add a column back into an adjacent column
                    // group
                    int adjacentPos = (moveDirection == MoveDirectionEnum.RIGHT) ? fromColumnPosition + 1 : fromColumnPosition - 1;
                    // check if there is an adjacent column group
                    Group adjacentColumnGroup = groupModel.getGroupByPosition(adjacentPos);
                    if (adjacentColumnGroup != null && !adjacentColumnGroup.isUnbreakable()) {
                        addPositionsToGroup(
                                groupModel,
                                adjacentColumnGroup,
                                fromColumnPositions,
                                reorderEvent.getBeforeFromColumnIndexes(),
                                (adjacentColumnGroup.isCollapsed() && moveDirection != MoveDirectionEnum.RIGHT) ? toColumnPosition : adjacentPos,
                                moveDirection);
                    }
                } else if (fromColumnGroup != null
                        && toColumnGroup != null
                        && !fromColumnGroup.equals(toColumnGroup)
                        && (!ColumnGroupUtils.isGroupReordered(fromColumnGroup, fromColumnPositions) || fromColumnGroup.isCollapsed() || fromColumnGroup.getVisiblePositions().size() == 1)) {

                    removePositionsFromGroup(
                            groupModel,
                            fromColumnGroup,
                            fromColumnPositions,
                            reorderEvent.getBeforeFromColumnIndexes(),
                            fromColumnPositions[0],
                            moveDirection);
                    addPositionsToGroup(
                            groupModel,
                            toColumnGroup,
                            fromColumnPositions,
                            reorderEvent.getBeforeFromColumnIndexes(),
                            toColumnPosition,
                            moveDirection);
                }

                groupModel.updateVisibleStartPositions();
            }
        }

        private void addPositionsToGroup(
                GroupModel groupModel,
                Group group,
                int[] fromColumnPositions,
                Collection<Integer> fromColumnIndexes,
                int toPosition,
                MoveDirectionEnum moveDirection) {

            if (!group.isUnbreakable()) {
                // increase the span as column is moved inside group
                group.setOriginalSpan(group.getOriginalSpan() + fromColumnPositions.length);

                // update the start index
                if (group.isLeftEdge(toPosition)) {
                    int newStartIndex = (moveDirection == MoveDirectionEnum.RIGHT)
                            ? getPositionLayer().getColumnIndexByPosition(group.getVisibleStartPosition() - fromColumnPositions.length)
                            : getPositionLayer().getColumnIndexByPosition(group.getVisibleStartPosition());

                    if (group.getVisibleStartIndex() == group.getStartIndex()) {
                        group.setStartIndex(newStartIndex);
                    }

                    group.setVisibleStartIndex(newStartIndex);
                }

                // add the member indexes
                group.addMembers(fromColumnIndexes);

                if (group.isCollapsed()) {
                    // update collapsed state
                    UpdateColumnGroupCollapseCommand cmd = new UpdateColumnGroupCollapseCommand(groupModel, group);
                    if (group.isLeftEdge(toPosition)) {
                        cmd.getIndexesToHide().addAll(group.getMembers());

                        if (!group.getStaticIndexes().isEmpty()) {
                            group.setVisibleSpan(group.getVisibleSpan() + fromColumnPositions.length);
                        }

                        // the update command will trigger a hide event to hide
                        // the previous visible first column, therefore we need
                        // to update the visible start position before firing
                        // the command to ensure the hide handling is operating
                        // on the correct positions
                        group.updateVisibleStartPosition();
                    } else {
                        cmd.getIndexesToHide().addAll(fromColumnIndexes);
                        group.setVisibleSpan(group.getVisibleSpan() + fromColumnPositions.length);
                    }
                    getPositionLayer().doCommand(cmd);
                } else {
                    // we update the visible span only in case the group is not
                    // collapsed
                    group.setVisibleSpan(group.getVisibleSpan() + fromColumnPositions.length);
                }
            }
        }

        private void removePositionsFromGroup(
                GroupModel groupModel,
                Group group,
                int[] fromColumnPositions,
                Collection<Integer> fromColumnIndexes,
                int fromColumnPosition,
                MoveDirectionEnum moveDirection) {

            if (!group.isUnbreakable()) {
                boolean collapsed = group.isCollapsed();
                if (collapsed) {
                    int fromColumnIndex = getPositionLayer().getColumnIndexByPosition(fromColumnPosition);
                    // we need to expand to make the next column visible again
                    expandGroup(groupModel, group);
                    fromColumnPosition = getPositionLayer().getColumnPositionByIndex(fromColumnIndex);
                }

                // decrease the span as column is moved out of group
                group.setOriginalSpan(group.getOriginalSpan() - fromColumnPositions.length);
                group.setVisibleSpan(group.getVisibleSpan() - fromColumnPositions.length);

                // update the start index
                if (group.isLeftEdge(fromColumnPosition)) {
                    int newStartIndex = (moveDirection == MoveDirectionEnum.RIGHT)
                            ? getPositionLayer().getColumnIndexByPosition(group.getVisibleStartPosition())
                            : getPositionLayer().getColumnIndexByPosition(group.getVisibleStartPosition() + fromColumnPositions.length);
                    group.setStartIndex(newStartIndex);
                    group.setVisibleStartIndex(newStartIndex);
                }

                // remove the member indexes
                group.removeMembers(fromColumnIndexes);

                // remove static index if removed position was a static index
                group.removeStaticIndexes(ArrayUtil.asIntArray(fromColumnIndexes));

                if (group.getOriginalSpan() > 0) {
                    group.updateVisibleStartPosition();

                    if (collapsed) {
                        // collapse again
                        collapseGroup(groupModel, group);
                    }
                } else {
                    // all members where removed from the group, so we remove
                    // the group itself
                    groupModel.removeGroup(group);
                }
            }
        }
    }
}

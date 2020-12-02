/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.DimensionallyDependentIndexLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DimensionallyDependentLayer;
import org.eclipse.nebula.widgets.nattable.group.RowGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.command.RowGroupExpandCollapseCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.IndexPositionConverter;
import org.eclipse.nebula.widgets.nattable.group.performance.command.GroupMultiRowReorderCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.GroupMultiRowReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.GroupRowReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.GroupRowReorderEndCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.GroupRowReorderStartCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupCollapseCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupExpandCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupReorderEndCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupReorderStartCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupsCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.command.UpdateRowGroupCollapseCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.config.DefaultRowGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.group.performance.config.GroupHeaderConfigLabels;
import org.eclipse.nebula.widgets.nattable.group.performance.painter.RowGroupHeaderGridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
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
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.reorder.event.RowReorderEvent;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.swt.graphics.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds the row grouping functionality to the row header. Also persists the
 * state of the row groups when {@link NatTable#saveState(String, Properties)}
 * is invoked.
 * <p>
 * Internally uses a collection of {@link GroupModel} to track the row groups on
 * multiple levels.
 * </p>
 * <p>
 * It supports multiple row grouping levels. The levels are 0 based and
 * configured right-to-left. That means if 3 levels of row groups are defined,
 * the first level==0 is the right most columnPosition==2, and the left most
 * level==2 is on columnPosition==0.
 * </p>
 *
 * @since 1.6
 */
public class RowGroupHeaderLayer extends AbstractLayerTransform {

    private static final Logger LOG = LoggerFactory.getLogger(RowGroupHeaderLayer.class);

    private static final String PERSISTENCE_KEY_ROW_GROUPS = ".rowGroups"; //$NON-NLS-1$

    private final List<GroupModel> model;

    /**
     * {@link SizeConfig} instance for the column width configuration.
     */
    private final SizeConfig columnWidthConfig = new SizeConfig(20);

    /**
     * Flag which is used to tell the {@link RowGroupHeaderLayer} whether to
     * calculate the width of the layer dependent on row group configuration or
     * not. If it is set to <code>true</code> the row header will check if row
     * groups are configured and if not, the width of the row header will not
     * show the double width for showing row groups.
     */
    private boolean calculateWidth = false;

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
     * Needed to be able to convert the row position based on the
     * {@link #positionLayer} to a position in this layer.
     */
    private List<ILayer> layerPath;

    /**
     * Position that is tracked on row group reorder via dragging. Needed
     * because on drag operations the viewport could scroll and therefore the
     * from position is not the original one anymore.
     */
    private int reorderFromRowPosition;

    /**
     * Map in which it is stored if reordering is supported per level.
     */
    private Map<Integer, Boolean> reorderSupportedOnLevel = new HashMap<>();

    /**
     * The {@link CompositeFreezeLayer} in case it is part of the layer
     * composition. Needed to deal with groups in frozen state as row positions
     * could get ambiguous on scrolling.
     */
    private CompositeFreezeLayer compositeFreezeLayer;

    /**
     * Creates a {@link RowGroupHeaderLayer} with the specified configurations
     * and one grouping level. Uses the SelectionLayer as positionLayer and the
     * default configuration.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the RowHeaderLayer.
     * @param selectionLayer
     *            The SelectionLayer needed for command handlers that inspect
     *            the selection on handling.
     */
    public RowGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            SelectionLayer selectionLayer) {

        this(underlyingHeaderLayer, selectionLayer, selectionLayer, 1, true);
    }

    /**
     * Creates a {@link RowGroupHeaderLayer} with the specified configurations.
     * Uses the SelectionLayer as positionLayer and the default configuration.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the RowHeaderLayer.
     * @param selectionLayer
     *            The SelectionLayer needed for command handlers that inspect
     *            the selection on handling.
     * @param numberOfGroupLevels
     *            The number of group levels that should be supported.
     *            Additional levels can also be added via
     *            {@link #addGroupingLevel()}.
     */
    public RowGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            SelectionLayer selectionLayer,
            int numberOfGroupLevels) {

        this(underlyingHeaderLayer, selectionLayer, selectionLayer, numberOfGroupLevels, true);
    }

    /**
     * Creates a {@link RowGroupHeaderLayer} with the specified configurations
     * and one grouping level. Uses the default configuration.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the RowHeaderLayer.
     * @param positionLayer
     *            The positionLayer to which this layer should be mapped to,
     *            needed to handle column position transformations without
     *            taking the viewport into account. Typically the
     *            SelectionLayer.
     * @param selectionLayer
     *            The SelectionLayer needed for command handlers that inspect
     *            the selection on handling.
     */
    public RowGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            IUniqueIndexLayer positionLayer,
            SelectionLayer selectionLayer) {

        this(underlyingHeaderLayer, positionLayer, selectionLayer, 1, true);
    }

    /**
     * Creates a {@link RowGroupHeaderLayer} with the specified configurations.
     * Uses the default configuration.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the RowHeaderLayer.
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
    public RowGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            IUniqueIndexLayer positionLayer,
            SelectionLayer selectionLayer,
            int numberOfGroupLevels) {

        this(underlyingHeaderLayer, positionLayer, selectionLayer, numberOfGroupLevels, true);
    }

    /**
     * Creates a {@link RowGroupHeaderLayer} with the specified configurations.
     * Takes the {@link SelectionLayer} as positionLayer.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the RowHeaderLayer.
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
    public RowGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            SelectionLayer selectionLayer,
            int numberOfGroupLevels,
            boolean useDefaultConfiguration) {

        this(underlyingHeaderLayer, selectionLayer, selectionLayer, numberOfGroupLevels, useDefaultConfiguration);
    }

    /**
     * Creates a {@link RowGroupHeaderLayer} with one grouping level and the
     * specified configurations. Takes the {@link SelectionLayer} as
     * positionLayer.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the RowHeaderLayer.
     * @param selectionLayer
     *            The SelectionLayer needed for command handlers that inspect
     *            the selection on handling.
     * @param useDefaultConfiguration
     *            <code>true</code> if the default configuration should be
     *            applied, <code>false</code> if a custom configuration will be
     *            applied afterwards.
     */
    public RowGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            SelectionLayer selectionLayer,
            boolean useDefaultConfiguration) {

        this(underlyingHeaderLayer, selectionLayer, selectionLayer, 1, useDefaultConfiguration);
    }

    /**
     * Creates a {@link RowGroupHeaderLayer} with one grouping level and the
     * specified configurations.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the RowHeaderLayer.
     * @param positionLayer
     *            The positionLayer to which this layer should be mapped to,
     *            needed to handle row position transformations without taking
     *            the viewport into account. Typically the SelectionLayer.
     * @param selectionLayer
     *            The SelectionLayer needed for command handlers that inspect
     *            the selection on handling.
     * @param useDefaultConfiguration
     *            <code>true</code> if the default configuration should be
     *            applied, <code>false</code> if a custom configuration will be
     *            applied afterwards.
     */
    public RowGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            IUniqueIndexLayer positionLayer,
            SelectionLayer selectionLayer,
            boolean useDefaultConfiguration) {
        this(underlyingHeaderLayer, selectionLayer, selectionLayer, 1, useDefaultConfiguration);
    }

    /**
     * Creates a {@link RowGroupHeaderLayer} with the specified configurations.
     *
     * @param underlyingHeaderLayer
     *            The underlying layer on whose top this layer should be
     *            created, typically the RowHeaderLayer.
     * @param positionLayer
     *            The positionLayer to which this layer should be mapped to,
     *            needed to handle row position transformations without taking
     *            the viewport into account. Typically the SelectionLayer.
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
    public RowGroupHeaderLayer(
            ILayer underlyingHeaderLayer,
            IUniqueIndexLayer positionLayer,
            SelectionLayer selectionLayer,
            int numberOfGroupLevels,
            boolean useDefaultConfiguration) {

        super(underlyingHeaderLayer);

        this.positionLayer = positionLayer;
        this.indexPositionConverter = new GroupModel.IndexPositionConverter() {

            @Override
            public int convertPositionToIndex(int position) {
                return positionLayer.getRowIndexByPosition(position);
            }

            @Override
            public int convertIndexToPosition(int index) {
                return positionLayer.getRowPositionByIndex(index);
            }
        };

        this.model = new ArrayList<>(numberOfGroupLevels);
        for (int i = 0; i < numberOfGroupLevels; i++) {
            GroupModel groupModel = new GroupModel();
            groupModel.setIndexPositionConverter(this.indexPositionConverter);
            this.model.add(groupModel);
            this.reorderSupportedOnLevel.put(i, Boolean.TRUE);
        }

        this.layerPath = findLayerPath(this, 0);
        this.layerPainter = new RowGroupHeaderGridLineCellLayerPainter(this);

        // add listener on dependent layer to be notified about structural
        // changes
        positionLayer.addLayerListener(new StructuralChangeLayerListener());

        registerCommandHandlers(selectionLayer);

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultRowGroupHeaderLayerConfiguration(false));
        }
    }

    /**
     * @return The {@link ILayerPainter} that is used by this layer. Typically
     *         the {@link RowGroupHeaderGridLineCellLayerPainter} to support
     *         rendering of huge row group cells by inspecting the
     *         {@link #showAlwaysGroupNames} attribute.
     */
    @Override
    public ILayerPainter getLayerPainter() {
        // return the ILayerPainter set to this layer, not the ILayerPainter
        // from the underlying layer as specified in AbstractLayerTransform
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
        registerCommandHandler(new RowGroupsCommandHandler(this, selectionLayer));
        registerCommandHandler(new ConfigureScalingCommandHandler(this.columnWidthConfig, null));

        // group reordering
        registerCommandHandler(new RowGroupReorderCommandHandler(this));
        registerCommandHandler(new RowGroupReorderStartCommandHandler(this));
        registerCommandHandler(new RowGroupReorderEndCommandHandler(this));

        // register command handlers to add checks if a reordering is valid in
        // case of unbreakable groups
        getPositionLayer().registerCommandHandler(new GroupRowReorderCommandHandler(this));
        getPositionLayer().registerCommandHandler(new GroupRowReorderStartCommandHandler(this));
        getPositionLayer().registerCommandHandler(new GroupRowReorderEndCommandHandler(this));
        getPositionLayer().registerCommandHandler(new GroupMultiRowReorderCommandHandler(this));
    }

    /**
     * Convenience method to get the {@link GroupModel} on level 0. Useful for
     * single level row grouping.
     *
     * @return The {@link GroupModel} for level 0.
     */
    public GroupModel getGroupModel() {
        return getGroupModel(0);
    }

    /**
     * Return the {@link GroupModel} for the given grouping level. Note that the
     * levels are right-to-left, so level 0 is the right most grouping level.
     *
     * @param level
     *            The grouping level. Value is right-to-left.
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
        groupModel.setIndexPositionConverter(this.indexPositionConverter);
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
     * @param rowPosition
     *            The row position for which the layer path should be
     * @return The path of {@link ILayer} from the {@link #positionLayer} to the
     *         given {@link ILayer} or <code>null</code> if a direct path is not
     *         available.
     */
    List<ILayer> findLayerPath(ILayer layer, int rowPosition) {

        if (layer == getPositionLayer()) {
            List<ILayer> result = new ArrayList<>();
            result.add(layer);
            return result;
        }

        if (this.compositeFreezeLayer == null && layer instanceof CompositeFreezeLayer) {
            this.compositeFreezeLayer = (CompositeFreezeLayer) layer;
        }

        // handle collection
        List<ILayer> result = null;
        Collection<ILayer> underlyingLayers = layer.getUnderlyingLayersByRowPosition(rowPosition);
        if (underlyingLayers != null) {
            for (ILayer underlyingLayer : underlyingLayers) {
                if (underlyingLayer != null) {
                    result = findLayerPath(underlyingLayer, rowPosition);
                }
            }
        }

        // handle vertical dependency
        if (result == null && layer instanceof DimensionallyDependentLayer) {
            result = findLayerPath(((DimensionallyDependentLayer) layer).getVerticalLayerDependency(), rowPosition);
        }
        if (result == null && this.underlyingLayer instanceof DimensionallyDependentIndexLayer) {
            result = findLayerPath(((DimensionallyDependentIndexLayer) layer).getVerticalLayerDependency(), rowPosition);
        }

        // in case of the CompositeFreezeLayer it can happen that for the last
        // rows in scrolled state the path cannot be determined as
        // getUnderlyingLayersByPosition() returns an empty collection because
        // it is above the ViewportLayer. We therefore need a special handling
        // to check additionally below the ViewportLayer.
        if (result == null && layer instanceof CompositeFreezeLayer) {
            result = findLayerPath(((CompositeFreezeLayer) layer).getChildLayerByLayoutCoordinate(1, 1), rowPosition);
        }

        if (result != null) {
            result.add(layer);
        }

        return result;
    }

    /**
     * Converts the given row position the {@link #layerPath} upwards.
     *
     * @param rowPosition
     *            The row position to convert.
     * @return The upwards converted row position.
     */
    protected int convertRowPositionUpwards(int rowPosition) {
        int converted = rowPosition;

        // This could be for example when the CompositeFreezeLayer is in the
        // composition. At creation time the underlying layers would be empty
        // because the height is not yet calculated.
        List<ILayer> path = this.layerPath;
        if (path == null) {
            path = findLayerPath(this, rowPosition);
        }

        if (path != null) {
            for (int i = 0; i < path.size() - 1; i++) {
                ILayer underlying = path.get(i);
                ILayer upper = path.get(i + 1);
                converted = upper.underlyingToLocalRowPosition(underlying, converted);
            }
        }
        return converted;
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof RowGroupExpandCollapseCommand
                && command.convertToTargetLayer(getPositionLayer())) {
            // only RowGroupExpandCollapseCommand needs to be converted to
            // positionLayer so also currently not visible row groups can be
            // expanded/collapsed
            RowGroupExpandCollapseCommand cmd = (RowGroupExpandCollapseCommand) command;
            int columnPosition = cmd.getLocalColumnPosition(this);
            int rowPosition = cmd.getRowPosition();

            Object[] found = findGroupForCoordinates(columnPosition, rowPosition);
            if (found != null) {
                GroupModel groupModel = (GroupModel) found[0];
                Group group = (Group) found[1];
                if (group.isCollapsed()) {
                    expandGroup(groupModel, group);
                } else {
                    collapseGroup(groupModel, group);
                }
            }
            return true;
        } else if (command instanceof ColumnResizeCommand
                && command.convertToTargetLayer(this)
                && ((ColumnResizeCommand) command).getColumnPosition() < getColumnCount() - 1) {
            ColumnResizeCommand columnResizeCommand = (ColumnResizeCommand) command;
            int newColumnWidth = columnResizeCommand.downScaleValue()
                    ? this.columnWidthConfig.downScale(columnResizeCommand.getNewColumnWidth())
                    : columnResizeCommand.getNewColumnWidth();

            setColumnWidth(columnResizeCommand.getColumnPosition(), newColumnWidth);
            fireLayerEvent(new ColumnResizeEvent(this, columnResizeCommand.getColumnPosition()));
            return true;
        } else if (command instanceof MultiColumnResizeCommand && command.convertToTargetLayer(this)) {
            MultiColumnResizeCommand columnResizeCommand = (MultiColumnResizeCommand) command;
            for (int column : columnResizeCommand.getColumnPositionsArray()) {
                int newColumnWidth = columnResizeCommand.downScaleValue()
                        ? this.columnWidthConfig.downScale(columnResizeCommand.getColumnWidth(column))
                        : columnResizeCommand.getColumnWidth(column);

                setColumnWidth(column, newColumnWidth);
                fireLayerEvent(new ColumnResizeEvent(this, column));
                // do not consume as additional columns might need to get
                // updated too
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
            groupModel.saveState(prefix + PERSISTENCE_KEY_ROW_GROUPS + "_" + level, properties); //$NON-NLS-1$
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
            groupModel.loadState(prefix + PERSISTENCE_KEY_ROW_GROUPS + "_" + level, properties); //$NON-NLS-1$
            // trigger real collapse of collapsed groups in model
            List<Group> collapsedGroups = new ArrayList<>();
            for (Group group : groupModel.getGroups()) {
                if (group.isCollapsed()) {
                    collapsedGroups.add(group);
                }
            }
            if (!collapsedGroups.isEmpty()) {
                doCommand(new RowGroupCollapseCommand(groupModel, collapsedGroups));
            }
            level++;
        }

        fireLayerEvent(new RowStructuralRefreshEvent(this));
    }

    // Horizontal features

    // Columns

    @Override
    public int getColumnCount() {
        return this.underlyingLayer.getColumnCount() + this.model.size();
    }

    @Override
    public int getPreferredColumnCount() {
        return this.underlyingLayer.getPreferredColumnCount() + this.model.size();
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        int columnCount = this.model.size();
        if (columnPosition < columnCount) {
            return columnPosition;
        } else {
            return this.underlyingLayer.getColumnIndexByPosition(columnPosition - columnCount);
        }
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        int columnCount = this.model.size();
        if (localColumnPosition < columnCount) {
            return localColumnPosition;
        } else {
            return localColumnPosition - columnCount;
        }
    }

    // Width

    private int getGroupingWidth() {
        if (!this.calculateWidth) {
            return this.columnWidthConfig.getAggregateSize(this.model.size());
        }

        int width = 0;
        for (int i = 0; i < this.model.size(); i++) {
            GroupModel groupModel = this.model.get(i);
            if (!groupModel.isEmpty()) {
                width += this.columnWidthConfig.getSize(getColumnPositionForLevel(i));
            }
        }
        return width;
    }

    @Override
    public int getWidth() {
        return getGroupingWidth() + this.underlyingLayer.getWidth();
    }

    @Override
    public int getPreferredWidth() {
        return getGroupingWidth() + this.underlyingLayer.getPreferredWidth();
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        int columnCount = this.model.size();
        if (columnPosition < columnCount) {
            if (!this.calculateWidth) {
                return this.columnWidthConfig.getSize(columnPosition);
            } else {
                int level = getLevelForColumnPosition(columnPosition);
                return getGroupModel(level).isEmpty() ? 0 : this.columnWidthConfig.getSize(columnPosition);
            }
        } else {
            return this.underlyingLayer.getColumnWidthByPosition(columnPosition - columnCount);
        }
    }

    /**
     * Set the column width for grouping level 0.
     *
     * @param columnWidth
     *            The width to set for grouping level 0.
     */
    public void setColumnWidth(int columnWidth) {
        setColumnWidth(getColumnPositionForLevel(0), columnWidth);
    }

    /**
     * Set the column width for the given column in this layer.
     * <p>
     * <b>Note: </b> Use {@link #getLevelForColumnPosition(int)} if the column
     * position for a level needs to be determined.
     * </p>
     *
     * @param column
     *            The column whose width should be set.
     * @param columnWidth
     *            The width to set for the given column position.
     */
    public void setColumnWidth(int column, int columnWidth) {
        this.columnWidthConfig.setSize(column, columnWidth);
    }

    /**
     *
     * @param level
     *            The level for which the column position is requested.
     * @return The column position for the given grouping level.
     */
    public int getColumnPositionForLevel(int level) {
        return this.model.size() - level - 1;
    }

    /**
     *
     * @param columnPosition
     *            The column position for which the level is requested.
     * @return The level for the given column position.
     */
    public int getLevelForColumnPosition(int columnPosition) {
        return this.model.size() - columnPosition - 1;
    }

    // Column resize

    @Override
    public boolean isColumnPositionResizable(int columnPosition) {
        int columnCount = this.model.size();
        if (columnPosition < columnCount) {
            return this.columnWidthConfig.isPositionResizable(columnPosition);
        } else {
            return this.underlyingLayer.isColumnPositionResizable(columnPosition - columnCount);
        }
    }

    /**
     * Set the column resizable configuration for the given column position.
     *
     * @param columnPosition
     *            The column for which the resizable flag should be set.
     * @param resizable
     *            <code>true</code> if the column should be resizable,
     *            <code>false</code> if not.
     */
    public void setColumnPositionResizable(int columnPosition, boolean resizable) {
        this.columnWidthConfig.setPositionResizable(columnPosition, resizable);
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        int groupWidth = getGroupingWidth();
        if (x <= groupWidth) {
            return LayerUtil.getColumnPositionByX(this, x);
        } else {
            return this.model.size() + this.underlyingLayer.getColumnPositionByX(x - groupWidth);
        }
    }

    @Override
    public int getStartXOfColumnPosition(int columnPosition) {
        int columnCount = this.model.size();
        if (columnPosition < columnCount) {
            if (!this.calculateWidth) {
                return this.columnWidthConfig.getAggregateSize(columnPosition);
            } else {
                int startX = 0;
                for (int i = 0; i < columnPosition; i++) {
                    GroupModel groupModel = this.model.get(i);
                    if (!groupModel.isEmpty()) {
                        startX += this.columnWidthConfig.getSize(getColumnPositionForLevel(i));
                    }
                }
                return startX;
            }
        } else {
            return getGroupingWidth()
                    + this.underlyingLayer.getStartXOfColumnPosition(columnPosition - this.model.size());
        }
    }

    // Cell features

    @Override
    public ILayerCell getCellByPosition(final int columnPosition, final int rowPosition) {
        // Row group header cell
        if (columnPosition < this.model.size()) {
            int level = getLevelForColumnPosition(columnPosition);
            Group group = getGroupByPosition(level, rowPosition);
            if (group != null) {
                int start = convertRowPositionUpwards(getPositionLayer().getRowPositionByIndex(group.getVisibleStartIndex()));

                // check if there is a level above that does not have a group
                int column = columnPosition;
                int columnSpan = 1;
                while (level < (this.model.size() - 1)) {
                    level++;
                    Group upperGroup = getGroupByPosition(level, rowPosition);
                    if (upperGroup == null) {
                        column--;
                        columnSpan++;
                    } else {
                        break;
                    }
                }

                // if the header should be shown always, e.g. because of
                // huge row groups, the start will not below 0 and the
                // end not below row count
                int rowSpan = getRowSpan(group);
                if (this.showAlwaysGroupNames) {
                    if (start < 0) {
                        rowSpan += start;
                        start = 0;
                    }

                    if (start + rowSpan > getRowCount()) {
                        rowSpan = getRowCount() - start;
                    }
                }

                return new LayerCell(
                        this,
                        column,
                        start,
                        columnPosition,
                        rowPosition,
                        columnSpan,
                        rowSpan);
            } else {
                // for the level there is no group, check if the level below has
                // a group to calculate the row spanning
                int columnSpan = 2;
                Group subGroup = null;
                while (level > 0) {
                    level--;
                    group = getGroupByPosition(level, rowPosition);
                    if (group == null) {
                        columnSpan++;
                    } else {
                        subGroup = group;
                    }
                }

                if (subGroup != null) {
                    int start = convertRowPositionUpwards(getPositionLayer().getRowPositionByIndex(subGroup.getVisibleStartIndex()));
                    int rowSpan = getRowSpan(subGroup);

                    // if the header should be shown always, e.g. because of
                    // huge row groups, the start will not below 0 and the
                    // end not below row count
                    if (this.showAlwaysGroupNames) {
                        if (start < 0) {
                            rowSpan += start;
                            start = 0;
                        }

                        if (start + rowSpan > getRowCount()) {
                            rowSpan = getRowCount() - start;
                        }
                    }

                    return new LayerCell(
                            this,
                            columnPosition,
                            start,
                            columnPosition,
                            rowPosition,
                            columnSpan,
                            rowSpan);
                } else {
                    // get the cell from the underlying layer
                    final int span = columnSpan;
                    ILayerCell cell = this.underlyingLayer.getCellByPosition(0, rowPosition);
                    if (cell != null) {
                        cell = new TransformedLayerCell(cell) {
                            @Override
                            public ILayer getLayer() {
                                return RowGroupHeaderLayer.this;
                            }

                            @Override
                            public int getColumnSpan() {
                                return span;
                            }

                            @Override
                            public int getColumnPosition() {
                                return columnPosition;
                            }

                            @Override
                            public int getOriginColumnPosition() {
                                return columnPosition;
                            }
                        };
                    }
                    return cell;
                }
            }
        } else {

            int columnSpan = 1;
            // check for special case if a row header data provider supports
            // multiple columns
            if (columnPosition - 1 < this.model.size()) {
                // check if one column to the left has a group
                int level = getLevelForColumnPosition(columnPosition - 1);

                Group group = null;
                while (level < this.model.size()) {
                    group = getGroupByPosition(level, rowPosition);
                    if (group == null) {
                        columnSpan++;
                    } else {
                        break;
                    }
                    level++;
                }
            }

            final int span = columnSpan;
            ILayerCell cell = this.underlyingLayer.getCellByPosition(0, rowPosition);
            if (cell != null) {
                cell = new TransformedLayerCell(cell) {
                    @Override
                    public ILayer getLayer() {
                        return RowGroupHeaderLayer.this;
                    }

                    @Override
                    public int getColumnSpan() {
                        return span;
                    }

                    @Override
                    public int getColumnPosition() {
                        return columnPosition;
                    }

                    @Override
                    public int getOriginColumnPosition() {
                        return columnPosition - (span - 1);
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
            // because the origin row positions of a spanned cell could be
            // ambiguous on scrolling
            ILayerCell cell = getCellByPosition(columnPosition, rowPosition);
            int[] rowBounds = this.compositeFreezeLayer.getRowBounds(
                    rowPosition,
                    cell.getOriginRowPosition(),
                    cell.getOriginRowPosition() + cell.getRowSpan() - 1);
            bounds.y = rowBounds[0];
            bounds.height = rowBounds[1];
        }

        return bounds;
    }

    /**
     * Get the {@link Group} for the row at the given row position for level 0.
     * Will transform the given row position to a position matching the position
     * layer for correct resolution.
     *
     * @param rowPosition
     *            The row position related to this layer.
     * @return The {@link Group} at the given row position or <code>null</code>
     *         if there is no {@link Group} at this position.
     */
    public Group getGroupByPosition(int rowPosition) {
        return getGroupByPosition(0, rowPosition);
    }

    /**
     * Get the {@link Group} for the row at the given row position for the given
     * grouping level. Will transform the given row position to a position
     * matching the position layer for correct resolution.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param rowPosition
     *            The row position related to this layer.
     * @return The {@link Group} at the given row position or <code>null</code>
     *         if there is no {@link Group} at this position.
     */
    public Group getGroupByPosition(int level, int rowPosition) {
        // calculate the position matching the position layer
        int posRow = LayerUtil.convertRowPosition(this, rowPosition, getPositionLayer());
        if (posRow > -1) {
            GroupModel groupModel = getGroupModel(level);
            if (groupModel != null) {
                return groupModel.getGroupByPosition(posRow);
            }
        }
        return null;
    }

    /**
     * Finds a {@link Group} and its parent {@link GroupModel} based on the
     * coordinates.
     *
     * @param columnPosition
     *            The column position based on this layer.
     * @param rowPosition
     *            The row position based on the position layer.
     * @return Object array where the first item is the {@link GroupModel} and
     *         the second item is the found {@link Group}. Returns
     *         <code>null</code> if either no {@link GroupModel} or no
     *         {@link Group} was found.
     */
    protected Object[] findGroupForCoordinates(int columnPosition, int rowPosition) {
        int level = getLevelForColumnPosition(columnPosition);
        GroupModel groupModel = null;
        Group group = null;

        for (; level >= 0; level--) {
            groupModel = getGroupModel(level);
            if (groupModel != null) {
                group = groupModel.getGroupByPosition(rowPosition);
                if (group != null) {
                    return new Object[] { groupModel, group };
                }
            }
        }

        return null;
    }

    /**
     * Checks if there is a {@link Group} configured for the given row position
     * at any level.
     *
     * @param rowPosition
     *            The row position related to this layer.
     * @return <code>true</code> if there is a {@link Group} at the given row
     *         position, <code>false</code> if not.
     */
    public boolean isPartOfAGroup(int rowPosition) {
        for (int level = 0; level < this.model.size(); level++) {
            if (isPartOfAGroup(level, rowPosition)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is a {@link Group} configured for the given row position
     * at the given level.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based right-to-left.
     * @param rowPosition
     *            The row position related to this layer.
     * @return <code>true</code> if there is a {@link Group} at the given row
     *         position, <code>false</code> if not.
     */
    public boolean isPartOfAGroup(int level, int rowPosition) {
        Group group = getGroupByPosition(level, rowPosition);
        return group != null;
    }

    /**
     * Check if the specified position belongs to a {@link Group} and if this
     * {@link Group} is unbreakable. Convenience method for checks on level 0.
     *
     * @param rowPosition
     *            The position used to retrieve the corresponding group related
     *            to this layer.
     * @return <code>true</code> if the specified position belongs to a
     *         {@link Group} and this {@link Group} is unbreakable,
     *         <code>false</code> if not.
     */
    public boolean isPartOfAnUnbreakableGroup(int rowPosition) {
        return isPartOfAnUnbreakableGroup(0, rowPosition);
    }

    /**
     * Check if the specified position belongs to a {@link Group} at the
     * specified level and if this {@link Group} is unbreakable.
     *
     * @param level
     *            The level for which the check should be performed.
     * @param rowPosition
     *            The position used to retrieve the corresponding group related
     *            to this layer.
     * @return <code>true</code> if the specified position belongs to a
     *         {@link Group} at the specified level and this {@link Group} is
     *         unbreakable, <code>false</code> if not.
     */
    public boolean isPartOfAnUnbreakableGroup(int level, int rowPosition) {
        Group group = getGroupByPosition(level, rowPosition);
        if (group != null) {
            return group.isUnbreakable();
        }
        return false;
    }

    /**
     * Calculates the span of a cell in a group. Takes into account collapsed
     * and hidden rows in the group.
     *
     * @param group
     *            the group for which the span should be calculated.
     */
    public int getRowSpan(Group group) {
        int sizeOfGroup = group.getVisibleSpan();

        if (group.isCollapsed()) {
            int sizeOfStaticRows = group.getStaticIndexes().length;
            if (sizeOfStaticRows == 0) {
                return 1;
            } else {
                int staticSize = 0;
                for (int index : group.getStaticIndexes()) {
                    if (getPositionLayer().getRowPositionByIndex(index) >= 0) {
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
        if (columnPosition < this.model.size() && isPartOfAGroup(getLevelForColumnPosition(columnPosition), rowPosition)) {
            return DisplayMode.NORMAL;
        } else {
            return this.underlyingLayer.getDisplayModeByPosition(columnPosition, rowPosition);
        }
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        int posRow = LayerUtil.convertRowPosition(this, rowPosition, getPositionLayer());
        Object[] found = findGroupForCoordinates(columnPosition, posRow);
        Group group = found != null ? (Group) found[1] : null;

        if (columnPosition < this.model.size() && group != null) {
            LabelStack stack = new LabelStack();
            if (getConfigLabelAccumulator() != null) {
                getConfigLabelAccumulator().accumulateConfigLabels(stack, columnPosition, rowPosition);
            }
            stack.addLabel(GridRegion.ROW_GROUP_HEADER);

            if (group != null && group.isCollapseable()) {
                if (group.isCollapsed()) {
                    stack.addLabelOnTop(GroupHeaderConfigLabels.GROUP_COLLAPSED_CONFIG_TYPE);
                } else {
                    stack.addLabelOnTop(GroupHeaderConfigLabels.GROUP_EXPANDED_CONFIG_TYPE);
                }
            }

            return stack;
        } else {
            return this.underlyingLayer.getConfigLabelsByPosition(columnPosition, rowPosition);
        }
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        if (columnPosition < this.model.size()) {
            int level = getLevelForColumnPosition(columnPosition);
            Group group = getGroupByPosition(level, rowPosition);
            while (group == null && level > 0) {
                level--;
                group = getGroupByPosition(level, rowPosition);
            }

            if (group != null) {
                return group.getName();
            }
        }

        return this.underlyingLayer.getDataValueByPosition(0, rowPosition);
    }

    @Override
    public LabelStack getRegionLabelsByXY(int x, int y) {
        if (x < getGroupingWidth()) {
            for (int i = 0; i < this.model.size(); i++) {
                if (isPartOfAGroup(i, getRowPositionByY(y))) {
                    return new LabelStack(GridRegion.ROW_GROUP_HEADER);
                }
            }
        }

        return this.underlyingLayer.getRegionLabelsByXY(x, y - getGroupingWidth());
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
     * Adds the given positions to the group to which the given row position
     * belongs to.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based bottom-up.
     * @param rowPosition
     *            The row position related to this layer to get the
     *            corresponding group to which the given positions should be
     *            added.
     * @param positions
     *            The positions to add corresponding to this layer.
     */
    public void addPositionsToGroup(int level, int rowPosition, int... positions) {
        Group group = getGroupByPosition(level, rowPosition);
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
            converted[pos] = LayerUtil.convertRowPosition(this, positions[pos], getPositionLayer());
        }

        if (group.isCollapsed()) {
            getPositionLayer().doCommand(new RowGroupExpandCommand(getGroupModel(level), group));
        }

        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.addPositionsToGroup(group, converted);
        }

        fireLayerEvent(new ColumnStructuralRefreshEvent(RowGroupHeaderLayer.this.underlyingLayer));
    }

    /**
     * Removes the given positions from corresponding groups. Only performs an
     * action if the position is part of the group.
     * <p>
     * <b>Note:</b><br>
     * A removal will only happen for rows at the beginning or the end of a
     * group. Removing a position in the middle will cause removal of rows at
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
            converted[pos] = LayerUtil.convertRowPosition(this, positions[pos], getPositionLayer());
        }

        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            for (int i = converted.length - 1; i >= 0; i--) {
                int pos = converted[i];
                Group group = groupModel.getGroupByPosition(pos);
                if (group.isCollapsed()) {
                    getPositionLayer().doCommand(new RowGroupExpandCommand(groupModel, group));
                }
                groupModel.removePositionsFromGroup(group, pos);
            }
        }

        // fire the event to update row group rendering in case calculate
        // width is enabled
        if (this.calculateWidth) {
            fireLayerEvent(new ColumnStructuralRefreshEvent(RowGroupHeaderLayer.this.underlyingLayer));
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
                getPositionLayer().doCommand(new RowGroupExpandCommand(getGroupModel(level), group));
            }
        }
    }

    /**
     * Removes the group identified by the given row position.
     *
     * @param rowPosition
     *            The group that contains the given row position.
     */
    public void removeGroup(int rowPosition) {
        removeGroup(0, rowPosition);
    }

    /**
     * Removes the group identified by the given row position.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based right-to-left.
     * @param rowPosition
     *            The group that contains the given row position.
     */
    public void removeGroup(int level, int rowPosition) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            Group group = groupModel.removeGroup(rowPosition);
            if (group != null && group.isCollapsed()) {
                getPositionLayer().doCommand(new RowGroupExpandCommand(getGroupModel(level), group));
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
     *            is zero based right-to-left.
     * @param group
     *            The group to remove.
     */
    public void removeGroup(int level, Group group) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.removeGroup(group);
            if (group != null && group.isCollapsed()) {
                getPositionLayer().doCommand(new RowGroupExpandCommand(getGroupModel(level), group));
            }
        }
    }

    /**
     * Removes all groups in all levels.
     */
    public void clearAllGroups() {
        for (GroupModel groupModel : this.model) {
            if (!groupModel.isEmpty()) {
                getPositionLayer().doCommand(new RowGroupExpandCommand(groupModel, groupModel.getGroups()));
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
            getPositionLayer().doCommand(new RowGroupExpandCommand(groupModel, groupModel.getGroups()));
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
    public void addStaticRowIndexesToGroup(String groupName, int... staticIndexes) {
        addStaticRowIndexesToGroup(0, groupName, staticIndexes);
    }

    /**
     * Adds the given indexes as static indexes to the group that is identified
     * by the given group name. Static indexes are the indexes that stay visible
     * when the group is collapsed.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based right-to-left.
     * @param groupName
     *            The name of a group to which the the static indexes should be
     *            added to.
     * @param staticIndexes
     *            The static indexes to add.
     */
    public void addStaticRowIndexesToGroup(int level, String groupName, int... staticIndexes) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.addStaticIndexesToGroup(groupName, staticIndexes);
        }
    }

    /**
     * Adds the given indexes as static indexes to the group that is identified
     * by the given row position. Static indexes are the indexes that stay
     * visible when the group is collapsed.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based right-to-left.
     * @param rowPosition
     *            The position of a group to which the the static indexes should
     *            be added to.
     * @param staticIndexes
     *            The static indexes to add.
     */
    public void addStaticRowIndexesToGroup(int level, int rowPosition, int... staticIndexes) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            groupModel.addStaticIndexesToGroup(rowPosition, staticIndexes);
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
     *            is zero based right-to-left.
     * @param groupName
     *            The name of the group that should be collapsed.
     */
    public void collapseGroup(int level, String groupName) {
        collapseGroup(getGroupModel(level), getGroupByName(groupName));
    }

    /**
     * Collapses the group for the given position, if the row at the specified
     * position belongs to a group.
     *
     * @param position
     *            The position corresponding to this layer whose corresponding
     *            group should be collapsed.
     */
    public void collapseGroup(int position) {
        collapseGroup(0, position);
    }

    /**
     * Collapses the group for the given position, if the row at the specified
     * position belongs to a group.
     *
     * @param level
     *            The grouping level for which the group is requested. The level
     *            is zero based right-to-left.
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
            getPositionLayer().doCommand(new RowGroupCollapseCommand(groupModel, group));
        }
    }

    /**
     * Collapses all groups in all levels.
     */
    public void collapseAllGroups() {
        for (GroupModel groupModel : this.model) {
            if (!groupModel.isEmpty()) {
                getPositionLayer().doCommand(new RowGroupCollapseCommand(groupModel, groupModel.getGroups()));
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
            getPositionLayer().doCommand(new RowGroupCollapseCommand(groupModel, groupModel.getGroups()));
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
     *            is zero based right-to-left.
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
     *            is zero based right-to-left.
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
            getPositionLayer().doCommand(new RowGroupExpandCommand(groupModel, group));
        }
    }

    /**
     * Expands all groups in all levels.
     */
    public void expandAllGroups() {
        for (GroupModel groupModel : this.model) {
            if (!groupModel.isEmpty()) {
                getPositionLayer().doCommand(new RowGroupExpandCommand(groupModel, groupModel.getGroups()));
            }
        }
    }

    /**
     * Expands all groups in the given level.
     *
     * @param level
     *            The grouping level that should be expanded. The level is zero
     *            based right-to-left.
     */
    public void expandAllGroups(int level) {
        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null && !groupModel.isEmpty()) {
            getPositionLayer().doCommand(new RowGroupExpandCommand(groupModel, groupModel.getGroups()));
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
     *            is zero based right-to-left.
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
     *            is zero based right-to-left.
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
     *            is zero based right-to-left.
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
     *            is zero based right-to-left.
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
     *            is zero based right-to-left.
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
     *            is zero based right-to-left.
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
     *            is zero based right-to-left.
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
     *            is zero based right-to-left.
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

    // width

    /**
     *
     * @return <code>true</code> if the a check is performed whether row groups
     *         are configured or not. If not the width of the layer will not
     *         show additional width for showing row groups. <code>false</code>
     *         if the width should be fixed regardless of existing row groups.
     */
    public boolean isCalculateWidth() {
        return this.calculateWidth;
    }

    /**
     * Configure whether the {@link RowGroupHeaderLayer} should calculate the
     * width of the layer dependent on row group configuration or not.
     *
     * @param calculateWidth
     *            <code>true</code> if the layer should check if row groups are
     *            configured and if not, the width of the row header will not
     *            show the double width for showing row groups.
     *            <code>false</code> if the width should be fixed regardless of
     *            existing row groups.
     */
    public void setCalculateWidth(boolean calculateWidth) {
        boolean changed = calculateWidth != this.calculateWidth;
        this.calculateWidth = calculateWidth;
        if (changed) {
            this.fireLayerEvent(new ColumnStructuralRefreshEvent(this));
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
     * this value to <code>true</code> is recommended for huge row groups to
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
     * Used to support row reordering via drag and drop. Needed because on drag
     * the viewport could scroll and therefore on drag end the initial position
     * could not be determined anymore.
     *
     * @return The position from which a row reorder operation was started.
     *         Position is based on the configured {@link #positionLayer}.
     */
    public int getReorderFromRowPosition() {
        return this.reorderFromRowPosition;
    }

    /**
     * Set the position from which a row group drag operation was started.
     * <p>
     * Used to support row reordering via drag and drop. Needed because on drag
     * the viewport could scroll and therefore on drag end the initial position
     * could not be determined anymore.
     *
     * @param fromRowPosition
     *            The position from which a row reorder operation was started.
     *            Position needs to be based on the configured
     *            {@link #positionLayer}.
     */
    public void setReorderFromRowPosition(int fromRowPosition) {
        this.reorderFromRowPosition = fromRowPosition;
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
     * Reorder a row group for the fromRowPosition at the given level to the
     * specified toRowPosition.
     *
     * @param level
     *            The group level on which the group reorder should be
     *            performed.
     * @param fromRowPosition
     *            The row position of a row in the row group that should be
     *            reordered. Based on the configured {@link #positionLayer}.
     * @param toRowPosition
     *            The row position to which a row group should be reordered to.
     *            Based on the configured {@link #positionLayer}.
     * @return <code>true</code> if the reorder command was executed and
     *         consumed successfully
     */
    public boolean reorderRowGroup(int level, int fromRowPosition, int toRowPosition) {
        if (!isReorderSupportedOnLevel(level)
                || !RowGroupUtils.isBetweenTwoGroups(
                        this,
                        level,
                        toRowPosition,
                        toRowPosition < getPositionLayer().getRowCount(),
                        PositionUtil.getVerticalMoveDirection(fromRowPosition, toRowPosition))) {

            // consume the command and avoid reordering a group into another
            // group
            return true;
        }

        // additional check if the group itself is part of an unbreakable higher
        // level group
        if (level < getLevelCount() - 1
                && !RowGroupUtils.isReorderValid(
                        this,
                        level + 1,
                        fromRowPosition,
                        toRowPosition,
                        toRowPosition < getPositionLayer().getRowCount())) {
            // consume the command and avoid reordering that breaks an
            // unbreakable group
            return true;
        }

        GroupModel groupModel = getGroupModel(level);
        if (groupModel != null) {
            Group group = groupModel.getGroupByPosition(fromRowPosition);
            if (group != null) {
                int toPosition = toRowPosition;

                // we need to convert and fire the command on the underlying
                // layer of the positionLayer as otherwise the special command
                // handler is activated
                int underlyingTo = getPositionLayer().localToUnderlyingRowPosition(toPosition);

                // we reorder by index so even hidden columns in a group are
                // reordered
                GroupMultiRowReorderCommand command =
                        new GroupMultiRowReorderCommand(
                                getPositionLayer(),
                                group.getMembers(),
                                underlyingTo);
                Group toBottom = groupModel.getGroupByPosition(toPosition);
                if (toBottom != null) {
                    command.setGroupToBottom(toBottom);
                } else {
                    // check if there is a group to the top
                    Group toTop = groupModel.getGroupByPosition(toPosition - 1);
                    if (toTop != null) {
                        command.setGroupToTop(toTop);
                    }
                }
                command.setReorderByIndex(true);

                return getPositionLayer().getUnderlyingLayerByPosition(0, 0).doCommand(command);
            }
        }
        return false;
    }

    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> labels = super.getProvidedLabels();

        labels.add(GridRegion.ROW_GROUP_HEADER);
        labels.add(GroupHeaderConfigLabels.GROUP_COLLAPSED_CONFIG_TYPE);
        labels.add(GroupHeaderConfigLabels.GROUP_EXPANDED_CONFIG_TYPE);

        // add the labels configured via IConfigLabelAccumulator
        if (getConfigLabelAccumulator() instanceof IConfigLabelProvider) {
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
            // special handling of RowReorderEvent because that events
            // removes and adds a row at the same time and we need special
            // handling in terms of left edge to ungroup if possible
            if (event instanceof RowReorderEvent) {
                // handle reorder event and update model
                updateRowGroupModel((RowReorderEvent) event);
            } else if (event instanceof IStructuralChangeEvent &&
                    ((IStructuralChangeEvent) event).isVerticalStructureChanged()) {
                IStructuralChangeEvent changeEvent = (IStructuralChangeEvent) event;
                Collection<StructuralDiff> rowDiffs = changeEvent.getRowDiffs();
                if (rowDiffs != null && !rowDiffs.isEmpty()) {

                    int[] deletedPositions = getDeletedPositions(rowDiffs);
                    if (deletedPositions != null) {
                        // check if the number of positions are the same as the
                        // number of indexes, otherwise trigger a consistency
                        // check. reason is that the ranges are modified to be
                        // always in a valid range, and that could cause a loss
                        // of hidden positions on conversion
                        if (event instanceof RowStructuralChangeEvent
                                && ((RowStructuralChangeEvent) event).getRowIndexes().length > deletedPositions.length) {
                            // this triggers a consistency check
                            handleDeleteDiffs(new int[0]);
                        } else {
                            handleDeleteDiffs(deletedPositions);
                        }
                    }

                    for (StructuralDiff diff : rowDiffs) {
                        if (diff.getDiffType() == DiffTypeEnum.ADD) {
                            // update visible start positions of all groups
                            updateVisibleStartPositions();

                            for (GroupModel groupModel : RowGroupHeaderLayer.this.model) {
                                Map<Group, UpdateRowGroupCollapseCommand> collapseUpdates = new HashMap<>();

                                // find group and update visible span
                                for (int i = diff.getAfterPositionRange().start; i < diff.getAfterPositionRange().end; i++) {
                                    Group group = groupModel.getGroupByPosition(i);
                                    int newStartIndex = getPositionLayer().getRowIndexByPosition(i);
                                    if (group != null && group.getVisibleStartPosition() <= i) {
                                        if (!group.isCollapsed()) {
                                            group.setVisibleSpan(group.getVisibleSpan() + 1);
                                        } else {
                                            // update collapsed state
                                            UpdateRowGroupCollapseCommand cmd = collapseUpdates.get(group);
                                            if (cmd == null) {
                                                cmd = new UpdateRowGroupCollapseCommand(groupModel, group);
                                                collapseUpdates.put(group, cmd);
                                            }
                                            if (!group.containsStaticIndex(newStartIndex)) {
                                                cmd.addIndexesToShow(newStartIndex);
                                                cmd.addIndexesToHide(getPositionLayer().getRowIndexByPosition(i) + 1);
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
                                        Group topGroup = groupModel.getGroupByPosition(i - 1);
                                        if (topGroup != null
                                                && topGroup.getVisibleSpan() < topGroup.getOriginalSpan()
                                                && topGroup.hasMember(newStartIndex)) {
                                            if (!topGroup.isCollapsed()) {
                                                topGroup.setVisibleSpan(topGroup.getVisibleSpan() + 1);
                                            } else {
                                                if (!topGroup.containsStaticIndex(newStartIndex)) {
                                                    // update collapsed state
                                                    UpdateRowGroupCollapseCommand cmd = collapseUpdates.get(topGroup);
                                                    if (cmd == null) {
                                                        cmd = new UpdateRowGroupCollapseCommand(groupModel, topGroup);
                                                        collapseUpdates.put(topGroup, cmd);
                                                    }
                                                    cmd.addIndexesToHide(getPositionLayer().getRowIndexByPosition(i));
                                                } else {
                                                    topGroup.setVisibleSpan(topGroup.getVisibleSpan() + 1);
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
                                                for (int e = diff.getAfterPositionRange().end; e > diff.getAfterPositionRange().start; e--) {
                                                    g = groupModel.getGroupByPosition(e);
                                                    if (g != null && g.hasMember(newStartIndex)) {
                                                        g.setStartIndex(newStartIndex);
                                                        g.setVisibleStartIndex(newStartIndex);
                                                        g.setVisibleSpan(g.getVisibleSpan() + 1);
                                                        g.updateVisibleStartPosition();
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                for (UpdateRowGroupCollapseCommand cmd : collapseUpdates.values()) {
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
                    // the table) or a complete refresh was triggered which
                    // means there are no diffs
                    performConsistencyCheck(rowDiffs == null);
                }
            }
        }

        private int[] getDeletedPositions(Collection<StructuralDiff> rowDiffs) {
            MutableIntList result = IntLists.mutable.empty();
            boolean deleteDiffFound = false;
            for (StructuralDiff diff : rowDiffs) {
                if (diff.getDiffType() == DiffTypeEnum.DELETE) {
                    deleteDiffFound = true;
                    int[] positions = PositionUtil.getPositions(diff.getBeforePositionRange());
                    result.addAll(positions);
                }
            }
            if (deleteDiffFound) {
                // we need to handle the deleted positions backwards
                return result.sortThis().reverseThis().toArray();
            } else {
                // there was no DELETE diff, so we return null
                return null;
            }
        }

        private void handleDeleteDiffs(int[] positionList) {
            if (positionList.length > 0) {
                for (GroupModel groupModel : RowGroupHeaderLayer.this.model) {
                    // we need to create the MutableIntList on a copy of the
                    // positionList array, because modifications on the
                    // MutableIntList will be traversed to the the underlying
                    // array
                    MutableIntList groupPositionList = IntLists.mutable.of(Arrays.copyOf(positionList, positionList.length));
                    while (!groupPositionList.isEmpty()) {
                        // find group and update visible span
                        // we need to iterate because one could hide the
                        // last row in one group and the first of
                        // another group at once
                        int pos = groupPositionList.get(0);
                        Group group = groupModel.getGroupByPosition(pos);
                        if (group != null) {
                            // find all positions belonging to
                            // the group
                            MutableIntList groupPositions = IntLists.mutable.of(group.getVisiblePositions());

                            // find the positions in the group
                            // that are hidden
                            MutableIntList hiddenGroupPositions = groupPositions.select(groupPositionList::contains);

                            // update the positionList as we
                            // handled the hidden rows
                            groupPositionList.removeAll(hiddenGroupPositions);

                            groupPositions.removeAll(hiddenGroupPositions);
                            if (groupPositions.size() > 0) {
                                group.setVisibleSpan(groupPositions.size());
                                hiddenGroupPositions.forEach(i -> {
                                    if (group.getVisibleStartPosition() == i) {
                                        group.setVisibleStartIndex(getPositionLayer().getRowIndexByPosition(i - groupPositionList.size()));
                                    }
                                });
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
                performConsistencyCheck(false);
            }
        }

        private void updateVisibleStartPositions() {
            for (GroupModel groupModel : RowGroupHeaderLayer.this.model) {
                groupModel.updateVisibleStartPositions();
            }
        }

        private void performConsistencyCheck(boolean updateStartIndex) {
            for (GroupModel groupModel : RowGroupHeaderLayer.this.model) {
                groupModel.performConsistencyCheck(updateStartIndex);
            }
        }

        private void updateRowGroupModel(RowReorderEvent reorderEvent) {

            int[] fromRowPositions = PositionUtil.getPositions(reorderEvent.getBeforeFromRowPositionRanges());
            int toRowPosition = reorderEvent.getBeforeToRowPosition();
            boolean reorderToTopEdge = reorderEvent.isReorderToTopEdge();

            int fromRowPosition = toRowPosition;

            if (toRowPosition > fromRowPositions[fromRowPositions.length - 1]) {
                // Moving down
                fromRowPosition = fromRowPositions[fromRowPositions.length - 1];
            } else if (toRowPosition < fromRowPositions[fromRowPositions.length - 1]) {
                // Moving up
                fromRowPosition = fromRowPositions[0];
            }

            MoveDirectionEnum moveDirection = PositionUtil.getVerticalMoveDirection(fromRowPosition, toRowPosition);

            if (reorderToTopEdge
                    && toRowPosition > 0
                    && MoveDirectionEnum.DOWN == moveDirection) {
                toRowPosition--;
            }

            if (fromRowPosition == -1 || toRowPosition == -1) {
                LOG.error("Invalid reorder positions, fromPosition: {}, toPosition: {}", fromRowPosition, toRowPosition); //$NON-NLS-1$
                return;
            }

            for (GroupModel groupModel : RowGroupHeaderLayer.this.model) {
                Group toRowGroup = groupModel.getGroupByPosition(toRowPosition);

                Group fromRowGroup = groupModel.getGroupByPosition(fromRowPosition);

                if (fromRowGroup != null
                        && toRowGroup != null
                        && fromRowGroup.equals(toRowGroup)) {
                    // movement inside a row group

                    // if from and to position are the same and it is a
                    // row at the edge of a group, remove the
                    // position from the group
                    if (fromRowPosition == toRowPosition
                            && (!RowGroupUtils.isGroupReordered(fromRowGroup, fromRowPositions)
                                    || (fromRowGroup.isCollapsed() && fromRowGroup.getMembers().length > 1))
                            && (fromRowGroup.isGroupStart(fromRowPosition)
                                    || fromRowGroup.isGroupEnd(fromRowPosition))) {
                        if (MoveDirectionEnum.DOWN == moveDirection) {
                            int pos[] = new int[fromRowPositions.length];
                            int index = 0;
                            for (int from = fromRowPositions.length - 1; from >= 0; from--) {
                                pos[index] = fromRowPositions[from];
                                index++;
                            }
                            removePositionsFromGroup(
                                    groupModel,
                                    fromRowGroup,
                                    pos,
                                    reorderEvent.getBeforeFromRowIndexesArray(),
                                    fromRowPosition,
                                    moveDirection);
                        } else {
                            removePositionsFromGroup(
                                    groupModel,
                                    fromRowGroup,
                                    fromRowPositions,
                                    reorderEvent.getBeforeFromRowIndexesArray(),
                                    fromRowPosition,
                                    moveDirection);
                        }
                        break;
                    }

                    // if we moved the first visible start position or
                    // moved another row to the visible start
                    // position, we need to update the start index
                    if (fromRowPosition != toRowPosition
                            && (fromRowGroup.getVisibleStartPosition() == fromRowPositions[0]
                                    || toRowGroup.getVisibleStartPosition() == toRowPosition)) {
                        int newStartIndex = getPositionLayer().getRowIndexByPosition(fromRowGroup.getVisibleStartPosition());
                        fromRowGroup.setVisibleStartIndex(newStartIndex);
                        if (!fromRowGroup.isCollapsed()
                                || fromRowGroup.containsStaticIndex(fromRowGroup.getStartIndex())) {
                            // if we move inside a collapsed group, there are
                            // static indexes, and therefore we do not update
                            // the start index, as the start index should be the
                            // same on expand
                            fromRowGroup.setStartIndex(newStartIndex);
                        }
                    }
                } else if (fromRowGroup == null
                        && toRowGroup != null) {
                    addPositionsToGroup(
                            groupModel,
                            toRowGroup,
                            fromRowPositions,
                            reorderEvent.getBeforeFromRowIndexesArray(),
                            toRowPosition,
                            moveDirection);
                } else if (fromRowGroup != null
                        && toRowGroup == null
                        && !RowGroupUtils.isGroupReordered(fromRowGroup, fromRowPositions)) {
                    removePositionsFromGroup(
                            groupModel,
                            fromRowGroup,
                            fromRowPositions,
                            reorderEvent.getBeforeFromRowIndexesArray(),
                            fromRowPosition,
                            moveDirection);
                } else if (fromRowGroup == null
                        && toRowGroup == null
                        && fromRowPosition == toRowPosition
                        && fromRowPositions.length == 1) {
                    // this might happen on drag and drop operations when
                    // trying to add a column back into an adjacent column
                    // group
                    int adjacentPos = (moveDirection == MoveDirectionEnum.DOWN) ? fromRowPosition + 1 : fromRowPosition - 1;
                    // check if there is an adjacent column group
                    Group adjacentRowGroup = groupModel.getGroupByPosition(adjacentPos);
                    if (adjacentRowGroup != null && !adjacentRowGroup.isUnbreakable()) {
                        addPositionsToGroup(
                                groupModel,
                                adjacentRowGroup,
                                fromRowPositions,
                                reorderEvent.getBeforeFromRowIndexesArray(),
                                (adjacentRowGroup.isCollapsed() && moveDirection != MoveDirectionEnum.DOWN) ? toRowPosition : adjacentPos,
                                moveDirection);
                    }
                } else if (fromRowGroup != null
                        && toRowGroup != null
                        && !fromRowGroup.equals(toRowGroup)
                        && (!RowGroupUtils.isGroupReordered(fromRowGroup, fromRowPositions)
                                || (!fromRowGroup.isCollapsed() && fromRowGroup.getVisiblePositions().length == 1))) {

                    removePositionsFromGroup(
                            groupModel,
                            fromRowGroup,
                            fromRowPositions,
                            reorderEvent.getBeforeFromRowIndexesArray(),
                            fromRowPositions[0],
                            moveDirection);
                    addPositionsToGroup(
                            groupModel,
                            toRowGroup,
                            fromRowPositions,
                            reorderEvent.getBeforeFromRowIndexesArray(),
                            toRowPosition,
                            moveDirection);
                }

                groupModel.updateVisibleStartPositions();
            }
        }

        private void addPositionsToGroup(
                GroupModel groupModel,
                Group group,
                int[] fromRowPositions,
                int[] fromRowIndexes,
                int toPosition,
                MoveDirectionEnum moveDirection) {

            if (!group.isUnbreakable()) {
                // increase the span as column is moved inside group
                group.setOriginalSpan(group.getOriginalSpan() + fromRowPositions.length);

                // update the start index
                if (group.isGroupStart(toPosition)) {
                    int newStartIndex = (moveDirection == MoveDirectionEnum.DOWN)
                            ? getPositionLayer().getRowIndexByPosition(group.getVisibleStartPosition() - fromRowPositions.length)
                            : getPositionLayer().getRowIndexByPosition(group.getVisibleStartPosition());

                    if (group.getVisibleStartIndex() == group.getStartIndex()) {
                        group.setStartIndex(newStartIndex);
                    }

                    group.setVisibleStartIndex(newStartIndex);
                }

                // add the member indexes
                group.addMembers(fromRowIndexes);

                if (group.isCollapsed()) {
                    // update collapsed state
                    UpdateRowGroupCollapseCommand cmd = new UpdateRowGroupCollapseCommand(groupModel, group);
                    if (group.isGroupStart(toPosition)) {
                        cmd.addIndexesToHide(group.getMembers());

                        if (group.getStaticIndexes().length > 0) {
                            group.setVisibleSpan(group.getVisibleSpan() + fromRowPositions.length);
                        }

                        // the update command will trigger a hide event to hide
                        // the previous visible first row, therefore we need
                        // to update the visible start position before firing
                        // the command to ensure the hide handling is operating
                        // on the correct positions
                        group.updateVisibleStartPosition();
                    } else {
                        cmd.addIndexesToHide(fromRowIndexes);
                        group.setVisibleSpan(group.getVisibleSpan() + fromRowPositions.length);
                    }
                    getPositionLayer().doCommand(cmd);
                } else {
                    // we update the visible span only in case the group is not
                    // collapsed
                    group.setVisibleSpan(group.getVisibleSpan() + fromRowPositions.length);
                }
            }
        }

        private void removePositionsFromGroup(
                GroupModel groupModel,
                Group group,
                int[] fromRowPositions,
                int[] fromRowIndexes,
                int fromRowPosition,
                MoveDirectionEnum moveDirection) {

            if (!group.isUnbreakable()) {
                boolean collapsed = group.isCollapsed();
                if (collapsed) {
                    int fromRowIndex = getPositionLayer().getRowIndexByPosition(fromRowPosition);
                    // we need to expand to make the next row visible again
                    expandGroup(groupModel, group);
                    fromRowPosition = getPositionLayer().getRowPositionByIndex(fromRowIndex);
                }

                // decrease the span as row is moved out of group
                group.setOriginalSpan(group.getOriginalSpan() - fromRowPositions.length);
                group.setVisibleSpan(group.getVisibleSpan() - fromRowPositions.length);

                // update the start index
                if (group.isGroupStart(fromRowPosition)) {
                    int newStartIndex = (moveDirection == MoveDirectionEnum.DOWN)
                            ? getPositionLayer().getRowIndexByPosition(group.getVisibleStartPosition())
                            : getPositionLayer().getRowIndexByPosition(group.getVisibleStartPosition() + fromRowPositions.length);
                    group.setStartIndex(newStartIndex);
                    group.setVisibleStartIndex(newStartIndex);
                }

                // remove the member indexes
                group.removeMembers(fromRowIndexes);

                // remove static index if removed position was a static index
                group.removeStaticIndexes(fromRowIndexes);

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

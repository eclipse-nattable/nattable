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
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 448021, 453707
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.collections.api.factory.primitive.IntSets;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.hideshow.AbstractRowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeCollapseAllCommandHandler;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandAllCommandHandler;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandCollapseCommandHandler;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandToLevelCommandHandler;
import org.eclipse.nebula.widgets.nattable.tree.config.DefaultTreeLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.tree.config.TreeConfigAttributes;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeLayer extends AbstractRowHideShowLayer {

    private static final Logger LOG = LoggerFactory.getLogger(TreeLayer.class);

    public static final String TREE_COLUMN_CELL = "TREE_COLUMN_CELL"; //$NON-NLS-1$

    public static final int TREE_COLUMN_NUMBER = 0;

    /**
     * Flag to configure whether the tree column should be identified by
     * position or by index. Default is position.
     */
    private boolean useTreeColumnIndex = false;

    /**
     * The ITreeRowModelListener that is used to get information about the tree
     * structure.
     */
    private final ITreeRowModel<?> treeRowModel;

    /**
     * Collection of all row indexes that are hidden if tree nodes are
     * collapsed.
     * <p>
     * Note: This collection is only in use if the used {@link ITreeRowModel}
     * implementation is returning the row indexes of affected rows on
     * expand/collapse. There are also implementations that use another approach
     * where the hide/show approach is not used (e.g. GlazedListTreeRowModel)
     * </p>
     */
    private MutableIntSet hiddenRowIndexes = IntSets.mutable.empty();

    /**
     * The IndentedTreeImagePainter that paints indentation to the left of the
     * configured base painter and icons for expand/collapse if possible, to
     * render tree structure accordingly.
     */
    private IndentedTreeImagePainter indentedTreeImagePainter;

    /**
     * Creates a TreeLayer instance based on the given information. Will use a
     * default IndentedTreeImagePainter that uses 10 pixels for indentation and
     * simple + and - icons for expand/collapse icons. It also uses the
     * DefaultTreeLayerConfiguration.
     *
     * @param underlyingLayer
     *            The underlying layer on whose top this layer will be set.
     * @param treeRowModel
     *            The ITreeRowModelListener that is used to get information
     *            about the tree structure.
     */
    public TreeLayer(IUniqueIndexLayer underlyingLayer, ITreeRowModel<?> treeRowModel) {
        this(underlyingLayer, treeRowModel, new IndentedTreeImagePainter());
    }

    /**
     * Creates a TreeLayer instance based on the given information. Allows to
     * specify the IndentedTreeImagePainter while using the
     * DefaultTreeLayerConfiguration.
     *
     * @param underlyingLayer
     *            The underlying layer on whose top this layer will be set.
     * @param treeRowModel
     *            The ITreeRowModelListener that is used to get information
     *            about the tree structure.
     * @param indentedTreeImagePainter
     *            The IndentedTreeImagePainter that paints indentation to the
     *            left of the configured base painter and icons for
     *            expand/collapse if possible, to render tree structure
     *            accordingly.
     */
    public TreeLayer(
            IUniqueIndexLayer underlyingLayer,
            ITreeRowModel<?> treeRowModel,
            IndentedTreeImagePainter indentedTreeImagePainter) {
        this(underlyingLayer, treeRowModel, indentedTreeImagePainter, true);
    }

    /**
     * Creates a TreeLayer instance based on the given information. Will use a
     * default IndentedTreeImagePainter that uses 10 pixels for indentation and
     * simple + and - icons for expand/collapse icons.
     *
     * @param underlyingLayer
     *            The underlying layer on whose top this layer will be set.
     * @param treeRowModel
     *            The ITreeRowModelListener that is used to get information
     *            about the tree structure.
     * @param useDefaultConfiguration
     *            <code>true</code> to use the DefaultTreeLayerConfiguration,
     *            <code>false</code> if you want to specify your own
     *            configuration.
     */
    public TreeLayer(
            IUniqueIndexLayer underlyingLayer,
            ITreeRowModel<?> treeRowModel,
            boolean useDefaultConfiguration) {
        this(underlyingLayer,
                treeRowModel,
                new IndentedTreeImagePainter(),
                useDefaultConfiguration);
    }

    /**
     * Creates a TreeLayer instance based on the given information.
     *
     * @param underlyingLayer
     *            The underlying layer on whose top this layer will be set.
     * @param treeRowModel
     *            The ITreeRowModelListener that is used to get information
     *            about the tree structure.
     * @param indentedTreeImagePainter
     *            The IndentedTreeImagePainter that paints indentation to the
     *            left of the configured base painter and icons for
     *            expand/collapse if possible, to render tree structure
     *            accordingly.
     * @param useDefaultConfiguration
     *            <code>true</code> to use the DefaultTreeLayerConfiguration,
     *            <code>false</code> if you want to specify your own
     *            configuration.
     */
    public TreeLayer(
            IUniqueIndexLayer underlyingLayer,
            ITreeRowModel<?> treeRowModel,
            IndentedTreeImagePainter indentedTreeImagePainter,
            boolean useDefaultConfiguration) {

        super(underlyingLayer);
        this.treeRowModel = treeRowModel;

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultTreeLayerConfiguration(this));
        }

        this.indentedTreeImagePainter = indentedTreeImagePainter;

        registerCommandHandler(new TreeExpandCollapseCommandHandler(this));
        registerCommandHandler(new TreeCollapseAllCommandHandler(this));
        registerCommandHandler(new TreeExpandAllCommandHandler(this));
        registerCommandHandler(new TreeExpandToLevelCommandHandler(this));
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        LabelStack configLabels = super.getConfigLabelsByPosition(columnPosition, rowPosition);

        if (isTreeColumn(columnPosition)) {
            configLabels.addLabelOnTop(TREE_COLUMN_CELL);

            ILayerCell cell = getCellByPosition(columnPosition, rowPosition);
            if (cell != null) {
                int rowIndex = getRowIndexByPosition(cell.getOriginRowPosition());
                configLabels.addLabelOnTop(
                        DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + this.treeRowModel.depth(rowIndex));
                if (!this.treeRowModel.hasChildren(rowIndex)) {
                    configLabels.addLabelOnTop(DefaultTreeLayerConfiguration.TREE_LEAF_CONFIG_TYPE);
                } else {
                    if (this.treeRowModel.isCollapsed(rowIndex)) {
                        configLabels.addLabelOnTop(DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE);
                    } else {
                        configLabels.addLabelOnTop(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE);
                    }
                }
            }
        }
        return configLabels;
    }

    /**
     * @return The ITreeRowModelListener that is used to get information about
     *         the tree structure.
     */
    public ITreeRowModel<?> getModel() {
        return this.treeRowModel;
    }

    /**
     * @param columnPosition
     *            The column position to check.
     * @return <code>true</code> if the given column position is the tree
     *         column, <code>false</code> if not.
     * @since 1.6
     */
    protected boolean isTreeColumn(int columnPosition) {
        if (this.useTreeColumnIndex) {
            return getColumnIndexByPosition(columnPosition) == TREE_COLUMN_NUMBER;
        }

        return columnPosition == TREE_COLUMN_NUMBER;
    }

    @Override
    public ICellPainter getCellPainter(
            int columnPosition, int rowPosition,
            ILayerCell cell, IConfigRegistry configRegistry) {
        ICellPainter cellPainter = super.getCellPainter(
                columnPosition, rowPosition, cell, configRegistry);

        if (cell.getConfigLabels().hasLabel(TREE_COLUMN_CELL)) {

            ICellPainter treeCellPainter = configRegistry.getConfigAttribute(
                    TreeConfigAttributes.TREE_STRUCTURE_PAINTER,
                    cell.getDisplayMode(),
                    cell.getConfigLabels());

            if (treeCellPainter != null) {
                IndentedTreeImagePainter treePainter = findIndentedTreeImagePainter(treeCellPainter);

                if (treePainter != null) {
                    treePainter.setBaseCellPainter(cellPainter);
                    cellPainter = treeCellPainter;
                } else {
                    // log error
                    LOG.warn("There is no IndentedTreeImagePainter found for TREE_STRUCTURE_PAINTER, " //$NON-NLS-1$
                            + "using local configured IndentedTreeImagePainter as fallback"); //$NON-NLS-1$
                    // fallback
                    this.indentedTreeImagePainter.setBaseCellPainter(cellPainter);
                    cellPainter = new BackgroundPainter(this.indentedTreeImagePainter);
                }
            } else {
                // backwards compatibility fallback
                this.indentedTreeImagePainter.setBaseCellPainter(cellPainter);
                cellPainter = new BackgroundPainter(this.indentedTreeImagePainter);
            }
        }

        return cellPainter;
    }

    private IndentedTreeImagePainter findIndentedTreeImagePainter(ICellPainter painter) {
        IndentedTreeImagePainter result = null;
        if (painter instanceof IndentedTreeImagePainter) {
            result = (IndentedTreeImagePainter) painter;
        } else if (painter instanceof CellPainterWrapper
                && ((CellPainterWrapper) painter).getWrappedPainter() != null) {
            result = findIndentedTreeImagePainter(((CellPainterWrapper) painter).getWrappedPainter());
        } else if (painter instanceof CellPainterDecorator) {
            result = findIndentedTreeImagePainter(((CellPainterDecorator) painter).getBaseCellPainter());
            if (result == null) {
                result = findIndentedTreeImagePainter(((CellPainterDecorator) painter).getDecoratorCellPainter());
            }
        }
        return result;
    }

    @Override
    public boolean isRowIndexHidden(int rowIndex) {
        return this.hiddenRowIndexes.contains(rowIndex) || isHiddenInUnderlyingLayer(rowIndex);
    }

    @Override
    public Collection<Integer> getHiddenRowIndexes() {
        return ArrayUtil.asIntegerList(this.hiddenRowIndexes.toSortedArray());
    }

    @Override
    public int[] getHiddenRowIndexesArray() {
        return this.hiddenRowIndexes.toSortedArray();
    }

    @Override
    public boolean hasHiddenRows() {
        return !this.hiddenRowIndexes.isEmpty();
    }

    /**
     * Performs an expand/collapse action dependent on the current state of the
     * tree node at the given row index.
     *
     * @param parentIndex
     *            The index of the row that shows the tree node for which the
     *            expand/collapse action should be performed.
     */
    public void expandOrCollapseIndex(int parentIndex) {
        if (this.treeRowModel.isCollapsed(parentIndex)) {
            expandTreeRow(parentIndex);
        } else {
            collapseTreeRow(parentIndex);
        }
    }

    /**
     * Collapses the tree node for the given row index.
     *
     * @param parentIndex
     *            The index of the row that shows the node that should be
     *            collapsed
     */
    public void collapseTreeRow(int parentIndex) {
        int[] rowIndexes = this.treeRowModel.collapse(parentIndex).stream()
                .mapToInt(Integer::intValue)
                .sorted()
                .toArray();

        // if the rowPos is negative, it is not visible because of hidden
        // state in an underlying layer
        int[] rowPositions = Arrays.stream(rowIndexes)
                .map(this::getRowPositionByIndex)
                .filter(rowPos -> rowPos >= 0)
                .sorted()
                .toArray();

        // collect the indexes that where really hidden in this layer
        int[] hiddenIndexes = Arrays.stream(rowPositions)
                .map(this::getRowIndexByPosition)
                .sorted()
                .toArray();

        this.hiddenRowIndexes.addAll(rowIndexes);
        invalidateCache();
        fireLayerEvent(new HideRowPositionsEvent(this, rowPositions, hiddenIndexes));
    }

    /**
     * Collapses all tree nodes in the tree.
     */
    public void collapseAll() {
        int[] rowIndexes = this.treeRowModel.collapseAll().stream()
                .mapToInt(Integer::intValue)
                .sorted()
                .toArray();

        // if the rowPos is negative, it is not visible because of hidden
        // state in an underlying layer
        int[] rowPositions = Arrays.stream(rowIndexes)
                .map(this::getRowPositionByIndex)
                .filter(rowPos -> rowPos >= 0)
                .sorted()
                .toArray();

        // collect the indexes that where really hidden in this layer
        int[] hiddenIndexes = Arrays.stream(rowPositions)
                .map(this::getRowIndexByPosition)
                .sorted()
                .toArray();

        this.hiddenRowIndexes.addAll(rowIndexes);
        invalidateCache();
        fireLayerEvent(new HideRowPositionsEvent(this, rowPositions, hiddenIndexes));
    }

    /**
     * Expands the tree node for the given row index.
     *
     * @param parentIndex
     *            The index of the row that shows the node that should be
     *            expanded
     */
    public void expandTreeRow(int parentIndex) {
        internalExpand(this.treeRowModel.expand(parentIndex));
    }

    /**
     * Expands the tree node for the given row index in the tree to a certain
     * level.
     *
     * @param parentIndex
     *            The index of the row that shows the node that should be
     *            expanded
     * @param level
     *            The level to which the tree node should be expanded.
     */
    public void expandTreeRowToLevel(int parentIndex, int level) {
        internalExpand(this.treeRowModel.expandToLevel(parentIndex, level));
    }

    /**
     * Expands all tree nodes in the tree.
     */
    public void expandAll() {
        List<Integer> rowIndexes = this.treeRowModel.expandAll();
        // optimization to avoid huge empty collections in memory after clear
        this.hiddenRowIndexes = IntSets.mutable.empty();
        invalidateCache();
        fireLayerEvent(new ShowRowPositionsEvent(
                this,
                rowIndexes.stream()
                        .mapToInt(this::getRowPositionByIndex)
                        .filter(r -> r >= 0)
                        .sorted()
                        .toArray()));
    }

    /**
     * Expands all tree nodes in the tree to a certain level.
     *
     * @param level
     *            The level to which the tree node should be expanded.
     */
    public void expandAllToLevel(int level) {
        internalExpand(this.treeRowModel.expandToLevel(level));
    }

    /**
     * Remove the given row indexes from the local collection of hidden row
     * indexes, invalidate the cache and fire a {@link ShowRowPositionsEvent}.
     *
     * @param rowIndexes
     *            The row indexes to show again.
     */
    private void internalExpand(List<Integer> rowIndexes) {
        this.hiddenRowIndexes.removeAll(rowIndexes.stream().mapToInt(Integer::intValue).toArray());
        invalidateCache();
        fireLayerEvent(new ShowRowPositionsEvent(
                this,
                rowIndexes.stream()
                        .mapToInt(this::getRowPositionByIndex)
                        .filter(r -> r >= 0)
                        .sorted()
                        .toArray()));
    }

    /**
     * Checks the underlying layer if the row is hidden by another layer.
     *
     * @param rowIndex
     *            The index of the row whose hidden state should be checked
     * @return <code>true</code> if the row at the given index is hidden in the
     *         underlying layer <code>false</code> if not.
     */
    private boolean isHiddenInUnderlyingLayer(int rowIndex) {
        IUniqueIndexLayer underlyingLayer = getUnderlyingLayer();
        return (underlyingLayer.getRowPositionByIndex(rowIndex) == -1);
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        // special command transformations are needed to hide also child nodes
        if (command instanceof RowHideCommand) {
            return handleRowHideCommand((RowHideCommand) command);
        } else if (command instanceof MultiRowHideCommand) {
            return handleMultiRowHideCommand((MultiRowHideCommand) command);
        }
        return super.doCommand(command);
    }

    /**
     * Checks if the given command tries to hide a row that is a node that is
     * not collapsed and has children. In that case also the child rows need to
     * be hidden.
     *
     * @param command
     *            The {@link RowHideCommand} to process
     * @return <code>true</code> if the command has been handled,
     *         <code>false</code> otherwise
     */
    protected boolean handleRowHideCommand(RowHideCommand command) {
        // transform position to index
        if (command.convertToTargetLayer(this)) {
            int rowIndex = getRowIndexByPosition(command.getRowPosition());
            if (this.treeRowModel.hasChildren(rowIndex)
                    && !this.treeRowModel.isCollapsed(rowIndex)) {
                List<Integer> childIndexes = this.treeRowModel.getChildIndexes(rowIndex);
                int[] childPositions = new int[childIndexes.size() + 1];
                childPositions[0] = command.getRowPosition();
                for (int i = 1; i < childIndexes.size() + 1; i++) {
                    int childPos = getRowPositionByIndex(childIndexes.get(i - 1));
                    childPositions[i] = childPos;
                }
                return super.doCommand(new MultiRowHideCommand(this, childPositions));
            }
        }
        return super.doCommand(command);
    }

    /**
     * Checks if the given command tries to hide rows that are nodes that are
     * not collapsed and have children. In that case also the child rows need to
     * be hidden.
     *
     * @param command
     *            The {@link MultiRowHideCommand} to process
     * @return <code>true</code> if the command has been handled,
     *         <code>false</code> otherwise
     */
    protected boolean handleMultiRowHideCommand(MultiRowHideCommand command) {
        // transform position to index
        if (command.convertToTargetLayer(this)) {
            List<Integer> rowPositionsToHide = new ArrayList<>();
            for (int rowPos : command.getRowPositionsArray()) {
                rowPositionsToHide.add(rowPos);
                int rowIndex = getRowIndexByPosition(rowPos);
                if (this.treeRowModel.hasChildren(rowIndex)
                        && !this.treeRowModel.isCollapsed(rowIndex)) {
                    List<Integer> childIndexes = this.treeRowModel.getChildIndexes(rowIndex);
                    for (Integer childIndex : childIndexes) {
                        rowPositionsToHide.add(getRowPositionByIndex(childIndex));
                    }
                }
            }

            int[] childPositions = new int[rowPositionsToHide.size()];
            for (int i = 0; i < rowPositionsToHide.size(); i++) {
                childPositions[i] = rowPositionsToHide.get(i);
            }
            return super.doCommand(new MultiRowHideCommand(this, childPositions));
        }
        return super.doCommand(command);
    }

    /**
     * @return <code>true</code> if the column index is used to determine the
     *         tree column, <code>false</code> if the column position is used.
     *         Default is <code>false</code>.
     */
    public boolean isUseTreeColumnIndex() {
        return this.useTreeColumnIndex;
    }

    /**
     * Configure whether (column index == 0) or (column position == 0) should be
     * performed to identify the tree column.
     *
     * @param useTreeColumnIndex
     *            <code>true</code> if the column index should be used to
     *            determine the tree column, <code>false</code> if the column
     *            position should be used.
     */
    public void setUseTreeColumnIndex(boolean useTreeColumnIndex) {
        this.useTreeColumnIndex = useTreeColumnIndex;
    }

    /**
     * @since 1.4
     */
    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> result = super.getProvidedLabels();

        result.add(TreeLayer.TREE_COLUMN_CELL);
        result.add(DefaultTreeLayerConfiguration.TREE_LEAF_CONFIG_TYPE);
        result.add(DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE);
        result.add(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE);
        // configure 5 levels to be configurable via CSS
        // if you need more you need to override this method
        result.add(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + "0"); //$NON-NLS-1$
        result.add(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + "1"); //$NON-NLS-1$
        result.add(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + "2"); //$NON-NLS-1$
        result.add(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + "3"); //$NON-NLS-1$
        result.add(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + "4"); //$NON-NLS-1$

        return result;
    }
}

/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Jonas Hugo <Jonas.Hugo@jeppesen.com>,
 *       Markus Wahl <Markus.Wahl@jeppesen.com> - Delegate markers to model if
 *         model is an IMarkerSelectionModel. Add getters and setters for marker fields
 *     neal zhang <nujiah001@126.com> - change some methods and fields visibility
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 446275, 453851
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.edit.command.EditSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.fillhandle.FillHandleLayerPainter;
import org.eclipse.nebula.widgets.nattable.fillhandle.action.FillHandleDragMode;
import org.eclipse.nebula.widgets.nattable.grid.command.InitializeAutoResizeColumnsCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.command.InitializeAutoResizeRowsCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiRowResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.search.command.SearchGridCellsCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.command.ClearAllSelectionsCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectAllCommand;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Enables selection of column, rows, cells etc. on the table. Also responds to
 * UI bindings by changing the current selection. Internally it uses the
 * {@link ISelectionModel} to track the selection state.
 *
 * @see DefaultSelectionLayerConfiguration
 * @see MoveDirectionEnum
 */
public class SelectionLayer extends AbstractIndexLayerTransform {

    public static final int MOVE_ALL = -1;
    public static final int NO_SELECTION = -1;

    public enum MoveDirectionEnum {
        UP, DOWN, LEFT, RIGHT, NONE;
    }

    protected ISelectionModel selectionModel;
    protected IUniqueIndexLayer underlyingLayer;
    protected final PositionCoordinate lastSelectedCell;
    protected final PositionCoordinate selectionAnchor;
    protected Rectangle lastSelectedRegion;

    /**
     * The region <i>selected</i> via fill handle to extend the current
     * selection for triggering a fill action. Can be <code>null</code>.
     *
     * @since 1.4
     */
    protected Rectangle fillHandleRegion;

    /**
     * The bottom right cell in a contiguous selection or <code>null</code> if
     * there is no selection or the selection is not contiguous. Needed to
     * identify the cell on which the fill handle should be rendered.
     *
     * @since 1.4
     */
    protected PositionCoordinate bottomRightInSelection;

    protected SelectRowCommandHandler selectRowCommandHandler;
    protected SelectCellCommandHandler selectCellCommandHandler;
    protected SelectColumnCommandHandler selectColumnCommandHandler;

    public SelectionLayer(IUniqueIndexLayer underlyingLayer) {
        this(underlyingLayer, null, true);
    }

    public SelectionLayer(IUniqueIndexLayer underlyingLayer, boolean useDefaultConfiguration) {
        this(underlyingLayer, null, useDefaultConfiguration);
    }

    public SelectionLayer(IUniqueIndexLayer underlyingLayer, ISelectionModel selectionModel,
            boolean useDefaultConfiguration) {
        this(underlyingLayer, selectionModel, useDefaultConfiguration, true);
    }

    /**
     * @deprecated the ISelectionModel is now itself an ILayerEventHandler
     */
    @Deprecated
    public SelectionLayer(IUniqueIndexLayer underlyingLayer, ISelectionModel selectionModel,
            boolean useDefaultConfiguration, boolean registerDefaultEventHandler) {
        super(underlyingLayer);
        this.underlyingLayer = underlyingLayer;

        setLayerPainter(new SelectionLayerPainter());

        setSelectionModel(selectionModel);

        this.lastSelectedCell = new PositionCoordinate(this, NO_SELECTION, NO_SELECTION);
        this.selectionAnchor = new PositionCoordinate(this, NO_SELECTION, NO_SELECTION);

        this.selectRowCommandHandler = new SelectRowCommandHandler(this);
        this.selectCellCommandHandler = new SelectCellCommandHandler(this);
        this.selectColumnCommandHandler = new SelectColumnCommandHandler(this);

        registerCommandHandlers();

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultSelectionLayerConfiguration());
        }
    }

    public ISelectionModel getSelectionModel() {
        return this.selectionModel;
    }

    public void setSelectionModel(ISelectionModel selectionModel) {
        if (this.selectionModel != null) {
            unregisterEventHandler(this.selectionModel);
        }
        this.selectionModel = selectionModel != null ? selectionModel : new SelectionModel(this);
        registerEventHandler(this.selectionModel);
    }

    @Override
    public ILayerPainter getLayerPainter() {
        return this.layerPainter;
    }

    public void addSelection(Rectangle selection) {
        if (!selection.equals(getLastSelectedRegion())) {
            setSelectionAnchor(getLastSelectedCell().columnPosition, getLastSelectedCell().rowPosition);
            setLastSelectedRegion(selection);
        }

        this.selectionModel.addSelection(selection);
    }

    public void clear() {
        clear(true);
    }

    public void clear(boolean fireSelectionEvent) {
        this.selectionModel.clearSelection();

        boolean validLastSelectedCell = hasSelection(getLastSelectedCell());
        setLastSelectedCell(NO_SELECTION, NO_SELECTION);
        setLastSelectedRegion(new Rectangle(0, 0, 0, 0));

        setSelectionAnchor(NO_SELECTION, NO_SELECTION);

        if (validLastSelectedCell && fireSelectionEvent) {
            fireCellSelectionEvent(
                    getLastSelectedCell().columnPosition,
                    getLastSelectedCell().rowPosition,
                    false, false, false);
        }
    }

    public void clearSelection(int columnPosition, int rowPosition) {
        this.selectionModel.clearSelection(columnPosition, rowPosition);

        if (getSelectionAnchor().columnPosition == columnPosition
                && getSelectionAnchor().rowPosition == rowPosition) {
            setSelectionAnchor(NO_SELECTION, NO_SELECTION);
        }

        if (this.selectionModel.isEmpty()) {
            setLastSelectedCell(NO_SELECTION, NO_SELECTION);
        }
    }

    public void clearSelection(Rectangle selection) {
        this.selectionModel.clearSelection(selection);

        // if the selection anchor is within the selection that is removed
        // it needs to be cleared also
        Point anchorPoint = new Point(
                getSelectionAnchor().columnPosition,
                getSelectionAnchor().rowPosition);
        if (selection.contains(anchorPoint)) {
            setSelectionAnchor(NO_SELECTION, NO_SELECTION);
        }

        if (this.selectionModel.isEmpty()) {
            setLastSelectedCell(NO_SELECTION, NO_SELECTION);
        }
    }

    public void selectAll() {
        Rectangle selection = new Rectangle(0, 0, getColumnCount(), getRowCount());
        if (getLastSelectedCell().columnPosition == SelectionLayer.NO_SELECTION
                || getLastSelectedCell().rowPosition == SelectionLayer.NO_SELECTION) {
            setLastSelectedCell(0, 0);
        }
        addSelection(selection);
        fireCellSelectionEvent(
                getLastSelectedCell().columnPosition,
                getLastSelectedCell().rowPosition,
                false, false, false);
    }

    // Cell features

    public boolean isCellPositionSelected(int columnPosition, int rowPosition) {
        return this.selectionModel.isCellPositionSelected(columnPosition, rowPosition);
    }

    public void setSelectedCell(int columnPosition, int rowPosition) {
        selectCell(columnPosition, rowPosition, false, false);
    }

    /**
     * When extending a selected area we need to start adding cells from the
     * last selected cell. If we are not extending a selection we need to move
     * from the selection <i>anchor</i>.
     */
    protected PositionCoordinate getCellPositionToMoveFrom(boolean withShiftMask, boolean withControlMask) {
        return (!withShiftMask && !withControlMask) ? getSelectionAnchor() : getLastSelectedCellPosition();
    }

    public PositionCoordinate[] getSelectedCellPositions() {
        int[] selectedColumnPositions = getSelectedColumnPositions();
        Set<Range> selectedRowPositions = getSelectedRowPositions();

        List<PositionCoordinate> selectedCells = new LinkedList<PositionCoordinate>();

        for (int columnPositionIndex = 0; columnPositionIndex < selectedColumnPositions.length; columnPositionIndex++) {
            final int columnPosition = selectedColumnPositions[columnPositionIndex];

            for (Range rowIndexRange : selectedRowPositions) {
                for (int rowPositionIndex = rowIndexRange.start; rowPositionIndex < rowIndexRange.end; rowPositionIndex++) {
                    if (this.selectionModel.isCellPositionSelected(columnPosition, rowPositionIndex)) {
                        selectedCells.add(new PositionCoordinate(this, columnPosition, rowPositionIndex));
                    }
                }
            }
        }
        return selectedCells.toArray(new PositionCoordinate[0]);
    }

    /**
     * Retrieves the ILayerCells out of the SelectionLayer that are currently
     * marked as selected in the SelectionModel. Takes spanning into account.
     *
     * @return The selected ILayerCells
     */
    public Collection<ILayerCell> getSelectedCells() {
        Set<ILayerCell> selectedCells = new HashSet<ILayerCell>();

        PositionCoordinate[] selectedCoords = getSelectedCellPositions();
        for (PositionCoordinate coord : selectedCoords) {
            selectedCells.add(getCellByPosition(coord.columnPosition, coord.rowPosition));
        }

        return selectedCells;
    }

    /**
     * Calculates the selected cells - taking into account Shift and Ctrl key
     * presses.
     */
    public void selectCell(int columnPosition, int rowPosition, boolean withShiftMask, boolean withControlMask) {
        this.selectCellCommandHandler.selectCell(columnPosition, rowPosition, withShiftMask, withControlMask);
    }

    public void selectRegion(int startColumnPosition, int startRowPosition, int regionWidth, int regionHeight) {
        if (getLastSelectedRegion() == null) {
            setLastSelectedRegion(new Rectangle(
                    startColumnPosition,
                    startRowPosition,
                    regionWidth,
                    regionHeight));
        } else {
            setLastSelectedRegion(
                    startColumnPosition,
                    startRowPosition,
                    regionWidth,
                    regionHeight);
        }
        this.selectionModel.addSelection(new Rectangle(
                getLastSelectedRegion().x,
                getLastSelectedRegion().y,
                getLastSelectedRegion().width,
                getLastSelectedRegion().height));
    }

    protected void setLastSelectedRegion(Rectangle region) {
        if (this.selectionModel instanceof IMarkerSelectionModel) {
            ((IMarkerSelectionModel) this.selectionModel).setLastSelectedRegion(region);
        } else {
            // if the given region is null or the current lastSelectedRegion is
            // null, simply set the reference
            if (region == null || this.lastSelectedRegion == null) {
                this.lastSelectedRegion = region;
            } else {
                // we are modifying the values of the current lastSelectedRegion
                // instead of setting a new reference because of reference
                // issues in other places
                this.lastSelectedRegion.x = region.x;
                this.lastSelectedRegion.y = region.y;
                this.lastSelectedRegion.width = region.width;
                this.lastSelectedRegion.height = region.height;
            }
        }
    }

    protected void setLastSelectedRegion(
            int startColumnPosition, int startRowPosition,
            int regionWidth, int regionHeight) {
        if (this.selectionModel instanceof IMarkerSelectionModel) {
            ((IMarkerSelectionModel) this.selectionModel).setLastSelectedRegion(
                    startColumnPosition,
                    startRowPosition,
                    regionWidth,
                    regionHeight);
        } else {
            this.lastSelectedRegion.x = startColumnPosition;
            this.lastSelectedRegion.y = startRowPosition;
            this.lastSelectedRegion.width = regionWidth;
            this.lastSelectedRegion.height = regionHeight;
        }
    }

    // Selection anchor

    public PositionCoordinate getSelectionAnchor() {
        if (this.selectionModel instanceof IMarkerSelectionModel) {
            Point coordinate = ((IMarkerSelectionModel) this.selectionModel).getSelectionAnchor();
            return new PositionCoordinate(this, coordinate.x, coordinate.y);
        } else {
            return this.selectionAnchor;
        }
    }

    public void moveSelectionAnchor(int startColumnPositionInRegion, int startRowPosition) {
        setSelectionAnchor(startColumnPositionInRegion, startRowPosition);
    }

    void setSelectionAnchor(int columnPosition, int rowPosition) {
        if (this.selectionModel instanceof IMarkerSelectionModel) {
            ((IMarkerSelectionModel) this.selectionModel).setSelectionAnchor(
                    new Point(columnPosition, rowPosition));
        } else {
            this.selectionAnchor.columnPosition = columnPosition;
            this.selectionAnchor.rowPosition = rowPosition;
        }
    }

    // Last selected

    public PositionCoordinate getLastSelectedCellPosition() {
        PositionCoordinate coordinate = getLastSelectedCell();
        if (hasSelection(coordinate)) {
            return coordinate;
        } else {
            return null;
        }
    }

    PositionCoordinate getLastSelectedCell() {
        if (this.selectionModel instanceof IMarkerSelectionModel) {
            Point coordinate = ((IMarkerSelectionModel) this.selectionModel).getLastSelectedCell();
            return new PositionCoordinate(this, coordinate.x, coordinate.y);
        } else {
            return this.lastSelectedCell;
        }
    }

    static boolean hasSelection(PositionCoordinate coordinate) {
        return coordinate.columnPosition != NO_SELECTION
                && coordinate.rowPosition != NO_SELECTION;
    }

    public void setLastSelectedCell(int columnPosition, int rowPosition) {
        if (this.selectionModel instanceof IMarkerSelectionModel) {
            ((IMarkerSelectionModel) this.selectionModel).setLastSelectedCell(
                    new Point(columnPosition, rowPosition));
        } else {
            this.lastSelectedCell.columnPosition = columnPosition;
            this.lastSelectedCell.rowPosition = rowPosition;
        }
    }

    public Rectangle getLastSelectedRegion() {
        if (this.selectionModel instanceof IMarkerSelectionModel) {
            return ((IMarkerSelectionModel) this.selectionModel).getLastSelectedRegion();
        } else {
            return this.lastSelectedRegion;
        }
    }

    // Column features

    public boolean hasColumnSelection() {
        return getLastSelectedCell().columnPosition != NO_SELECTION;
    }

    public int[] getSelectedColumnPositions() {
        return this.selectionModel.getSelectedColumnPositions();
    }

    public boolean isColumnPositionSelected(int columnPosition) {
        return this.selectionModel.isColumnPositionSelected(columnPosition);
    }

    public int[] getFullySelectedColumnPositions() {
        return this.selectionModel.getFullySelectedColumnPositions(getRowCount());
    }

    public boolean isColumnPositionFullySelected(int columnPosition) {
        return this.selectionModel.isColumnPositionFullySelected(columnPosition, getRowCount());
    }

    public void selectColumn(
            int columnPosition, int rowPosition,
            boolean withShiftMask, boolean withControlMask) {
        this.selectColumnCommandHandler.selectColumn(
                columnPosition, rowPosition,
                withShiftMask, withControlMask);
    }

    // Row features

    public boolean hasRowSelection() {
        return getLastSelectedCell().rowPosition != NO_SELECTION;
    }

    public int getSelectedRowCount() {
        return this.selectionModel.getSelectedRowCount();
    }

    public Set<Range> getSelectedRowPositions() {
        return this.selectionModel.getSelectedRowPositions();
    }

    public boolean isRowPositionSelected(int rowPosition) {
        return this.selectionModel.isRowPositionSelected(rowPosition);
    }

    public int[] getFullySelectedRowPositions() {
        return this.selectionModel.getFullySelectedRowPositions(getColumnCount());
    }

    public boolean isRowPositionFullySelected(int rowPosition) {
        return this.selectionModel.isRowPositionFullySelected(rowPosition, getColumnCount());
    }

    public void selectRow(
            int columnPosition, int rowPosition,
            boolean withShiftMask, boolean withControlMask) {
        this.selectRowCommandHandler.selectRows(
                columnPosition,
                Arrays.asList(Integer.valueOf(rowPosition)),
                withShiftMask,
                withControlMask,
                rowPosition);
    }

    // ILayer methods

    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
        String displayMode = super.getDisplayModeByPosition(columnPosition, rowPosition);
        if (isCellPositionSelected(columnPosition, rowPosition)) {
            if (DisplayMode.HOVER.equals(displayMode)) {
                return DisplayMode.SELECT_HOVER;
            }
            return DisplayMode.SELECT;
        }
        return displayMode;
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        LabelStack labelStack = super.getConfigLabelsByPosition(columnPosition, rowPosition);

        ILayerCell cell = getCellByPosition(columnPosition, rowPosition);
        if (cell != null) {
            Rectangle cellRectangle = new Rectangle(
                    cell.getOriginColumnPosition(),
                    cell.getOriginRowPosition(),
                    cell.getColumnSpan(),
                    cell.getRowSpan());

            if (cellRectangle.contains(getSelectionAnchor().columnPosition, getSelectionAnchor().rowPosition)) {
                labelStack.addLabelOnTop(SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
            }

            if (this.bottomRightInSelection != null
                    && cellRectangle.contains(
                            this.bottomRightInSelection.columnPosition,
                            this.bottomRightInSelection.rowPosition)) {
                labelStack.addLabel(SelectionStyleLabels.FILL_HANDLE_CELL);
            }
        }

        if (this.fillHandleRegion != null
                && this.fillHandleRegion.contains(cell.getColumnIndex(), cell.getRowIndex())) {
            labelStack.addLabel(SelectionStyleLabels.FILL_HANDLE_REGION);
        }
        return labelStack;
    }

    // Command handling

    @Override
    protected void registerCommandHandlers() {
        // Command handlers also registered by the
        // DefaultSelectionLayerConfiguration
        registerCommandHandler(this.selectCellCommandHandler);
        registerCommandHandler(this.selectRowCommandHandler);
        registerCommandHandler(this.selectColumnCommandHandler);

        registerCommandHandler(new EditSelectionCommandHandler(this));
        registerCommandHandler(new InitializeAutoResizeColumnsCommandHandler(this));
        registerCommandHandler(new InitializeAutoResizeRowsCommandHandler(this));
        registerCommandHandler(new CopyDataCommandHandler(this));
        registerCommandHandler(new SearchGridCellsCommandHandler(this));
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof SelectAllCommand
                && command.convertToTargetLayer(this)) {
            selectAll();
            return true;
        } else if (command instanceof ClearAllSelectionsCommand
                && command.convertToTargetLayer(this)) {
            clear();
            return true;
        } else if (command instanceof ColumnHideCommand
                && command.convertToTargetLayer(this)) {
            return handleColumnHideCommand((ColumnHideCommand) command);
        } else if (command instanceof MultiColumnHideCommand
                && command.convertToTargetLayer(this)) {
            return handleMultiColumnHideCommand((MultiColumnHideCommand) command);
        } else if (command instanceof RowHideCommand
                && command.convertToTargetLayer(this)) {
            return handleRowHideCommand((RowHideCommand) command);
        } else if (command instanceof MultiRowHideCommand
                && command.convertToTargetLayer(this)) {
            return handleMultiRowHideCommand((MultiRowHideCommand) command);
        } else if (command instanceof ColumnResizeCommand
                && command.convertToTargetLayer(this)) {
            return handleColumnResizeCommand((ColumnResizeCommand) command);
        } else if (command instanceof RowResizeCommand
                && command.convertToTargetLayer(this)) {
            return handleRowResizeCommand((RowResizeCommand) command);
        }
        return super.doCommand(command);
    }

    public void fireCellSelectionEvent(int columnPosition, int rowPosition,
            boolean forcingEntireCellIntoViewport, boolean withShiftMask,
            boolean withControlMask) {

        final CellSelectionEvent selectionEvent =
                new CellSelectionEvent(this,
                        columnPosition,
                        rowPosition,
                        withShiftMask,
                        withControlMask);
        fireLayerEvent(selectionEvent);
    }

    // command transformations

    /**
     * Will check if there are fully selected column positions. If there is at
     * least one fully selected column position, the {@link ColumnHideCommand}
     * will be consumed and a {@link MultiColumnHideCommand} will be created and
     * executed further down the layer stack, that contains all fully selected
     * column positions. Otherwise the given command will be executed further.
     * <br>
     *
     * This is necessary because neither the ColumnHideShowLayer nor the action
     * that caused the execution of the {@link ColumnHideCommand} is aware of
     * the presence of the {@link SelectionLayer}. Without this transformation,
     * only the column on which the action was called will be hidden instead of
     * all selected ones.
     *
     * @param command
     *            The {@link ColumnHideCommand} to process
     * @return <code>true</code> if the command has been handled,
     *         <code>false</code> otherwise
     */
    protected boolean handleColumnHideCommand(ColumnHideCommand command) {
        if (isColumnPositionFullySelected(command.getColumnPosition())) {
            return handleMultiColumnHideCommand(new MultiColumnHideCommand(this, getFullySelectedColumnPositions()));
        } else {
            return super.doCommand(command);
        }
    }

    /**
     * Previous to processing the given {@link MultiColumnHideCommand} down the
     * layer stack, the fully selected column positions selection state will be
     * cleared. This is necessary so the selection also disappears for the
     * selected columns. Otherwise after hiding the selection will be showed for
     * different columns.
     *
     * @param command
     *            The {@link MultiColumnHideCommand} to process
     * @return <code>true</code> if the command has been handled,
     *         <code>false</code> otherwise
     */
    protected boolean handleMultiColumnHideCommand(MultiColumnHideCommand command) {
        for (int columnPosition : command.getColumnPositions()) {
            if (isColumnPositionFullySelected(columnPosition)) {
                Rectangle selection = new Rectangle(
                        columnPosition,
                        0,
                        1,
                        Integer.MAX_VALUE);
                clearSelection(selection);
            }
        }
        return super.doCommand(command);
    }

    /**
     * Will check if there are fully selected row positions. If there is at
     * least one fully selected row position, the {@link RowHideCommand} will be
     * consumed and a {@link MultiRowHideCommand} will be created and executed
     * further down the layer stack, that contains all fully selected row
     * positions. Otherwise the given command will be executed further.<br>
     *
     * This is necessary because neither the RowHideShowLayer nor the action
     * that caused the execution of the {@link RowHideCommand} is aware of the
     * presence of the {@link SelectionLayer}. Without this transformation, only
     * the row on which the action was called will be hidden instead of all
     * selected ones.
     *
     * @param command
     *            The {@link RowHideCommand} to process
     * @return <code>true</code> if the command has been handled,
     *         <code>false</code> otherwise
     */
    protected boolean handleRowHideCommand(RowHideCommand command) {
        if (isRowPositionFullySelected(command.getRowPosition())) {
            return handleMultiRowHideCommand(new MultiRowHideCommand(this, getFullySelectedRowPositions()));
        } else {
            return super.doCommand(command);
        }
    }

    /**
     * Previous to processing the given {@link MultiRowHideCommand} down the
     * layer stack, the fully selected row positions selection state will be
     * cleared. This is necessary so the selection also disappears for the
     * selected rows. Otherwise after hiding the selection will be showed for
     * different rows.
     *
     * @param command
     *            The {@link MultiRowHideCommand} to process
     * @return <code>true</code> if the command has been handled,
     *         <code>false</code> otherwise
     */
    protected boolean handleMultiRowHideCommand(MultiRowHideCommand command) {
        for (int rowPosition : command.getRowPositions()) {
            if (isRowPositionFullySelected(rowPosition)) {
                Rectangle selection = new Rectangle(
                        0,
                        rowPosition,
                        Integer.MAX_VALUE,
                        1);
                clearSelection(selection);
            }
        }
        return super.doCommand(command);
    }

    /**
     * Will check if there are fully selected column positions. If there is at
     * least one fully selected column position, the {@link ColumnResizeCommand}
     * will be consumed and a {@link MultiColumnResizeCommand} will be created
     * and executed further down the layer stack, that contains all fully
     * selected column positions. Otherwise the given command will be executed
     * further.<br>
     *
     * This is necessary because neither the underlying layers are not aware of
     * the presence of the {@link SelectionLayer}. Without this transformation,
     * only the column on which the action was called will be resized instead of
     * all selected ones.
     *
     * @param command
     *            The {@link ColumnResizeCommand} to process
     * @return <code>true</code> if the command has been handled,
     *         <code>false</code> otherwise
     */
    protected boolean handleColumnResizeCommand(ColumnResizeCommand command) {
        if (isColumnPositionFullySelected(command.getColumnPosition())) {
            return super.doCommand(
                    new MultiColumnResizeCommand(this,
                            this.selectionModel.getFullySelectedColumnPositions(getRowCount()),
                            command.getNewColumnWidth()));
        } else {
            return super.doCommand(command);
        }
    }

    /**
     * Will check if there are fully selected row positions. If there is at
     * least one fully selected row position, the {@link RowResizeCommand} will
     * be consumed and a {@link MultiRowResizeCommand} will be created and
     * executed further down the layer stack, that contains all fully selected
     * row positions. Otherwise the given command will be executed further.<br>
     *
     * This is necessary because neither the underlying layers are not aware of
     * the presence of the {@link SelectionLayer}. Without this transformation,
     * only the row on which the action was called will be resized instead of
     * all selected ones.
     *
     * @param command
     *            The {@link RowResizeCommand} to process
     * @return <code>true</code> if the command has been handled,
     *         <code>false</code> otherwise
     */
    protected boolean handleRowResizeCommand(RowResizeCommand command) {
        if (isRowPositionFullySelected(command.getRowPosition())) {
            return super.doCommand(
                    new MultiRowResizeCommand(this,
                            this.selectionModel.getFullySelectedRowPositions(getColumnCount()),
                            command.getNewHeight()));
        } else {
            return super.doCommand(command);
        }
    }

    /**
     * Set the region that is currently <i>selected</i> via fill handle to
     * extend the current active selection for triggering a fill action.
     *
     * @param region
     *            The region <i>selected</i> via fill handle.
     *
     * @see FillHandleDragMode
     * @see FillHandleLayerPainter
     *
     * @since 1.4
     */
    public void setFillHandleRegion(Rectangle region) {
        this.fillHandleRegion = region;
    }

    /**
     * Returns the region that is currently <i>selected</i> via fill handle to
     * extend the current active selection. Used to perform actions on drag
     * &amp; drop of the fill handle.
     *
     * @return The region <i>selected</i> via fill handle or <code>null</code>.
     *
     * @since 1.4
     */
    public Rectangle getFillHandleRegion() {
        return this.fillHandleRegion;
    }

    /**
     * Marks the bottom right cell in a contiguous selection within the
     * {@link SelectionLayer}. Also removes the markup in case there is no
     * selection or the selection is not contiguous.
     *
     * @since 1.4
     */
    public void markFillHandleCell() {
        ILayerCell bottomRight = SelectionUtils.getBottomRightCellInSelection(this);
        if (bottomRight != null) {
            this.bottomRightInSelection = new PositionCoordinate(
                    this,
                    bottomRight.getColumnPosition(),
                    bottomRight.getRowPosition());
        } else {
            this.bottomRightInSelection = null;
        }
    }

    /**
     * @since 1.4
     */
    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> labels = super.getProvidedLabels();

        labels.add(SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
        labels.add(SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE);
        labels.add(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE);
        labels.add(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE);
        labels.add(SelectionStyleLabels.FILL_HANDLE_REGION);
        labels.add(SelectionStyleLabels.FILL_HANDLE_CELL);
        labels.add(SelectionStyleLabels.COPY_BORDER_STYLE);

        return labels;
    }
}

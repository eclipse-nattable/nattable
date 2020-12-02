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
package org.eclipse.nebula.widgets.nattable.layer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.graphics.Rectangle;

/**
 * <p>
 * A Layer is a rectangular region of grid cells. A layer has methods to access
 * its columns, rows, width and height. A layer can be stacked on top of another
 * layer in order to expose a transformed view of its underlying layer's grid
 * cell structure.
 * </p>
 * <p>
 * Columns and rows in a layer are referenced either by <b>position</b> or
 * <b>index</b>. The position of a column/row in a layer corresponds to the
 * physical location of the column/row in the layer. The index of a column/row
 * in a layer corresponds to the location of the column/row in the lowest level
 * layer in the layer stack. These concepts are illustrated by the following
 * example:
 * </p>
 *
 * <pre>
 * Hide Layer C
 * 0 1 2 3 4 &lt;- column positions
 * 1 0 3 4 5 &lt;- column indexes
 *
 * Reorder Layer B
 * 0 1 2 3 4 5 &lt;- column positions
 * 2 1 0 3 4 5 &lt;- column indexes
 *
 * Data Layer A
 * 0 1 2 3 4 5 &lt;- column positions
 * 0 1 2 3 4 5 &lt;- column indexes
 * </pre>
 * <p>
 * In the above example, Hide Layer C is stacked on top of Reorder Layer B,
 * which is in turn stacked on top of Data Layer A. The positions in Data Layer
 * A are the same as its indexes, because it is the lowest level layer in the
 * stack. Reorder Layer B reorders column 0 of its underlying layer (Data Layer
 * A) after column 2 of its underlying layer. Hide Layer C hides the first
 * column of its underlying layer (Reorder Layer B).
 * </p>
 * <p>
 * Layers can also be laterally composed into larger layers. For instance, the
 * standard grid layer is composed of a body layer, column header layer, row
 * header layer, and corner layer:
 * </p>
 * <table border=1>
 * <caption>layer composition</caption>
 * <tr>
 * <td>corner</td>
 * <td>column header</td>
 * </tr>
 * <tr>
 * <td>row header</td>
 * <td>body</td>
 * </tr>
 * </table>
 *
 * @see CompositeLayer
 */
public interface ILayer extends ILayerListener, IPersistable {

    // Dispose

    /**
     * Dispose any resource allocated by this layer.
     */
    public void dispose();

    // Persistence

    /**
     * Register an {@link IPersistable} that can write its state to the state
     * {@link Properties} instance when the layer is persisted.
     *
     * @param persistable
     *            The persistable that should be registered.
     */
    public void registerPersistable(IPersistable persistable);

    /**
     * Unregister the given {@link IPersistable}.
     *
     * @param persistable
     *            The persistable to unregister.
     */
    public void unregisterPersistable(IPersistable persistable);

    // Configuration

    /**
     * Configure this layer, e.g. add any key/mouse bindings and other general
     * configuration.
     * <p>
     * This method is triggered by {@link NatTable#configure()} and executed
     * down the layer stack.
     * </p>
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} instance owned by the
     *            {@link NatTable} this layer is attached to.
     * @param uiBindingRegistry
     *            The {@link UiBindingRegistry} instance owned by
     *            {@link NatTable} this layer is attached to.
     *
     * @since 2.0
     */
    public void configure(IConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry);

    // Region

    /**
     * Return the {@link LabelStack} containing the region labels for the cell
     * at the given pixel position.
     *
     * @param x
     *            the x pixel coordinate
     * @param y
     *            the y pixel coordinate
     * @return LabelStack containing the region labels for the cell at the given
     *         pixel position.
     */
    public LabelStack getRegionLabelsByXY(int x, int y);

    // Commands

    /**
     * Opportunity to respond to a command as it flows down the stack. If the
     * layer is not interested in the command it should allow the command to
     * keep traveling down the stack.
     * <p>
     * <b>Note:</b> Before the layer can process a command it <i>must</i>
     * convert the command to its local coordinates using
     * {@link ILayerCommand#convertToTargetLayer(ILayer)}
     *
     * @param command
     *            The command to execute.
     * @return <code>true</code> if the command has been handled and was
     *         therefore consumed, <code>false</code> otherwise.
     */
    public boolean doCommand(ILayerCommand command);

    /**
     * Register an {@link ILayerCommandHandler} to handle a command in this
     * layer. Only one {@link ILayerCommandHandler} per {@link ILayerCommand}
     * can be registered per layer.
     *
     * @param commandHandler
     *            The command handler to register with this layer.
     */
    public void registerCommandHandler(ILayerCommandHandler<?> commandHandler);

    /**
     * Unregister the {@link ILayerCommandHandler} that is registered for the
     * given {@link ILayerCommand} class.
     *
     * @param commandClass
     *            The {@link ILayerCommand} class for which the
     *            {@link ILayerCommandHandler} should be unregistered.
     */
    public void unregisterCommandHandler(Class<? extends ILayerCommand> commandClass);

    // Events

    /**
     * Events can be fired to notify other components of the grid. Events travel
     * <i>up</i> the layer stack and may cause a repaint.
     * <p>
     * Example: When the contents of the grid change {@link IVisualChangeEvent}
     * can be fired to notify other layers to refresh their caches etc.
     *
     * @param event
     *            the event to fire
     */
    public void fireLayerEvent(ILayerEvent event);

    /**
     * Add a general {@link ILayerListener} to handle {@link ILayerEvent}s on
     * this layer.
     *
     * @param listener
     *            The {@link ILayerListener} to add.
     */
    public void addLayerListener(ILayerListener listener);

    /**
     * Remove the given {@link ILayerListener} from this layer.
     *
     * @param listener
     *            The {@link ILayerListener} to remove.
     */
    public void removeLayerListener(ILayerListener listener);

    /**
     * Check if an {@link ILayerListener} of the given type is registered on
     * this layer or not.
     *
     * @param layerListenerClass
     *            The type of {@link ILayerListener} to check for.
     * @return <code>true</code> if this {@link ILayer} has a
     *         {@link ILayerListener} of the specified type registered,
     *         <code>false</code> if there is no such listener registered.
     */
    public boolean hasLayerListener(Class<? extends ILayerListener> layerListenerClass);

    /**
     *
     * @return The {@link ILayerPainter} used to render this layer.
     */
    public ILayerPainter getLayerPainter();

    // Client area

    /**
     *
     * @return The {@link IClientAreaProvider} that specifies the rectangular
     *         area available on this layer.
     */
    public IClientAreaProvider getClientAreaProvider();

    /**
     *
     * @param clientAreaProvider
     *            The {@link IClientAreaProvider} that specifies the rectangular
     *            area available on this layer.
     */
    public void setClientAreaProvider(IClientAreaProvider clientAreaProvider);

    // Horizontal features

    // Columns

    /**
     * @return The number of columns in this layer.
     */
    public int getColumnCount();

    public int getPreferredColumnCount();

    /**
     * Gets the underlying non-transformed column index for the given column
     * position on this layer.
     *
     * @param columnPosition
     *            The column position relative to this layer.
     * @return An underlying non-transformed column index, or -1 if the given
     *         column position does not exist within this coordinate system.
     */
    public int getColumnIndexByPosition(int columnPosition);

    /**
     * Convert a column position to the coordinates of the underlying layer.
     * This is possible since each layer is aware of its underlying layer.
     *
     * @param localColumnPosition
     *            column position in local (the layer's own) coordinates
     * @return column position in the underlying layer's coordinates
     */
    public int localToUnderlyingColumnPosition(int localColumnPosition);

    /**
     * Transforms the column position relative to the given underlying layer to
     * this layer coordinates.
     *
     * @param sourceUnderlyingLayer
     *            The underlying layer to which the given column position
     *            matches.
     * @param underlyingColumnPosition
     *            The column position in the given underlying layer that should
     *            be converted to a local column position.
     * @return The given column position transformed to be local to this layer.
     */
    public int underlyingToLocalColumnPosition(
            ILayer sourceUnderlyingLayer,
            int underlyingColumnPosition);

    /**
     * Transforms the column position ranges relative to the given underlying
     * layer to this layer coordinates.
     *
     * @param sourceUnderlyingLayer
     *            The underlying layer to which the given column positions
     *            match.
     * @param underlyingColumnPositionRanges
     *            The column position ranges relative to the given underlying
     *            layer that should be converted to local column positions.
     * @return The given column position ranges transformed to this layer.
     */
    public Collection<Range> underlyingToLocalColumnPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingColumnPositionRanges);

    // Width

    /**
     * Returns the total width in pixels of this layer.
     *
     * @return The total width in pixels of this layer.
     */
    public int getWidth();

    public int getPreferredWidth();

    /**
     * Returns the width in pixels of the given column.
     *
     * The width of invisible and non-existing columns is 0.
     *
     * @param columnPosition
     *            The column position in this layer.
     *
     * @return The width of the column.
     */
    public int getColumnWidthByPosition(int columnPosition);

    // Column resize

    /**
     * Check if the column at the given position is resizable.
     *
     * @param columnPosition
     *            The column position to check.
     * @return <code>true</code> if the column is resizable, <code>false</code>
     *         if not.
     */
    public boolean isColumnPositionResizable(int columnPosition);

    // X

    /**
     * Returns the column position that contains the given x coordinate.
     *
     * @param x
     *            A horizontal pixel location relative to the pixel boundary of
     *            this layer.
     * @return A column position relative to the associated coordinate system,
     *         or -1 if there is no column that contains x.
     */
    public int getColumnPositionByX(int x);

    /**
     * Returns the x offset in pixels of the given column.
     *
     * @param columnPosition
     *            The column position in this layer.
     * @return The x offset of the column, or -1.
     */
    public int getStartXOfColumnPosition(int columnPosition);

    // Underlying

    /**
     * Returns the layers that are directly below this layer for the given
     * column position. For simple layers this collection will typically only
     * have one entry. Layer compositions might return multiple values, e.g. in
     * a default grid there will be 2 layers in the collection as there are two
     * layers involved in a column.
     *
     * @param columnPosition
     *            The column position for which the underlying layers are
     *            requested.
     * @return The layers that are directly below this layer for the given
     *         column position or <code>null</code> if this layer has no
     *         underlying layers.
     */
    public Collection<ILayer> getUnderlyingLayersByColumnPosition(int columnPosition);

    // Vertical features

    // Rows

    /**
     * @return The number of rows in this layer.
     */
    public int getRowCount();

    public int getPreferredRowCount();

    /**
     * Gets the underlying non-transformed row index for the given row position
     * on this layer.
     *
     * @param rowPosition
     *            The row position relative to this layer.
     * @return An underlying non-transformed row index, or -1 if the given row
     *         position does not exist within this coordinate system.
     */
    public int getRowIndexByPosition(int rowPosition);

    /**
     * Convert a row position to the coordinates of the underlying layer. This
     * is possible since each layer is aware of its underlying layer.
     *
     * @param localRowPosition
     *            row position in local (the layer's own) coordinates
     * @return row position in the underlying layer's coordinates
     */
    public int localToUnderlyingRowPosition(int localRowPosition);

    /**
     * Transforms the row position relative to the given underlying layer to
     * this layer coordinates.
     *
     * @param sourceUnderlyingLayer
     *            The underlying layer to which the given row position matches.
     * @param underlyingRowPosition
     *            The row position in the given underlying layer that should be
     *            converted to a local row position.
     * @return The given row position transformed to be local to this layer.
     */
    public int underlyingToLocalRowPosition(
            ILayer sourceUnderlyingLayer,
            int underlyingRowPosition);

    /**
     * Transforms the row position ranges relative to the given underlying layer
     * to this layer coordinates.
     *
     * @param sourceUnderlyingLayer
     *            The underlying layer to which the given row positions match.
     * @param underlyingRowPositionRanges
     *            The row position ranges relative to the given underlying layer
     *            that should be converted to local row positions.
     * @return The given row position ranges transformed to this layer.
     */
    public Collection<Range> underlyingToLocalRowPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingRowPositionRanges);

    // Height

    /**
     * Returns the total height in pixels of this layer.
     *
     * @return The total height in pixels of this layer.
     */
    public int getHeight();

    public int getPreferredHeight();

    /**
     * Returns the height in pixels of the given row.
     *
     * The height of invisible and non-existing rows is 0.
     *
     * @param rowPosition
     *            The row position in this layer.
     *
     * @return The height of the row.
     */
    public int getRowHeightByPosition(int rowPosition);

    // Row resize

    /**
     * Check if the row at the given position is resizable.
     *
     * @param rowPosition
     *            The row position to check.
     * @return <code>true</code> if the row is resizable, <code>false</code> if
     *         not.
     */
    public boolean isRowPositionResizable(int rowPosition);

    // Y

    /**
     * Returns the row position that contains the given y coordinate.
     *
     * @param y
     *            a vertical pixel location relative to the pixel boundary of
     *            this layer
     * @return a row position relative to the associated coordinate system, or
     *         -1 if there is no row that contains y
     */
    public int getRowPositionByY(int y);

    /**
     * Returns the y offset in pixels of the given row.
     *
     * @param rowPosition
     *            the row position in this layer
     *
     * @return the y offset of the row, or -1
     */
    public int getStartYOfRowPosition(int rowPosition);

    // Underlying

    /**
     * Returns the layers that are directly below this layer for the given row
     * position. For simple layers this collection will typically only have one
     * entry. Layer compositions might return multiple values, e.g. in a default
     * grid there will be 2 layers in the collection as there are two layers
     * involved in a row.
     *
     * @param rowPosition
     *            The row position for which the underlying layers are
     *            requested.
     * @return The layers that are directly below this layer for the given row
     *         position or <code>null</code> if this layer has no underlying
     *         layers.
     */
    public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition);

    // Cell features

    /**
     * Returns the cell for the given coordinates on this layer.
     *
     * @param columnPosition
     *            The column position of the requested cell.
     * @param rowPosition
     *            The row position of the requested cell.
     * @return The {@link ILayerCell} for the given coordinates in this layer or
     *         <code>null</code> if the coordinates are invalid on this layer.
     */
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition);

    /**
     * Calculates the bounds in pixel for the given cell position.
     *
     * @param columnPosition
     *            the column position of the cell
     * @param rowPosition
     *            the row position of the cell
     *
     * @return the bounds, or <code>null</code> if there are no valid bounds
     */
    public Rectangle getBoundsByPosition(int columnPosition, int rowPosition);

    /**
     * Returns the active {@link DisplayMode} for the cell at the given
     * coordinates. Needed to retrieve the corresponding configurations out of
     * the {@link IConfigRegistry}. The default value is
     * {@link DisplayMode#NORMAL}. The SelectionLayer for example overrides this
     * to return {@link DisplayMode#SELECT} for cells that are currently
     * selected.
     *
     * @param columnPosition
     *            The column position of the cell.
     * @param rowPosition
     *            The row position of the cell.
     * @return {@link DisplayMode} for the cell at the given coordinates.
     */
    public String getDisplayModeByPosition(int columnPosition, int rowPosition);

    /**
     * Returns the config labels for the cell at the given coordinates. Needed
     * to retrieve the corresponding configurations out of the
     * {@link IConfigRegistry}.
     *
     * @param columnPosition
     *            The column position of the cell.
     * @param rowPosition
     *            The row position of the cell.
     * @return The {@link LabelStack} with the config labels for the cell at the
     *         given coordinates.
     */
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition);

    /**
     * Returns the data value for the cell at the given coordinates.
     *
     * @param columnPosition
     *            The column position of the cell.
     * @param rowPosition
     *            The row position of the cell.
     * @return The data value for the cell at the given coordinates.
     */
    public Object getDataValueByPosition(int columnPosition, int rowPosition);

    /**
     * Returns the layer that is directly below this layer for the given cell
     * coordinate.
     *
     * @param columnPosition
     *            The column position for which the underlying layer is
     *            requested.
     * @param rowPosition
     *            The row position for which the underlying layer is requested.
     * @return The layer that is directly below this layer for the given cell
     *         coordinates or <code>null</code> if this layer has no underlying
     *         layers.
     */
    public ILayer getUnderlyingLayerByPosition(int columnPosition, int rowPosition);

    /**
     * Return the {@link ICellPainter} for the given {@link ILayerCell} at the
     * given coordinates out of the given {@link IConfigRegistry}.
     *
     * @param columnPosition
     *            The column position of the cell.
     * @param rowPosition
     *            The row position of the cell.
     * @param cell
     *            The {@link ILayerCell} for which the {@link ICellPainter} is
     *            requested.
     * @param configRegistry
     *            The {@link IConfigRegistry} to retrieve the painter from.
     * @return The {@link ICellPainter} for the given cell at the given
     *         coordinates or <code>null</code> if no painter is configured.
     */
    public ICellPainter getCellPainter(
            int columnPosition,
            int rowPosition,
            ILayerCell cell,
            IConfigRegistry configRegistry);

    /**
     * @return <code>true</code> if the layer has a dynamic size (e.g. viewport)
     *         or a fixed size.
     * @since 2.0
     */
    public default boolean isDynamicSizeLayer() {
        return false;
    }

    /**
     * @return The collection of labels that are provided by this layer used
     *         e.g. for CSS styling.
     * @since 2.0
     */
    public default Collection<String> getProvidedLabels() {
        return new LinkedHashSet<>();
    }

}

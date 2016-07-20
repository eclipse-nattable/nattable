/*******************************************************************************
 * Copyright (c) 2013, 2016 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.editor.TableCellEditor;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * ICellPainter that renders a data collection as sub cells within a NatTable
 * cell. It uses an internal ICellPainter that can be set to render the contents
 * of the sub cells and inserts grid lines between the elements in the list.
 * <p>
 * Note:
 * <ul>
 * <li>As this painter simulates but is not really an internal table, on
 * selection the whole internal table will get selected.</li>
 * <li>As the internal table structure that is rendered by this painter is not
 * related to NatTable table structure, the NatTable configuration mechanism
 * doesn'apply to the internal sub cells differently. Instead the same
 * configuration will be applied to all sub cells.</li>
 * </ul>
 *
 * <p>
 * This painter is intended for an editable NatTable in combination with the
 * TableCellEditor.
 *
 * @see TableCellEditor
 */
public class TableCellPainter extends BackgroundPainter {

    /**
     * The ICellPainter that should be used to render the internal sub cells.
     */
    private final ICellPainter internalPainter;
    /**
     * The color that should be used to render the grid lines in the sub table
     * in {@link DisplayMode#NORMAL}
     */
    private Color gridColor;
    /**
     * The color that should be used to render the grid lines in the sub table
     * in {@link DisplayMode#SELECT}
     */
    private Color selectedGridColor;
    /**
     * The height of the sub cells to use. Setting a value >= 0 will result in
     * using the specified fixed sub cell heights, a negative value will result
     * in dynamically calculated sub cell heights dependent on the content.
     */
    private int fixedSubCellHeight;
    /**
     * Flag to configure if this painter shall resize the row height of the
     * parent cell so it can show all available data in the internal table or
     * not. Default is <code>true</code>.
     */
    private boolean calculateParentCellHeight = true;
    /**
     * Flag to configure whether this painter should render the background or
     * not.
     */
    private boolean paintBg = true;

    /**
     * Creates a TableCellPainter that uses the following default settings:
     * <ul>
     * <li>internal painter = TextPainter</li>
     * <li>grid color = gray</li>
     * <li>selected grid color = white</li>
     * <li>fixed sub cell row height = 20</li>
     * <li>calculate parent cell height = true</li>
     * </ul>
     * Despite the internal painter you can change these settings after creation
     * with the corresponding setters. If you want to use a different internal
     * painter you should use the corresponding constructor. Changing the
     * internal painter at runtime is not intended.
     */
    public TableCellPainter() {
        this(new TextPainter());
    }

    /**
     * Creates a TableCellPainter that uses the given ICellPainter as internal
     * painter for the sub cells created and rendered by this painter.
     * <p>
     * Will use the following default settings:
     * <ul>
     * <li>grid color = gray</li>
     * <li>selected grid color = white</li>
     * <li>fixed sub cell row height = 20</li>
     * <li>calculate parent cell height = true</li>
     * </ul>
     * You can change these settings after creation with the corresponding
     * setters.
     *
     * @param internalPainter
     *            The ICellPainter that should be used to render the internal
     *            sub cells.
     */
    public TableCellPainter(ICellPainter internalPainter) {
        this(internalPainter, GUIHelper.COLOR_GRAY, GUIHelper.COLOR_WHITE, 20, true);
    }

    /**
     * Creates a TableCellPainter that uses the given values for configuration.
     *
     * @param internalPainter
     *            The ICellPainter that should be used to render the internal
     *            sub cells.
     * @param gridColor
     *            The color that should be used to render the grid lines in the
     *            sub table in {@link DisplayMode#NORMAL}
     * @param selectedGridColor
     *            The color that should be used to render the grid lines in the
     *            sub table in {@link DisplayMode#SELECT}
     * @param fixedSubCellHeight
     *            The height of the sub cells to use. Setting a value &gt;= 0
     *            will result in using the specified fixed sub cell heights, a
     *            negative value will result in dynamically calculated sub cell
     *            heights dependent on the content.
     * @param calculateParentCellHeight
     *            Whether this painter shall resize the row height of the parent
     *            cell to show all available data in the internal table or not.
     */
    public TableCellPainter(
            ICellPainter internalPainter,
            Color gridColor,
            Color selectedGridColor,
            int fixedSubCellHeight,
            boolean calculateParentCellHeight) {
        this.internalPainter = internalPainter;
        this.setGridColor(gridColor);
        this.setSelectedGridColor(selectedGridColor);
        this.setFixedSubCellHeight(fixedSubCellHeight);
        this.setCalculateParentCellHeight(calculateParentCellHeight);
    }

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {

        if (this.paintBg) {
            super.paintCell(cell, gc, bounds, configRegistry);
        }

        Object[] cellDataArray = getDataAsArray(cell);
        if (cellDataArray != null) {
            // iterate over all values in the collection and render them
            // separately by creating temporary sub cells
            // and adding grid lines for the sub cells
            int subGridY = bounds.y;

            if (cellDataArray != null) {
                Color originalColor = gc.getForeground();

                for (int i = 0; i < cellDataArray.length; i++) {
                    ILayerCell subCell = createSubLayerCell(cell, cellDataArray[i]);

                    int subCellHeight = getSubCellHeight(subCell, gc, configRegistry);
                    Rectangle subCellBounds = new Rectangle(
                            bounds.x,
                            subGridY,
                            bounds.width,
                            subCellHeight);

                    this.getInternalPainter().paintCell(subCell, gc, subCellBounds, configRegistry);

                    // render sub grid line
                    // update subGridY for calculated height
                    subGridY += subCellHeight + 1;
                    gc.setForeground(cell.getDisplayMode().equals(DisplayMode.SELECT)
                            ? getSelectedGridColor() : getGridColor());
                    gc.drawLine(bounds.x, subGridY, bounds.x + bounds.width, subGridY);
                    gc.setForeground(originalColor);
                    // increase subGridY by 1 so the next sub cell renders below
                    subGridY += 1;
                }

                // perform resize if necessary
                int neededHeight = subGridY - bounds.y;
                if (isCalculateParentCellHeight()
                        && (neededHeight > cell.getBounds().height)) {
                    ILayer layer = cell.getLayer();
                    layer.doCommand(new RowResizeCommand(layer, cell.getRowPosition(), neededHeight));
                }
            }
        } else {
            this.getInternalPainter().paintCell(cell, gc, bounds, configRegistry);
        }
    }

    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        Object[] cellDataArray = getDataAsArray(cell);
        if (cellDataArray != null) {
            int width = 0;
            for (Object data : cellDataArray) {
                ILayerCell subCell = createSubLayerCell(cell, data);
                width = Math.max(
                        width,
                        this.getInternalPainter().getPreferredWidth(subCell, gc, configRegistry));
            }
            return width;
        }
        return this.getInternalPainter().getPreferredWidth(cell, gc, configRegistry);
    }

    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        Object[] cellDataArray = getDataAsArray(cell);
        if (cellDataArray != null) {
            int height = 0;
            for (Object data : cellDataArray) {
                ILayerCell subCell = createSubLayerCell(cell, data);
                height += this.getInternalPainter().getPreferredHeight(subCell, gc, configRegistry);
            }
            // add number of items-1 to calculate the pixels for grid lines
            height += cellDataArray.length;
            return height;
        }
        return this.getInternalPainter().getPreferredHeight(cell, gc, configRegistry);
    }

    /**
     * Checks if the data value of the given cell is of type Collection or
     * Array. Will return the Collection or Array as Object[] or
     * <code>null</code> if the data value is not a Collection or Array.
     *
     * @param cell
     *            The cell that should be checked for its data.
     * @return The Object[] representation of the data value if it is of type
     *         Collection or Array, or <code>null</code> if the data value is
     *         not a Collection or Array.
     */
    protected Object[] getDataAsArray(ILayerCell cell) {
        Object cellData = cell.getDataValue();
        Object[] cellDataArray = null;
        if (cellData.getClass().isArray()) {
            cellDataArray = (Object[]) cellData;
        } else if (cellData instanceof Collection) {
            Collection<?> cellDataCollection = (Collection<?>) cellData;
            cellDataArray = cellDataCollection.toArray();
        }

        return cellDataArray;
    }

    /**
     * Creating a temporary sub cell that represents one data value in the
     * collection of data to be shown in the cell. This is then used to get the
     * size and paint the cell using the underlying painter.
     *
     * @param cell
     *            The parent cell for which the sub cell should be created
     * @param dataValue
     *            The data value that should be contained in the sub cell
     * @return A temporary sub cell of the given parent cell used to paint and
     *         calculate inner cells.
     */
    protected ILayerCell createSubLayerCell(final ILayerCell cell, final Object dataValue) {
        LayerCell subCell = new LayerCell(cell.getLayer(),
                cell.getOriginColumnPosition(), cell.getOriginRowPosition(),
                // no spanning
                cell.getColumnPosition(), cell.getRowPosition(), 0, 0) {

            @Override
            public Object getDataValue() {
                // the new sub cell should only return the sub data instead of
                // asking the layers for it
                return dataValue;
            }
        };

        return subCell;
    }

    /**
     * Get the height for the sub cell.
     *
     * @param subCell
     *            The temporary sub cell that is used to ask the internal
     *            painter for rendering
     * @param gc
     *            The GC that is used for rendering
     * @param configRegistry
     *            The ConfigRegistry that contains the configurations of the
     *            current NatTable
     * @return The height for the sub cell dependent on the fixedSubCellHeight
     *         configuration
     */
    protected int getSubCellHeight(ILayerCell subCell, GC gc, IConfigRegistry configRegistry) {
        return (this.fixedSubCellHeight >= 0)
                ? this.fixedSubCellHeight : this.getInternalPainter().getPreferredHeight(subCell, gc, configRegistry);
    }

    /**
     * This getter is introduced to allow overriding in subclasses to add
     * support for mixed internal painters, like for example different painters
     * dependent on the data type.
     * <p>
     * Note: As the internal table structure that is rendered by this painter is
     * not related to NatTable table structure, the NatTable configuration
     * mechanism doesn'apply to the internal sub cells differently. Instead the
     * same configuration will be applied to all sub cells.
     *
     * @return The ICellPainter that should be used to render the internal sub
     *         cells.
     */
    protected ICellPainter getInternalPainter() {
        return this.internalPainter;
    }

    /**
     * @return The color that is used to render the grid lines in the sub table
     *         in {@link DisplayMode#NORMAL}
     */
    public Color getGridColor() {
        return this.gridColor;
    }

    /**
     * @param gridColor
     *            The color that should be used to render the grid lines in the
     *            sub table in {@link DisplayMode#NORMAL}
     */
    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    /**
     * @return The color that is used to render the grid lines in the sub table
     *         in {@link DisplayMode#SELECT}
     */
    public Color getSelectedGridColor() {
        return this.selectedGridColor;
    }

    /**
     * @param selectedGridColor
     *            The color that should be used to render the grid lines in the
     *            sub table in {@link DisplayMode#SELECT}
     */
    public void setSelectedGridColor(Color selectedGridColor) {
        this.selectedGridColor = selectedGridColor;
    }

    /**
     * @return The height of the sub cells to use. A value &gt;= 0 results in
     *         using the specified fixed sub cell heights, a negative value
     *         results in dynamically calculated sub cell heights dependent on
     *         the content.
     */
    public int getFixedSubCellHeight() {
        return this.fixedSubCellHeight;
    }

    /**
     * Setting a value &gt;= 0 will result in using a fixed height of the sub
     * cells. Setting the value to a negative number will ask the internal
     * painter for the sub cells to calculate the height regarding the content.
     *
     * @param fixedSubCellHeight
     *            The height of the sub cells to use.
     */
    public void setFixedSubCellHeight(int fixedSubCellHeight) {
        this.fixedSubCellHeight = fixedSubCellHeight;
    }

    /**
     * @return <code>true</code> if this painter resizes the row height of the
     *         parent cell to show all available data in the internal table,
     *         <code>false</code> if not.
     */
    public boolean isCalculateParentCellHeight() {
        return this.calculateParentCellHeight;
    }

    /**
     * @param calculateParentCellHeight
     *            Whether this painter shall resize the row height of the parent
     *            cell to show all available data in the internal table or not.
     */
    public void setCalculateParentCellHeight(boolean calculateParentCellHeight) {
        this.calculateParentCellHeight = calculateParentCellHeight;
    }

    /**
     * @param paintBg
     *            <code>true</code> to paint the background, <code>false</code>
     *            if not. <code>true</code> is default
     * @since 1.4
     */
    public void setPaintBg(boolean paintBg) {
        this.paintBg = paintBg;
    }
}

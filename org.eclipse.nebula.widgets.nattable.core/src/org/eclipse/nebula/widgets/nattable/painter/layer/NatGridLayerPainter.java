/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Added scaling
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.layer;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.resize.command.RowSizeConfigurationCommand;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Specialization of NatLayerPainter that fills the background with grid lines
 * to create the same look and feel as native table controls. It is possible to
 * specify the grid line color directly or via ConfigRegistry
 * {@link CellConfigAttributes#GRID_LINE_COLOR}, where the ConfigRegistry entry
 * will win over direct configuration.
 * <p>
 * If there should be several fake rows rendered, you need to set the default
 * row height that should be used for rendering the fake row lines. Otherwise
 * this will be skipped and only the column lines will be rendered to the
 * bottom.
 * </p>
 */
public class NatGridLayerPainter extends NatLayerPainter implements ILayerCommandHandler<RowSizeConfigurationCommand> {

    private final Color gridColor;

    private int defaultRowHeight = 0;

    /**
     * @param natTable
     *            The NatTable instance for which the NatGridLayerPainter should
     *            render the background.
     */
    public NatGridLayerPainter(NatTable natTable) {
        this(natTable, GUIHelper.COLOR_GRAY);
    }

    /**
     * @param natTable
     *            The NatTable instance for which the NatGridLayerPainter should
     *            render the background.
     * @param gridColor
     *            The Color that should be used to render the grid lines. Note
     *            that an entry for {@link CellConfigAttributes#GRID_LINE_COLOR}
     *            will override this value at runtime.
     */
    public NatGridLayerPainter(NatTable natTable, Color gridColor) {
        // as we don't render row lines, there is no need to register the
        // command handler
        this(natTable, gridColor, 0, false);
    }

    /**
     * @param natTable
     *            The NatTable instance for which the NatGridLayerPainter should
     *            render the background.
     * @param defaultRowHeight
     *            The row height that should be used to render fake rows to the
     *            bottom. Setting a value of 0 will avoid rendering fake row
     *            lines.
     */
    public NatGridLayerPainter(NatTable natTable, int defaultRowHeight) {
        this(natTable, GUIHelper.COLOR_GRAY, defaultRowHeight, true);
    }

    /**
     * @param natTable
     *            The NatTable instance for which the NatGridLayerPainter should
     *            render the background.
     * @param defaultRowHeight
     *            The row height that should be used to render fake rows to the
     *            bottom. Setting a value of 0 will avoid rendering fake row
     *            lines.
     * @param registerCommandHandler
     *            If set to <code>true</code> this class registers as
     *            {@link ILayerCommandHandler} for the
     *            {@link RowSizeConfigurationCommand} on the NatTable instance.
     * @since 2.3
     */
    public NatGridLayerPainter(NatTable natTable, int defaultRowHeight, boolean registerCommandHandler) {
        this(natTable, GUIHelper.COLOR_GRAY, defaultRowHeight, registerCommandHandler);
    }

    /**
     * @param natTable
     *            The NatTable instance for which the NatGridLayerPainter should
     *            render the background.
     * @param gridColor
     *            The Color that should be used to render the grid lines. Note
     *            that an entry for {@link CellConfigAttributes#GRID_LINE_COLOR}
     *            will override this value at runtime.
     * @param defaultRowHeight
     *            The row height that should be used to render fake rows to the
     *            bottom. Setting a value of 0 will avoid rendering fake row
     *            lines.
     */
    public NatGridLayerPainter(NatTable natTable, Color gridColor, int defaultRowHeight) {
        this(natTable, gridColor, defaultRowHeight, true);
    }

    /**
     * @param natTable
     *            The NatTable instance for which the NatGridLayerPainter should
     *            render the background.
     * @param gridColor
     *            The Color that should be used to render the grid lines. Note
     *            that an entry for {@link CellConfigAttributes#GRID_LINE_COLOR}
     *            will override this value at runtime.
     * @param defaultRowHeight
     *            The row height that should be used to render fake rows to the
     *            bottom. Setting a value of 0 will avoid rendering fake row
     *            lines.
     * @param registerCommandHandler
     *            If set to <code>true</code> this class registers as
     *            {@link ILayerCommandHandler} for the
     *            {@link RowSizeConfigurationCommand} on the NatTable instance.
     * @since 2.3
     */
    public NatGridLayerPainter(NatTable natTable, Color gridColor, int defaultRowHeight, boolean registerCommandHandler) {
        super(natTable);
        this.gridColor = gridColor;
        setDefaultRowHeight(defaultRowHeight);

        if (registerCommandHandler) {
            registerCommandHandler();
        }
    }

    @Override
    protected void paintBackground(
            ILayer natLayer,
            GC gc,
            int xOffset,
            int yOffset,
            Rectangle rectangle,
            IConfigRegistry configRegistry) {

        super.paintBackground(natLayer, gc, xOffset, yOffset, rectangle, configRegistry);

        Color gColor = configRegistry.getConfigAttribute(
                CellConfigAttributes.GRID_LINE_COLOR,
                DisplayMode.NORMAL);
        gc.setForeground(gColor != null ? gColor : this.gridColor);

        drawHorizontalLines(natLayer, gc, rectangle);
        drawVerticalLines(natLayer, gc, rectangle);
    }

    private void drawHorizontalLines(ILayer natLayer, GC gc, Rectangle rectangle) {
        int endX = rectangle.x + rectangle.width;

        int rowPositionByY = natLayer.getRowPositionByY(rectangle.y + rectangle.height);
        int maxRowPosition = rowPositionByY > 0
                ? Math.min(natLayer.getRowCount(), rowPositionByY)
                : natLayer.getRowCount();

        int y = 0;
        for (int rowPosition = natLayer.getRowPositionByY(rectangle.y); rowPosition < maxRowPosition; rowPosition++) {
            y = natLayer.getStartYOfRowPosition(rowPosition) + natLayer.getRowHeightByPosition(rowPosition) - 1;
            gc.drawLine(rectangle.x, y, endX, y);
        }

        // render fake row lines to the bottom
        int rowHeight = getDefaultRowHeight();
        if (rowHeight > 0) {
            int endY = rectangle.y + rectangle.height;
            while (y < endY) {
                y += rowHeight;
                gc.drawLine(rectangle.x, y, endX, y);
            }
        }
    }

    private void drawVerticalLines(ILayer natLayer, GC gc, Rectangle rectangle) {
        int endY = rectangle.y + rectangle.height;

        int columnPositionByX = natLayer.getColumnPositionByX(rectangle.x + rectangle.width);
        int maxColumnPosition = columnPositionByX > 0
                ? Math.min(natLayer.getColumnCount(), columnPositionByX)
                : natLayer.getColumnCount();
        for (int columnPosition = natLayer.getColumnPositionByX(rectangle.x); columnPosition < maxColumnPosition; columnPosition++) {
            int x = natLayer.getStartXOfColumnPosition(columnPosition) + natLayer.getColumnWidthByPosition(columnPosition) - 1;
            gc.drawLine(x, rectangle.y, x, endY);
        }
    }

    /**
     *
     * @return The currently configured unscaled height that is used to render
     *         fake rows.
     * @since 2.3
     */
    public int getConfiguredDefaultRowHeight() {
        return this.defaultRowHeight;
    }

    /**
     *
     * @return The currently used height that is used to render fake rows scaled
     *         to match the NatTable scaling.
     */
    public int getDefaultRowHeight() {
        return GUIHelper.convertVerticalPixelToDpi(this.defaultRowHeight, this.natTable.getConfigRegistry());
    }

    /**
     *
     * @param defaultRowHeight
     *            The value that should be used to render fake rows. The value
     *            needs to be given in pixels, as the scaling calculation is
     *            done on rendering.
     */
    public void setDefaultRowHeight(int defaultRowHeight) {
        this.defaultRowHeight = defaultRowHeight;
    }

    /**
     * Register ourself as {@link ILayerCommandHandler} for the
     * {@link RowSizeConfigurationCommand} on the NatTable instance. This
     * enables automatic updates of the {@link #defaultRowHeight} if it is
     * updated via {@link RowSizeConfigurationCommand}.
     *
     * @since 2.3
     */
    public void registerCommandHandler() {
        this.natTable.registerCommandHandler(this);
    }

    /**
     * Unregister ourself as {@link ILayerCommandHandler} for the
     * {@link RowSizeConfigurationCommand} from the NatTable instance.
     *
     * @since 2.3
     */
    public void unregisterCommandHandler() {
        this.natTable.unregisterCommandHandler(getCommandClass());
    }

    /**
     * @since 2.3
     */
    @Override
    public boolean doCommand(ILayer targetLayer, RowSizeConfigurationCommand command) {
        // update the default row height if the command is used to update the
        // default row height in general
        if (command.label == null && command.newRowHeight != null) {
            setDefaultRowHeight(command.newRowHeight);
        }
        // we do not consume the command so other command handlers on other
        // layers are able to react
        return false;
    }

    @Override
    public Class<RowSizeConfigurationCommand> getCommandClass() {
        return RowSizeConfigurationCommand.class;
    }

}

/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *    Loris Securo <lorissek@gmail.com> - Bug 499622
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style.theme;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfigAttributes;
import org.eclipse.nebula.widgets.nattable.freeze.IFreezeConfigAttributes;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultGridLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayerPainter;
import org.eclipse.nebula.widgets.nattable.sort.config.DefaultSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.config.TreeConfigAttributes;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.swt.graphics.Color;

/**
 * Specialised {@link IConfiguration} that combines style configurations for
 * different parts of a NatTable composition. It can be used in two different
 * ways:
 * <ol>
 * <li>As it is a {@link IConfiguration} it can be simply added to a NatTable
 * instance like any other configuration.
 *
 * <pre>
 * NatTable natTable = new NatTable(parent, layer, false);
 * natTable.addConfiguration(new MyThemeConfiguration());
 * natTable.configure();
 * </pre>
 *
 * Using it like this will apply the style configurations, but might cause
 * issues when trying to switch to another theme configuration, because the
 * configurations can not be cleaned up correctly.</li>
 * <li>Setting the ThemeConfiguration via
 * {@link NatTable#setTheme(ThemeConfiguration)}. This will internally use a
 * {@link ThemeManager} which supports switching themes at runtime.</li>
 * </ol>
 */
public abstract class ThemeConfiguration extends AbstractRegistryConfiguration {

    /**
     * Flag to configure whether the corner should be styled like the column
     * header in a grid composition. The ThemeConfiguration supports different
     * styling of different regions, so the default value is <code>false</code>.
     * But typically the corner is styled like the column header to provide a
     * concise styling.
     */
    protected boolean styleCornerLikeColumnHeader = false;

    /**
     * Collection of {@link IThemeExtension} that should be added to this
     * ThemeConfiguration.
     */
    protected final List<IThemeExtension> extensions = new ArrayList<IThemeExtension>();

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        configureDefaultStyle(configRegistry);
        configureColumnHeaderStyle(configRegistry);
        configureRowHeaderStyle(configRegistry);
        configureCornerStyle(configRegistry);

        configureHoverStyle(configRegistry);
        configureHoverSelectionStyle(configRegistry);

        configureDefaultSelectionStyle(configRegistry);
        configureColumnHeaderSelectionStyle(configRegistry);
        configureRowHeaderSelectionStyle(configRegistry);
        configureCornerSelectionStyle(configRegistry);
        configureSelectionAnchorStyle(configRegistry);

        configureAlternatingRowStyle(configRegistry);

        configureColumnGroupHeaderStyle(configRegistry);
        configureRowGroupHeaderStyle(configRegistry);

        configureSortHeaderStyle(configRegistry);
        configureSelectedSortHeaderStyle(configRegistry);

        configureFilterRowStyle(configRegistry);

        configureTreeStyle(configRegistry);

        configureFreezeStyle(configRegistry);

        configureGridLineStyle(configRegistry);

        configureSummaryRowStyle(configRegistry);

        configureEditErrorStyle(configRegistry);

        configureFillHandleStyle(configRegistry);

        configureCopyBorderStyle(configRegistry);

        for (IThemeExtension extension : this.extensions) {
            extension.registerStyles(configRegistry);
        }
    }

    /**
     * Register default style configurations. Typically these configurations are
     * used be the body region and will be overridden by more specific
     * configurations of the header regions or custom styling based on labels.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureDefaultStyle(IConfigRegistry configRegistry) {
        IStyle defaultStyle = getDefaultCellStyle();
        if (!isStyleEmpty(defaultStyle)) {
            // register body cell style for every display mode
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, defaultStyle);
        }

        ICellPainter defaultPainter = getDefaultCellPainter();
        if (defaultPainter != null) {
            // register body cell painter for every display mode
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, defaultPainter);
        }
    }

    /**
     * @return The {@link IStyle} that should be used by default to render
     *         NatTable.
     */
    protected abstract IStyle getDefaultCellStyle();

    /**
     * Returns the default {@link ICellPainter} that is used to render the
     * NatTable.
     * <p>
     * Typically this {@link ICellPainter} is used to render the body region and
     * is overridden for other regions or other custom styling configurations
     * based on labels.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used by default to render
     *         NatTable.
     */
    protected abstract ICellPainter getDefaultCellPainter();

    /**
     * Register the style configurations for rendering the column header in a
     * NatTable.
     * <p>
     * By default this means to register the style configurations against
     * {@link DisplayMode#NORMAL} and config/region label
     * {@link GridRegion#COLUMN_HEADER}.
     * </p>
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureColumnHeaderStyle(IConfigRegistry configRegistry) {
        IStyle columnHeaderStyle = getColumnHeaderStyle();
        if (!isStyleEmpty(columnHeaderStyle)) {
            // register column header cell style in normal mode
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, columnHeaderStyle,
                    DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
        }

        ICellPainter columnHeaderCellPainter = getColumnHeaderCellPainter();
        if (columnHeaderCellPainter != null) {
            // register column header cell painter in normal mode
            // will also be used in other modes if no other cell painter is
            // registered explicitly
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, columnHeaderCellPainter,
                    DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render the column
     * header in a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#NORMAL} in the region with the region label
     * {@link GridRegion#COLUMN_HEADER}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the column
     *         header in a NatTable.
     */
    protected abstract IStyle getColumnHeaderStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the column
     * header in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#NORMAL} in the region with the region label
     * {@link GridRegion#COLUMN_HEADER}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the column
     *         header in a NatTable.
     */
    protected abstract ICellPainter getColumnHeaderCellPainter();

    /**
     * Register the style configurations for rendering the row header in a
     * NatTable.
     * <p>
     * By default this means to register the style configurations against
     * {@link DisplayMode#NORMAL} and config/region label
     * {@link GridRegion#ROW_HEADER}.
     * </p>
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureRowHeaderStyle(IConfigRegistry configRegistry) {
        IStyle rowHeaderStyle = getRowHeaderStyle();
        if (!isStyleEmpty(rowHeaderStyle)) {
            // register row header cell style in normal mode
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, rowHeaderStyle,
                    DisplayMode.NORMAL, GridRegion.ROW_HEADER);
        }

        ICellPainter rowHeaderCellPainter = getRowHeaderCellPainter();
        if (rowHeaderCellPainter != null) {
            // register row header cell painter in normal mode
            // will also be used in other modes if no other cell painter is
            // registered explicitly
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, rowHeaderCellPainter,
                    DisplayMode.NORMAL, GridRegion.ROW_HEADER);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render the row header
     * in a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#NORMAL} in the region with the region label
     * {@link GridRegion#ROW_HEADER}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the row header
     *         in a NatTable.
     */
    protected abstract IStyle getRowHeaderStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the row
     * header in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#NORMAL} in the region with the region label
     * {@link GridRegion#ROW_HEADER}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the row
     *         header in a NatTable.
     */
    protected abstract ICellPainter getRowHeaderCellPainter();

    /**
     * Register the style configurations for rendering the corner in a NatTable.
     * <p>
     * By default this means to register the style configurations against
     * {@link DisplayMode#NORMAL} and config/region label
     * {@link GridRegion#CORNER}.
     * </p>
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureCornerStyle(IConfigRegistry configRegistry) {
        IStyle cornerStyle = this.styleCornerLikeColumnHeader ? getColumnHeaderStyle()
                : getCornerStyle();
        if (!isStyleEmpty(cornerStyle)) {
            // register corner cell style in normal mode
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, cornerStyle,
                    DisplayMode.NORMAL, GridRegion.CORNER);
        }

        ICellPainter cornerCellPainter = this.styleCornerLikeColumnHeader ? getColumnHeaderCellPainter()
                : getCornerCellPainter();
        if (cornerCellPainter != null) {
            // register corner cell painter in normal mode
            // will also be used in other modes if no other cell painter is
            // registered explicitly
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, cornerCellPainter,
                    DisplayMode.NORMAL, GridRegion.CORNER);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render the corner of a
     * NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#NORMAL} in the region with the region label
     * {@link GridRegion#CORNER}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the corner of a
     *         NatTable.
     */
    protected abstract IStyle getCornerStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the corner
     * in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#NORMAL} in the region with the region label
     * {@link GridRegion#CORNER}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the corner
     *         in a NatTable.
     */
    protected abstract ICellPainter getCornerCellPainter();

    /**
     * Register the style configurations for hovering.
     * <p>
     * This means to register the style configurations against
     * {@link DisplayMode#HOVER}. Additionally the GridRegion labels are used to
     * register hover styles per region.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureHoverStyle(IConfigRegistry configRegistry) {
        IStyle defaultHoverStyle = getDefaultHoverStyle();
        if (!isStyleEmpty(defaultHoverStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, defaultHoverStyle,
                    DisplayMode.HOVER);
        }
        ICellPainter defaultHoverCellPainter = getDefaultHoverCellPainter();
        if (defaultHoverCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, defaultHoverCellPainter,
                    DisplayMode.HOVER);
        }

        IStyle bodyHoverStyle = getBodyHoverStyle();
        if (!isStyleEmpty(bodyHoverStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, bodyHoverStyle,
                    DisplayMode.HOVER, GridRegion.BODY);
        }
        ICellPainter bodyHoverCellPainter = getBodyHoverCellPainter();
        if (bodyHoverCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, bodyHoverCellPainter,
                    DisplayMode.HOVER, GridRegion.BODY);
        }

        IStyle columnHeaderHoverStyle = getColumnHeaderHoverStyle();
        if (!isStyleEmpty(columnHeaderHoverStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, columnHeaderHoverStyle,
                    DisplayMode.HOVER, GridRegion.COLUMN_HEADER);
        }
        ICellPainter columnHeaderHoverCellPainter = getColumnHeaderHoverCellPainter();
        if (columnHeaderHoverCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    columnHeaderHoverCellPainter, DisplayMode.HOVER,
                    GridRegion.COLUMN_HEADER);
        }

        IStyle rowHeaderHoverStyle = getRowHeaderHoverStyle();
        if (!isStyleEmpty(rowHeaderHoverStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, rowHeaderHoverStyle,
                    DisplayMode.HOVER, GridRegion.ROW_HEADER);
        }
        ICellPainter rowHeaderHoverCellPainter = getRowHeaderHoverCellPainter();
        if (rowHeaderHoverCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    rowHeaderHoverCellPainter, DisplayMode.HOVER,
                    GridRegion.ROW_HEADER);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used by default to render
     * hovered cells in a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#HOVER}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render hovered cells in
     *         a NatTable.
     */
    protected abstract IStyle getDefaultHoverStyle();

    /**
     * Returns the {@link ICellPainter} that should be used by default to render
     * hovered cells in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#HOVER}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render hovered
     *         cells in a NatTable.
     */
    protected abstract ICellPainter getDefaultHoverCellPainter();

    /**
     * Returns the {@link IStyle} that should be used to render hovered cells in
     * a NatTable body region.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#HOVER} in the region with the region label
     * {@link GridRegion#BODY}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render hovered cells in
     *         a NatTable body region.
     */
    protected abstract IStyle getBodyHoverStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render hovered
     * cells in a NatTable body region.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#HOVER} in the region with the region label
     * {@link GridRegion#BODY}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render hovered
     *         cells in a NatTable body region.
     */
    protected abstract ICellPainter getBodyHoverCellPainter();

    /**
     * Returns the {@link IStyle} that should be used to render hovered cells in
     * a NatTable column header region.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#HOVER} in the region with the region label
     * {@link GridRegion#COLUMN_HEADER}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render hovered cells in
     *         a NatTable column header region.
     */
    protected abstract IStyle getColumnHeaderHoverStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render hovered
     * cells in a NatTable column header region.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#HOVER} in the region with the region label
     * {@link GridRegion#COLUMN_HEADER}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render hovered
     *         cells in a NatTable column header region.
     */
    protected abstract ICellPainter getColumnHeaderHoverCellPainter();

    /**
     * Returns the {@link IStyle} that should be used to render hovered cells in
     * a NatTable row header region.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#HOVER} in the region with the region label
     * {@link GridRegion#ROW_HEADER}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render hovered cells in
     *         a NatTable row header region.
     */
    protected abstract IStyle getRowHeaderHoverStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render hovered
     * cells in a NatTable row header region.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#HOVER} in the region with the region label
     * {@link GridRegion#ROW_HEADER}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render hovered
     *         cells in a NatTable row header region.
     */
    protected abstract ICellPainter getRowHeaderHoverCellPainter();

    /**
     * Register the style configurations for hovering selections.
     * <p>
     * This means to register the style configurations against
     * {@link DisplayMode#SELECT_HOVER}. Additionally the GridRegion labels are
     * used to register hover styles per region.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureHoverSelectionStyle(IConfigRegistry configRegistry) {
        IStyle defaultHoverStyle = getDefaultHoverSelectionStyle();
        if (!isStyleEmpty(defaultHoverStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, defaultHoverStyle,
                    DisplayMode.SELECT_HOVER);
        }
        ICellPainter defaultHoverCellPainter = getDefaultHoverSelectionCellPainter();
        if (defaultHoverCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, defaultHoverCellPainter,
                    DisplayMode.SELECT_HOVER);
        }

        IStyle bodyHoverStyle = getBodyHoverSelectionStyle();
        if (!isStyleEmpty(bodyHoverStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, bodyHoverStyle,
                    DisplayMode.SELECT_HOVER, GridRegion.BODY);
        }
        ICellPainter bodyHoverCellPainter = getBodyHoverSelectionCellPainter();
        if (bodyHoverCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, bodyHoverCellPainter,
                    DisplayMode.SELECT_HOVER, GridRegion.BODY);
        }

        IStyle columnHeaderHoverStyle = getColumnHeaderHoverSelectionStyle();
        if (!isStyleEmpty(columnHeaderHoverStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, columnHeaderHoverStyle,
                    DisplayMode.SELECT_HOVER, GridRegion.COLUMN_HEADER);
        }
        ICellPainter columnHeaderHoverCellPainter = getColumnHeaderHoverSelectionCellPainter();
        if (columnHeaderHoverCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    columnHeaderHoverCellPainter, DisplayMode.SELECT_HOVER,
                    GridRegion.COLUMN_HEADER);
        }

        IStyle rowHeaderHoverStyle = getRowHeaderHoverSelectionStyle();
        if (!isStyleEmpty(rowHeaderHoverStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, rowHeaderHoverStyle,
                    DisplayMode.SELECT_HOVER, GridRegion.ROW_HEADER);
        }
        ICellPainter rowHeaderHoverCellPainter = getRowHeaderHoverSelectionCellPainter();
        if (rowHeaderHoverCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    rowHeaderHoverCellPainter, DisplayMode.SELECT_HOVER,
                    GridRegion.ROW_HEADER);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used by default to render
     * hovered selected cells in a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#SELECT_HOVER}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render hovered selected
     *         cells in a NatTable.
     */
    protected abstract IStyle getDefaultHoverSelectionStyle();

    /**
     * Returns the {@link ICellPainter} that should be used by default to render
     * hovered selected cells in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#SELECT_HOVER}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render hovered
     *         selected cells in a NatTable.
     */
    protected abstract ICellPainter getDefaultHoverSelectionCellPainter();

    /**
     * Returns the {@link IStyle} that should be used to render hovered selected
     * cells in a NatTable body region.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#SELECT_HOVER} in the region with the region label
     * {@link GridRegion#BODY}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render hovered selected
     *         cells in a NatTable body region.
     */
    protected abstract IStyle getBodyHoverSelectionStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render hovered
     * selected cells in a NatTable body region.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#SELECT_HOVER} in the region with the region label
     * {@link GridRegion#BODY}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render hovered
     *         selected cells in a NatTable body region.
     */
    protected abstract ICellPainter getBodyHoverSelectionCellPainter();

    /**
     * Returns the {@link IStyle} that should be used to render hovered selected
     * cells in a NatTable column header region.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#SELECT_HOVER} in the region with the region label
     * {@link GridRegion#COLUMN_HEADER}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render hovered selected
     *         cells in a NatTable column header region.
     */
    protected abstract IStyle getColumnHeaderHoverSelectionStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render hovered
     * selected cells in a NatTable column header region.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#SELECT_HOVER} in the region with the region label
     * {@link GridRegion#COLUMN_HEADER}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render hovered
     *         selected cells in a NatTable column header region.
     */
    protected abstract ICellPainter getColumnHeaderHoverSelectionCellPainter();

    /**
     * Returns the {@link IStyle} that should be used to render hovered selected
     * cells in a NatTable row header region.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#SELECT_HOVER} in the region with the region label
     * {@link GridRegion#ROW_HEADER}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render hovered selected
     *         cells in a NatTable row header region.
     */
    protected abstract IStyle getRowHeaderHoverSelectionStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render hovered
     * selected cells in a NatTable row header region.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#SELECT_HOVER} in the region with the region label
     * {@link GridRegion#ROW_HEADER}.
     * </p>
     * <p>
     * Note: This configuration is only working if the {@link HoverLayer} is
     * part of the layer stack. Otherwise the configuration will not have any
     * effect.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render hovered
     *         selected cells in a NatTable row header region.
     */
    protected abstract ICellPainter getRowHeaderHoverSelectionCellPainter();

    /**
     * Register default selection style configurations. Typically these
     * configurations are used be the body region and will be overridden by more
     * specific configurations of the header regions or custom styling based on
     * labels.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureDefaultSelectionStyle(IConfigRegistry configRegistry) {
        // register body cell style for every display mode
        IStyle defaultSelectionStyle = getDefaultSelectionCellStyle();
        if (!isStyleEmpty(defaultSelectionStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, defaultSelectionStyle,
                    DisplayMode.SELECT);
        }

        // register body cell painter for every display mode
        ICellPainter defaultSelectionCellPainter = getDefaultSelectionCellPainter();
        if (defaultSelectionCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    defaultSelectionCellPainter, DisplayMode.SELECT);
        }
    }

    /**
     * @return The {@link IStyle} that should be used by default to render
     *         selected cells in NatTable.
     */
    protected abstract IStyle getDefaultSelectionCellStyle();

    /**
     * Returns the default {@link ICellPainter} that is used to render selected
     * cells in NatTable.
     * <p>
     * Typically this {@link ICellPainter} is used to render the body region and
     * is overridden for other regions or other custom styling configurations
     * based on labels.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used by default to render
     *         selected cells in NatTable.
     */
    protected abstract ICellPainter getDefaultSelectionCellPainter();

    /**
     * Register the style configurations for rendering the selection in a column
     * header in a NatTable.
     * <p>
     * By default this means to register the style configurations against
     * {@link DisplayMode#SELECT} and config/region label
     * {@link GridRegion#COLUMN_HEADER}. The styling for rendering full column
     * selection is configured against the label
     * {@link SelectionStyleLabels#COLUMN_FULLY_SELECTED_STYLE}.
     * </p>
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureColumnHeaderSelectionStyle(
            IConfigRegistry configRegistry) {
        IStyle columnHeaderStyle = getColumnHeaderSelectionStyle();
        if (!isStyleEmpty(columnHeaderStyle)) {
            // register column header cell style in select mode
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, columnHeaderStyle,
                    DisplayMode.SELECT, GridRegion.COLUMN_HEADER);
        }

        ICellPainter columnHeaderCellPainter = getColumnHeaderSelectionCellPainter();
        if (columnHeaderCellPainter != null) {
            // register column header cell painter in select mode
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, columnHeaderCellPainter,
                    DisplayMode.SELECT, GridRegion.COLUMN_HEADER);
        }

        IStyle fullSelectionColumnHeaderStyle = getColumnHeaderFullSelectionStyle();
        if (!isStyleEmpty(fullSelectionColumnHeaderStyle)) {
            // register column header cell style in select mode when all cells
            // in the column are selected
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    fullSelectionColumnHeaderStyle, DisplayMode.SELECT,
                    SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE);
        }

        ICellPainter fullSelectionColumnHeaderCellPainter = getColumnHeaderFullSelectionCellPainter();
        if (fullSelectionColumnHeaderCellPainter != null) {
            // register column header cell painter in select mode when all cells
            // in the column are selected
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    fullSelectionColumnHeaderCellPainter, DisplayMode.SELECT,
                    SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render the selected
     * cells in the column header of a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#SELECT} in the region with the region label
     * {@link GridRegion#COLUMN_HEADER}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the column
     *         header in a NatTable.
     */
    protected abstract IStyle getColumnHeaderSelectionStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the
     * selected cells in the column header of a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#SELECT} in the region with the region label
     * {@link GridRegion#COLUMN_HEADER}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the column
     *         header in a NatTable.
     */
    protected abstract ICellPainter getColumnHeaderSelectionCellPainter();

    /**
     * Returns the {@link IStyle} that should be used to render the selected
     * cells in the column header of a NatTable when all cells in the column are
     * selected.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#SELECT} and the label
     * {@link SelectionStyleLabels#COLUMN_FULLY_SELECTED_STYLE}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the column
     *         header in a NatTable when all cells in the column are selected.
     */
    protected abstract IStyle getColumnHeaderFullSelectionStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the
     * selected cells in the column header of a NatTable when all cells in the
     * column are selected.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#SELECT} and the label
     * {@link SelectionStyleLabels#COLUMN_FULLY_SELECTED_STYLE}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the column
     *         header in a NatTable when all cells in the column are selected.
     */
    protected abstract ICellPainter getColumnHeaderFullSelectionCellPainter();

    /**
     * Register the style configurations for rendering the selection in a row
     * header in a NatTable.
     * <p>
     * By default this means to register the style configurations against
     * {@link DisplayMode#SELECT} and config/region label
     * {@link GridRegion#ROW_HEADER}. The styling for rendering full row
     * selection is configured against the label
     * {@link SelectionStyleLabels#ROW_FULLY_SELECTED_STYLE}.
     * </p>
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureRowHeaderSelectionStyle(
            IConfigRegistry configRegistry) {
        IStyle rowHeaderStyle = getRowHeaderSelectionStyle();
        if (!isStyleEmpty(rowHeaderStyle)) {
            // register column header cell style in select mode
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, rowHeaderStyle,
                    DisplayMode.SELECT, GridRegion.ROW_HEADER);
        }

        ICellPainter rowHeaderCellPainter = getRowHeaderSelectionCellPainter();
        if (rowHeaderCellPainter != null) {
            // register column header cell painter in select mode
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, rowHeaderCellPainter,
                    DisplayMode.SELECT, GridRegion.ROW_HEADER);
        }

        IStyle fullSelectionRowHeaderStyle = getRowHeaderFullSelectionStyle();
        if (!isStyleEmpty(fullSelectionRowHeaderStyle)) {
            // register row header cell style in select mode when all cells in
            // the row are selected
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    fullSelectionRowHeaderStyle, DisplayMode.SELECT,
                    SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE);
        }

        ICellPainter fullSelectionRowHeaderCellPainter = getRowHeaderFullSelectionCellPainter();
        if (fullSelectionRowHeaderCellPainter != null) {
            // register row header cell painter in select mode when all cells in
            // the row are selected
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    fullSelectionRowHeaderCellPainter, DisplayMode.SELECT,
                    SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render the selected
     * cells in the row header of a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#SELECT} in the region with the region label
     * {@link GridRegion#ROW_HEADER}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the row header
     *         in a NatTable.
     */
    protected abstract IStyle getRowHeaderSelectionStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the
     * selected cells in the row header of a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#SELECT} in the region with the region label
     * {@link GridRegion#ROW_HEADER}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the row
     *         header in a NatTable.
     */
    protected abstract ICellPainter getRowHeaderSelectionCellPainter();

    /**
     * Returns the {@link IStyle} that should be used to render the selected
     * cells in the row header of a NatTable when all cells in the row are
     * selected.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#SELECT} and the label
     * {@link SelectionStyleLabels#ROW_FULLY_SELECTED_STYLE}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the row header
     *         in a NatTable when all cells in the row are selected.
     */
    protected abstract IStyle getRowHeaderFullSelectionStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the
     * selected cells in the row header of a NatTable when all cells in the row
     * are selected.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#SELECT} and the label
     * {@link SelectionStyleLabels#ROW_FULLY_SELECTED_STYLE}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the row
     *         header in a NatTable when all cells in the row are selected.
     */
    protected abstract ICellPainter getRowHeaderFullSelectionCellPainter();

    /**
     * Register the style configurations for rendering the selection in the
     * corner of a NatTable.
     * <p>
     * By default this means to register the style configurations against
     * {@link DisplayMode#SELECT} and config/region label
     * {@link GridRegion#CORNER}.
     * </p>
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureCornerSelectionStyle(IConfigRegistry configRegistry) {
        IStyle cornerStyle = this.styleCornerLikeColumnHeader ? getColumnHeaderSelectionStyle()
                : getCornerSelectionStyle();
        if (!isStyleEmpty(cornerStyle)) {
            // register corner cell style in select mode
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, cornerStyle,
                    DisplayMode.SELECT, GridRegion.CORNER);
        }

        ICellPainter cornerCellPainter = this.styleCornerLikeColumnHeader ? getColumnHeaderSelectionCellPainter()
                : getCornerSelectionCellPainter();
        if (cornerCellPainter != null) {
            // register corner cell painter in select mode
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, cornerCellPainter,
                    DisplayMode.SELECT, GridRegion.CORNER);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render the selected
     * cells in the corner of a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#SELECT} in the region with the region label
     * {@link GridRegion#CORNER}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the corner in a
     *         NatTable.
     */
    protected abstract IStyle getCornerSelectionStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the
     * selected cells in the corner of a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#SELECT} in the region with the region label
     * {@link GridRegion#CORNER}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the corner
     *         in a NatTable.
     */
    protected abstract ICellPainter getCornerSelectionCellPainter();

    /**
     * Register the style configurations to render the selection anchor.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureSelectionAnchorStyle(IConfigRegistry configRegistry) {
        // Selection anchor style for normal display mode
        IStyle anchorStyle = getSelectionAnchorStyle();
        if (!isStyleEmpty(anchorStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, anchorStyle,
                    DisplayMode.NORMAL,
                    SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
        }
        ICellPainter anchorPainter = getSelectionAnchorCellPainter();
        if (anchorPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, anchorPainter,
                    DisplayMode.NORMAL,
                    SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
        }

        // Selection anchor style for select display mode
        IStyle selectionAnchorStyle = getSelectionAnchorSelectionStyle();
        if (!isStyleEmpty(selectionAnchorStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, selectionAnchorStyle,
                    DisplayMode.SELECT,
                    SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
        }
        ICellPainter selectionAnchorPainter = getSelectionAnchorSelectionCellPainter();
        if (selectionAnchorPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, selectionAnchorPainter,
                    DisplayMode.SELECT,
                    SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
        }

        // configure selection anchor grid line style
        IStyle gridLineStyle = getSelectionAnchorGridLineStyle();
        if (!isStyleEmpty(gridLineStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, gridLineStyle,
                    DisplayMode.SELECT,
                    SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE);
        }
    }

    /**
     * Returns the {@link IStyle} that is used to render the selection anchor in
     * normal display mode.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#NORMAL} for the cell that contains the cell label
     * {@link SelectionStyleLabels#SELECTION_ANCHOR_STYLE}.
     * </p>
     * <p>
     * Typically only the border style is set here for a concise rendering.
     * </p>
     *
     * @return The {@link IStyle} that is used to render the selection anchor in
     *         normal display mode.
     */
    protected abstract IStyle getSelectionAnchorStyle();

    /**
     * Returns the {@link ICellPainter} that is used to render the selection
     * anchor in normal display mode.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#NORMAL} for the cell that contains the cell label
     * {@link SelectionStyleLabels#SELECTION_ANCHOR_STYLE}.
     * </p>
     * <p>
     * Typically there is no other painter registered for rendering selections,
     * which simply causes different styling.
     * </p>
     *
     * @return The {@link ICellPainter} that is used to render the selection
     *         anchor in normal display mode.
     */
    protected abstract ICellPainter getSelectionAnchorCellPainter();

    /**
     * Returns the {@link IStyle} that is used to render the selection anchor in
     * selection display mode.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#SELECT} for the cell that contains the cell label
     * {@link SelectionStyleLabels#SELECTION_ANCHOR_STYLE}.
     * </p>
     * <p>
     * Typically the border style should be the same as used by
     * {@link ThemeConfiguration#getSelectionAnchorStyle()}.
     * </p>
     *
     * @return The {@link IStyle} that is used to render the selection anchor in
     *         selection display mode.
     */
    protected abstract IStyle getSelectionAnchorSelectionStyle();

    /**
     * Returns the {@link ICellPainter} that is used to render the selection
     * anchor in selection display mode.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#SELECT} for the cell that contains the cell label
     * {@link SelectionStyleLabels#SELECTION_ANCHOR_STYLE}.
     * </p>
     * <p>
     * Typically there is no other painter registered for rendering selections,
     * which simply causes different styling.
     * </p>
     *
     * @return The {@link ICellPainter} that is used to render the selection
     *         anchor in selection display mode.
     */
    protected abstract ICellPainter getSelectionAnchorSelectionCellPainter();

    /**
     * Returns the {@link IStyle} that should be used to render the grid lines
     * around the selection anchor. That means this style will be registered
     * against the label
     * {@link SelectionStyleLabels#SELECTION_ANCHOR_GRID_LINE_STYLE}. It will be
     * interpreted by the {@link SelectionLayerPainter} which only checks the
     * {@link CellStyleAttributes#BORDER_STYLE} attribute. All other style
     * configurations won't be interpreted.
     *
     * @return The {@link IStyle} that should be used to render the grid lines
     *         around the selection anchor.
     *
     * @see SelectionLayerPainter
     */
    protected abstract IStyle getSelectionAnchorGridLineStyle();

    /**
     * When creating a NatTable grid composition, using the
     * {@link DefaultGridLayerConfiguration}, the
     * {@link AlternatingRowConfigLabelAccumulator} is configured for the body
     * region to apply labels for alternating rows. It applies the following
     * labels for which this method registers styles:
     * <ul>
     * <li>{@link AlternatingRowConfigLabelAccumulator#EVEN_ROW_CONFIG_TYPE}
     * </li>
     * <li>{@link AlternatingRowConfigLabelAccumulator#EVEN_ROW_CONFIG_TYPE}
     * </li>
     * </ul>
     * If the {@link AlternatingRowConfigLabelAccumulator} is not configured,
     * this style configuration will have no effect.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureAlternatingRowStyle(IConfigRegistry configRegistry) {
        IStyle evenStyle = getEvenRowStyle();
        if (!isStyleEmpty(evenStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, evenStyle,
                    DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
        }

        ICellPainter evenCellPainter = getEvenRowCellPainter();
        if (evenCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, evenCellPainter,
                    DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
        }

        IStyle oddStyle = getOddRowStyle();
        if (!isStyleEmpty(oddStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, oddStyle,
                    DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);
        }

        ICellPainter oddCellPainter = getOddRowCellPainter();
        if (oddCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, oddCellPainter,
                    DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render alternating
     * rows.
     * <p>
     * That means this {@link IStyle} is registered against the label
     * {@link AlternatingRowConfigLabelAccumulator#EVEN_ROW_CONFIG_TYPE}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that is used to render rows that contain the
     *         label
     *         {@link AlternatingRowConfigLabelAccumulator#EVEN_ROW_CONFIG_TYPE}
     *         in the label stack.
     */
    protected abstract IStyle getEvenRowStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render
     * alternating rows.
     * <p>
     * That means this {@link ICellPainter} is registered against the label
     * {@link AlternatingRowConfigLabelAccumulator#EVEN_ROW_CONFIG_TYPE}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that is used to render rows that contain
     *         the label
     *         {@link AlternatingRowConfigLabelAccumulator#EVEN_ROW_CONFIG_TYPE}
     *         in the label stack.
     */
    protected abstract ICellPainter getEvenRowCellPainter();

    /**
     * Returns the {@link IStyle} that should be used to render alternating
     * rows.
     * <p>
     * That means this {@link IStyle} is registered against the label
     * {@link AlternatingRowConfigLabelAccumulator#ODD_ROW_CONFIG_TYPE}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that is used to render rows that contain the
     *         label
     *         {@link AlternatingRowConfigLabelAccumulator#ODD_ROW_CONFIG_TYPE}
     *         in the label stack.
     */
    protected abstract IStyle getOddRowStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render
     * alternating rows.
     * <p>
     * That means this {@link ICellPainter} is registered against the label
     * {@link AlternatingRowConfigLabelAccumulator#ODD_ROW_CONFIG_TYPE}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that is used to render rows that contain
     *         the label
     *         {@link AlternatingRowConfigLabelAccumulator#ODD_ROW_CONFIG_TYPE}
     *         in the label stack.
     */
    protected abstract ICellPainter getOddRowCellPainter();

    /**
     * This method is used to register style configurations for the column group
     * header in a NatTable.
     * <p>
     * When adding the ColumnGroupHeaderLayer to a layer stack, there will be a
     * new region with region label {@link GridRegion#COLUMN_GROUP_HEADER}.
     * Typically it will share the same styling as the column header and have an
     * internally configured {@link ICellPainter} to render dependent on the
     * state of the ColumnGroupModel.
     * </p>
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureColumnGroupHeaderStyle(
            IConfigRegistry configRegistry) {
        IStyle columnGroupHeaderStyle = getColumnGroupHeaderStyle();
        if (!isStyleEmpty(columnGroupHeaderStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, columnGroupHeaderStyle,
                    DisplayMode.NORMAL, GridRegion.COLUMN_GROUP_HEADER);
        }

        ICellPainter columnGroupHeaderCellPainter = getColumnGroupHeaderCellPainter();
        if (columnGroupHeaderCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    columnGroupHeaderCellPainter, DisplayMode.NORMAL,
                    GridRegion.COLUMN_GROUP_HEADER);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render the column group
     * header in a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#NORMAL} in the region with the region label
     * {@link GridRegion#COLUMN_GROUP_HEADER}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the column group
     *         header in a NatTable.
     */
    protected abstract IStyle getColumnGroupHeaderStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the column
     * group header in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#NORMAL} in the region with the region label
     * {@link GridRegion#COLUMN_GROUP_HEADER}.
     * </p>
     * <p>
     * If this method returns <code>null</code> the default configuration of the
     * ColumnGroupHeaderLayer is used. By default the
     * ColumnGroupHeaderTextPainter is registered to render icons corresponding
     * to the expand/collapse state of the column group.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the column
     *         group header in a NatTable.
     */
    protected abstract ICellPainter getColumnGroupHeaderCellPainter();

    /**
     * This method is used to register style configurations for the row group
     * header in a NatTable.
     * <p>
     * When adding the RowGroupHeaderLayer to a layer stack, there will be a new
     * region with region label {@link GridRegion#ROW_GROUP_HEADER}. Typically
     * it will share the same styling as the row header and have an internally
     * configured {@link ICellPainter} to render dependent on the state of the
     * RowGroupModel.
     * </p>
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureRowGroupHeaderStyle(IConfigRegistry configRegistry) {
        IStyle rowGroupHeaderStyle = getRowGroupHeaderStyle();
        if (!isStyleEmpty(rowGroupHeaderStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, rowGroupHeaderStyle,
                    DisplayMode.NORMAL, GridRegion.ROW_GROUP_HEADER);
        }

        ICellPainter rowGroupHeaderCellPainter = getRowGroupHeaderCellPainter();
        if (rowGroupHeaderCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    rowGroupHeaderCellPainter, DisplayMode.NORMAL,
                    GridRegion.ROW_GROUP_HEADER);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render the row group
     * header in a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#NORMAL} in the region with the region label
     * {@link GridRegion#ROW_GROUP_HEADER}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the row group
     *         header in a NatTable.
     */
    protected abstract IStyle getRowGroupHeaderStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the row
     * group header in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#NORMAL} in the region with the region label
     * {@link GridRegion#ROW_GROUP_HEADER}.
     * </p>
     * <p>
     * If this method returns <code>null</code> the default configuration of the
     * RowGroupHeaderLayer is used. By default the RowGroupHeaderTextPainter is
     * registered to render icons corresponding to the expand/collapse state of
     * the row group.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the row
     *         group header in a NatTable.
     */
    protected abstract ICellPainter getRowGroupHeaderCellPainter();

    /**
     * This method is used to register styles for the sort header layer. It will
     * register the {@link IStyle} and the {@link ICellPainter} for both sort
     * states which cause adding the following labels to the configuration label
     * stack of a cell:
     * <ul>
     * <li>DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE</li>
     * <li>DefaultSortConfiguration.SORT_UP_CONFIG_TYPE</li>
     * </ul>
     * Typically the {@link ICellPainter} itself takes care about the sort
     * state. If this needs to be handled differently, this method needs to be
     * overridden.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureSortHeaderStyle(IConfigRegistry configRegistry) {
        IStyle sortStyle = getSortHeaderStyle();
        if (!isStyleEmpty(sortStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, sortStyle,
                    DisplayMode.NORMAL,
                    DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, sortStyle,
                    DisplayMode.NORMAL,
                    DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
        }

        ICellPainter cellPainter = getSortHeaderCellPainter();
        if (cellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, cellPainter,
                    DisplayMode.NORMAL,
                    DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, cellPainter,
                    DisplayMode.NORMAL,
                    DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render the sort header
     * in a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#NORMAL} for the configurations labels
     * {@link DefaultSortConfiguration#SORT_DOWN_CONFIG_TYPE} and
     * {@link DefaultSortConfiguration#SORT_UP_CONFIG_TYPE}. If you need to
     * configure different styles for different sort states, you need to
     * override
     * {@link ThemeConfiguration#configureSortHeaderStyle(IConfigRegistry)}.
     * Usually the default painter is taking care of the different sort states.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the sort header
     *         in a NatTable.
     */
    protected abstract IStyle getSortHeaderStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the sort
     * header cells in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#NORMAL} for the configurations labels
     * {@link DefaultSortConfiguration#SORT_DOWN_CONFIG_TYPE} and
     * {@link DefaultSortConfiguration#SORT_UP_CONFIG_TYPE}. If you need to
     * configure different painters for different sort states, you need to
     * override
     * {@link ThemeConfiguration#configureSortHeaderStyle(IConfigRegistry)}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the sort
     *         header in a NatTable.
     */
    protected abstract ICellPainter getSortHeaderCellPainter();

    /**
     * This method is used to register styles for the selected sort header
     * layer. It will register the {@link IStyle} and the {@link ICellPainter}
     * for both sort states which cause adding the following labels to the
     * configuration label stack of a cell:
     * <ul>
     * <li>DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE</li>
     * <li>DefaultSortConfiguration.SORT_UP_CONFIG_TYPE</li>
     * </ul>
     * Typically the {@link ICellPainter} itself takes care about the sort
     * state. If this needs to be handled differently, this method needs to be
     * overridden.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureSelectedSortHeaderStyle(
            IConfigRegistry configRegistry) {
        IStyle sortStyle = getSelectedSortHeaderStyle();
        if (!isStyleEmpty(sortStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, sortStyle,
                    DisplayMode.SELECT,
                    DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, sortStyle,
                    DisplayMode.SELECT,
                    DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
        }

        ICellPainter cellPainter = getSelectedSortHeaderCellPainter();
        if (cellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, cellPainter,
                    DisplayMode.SELECT,
                    DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, cellPainter,
                    DisplayMode.SELECT,
                    DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render the sort header
     * in a NatTable in selected state.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#SELECT} for the configurations labels
     * {@link DefaultSortConfiguration#SORT_DOWN_CONFIG_TYPE} and
     * {@link DefaultSortConfiguration#SORT_UP_CONFIG_TYPE}. If you need to
     * configure different styles for different sort states, you need to
     * override
     * {@link ThemeConfiguration#configureSortHeaderStyle(IConfigRegistry)}.
     * Usually the default painter is taking care of the different sort states.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the selected
     *         sort header in a NatTable.
     */
    protected abstract IStyle getSelectedSortHeaderStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the sort
     * header cells in a NatTable in selected state.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#SELECT} for the configurations labels
     * {@link DefaultSortConfiguration#SORT_DOWN_CONFIG_TYPE} and
     * {@link DefaultSortConfiguration#SORT_UP_CONFIG_TYPE}. If you need to
     * configure different painters for different sort states, you need to
     * override
     * {@link ThemeConfiguration#configureSortHeaderStyle(IConfigRegistry)}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the
     *         selected sort header in a NatTable.
     */
    protected abstract ICellPainter getSelectedSortHeaderCellPainter();

    /**
     * This method is used to register style configurations for the filter row.
     * It will only be applied in case the FilterRowHeaderLayer is involved,
     * which introduces a new region that is recognised by
     * {@link GridRegion#FILTER_ROW}.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureFilterRowStyle(IConfigRegistry configRegistry) {
        IStyle filterRowStyle = getFilterRowStyle();
        if (!isStyleEmpty(filterRowStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, filterRowStyle,
                    DisplayMode.NORMAL, GridRegion.FILTER_ROW);
        }

        ICellPainter cellPainter = getFilterRowCellPainter();
        if (cellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, cellPainter,
                    DisplayMode.NORMAL, GridRegion.FILTER_ROW);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render the filter row
     * in a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#NORMAL} in the region with the region label
     * {@link GridRegion#FILTER_ROW}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the filter row
     *         in a NatTable.
     */
    protected abstract IStyle getFilterRowStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the filter
     * row cells in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#NORMAL} in the region with the region label
     * {@link GridRegion#FILTER_ROW}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the filter
     *         row in a NatTable.
     */
    protected abstract ICellPainter getFilterRowCellPainter();

    /**
     * This method is used to register style configurations for a tree
     * representation. It will only be applied in case a TreeLayer is involved,
     * which adds the configuration label {@link TreeLayer#TREE_COLUMN_CELL} to
     * the tree column.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureTreeStyle(IConfigRegistry configRegistry) {
        IStyle treeStyle = getTreeStyle();
        if (!isStyleEmpty(treeStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, treeStyle,
                    DisplayMode.NORMAL, TreeLayer.TREE_COLUMN_CELL);
        }

        ICellPainter cellPainter = getTreeCellPainter();
        if (cellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, cellPainter,
                    DisplayMode.NORMAL, TreeLayer.TREE_COLUMN_CELL);
        }

        IStyle treeSelectionStyle = getTreeSelectionStyle();
        if (!isStyleEmpty(treeSelectionStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, treeSelectionStyle,
                    DisplayMode.SELECT, TreeLayer.TREE_COLUMN_CELL);
        }

        ICellPainter selectionCellPainter = getTreeSelectionCellPainter();
        if (selectionCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, selectionCellPainter,
                    DisplayMode.SELECT, TreeLayer.TREE_COLUMN_CELL);
        }

        ICellPainter treePainter = getTreeStructurePainter();
        if (treePainter != null) {
            configRegistry.registerConfigAttribute(
                    TreeConfigAttributes.TREE_STRUCTURE_PAINTER, treePainter,
                    DisplayMode.NORMAL);
        }

        ICellPainter treeSelectionPainter = getTreeStructureSelectionPainter();
        if (treeSelectionPainter != null) {
            configRegistry.registerConfigAttribute(
                    TreeConfigAttributes.TREE_STRUCTURE_PAINTER,
                    treeSelectionPainter, DisplayMode.SELECT);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render the tree column
     * cells in a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#NORMAL} and the configuration label
     * {@link TreeLayer#TREE_COLUMN_CELL}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the tree column
     *         in a NatTable.
     */
    protected abstract IStyle getTreeStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the tree
     * column cells in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#NORMAL} and the configuration label
     * {@link TreeLayer#TREE_COLUMN_CELL}.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the tree
     *         column in a NatTable.
     */
    protected abstract ICellPainter getTreeCellPainter();

    /**
     * Returns the {@link IStyle} that should be used to render the selected
     * tree column cells in a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#SELECT} and the configuration label
     * {@link TreeLayer#TREE_COLUMN_CELL}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the selected
     *         tree column in a NatTable.
     */
    protected abstract IStyle getTreeSelectionStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the
     * selected tree column cells in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#SELECT} and the configuration label
     * {@link TreeLayer#TREE_COLUMN_CELL}.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the
     *         selected tree column in a NatTable.
     */
    protected abstract ICellPainter getTreeSelectionCellPainter();

    /**
     * Returns the {@link ICellPainter} that should be used to render the tree
     * structure in a NatTable. It needs to be an
     * {@link IndentedTreeImagePainter} to show the expand/collapsed state
     * aswell as the indentation for the tree level. It that can be wrapped with
     * several {@link CellPainterWrapper}. If there is no
     * {@link IndentedTreeImagePainter} in the painter hierarchy, this
     * configuration attribute will be ignored by the TreeLayer.
     *
     * @return The {@link IndentedTreeImagePainter} that should be used to
     *         render the tree structure in a NatTable.
     */
    protected abstract ICellPainter getTreeStructurePainter();

    /**
     * Returns the {@link ICellPainter} that should be used to render the
     * selected tree structure in a NatTable. It needs to be an
     * {@link IndentedTreeImagePainter} to show the expand/collapsed state
     * aswell as the indentation for the tree level. It that can be wrapped with
     * several {@link CellPainterWrapper}. If there is no
     * {@link IndentedTreeImagePainter} in the painter hierarchy, this
     * configuration attribute will be ignored by the TreeLayer.
     *
     * @return The {@link IndentedTreeImagePainter} that should be used to
     *         render the selected tree structure in a NatTable.
     */
    protected abstract ICellPainter getTreeStructureSelectionPainter();

    /**
     * This method is used to register style configurations for a summary row.
     * It will only be applied in case a SummaryRowLayer is involved, which adds
     * the configuration label
     * {@link SummaryRowLayer#DEFAULT_SUMMARY_ROW_CONFIG_LABEL} to the summary
     * row.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureSummaryRowStyle(IConfigRegistry configRegistry) {
        IStyle style = getSummaryRowStyle();
        if (!isStyleEmpty(style)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL,
                    SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
        }

        ICellPainter cellPainter = getSummaryRowCellPainter();
        if (cellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, cellPainter,
                    DisplayMode.NORMAL,
                    SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
        }

        style = getSummaryRowSelectionStyle();
        if (!isStyleEmpty(style)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT,
                    SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
        }

        cellPainter = getSummaryRowSelectionCellPainter();
        if (cellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, cellPainter,
                    DisplayMode.SELECT,
                    SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render the summary row
     * cells in a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#NORMAL} and the configuration label
     * {@link SummaryRowLayer#DEFAULT_SUMMARY_ROW_CONFIG_LABEL}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the summary row
     *         in a NatTable.
     */
    protected abstract IStyle getSummaryRowStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the
     * summary row cells in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#NORMAL} and the configuration label
     * {@link SummaryRowLayer#DEFAULT_SUMMARY_ROW_CONFIG_LABEL}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the
     *         summary row in a NatTable.
     */
    protected abstract ICellPainter getSummaryRowCellPainter();

    /**
     * Returns the {@link IStyle} that should be used to render the selected
     * summary row cells in a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#SELECT} and the configuration label
     * {@link SummaryRowLayer#DEFAULT_SUMMARY_ROW_CONFIG_LABEL}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link IStyle} that should be used to render the selected
     *         summary row cells in a NatTable.
     */
    protected abstract IStyle getSummaryRowSelectionStyle();

    /**
     * Returns the {@link ICellPainter} that should be used to render the
     * selected summary row cells in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#SELECT} and the configuration label
     * {@link SummaryRowLayer#DEFAULT_SUMMARY_ROW_CONFIG_LABEL}.
     * </p>
     * <p>
     * If this method returns <code>null</code>, no value will be registered to
     * keep the IConfigRegistry clean. The result would be the same, as if no
     * value is found in the IConfigRegistry. In this case the rendering will
     * fallback to the default configuration.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the
     *         selected summary row cells in a NatTable.
     */
    protected abstract ICellPainter getSummaryRowSelectionCellPainter();

    /**
     * This method is used to register the style attributes for freeze
     * rendering. This mainly means to specify the color that is used to render
     * the freeze separator.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureFreezeStyle(IConfigRegistry configRegistry) {
        if (getFreezeSeparatorColor() != null) {
            configRegistry.registerConfigAttribute(
                    IFreezeConfigAttributes.SEPARATOR_COLOR,
                    getFreezeSeparatorColor());
        }
    }

    /**
     * Returns the {@link Color} that should be used to render the freeze
     * separator. If <code>null</code> is returned, the default separator color
     * will be used.
     *
     * @return The {@link Color} that should be used to render the freeze
     *         separator.
     */
    protected abstract Color getFreezeSeparatorColor();

    /**
     * This method is used to register the grid line styling, which consists of
     * grid line color and the configuration if grid lines should be rendered or
     * not. These configurations will be interpreted by the
     * GridLineCellLayerPainter.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureGridLineStyle(IConfigRegistry configRegistry) {
        if (getGridLineColor() != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.GRID_LINE_COLOR, getGridLineColor());
        }

        if (getRenderColumnHeaderGridLines() != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.RENDER_GRID_LINES,
                    getRenderColumnHeaderGridLines(), DisplayMode.NORMAL,
                    GridRegion.COLUMN_HEADER);
        }
        if (getRenderCornerGridLines() != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.RENDER_GRID_LINES,
                    getRenderCornerGridLines(), DisplayMode.NORMAL,
                    GridRegion.CORNER);
        }
        if (getRenderRowHeaderGridLines() != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.RENDER_GRID_LINES,
                    getRenderRowHeaderGridLines(), DisplayMode.NORMAL,
                    GridRegion.ROW_HEADER);
        }
        if (getRenderBodyGridLines() != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.RENDER_GRID_LINES,
                    getRenderBodyGridLines(), DisplayMode.NORMAL,
                    GridRegion.BODY);
        }
        if (getRenderFilterRowGridLines() != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.RENDER_GRID_LINES,
                    getRenderFilterRowGridLines(), DisplayMode.NORMAL,
                    GridRegion.FILTER_ROW);
        }
    }

    /**
     * Returns the {@link Color} that should be used to render the grid lines.
     * If <code>null</code> is returned, the default grid line color will be
     * used.
     *
     * @return The {@link Color} that should be used to render the grid lines.
     */
    protected abstract Color getGridLineColor();

    /**
     * Returns whether grid lines in the column header should be rendered or
     * not.
     * <p>
     * That means this configuration is registered against
     * {@link DisplayMode#NORMAL} and the configuration label
     * {@link GridRegion#COLUMN_HEADER}.
     * </p>
     *
     * @return <code>true</code> if grid lines in the column header region
     *         should be rendered, <code>false</code> if not.
     */
    protected abstract Boolean getRenderColumnHeaderGridLines();

    /**
     * Returns whether grid lines in the corner region should be rendered or
     * not.
     * <p>
     * That means this configuration is registered against
     * {@link DisplayMode#NORMAL} and the configuration label
     * {@link GridRegion#CORNER}.
     * </p>
     *
     * @return <code>true</code> if grid lines in the corner region should be
     *         rendered, <code>false</code> if not.
     */
    protected abstract Boolean getRenderCornerGridLines();

    /**
     * Returns whether grid lines in the row header should be rendered or not.
     * <p>
     * That means this configuration is registered against
     * {@link DisplayMode#NORMAL} and the configuration label
     * {@link GridRegion#ROW_HEADER}.
     * </p>
     *
     * @return <code>true</code> if grid lines in the row header region should
     *         be rendered, <code>false</code> if not.
     */
    protected abstract Boolean getRenderRowHeaderGridLines();

    /**
     * Returns whether grid lines in the body should be rendered or not.
     * <p>
     * That means this configuration is registered against
     * {@link DisplayMode#NORMAL} and the configuration label
     * {@link GridRegion#BODY}.
     * </p>
     *
     * @return <code>true</code> if grid lines in the body region should be
     *         rendered, <code>false</code> if not.
     */
    protected abstract Boolean getRenderBodyGridLines();

    /**
     * Returns whether grid lines in the filter row should be rendered or not.
     * <p>
     * That means this configuration is registered against
     * {@link DisplayMode#NORMAL} and the configuration label
     * {@link GridRegion#FILTER_ROW}.
     * </p>
     *
     * @return <code>true</code> if grid lines in the filter row should be
     *         rendered, <code>false</code> if not.
     */
    protected abstract Boolean getRenderFilterRowGridLines();

    /**
     * This method is used to register the styles that should be applied to an
     * editor control in case of conversion/validation errors.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureEditErrorStyle(IConfigRegistry configRegistry) {
        if (!isStyleEmpty(getConversionErrorStyle())) {
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CONVERSION_ERROR_STYLE,
                    getConversionErrorStyle(), DisplayMode.EDIT);
        }

        if (!isStyleEmpty(getValidationErrorStyle())) {
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.VALIDATION_ERROR_STYLE,
                    getValidationErrorStyle(), DisplayMode.EDIT);
        }
    }

    /**
     * Returns the {@link IStyle} that should be applied to an editor in case
     * the input is invalid in terms of conversion errors.
     * <p>
     * That means the {@link EditConfigAttributes#CONVERSION_ERROR_STYLE}
     * configuration is registered against {@link DisplayMode#EDIT}.
     * </p>
     * <p>
     * Note that only style informations for foreground colour, background
     * colour and font are interpreted, as the style informations will be
     * applied to the underlying editor control.
     * </p>
     * <p>
     * Currently mainly the TextCellEditor is using this {@link IStyle} via
     * RenderErrorHandling and the TableCellEditor.
     * </p>
     * <p>
     * If this method returns <code>null</code>, the default behaviour of using
     * a red foreground colour will be used on conversion errors.
     * </p>
     *
     * @return The {@link IStyle} that should be applied to an editor control in
     *         case of conversion errors.
     */
    protected abstract IStyle getConversionErrorStyle();

    /**
     * Returns the {@link IStyle} that should be applied to an editor in case
     * the input is invalid in terms of validation errors.
     * <p>
     * That means the {@link EditConfigAttributes#VALIDATION_ERROR_STYLE}
     * configuration is registered against {@link DisplayMode#EDIT}.
     * </p>
     * <p>
     * Note that only style informations for foreground colour, background
     * colour and font are interpreted, as the style informations will be
     * applied to the underlying editor control.
     * </p>
     * <p>
     * Currently mainly the TextCellEditor is using this {@link IStyle} via
     * RenderErrorHandling and the TableCellEditor.
     * </p>
     * <p>
     * If this method returns <code>null</code>, the default behaviour of using
     * a red foreground colour will be used on validation errors.
     * </p>
     *
     * @return The {@link IStyle} that should be applied to an editor control in
     *         case of validation errors.
     */
    protected abstract IStyle getValidationErrorStyle();

    /**
     * Register the style configurations to render the fill handle.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     * @since 1.5
     */
    protected void configureFillHandleStyle(IConfigRegistry configRegistry) {
        Color fillHandleColor = getFillHandleColor();
        if (fillHandleColor != null) {
            configRegistry.registerConfigAttribute(
                    FillHandleConfigAttributes.FILL_HANDLE_COLOR, fillHandleColor,
                    DisplayMode.NORMAL);
        }
        BorderStyle fillHandleBorderStyle = getFillHandleBorderStyle();
        if (fillHandleBorderStyle != null) {
            configRegistry.registerConfigAttribute(
                    FillHandleConfigAttributes.FILL_HANDLE_BORDER_STYLE, fillHandleBorderStyle,
                    DisplayMode.NORMAL);
        }
        BorderStyle fillHandleRegionBorderStyle = getFillHandleRegionBorderStyle();
        if (fillHandleRegionBorderStyle != null) {
            configRegistry.registerConfigAttribute(
                    FillHandleConfigAttributes.FILL_HANDLE_REGION_BORDER_STYLE, fillHandleRegionBorderStyle,
                    DisplayMode.NORMAL);
        }
    }

    /**
     * Returns the {@link Color} that should be used to render the fill handle.
     *
     * @return The {@link Color} that should be used to render the fill handle.
     * @since 1.5
     */
    // TODO Change it to abstract in next major version
    protected Color getFillHandleColor() {
        return null;
    }

    /**
     * Returns the {@link BorderStyle} that should be used to render the border
     * of the fill handle.
     *
     * @return The {@link BorderStyle} that should be used to render the border
     *         of the fill handle.
     * @since 1.5
     */
    // TODO Change it to abstract in next major version
    protected BorderStyle getFillHandleBorderStyle() {
        return null;
    }

    /**
     * Returns the {@link BorderStyle} that should be used to render the border
     * around the fill handle region.
     *
     * @return The {@link BorderStyle} that should be used to render the border
     *         around the fill handle region.
     * @since 1.5
     */
    // TODO Change it to abstract in next major version
    protected BorderStyle getFillHandleRegionBorderStyle() {
        return null;
    }

    /**
     * Register the style configurations to render the copy border.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     * @since 1.5
     */
    protected void configureCopyBorderStyle(IConfigRegistry configRegistry) {
        IStyle copyBorderStyle = getCopyBorderStyle();
        if (!isStyleEmpty(copyBorderStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, copyBorderStyle,
                    DisplayMode.NORMAL,
                    SelectionStyleLabels.COPY_BORDER_STYLE);
        }
    }

    /**
     * Returns the {@link IStyle} that should be used to render the copy border.
     * That means this style will be registered against the label
     * {@link SelectionStyleLabels#COPY_BORDER_STYLE}. Only the
     * {@link CellStyleAttributes#BORDER_STYLE} attribute will be interpreted.
     *
     * @return The {@link IStyle} that should be used to render the copy border.
     * @since 1.5
     */
    // TODO Change it to abstract in next major version
    protected IStyle getCopyBorderStyle() {
        return null;
    }

    /**
     * Null-safe check if a {@link IStyle} is empty or not.
     *
     * @param style
     *            The {@link IStyle} to check.
     * @return <code>true</code> if the given {@link IStyle} is
     *         <code>null</code> or has no value set for any
     *         {@link CellStyleAttributes}, <code>false</code> if at least one
     *         attribute is set.
     */
    public static boolean isStyleEmpty(IStyle style) {
        if (style == null) {
            return true;
        }

        if (style.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR) == null
                && style.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR) == null
                && style.getAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR) == null
                && style.getAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR) == null
                && style.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT) == null
                && style.getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT) == null
                && style.getAttributeValue(CellStyleAttributes.FONT) == null
                && style.getAttributeValue(CellStyleAttributes.IMAGE) == null
                && style.getAttributeValue(CellStyleAttributes.BORDER_STYLE) == null
                && style.getAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR) == null
                && style.getAttributeValue(CellStyleAttributes.TEXT_DECORATION) == null) {
            return true;
        }

        return false;
    }

    /**
     * Unregister the style configurations that were applied by this
     * ThemeConfiguration. This is necessary to ensure a clean IConfigRegistry
     * state before applying a new ThemeConfiguration. Otherwise it would be
     * possible to accidentally mix style configurations.
     * <p>
     * It will only unregister configurations that were applied by this
     * ThemeConfiguration. This means for every configuration a check is
     * performed whether there is a value configured in this theme or not. This
     * ensures to not removing configurations that are set somewhere else, e.g.
     * the ICellPainter that is used for the column group header is registered
     * internally by the layer itself, if the theme doesn't change it, it also
     * shouldn't unregister it in case of theme changes.
     * </p>
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configurations were applied to.
     */
    public void unregisterThemeStyleConfigurations(
            IConfigRegistry configRegistry) {

        // unregister default style configurations
        if (!isStyleEmpty(getDefaultCellStyle()))
            configRegistry
                    .unregisterConfigAttribute(CellConfigAttributes.CELL_STYLE);
        if (getDefaultCellPainter() != null)
            configRegistry
                    .unregisterConfigAttribute(CellConfigAttributes.CELL_PAINTER);

        // unregister default selection style configurations
        if (!isStyleEmpty(getDefaultSelectionCellStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT);
        if (getDefaultSelectionCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.SELECT);

        // unregister column header style configurations
        if (!isStyleEmpty(getColumnHeaderStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                    GridRegion.COLUMN_HEADER);
        if (getColumnHeaderCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL,
                    GridRegion.COLUMN_HEADER);

        // unregister column header selection style configurations
        if (!isStyleEmpty(getColumnHeaderSelectionStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                    GridRegion.COLUMN_HEADER);
        if (getColumnHeaderSelectionCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.SELECT,
                    GridRegion.COLUMN_HEADER);
        if (!isStyleEmpty(getColumnHeaderFullSelectionStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                    SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE);
        if (getColumnHeaderFullSelectionCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.SELECT,
                    SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE);

        // unregister row header style configurations
        if (!isStyleEmpty(getRowHeaderStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                    GridRegion.ROW_HEADER);
        if (getRowHeaderCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL,
                    GridRegion.ROW_HEADER);

        // unregister row header selection style configurations
        if (!isStyleEmpty(getRowHeaderSelectionStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                    GridRegion.ROW_HEADER);
        if (getRowHeaderSelectionCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.SELECT,
                    GridRegion.ROW_HEADER);
        if (!isStyleEmpty(getRowHeaderFullSelectionStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                    SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE);
        if (getRowHeaderFullSelectionCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.SELECT,
                    SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE);

        // unregister corner style configurations
        if (!isStyleEmpty(getCornerStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                    GridRegion.CORNER);
        if (getCornerCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL,
                    GridRegion.CORNER);

        // unregister corner header selection style configurations
        if (!isStyleEmpty(getCornerSelectionStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                    GridRegion.CORNER);
        if (getCornerSelectionCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.SELECT,
                    GridRegion.CORNER);

        // unregister hover styling
        if (!isStyleEmpty(getDefaultHoverStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.HOVER);
        if (getDefaultHoverCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.HOVER);
        if (!isStyleEmpty(getBodyHoverStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.HOVER,
                    GridRegion.BODY);
        if (getBodyHoverCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.HOVER,
                    GridRegion.BODY);
        if (!isStyleEmpty(getColumnHeaderHoverStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.HOVER,
                    GridRegion.COLUMN_HEADER);
        if (getColumnHeaderHoverCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.HOVER,
                    GridRegion.COLUMN_HEADER);
        if (!isStyleEmpty(getRowHeaderHoverStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.HOVER,
                    GridRegion.ROW_HEADER);
        if (getRowHeaderHoverCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.HOVER,
                    GridRegion.ROW_HEADER);

        // unregister hover selection styling
        if (!isStyleEmpty(getDefaultHoverSelectionStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT_HOVER);
        if (getDefaultHoverSelectionCellPainter() != null)
            configRegistry
                    .unregisterConfigAttribute(
                            CellConfigAttributes.CELL_PAINTER,
                            DisplayMode.SELECT_HOVER);
        if (!isStyleEmpty(getBodyHoverSelectionStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT_HOVER,
                    GridRegion.BODY);
        if (getBodyHoverSelectionCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    DisplayMode.SELECT_HOVER, GridRegion.BODY);
        if (!isStyleEmpty(getColumnHeaderHoverSelectionStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT_HOVER,
                    GridRegion.COLUMN_HEADER);
        if (getColumnHeaderHoverSelectionCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    DisplayMode.SELECT_HOVER, GridRegion.COLUMN_HEADER);
        if (!isStyleEmpty(getRowHeaderHoverSelectionStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT_HOVER,
                    GridRegion.ROW_HEADER);
        if (getRowHeaderHoverSelectionCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    DisplayMode.SELECT_HOVER, GridRegion.ROW_HEADER);

        // unregister selection anchor style configuration
        if (!isStyleEmpty(getSelectionAnchorStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                    SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
        if (getSelectionAnchorCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL,
                    SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
        if (!isStyleEmpty(getSelectionAnchorSelectionStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                    SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
        if (getSelectionAnchorSelectionCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.SELECT,
                    SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
        if (!isStyleEmpty(getSelectionAnchorGridLineStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                    SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE);

        // unregister alternating row style configuration
        if (!isStyleEmpty(getEvenRowStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
        if (getEvenRowCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);

        if (!isStyleEmpty(getOddRowStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);
        if (getOddRowCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);

        // unregister column group header style configuration
        if (!isStyleEmpty(getColumnGroupHeaderStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                    GridRegion.COLUMN_GROUP_HEADER);
        if (getColumnGroupHeaderCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL,
                    GridRegion.COLUMN_GROUP_HEADER);

        // unregister row group header style configuration
        if (!isStyleEmpty(getRowGroupHeaderStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                    GridRegion.ROW_GROUP_HEADER);
        if (getRowGroupHeaderCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL,
                    GridRegion.ROW_GROUP_HEADER);

        // unregister sort header style configuration
        if (!isStyleEmpty(getSortHeaderStyle())) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                    DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                    DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
        }
        if (getSortHeaderCellPainter() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL,
                    DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL,
                    DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
        }

        if (!isStyleEmpty(getSelectedSortHeaderStyle())) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                    DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                    DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
        }
        if (getSelectedSortHeaderCellPainter() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.SELECT,
                    DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.SELECT,
                    DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
        }

        // unregister filter row style configuration
        if (!isStyleEmpty(getFilterRowStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                    GridRegion.FILTER_ROW);
        if (getFilterRowCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL,
                    GridRegion.FILTER_ROW);

        // unregister tree style configuration
        if (!isStyleEmpty(getTreeStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                    TreeLayer.TREE_COLUMN_CELL);
        if (getTreeCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL,
                    TreeLayer.TREE_COLUMN_CELL);
        if (!isStyleEmpty(getTreeSelectionStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                    TreeLayer.TREE_COLUMN_CELL);
        if (getTreeSelectionCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.SELECT,
                    TreeLayer.TREE_COLUMN_CELL);
        if (getTreeStructurePainter() != null)
            configRegistry.unregisterConfigAttribute(
                    TreeConfigAttributes.TREE_STRUCTURE_PAINTER,
                    DisplayMode.NORMAL);
        if (getTreeStructureSelectionPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    TreeConfigAttributes.TREE_STRUCTURE_PAINTER,
                    DisplayMode.SELECT);

        // unregister summary row style configuration
        if (!isStyleEmpty(getSummaryRowStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                    SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
        if (getSummaryRowCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL,
                    SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
        if (!isStyleEmpty(getSummaryRowSelectionStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                    SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
        if (getSummaryRowSelectionCellPainter() != null)
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, DisplayMode.SELECT,
                    SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);

        // unregister freeze separator color
        if (getFreezeSeparatorColor() != null) {
            configRegistry
                    .unregisterConfigAttribute(IFreezeConfigAttributes.SEPARATOR_COLOR);
        }

        // unregister grid line configuration
        if (getGridLineColor() != null) {
            configRegistry
                    .unregisterConfigAttribute(CellConfigAttributes.GRID_LINE_COLOR);
        }

        // unregister grid line rendering
        if (getRenderColumnHeaderGridLines() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.RENDER_GRID_LINES, DisplayMode.NORMAL,
                    GridRegion.COLUMN_HEADER);
        }
        if (getRenderCornerGridLines() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.RENDER_GRID_LINES, DisplayMode.NORMAL,
                    GridRegion.CORNER);
        }
        if (getRenderRowHeaderGridLines() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.RENDER_GRID_LINES, DisplayMode.NORMAL,
                    GridRegion.ROW_HEADER);
        }
        if (getRenderBodyGridLines() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.RENDER_GRID_LINES, DisplayMode.NORMAL,
                    GridRegion.BODY);
        }
        if (getRenderFilterRowGridLines() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.RENDER_GRID_LINES, DisplayMode.NORMAL,
                    GridRegion.FILTER_ROW);
        }

        // unregister conversion/validation style configuration
        if (getConversionErrorStyle() != null) {
            configRegistry.unregisterConfigAttribute(
                    EditConfigAttributes.CONVERSION_ERROR_STYLE,
                    DisplayMode.EDIT);
        }
        if (getValidationErrorStyle() != null) {
            configRegistry.unregisterConfigAttribute(
                    EditConfigAttributes.VALIDATION_ERROR_STYLE,
                    DisplayMode.EDIT);
        }

        // unregister fill handle style configuration
        if (getFillHandleColor() != null) {
            configRegistry.unregisterConfigAttribute(
                    FillHandleConfigAttributes.FILL_HANDLE_COLOR,
                    DisplayMode.NORMAL, SelectionStyleLabels.FILL_HANDLE_CELL);
        }
        if (getFillHandleBorderStyle() != null) {
            configRegistry.unregisterConfigAttribute(
                    FillHandleConfigAttributes.FILL_HANDLE_BORDER_STYLE,
                    DisplayMode.NORMAL, SelectionStyleLabels.FILL_HANDLE_CELL);
        }
        if (getFillHandleRegionBorderStyle() != null) {
            configRegistry.unregisterConfigAttribute(
                    FillHandleConfigAttributes.FILL_HANDLE_REGION_BORDER_STYLE,
                    DisplayMode.NORMAL, SelectionStyleLabels.FILL_HANDLE_REGION);
        }

        // unregister copy border style configuration
        if (!isStyleEmpty(getCopyBorderStyle()))
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                    SelectionStyleLabels.COPY_BORDER_STYLE);

        // unregister possible extension styles
        for (IThemeExtension extension : this.extensions) {
            extension.unregisterStyles(configRegistry);
        }

    }

    /**
     * Adds an IThemeExtension to this ThemeConfiguration which adds additional
     * styling configuration on top.
     *
     * @param extension
     *            The IThemeExtension that should be added to this
     *            ThemeConfiguration.
     */
    public void addThemeExtension(IThemeExtension extension) {
        this.extensions.add(extension);
    }

    /**
     * Removes an IThemeExtension that was added to this ThemeConfiguration
     * before.
     *
     * @param extension
     *            The IThemeExtension that should be removed from this
     *            ThemeConfiguration.
     */
    public void removeThemeExtension(IThemeExtension extension) {
        this.extensions.remove(extension);
    }
}

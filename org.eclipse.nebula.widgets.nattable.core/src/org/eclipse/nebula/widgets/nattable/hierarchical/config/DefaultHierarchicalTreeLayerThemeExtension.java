/*******************************************************************************
 * Copyright (c) 2020 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical.config;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeAlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.TextDecorationEnum;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.theme.IThemeExtension;
import org.eclipse.nebula.widgets.nattable.style.theme.ThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.config.TreeConfigAttributes;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.nebula.widgets.nattable.tree.painter.TreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * {@link IThemeExtension} that adds styling configurations for the
 * {@link HierarchicalTreeLayer}.
 * <p>
 * The theme styling for the {@link HierarchicalTreeLayer} is implemented as a
 * {@link IThemeExtension} because the styling configurations for trees collide
 * with the default tree styling configurations. It is not possible to combine
 * the styling of a standard tree and a hierarchical tree.
 * </p>
 *
 * @since 2.0
 */
public class DefaultHierarchicalTreeLayerThemeExtension implements IThemeExtension {

    // default cell style

    public Color defaultBgColor = GUIHelper.COLOR_WHITE;
    public Color defaultFgColor = GUIHelper.COLOR_BLACK;
    public Color defaultGradientBgColor = GUIHelper.COLOR_WHITE;
    public Color defaultGradientFgColor = GUIHelper.getColor(136, 212, 215);
    public HorizontalAlignmentEnum defaultHAlign = HorizontalAlignmentEnum.LEFT;
    public VerticalAlignmentEnum defaultVAlign = VerticalAlignmentEnum.TOP;
    public Font defaultFont = GUIHelper.DEFAULT_FONT;
    public Image defaultImage = null;
    public BorderStyle defaultBorderStyle = null;
    public Character defaultPWEchoChar = null;
    public TextDecorationEnum defaultTextDecoration = null;

    public ICellPainter defaultCellPainter = null;

    // NORMAL tree style
    public Color treeBgColor = null;
    public Color treeFgColor = null;
    public Color treeGradientBgColor = null;
    public Color treeGradientFgColor = null;
    public HorizontalAlignmentEnum treeHAlign = HorizontalAlignmentEnum.LEFT;
    public VerticalAlignmentEnum treeVAlign = VerticalAlignmentEnum.TOP;
    public Font treeFont = null;
    public Image treeImage = null;
    public BorderStyle treeBorderStyle = null;
    public Character treePWEchoChar = null;
    public TextDecorationEnum treeTextDecoration = null;

    // SELECT tree style
    public Color treeSelectionBgColor = null;
    public Color treeSelectionFgColor = null;
    public Color treeSelectionGradientBgColor = null;
    public Color treeSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum treeSelectionHAlign = HorizontalAlignmentEnum.LEFT;
    public VerticalAlignmentEnum treeSelectionVAlign = VerticalAlignmentEnum.TOP;
    public Font treeSelectionFont = null;
    public Image treeSelectionImage = null;
    public BorderStyle treeSelectionBorderStyle = null;
    public Character treeSelectionPWEchoChar = null;
    public TextDecorationEnum treeSelectionTextDecoration = null;

    // NORMAL level header style
    public Color levelHeaderBgColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
    public Color levelHeaderFgColor = null;
    public Color levelHeaderGradientBgColor = null;
    public Color levelHeaderGradientFgColor = null;
    public HorizontalAlignmentEnum levelHeaderHAlign = null;
    public VerticalAlignmentEnum levelHeaderVAlign = null;
    public Font levelHeaderFont = null;
    public Image levelHeaderImage = null;
    public BorderStyle levelHeaderBorderStyle = null;
    public Character levelHeaderPWEchoChar = null;
    public TextDecorationEnum levelHeaderTextDecoration = null;

    // SELECT level header style
    public Color levelHeaderSelectionBgColor = GUIHelper.COLOR_GRAY;
    public Color levelHeaderSelectionFgColor = null;
    public Color levelHeaderSelectionGradientBgColor = null;
    public Color levelHeaderSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum levelHeaderSelectionHAlign = null;
    public VerticalAlignmentEnum levelHeaderSelectionVAlign = null;
    public Font levelHeaderSelectionFont = null;
    public Image levelHeaderSelectionImage = null;
    public BorderStyle levelHeaderSelectionBorderStyle = null;
    public Character levelHeaderSelectionPWEchoChar = null;
    public TextDecorationEnum levelHeaderSelectionTextDecoration = null;

    // alternating row style

    public Color evenRowBgColor = GUIHelper.COLOR_WHITE;
    public Color evenRowFgColor = null;
    public Color evenRowGradientBgColor = null;
    public Color evenRowGradientFgColor = null;
    public HorizontalAlignmentEnum evenRowHAlign = null;
    public VerticalAlignmentEnum evenRowVAlign = null;
    public Font evenRowFont = null;
    public Image evenRowImage = null;
    public BorderStyle evenRowBorderStyle = null;
    public Character evenRowPWEchoChar = null;
    public TextDecorationEnum evenRowTextDecoration = null;

    public Color oddRowBgColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
    public Color oddRowFgColor = null;
    public Color oddRowGradientBgColor = null;
    public Color oddRowGradientFgColor = null;
    public HorizontalAlignmentEnum oddRowHAlign = null;
    public VerticalAlignmentEnum oddRowVAlign = null;
    public Font oddRowFont = null;
    public Image oddRowImage = null;
    public BorderStyle oddRowBorderStyle = null;
    public Character oddRowPWEchoChar = null;
    public TextDecorationEnum oddRowTextDecoration = null;

    public ICellPainter treeCellPainter = null;
    public ICellPainter treeSelectionCellPainter = null;

    public ICellPainter treeStructurePainter = null;
    public ICellPainter treeStructureSelectionPainter = null;

    public ICellPainter collapsedChildPainter = null;

    public ICellPainter levelHeaderCellPainter = null;
    public ICellPainter levelHeaderSelectionCellPainter = null;

    public ICellPainter evenRowCellPainter = null;
    public ICellPainter oddRowCellPainter = null;

    @Override
    public void createPainterInstances() {
        // configure the default cell painter
        this.defaultCellPainter = new PaddingDecorator(new TextPainter(), 2);

        // configure the tree structure painter
        ICellPainter treeImagePainter = new PaddingDecorator(
                new TreeImagePainter(),
                5, 2, 5, 2);

        IndentedTreeImagePainter treePainter = new IndentedTreeImagePainter(0, CellEdgeEnum.TOP_LEFT, treeImagePainter);
        treePainter.getInternalPainter().setPaintDecorationDependent(false);

        this.treeStructurePainter = new BackgroundPainter(treePainter);

        // configure the style and the cell painter for the tree/node columns
        // necessary because the IndentedTreeImagePainter is inspecting and
        // using the underlying painter
        this.treeCellPainter = new PaddingDecorator(new TextPainter(), 2, 2, 2, 15);

        this.collapsedChildPainter = new BackgroundPainter();
    }

    @Override
    public void registerStyles(IConfigRegistry configRegistry) {
        // create the painter instances before they are registered.
        createPainterInstances();

        configureDefaultStyle(configRegistry);
        configureTreeStyle(configRegistry);
        configureLevelHeaderStyle(configRegistry);
        configureAlternatingRowStyle(configRegistry);
    }

    /**
     * Register default style configurations. Typically these configurations are
     * used be the body region and will be overridden by more specific
     * configurations of the header regions or custom styling based on labels.
     * <p>
     * It is necessary to override the default configuration provided by the
     * base {@link ThemeConfiguration} to render the hierarchical representation
     * correctly. Especially the horizontal and vertical alignment need to be
     * adjusted from the default.
     * </p>
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureDefaultStyle(IConfigRegistry configRegistry) {
        IStyle defaultStyle = getDefaultCellStyle();
        if (!ThemeConfiguration.isStyleEmpty(defaultStyle)) {
            // register body cell style for every display mode
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    defaultStyle);
        }

        ICellPainter defaultPainter = getDefaultCellPainter();
        if (defaultPainter != null) {
            // register body cell painter for every display mode
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    defaultPainter);
        }
    }

    /**
     * @return The {@link IStyle} that should be used by default to render
     *         NatTable.
     */
    protected IStyle getDefaultCellStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.defaultBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.defaultFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.defaultGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.defaultGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.defaultHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.defaultVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.defaultFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.defaultImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.defaultBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.defaultPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.defaultTextDecoration);
        return cellStyle;
    }

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
    protected ICellPainter getDefaultCellPainter() {
        return this.defaultCellPainter;
    }

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
        if (!ThemeConfiguration.isStyleEmpty(treeStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    treeStyle,
                    DisplayMode.NORMAL,
                    TreeLayer.TREE_COLUMN_CELL);
        }

        ICellPainter cellPainter = getTreeCellPainter();
        if (cellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    cellPainter,
                    DisplayMode.NORMAL,
                    TreeLayer.TREE_COLUMN_CELL);
        }

        IStyle treeSelectionStyle = getTreeSelectionStyle();
        if (!ThemeConfiguration.isStyleEmpty(treeSelectionStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    treeSelectionStyle,
                    DisplayMode.SELECT,
                    TreeLayer.TREE_COLUMN_CELL);
        }

        ICellPainter selectionCellPainter = getTreeSelectionCellPainter();
        if (selectionCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    selectionCellPainter,
                    DisplayMode.SELECT,
                    TreeLayer.TREE_COLUMN_CELL);
        }

        ICellPainter treePainter = getTreeStructurePainter();
        if (treePainter != null) {
            configRegistry.registerConfigAttribute(
                    TreeConfigAttributes.TREE_STRUCTURE_PAINTER,
                    treePainter,
                    DisplayMode.NORMAL);
        }

        ICellPainter treeSelectionPainter = getTreeStructureSelectionPainter();
        if (treeSelectionPainter != null) {
            configRegistry.registerConfigAttribute(
                    TreeConfigAttributes.TREE_STRUCTURE_PAINTER,
                    treeSelectionPainter,
                    DisplayMode.SELECT);
        }

        ICellPainter collapsedChildPainter = getCollapsedChildPainter();
        if (collapsedChildPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    collapsedChildPainter,
                    DisplayMode.NORMAL,
                    HierarchicalTreeLayer.COLLAPSED_CHILD);
        }
    }

    /**
     * By default the levels in a {@link HierarchicalTreeLayer} are separated by
     * additional level header columns. Those columns are marked with the label
     * {@link HierarchicalTreeLayer#LEVEL_HEADER_CELL}. In this method the
     * styling for those cells can be configured.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureLevelHeaderStyle(IConfigRegistry configRegistry) {

        // configure styling for tree level header
        IStyle levelHeaderStyle = getLevelHeaderStyle();
        if (!ThemeConfiguration.isStyleEmpty(levelHeaderStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    levelHeaderStyle,
                    DisplayMode.NORMAL,
                    HierarchicalTreeLayer.LEVEL_HEADER_CELL);
        }

        ICellPainter levelHeaderCellPainter = getLevelHeaderCellPainter();
        if (levelHeaderCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    levelHeaderCellPainter,
                    DisplayMode.NORMAL,
                    HierarchicalTreeLayer.LEVEL_HEADER_CELL);
        }

        IStyle levelHeaderSelectedStyle = getLevelHeaderSelectionStyle();
        if (!ThemeConfiguration.isStyleEmpty(levelHeaderSelectedStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    levelHeaderSelectedStyle,
                    DisplayMode.SELECT,
                    HierarchicalTreeLayer.LEVEL_HEADER_CELL);
        }

        ICellPainter levelHeaderSelectionCellPainter = getLevelHeaderSelectionCellPainter();
        if (levelHeaderSelectionCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    levelHeaderSelectionCellPainter,
                    DisplayMode.SELECT,
                    HierarchicalTreeLayer.LEVEL_HEADER_CELL);
        }

    }

    /**
     * When creating a {@link HierarchicalTreeLayer}, using the
     * {@link DefaultHierarchicalTreeLayerConfiguration}, the
     * {@link HierarchicalTreeAlternatingRowConfigLabelAccumulator} is
     * configured for the body region to apply labels for alternating rows. It
     * applies the following labels for which this method registers styles:
     * <ul>
     * <li>{@link AlternatingRowConfigLabelAccumulator#EVEN_ROW_CONFIG_TYPE}
     * </li>
     * <li>{@link AlternatingRowConfigLabelAccumulator#EVEN_ROW_CONFIG_TYPE}
     * </li>
     * </ul>
     * If the {@link HierarchicalTreeAlternatingRowConfigLabelAccumulator} is
     * not configured, this style configuration will have no effect.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configuration should be applied to.
     */
    protected void configureAlternatingRowStyle(IConfigRegistry configRegistry) {
        IStyle evenStyle = getEvenRowStyle();
        if (!ThemeConfiguration.isStyleEmpty(evenStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    evenStyle,
                    DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
        }

        ICellPainter evenCellPainter = getEvenRowCellPainter();
        if (evenCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    evenCellPainter,
                    DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
        }

        IStyle oddStyle = getOddRowStyle();
        if (!ThemeConfiguration.isStyleEmpty(oddStyle)) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    oddStyle,
                    DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);
        }

        ICellPainter oddCellPainter = getOddRowCellPainter();
        if (oddCellPainter != null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    oddCellPainter,
                    DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);
        }
    }

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
    protected ICellPainter getTreeCellPainter() {
        return this.treeCellPainter;
    }

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
    protected ICellPainter getTreeSelectionCellPainter() {
        return this.treeSelectionCellPainter;
    }

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
    protected ICellPainter getTreeStructurePainter() {
        return this.treeStructurePainter;
    }

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
    protected ICellPainter getTreeStructureSelectionPainter() {
        return this.treeStructureSelectionPainter;
    }

    /**
     * Returns the {@link ICellPainter} that should be used to render the
     * content in cells of collapsed childs, e.g. an empty painter to paint
     * empty cells.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#NORMAL} and the configuration label
     * {@link HierarchicalTreeLayer#COLLAPSED_CHILD}.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the tree
     *         column in a NatTable.
     */
    protected ICellPainter getCollapsedChildPainter() {
        return this.collapsedChildPainter;
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
    protected IStyle getTreeStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.treeBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.treeFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.treeGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.treeGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.treeHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.treeVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.treeFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.treeImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.treeBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.treePWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.treeTextDecoration);
        return cellStyle;
    }

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
    protected IStyle getTreeSelectionStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.treeSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.treeSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.treeSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.treeSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.treeSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.treeSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.treeSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.treeSelectionImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.treeSelectionBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.treeSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.treeSelectionTextDecoration);
        return cellStyle;
    }

    /**
     * Returns the {@link IStyle} that should be used to render the level header
     * column cells in a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#NORMAL} and the configuration label
     * {@link HierarchicalTreeLayer#LEVEL_HEADER_CELL}.
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
    protected IStyle getLevelHeaderStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.levelHeaderBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.levelHeaderFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.levelHeaderGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.levelHeaderGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.levelHeaderHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.levelHeaderVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.levelHeaderFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.levelHeaderImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.levelHeaderBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.levelHeaderPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.levelHeaderTextDecoration);
        return cellStyle;
    }

    /**
     * Returns the {@link ICellPainter} that should be used to render the tree
     * column cells in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#NORMAL} and the configuration label
     * {@link HierarchicalTreeLayer#LEVEL_HEADER_CELL}.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the tree
     *         column in a NatTable.
     */
    protected ICellPainter getLevelHeaderCellPainter() {
        return this.levelHeaderCellPainter;
    }

    /**
     * Returns the {@link IStyle} that should be used to render the selected
     * level header column cells in a NatTable.
     * <p>
     * That means this {@link IStyle} is registered against
     * {@link DisplayMode#SELECT} and the configuration label
     * {@link HierarchicalTreeLayer#LEVEL_HEADER_CELL}.
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
    protected IStyle getLevelHeaderSelectionStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.levelHeaderSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.levelHeaderSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.levelHeaderSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.levelHeaderSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.levelHeaderSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.levelHeaderSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.levelHeaderSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.levelHeaderSelectionImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.levelHeaderSelectionBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.levelHeaderSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.levelHeaderSelectionTextDecoration);
        return cellStyle;
    }

    /**
     * Returns the {@link ICellPainter} that should be used to render the
     * selected level header column cells in a NatTable.
     * <p>
     * That means this {@link ICellPainter} is registered against
     * {@link DisplayMode#SELECT} and the configuration label
     * {@link HierarchicalTreeLayer#LEVEL_HEADER_CELL}.
     * </p>
     *
     * @return The {@link ICellPainter} that should be used to render the
     *         selected tree column in a NatTable.
     */
    protected ICellPainter getLevelHeaderSelectionCellPainter() {
        return this.levelHeaderSelectionCellPainter;
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
    protected IStyle getEvenRowStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.evenRowBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.evenRowFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.evenRowGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.evenRowGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.evenRowHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.evenRowVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.evenRowFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.evenRowImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.evenRowBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.evenRowPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.evenRowTextDecoration);
        return cellStyle;
    }

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
    protected ICellPainter getEvenRowCellPainter() {
        return this.evenRowCellPainter;
    }

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
    protected IStyle getOddRowStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.oddRowBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.oddRowFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.oddRowGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.oddRowGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.oddRowHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.oddRowVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.oddRowFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.oddRowImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.oddRowBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.oddRowPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.oddRowTextDecoration);
        return cellStyle;
    }

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
    protected ICellPainter getOddRowCellPainter() {
        return this.oddRowCellPainter;
    }

    @Override
    public void unregisterStyles(IConfigRegistry configRegistry) {
        if (!ThemeConfiguration.isStyleEmpty(getDefaultCellStyle())) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE);
        }

        if (getDefaultCellPainter() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER);
        }

        if (!ThemeConfiguration.isStyleEmpty(getTreeStyle())) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    DisplayMode.NORMAL,
                    TreeLayer.TREE_COLUMN_CELL);
        }

        if (getTreeCellPainter() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    DisplayMode.NORMAL,
                    TreeLayer.TREE_COLUMN_CELL);
        }

        if (!ThemeConfiguration.isStyleEmpty(getTreeSelectionStyle())) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    DisplayMode.SELECT,
                    TreeLayer.TREE_COLUMN_CELL);
        }

        if (getTreeSelectionCellPainter() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    DisplayMode.SELECT,
                    TreeLayer.TREE_COLUMN_CELL);
        }

        if (getTreeStructurePainter() != null) {
            configRegistry.unregisterConfigAttribute(
                    TreeConfigAttributes.TREE_STRUCTURE_PAINTER,
                    DisplayMode.NORMAL);
        }

        if (getTreeStructureSelectionPainter() != null) {
            configRegistry.unregisterConfigAttribute(
                    TreeConfigAttributes.TREE_STRUCTURE_PAINTER,
                    DisplayMode.SELECT);
        }

        if (getCollapsedChildPainter() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    DisplayMode.NORMAL,
                    HierarchicalTreeLayer.COLLAPSED_CHILD);
        }

        // unregister styling for level header
        if (!ThemeConfiguration.isStyleEmpty(getLevelHeaderStyle())) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    DisplayMode.NORMAL,
                    HierarchicalTreeLayer.LEVEL_HEADER_CELL);
        }

        if (getLevelHeaderCellPainter() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    DisplayMode.NORMAL,
                    HierarchicalTreeLayer.LEVEL_HEADER_CELL);
        }

        if (!ThemeConfiguration.isStyleEmpty(getLevelHeaderSelectionStyle())) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    DisplayMode.SELECT,
                    HierarchicalTreeLayer.LEVEL_HEADER_CELL);
        }

        if (getLevelHeaderSelectionCellPainter() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    DisplayMode.SELECT,
                    HierarchicalTreeLayer.LEVEL_HEADER_CELL);
        }

        // unregister alternating row style configuration
        if (!ThemeConfiguration.isStyleEmpty(getEvenRowStyle())) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
        }

        if (getEvenRowCellPainter() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
        }

        if (!ThemeConfiguration.isStyleEmpty(getOddRowStyle())) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);
        }

        if (getOddRowCellPainter() != null) {
            configRegistry.unregisterConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    DisplayMode.NORMAL,
                    AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);
        }
    }
}

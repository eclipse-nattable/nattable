/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical.config;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.EditableRule;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeAlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.hierarchical.action.HierarchicalTreeColumnReorderDragMode;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.action.TreeExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.tree.config.TreeConfigAttributes;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.nebula.widgets.nattable.tree.painter.TreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.action.AggregateDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.CellDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * The default configuration for the {@link HierarchicalTreeLayer}. Adds default
 * styling and default bindings.
 *
 * @see HierarchicalTreeLayer
 *
 * @since 1.6
 */
public class DefaultHierarchicalTreeLayerConfiguration implements IConfiguration {

    protected HierarchicalTreeLayer treeLayer;

    public Color evenRowBgColor = GUIHelper.COLOR_WHITE;
    public Color oddRowBgColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
    public Color levelHeaderColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
    public Color levelHeaderSelectedColor = GUIHelper.COLOR_GRAY;

    /**
     * The {@link HierarchicalTreeLayer} which should be configured by this
     * configuration.
     */
    public DefaultHierarchicalTreeLayerConfiguration(HierarchicalTreeLayer treeLayer) {
        this.treeLayer = treeLayer;
    }

    @Override
    public void configureLayer(ILayer layer) {
        HierarchicalTreeAlternatingRowConfigLabelAccumulator accumulator =
                new HierarchicalTreeAlternatingRowConfigLabelAccumulator(this.treeLayer);
        this.treeLayer.setConfigLabelAccumulator(accumulator);
        this.treeLayer.addLayerListener(accumulator);
        this.treeLayer.registerCommandHandler(accumulator);
        accumulator.calculateLabels();
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        // configure the tree structure painter
        ICellPainter treeImagePainter = new PaddingDecorator(
                new TreeImagePainter(),
                5, 2, 5, 2);

        IndentedTreeImagePainter treePainter = new IndentedTreeImagePainter(0, CellEdgeEnum.TOP_LEFT, treeImagePainter);
        treePainter.getInternalPainter().setPaintDecorationDependent(false);

        ICellPainter treeStructurePainter = new BackgroundPainter(treePainter);
        configRegistry.registerConfigAttribute(
                TreeConfigAttributes.TREE_STRUCTURE_PAINTER,
                treeStructurePainter,
                DisplayMode.NORMAL);

        // configure the style and the cell painter for the tree/node columns
        // necessary because the IndentedTreeImagePainter is inspecting and
        // using the underlying painter
        ICellPainter basePainter = new PaddingDecorator(new TextPainter(), 2, 2, 2, 15);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER,
                basePainter,
                DisplayMode.NORMAL,
                TreeLayer.TREE_COLUMN_CELL);

        Style treeStyle = new Style();
        treeStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                VerticalAlignmentEnum.TOP);
        treeStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                HorizontalAlignmentEnum.LEFT);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                treeStyle,
                DisplayMode.NORMAL,
                TreeLayer.TREE_COLUMN_CELL);

        // configure styling for tree level header
        Style levelHeaderStyle = new Style();
        levelHeaderStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.levelHeaderColor);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                levelHeaderStyle,
                DisplayMode.NORMAL,
                HierarchicalTreeLayer.LEVEL_HEADER_CELL);

        Style levelHeaderSelectedStyle = new Style();
        levelHeaderSelectedStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.levelHeaderSelectedColor);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                levelHeaderSelectedStyle,
                DisplayMode.SELECT,
                HierarchicalTreeLayer.LEVEL_HEADER_CELL);

        // register special empty painter to not render content in collapsed
        // childs
        // this also allows for example some different styling of collapsed
        // childs
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER,
                new BackgroundPainter(),
                DisplayMode.NORMAL,
                HierarchicalTreeLayer.COLLAPSED_CHILD);

        // configure alternate row style
        Style evenRowCellStyle = new Style();
        evenRowCellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.evenRowBgColor);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                evenRowCellStyle,
                DisplayMode.NORMAL,
                AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);

        Style oddRowCellStyle = new Style();
        oddRowCellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.oddRowBgColor);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                oddRowCellStyle,
                DisplayMode.NORMAL,
                AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);

        configureEditableRules(configRegistry);
    }

    /**
     * Configures the editable rules for the level header column cells and the
     * collapsed childs.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} to which the configurations should
     *            be applied.
     */
    protected void configureEditableRules(IConfigRegistry configRegistry) {
        // disable editing always for level header columns
        configRegistry.registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                EditableRule.NEVER_EDITABLE,
                DisplayMode.NORMAL,
                HierarchicalTreeLayer.LEVEL_HEADER_CELL);

        // disable editing always for collapsed childs
        configRegistry.registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                EditableRule.NEVER_EDITABLE,
                DisplayMode.NORMAL,
                HierarchicalTreeLayer.COLLAPSED_CHILD);

        // disable editing always for empty childs
        configRegistry.registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                EditableRule.NEVER_EDITABLE,
                DisplayMode.NORMAL,
                HierarchicalTreeLayer.NO_OBJECT_IN_LEVEL);
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        TreeExpandCollapseAction treeExpandCollapseAction = new TreeExpandCollapseAction();

        CellPainterMouseEventMatcher treeImagePainterMouseEventMatcher =
                new CellPainterMouseEventMatcher(
                        GridRegion.BODY,
                        MouseEventMatcher.LEFT_BUTTON,
                        TreeImagePainter.class);

        uiBindingRegistry.registerFirstSingleClickBinding(
                treeImagePainterMouseEventMatcher, treeExpandCollapseAction);

        // Obscure any mouse down bindings for this image painter
        uiBindingRegistry.registerFirstMouseDownBinding(
                treeImagePainterMouseEventMatcher, new NoOpMouseAction());

        // configure a specialized ColumnReorderDragMode
        uiBindingRegistry.registerMouseDragMode(
                MouseEventMatcher.columnHeaderLeftClick(SWT.NONE),
                new AggregateDragMode(
                        new CellDragMode(),
                        new HierarchicalTreeColumnReorderDragMode(this.treeLayer)));
    }

}

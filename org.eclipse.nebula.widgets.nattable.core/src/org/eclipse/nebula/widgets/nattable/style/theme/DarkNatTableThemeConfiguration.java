/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style.theme;

import org.eclipse.nebula.widgets.nattable.group.painter.ColumnGroupExpandCollapseImagePainter;
import org.eclipse.nebula.widgets.nattable.group.painter.ColumnGroupHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.group.painter.RowGroupExpandCollapseImagePainter;
import org.eclipse.nebula.widgets.nattable.group.painter.RowGroupHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.GradientBackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.VerticalTextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortIconPainter;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.nebula.widgets.nattable.tree.painter.TreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

/**
 * Theme configuration that overrides the stylings set in the
 * {@link DefaultNatTableThemeConfiguration} to give the NatTable a dark modern
 * look &amp; feel.
 *
 * <p>
 * This default theme configuration does not set a different value for the
 * filter row cell painter, as in this case it would override the specialized
 * painter that needs to be registered for combobox filterrows.
 * </p>
 */
public class DarkNatTableThemeConfiguration extends ModernNatTableThemeConfiguration {
    {
        this.defaultBgColor = GUIHelper.COLOR_BLACK;
        this.defaultFgColor = GUIHelper.COLOR_WIDGET_LIGHT_SHADOW;

        // column header styling
        this.cHeaderFgColor = GUIHelper.COLOR_WIDGET_LIGHT_SHADOW;
        this.cHeaderGradientBgColor = GUIHelper.COLOR_WIDGET_DARK_SHADOW;
        this.cHeaderGradientFgColor = GUIHelper.COLOR_BLACK;
        this.cHeaderCellPainter =
                new GradientBackgroundPainter(
                        new PaddingDecorator(
                                new TextPainter(false, false),
                                0,
                                5,
                                0,
                                5,
                                false),
                        true);

        // column header selection style
        this.cHeaderSelectionFgColor = GUIHelper.COLOR_WIDGET_LIGHT_SHADOW;
        this.cHeaderSelectionGradientBgColor = GUIHelper.COLOR_BLACK;
        this.cHeaderSelectionGradientFgColor = GUIHelper.COLOR_WIDGET_DARK_SHADOW;
        this.cHeaderSelectionCellPainter =
                new GradientBackgroundPainter(
                        new PaddingDecorator(
                                new TextPainter(false, false),
                                0,
                                5,
                                0,
                                5,
                                false),
                        true);

        // row header styling
        this.rHeaderFgColor = GUIHelper.COLOR_WIDGET_LIGHT_SHADOW;
        this.rHeaderGradientBgColor = GUIHelper.COLOR_WIDGET_DARK_SHADOW;
        this.rHeaderGradientFgColor = GUIHelper.COLOR_BLACK;
        this.rHeaderCellPainter =
                new GradientBackgroundPainter(
                        new TextPainter(false, false));

        // row header selection style
        this.rHeaderSelectionFgColor = GUIHelper.COLOR_WIDGET_LIGHT_SHADOW;
        this.rHeaderSelectionGradientBgColor = GUIHelper.COLOR_BLACK;
        this.rHeaderSelectionGradientFgColor = GUIHelper.COLOR_WIDGET_DARK_SHADOW;
        this.rHeaderSelectionCellPainter =
                new GradientBackgroundPainter(
                        new TextPainter(false, false));

        // no alternate row styling
        this.evenRowBgColor = GUIHelper.COLOR_BLACK;
        this.oddRowBgColor = GUIHelper.COLOR_BLACK;

        // default selection style
        this.defaultSelectionBgColor = GUIHelper.COLOR_DARK_GRAY;
        this.defaultSelectionFgColor = GUIHelper.COLOR_WIDGET_LIGHT_SHADOW;

        // selection anchor
        this.selectionAnchorSelectionBgColor = GUIHelper.COLOR_WIDGET_DARK_SHADOW;
        this.selectionAnchorSelectionFgColor = GUIHelper.COLOR_WIDGET_LIGHT_SHADOW;
        this.selectionAnchorBorderStyle = new BorderStyle(1, GUIHelper.COLOR_DARK_GRAY, LineStyleEnum.SOLID);

        // column/row group header style
        this.cGroupHeaderCellPainter =
                new GradientBackgroundPainter(
                        new ColumnGroupHeaderTextPainter(
                                new TextPainter(false, false),
                                CellEdgeEnum.RIGHT,
                                new ColumnGroupExpandCollapseImagePainter(false, true),
                                false,
                                0,
                                false));
        this.rGroupHeaderCellPainter =
                new GradientBackgroundPainter(
                        new PaddingDecorator(
                                new RowGroupHeaderTextPainter(
                                        new VerticalTextPainter(false, false),
                                        CellEdgeEnum.BOTTOM,
                                        new RowGroupExpandCollapseImagePainter(false, true),
                                        false,
                                        0,
                                        true),
                                0,
                                0,
                                2,
                                0,
                                false));

        // sort header styling
        this.sortHeaderCellPainter =
                new GradientBackgroundPainter(
                        new PaddingDecorator(
                                new SortableHeaderTextPainter(
                                        new TextPainter(false, false),
                                        CellEdgeEnum.RIGHT,
                                        new SortIconPainter(false, true),
                                        false,
                                        0,
                                        false),
                                0,
                                2,
                                0,
                                5,
                                false),
                        true);
        this.selectedSortHeaderCellPainter =
                new GradientBackgroundPainter(
                        new PaddingDecorator(
                                new SortableHeaderTextPainter(
                                        new TextPainter(false, false),
                                        CellEdgeEnum.RIGHT,
                                        new SortIconPainter(false, true),
                                        false,
                                        0,
                                        false),
                                0,
                                2,
                                0,
                                5,
                                false),
                        true);

        TreeImagePainter imagePainter =
                new TreeImagePainter(
                        false,
                        GUIHelper.getImage("right_inv"), //$NON-NLS-1$
                        GUIHelper.getImage("right_down_inv"), //$NON-NLS-1$
                        null);
        this.treeStructurePainter =
                new BackgroundPainter(
                        new PaddingDecorator(
                                new IndentedTreeImagePainter(
                                        10,
                                        null,
                                        CellEdgeEnum.LEFT,
                                        imagePainter,
                                        false,
                                        2,
                                        true),
                                0,
                                5,
                                0,
                                5,
                                false));

        this.filterRowBgColor = GUIHelper.COLOR_WIDGET_DARK_SHADOW;

        this.summaryRowBgColor = GUIHelper.COLOR_DARK_GRAY;
        this.summaryRowFgColor = GUIHelper.COLOR_WIDGET_LIGHT_SHADOW;

        this.summaryRowSelectionBgColor = GUIHelper.COLOR_WIDGET_DARK_SHADOW;

        this.renderCornerGridLines = true;
        this.renderColumnHeaderGridLines = true;
    }
}

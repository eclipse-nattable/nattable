/*******************************************************************************
 * Copyright (c) 2020 Dirk Fauth and others.
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

import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.GradientBackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.style.theme.DarkNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.style.theme.IThemeExtension;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.nebula.widgets.nattable.tree.painter.TreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

/**
 * {@link IThemeExtension} for {@link HierarchicalTreeLayer} to match the
 * {@link DarkNatTableThemeConfiguration}.
 *
 * @since 2.0
 */
public class DarkHierarchicalTreeLayerThemeExtension extends ModernHierarchicalTreeLayerThemeExtension {

    public DarkHierarchicalTreeLayerThemeExtension() {
        this.defaultBgColor = GUIHelper.COLOR_BLACK;
        this.defaultFgColor = GUIHelper.COLOR_WIDGET_LIGHT_SHADOW;

        // no alternate row styling
        this.evenRowBgColor = GUIHelper.COLOR_BLACK;
        this.oddRowBgColor = GUIHelper.COLOR_BLACK;

        // level header styling
        this.levelHeaderFgColor = GUIHelper.COLOR_WIDGET_LIGHT_SHADOW;
        this.levelHeaderGradientBgColor = GUIHelper.COLOR_WIDGET_DARK_SHADOW;
        this.levelHeaderGradientFgColor = GUIHelper.COLOR_BLACK;

        // level header selection style
        this.levelHeaderSelectionFgColor = GUIHelper.COLOR_WIDGET_LIGHT_SHADOW;
        this.levelHeaderSelectionGradientBgColor = GUIHelper.COLOR_BLACK;
        this.levelHeaderSelectionGradientFgColor = GUIHelper.COLOR_WIDGET_DARK_SHADOW;

    }

    @Override
    public void createPainterInstances() {
        super.createPainterInstances();

        ICellPainter treeImagePainter = new PaddingDecorator(
                new TreeImagePainter(
                        false,
                        GUIHelper.getImage("right_inv"), //$NON-NLS-1$
                        GUIHelper.getImage("right_down_inv"), //$NON-NLS-1$
                        null),
                5, 2, 5, 2);

        IndentedTreeImagePainter treePainter = new IndentedTreeImagePainter(
                0,
                null,
                CellEdgeEnum.TOP_LEFT,
                treeImagePainter,
                false,
                2,
                false);

        this.treeStructurePainter = new BackgroundPainter(treePainter);

        this.levelHeaderCellPainter = new GradientBackgroundPainter();
        this.levelHeaderSelectionCellPainter = new GradientBackgroundPainter();

    }

}

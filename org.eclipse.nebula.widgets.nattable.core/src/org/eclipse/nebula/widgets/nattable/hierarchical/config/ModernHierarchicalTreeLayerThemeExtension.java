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
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.style.theme.IThemeExtension;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.nebula.widgets.nattable.tree.painter.TreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

/**
 * {@link IThemeExtension} for {@link HierarchicalTreeLayer} to match the
 * {@link ModernNatTableThemeConfiguration}.
 *
 * @since 2.0
 */
public class ModernHierarchicalTreeLayerThemeExtension extends DefaultHierarchicalTreeLayerThemeExtension {

    public ModernHierarchicalTreeLayerThemeExtension() {
        // no alternate row styling
        this.evenRowBgColor = GUIHelper.COLOR_WHITE;
        this.oddRowBgColor = GUIHelper.COLOR_WHITE;
    }

    @Override
    public void createPainterInstances() {
        super.createPainterInstances();

        ICellPainter treeImagePainter = new PaddingDecorator(
                new TreeImagePainter(
                        false,
                        GUIHelper.getImage("right"), //$NON-NLS-1$
                        GUIHelper.getImage("right_down"), //$NON-NLS-1$
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

        ICellPainter treeSelectionImagePainter = new PaddingDecorator(
                new TreeImagePainter(
                        false,
                        GUIHelper.getImage("right_inv"), //$NON-NLS-1$
                        GUIHelper.getImage("right_down_inv"), //$NON-NLS-1$
                        null),
                5, 2, 5, 2);

        IndentedTreeImagePainter treeSelectionPainter = new IndentedTreeImagePainter(
                0,
                null,
                CellEdgeEnum.TOP_LEFT,
                treeSelectionImagePainter,
                false,
                2,
                false);

        this.treeStructureSelectionPainter = new BackgroundPainter(treeSelectionPainter);

    }

}

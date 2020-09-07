/*******************************************************************************
 * Copyright (c) 2014, 2020 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.style.theme.IThemeExtension;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;

/**
 * {@link IThemeExtension} for the GroupBy feature that matches the
 * {@link ModernNatTableThemeConfiguration}.
 */
public class ModernGroupByThemeExtension extends DefaultGroupByThemeExtension {

    public ModernGroupByThemeExtension() {
        FontData groupByFontData = GUIHelper.DEFAULT_FONT.getFontData()[0];
        groupByFontData.setStyle(SWT.BOLD);

        this.groupByObjectFont = GUIHelper.getFont(groupByFontData);

        this.groupByObjectSelectionFont = GUIHelper.getFont(groupByFontData);

        this.groupByHint = "Drag columns here to group by column values"; //$NON-NLS-1$

        FontData groupByHintFontData = GUIHelper.DEFAULT_FONT.getFontData()[0];
        groupByHintFontData.setStyle(SWT.ITALIC);
        this.groupByHintFont = GUIHelper.getFont(groupByHintFontData);
    }

    @Override
    public void createPainterInstances() {
        super.createPainterInstances();

        this.groupByObjectCellPainter = new BackgroundPainter(
                new PaddingDecorator(new GroupByCellTextPainter(), 0, 5, 0, 5));

        this.groupByObjectSelectionCellPainter = new BackgroundPainter(
                new PaddingDecorator(new GroupByCellTextPainter(), 0, 5, 0, 5));
    }
}

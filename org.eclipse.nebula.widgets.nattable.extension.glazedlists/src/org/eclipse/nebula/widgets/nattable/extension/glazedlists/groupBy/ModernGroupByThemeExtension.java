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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;

/**
 * @author Dirk Fauth
 *
 */
public class ModernGroupByThemeExtension extends DefaultGroupByThemeExtension {

    {
        FontData groupByFontData = GUIHelper.DEFAULT_FONT.getFontData()[0];
        groupByFontData.setStyle(SWT.BOLD);

        this.groupByObjectFont = GUIHelper.getFont(groupByFontData);
        this.groupByObjectCellPainter = new BackgroundPainter(
                new PaddingDecorator(new GroupByCellTextPainter(), 0, 5, 0, 5));

        this.groupByObjectSelectionFont = GUIHelper.getFont(groupByFontData);
        this.groupByObjectSelectionCellPainter = new BackgroundPainter(
                new PaddingDecorator(new GroupByCellTextPainter(), 0, 5, 0, 5));

        this.groupByHint = "Drag columns here to group by column values"; //$NON-NLS-1$

        FontData groupByHintFontData = GUIHelper.DEFAULT_FONT.getFontData()[0];
        groupByHintFontData.setStyle(SWT.ITALIC);
        this.groupByHintFont = GUIHelper.getFont(groupByHintFontData);
    }
}

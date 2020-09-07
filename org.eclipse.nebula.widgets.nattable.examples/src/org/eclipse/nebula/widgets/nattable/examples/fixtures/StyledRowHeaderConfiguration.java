/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.fixtures;

import org.eclipse.nebula.widgets.nattable.examples.examples._104_Styling._000_Styled_grid;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;

/**
 * Customize the default row header style. This has to be add back to the table.
 *
 * @see _000_Styled_grid
 */
public class StyledRowHeaderConfiguration extends DefaultRowHeaderStyleConfiguration {

    public StyledRowHeaderConfiguration() {
        this.font = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL));

        Image bgImage = GUIHelper.getImageByURL("rowHeaderBg",
                getClass().getResource("row_header_bg.png"));
        TextPainter txtPainter = new TextPainter(false, false);
        ICellPainter bgImagePainter =
                new BackgroundImagePainter(txtPainter, bgImage, null);
        this.cellPainter = bgImagePainter;
    }
}

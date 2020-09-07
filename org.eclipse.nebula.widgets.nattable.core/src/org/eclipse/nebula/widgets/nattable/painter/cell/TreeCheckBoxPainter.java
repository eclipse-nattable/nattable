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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.CheckBoxStateEnum;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public abstract class TreeCheckBoxPainter extends ImagePainter {

    private final Image checkedImg;
    private final Image semicheckedImg;
    private final Image uncheckedImg;

    public TreeCheckBoxPainter() {
        this(
                GUIHelper.getImage("checked"), //$NON-NLS-1$
                GUIHelper.getImage("semichecked"), //$NON-NLS-1$
                GUIHelper.getImage("unchecked") //$NON-NLS-1$
        );
    }

    public TreeCheckBoxPainter(Image checkedImg, Image semicheckedImage, Image uncheckedImg) {
        this.checkedImg = checkedImg;
        this.semicheckedImg = semicheckedImage;
        this.uncheckedImg = uncheckedImg;
    }

    public int getPreferredWidth(boolean checked) {
        return getImage(checked).getBounds().width;
    }

    public int getPreferredHeight(boolean checked) {
        return getImage(checked).getBounds().height;
    }

    public void paintIconImage(GC gc, Rectangle rectangle, int yOffset, boolean checked) {
        Image checkBoxImage = getImage(checked);

        // Center image
        int x = rectangle.x + (rectangle.width / 2) - (checkBoxImage.getBounds().width / 2);

        gc.drawImage(checkBoxImage, x, rectangle.y + yOffset);
    }

    public Image getImage(boolean checked) {
        return checked ? this.checkedImg : this.uncheckedImg;
    }

    @Override
    protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
        Image result = null;
        switch (getCheckBoxState(cell)) {
            case CHECKED:
                result = this.checkedImg;
                break;
            case SEMICHECKED:
                result = this.semicheckedImg;
                break;
            default:
                result = this.uncheckedImg;
                break;
        }
        return result;
    }

    protected abstract CheckBoxStateEnum getCheckBoxState(ILayerCell cell);

}

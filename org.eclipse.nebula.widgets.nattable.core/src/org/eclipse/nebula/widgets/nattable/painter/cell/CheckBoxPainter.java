/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Specialization of {@link ImagePainter} that renders a checkbox based on a
 * boolean value.
 */
public class CheckBoxPainter extends ImagePainter {

    private final Image checkedImg;
    private final Image uncheckedImg;

    /**
     * Create a new {@link CheckBoxPainter} with the default images.
     */
    public CheckBoxPainter() {
        this(true);
    }

    /**
     * Create a new {@link CheckBoxPainter} with the default images.
     *
     * @param paintBg
     *            <code>true</code> if this painter should itself paint the
     *            background, <code>false</code> if background painting should
     *            be skipped.
     * @since 1.4
     */
    public CheckBoxPainter(boolean paintBg) {
        this(paintBg, false);
    }

    /**
     * Create a new {@link CheckBoxPainter} with the default images.
     *
     * @param paintBg
     *            <code>true</code> if this painter should itself paint the
     *            background, <code>false</code> if background painting should
     *            be skipped.
     * @param invertIcons
     *            Specify whether the default icons should be used (black check
     *            mark) or if inverted icons should be used (white check mark).
     *
     * @since 2.0
     */
    public CheckBoxPainter(boolean paintBg, boolean invertIcons) {
        super(paintBg);
        this.checkedImg = GUIHelper.getImage("checked" + (invertIcons ? "_inv" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        this.uncheckedImg = GUIHelper.getImage("unchecked" + (invertIcons ? "_inv" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Create a new {@link CheckBoxPainter} with the given images for rendering
     * a checked/unchecked state.
     *
     * @param checkedImg
     *            The image to render the checked state (<code>true</code>).
     * @param uncheckedImg
     *            The image to render the unchecked state (<code>false</code>)
     */
    public CheckBoxPainter(Image checkedImg, Image uncheckedImg) {
        this(checkedImg, uncheckedImg, true);
    }

    /**
     * Create a new {@link CheckBoxPainter} with the given images for rendering
     * a checked/unchecked state.
     *
     * @param checkedImg
     *            The image to render the checked state (<code>true</code>).
     * @param uncheckedImg
     *            The image to render the unchecked state (<code>false</code>)
     * @param paintBg
     *            <code>true</code> if this painter should itself paint the
     *            background, <code>false</code> if background painting should
     *            be skipped.
     *
     * @since 1.4
     */
    public CheckBoxPainter(Image checkedImg, Image uncheckedImg, boolean paintBg) {
        super(paintBg);
        this.checkedImg = checkedImg;
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
        return isChecked(cell, configRegistry) ? this.checkedImg : this.uncheckedImg;
    }

    protected boolean isChecked(ILayerCell cell, IConfigRegistry configRegistry) {
        return convertDataType(cell, configRegistry).booleanValue();
    }

    protected Boolean convertDataType(ILayerCell cell, IConfigRegistry configRegistry) {
        if (cell.getDataValue() instanceof Boolean) {
            return (Boolean) cell.getDataValue();
        }
        IDisplayConverter displayConverter = configRegistry.getConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                cell.getDisplayMode(),
                cell.getConfigLabels());
        Boolean convertedValue = null;
        if (displayConverter != null) {
            convertedValue =
                    (Boolean) displayConverter.canonicalToDisplayValue(
                            cell, configRegistry, cell.getDataValue());
        }
        if (convertedValue == null) {
            convertedValue = Boolean.FALSE;
        }
        return convertedValue;
    }
}

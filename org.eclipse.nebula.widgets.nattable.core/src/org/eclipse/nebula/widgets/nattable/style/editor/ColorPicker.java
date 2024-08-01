/*******************************************************************************
 * Copyright (c) 2012, 2024 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.style.editor;

import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A button that displays a solid block of color and allows the user to pick a
 * color. The user can double click on the button in order to change the
 * selected color which also changes the background color of the button.
 *
 */
public class ColorPicker extends CLabel {

    private Color selectedColor;
    private Image image;

    public ColorPicker(Composite parent, final Color originalColor) {
        super(parent, SWT.SHADOW_OUT);
        if (originalColor == null) {
            throw new IllegalArgumentException("null"); //$NON-NLS-1$
        }
        this.selectedColor = originalColor;
        setImage(getColorImage(originalColor));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                ColorDialog dialog = new ColorDialog(new Shell(Display.getDefault(), SWT.SHELL_TRIM));
                dialog.setRGB(ColorPicker.this.selectedColor.getRGB());
                RGB selected = dialog.open();
                if (selected != null) {
                    update(selected);
                }
            }
        });
    }

    private Image getColorImage(Color color) {
        Display display = Display.getCurrent();
        this.image = new Image(display, new Rectangle(10, 10, 70, 20));
        GC gc = new GC(this.image);
        try {
            gc.setBackground(color);
            gc.fillRectangle(this.image.getBounds());
        } finally {
            gc.dispose();
        }
        return this.image;
    }

    private void update(RGB selected) {
        this.selectedColor = GUIHelper.getColor(selected);
        setImage(getColorImage(this.selectedColor));
    }

    /**
     * @return the Color most recently selected by the user. <em>Note that it is
     *         the responsibility of the client to dispose this resource</em>
     */
    public Color getSelectedColor() {
        return this.selectedColor;
    }

    /**
     * Set the current selected color that will be displayed by the picker.
     * <em>Note that this class is not responsible for destroying the given
     * Color object. It does not take ownership. Instead it will create its own
     * internal copy of the given Color resource.</em>
     *
     * @param backgroundColor
     */
    public void setSelectedColor(Color backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("null"); //$NON-NLS-1$
        }
        update(backgroundColor.getRGB());
    }

    @Override
    public void dispose() {
        super.dispose();
        this.image.dispose();
    }
}

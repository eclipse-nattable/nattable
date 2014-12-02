/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style.editor;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * A button that displays a font name and allows the user to pick another font.
 */
public class FontPicker {

    private Button button;
    private Font originalFont;
    private Font selectedFont;
    private FontData[] fontData = new FontData[1];
    private Font displayFont;

    public FontPicker(final Composite parent, Font originalFont) {
        this.button = new Button(parent, SWT.NONE);
        if (originalFont == null)
            throw new IllegalArgumentException("null"); //$NON-NLS-1$

        update(originalFont.getFontData()[0]);

        this.button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FontDialog dialog = new FontDialog(new Shell(Display
                        .getDefault(), SWT.SHELL_TRIM));
                dialog.setFontList(FontPicker.this.fontData);
                FontData selected = dialog.open();
                if (selected != null) {
                    update(selected);
                    FontPicker.this.button.pack(true);
                }
            }
        });
    }

    private void update(FontData data) {
        this.fontData[0] = data;
        this.selectedFont = GUIHelper.getFont(data);
        if (this.originalFont == null) {
            this.originalFont = this.selectedFont;
        }
        this.button.setText(data.getName() + ", " + data.getHeight() + "pt"); //$NON-NLS-1$ //$NON-NLS-2$
        this.button.setFont(createDisplayFont(data));
        this.button.setAlignment(SWT.CENTER);
        this.button.setToolTipText(Messages.getString("FontPicker.tooltip")); //$NON-NLS-1$
    }

    private Font createDisplayFont(FontData data) {
        FontData resizedData = new FontData(data.getName(), data.getHeight(),
                data.getStyle());
        this.displayFont = GUIHelper.getFont(resizedData);
        return this.displayFont;
    }

    /**
     * @return Font selected by the user.
     *         <em>Note that it is the responsibility of the client to dispose of this
     *         resource.</em>
     */
    public Font getSelectedFont() {
        return this.selectedFont;
    }

    public Font getOriginalFont() {
        return this.originalFont;
    }

    /**
     * Set the selected font.
     * <em>Note that this class will not take ownership of the passed resource. Instead it will
     * create and manage its own internal copy.</em>
     */
    public void setOriginalFont(Font font) {
        if (font != null) {
            this.originalFont = font;
            update(font.getFontData()[0]);
        }
    }

    /**
     * @return the button
     */
    public Button getButton() {
        return this.button;
    }

    public void setLayoutData(GridData gridData) {
        this.button.setLayoutData(gridData);
    }
}

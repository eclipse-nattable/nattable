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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * Adds a separator line and label to the parent panel.
 */
public class SeparatorPanel extends Composite {

    public SeparatorPanel(Composite parentPanel, String label) {
        super(parentPanel, SWT.NONE);
        initComponents(label);
    }

    public void initComponents(String label) {
        GridLayout gridLayout = new GridLayout(2, false);
        setLayout(gridLayout);

        GridData layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.horizontalAlignment = GridData.FILL;
        setLayoutData(layoutData);

        // Text label
        StyledText gridLinesLabel = new StyledText(this, SWT.NONE);
        gridLinesLabel.setEditable(false);
        Display display = Display.getDefault();
        FontData data = display.getSystemFont().getFontData()[0];
        Font font = new Font(display, data.getName(), data.getHeight(),
                SWT.BOLD);
        gridLinesLabel.setFont(font);
        gridLinesLabel.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WIDGET_BACKGROUND));
        gridLinesLabel.setText(label);

        // Separator line
        Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData separatorData = new GridData();
        separatorData.grabExcessHorizontalSpace = true;
        separatorData.horizontalAlignment = GridData.FILL;
        separatorData.horizontalIndent = 5;
        separator.setLayoutData(separatorData);
    }
}

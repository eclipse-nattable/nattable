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
package org.eclipse.nebula.widgets.nattable.columnRename;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.style.editor.AbstractEditorPanel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ColumnLabelPanel extends AbstractEditorPanel<String> {
    private Text textField;

    private final String columnLabel;
    private final String newColumnLabel;

    public ColumnLabelPanel(Composite parent, String columnLabel,
            String newColumnLabel) {
        super(parent, SWT.NONE);
        this.columnLabel = columnLabel;
        this.newColumnLabel = newColumnLabel;
        init();
    }

    private void init() {
        GridLayout gridLayout = new GridLayout(2, false);
        setLayout(gridLayout);

        // Original label
        if (this.columnLabel != null) {
            Label label = new Label(this, SWT.NONE);
            label.setText(Messages.getString("ColumnLabel.original")); //$NON-NLS-1$

            Label originalLabel = new Label(this, SWT.NONE);
            originalLabel.setText(this.columnLabel);
        }

        // Text field for new label
        Label renameLabel = new Label(this, SWT.NONE);
        renameLabel.setText(Messages.getString("ColumnLabel.rename")); //$NON-NLS-1$

        this.textField = new Text(this, SWT.BORDER);
        GridData gridData = new GridData(200, 15);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        this.textField.setLayoutData(gridData);

        if (this.newColumnLabel != null && this.newColumnLabel.length() > 0) {
            this.textField.setText(this.newColumnLabel);
            this.textField.selectAll();
        }
    }

    @Override
    public void edit(String newColumnHeaderLabel) throws Exception {
        if (newColumnHeaderLabel != null && newColumnHeaderLabel.length() > 0) {
            this.textField.setText(newColumnHeaderLabel);
            this.textField.selectAll();
        }
    }

    @Override
    public String getEditorName() {
        return Messages.getString("ColumnLabel.editorName"); //$NON-NLS-1$
    }

    @Override
    public String getNewValue() {
        if (this.textField.isEnabled()
                && this.textField.getText() != null
                && this.textField.getText().length() > 0) {
            return this.textField.getText();
        }
        return null;
    }
}

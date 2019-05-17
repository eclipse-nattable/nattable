/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.rename;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.style.editor.AbstractEditorPanel;
import org.eclipse.nebula.widgets.nattable.ui.rename.HeaderRenameDialog.RenameDialogLabels;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Panel for editing the label of a header cell.
 *
 * @since 1.6
 */
public class HeaderLabelPanel extends AbstractEditorPanel<String> {

    private Text textField;

    private final String currentLabel;
    private final String newLabel;
    private final RenameDialogLabels dialogLabels;

    public HeaderLabelPanel(Composite parent, String currentLabel, String newLabel, RenameDialogLabels dialogLabels) {
        super(parent, SWT.NONE);
        this.currentLabel = currentLabel;
        this.newLabel = newLabel;
        this.dialogLabels = dialogLabels;
        init();
    }

    private void init() {
        GridLayout gridLayout = new GridLayout(2, false);
        setLayout(gridLayout);

        // Original label
        if (this.currentLabel != null) {
            Label label = new Label(this, SWT.NONE);
            label.setText(Messages.getString("HeaderLabel.original")); //$NON-NLS-1$

            Label originalLabel = new Label(this, SWT.NONE);
            originalLabel.setText(this.currentLabel);
        }

        // Text field for new label
        Label renameLabel = new Label(this, SWT.NONE);
        renameLabel.setText(Messages.getString("HeaderLabel.rename")); //$NON-NLS-1$

        this.textField = new Text(this, SWT.BORDER);
        GridData gridData = new GridData(200, 15);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        this.textField.setLayoutData(gridData);

        if (this.newLabel != null && this.newLabel.length() > 0) {
            this.textField.setText(this.newLabel);
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
        return this.dialogLabels.editorName;
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

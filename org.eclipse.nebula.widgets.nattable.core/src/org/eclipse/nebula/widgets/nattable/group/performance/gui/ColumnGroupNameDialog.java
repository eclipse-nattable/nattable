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
package org.eclipse.nebula.widgets.nattable.group.performance.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog that is used to specify a name for a column group, either for creation
 * or renaming a column group.
 *
 * @since 1.6
 */
public class ColumnGroupNameDialog extends Dialog {

    private Button createButton;
    private Text groupNameText;
    private String columnGroupName;

    public ColumnGroupNameDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL);
    }

    @Override
    public void create() {
        super.create();
        getShell().setText(Messages.getString("ColumnGroups.createColumnGroupDialogTitle")); //$NON-NLS-1$
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

        GridDataFactory.fillDefaults()
                .minSize(200, 100)
                .align(SWT.FILL, SWT.FILL)
                .grab(true, false)
                .applyTo(createInputPanel(composite));

        Composite buttonPanel = createButtonSection(composite);
        GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BOTTOM)
                .grab(true, true)
                .applyTo(buttonPanel);

        return composite;
    }

    private Composite createButtonSection(Composite composite) {
        Composite panel = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 0;
        layout.makeColumnsEqualWidth = false;
        layout.horizontalSpacing = 2;
        panel.setLayout(layout);

        this.createButton = createButton(
                panel,
                IDialogConstants.OK_ID,
                Messages.getString("ColumnGroups.createButtonLabel"), false); //$NON-NLS-1$
        GridDataFactory.swtDefaults()
                .align(SWT.RIGHT, SWT.BOTTOM)
                .grab(true, true)
                .applyTo(this.createButton);

        this.createButton.setEnabled(false);
        getShell().setDefaultButton(this.createButton);

        Button closeButton = createButton(
                panel,
                IDialogConstants.CANCEL_ID,
                Messages.getString("AbstractStyleEditorDialog.cancelButton"), false); //$NON-NLS-1$
        GridDataFactory.swtDefaults()
                .align(SWT.RIGHT, SWT.BOTTOM)
                .grab(false, false)
                .applyTo(closeButton);

        return panel;
    }

    private Composite createInputPanel(final Composite composite) {
        final Composite row = new Composite(composite, SWT.NONE);
        row.setLayout(new GridLayout(2, false));

        final Label createLabel = new Label(row, SWT.NONE);
        createLabel.setText(Messages.getString("ColumnGroups.createGroupLabel") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
        GridDataFactory.fillDefaults()
                .align(SWT.LEFT, SWT.CENTER)
                .applyTo(createLabel);

        this.groupNameText = new Text(row, SWT.SINGLE | SWT.BORDER);
        GridDataFactory.fillDefaults()
                .grab(true, false)
                .applyTo(this.groupNameText);

        this.groupNameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                ColumnGroupNameDialog.this.createButton.setEnabled(ColumnGroupNameDialog.this.groupNameText.getText().length() > 0);
            }
        });

        return row;
    }

    @Override
    protected void okPressed() {
        this.columnGroupName = this.groupNameText.getText();
        super.okPressed();
    }

    /**
     *
     * @return The column group name that was entered in the input field.
     */
    public String getColumnGroupName() {
        return this.columnGroupName;
    }
}

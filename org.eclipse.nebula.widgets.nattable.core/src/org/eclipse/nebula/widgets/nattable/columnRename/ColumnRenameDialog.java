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
package org.eclipse.nebula.widgets.nattable.columnRename;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.style.editor.AbstractStyleEditorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColumnRenameDialog extends AbstractStyleEditorDialog {

    private static final Logger LOG = LoggerFactory.getLogger(ColumnRenameDialog.class);

    private ColumnLabelPanel columnLabelPanel;
    private final String columnLabel;
    private String renamedColumnLabel;

    public ColumnRenameDialog(Shell parent, String columnLabel, String renamedColumnLabel) {
        super(parent);
        this.columnLabel = columnLabel;
        this.renamedColumnLabel = renamedColumnLabel;
    }

    @Override
    protected void initComponents(final Shell shell) {
        GridLayout shellLayout = new GridLayout();
        shell.setLayout(shellLayout);
        shell.setText(Messages.getString("ColumnRenameDialog.shellTitle")); //$NON-NLS-1$

        // Closing the window is the same as canceling the form
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                doFormCancel(shell);
            }
        });

        // Tabs panel
        Composite panel = new Composite(shell, SWT.NONE);
        panel.setLayout(new GridLayout());

        GridData fillGridData = new GridData();
        fillGridData.grabExcessHorizontalSpace = true;
        fillGridData.horizontalAlignment = GridData.FILL;
        panel.setLayoutData(fillGridData);

        this.columnLabelPanel = new ColumnLabelPanel(panel, this.columnLabel,
                this.renamedColumnLabel);
        try {
            this.columnLabelPanel.edit(this.renamedColumnLabel);
        } catch (Exception e) {
            LOG.warn("Error on edit", e); //$NON-NLS-1$
        }
    }

    @Override
    protected void doFormOK(Shell shell) {
        this.renamedColumnLabel = this.columnLabelPanel.getNewValue();
        shell.dispose();
    }

    @Override
    protected void doFormClear(Shell shell) {
        this.renamedColumnLabel = null;
        shell.dispose();
    }

    public String getNewColumnLabel() {
        return this.renamedColumnLabel;
    }
}

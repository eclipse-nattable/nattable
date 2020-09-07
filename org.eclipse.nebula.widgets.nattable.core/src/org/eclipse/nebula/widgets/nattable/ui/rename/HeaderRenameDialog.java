/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.rename;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.style.editor.AbstractStyleEditorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog to support renaming of header cells, e.g. the column header or
 * grouping headers.
 *
 * @since 1.6
 */
public class HeaderRenameDialog extends AbstractStyleEditorDialog {

    private static final Log LOG = LogFactory.getLog(HeaderRenameDialog.class);

    public enum RenameDialogLabels {
        COLUMN_RENAME(
                Messages.getString("ColumnGroups.renameColumnGroup"), //$NON-NLS-1$
                Messages.getString("ColumnLabel.editorName")) { //$NON-NLS-1$
        },
        ROW_RENAME(
                Messages.getString("RowGroups.renameRowGroup"), //$NON-NLS-1$
                Messages.getString("RowRenameDialog.editorName")) { //$NON-NLS-1$
        };

        public final String shellTitle;
        public final String editorName;

        RenameDialogLabels(String shellTitle, String editorName) {
            this.shellTitle = shellTitle;
            this.editorName = editorName;
        }
    }

    private final String originalLabel;
    private String renamedLabel;
    private final RenameDialogLabels dialogLabels;
    private HeaderLabelPanel headerLabelPanel;

    public HeaderRenameDialog(Shell parent, String originalLabel, String renamedLabel, RenameDialogLabels dialogLabels) {
        super(parent);
        this.originalLabel = originalLabel;
        this.renamedLabel = renamedLabel;
        this.dialogLabels = dialogLabels;
    }

    @Override
    protected void initComponents(final Shell shell) {
        GridLayout shellLayout = new GridLayout();
        shell.setLayout(shellLayout);
        shell.setText(this.dialogLabels.shellTitle);

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

        this.headerLabelPanel = new HeaderLabelPanel(panel, this.originalLabel, this.renamedLabel, this.dialogLabels);
        try {
            this.headerLabelPanel.edit(this.renamedLabel);
        } catch (Exception e) {
            LOG.warn(e);
        }
    }

    @Override
    protected void doFormOK(Shell shell) {
        this.renamedLabel = this.headerLabelPanel.getNewValue();
        shell.dispose();
    }

    @Override
    protected void doFormClear(Shell shell) {
        this.renamedLabel = null;
        shell.dispose();
    }

    public String getNewLabel() {
        return this.renamedLabel;
    }
}

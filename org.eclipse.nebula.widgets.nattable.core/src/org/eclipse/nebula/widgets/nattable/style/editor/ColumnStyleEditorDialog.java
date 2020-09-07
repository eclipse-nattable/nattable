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
package org.eclipse.nebula.widgets.nattable.style.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class ColumnStyleEditorDialog extends AbstractStyleEditorDialog {

    private static final Log LOG = LogFactory.getLog(ColumnStyleEditorDialog.class);

    // Tabs in the dialog
    private CellStyleEditorPanel cellStyleEditorPanel;
    private BorderStyleEditorPanel borderStyleEditorPanel;

    // These are populated on OK button press
    protected Style newColumnCellStyle;
    protected BorderStyle newBorderStyle;

    private final Style columnStyle;

    public ColumnStyleEditorDialog(Shell parent, Style columnCellStyle) {
        super(parent);
        this.columnStyle = columnCellStyle;

        this.newColumnCellStyle = this.columnStyle;
        if (columnCellStyle != null) {
            this.newBorderStyle = this.columnStyle.getAttributeValue(CellStyleAttributes.BORDER_STYLE);
        }
    }

    @Override
    protected void initComponents(final Shell shell) {
        shell.setLayout(new GridLayout());
        shell.setText(Messages.getString("ColumnStyleEditorDialog.shellTitle")); //$NON-NLS-1$

        // Closing the window is the same as canceling the form
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                doFormCancel(shell);
            }

        });

        // Tabs panel
        Composite tabPanel = new Composite(shell, SWT.NONE);
        tabPanel.setLayout(new GridLayout());

        GridData fillGridData = new GridData();
        fillGridData.grabExcessHorizontalSpace = true;
        fillGridData.horizontalAlignment = GridData.FILL;
        tabPanel.setLayoutData(fillGridData);

        CTabFolder tabFolder = new CTabFolder(tabPanel, SWT.BORDER);
        tabFolder.setLayout(new GridLayout());
        tabFolder.setLayoutData(fillGridData);

        CTabItem columnTab = new CTabItem(tabFolder, SWT.NONE);
        columnTab.setText(Messages.getString("ColumnStyleEditorDialog.column")); //$NON-NLS-1$
        columnTab.setImage(GUIHelper.getDisplayImage("column")); //$NON-NLS-1$
        columnTab.setControl(createColumnPanel(tabFolder));

        try {
            this.cellStyleEditorPanel.edit(this.columnStyle);
            this.borderStyleEditorPanel.edit(this.columnStyle.getAttributeValue(CellStyleAttributes.BORDER_STYLE));
        } catch (Exception e) {
            LOG.error("Error on style editing", e); //$NON-NLS-1$
        }
    }

    private Composite createColumnPanel(Composite parent) {
        Composite columnPanel = new Composite(parent, SWT.NONE);
        columnPanel.setLayout(new GridLayout());

        new SeparatorPanel(columnPanel, Messages.getString("ColumnStyleEditorDialog.styling")); //$NON-NLS-1$
        this.cellStyleEditorPanel = new CellStyleEditorPanel(columnPanel, SWT.NONE);

        new SeparatorPanel(columnPanel, Messages.getString("ColumnStyleEditorDialog.border")); //$NON-NLS-1$
        this.borderStyleEditorPanel = new BorderStyleEditorPanel(columnPanel, SWT.NONE);
        return columnPanel;
    }

    @Override
    protected void doFormOK(Shell shell) {
        this.newColumnCellStyle = this.cellStyleEditorPanel.getNewValue();
        this.newBorderStyle = this.borderStyleEditorPanel.getNewValue();
        shell.dispose();
    }

    @Override
    protected void doFormClear(Shell shell) {
        this.newColumnCellStyle = null;
        shell.dispose();
    }

    // Getters for the modified style

    public Style getNewColumnCellStyle() {
        return this.newColumnCellStyle;
    }

    public BorderStyle getNewColumnBorderStyle() {
        return this.newBorderStyle;
    }
}

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

public class ColumnRenameDialog extends AbstractStyleEditorDialog {
	
	private static final Log log = LogFactory.getLog(ColumnRenameDialog.class);
	
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

		columnLabelPanel = new ColumnLabelPanel(panel, columnLabel, renamedColumnLabel);
		try {
			columnLabelPanel.edit(renamedColumnLabel);
		} catch (Exception e) {
			log.warn(e);
		}
	}

	@Override
	protected void doFormOK(Shell shell) {
		renamedColumnLabel = columnLabelPanel.getNewValue();
		shell.dispose();
	}

	@Override
	protected void doFormClear(Shell shell) {
		renamedColumnLabel = null;
		shell.dispose();
	}

	public String getNewColumnLabel() {
		return renamedColumnLabel;
	}
}

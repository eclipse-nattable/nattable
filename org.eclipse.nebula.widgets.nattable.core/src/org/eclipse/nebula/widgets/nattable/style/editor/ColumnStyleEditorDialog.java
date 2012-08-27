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

		this.newColumnCellStyle = columnCellStyle;
		if (columnCellStyle != null) {
			this.newBorderStyle = columnStyle.getAttributeValue(CellStyleAttributes.BORDER_STYLE);
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
		columnTab.setImage(GUIHelper.getImage("column")); //$NON-NLS-1$
		columnTab.setControl(createColumnPanel(tabFolder));

		try {
			cellStyleEditorPanel.edit(columnStyle);
			borderStyleEditorPanel.edit(columnStyle.getAttributeValue(CellStyleAttributes.BORDER_STYLE));
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

/*	Grid level styling
 * private Composite createBlotterPanel(Composite parent) {
		Composite blotterPanel = new Composite(parent, SWT.NONE);
		GridLayout panelLayout = new GridLayout();
		blotterPanel.setLayout(panelLayout);

		GridData panelLayoutData = new GridData();
		panelLayoutData.grabExcessHorizontalSpace = true;
		panelLayoutData.grabExcessVerticalSpace = true;
		panelLayoutData.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		panelLayoutData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		panelLayoutData.horizontalIndent = 20;
		blotterPanel.setLayoutData(panelLayoutData);

		new SeparatorPanel(blotterPanel, "Styling");
		gridColorsEditorPanel = new GridColorsEditorPanel(blotterPanel, gridStyle);

		return blotterPanel;
	}
*/
	private Composite createColumnPanel(Composite parent) {
		Composite columnPanel = new Composite(parent, SWT.NONE);
		columnPanel.setLayout(new GridLayout());

		new SeparatorPanel(columnPanel, Messages.getString("ColumnStyleEditorDialog.styling")); //$NON-NLS-1$
		cellStyleEditorPanel = new CellStyleEditorPanel(columnPanel, SWT.NONE);

		new SeparatorPanel(columnPanel, Messages.getString("ColumnStyleEditorDialog.border")); //$NON-NLS-1$
		borderStyleEditorPanel = new BorderStyleEditorPanel(columnPanel, SWT.NONE);
		return columnPanel;
	}

	@Override
	protected void doFormOK(Shell shell) {
		newColumnCellStyle = cellStyleEditorPanel.getNewValue();
		newBorderStyle = borderStyleEditorPanel.getNewValue();
		shell.dispose();
	}

	@Override
	protected void doFormClear(Shell shell) {
		this.newColumnCellStyle = null;
		shell.dispose();
	}

	// Getters for the modified style

	public Style getNewColumnCellStyle() {
		return newColumnCellStyle;
	}

	public BorderStyle getNewColumnBorderStyle() {
		return newBorderStyle;
	}
}

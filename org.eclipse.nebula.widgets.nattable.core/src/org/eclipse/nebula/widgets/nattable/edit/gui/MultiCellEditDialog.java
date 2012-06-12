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
package org.eclipse.nebula.widgets.nattable.edit.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.edit.ActiveCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.EditTypeEnum;
import org.eclipse.nebula.widgets.nattable.edit.ICellEditHandler;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class MultiCellEditDialog extends Dialog {

	private static final String SET = Messages.getString("MultiCellEditDialog.set"); //$NON-NLS-1$
	private static final String INCREASE_BY = Messages.getString("MultiCellEditDialog.increase"); //$NON-NLS-1$
	private static final String DECREASE_BY = Messages.getString("MultiCellEditDialog.decrease"); //$NON-NLS-1$
	private static final String ADJUST_BY 	= Messages.getString("MultiCellEditDialog.adjust"); //$NON-NLS-1$
	private static final String [] OPTIONS_DEFAULT = {SET, INCREASE_BY, DECREASE_BY};
	private static final String [] OPTIONS_ADJUST = {SET, ADJUST_BY};

	private final ICellEditor cellEditor;
	private final Object originalCanonicalValue;
	private final Character initialEditValue;
	private final boolean allowIncrementDecrement;

	private Combo updateCombo;
	private int lastSelectedIndex = 0;

	private Object editorValue;
    private boolean useAdjustBy;

	private IConfigRegistry configRegistry;
	private ILayerCell cell;
	
	public MultiCellEditDialog(Shell parentShell,
			final ICellEditor cellEditor,
			final Object originalCanonicalValue,
			final Character initialEditValue,
			final boolean allowIncrementDecrement,
			final IConfigRegistry configRegistry,
			final ILayerCell cell) {

		super(parentShell);
		setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL| SWT.DIALOG_TRIM);

		this.cellEditor = cellEditor;
		this.originalCanonicalValue = originalCanonicalValue;
		this.initialEditValue = initialEditValue;
		this.allowIncrementDecrement = allowIncrementDecrement;

		this.configRegistry = configRegistry;
		this.cell = cell;
	}

	@Deprecated
	public MultiCellEditDialog(Shell parentShell,
			final ICellEditor cellEditor,
			final IDisplayConverter dataTypeConverter,
			final IStyle cellStyle,
			final IDataValidator dataValidator,
			final Object originalCanonicalValue,
			final Character initialEditValue,
			final boolean allowIncrementDecrement) {

		super(parentShell);
		setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL| SWT.DIALOG_TRIM);

		this.cellEditor = cellEditor;
//		this.dataTypeConverter = dataTypeConverter;
//		this.cellStyle = cellStyle;
//		this.dataValidator = dataValidator;
		this.originalCanonicalValue = originalCanonicalValue;
		this.initialEditValue = initialEditValue;
		this.allowIncrementDecrement = allowIncrementDecrement;
	}

	public void setUseAdjustByOptionInsteadOfIncrementDecrement(boolean useAdjustBy) {
        this.useAdjustBy = useAdjustBy;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("MultiCellEditDialog.shellTitle")); //$NON-NLS-1$
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

		GridLayout panelLayout = new GridLayout(allowIncrementDecrement ? 2 : 1,false);
		panelLayout.marginWidth = 8;
		panel.setLayout(panelLayout);

		if (allowIncrementDecrement) {
			createUpdateCombo(panel);
		}

		ActiveCellEditor.close();
//		ActiveCellEditor.activate(cellEditor, panel, originalCanonicalValue, initialEditValue, dataTypeConverter, cellStyle, EditModeEnum.MULTI, dataValidator, new MultiEditHandler(), 0, 0, 0, 0);
		ActiveCellEditor.activate(cellEditor, panel, originalCanonicalValue, initialEditValue, EditModeEnum.MULTI, new MultiEditHandler(), cell, configRegistry);
		Control editorControl = ActiveCellEditor.getControl();
		// propagate the ESC event from the editor to the dialog
		editorControl.addKeyListener(getEscKeyListener());

		final GridDataFactory layoutData = GridDataFactory.fillDefaults().grab(true, false).hint(100, 20);
		if (allowIncrementDecrement) {
			layoutData.indent(5, 0);
		}
		layoutData.applyTo(editorControl);

		return panel;
	}

	/**
	 * Create a listener for the ESC key. Cancel and dispose dialog.
	 */
	private KeyListener getEscKeyListener() {
		return new KeyListener() {

			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					closeDialog();
				}
			}

			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					closeDialog();
				}
			}

			private void closeDialog() {
				setReturnCode(SWT.CANCEL);
				close();
			}
		};
	}

	private void createUpdateCombo(Composite composite) {
		updateCombo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);

		for (String option : useAdjustBy ? OPTIONS_ADJUST : OPTIONS_DEFAULT) {
			updateCombo.add(option);
		}

		updateCombo.select(0);

		updateCombo.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent arg0) {
				lastSelectedIndex = updateCombo.getSelectionIndex();
			}

		});

		GridDataFactory.swtDefaults().applyTo(updateCombo);
	}

	@Override
	protected void okPressed() {
		if (ActiveCellEditor.isValid()) {
			if (ActiveCellEditor.validateCanonicalValue()) {
				editorValue = ActiveCellEditor.getCanonicalValue();
				super.okPressed();
			}
		}
	}

	public EditTypeEnum getEditType() {
		if (allowIncrementDecrement && updateCombo != null) {

			int selectionIndex = updateCombo.isDisposed() ? lastSelectedIndex : updateCombo.getSelectionIndex();

			if (useAdjustBy) {
			    switch (selectionIndex) {
			        case 1:
			            return EditTypeEnum.ADJUST;
			    }
			} else {
    			switch (selectionIndex) {
    			case 1:
    				return EditTypeEnum.INCREASE;
    			case 2:
    				return EditTypeEnum.DECREASE;
    			}
			}
		}
		return EditTypeEnum.SET;
	}

	public Object getEditorValue() {
		return editorValue;
	}

	class MultiEditHandler implements ICellEditHandler {
		public boolean commit(MoveDirectionEnum direction, boolean closeAfterCommit) {
			if (direction == MoveDirectionEnum.NONE) {
				if (closeAfterCommit) {
					okPressed();
					return true;
				}
			}
			return false;
		}
	}

}

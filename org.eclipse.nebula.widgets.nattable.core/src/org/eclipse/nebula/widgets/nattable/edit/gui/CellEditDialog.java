/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.nattable.edit.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.DialogEditHandler;
import org.eclipse.nebula.widgets.nattable.edit.EditTypeEnum;
import org.eclipse.nebula.widgets.nattable.edit.ICellEditHandler;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog that supports editing of cells in NatTable. Is used for multi cell editing
 * and for dialog only editors.
 * 
 * @author Dirk Fauth
 *
 */
public class CellEditDialog extends Dialog implements ICellEditDialog {

	/**
	 * The value that should be propagated to the editor control.
	 * Needed because for multi cell editing or editor activation by
	 * letter/digit key will result in a different value to populate
	 * for some editors than populating the value out of the cell/
	 * data model directly.
	 */
	protected final Object originalCanonicalValue;

	/**
	 * The cell editor that should be integrated and activated in this dialog.
	 */
	protected final ICellEditor cellEditor;
	
	/**
	 * The {@link ICellEditHandler} that should be used by the editor.
	 */
	protected DialogEditHandler cellEditHandler = new DialogEditHandler();

	/**
	 * The cell that should be edited. Needed because editor activation
	 * retrieves the configuration for editing directly out of the cell.
	 */
	protected final ILayerCell cell;
	
	/**
	 * The {@link IConfigRegistry} containing the configuration of the current NatTable 
	 * instance the command should be executed for. This is necessary because the edit 
	 * controllers in the current architecture are not aware of the instance they are 
	 * running in and therefore it is needed for activation of editors.
	 */
	protected final IConfigRegistry configRegistry;

	/**
	 * @param parentShell the parent shell, or <code>null</code> to create a top-level
	 * 			shell
	 * @param originalCanonicalValue The value that should be propagated to the editor 
	 * 			control. Needed because for multi cell editing or editor activation by
	 * 			letter/digit key will result in a different value to populate for some 
	 * 			editors than populating the value out of the cell/data model directly.
	 * @param cell The cell that should be edited. Needed because editor activation
	 * 			retrieves the configuration for editing directly out of the cell.
	 * @param cellEditor The {@link ICellEditor} that will be used as editor control
	 * 			within the dialog.
	 * @param configRegistry The {@link IConfigRegistry} containing the configuration of 
	 * 			the current NatTable instance the command should be executed for. This is 
	 * 			necessary because the edit controllers in the current architecture are not 
	 * 			aware of the instance they are running in and therefore it is needed for 
	 * 			activation of editors.
	 */
	public CellEditDialog(Shell parentShell,
			final Object originalCanonicalValue,
			final ILayerCell cell,
			final ICellEditor cellEditor,
			final IConfigRegistry configRegistry) {

		super(parentShell);
		this.originalCanonicalValue = originalCanonicalValue;
		this.cell = cell;
		this.cellEditor = cellEditor;
		this.configRegistry = configRegistry;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("CellEditDialog.shellTitle")); //$NON-NLS-1$
		newShell.setImage(GUIHelper.getImage("editor")); //$NON-NLS-1$
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		//if the editor could not be committed, we should not proceed with closing the editor, as the
		//entered value is not valid in terms of conversion/validation
		if (this.cellEditor.commit(MoveDirectionEnum.NONE, true)) {
			super.okPressed();
		}
	}
	
	@Override
	protected void cancelPressed() {
		this.cellEditor.close();
		super.cancelPressed();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

		GridLayout panelLayout = new GridLayout(1, true);
		panelLayout.marginWidth = 8;
		panel.setLayout(panelLayout);

		//activate the new editor
		this.cellEditor.activateCell(
				panel, 
				this.originalCanonicalValue, 
				EditModeEnum.DIALOG, 
				this.cellEditHandler, 
				this.cell, 
				this.configRegistry);
		
		Control editorControl = this.cellEditor.getEditorControl();
		
		// propagate the ESC event from the editor to the dialog
		editorControl.addKeyListener(getEscKeyListener());

		//if the editor control already has no layout data set already, apply the default one
		//this check allows to specify a custom layout data while creating the editor control
		//in the ICellEditor
		if (editorControl.getLayoutData() == null) {
			GridDataFactory.fillDefaults().grab(true, false).hint(100, 20).applyTo(editorControl);
		}

		return panel;
	}

	@Override
	public Object getCommittedValue() {
		return this.cellEditHandler.getCommittedValue();
	}

	@Override
	public EditTypeEnum getEditType() {
		return EditTypeEnum.SET;
	}
	
	/**
	 * {@inheritDoc}
	 * @return This implementation will always return processValue, as there is no processing
	 * 			specified in this {@link ICellEditDialog} implementation and therefore the value
	 * 			that was committed to the editor will be updated to the data model.
	 */
	@Override
	public Object calculateValue(Object currentValue, Object processValue) {
		return processValue;
	}
	
	/**
	 * @return KeyListener that intercepts the ESC key to cancel editing, 
	 * 			close the editor and close the dialog.
	 */
	protected KeyListener getEscKeyListener() {
		return new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					cancelPressed();
				}
			}
	
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					cancelPressed();
				}
			}
		};
	}

}

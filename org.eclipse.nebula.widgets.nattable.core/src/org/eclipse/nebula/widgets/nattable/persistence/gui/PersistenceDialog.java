/*******************************************************************************
 * Copyright (c) 2012 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.nattable.persistence.gui;

import java.util.Properties;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.persistence.PersistenceHelper;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog that allows to save and load NatTable state.
 * It will operate on the the specified NatTable and Properties instances.
 * If the Properties need to be persisted e.g. in the file system, the
 * developer has to take care of that himself.
 * 
 * @author Dirk Fauth
 */
public class PersistenceDialog extends Dialog {

	/**
	 * Constant ID for the save button of this dialog.
	 */
	public static final int SAVE_ID = 2;

	/**
	 * Constant ID for the load button of this dialog.
	 */
	public static final int LOAD_ID = 3;

	/**
	 * Constant ID for the delete button of this dialog.
	 */
	public static final int DELETE_ID = 4;
	
	/**
	 * The NatTable instance to apply the save/load operations.
	 */
	private NatTable natTable;
	
	/**
	 * The Properties instance that should be used for saving and loading.
	 */
	private Properties properties;
	
	/**
	 * Viewer containing the state configurations.
	 */
	private TableViewer viewer;
	
	/**
	 * Text input field for specifying the name of a configuration.
	 * If there is no input in this field when saving a state configuration
	 * the default state configuration will be used.
	 */
	private Text configNameText;
	
	/**
	 * Create a new dialog for handling NatTable state.
	 * 
	 * @param parentShell the parent shell, or <code>null</code> to create a top-level shell
	 * @param natTable The NatTable instance to apply the save/load operations.
	 * @param properties The Properties instance that should be used for saving and loading.
	 */
	public PersistenceDialog(Shell parentShell, NatTable natTable, Properties properties) {
		super(parentShell);
		setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL| SWT.DIALOG_TRIM);
		
		if (natTable == null) {
			throw new IllegalArgumentException("natTable can not be null!"); //$NON-NLS-1$
		}
		if (properties == null) {
			throw new IllegalArgumentException("properties can not be null!"); //$NON-NLS-1$
		}
		
		this.natTable = natTable;
		this.properties = properties;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite control = (Composite)super.createDialogArea(parent);
		
		Label viewerLabel = new Label(control, SWT.NONE);
		viewerLabel.setText(Messages.getString("PersistenceDialog.viewerLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().grab(true, false).applyTo(viewerLabel);

		this.viewer = new TableViewer(control);
		this.viewer.setContentProvider(new ArrayContentProvider());
		this.viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element != null && element.toString().isEmpty()) {
					return Messages.getString("PersistenceDialog.defaultStateConfigName"); //$NON-NLS-1$
				}
				return super.getText(element);
			}
		});
		
		//sort in alphabetical order
		this.viewer.setComparator(new ViewerComparator());
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(this.viewer.getControl());
		
		//layout textbox
		Composite nameContainer = new Composite(control, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		nameContainer.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(nameContainer);
		Label label = new Label(nameContainer, SWT.NONE);
		label.setText(Messages.getString("PersistenceDialog.nameLabel")); //$NON-NLS-1$
		this.configNameText = new Text(nameContainer, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(this.configNameText);
		
		this.configNameText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if ((event.keyCode == SWT.CR && event.stateMask == 0)
						|| (event.keyCode == SWT.KEYPAD_CR && event.stateMask == 0)) {
					buttonPressed(SAVE_ID);
				}
			}
		});
		
		//add click listener on viewer
		this.viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection != null && selection instanceof IStructuredSelection) {
					String configName = ((IStructuredSelection)selection).getFirstElement().toString();
					configNameText.setText(configName);
				}
			}
		});
		
		//add double click listener
		this.viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			public void doubleClick(DoubleClickEvent event) {
				buttonPressed(LOAD_ID);
			}
		});
		
		this.viewer.add(PersistenceHelper.getAvailableStates(this.properties).toArray());
		
		return control;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, DELETE_ID, Messages.getString("PersistenceDialog.buttonDelete"), false); //$NON-NLS-1$
		createButton(parent, SAVE_ID, Messages.getString("PersistenceDialog.buttonSave"), false); //$NON-NLS-1$
		createButton(parent, LOAD_ID, Messages.getString("PersistenceDialog.buttonLoad"), false); //$NON-NLS-1$
		createButton(parent, IDialogConstants.OK_ID, Messages.getString("PersistenceDialog.buttonDone"), true); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == SAVE_ID) {
			String configName = this.configNameText.getText();
			this.natTable.saveState(configName, this.properties);
			this.configNameText.setText(""); //$NON-NLS-1$

			for (int i = 0; i < this.viewer.getTable().getItemCount(); i++) {
				String element = this.viewer.getElementAt(i).toString();
				if (configName.equals(element)) {
					return;
				}
			}
			
			this.viewer.add(configName);
		} else if (buttonId == DELETE_ID) {
			ISelection selection = this.viewer.getSelection();
			if (selection != null && selection instanceof IStructuredSelection) {
				String configName = ((IStructuredSelection)selection).getFirstElement().toString();
				PersistenceHelper.deleteState(configName, this.properties);
				//remove the state name out of the viewer
				this.viewer.getTable().deselectAll();
				this.viewer.remove(configName);
				this.configNameText.setText(""); //$NON-NLS-1$
			}
		} else if (buttonId == LOAD_ID) {
			ISelection selection = this.viewer.getSelection();
			if (selection != null && selection instanceof IStructuredSelection) {
				String configName = ((IStructuredSelection)selection).getFirstElement().toString();
				this.natTable.loadState(configName, this.properties);
			}
			super.okPressed();
		} else {
			super.buttonPressed(buttonId);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("PersistenceDialog.title")); //$NON-NLS-1$
		newShell.setImage(GUIHelper.getImage("table_icon")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 300);
	};
	
	/**
	 * @return The Properties instance that is used for saving and loading.
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param properties The Properties instance that should be used for saving and loading.
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}

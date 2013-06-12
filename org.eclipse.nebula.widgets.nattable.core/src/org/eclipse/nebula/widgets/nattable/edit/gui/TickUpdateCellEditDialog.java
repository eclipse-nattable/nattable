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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.EditTypeEnum;
import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.tickupdate.ITickUpdateHandler;
import org.eclipse.nebula.widgets.nattable.tickupdate.TickUpdateConfigAttributes;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Specialization of {@link CellEditDialog} that adds additional functionality
 * to also use tick updates on the cell value(s). By selecting another edit type
 * than set, the value entered in the editor control will be processed with the
 * current value instead of simply setting this value. As using e.g. increase/decrease
 * on the current value allows different values than on set, the validator needs
 * to be wrapped so it is skipped for entering the value for processing.
 */
public class TickUpdateCellEditDialog extends CellEditDialog {

	private static final Log log = LogFactory.getLog(TickUpdateCellEditDialog.class);

	private static final String SET = Messages.getString("TickUpdateCellEditDialog.set"); //$NON-NLS-1$
	private static final String INCREASE_BY = Messages.getString("TickUpdateCellEditDialog.increase"); //$NON-NLS-1$
	private static final String DECREASE_BY = Messages.getString("TickUpdateCellEditDialog.decrease"); //$NON-NLS-1$
	private static final String ADJUST_BY 	= Messages.getString("TickUpdateCellEditDialog.adjust"); //$NON-NLS-1$
	private static final String [] OPTIONS_DEFAULT = {SET, INCREASE_BY, DECREASE_BY};
	private static final String [] OPTIONS_ADJUST = {SET, ADJUST_BY};
	
	/**
	 * The {@link ITickUpdateHandler} that will be used to process
	 * 	the tick updates after closing this editor by pressing ok.
	 */
	private final ITickUpdateHandler tickUpdateHandler;
	/**
	 * The combo control that contains the available actions used for tick update.
	 */
	private Combo updateCombo;
	/**
	 * The {@link EditTypeEnum} that represents the selected item in the tick update 
	 * combo control.
	 */
	private EditTypeEnum editType = EditTypeEnum.SET;
	/**
	 * Flag to determine whether this dialog should provide the set, increase and decrease
	 * tick update options, or the set and adjust options. Default is to not use the adjust
	 * options.
	 */
	private boolean useAdjustBy;
	/**
	 * The validator that is needed to be called when the tick update is processed.
	 * On executing a tick update it would otherwise be possible to exploit validation.
	 */
	private IDataValidator validator;
	
	/**
	 * 
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
	 * @param tickUpdateHandler The {@link ITickUpdateHandler} that will be used to process
	 * 			the tick updates after closing this editor by pressing ok.
	 */
	public TickUpdateCellEditDialog(Shell parentShell,
			final Object originalCanonicalValue,
			final ILayerCell cell,
			final ICellEditor cellEditor,
			final IConfigRegistry configRegistry,
			final ITickUpdateHandler tickUpdateHandler) {
		
		super(parentShell, originalCanonicalValue, cell, cellEditor, configRegistry);
		this.tickUpdateHandler = tickUpdateHandler;
		
		Boolean useAdjustByConfig = configRegistry.getConfigAttribute(
				TickUpdateConfigAttributes.USE_ADJUST_BY, 
				DisplayMode.EDIT, 
				cell.getConfigLabels().getLabels());
		this.useAdjustBy = useAdjustByConfig != null ? useAdjustByConfig : false;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

		GridLayout panelLayout = new GridLayout(2, false);
		panelLayout.marginWidth = 8;
		panel.setLayout(panelLayout);
		
		createUpdateCombo(panel);

		//activate the new editor
		this.cellEditor.activateCell(
				panel, 
				this.originalCanonicalValue, 
				EditModeEnum.DIALOG, 
				this.cellEditHandler, 
				this.cell, 
				this.configRegistry);
		
		this.validator = this.configRegistry.getConfigAttribute(
				EditConfigAttributes.DATA_VALIDATOR, 
				DisplayMode.EDIT, 
				this.cell.getConfigLabels().getLabels());
		
		if (this.cellEditor instanceof AbstractCellEditor) {
			//need to override the validator because increase decrease adjust don't need to validate immediately
			((AbstractCellEditor) this.cellEditor).setDataValidator(
					new TickUpdateDataValidatorWrapper(this.validator));
		}
		
		Control editorControl = this.cellEditor.getEditorControl();
		
		// propagate the ESC event from the editor to the dialog
		editorControl.addKeyListener(getEscKeyListener());
		
		GridDataFactory.fillDefaults().grab(true, false).hint(100, 20).indent(5, 0)
			.applyTo(editorControl);
		//if the editor control already has no layout data set already, apply the default one
		//this check allows to specify a custom layout data while creating the editor control
		//in the ICellEditor
		if (editorControl.getLayoutData() == null) {
			GridDataFactory.fillDefaults().grab(true, false).hint(100, 20).applyTo(editorControl);
		}

		return panel;
	}

	/**
	 * Create the combo control that contains the available actions used for tick update.
	 * The possible actions in this combo are specified by evaluating the useAdjustBy configuration.
	 * @param composite The composite control that will be the parent for the tick update combo.
	 */
	private void createUpdateCombo(Composite composite) {
		updateCombo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);

		for (String option : useAdjustBy ? OPTIONS_ADJUST : OPTIONS_DEFAULT) {
			updateCombo.add(option);
		}

		updateCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = updateCombo.getSelectionIndex();

				if (useAdjustBy) {
				    switch (selectionIndex) {
				        case 1:
				            editType = EditTypeEnum.ADJUST;
				            break;
				    }
				} else {
					switch (selectionIndex) {
						case 1:
							editType = EditTypeEnum.INCREASE;
							break;
						case 2:
							editType = EditTypeEnum.DECREASE;
							break;
					}
				}
			}
		});
		
		updateCombo.select(0);

		GridDataFactory.swtDefaults().applyTo(updateCombo);
	}

	@Override
	public EditTypeEnum getEditType() {
		return this.editType;
	}

	@Override
	public Object calculateValue(Object currentValue, Object processValue) {
		double delta = processValue instanceof Number 
				? ((Number)processValue).doubleValue() : Double.valueOf((String)processValue).doubleValue();
		if (this.editType == EditTypeEnum.ADJUST) {
		    if(delta >= 0) {
		    	this.editType = EditTypeEnum.INCREASE;
		    } else {
		    	this.editType = EditTypeEnum.DECREASE;
		    }
		}
		
		Object newValue = null;
        switch (this.editType) {
			case INCREASE:
				newValue = tickUpdateHandler.getIncrementedValue(currentValue, delta);
				break;
			case DECREASE:
				newValue = tickUpdateHandler.getDecrementedValue(currentValue, delta);
				break;
		}
        
		try {
			if (validator.validate(cell, configRegistry, newValue)) {
				return newValue;
			}
			else {
				log.warn("Tick update failed for value " + newValue //$NON-NLS-1$
						+ ". New value is not valid!"); //$NON-NLS-1$
			}
		}
		catch (Exception e) {
			log.warn("Tick update failed for value " + newValue //$NON-NLS-1$
					+ ". " + e.getLocalizedMessage()); //$NON-NLS-1$
		}

		//return the unprocessed value because after the tick the value is invalid
        return currentValue;
	}

	/**
	 * {@link IDataValidator} wrapper that is only used for tick update handling
	 * within this dialog. Will only execute the validation if {@link EditTypeEnum#SET}
	 * is used. Otherwise validation is skipped.
	 */
	private final class TickUpdateDataValidatorWrapper implements IDataValidator {
		
		IDataValidator wrappedValidator;
		
		TickUpdateDataValidatorWrapper(IDataValidator wrappedValidator) {
			this.wrappedValidator = wrappedValidator;
		}
		
		@Override
		public boolean validate(int columnIndex, int rowIndex, Object newValue) {
			if (editType == EditTypeEnum.SET) {
				return this.wrappedValidator.validate(columnIndex, rowIndex, newValue);
			}
			return true;
		}

		@Override
		public boolean validate(ILayerCell cell, IConfigRegistry configRegistry, Object newValue) {
			if (editType == EditTypeEnum.SET) {
				return this.wrappedValidator.validate(cell, configRegistry, newValue);
			}
			return true;
		}
	}
}

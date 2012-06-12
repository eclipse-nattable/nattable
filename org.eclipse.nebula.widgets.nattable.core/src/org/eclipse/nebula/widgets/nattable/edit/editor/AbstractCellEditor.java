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
package org.eclipse.nebula.widgets.nattable.edit.editor;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.ValidationFailedException;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.ICellEditHandler;
import org.eclipse.nebula.widgets.nattable.edit.config.LoggingErrorHandling;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleProxy;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class AbstractCellEditor implements ICellEditor {

	private boolean closed;
	private Composite parent;
	private ICellEditHandler editHandler;
	private IDisplayConverter displayConverter;
	private IStyle cellStyle;
	private IDataValidator dataValidator;
	protected EditModeEnum editMode;
	protected ILayerCell layerCell;
	
	protected IEditErrorHandler conversionEditErrorHandler;
	protected IEditErrorHandler validationEditErrorHandler;
	protected IConfigRegistry configRegistry;
	protected LabelStack labelStack;
	
	public final Control activateCell(Composite parent, Object originalCanonicalValue, Character initialEditValue,
			EditModeEnum editMode, ICellEditHandler editHandler, ILayerCell cell, IConfigRegistry configRegistry) {

		this.closed = false;
		this.parent = parent;
		this.editHandler = editHandler;
		this.editMode = editMode;
		this.layerCell = cell;
		this.configRegistry = configRegistry;
		this.labelStack = cell.getConfigLabels();
		final List<String> configLabels = labelStack.getLabels();
		this.displayConverter = configRegistry.getConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, DisplayMode.EDIT, configLabels);
		this.cellStyle = new CellStyleProxy(configRegistry, DisplayMode.EDIT, configLabels);
		this.dataValidator = configRegistry.getConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, DisplayMode.EDIT, configLabels);
		
		this.conversionEditErrorHandler = getEditErrorHandler(configRegistry, EditConfigAttributes.CONVERSION_ERROR_HANDLER, configLabels);
		this.validationEditErrorHandler = getEditErrorHandler(configRegistry, EditConfigAttributes.VALIDATION_ERROR_HANDLER, configLabels);
		
		return activateCell(parent, originalCanonicalValue, initialEditValue);
	}

	final protected int getColumnIndex() {
		return layerCell.getColumnIndex();
	}

	final protected int getRowIndex() {
		return layerCell.getRowIndex();
	}
	
	final protected int getColumnPosition() {
		return layerCell.getColumnPosition();
	}

	final protected int getRowPosition() {
		return layerCell.getRowPosition();
	}
	
	protected abstract Control activateCell(Composite parent, Object originalCanonicalValue, Character initialEditValue);

	public boolean validateCanonicalValue() {
		return validateCanonicalValue(this.conversionEditErrorHandler, this.validationEditErrorHandler);
	}
	
	public boolean validateCanonicalValue(IEditErrorHandler conErrorHandler, IEditErrorHandler valErrorHandler) {
		Object canonicalValue;
		try {
			//always do the conversion to check for valid entered data
			canonicalValue = getCanonicalValue();

			//if the conversion succeeded, remove error rendering if exists
			conErrorHandler.removeError(this);
		} catch (Exception e) {
			// conversion failed
			conErrorHandler.displayError(this, e);
			return false;
		}
		
		//do the validation if a validator is registered
		if (dataValidator != null) {
			try {
				boolean validationResult = dataValidator.validate(layerCell, configRegistry, canonicalValue);

				//if the validation succeeded, remove error rendering if exists
				if (validationResult) {
					valErrorHandler.removeError(this);
				} else {
					throw new ValidationFailedException("Error whilst validating cell value!"); //$NON-NLS-1$
				}
				return validationResult;
			} catch (Exception e) {
				//validation failed
				valErrorHandler.displayError(this, e);
				return false;
			}
		}
		
		return true;
	}
	
	protected IDisplayConverter getDataTypeConverter() {
		return displayConverter;
	}

	protected IStyle getCellStyle() {
		return cellStyle;
	}

	protected IDataValidator getDataValidator() {
		return dataValidator;
	}

	/**
	 * Commit and close editor.
	 * @see AbstractCellEditor#commit(MoveDirectionEnum, boolean)
	 */
	protected final boolean commit(MoveDirectionEnum direction) {
		return commit(direction, true);
	}
	
	/**
	 * Commit change - after validation.
	 * @param direction to move the selection in after a successful commit
	 * @param closeAfterCommit close the editor after a successful commit
	 */
	public final boolean commit(MoveDirectionEnum direction, boolean closeAfterCommit) {
		if (editHandler != null) {
			if (validateCanonicalValue()) {
				return (editHandler.commit(direction, closeAfterCommit));
			}
		}
		return false;
	}


	public void close() {
		closed = true;
		if (parent != null && !parent.isDisposed()) {
			parent.forceFocus();
		}
	}

	public boolean isClosed() {
		return closed;
	}
	
	private IEditErrorHandler getEditErrorHandler(
			IConfigRegistry configRegistry, ConfigAttribute<IEditErrorHandler> configAttribute, List<String> configLabels) {
		
		IEditErrorHandler errorHandler = configRegistry.getConfigAttribute(configAttribute, DisplayMode.EDIT, configLabels);
		if (errorHandler == null) {
			//set LoggingErrorHandling as default
			errorHandler = new LoggingErrorHandling();
		}
		return errorHandler;
	}

}

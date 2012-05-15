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
package org.eclipse.nebula.widgets.nattable.edit.config;


import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.data.convert.ConversionFailedException;
import org.eclipse.nebula.widgets.nattable.data.validate.ValidationFailedException;
import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractEditErrorHandler;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IEditErrorHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Strategy class for conversion/validation failures.
 * If the entered value is not valid, a warning dialog with the corresponding error message
 * will show up. The warning dialog gives the opportunity to discard the invalid input or
 * change it, which will cause the editor to stay open. 
 * Only handles errors on commit.
 * 
 * @author fipro
 */
public class DialogErrorHandling extends AbstractEditErrorHandler {

	protected ICellEditor editor;
	
	private String failureShellTitle = Messages.getString("DialogErrorHandlingStrategy.failureTitle"); //$NON-NLS-1$
	private String conversionFailureShellTitle = Messages.getString("DialogErrorHandlingStrategy.conversionFailureTitle"); //$NON-NLS-1$
	private String validationFailureShellTitle = Messages.getString("DialogErrorHandlingStrategy.validationFailureTitle"); //$NON-NLS-1$
	private String changeButtonLabel = Messages.getString("DialogErrorHandlingStrategy.warningDialog.changeButton"); //$NON-NLS-1$
	private String discardButtonLabel = Messages.getString("DialogErrorHandlingStrategy.warningDialog.discardButton");  //$NON-NLS-1$

	
	public DialogErrorHandling() {
		super(null);
	}
	
	public DialogErrorHandling(IEditErrorHandler underlyingErrorHandler) {
		super(underlyingErrorHandler);
	}
	
	@Override
	public void displayError(ICellEditor cellEditor, Exception e) {
		super.displayError(cellEditor, e);
		this.editor = cellEditor;
		
		String shellTitle = failureShellTitle;
		if (e instanceof ConversionFailedException && conversionFailureShellTitle != null) {
			shellTitle = conversionFailureShellTitle;
		} else if (e instanceof ValidationFailedException && validationFailureShellTitle != null) {
			shellTitle = validationFailureShellTitle;
		}
		showWarningDialog(e.getLocalizedMessage(), shellTitle);
	}
	
	/**
	 * Shows a warning dialog if the conversion or the validation returned an error message.
	 * Otherwise nothing happens.
	 */
	protected void showWarningDialog(String dialogMessage, String dialogTitle) {
		if (!isWarningDialogActive()) {
			//conversion/validation failed - so open dialog with error message
			
			if (dialogMessage != null) {
				MessageDialog warningDialog = new MessageDialog(
						Display.getCurrent().getActiveShell(), 
						dialogTitle, 
						null, 
						dialogMessage, 
						MessageDialog.WARNING, 
						new String[] {
							changeButtonLabel,
							discardButtonLabel}, 
						0);
				
				//if discard was selected close the editor
				if (warningDialog.open() == 1) {
					this.editor.close();
				}
			}
		}
	}
	
	/**
	 * Checks if the current active Shell is a conversion/validation failure warning dialog.
	 * As a Shell has not id it is checked if the Shell title is for conversion or validation
	 * failure in localized format.
	 * @return <code>true</code> if a warning dialog is active
	 */
	protected boolean isWarningDialogActive() {
		//check if the current active shell is a conversion or validation failure warning dialog
		Shell control = Display.getCurrent().getActiveShell();
		if (control != null &&
				(conversionFailureShellTitle.equals(control.getText())
						|| validationFailureShellTitle.equals(control.getText())
						|| failureShellTitle.equals(control.getText()))) {
			return true;
		}
		return false;
	}

	
	public String getFailureShellTitle() {
		return failureShellTitle;
	}

	public void setFailureShellTitle(String failureShellTitle) {
		this.failureShellTitle = failureShellTitle;
	}

	public String getConversionFailureShellTitle() {
		return conversionFailureShellTitle;
	}

	public void setConversionFailureShellTitle(String conversionFailureShellTitle) {
		this.conversionFailureShellTitle = conversionFailureShellTitle;
	}

	public String getValidationFailureShellTitle() {
		return validationFailureShellTitle;
	}

	public void setValidationFailureShellTitle(String validationFailureShellTitle) {
		this.validationFailureShellTitle = validationFailureShellTitle;
	}

	public String getChangeButtonLabel() {
		return changeButtonLabel;
	}

	public void setChangeButtonLabel(String changeButtonLabel) {
		this.changeButtonLabel = changeButtonLabel;
	}

	public String getDiscardButtonLabel() {
		return discardButtonLabel;
	}

	public void setDiscardButtonLabel(String discardButtonLabel) {
		this.discardButtonLabel = discardButtonLabel;
	}

}

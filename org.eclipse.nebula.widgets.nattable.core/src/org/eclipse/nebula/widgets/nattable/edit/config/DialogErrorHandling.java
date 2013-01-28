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
 * @author Dirk Fauth
 */
public class DialogErrorHandling extends AbstractEditErrorHandler {

	/**
	 * The {@link ICellEditor} for which this {@link DialogErrorHandling} is activated.
	 * Needed so it is possible to operate on it dependent on the users choice.
	 */
	protected ICellEditor editor;
	/**
	 * The shell title that will be used if there is no conversion or validation shell title configured.
	 */
	private String failureShellTitle = Messages.getString("DialogErrorHandlingStrategy.failureTitle"); //$NON-NLS-1$
	/**
	 * The shell title that will be used in case this {@link DialogErrorHandling} is called to handle
	 * a {@link ConversionFailedException}.
	 */
	private String conversionFailureShellTitle = Messages.getString("DialogErrorHandlingStrategy.conversionFailureTitle"); //$NON-NLS-1$
	/**
	 * The shell title that will be used in case this {@link DialogErrorHandling} is called to handle
	 * a {@link ValidationFailedException}.
	 */
	private String validationFailureShellTitle = Messages.getString("DialogErrorHandlingStrategy.validationFailureTitle"); //$NON-NLS-1$
	/**
	 * The text on the button for changing the entered value.
	 */
	private String changeButtonLabel = Messages.getString("DialogErrorHandlingStrategy.warningDialog.changeButton"); //$NON-NLS-1$
	/**
	 * The text on the button to discard the entered value.
	 */
	private String discardButtonLabel = Messages.getString("DialogErrorHandlingStrategy.warningDialog.discardButton");  //$NON-NLS-1$

	/**
	 * Create a new {@link DialogErrorHandling} with no underlying {@link IEditErrorHandler}
	 */
	public DialogErrorHandling() {
		super(null);
	}
	
	/**
	 * Create a new {@link DialogErrorHandling} using the given {@link IEditErrorHandler} as
	 * the underlying to allow chaining of error handling.
	 * @param underlyingErrorHandler The underlying {@link IEditErrorHandler}
	 */
	public DialogErrorHandling(IEditErrorHandler underlyingErrorHandler) {
		super(underlyingErrorHandler);
	}
	
	/**
	 * {@inheritDoc}
	 * After the error is handled by its underlying {@link IEditErrorHandler},
	 * a dialog will be opened showing the error message to the user, giving the
	 * opportunity to decide if the entered value should be discarded or if the
	 * editor should stay open so the value can be modified.
	 */
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

	/**
	 * @return The shell title that will be used if there is no conversion or validation shell 
	 * 			title configured.
	 */
	public String getFailureShellTitle() {
		return failureShellTitle;
	}

	/**
	 * @param failureShellTitle The shell title that should be used if there is no conversion or 
	 * 			validation shell title configured.
	 */
	public void setFailureShellTitle(String failureShellTitle) {
		this.failureShellTitle = failureShellTitle;
	}

	/**
	 * @return The shell title that will be used in case this {@link DialogErrorHandling} is called 
	 * 			to handle a {@link ConversionFailedException}.
	 */
	public String getConversionFailureShellTitle() {
		return conversionFailureShellTitle;
	}

	/**
	 * @param conversionFailureShellTitle The shell title that should be used in case this 
	 * 			{@link DialogErrorHandling} is called to handle a {@link ConversionFailedException}.
	 */
	public void setConversionFailureShellTitle(String conversionFailureShellTitle) {
		this.conversionFailureShellTitle = conversionFailureShellTitle;
	}

	/**
	 * @return The shell title that will be used in case this {@link DialogErrorHandling} is called to handle
	 * 			a {@link ValidationFailedException}.
	 */
	public String getValidationFailureShellTitle() {
		return validationFailureShellTitle;
	}

	/**
	 * @param validationFailureShellTitle The shell title that should be used in case this 
	 * 			{@link DialogErrorHandling} is called to handle a {@link ValidationFailedException}.
	 */
	public void setValidationFailureShellTitle(String validationFailureShellTitle) {
		this.validationFailureShellTitle = validationFailureShellTitle;
	}

	/**
	 * @return The text on the button for changing the entered value.
	 */
	public String getChangeButtonLabel() {
		return changeButtonLabel;
	}

	/**
	 * @param changeButtonLabel The text on the button for changing the entered value.
	 */
	public void setChangeButtonLabel(String changeButtonLabel) {
		this.changeButtonLabel = changeButtonLabel;
	}

	/**
	 * @return The text on the button to discard the entered value.
	 */
	public String getDiscardButtonLabel() {
		return discardButtonLabel;
	}

	/**
	 * @param discardButtonLabel The text on the button to discard the entered value.
	 */
	public void setDiscardButtonLabel(String discardButtonLabel) {
		this.discardButtonLabel = discardButtonLabel;
	}

}

/*******************************************************************************
 * Copyright (c) 2012, 2013, 2015 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Strategy class for conversion/validation failures. If the entered value is
 * not valid, a warning dialog with the corresponding error message will show
 * up. The warning dialog gives the opportunity to discard the invalid input or
 * change it, which will cause the editor to stay open. Only handles errors on
 * commit.
 */
public class DialogErrorHandling extends AbstractEditErrorHandler {

    /**
     * The {@link ICellEditor} for which this {@link DialogErrorHandling} is
     * activated. Needed so it is possible to operate on it dependent on the
     * users choice.
     */
    protected ICellEditor editor;
    /**
     * Flag to configure if this dialog allows to commit invalid data. This is
     * necessary to implement cross validation in NatTable by using dialogs to
     * tell the user what is wrong. By default this value is set to
     * <code>false</code> as cross validation is not the default validation use
     * case.
     */
    protected boolean allowCommit = false;
    /**
     * The shell title that will be used if there is no conversion or validation
     * shell title configured.
     */
    private String failureShellTitle = "%DialogErrorHandlingStrategy.failureTitle"; //$NON-NLS-1$
    /**
     * The shell title that will be used in case this
     * {@link DialogErrorHandling} is called to handle a
     * {@link ConversionFailedException}.
     */
    private String conversionFailureShellTitle = "%DialogErrorHandlingStrategy.conversionFailureTitle"; //$NON-NLS-1$
    /**
     * The shell title that will be used in case this
     * {@link DialogErrorHandling} is called to handle a
     * {@link ValidationFailedException}.
     */
    private String validationFailureShellTitle = "%DialogErrorHandlingStrategy.validationFailureTitle"; //$NON-NLS-1$
    /**
     * The text on the button for changing the entered value.
     */
    private String changeButtonLabel = "%DialogErrorHandlingStrategy.warningDialog.changeButton"; //$NON-NLS-1$
    /**
     * The text on the button to discard the entered value.
     */
    private String discardButtonLabel = "%DialogErrorHandlingStrategy.warningDialog.discardButton"; //$NON-NLS-1$
    /**
     * The text on the button to commit the entered invalid value. Needed to
     * support cross validation.
     */
    private String commitButtonLabel = "%DialogErrorHandlingStrategy.warningDialog.commitButton"; //$NON-NLS-1$

    /**
     * Create a new {@link DialogErrorHandling} with no underlying
     * {@link IEditErrorHandler} that does not support cross validation.
     */
    public DialogErrorHandling() {
        this(null, false);
    }

    /**
     * Create a new {@link DialogErrorHandling} with no underlying
     * {@link IEditErrorHandler} that gives the opportunity to configure the
     * error handling for cross validation.
     *
     * @param allowCommit
     *            Flag to configure if this dialog allows to commit invalid
     *            data. If this parameter is set to <code>true</code>, an
     *            additional button for committing the invalid data will be
     *            provided within the dialog.
     */
    public DialogErrorHandling(boolean allowCommit) {
        this(null, allowCommit);

    }

    /**
     * Create a new {@link DialogErrorHandling} using the given
     * {@link IEditErrorHandler} as the underlying to allow chaining of error
     * handling. Using this constructor there is no cross validation support.
     *
     * @param underlyingErrorHandler
     *            The underlying {@link IEditErrorHandler}
     */
    public DialogErrorHandling(IEditErrorHandler underlyingErrorHandler) {
        this(underlyingErrorHandler, false);
    }

    /**
     * Create a new {@link DialogErrorHandling} using the given
     * {@link IEditErrorHandler} as the underlying to allow chaining of error
     * handling.
     *
     * @param underlyingErrorHandler
     *            The underlying {@link IEditErrorHandler}
     * @param allowCommit
     *            Flag to configure if this dialog allows to commit invalid
     *            data. If this parameter is set to <code>true</code>, an
     *            additional button for committing the invalid data will be
     *            provided within the dialog.
     */
    public DialogErrorHandling(IEditErrorHandler underlyingErrorHandler, boolean allowCommit) {
        super(underlyingErrorHandler);
        this.allowCommit = allowCommit;
    }

    /**
     * {@inheritDoc} After the error is handled by its underlying
     * {@link IEditErrorHandler}, a dialog will be opened showing the error
     * message to the user, giving the opportunity to decide if the entered
     * value should be discarded or if the editor should stay open so the value
     * can be modified.
     */
    @Override
    public void displayError(ICellEditor cellEditor, Exception e) {
        super.displayError(cellEditor, e);
        this.editor = cellEditor;

        String shellTitle = getFailureShellTitle();
        if (e instanceof ConversionFailedException
                && !getConversionFailureShellTitle().isEmpty()) {
            shellTitle = getConversionFailureShellTitle();
        } else if (e instanceof ValidationFailedException
                && !getValidationFailureShellTitle().isEmpty()) {
            shellTitle = getValidationFailureShellTitle();
        }
        showWarningDialog(e.getLocalizedMessage(), shellTitle);
    }

    /**
     * Shows a warning dialog if the conversion or the validation returned an
     * error message. Otherwise nothing happens.
     */
    protected void showWarningDialog(String dialogMessage, String dialogTitle) {
        if (!isWarningDialogActive()) {
            // conversion/validation failed - so open dialog with error message

            if (dialogMessage != null) {
                String[] buttonLabels = this.allowCommit ?
                        new String[] { getChangeButtonLabel(), getDiscardButtonLabel(), getCommitButtonLabel() }
                        : new String[] { getChangeButtonLabel(), getDiscardButtonLabel() };

                MessageDialog warningDialog = new MessageDialog(
                    Display.getDefault().getActiveShell(),
                    dialogTitle,
                    null,
                    dialogMessage,
                    MessageDialog.WARNING,
                    buttonLabels,
                    0);

                // if discard was selected close the editor
                int returnCode = warningDialog.open();
                if (returnCode == 1) {
                    this.editor.close();
                }
                // if commit was selected, commit the value by skipping the
                // validation
                else if (returnCode == 2) {
                    this.editor.commit(MoveDirectionEnum.NONE, true, true);
                }
            }
        }
    }

    /**
     * Checks if the current active Shell is a conversion/validation failure
     * warning dialog. As a Shell has not id it is checked if the Shell title is
     * for conversion or validation failure in localized format.
     *
     * @return <code>true</code> if a warning dialog is active
     */
    protected boolean isWarningDialogActive() {
        // check if the current active shell is a conversion or validation
        // failure warning dialog
        Shell control = Display.getDefault().getActiveShell();
        if (control != null
                && (getConversionFailureShellTitle().equals(control.getText())
                        || getValidationFailureShellTitle().equals(control.getText())
                        || getFailureShellTitle().equals(control.getText()))) {
            return true;
        }
        return false;
    }

    /**
     * @return The shell title that will be used if there is no conversion or
     *         validation shell title configured.
     */
    public String getFailureShellTitle() {
        return getLocalized(this.failureShellTitle);
    }

    /**
     * @param failureShellTitle
     *            The shell title that should be used if there is no conversion
     *            or validation shell title configured.
     */
    public void setFailureShellTitle(String failureShellTitle) {
        this.failureShellTitle = failureShellTitle;
    }

    /**
     * @return The shell title that will be used in case this
     *         {@link DialogErrorHandling} is called to handle a
     *         {@link ConversionFailedException}.
     */
    public String getConversionFailureShellTitle() {
        return getLocalized(this.conversionFailureShellTitle);
    }

    /**
     * @param conversionFailureShellTitle
     *            The shell title that should be used in case this
     *            {@link DialogErrorHandling} is called to handle a
     *            {@link ConversionFailedException}.
     */
    public void setConversionFailureShellTitle(String conversionFailureShellTitle) {
        this.conversionFailureShellTitle = conversionFailureShellTitle;
    }

    /**
     * @return The shell title that will be used in case this
     *         {@link DialogErrorHandling} is called to handle a
     *         {@link ValidationFailedException}.
     */
    public String getValidationFailureShellTitle() {
        return getLocalized(this.validationFailureShellTitle);
    }

    /**
     * @param validationFailureShellTitle
     *            The shell title that should be used in case this
     *            {@link DialogErrorHandling} is called to handle a
     *            {@link ValidationFailedException}.
     */
    public void setValidationFailureShellTitle(String validationFailureShellTitle) {
        this.validationFailureShellTitle = validationFailureShellTitle;
    }

    /**
     * @return The text on the button for changing the entered value.
     */
    public String getChangeButtonLabel() {
        return getLocalized(this.changeButtonLabel);
    }

    /**
     * @param changeButtonLabel
     *            The text on the button for changing the entered value.
     */
    public void setChangeButtonLabel(String changeButtonLabel) {
        this.changeButtonLabel = changeButtonLabel;
    }

    /**
     * @return The text on the button to discard the entered value.
     */
    public String getDiscardButtonLabel() {
        return getLocalized(this.discardButtonLabel);
    }

    /**
     * @param discardButtonLabel
     *            The text on the button to discard the entered value.
     */
    public void setDiscardButtonLabel(String discardButtonLabel) {
        this.discardButtonLabel = discardButtonLabel;
    }

    /**
     * @return The text on the button to commit the entered value.
     * @since 1.4
     */
    public String getCommitButtonLabel() {
        return getLocalized(this.commitButtonLabel);
    }

    /**
     * @param commitButtonLabel
     *            The text on the button to commit the entered value.
     * @since 1.4
     */
    public void setCommitButtonLabel(String commitButtonLabel) {
        this.commitButtonLabel = commitButtonLabel;
    }

    private String getLocalized(String text) {
        return (text != null) ? Messages.getLocalizedMessage(text) : ""; //$NON-NLS-1$
    }

}

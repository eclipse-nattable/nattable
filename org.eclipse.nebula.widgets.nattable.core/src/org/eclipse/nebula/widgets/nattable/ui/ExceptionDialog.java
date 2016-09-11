/*******************************************************************************
 * Copyright (c) 2016 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog that can be used to show an error with an additional stacktrace.
 *
 * @since 1.5
 */
public class ExceptionDialog extends Dialog {

    private static final Log LOG = LogFactory.getLog(ExceptionDialog.class);

    private String title;
    private String message;
    private Exception exception;

    private Button detailsButton;
    private Text exceptionText;

    private boolean exceptionAreaCreated = false;

    protected ExceptionDialog(Shell parentShell, String title, String message, Exception exception) {
        super(parentShell);

        this.title = title;
        this.message = message != null ? message : exception.getLocalizedMessage();
        this.exception = exception;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.numColumns = 2;
        composite.setLayout(layout);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
        createMessageArea(composite);

        return composite;
    }

    protected Control createMessageArea(Composite composite) {
        // create composite
        // create image
        Image image = getParentShell().getDisplay().getSystemImage(SWT.ICON_ERROR);
        Label imageLabel = new Label(composite, SWT.NULL);
        image.setBackground(imageLabel.getBackground());
        imageLabel.setImage(image);
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(imageLabel);

        // create message
        if (this.message != null) {
            Label messageLabel = new Label(composite, SWT.WRAP);
            messageLabel.setText(this.message);
            GridDataFactory
                    .fillDefaults()
                    .align(SWT.FILL, SWT.CENTER)
                    .grab(true, false)
                    .hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
                    .applyTo(messageLabel);
        }

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Details buttons
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        if (this.exception != null) {
            this.detailsButton = createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL, false);
        }
    }

    @Override
    protected void buttonPressed(int id) {
        if (id == IDialogConstants.DETAILS_ID) {
            // was the details button pressed?
            toggleDetailsArea();
        } else {
            super.buttonPressed(id);
        }
    }

    private void toggleDetailsArea() {
        boolean opened = false;
        Point windowSize = getShell().getSize();
        if (this.exceptionAreaCreated) {
            this.exceptionText.dispose();
            this.exceptionAreaCreated = false;
            this.detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
            opened = false;
        } else {
            this.exceptionText = createExceptionText((Composite) getContents());
            this.detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
            getContents().getShell().layout();
            opened = true;
        }
        Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        int diffY = newSize.y - windowSize.y;
        // increase the dialog height if details were opened and such increase
        // is necessary
        // decrease the dialog height if details were closed and empty space
        // appeared
        if ((opened && diffY > 0) || (!opened && diffY < 0)) {
            getShell().setSize(new Point(windowSize.x, windowSize.y + (diffY)));
        }
    }

    private Text createExceptionText(Composite parent) {
        Text exceptionText = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridDataFactory
                .fillDefaults()
                .grab(true, true)
                .span(2, 1)
                .applyTo(exceptionText);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            this.exception.printStackTrace(pw);
            exceptionText.setText(sw.toString());
        } finally {
            try {
                sw.close();
                pw.close();
            } catch (IOException e) {
                LOG.error("Closing stream failed", e); //$NON-NLS-1$
            }
        }
        this.exceptionAreaCreated = true;
        return exceptionText;
    }

    /**
     * Opens an error dialog to display the given error.
     *
     * @param parentShell
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the title to use for this dialog, or <code>null</code> to
     *            indicate that the default title should be used
     * @param exception
     *            the error to show to the user
     * @return the code of the button that was pressed that resulted in this
     *         dialog closing. This will be <code>Dialog.OK</code> if the OK
     *         button was pressed, or <code>Dialog.CANCEL</code> if this
     *         dialog's close window decoration or the ESC key was used.
     */
    public static int open(Shell parentShell, String title, Exception exception) {
        return open(parentShell, title, exception.getLocalizedMessage(), exception);
    }

    /**
     * Opens an error dialog to display the given error.
     *
     * @param parentShell
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the title to use for this dialog, or <code>null</code> to
     *            indicate that the default title should be used
     * @param message
     *            the message to show in this dialog, or <code>null</code> to
     *            indicate that the error's message should be shown as the
     *            primary message
     * @param exception
     *            the error to show to the user
     * @return the code of the button that was pressed that resulted in this
     *         dialog closing. This will be <code>Dialog.OK</code> if the OK
     *         button was pressed, or <code>Dialog.CANCEL</code> if this
     *         dialog's close window decoration or the ESC key was used.
     */
    public static int open(Shell parentShell, String title, String message, Exception exception) {
        ExceptionDialog dialog = new ExceptionDialog(parentShell, title, message, exception);
        return dialog.open();
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(this.title);
        newShell.setImage(getParentShell().getDisplay().getSystemImage(SWT.ICON_ERROR));
    }
}

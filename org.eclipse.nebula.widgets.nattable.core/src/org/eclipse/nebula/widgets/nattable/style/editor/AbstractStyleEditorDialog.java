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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractStyleEditorDialog extends Dialog {

    private boolean cancelPressed = false;
    private Point location;

    public AbstractStyleEditorDialog(Shell parent) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    }

    public void setLocation(Point location) {
        this.location = new Point(location.x, location.y);
    }

    private void setLocation(Shell shell) {
        if (this.location != null) {
            if (this.location.x < getParent().getDisplay().getBounds().x) {
                this.location.x = getParent().getDisplay().getBounds().x;
            } else if (this.location.x + shell.getBounds().width > getParent()
                    .getDisplay().getBounds().x
                    + getParent().getDisplay().getBounds().width) {
                this.location.x = getParent().getDisplay().getBounds().x
                        + getParent().getDisplay().getBounds().width
                        - shell.getBounds().width;
            }
            if (this.location.y + shell.getBounds().height > getParent()
                    .getDisplay().getBounds().y
                    + getParent().getDisplay().getBounds().height) {
                this.location.y = getParent().getDisplay().getBounds().y
                        + getParent().getDisplay().getBounds().height
                        - shell.getBounds().height;
            }
            shell.setLocation(this.location);
        }
    }

    /**
     * Create all widgets to be displayed in the editor
     */
    protected abstract void initComponents(Shell shell);

    /**
     * Initialize and display the SWT shell. This is a blocking call.
     */
    public void open() {
        Shell shell = new Shell(getParent(), getStyle());
        shell.setImage(GUIHelper.getImage("preferences")); //$NON-NLS-1$
        shell.setText(getText());

        initComponents(shell);
        createButtons(shell);

        shell.pack();
        setLocation(shell);
        shell.open();
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }

    /**
     * Create OK, Reset and Cancel buttons
     */
    protected void createButtons(final Shell shell) {
        Composite buttonPanel = new Composite(shell, SWT.NONE);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.makeColumnsEqualWidth = false;
        gridLayout.horizontalSpacing = 2;
        buttonPanel.setLayout(gridLayout);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(buttonPanel);

        Button okButton = new Button(buttonPanel, SWT.PUSH);
        okButton.setText(Messages
                .getString("AbstractStyleEditorDialog.okButton")); //$NON-NLS-1$
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                doFormOK(shell);
            }
        });
        GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM)
                .minSize(70, 25).grab(true, true).applyTo(okButton);

        Button clearButton = new Button(buttonPanel, SWT.PUSH);
        clearButton.setText(Messages
                .getString("AbstractStyleEditorDialog.clearButton")); //$NON-NLS-1$
        clearButton.setToolTipText(Messages
                .getString("AbstractStyleEditorDialog.clearButtonTooltip")); //$NON-NLS-1$
        clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                doFormClear(shell);
            }
        });
        GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM)
                .minSize(80, 25).grab(false, false).applyTo(clearButton);

        Button cancelButton = new Button(buttonPanel, SWT.NONE);
        cancelButton.setText(Messages
                .getString("AbstractStyleEditorDialog.cancelButton")); //$NON-NLS-1$
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                doFormCancel(shell);
            }
        });
        GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM)
                .minSize(80, 25).grab(false, false).applyTo(cancelButton);

        shell.setDefaultButton(okButton);
    }

    /**
     * Respond to the OK button press. Read new state from the form.
     */
    protected abstract void doFormOK(Shell shell);

    protected void doFormCancel(Shell shell) {
        this.cancelPressed = true;
        shell.dispose();
    }

    protected void doFormClear(Shell shell) {
        shell.dispose();
    }

    public boolean isCancelPressed() {
        return this.cancelPressed;
    }
}

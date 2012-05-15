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
package org.eclipse.nebula.widgets.nattable.columnChooser.gui;


import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractColumnChooserDialog extends Dialog {

	protected ListenerList listeners = new ListenerList();
	private IDialogSettings dialogSettings;;

	public AbstractColumnChooserDialog(Shell parent) {
		super(parent);
		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, Messages.getString("AbstractColumnChooserDialog.doneButton"), true); //$NON-NLS-1$
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		composite.setLayout(new GridLayout(1, true));

		composite.getShell().setText(Messages.getString("AbstractColumnChooserDialog.shellTitle")); //$NON-NLS-1$
		composite.getShell().setImage(GUIHelper.getImage("preferences")); //$NON-NLS-1$

		populateDialogArea(composite);

		Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, false).span(((GridLayout)composite.getLayout()).numColumns, 1).applyTo(separator);

		return composite;
	}

	protected abstract void populateDialogArea(Composite composite);

	protected void createLabels(Composite parent, String availableStr, String selectedStr) {
		boolean availableSet = StringUtils.isNotEmpty(availableStr);
		boolean selectedSet = StringUtils.isNotEmpty(selectedStr);

		if (availableSet && selectedSet) {
			if (availableSet) {
				Label availableLabel = new Label(parent, SWT.NONE);
				availableLabel.setText(availableStr);
				GridDataFactory.swtDefaults().applyTo(availableLabel);
			}

			Label filler = new Label(parent, SWT.NONE);
			GridDataFactory.swtDefaults().span(availableSet ? 1 : 2, 1).applyTo(filler);

			if (selectedSet) {
				Label selectedLabel = new Label(parent, SWT.NONE);
				selectedLabel.setText(selectedStr);
				GridDataFactory.swtDefaults().span(2, 1).applyTo(selectedLabel);
			}
		}
	}

	public void addListener(Object listener) {
		listeners.add(listener);
	}

	public void removeListener(Object listener) {
		listeners.remove(listener);
	}

	@Override
	protected Point getInitialSize() {
		if (dialogSettings == null) {
			return new Point(500, 350);
		}
		Point initialSize = super.getInitialSize();
		return initialSize.x < 500 && initialSize.y < 350 ? new Point(500, 350) : initialSize;
	}

	public void setDialogSettings(IDialogSettings dialogSettings) {
		this.dialogSettings = dialogSettings;
	}
	
	protected IDialogSettings getDialogBoundsSettings() {
		return dialogSettings;
	}
}

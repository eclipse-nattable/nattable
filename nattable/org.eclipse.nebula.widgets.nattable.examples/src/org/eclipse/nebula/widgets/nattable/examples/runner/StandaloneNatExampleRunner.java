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
package org.eclipse.nebula.widgets.nattable.examples.runner;


import org.eclipse.nebula.widgets.nattable.examples.INatExample;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class StandaloneNatExampleRunner {

	public static void run(INatExample example) {
		run(800, 800, example);
	}

	public static void run(int shellWidth, int shellHeight, INatExample example) {
		// Setup
		Display display = Display.getDefault();
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setLayout(new FillLayout());
		shell.setSize(shellWidth, shellHeight);
		shell.setText(example.getName());

		// Create example control
		Control exampleControl = example.createExampleControl(shell);

		// Start
		example.onStart();

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		// Stop
		example.onStop();

		exampleControl.dispose();

		shell.dispose();
		display.dispose();
	}

}

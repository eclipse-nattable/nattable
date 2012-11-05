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
package org.eclipse.nebula.widgets.nattable.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class FileOutputStreamProvider implements IOutputStreamProvider {

	protected String defaultFileName;
	protected String[] defaultFilterNames;
	protected String[] defaultFilterExtensions;

	public FileOutputStreamProvider(String defaultFileName, String[] defaultFilterNames, String[] defaultFilterExtensions) {
		this.defaultFileName = defaultFileName;
		this.defaultFilterNames = defaultFilterNames;
		this.defaultFilterExtensions = defaultFilterExtensions;
	}
	
	public OutputStream getOutputStream(Shell shell) {
		FileDialog dialog = new FileDialog (shell, SWT.SAVE);
		
		String filterPath;
		String relativeFileName;
		
		int lastIndexOfFileSeparator = defaultFileName.lastIndexOf(File.separator);
		if (lastIndexOfFileSeparator >= 0) {
			filterPath = defaultFileName.substring(0, lastIndexOfFileSeparator);
			relativeFileName = defaultFileName.substring(lastIndexOfFileSeparator + 1);
		} else {
			filterPath = "/"; //$NON-NLS-1$
			relativeFileName = defaultFileName;
		}
		
		dialog.setFilterPath(filterPath);
		dialog.setOverwrite(true);

		dialog.setFileName(relativeFileName);
		dialog.setFilterNames(defaultFilterNames);
		dialog.setFilterExtensions(defaultFilterExtensions);
		String fileName = dialog.open();
		if (fileName == null) {
			return null;
		}
		
		try {
			return new PrintStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}

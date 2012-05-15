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

	protected String fileName;
	protected String[] filterNames;
	protected String[] filterExtensions;

	public FileOutputStreamProvider(String fileName, String[] filterNames, String[] filterExtensions) {
		this.fileName = fileName;
		this.filterNames = filterNames;
		this.filterExtensions = filterExtensions;
	}
	
	public OutputStream getOutputStream(Shell shell) {
		FileDialog dialog = new FileDialog (shell, SWT.SAVE);
		
		String filterPath;
		String relativeFileName;
		
		int lastIndexOfFileSeparator = fileName.lastIndexOf(File.separator);
		if (lastIndexOfFileSeparator >= 0) {
			filterPath = fileName.substring(0, lastIndexOfFileSeparator);
			relativeFileName = fileName.substring(lastIndexOfFileSeparator + 1);
		} else {
			filterPath = "/"; //$NON-NLS-1$
			relativeFileName = fileName;
		}
		
		dialog.setFilterPath(filterPath);
		dialog.setOverwrite(true);

		dialog.setFileName(relativeFileName);
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);
		fileName = dialog.open();
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
	
	/**
	 * This will be the default file name initially; after the dialog is closed this will be the user entered file name.
	 */
	public String getFileName() {
		return fileName;
	}
	
}

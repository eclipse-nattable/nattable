/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
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
import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Implementation of IOutputStreamProvider that will open a {@link FileDialog}
 * on requesting an OutputStream, to let a user specify the location to write a
 * file.
 */
public class FileOutputStreamProvider implements IOutputStreamProvider {

    protected String defaultFileName;
    protected String[] defaultFilterNames;
    protected String[] defaultFilterExtensions;

    protected String currentFileName;

    public FileOutputStreamProvider(
            String defaultFileName, String[] defaultFilterNames, String[] defaultFilterExtensions) {
        this.defaultFileName = defaultFileName;
        this.defaultFilterNames = defaultFilterNames;
        this.defaultFilterExtensions = defaultFilterExtensions;
    }

    /**
     * Opens a {@link FileDialog} to let a user choose the location to write the
     * export to, and returns the corresponding {@link PrintStream} to that
     * file.
     */
    @Override
    public OutputStream getOutputStream(Shell shell) {
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);

        String filterPath;
        String relativeFileName;

        int lastIndexOfFileSeparator = this.defaultFileName.lastIndexOf(File.separator);
        if (lastIndexOfFileSeparator >= 0) {
            filterPath = this.defaultFileName.substring(0, lastIndexOfFileSeparator);
            relativeFileName = this.defaultFileName.substring(lastIndexOfFileSeparator + 1);
        } else {
            filterPath = "/"; //$NON-NLS-1$
            relativeFileName = this.defaultFileName;
        }

        dialog.setFilterPath(filterPath);
        dialog.setOverwrite(true);

        dialog.setFileName(relativeFileName);
        dialog.setFilterNames(this.defaultFilterNames);
        dialog.setFilterExtensions(this.defaultFilterExtensions);
        this.currentFileName = dialog.open();
        if (this.currentFileName == null) {
            return null;
        }

        try {
            return new PrintStream(this.currentFileName);
        } catch (Exception e) {
            throw new RuntimeException(
                    Messages.getString("FileOutputStreamProvider.errorMessage", this.currentFileName), e); //$NON-NLS-1$
        }
    }

    @Override
    public File getResult() {
        return (this.currentFileName != null) ? new File(this.currentFileName) : null;
    }
}

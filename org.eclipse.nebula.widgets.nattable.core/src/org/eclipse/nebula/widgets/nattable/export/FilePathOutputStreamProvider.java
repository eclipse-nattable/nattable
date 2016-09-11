/*******************************************************************************
 * Copyright (c) 2016 Uwe Peuker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Uwe Peuker <dev@upeuker.net> - Bug 500789 - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.swt.widgets.Shell;

/**
 * Simple IOutputStreamProvider for a given filepath, no user interaction
 * necessary.
 *
 * Useful for integration of a fileselector anywere in the UI.
 *
 * @since 1.5
 */
public class FilePathOutputStreamProvider implements IOutputStreamProvider {

    private final String filePath;
    private OutputStream stream = null;

    public FilePathOutputStreamProvider(final String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Object getResult() {
        if (this.stream == null) {
            return null;
        }

        return new File(this.filePath);
    }

    @Override
    public OutputStream getOutputStream(final Shell shell) {
        try {
            this.stream = new PrintStream(this.filePath);
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(
                    Messages.getString("FileOutputStreamProvider.errorMessage", this.filePath), e); //$NON-NLS-1$
        }

        return this.stream;
    }
}

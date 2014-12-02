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

import java.io.OutputStream;

import org.eclipse.swt.widgets.Shell;

/**
 * Interface for specifying a provider for OutputStreams. Using this adds
 * support like e.g. dynamically creating an OutputStream with user interaction,
 * like opening a file selection dialog.
 */
public interface IOutputStreamProvider {

    /**
     * @param shell
     *            The current active Shell. Needed to add support for user
     *            interactions on specifying the OutputStream for example by
     *            opening a dialog.
     * @return The OutputStream to perform write operations to.
     */
    OutputStream getOutputStream(Shell shell);

    /**
     * @return The result that is produced by this IOutputStreamProvider.
     *         Usually this is the place where the OutputStream that is produced
     *         by this provider is pointing to (e.g. the file to which the
     *         OutputStream points to).
     */
    Object getResult();
}

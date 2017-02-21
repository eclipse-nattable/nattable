/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.export;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

/**
 * Interface for an exporter that can be used to export a NatTable. This type of
 * exporter gives the implementor full control on how the export should be done.
 * In comparison the {@link ILayerExporter} is used to export a NatTable data
 * driven by iterating over the content.
 *
 * @since 1.5
 */
public interface ITableExporter extends IExporter {

    /**
     * Export a given {@link ILayer} to the given {@link OutputStream}.
     *
     * @param shell
     *            The current active {@link Shell}.
     * @param progressBar
     *            The {@link ProgressBar} that can be used to report the export
     *            progress to the user.
     * @param outputStream
     *            The {@link OutputStream} to write the export to. Typically
     *            previously retrieved by calling
     *            {@link #getOutputStream(Shell)}.
     * @param layer
     *            The {@link ILayer} to export, typically a NatTable instance.
     * @param configRegistry
     *            The {@link IConfigRegistry} used to retrieve configuration
     *            information on exporting.
     * @throws IOException
     *             If an error occurs while exporting.
     */
    void exportTable(Shell shell,
            ProgressBar progressBar,
            OutputStream outputStream,
            ILayer layer,
            IConfigRegistry configRegistry) throws IOException;

}

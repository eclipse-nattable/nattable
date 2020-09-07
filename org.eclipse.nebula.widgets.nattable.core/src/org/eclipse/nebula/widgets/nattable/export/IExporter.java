/*******************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.export;

import java.io.OutputStream;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.widgets.Shell;

/**
 * Base interface for NatTable exporters to get the {@link OutputStream} and the
 * export result.
 *
 * @since 1.5
 */
public interface IExporter {

    /**
     * Get the {@link OutputStream} to which the export should be written to.
     *
     * @param shell
     *            The {@link Shell} to which the {@link ILayer} to export is
     *            connected to. Necessary to support user interactions via
     *            dialogs on configuring the output location.
     * @return The {@link OutputStream} to write the export to.
     */
    OutputStream getOutputStream(Shell shell);

    /**
     * @return The result that is produced by this {@link ITableExporter}.
     *         Usually the file that is created or written by this exporter.
     */
    Object getResult();

}

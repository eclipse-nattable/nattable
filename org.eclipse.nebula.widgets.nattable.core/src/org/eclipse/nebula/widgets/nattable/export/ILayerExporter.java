/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.export;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

/**
 * An ILayerExporter is used to export a NatTable to an external format. Usually
 * such an external format is some kind of Excel format. But it is also possible
 * to implement an exporter that exports to another format.
 * <p>
 * The ILayerExporter is registered to the IConfigRegistry via
 * {@link ExportConfigAttributes#EXPORTER} configuration attribute and used by
 * the {@link NatExporter} to perform the export.
 *
 * @see NatExporter
 * @see ExportConfigAttributes
 */
public interface ILayerExporter extends IExporter {

    /**
     * Need to be called only once at the beginning of an export operation. It
     * is used to initialize the export operation like e.g. letting a user
     * specify the export location via file selection dialog or creating a
     * workbook.
     * <p>
     * Note: Also on exporting multiple NatTable instances as part of a single
     * export operation, this method should only be called once before any
     * layers are exported.
     *
     * @param outputStream
     *            The OutputStream to write the export to.
     * @throws IOException
     *             If the beginning of an export already performs I/O operations
     *             that fail.
     */
    void exportBegin(OutputStream outputStream) throws IOException;

    /**
     * Need to be called only once at the end of an export operation. It is used
     * to cleanup resources after the export operation, like e.g. closing opened
     * streams.
     * <p>
     * Note: Also on exporting multiple NatTable instances as part of a single
     * export operation, this method should only be called once after all layers
     * are exported.
     *
     * @param outputStream
     *            The OutputStream to write the export to.
     * @throws IOException
     *             If finishing the export operation fails on an I/O operation.
     */
    void exportEnd(OutputStream outputStream) throws IOException;

    /**
     * Starts the export operation of one ILayer. Is used for example to
     * initialize a sheet in a workbook or open the root tags in a XML format.
     * <p>
     * On exporting multiple NatTable instances, this method needs to be called
     * once for every instance.
     *
     * @param outputStream
     *            The OutputStream to write the export to.
     * @param layerName
     *            The name that should be used as sheet name.
     * @throws IOException
     *             If an error occurred during writing the export.
     */
    void exportLayerBegin(OutputStream outputStream, String layerName)
            throws IOException;

    /**
     * Ends the export operation of one ILayer. Is used for example to finish
     * the export, like closing tags in a XML format.
     * <p>
     * On exporting multiple NatTable instances, this method needs to be called
     * once for every instance.
     *
     * @param outputStream
     *            The OutputStream to write the export to.
     * @param layerName
     *            The name that is used as sheet name. Usually not necessary,
     *            but in case there is caching involved in a custom
     *            ILayerExporter implementation, this can be used to retrieve
     *            the ILayer instance again.
     * @throws IOException
     *             If an error occurred during writing the export.
     */
    void exportLayerEnd(OutputStream outputStream, String layerName)
            throws IOException;

    /**
     * Starts the export operation of one row. Is used for example to initialize
     * a row in a sheet or open some tags in a XML format.
     *
     * @param outputStream
     *            The OutputStream to write the export to.
     * @param rowPosition
     *            The position of the row to export.
     * @throws IOException
     *             If an error occurred during writing the export.
     */
    void exportRowBegin(OutputStream outputStream, int rowPosition)
            throws IOException;

    /**
     * Ends the export operation of one row.
     *
     * @param outputStream
     *            The OutputStream to write the export to.
     * @param rowPosition
     *            The position of the row that was exported. Usually not
     *            necessary, but in case there is caching involved in a custom
     *            ILayerExporter implementation, this can be used to retrieve
     *            the row again.
     * @throws IOException
     *             If an error occurred during writing the export.
     */
    void exportRowEnd(OutputStream outputStream, int rowPosition)
            throws IOException;

    /**
     * Exports one cell.
     *
     * @param outputStream
     *            The OutputStream to write the export to.
     * @param exportDisplayValue
     *            The value that will be written to the export file. This value
     *            is determined by using the data value of the ILayerCell and
     *            the registered IExportFormatter within the NatExporter.
     * @param cell
     *            The ILayerCell that is currently exported.
     * @param configRegistry
     *            The ConfigRegistry to retrieve the registered style
     *            information of the cell that is currently exported.
     * @throws IOException
     *             If an error occurred during writing the export.
     */
    void exportCell(OutputStream outputStream, Object exportDisplayValue,
            ILayerCell cell, IConfigRegistry configRegistry) throws IOException;

}

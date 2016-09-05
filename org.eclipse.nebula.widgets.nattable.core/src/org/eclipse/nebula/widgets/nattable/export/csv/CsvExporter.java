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
package org.eclipse.nebula.widgets.nattable.export.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.export.FileOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.export.FilePathOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.export.ILayerExporter;
import org.eclipse.nebula.widgets.nattable.export.IOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.widgets.Shell;

/**
 * Simple CSV-Exporter ignoring cell spans.
 *
 * @since 1.5
 */
public class CsvExporter implements ILayerExporter {

    private static final String CSV_FILE_EXTENSION = "*.csv"; //$NON-NLS-1$
    private static final String CSV_FILE_FILTER = "CSV (*.csv)"; //$NON-NLS-1$
    private static final String DEFAULT_EXPORT_FILE_NAME = "csv_export.csv"; //$NON-NLS-1$
    private final IOutputStreamProvider outputStreamProvider;
    private String charset = "windows-1252"; //$NON-NLS-1$
    private String delimiter = ";"; //$NON-NLS-1$

    /* used during export runtime. */
    private final StringBuilder currentRow = new StringBuilder(2048);
    private boolean rowCellInserted;
    private Charset usedCharset;

    /**
     * Creates a CsvExporter using the given stream provider which defines the
     * destination for the export.
     *
     * If the stream provider ist set to null a default
     * {@link FileOutputStreamProvider} will be used.
     *
     * @param outputStreamProvider
     *            the used stream provider
     */
    public CsvExporter(final IOutputStreamProvider outputStreamProvider) {
        if (outputStreamProvider == null) {
            this.outputStreamProvider = new FileOutputStreamProvider(
                    DEFAULT_EXPORT_FILE_NAME,
                    new String[] { CSV_FILE_FILTER },
                    new String[] { CSV_FILE_EXTENSION });
        } else {
            this.outputStreamProvider = outputStreamProvider;
        }
    }

    /**
     * Creates a CsvExporter using a default {@link FileOutputStreamProvider} to
     * define the destination for the export.
     */
    public CsvExporter() {
        this((IOutputStreamProvider) null);
    }

    /**
     * Creates a CsvExporter using a {@link FilePathOutputStreamProvider}.
     *
     * The destination fpr the export will be a file with the given path.
     *
     * @param filePath
     *            the path of the export file
     */
    public CsvExporter(final String filePath) {
        this.outputStreamProvider = new FilePathOutputStreamProvider(filePath);
    }

    @Override
    public OutputStream getOutputStream(final Shell shell) {
        return this.outputStreamProvider.getOutputStream(shell);
    }

    @Override
    public Object getResult() {
        return this.outputStreamProvider.getResult();
    }

    @Override
    public void exportBegin(final OutputStream outputStream) throws IOException {
        this.usedCharset = Charset.forName(this.charset);
    }

    @Override
    public void exportEnd(final OutputStream outputStream) throws IOException {
        // do noting
    }

    @Override
    public void exportLayerBegin(final OutputStream outputStream, final String layerName) throws IOException {
        // do nothing
    }

    @Override
    public void exportLayerEnd(final OutputStream outputStream, final String layerName) throws IOException {
        // do nothing
    }

    @Override
    public void exportRowBegin(final OutputStream outputStream, final int rowPosition) throws IOException {
        this.currentRow.setLength(0);
        this.rowCellInserted = false;
    }

    @Override
    public void exportRowEnd(final OutputStream outputStream, final int rowPosition) throws IOException {
        this.currentRow.append(System.getProperty("line.separator")); //$NON-NLS-1$
        outputStream.write(this.currentRow.toString().getBytes(this.usedCharset));
    }

    @Override
    public void exportCell(
            final OutputStream outputStream,
            final Object exportDisplayValue,
            final ILayerCell cell,
            final IConfigRegistry configRegistry) throws IOException {

        if (this.rowCellInserted) {
            this.currentRow.append(this.delimiter);
        } else {
            this.rowCellInserted = true;
        }

        if (exportDisplayValue != null) {
            this.currentRow.append(exportDisplayValue);
        }
    }

    /**
     * Defines the name of the charset which should be used as encoding for the
     * export file.
     *
     * The default ist set to windows-1252.
     *
     * @param charset
     *            the name of the charset
     */
    public void setCharset(final String charset) {
        this.charset = charset;
    }

    /**
     * Defines the delimiter which should be used between columns.
     *
     * The default is the character ';'.
     *
     * @param delimiter
     *            the column delimiter
     */
    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }
}

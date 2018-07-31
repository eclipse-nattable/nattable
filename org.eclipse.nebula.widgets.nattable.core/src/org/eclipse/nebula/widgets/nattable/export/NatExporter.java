/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Uwe Peuker <dev@upeuker.net> - Bug 500788
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.export;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.formula.command.DisableFormulaEvaluationCommand;
import org.eclipse.nebula.widgets.nattable.formula.command.EnableFormulaEvaluationCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.print.command.PrintEntireGridCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOffCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOnCommand;
import org.eclipse.nebula.widgets.nattable.resize.AutoResizeHelper;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.summaryrow.command.CalculateSummaryRowValuesCommand;
import org.eclipse.nebula.widgets.nattable.ui.ExceptionDialog;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

/**
 * This class is used to perform exports of a NatTable or {@link ILayer} in a
 * NatTable composition. The exporter to use can be configured via
 * {@link IConfigRegistry} or directly given as method parameter.
 *
 * @see ExportConfigAttributes#EXPORTER
 * @see ExportConfigAttributes#TABLE_EXPORTER
 */
public class NatExporter {

    private static final Log LOG = LogFactory.getLog(NatExporter.class);

    /**
     * The {@link Shell} that should be used to open sub-dialogs and perform
     * export operations in a background thread.
     *
     * @since 1.5
     */
    protected final Shell shell;
    /**
     * Flag that indicates if the created export result should be opened after
     * the export is finished.
     *
     * @since 1.5
     */
    protected boolean openResult = true;
    /**
     * Flag that indicates that the export succeeded. Used to determine whether
     * the export result can be opened or not.
     *
     * @since 1.5
     */
    protected boolean exportSucceeded = true;
    /**
     * Flag to configure whether in-memory pre-rendering is enabled or not. This
     * is necessary in case content painters are used that are configured for
     * content based auto-resizing.
     *
     * @since 1.5
     */
    protected boolean preRender = true;
    /**
     * Flag to configure whether the export should be performed asynchronously
     * or synchronously. By default this flag is set to <code>true</code> and
     * the decision whether the execution should be performed synchronously or
     * not is made based on whether a {@link Shell} is set or not. If a
     * {@link Shell} is set and this flag is set to <code>false</code> the
     * execution is performed synchronously.
     *
     * @since 1.6
     */
    private boolean runAsynchronously = true;

    /**
     * Create a new {@link NatExporter}.
     *
     * @param shell
     *            The {@link Shell} that should be used to open sub-dialogs and
     *            perform export operations in a background thread. Can be
     *            <code>null</code> but could lead to
     *            {@link NullPointerException}s if {@link IExporter} are
     *            configured, that use a {@link FileOutputStreamProvider}.
     */
    public NatExporter(Shell shell) {
        this(shell, false);
    }

    /**
     * Create a new {@link NatExporter}.
     *
     * @param shell
     *            The {@link Shell} that should be used to open sub-dialogs and
     *            perform export operations in a background thread. Can be
     *            <code>null</code> but could lead to
     *            {@link NullPointerException}s if {@link IExporter} are
     *            configured, that use a {@link FileOutputStreamProvider}.
     * @param executeSynchronously
     *            Configure whether the export should be performed
     *            asynchronously or synchronously. By default the decision
     *            whether the execution should be performed synchronously or not
     *            is made based on whether a {@link Shell} is set or not. If a
     *            {@link Shell} is set and this flag is set to <code>true</code>
     *            the execution is performed synchronously.
     *
     * @since 1.6
     */
    public NatExporter(Shell shell, boolean executeSynchronously) {
        this.shell = shell;
        this.runAsynchronously = !executeSynchronously;
    }

    /**
     * Exports a single {@link ILayer} using the {@link ILayerExporter}
     * registered in the {@link IConfigRegistry} for the key
     * {@link ExportConfigAttributes#EXPORTER}.
     *
     * @param layer
     *            The {@link ILayer} to export, usually a NatTable instance.
     * @param configRegistry
     *            The {@link IConfigRegistry} of the NatTable instance to
     *            export, that contains the necessary export configurations.
     */
    public void exportSingleLayer(
            final ILayer layer,
            final IConfigRegistry configRegistry) {

        ILayerExporter exporter = configRegistry.getConfigAttribute(
                ExportConfigAttributes.EXPORTER,
                DisplayMode.NORMAL);

        exportSingleLayer(exporter, layer, configRegistry);
    }

    /**
     * Exports a single {@link ILayer} using the given {@link ILayerExporter}.
     *
     * @param exporter
     *            The {@link ILayerExporter} to use for exporting.
     * @param layer
     *            The {@link ILayer} to export, usually a NatTable instance.
     * @param configRegistry
     *            The {@link IConfigRegistry} of the NatTable instance to
     *            export, that contains the necessary export configurations.
     *
     * @since 1.5
     */
    public void exportSingleLayer(
            final ILayerExporter exporter,
            final ILayer layer,
            final IConfigRegistry configRegistry) {

        exportSingle(exporter, new BiConsumer<ILayerExporter, OutputStream>() {
            @Override
            public void apply(ILayerExporter exporter, OutputStream outputStream) {
                try {
                    exporter.exportBegin(outputStream);

                    exportLayer(exporter, outputStream, "", layer, configRegistry); //$NON-NLS-1$

                    exporter.exportEnd(outputStream);
                } catch (IOException e) {
                    // exception is handled in the caller
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Exports a single {@link ILayer} using the {@link ILayerExporter}
     * registered in the {@link IConfigRegistry} for the key
     * {@link ExportConfigAttributes#EXPORTER}.
     *
     * @param layer
     *            The {@link ILayer} to export, usually a NatTable instance.
     * @param configRegistry
     *            The {@link IConfigRegistry} of the NatTable instance to
     *            export, that contains the necessary export configurations.
     *
     * @since 1.5
     */
    public void exportSingleTable(
            final ILayer layer,
            final IConfigRegistry configRegistry) {

        final ITableExporter exporter = configRegistry.getConfigAttribute(
                ExportConfigAttributes.TABLE_EXPORTER,
                DisplayMode.NORMAL);

        exportSingleTable(exporter, layer, configRegistry);
    }

    /**
     * Exports a single {@link ILayer} using the given {@link ITableExporter}.
     *
     * @param exporter
     *            The {@link ITableExporter} to use for exporting.
     * @param layer
     *            The {@link ILayer} to export, usually a NatTable instance.
     * @param configRegistry
     *            The {@link IConfigRegistry} of the NatTable instance to
     *            export, that contains the necessary export configurations.
     *
     * @since 1.5
     */
    public void exportSingleTable(
            final ITableExporter exporter,
            final ILayer layer,
            final IConfigRegistry configRegistry) {

        exportSingle(exporter, new BiConsumer<ITableExporter, OutputStream>() {
            @Override
            public void apply(ITableExporter exporter, OutputStream outputStream) {
                exportLayer(exporter, outputStream, layer, configRegistry);
            }
        });
    }

    /**
     * Functional interface used to specify how the export should be performed
     * for different exporter interface implementations. Can be removed once the
     * source API level is updated to Java 1.8
     *
     * @param <T>
     * @param <U>
     */
    private interface BiConsumer<T, U> {
        void apply(T t, U u);
    }

    /**
     *
     * @param exporter
     *            The {@link IExporter} to use for exporting.
     * @param executable
     *            The consumer implementation that is used to execute the export
     *            and produce the output to an {@link OutputStream}
     */
    private <T extends IExporter> void exportSingle(final T exporter, final BiConsumer<T, OutputStream> executable) {

        Runnable exportRunnable = new Runnable() {
            @Override
            public void run() {
                final OutputStream outputStream = getOutputStream(exporter);
                if (outputStream != null) {
                    try {
                        executable.apply(exporter, outputStream);

                        NatExporter.this.exportSucceeded = true;
                    } catch (Exception e) {
                        NatExporter.this.exportSucceeded = false;
                        handleExportException(e);
                    } finally {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            LOG.error("Failed to close the output stream", e); //$NON-NLS-1$
                        }
                    }

                    openExport(exporter);
                }
            }
        };

        if (this.shell != null) {
            // Run with the SWT display so that the progress bar can paint
            if (this.runAsynchronously) {
                this.shell.getDisplay().asyncExec(exportRunnable);
            } else {
                this.shell.getDisplay().syncExec(exportRunnable);
            }
        } else {
            // execute in the current thread
            exportRunnable.run();
        }
    }

    /**
     * Export multiple NatTable instances to one file by using the given
     * ILayerExporter.
     *
     * @param exporter
     *            The ILayerExporter to use for exporting.
     * @param natTablesMap
     *            The NatTable instances to export. They keys in the map will be
     *            used as sheet titles while the values are the instances to
     *            export.
     */
    public void exportMultipleNatTables(
            final ILayerExporter exporter,
            final Map<String, NatTable> natTablesMap) {
        exportMultipleNatTables(exporter, natTablesMap, false, null);
    }

    /**
     * Export multiple NatTable instances to one file by using the given
     * ILayerExporter.
     *
     * @param exporter
     *            The ILayerExporter to use for exporting.
     * @param natTablesMap
     *            The NatTable instances to export. They keys in the map will be
     *            used as sheet titles while the values are the instances to
     *            export.
     * @param exportOnSameSheet
     *            Flag to configure whether multiple NatTable instances should
     *            be exported on the same sheet or not.
     * @param sheetName
     *            The sheet name that should be used in case of exporting
     *            multiple NatTables on a single sheet.
     * @since 1.5
     */
    public void exportMultipleNatTables(
            final ILayerExporter exporter,
            final Map<String, NatTable> natTablesMap,
            final boolean exportOnSameSheet,
            final String sheetName) {

        Runnable exportRunnable = new Runnable() {
            @Override
            public void run() {
                final OutputStream outputStream = getOutputStream(exporter);
                if (outputStream != null) {
                    try {
                        exporter.exportBegin(outputStream);

                        if (exportOnSameSheet) {
                            exporter.exportLayerBegin(outputStream, sheetName);
                        }

                        for (String name : natTablesMap.keySet()) {
                            NatTable natTable = natTablesMap.get(name);
                            exportLayer(exporter, outputStream, name, natTable, natTable.getConfigRegistry(), !exportOnSameSheet);
                        }

                        if (exportOnSameSheet) {
                            exporter.exportLayerEnd(outputStream, sheetName);
                        }

                        exporter.exportEnd(outputStream);

                        NatExporter.this.exportSucceeded = true;
                    } catch (Exception e) {
                        NatExporter.this.exportSucceeded = false;
                        handleExportException(e);
                    } finally {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            LOG.error("Failed to close the output stream", e); //$NON-NLS-1$
                        }
                    }
                }

                openExport(exporter);
            }
        };

        if (this.shell != null) {
            // Run with the SWT display so that the progress bar can paint
            if (this.runAsynchronously) {
                this.shell.getDisplay().asyncExec(exportRunnable);
            } else {
                this.shell.getDisplay().syncExec(exportRunnable);
            }
        } else {
            exportRunnable.run();
        }
    }

    /**
     * Exports the given layer to the outputStream using the provided exporter.
     * The {@link ILayerExporter#exportBegin(OutputStream)} method should be
     * called before this method is invoked, and
     * {@link ILayerExporter#exportEnd(OutputStream)} should be called after
     * this method returns. If multiple layers are being exported as part of a
     * single logical export operation, then
     * {@link ILayerExporter#exportBegin(OutputStream)} will be called once at
     * the very beginning, followed by n calls to this method, and finally
     * followed by {@link ILayerExporter#exportEnd(OutputStream)}.
     *
     * <p>
     * <b>Note:</b> This method calls
     * {@link #exportLayer(ILayerExporter, OutputStream, String, ILayer, IConfigRegistry, boolean)}
     * with the parameter <i>initExportLayer</i> set to <code>true</code>.
     * </p>
     *
     * @param exporter
     *            The {@link ILayerExporter} that should be used for exporting.
     * @param outputStream
     *            The {@link OutputStream} that should be used to write the
     *            export to.
     * @param layerName
     *            The name that should be set as sheet name of the export.
     * @param layer
     *            The {@link ILayer} that should be exported.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve the export
     *            configurations.
     */
    protected void exportLayer(
            final ILayerExporter exporter,
            final OutputStream outputStream,
            final String layerName,
            final ILayer layer,
            final IConfigRegistry configRegistry) {

        exportLayer(exporter, outputStream, layerName, layer, configRegistry, true);
    }

    /**
     * Exports the given layer to the outputStream using the provided exporter.
     * The {@link ILayerExporter#exportBegin(OutputStream)} method should be
     * called before this method is invoked, and
     * {@link ILayerExporter#exportEnd(OutputStream)} should be called after
     * this method returns. If multiple layers are being exported as part of a
     * single logical export operation, then
     * {@link ILayerExporter#exportBegin(OutputStream)} will be called once at
     * the very beginning, followed by n calls to this method, and finally
     * followed by {@link ILayerExporter#exportEnd(OutputStream)}.
     *
     * @param exporter
     *            The {@link ILayerExporter} that should be used for exporting.
     * @param outputStream
     *            The {@link OutputStream} that should be used to write the
     *            export to.
     * @param layerName
     *            The name that should be set as sheet name of the export.
     * @param layer
     *            The {@link ILayer} that should be exported.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve the export
     *            configurations.
     * @param initExportLayer
     *            flag to configure whether
     *            {@link ILayerExporter#exportLayerBegin(OutputStream, String)}
     *            and
     *            {@link ILayerExporter#exportLayerEnd(OutputStream, String)}
     *            should be called or not. Should be set to <code>true</code> if
     *            multiple NatTable instances should be exported on the same
     *            sheet.
     * @since 1.5
     */
    protected void exportLayer(
            final ILayerExporter exporter,
            final OutputStream outputStream,
            final String layerName,
            final ILayer layer,
            final IConfigRegistry configRegistry,
            final boolean initExportLayer) {

        exportLayer(new ITableExporter() {

            @Override
            public void exportTable(Shell shell, ProgressBar progressBar, OutputStream outputStream,
                    ILayer layer, IConfigRegistry configRegistry) throws IOException {

                if (initExportLayer) {
                    exporter.exportLayerBegin(outputStream, layerName);
                }

                int layerHeight = layer.getHeight();

                for (int rowPosition = 0; rowPosition < layer.getRowCount(); rowPosition++) {
                    if (layer.getRowHeightByPosition(rowPosition) > 0
                            && layer.getStartYOfRowPosition(rowPosition) < layerHeight) {
                        exporter.exportRowBegin(outputStream, rowPosition);
                        if (progressBar != null) {
                            progressBar.setSelection(rowPosition);
                        }

                        for (int columnPosition = 0; columnPosition < layer.getColumnCount(); columnPosition++) {
                            ILayerCell cell = layer.getCellByPosition(columnPosition, rowPosition);
                            if (cell != null) {
                                IExportFormatter exportFormatter = configRegistry.getConfigAttribute(
                                        ExportConfigAttributes.EXPORT_FORMATTER,
                                        cell.getDisplayMode(),
                                        cell.getConfigLabels().getLabels());
                                Object exportDisplayValue = exportFormatter.formatForExport(cell, configRegistry);

                                exporter.exportCell(outputStream, exportDisplayValue, cell, configRegistry);
                            }
                        }

                        exporter.exportRowEnd(outputStream, rowPosition);
                    }
                }

                if (initExportLayer) {
                    exporter.exportLayerEnd(outputStream, layerName);
                }
            }

            @Override
            public OutputStream getOutputStream(Shell shell) {
                return exporter.getOutputStream(shell);
            }

            @Override
            public Object getResult() {
                return exporter.getResult();
            }
        }, outputStream, layer, configRegistry);
    }

    /**
     * Exports the given {@link ILayer} to the given {@link OutputStream} using
     * the provided {@link ITableExporter}.
     *
     * @param exporter
     *            The {@link ITableExporter} that should be used for exporting.
     * @param outputStream
     *            The {@link OutputStream} that should be used to write the
     *            export to.
     * @param layer
     *            The {@link ILayer} that should be exported.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve the export
     *            configurations.
     *
     * @since 1.5
     */
    protected void exportLayer(
            final ITableExporter exporter,
            final OutputStream outputStream,
            final ILayer layer,
            final IConfigRegistry configRegistry) {

        if (this.preRender) {
            AutoResizeHelper.autoResize(layer, configRegistry);
        }

        IClientAreaProvider originalClientAreaProvider = layer.getClientAreaProvider();

        // This needs to be done so that the layer can return all the cells
        // not just the ones visible in the viewport
        layer.doCommand(new TurnViewportOffCommand());
        setClientAreaToMaximum(layer);

        // if a SummaryRowLayer is in the layer stack, we need to ensure that
        // the values are calculated
        layer.doCommand(new CalculateSummaryRowValuesCommand());

        // if a FormulaDataProvider is involved, we need to ensure that the
        // formula evaluation is disabled so the formula itself is exported
        // instead of the calculated value
        layer.doCommand(new DisableFormulaEvaluationCommand());

        ProgressBar progressBar = null;

        if (this.shell != null) {
            Shell childShell = new Shell(this.shell.getDisplay(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
            childShell.setText(Messages.getString("NatExporter.exporting")); //$NON-NLS-1$

            int endRow = layer.getRowCount() - 1;

            progressBar = new ProgressBar(childShell, SWT.SMOOTH);
            progressBar.setMinimum(0);
            progressBar.setMaximum(endRow);
            progressBar.setBounds(0, 0, 400, 25);
            progressBar.setFocus();

            childShell.pack();
            childShell.open();
        }

        try {
            exporter.exportTable(this.shell, progressBar, outputStream, layer, configRegistry);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // These must be fired at the end of the thread execution
            layer.setClientAreaProvider(originalClientAreaProvider);
            layer.doCommand(new TurnViewportOnCommand());

            layer.doCommand(new EnableFormulaEvaluationCommand());

            if (progressBar != null) {
                Shell childShell = progressBar.getShell();
                progressBar.dispose();
                childShell.dispose();
            }
        }
    }

    /**
     * Increase the client area so it can include the whole {@link ILayer}.
     *
     * @param layer
     *            The {@link ILayer} for which the client area should be
     *            maximized.
     *
     * @since 1.5
     */
    protected void setClientAreaToMaximum(ILayer layer) {
        final Rectangle maxClientArea = new Rectangle(0, 0, layer.getWidth(), layer.getHeight());

        layer.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return maxClientArea;
            }
        });

        layer.doCommand(new PrintEntireGridCommand());
    }

    /**
     * Open the export result in the matching application.
     *
     * @param exporter
     *            The {@link IExporter} that was used to perform the export.
     *            Needed to access the export result.
     *
     * @since 1.5
     */
    protected void openExport(IExporter exporter) {
        if (this.exportSucceeded
                && this.openResult
                && exporter.getResult() != null
                && exporter.getResult() instanceof File) {
            Program.launch(((File) exporter.getResult()).getAbsolutePath());
        }
    }

    /**
     * Sets the behavior after finishing the export.
     *
     * The default is opening the created export file with the associated
     * application. You can prevent the opening by setting openResult to
     * <code>false</code>.
     *
     * @param openResult
     *            set to <code>true</code> to open the created export file,
     *            <code>false</code> otherwise
     *
     * @since 1.5
     */
    public void setOpenResult(boolean openResult) {
        this.openResult = openResult;
    }

    /**
     * Method that is used to retrieve the {@link OutputStream} to write the
     * export to in a safe way. Any occurring exception will be handled inside.
     *
     * @param exporter
     *            The {@link ILayerExporter} that should be used
     * @return The {@link OutputStream} that is used to write the export to or
     *         <code>null</code> if an error occurs.
     *
     * @since 1.5
     */
    protected OutputStream getOutputStream(IExporter exporter) {
        OutputStream outputStream = null;
        try {
            outputStream = exporter.getOutputStream(this.shell);
        } catch (Exception e) {
            handleExportException(e);
        }
        return outputStream;
    }

    /**
     * Method that is used to handle exceptions that are raised while processing
     * the export.
     *
     * @param e
     *            The exception that should be handled.
     * @since 1.5
     */
    protected void handleExportException(Exception e) {
        LOG.error("Failed to export.", e); //$NON-NLS-1$

        ExceptionDialog.open(
                this.shell,
                Messages.getString("ErrorDialog.title"), //$NON-NLS-1$
                Messages.getString("NatExporter.errorMessagePrefix", e.getLocalizedMessage()), //$NON-NLS-1$
                e);
    }

    /**
     * Enable in-memory pre-rendering. This is necessary in case content
     * painters are used that are configured for content based auto-resizing.
     *
     * @since 1.5
     */
    public void enablePreRendering() {
        this.preRender = true;
    }

    /**
     * Disable in-memory pre-rendering. You should consider to disable
     * pre-rendering if no content painters are used that are configured for
     * content based auto-resizing.
     *
     * @since 1.5
     */
    public void disablePreRendering() {
        this.preRender = false;
    }
}

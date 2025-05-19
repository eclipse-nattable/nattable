/*******************************************************************************
 * Copyright (c) 2012, 2025 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Uwe Peuker <dev@upeuker.net> - Bug 500788
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.export;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
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
import org.eclipse.nebula.widgets.nattable.util.PlatformHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to perform exports of a NatTable or {@link ILayer} in a
 * NatTable composition. The exporter to use can be configured via
 * {@link IConfigRegistry} or directly given as method parameter.
 *
 * @see ExportConfigAttributes#EXPORTER
 * @see ExportConfigAttributes#TABLE_EXPORTER
 */
public class NatExporter {

    private static final Logger LOG = LoggerFactory.getLogger(NatExporter.class);

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
     * Flag to configure whether the progress should be reported via
     * {@link ProgressMonitorDialog}. If set to <code>false</code> a custom
     * shell with a {@link ProgressBar} will be shown.
     *
     * @since 2.3
     */
    private boolean useProgressDialog = false;

    /**
     * The {@link Runnable} that should be executed after the export finished
     * successfully. Useful in case {@link #openResult} is set to
     * <code>false</code> so an alternative for reporting the export success can
     * be configured.
     *
     * @since 2.6
     */
    private Runnable successRunnable;

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
        this(shell, executeSynchronously, false);
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
     * @param useProgressDialog
     *            Configure whether the progress should be reported via
     *            {@link ProgressMonitorDialog}. If set to <code>false</code> a
     *            custom shell with a {@link ProgressBar} will be shown if the
     *            shell parameter is not <code>null</code>.
     *
     * @since 2.3
     */
    public NatExporter(Shell shell, boolean executeSynchronously, boolean useProgressDialog) {
        this(shell, executeSynchronously, useProgressDialog, true, null);
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
     * @param useProgressDialog
     *            Configure whether the progress should be reported via
     *            {@link ProgressMonitorDialog}. If set to <code>false</code> a
     *            custom shell with a {@link ProgressBar} will be shown if the
     *            shell parameter is not <code>null</code>.
     * @param openResult
     *            Configure if the created export result should be opened after
     *            the export is finished.
     * @param successRunnable
     *            The {@link Runnable} that should be executed after the export
     *            finished successfully. Useful in case {@link #openResult} is
     *            set to <code>false</code> so an alternative for reporting the
     *            export success can be configured.
     *
     * @since 2.6
     */
    public NatExporter(Shell shell, boolean executeSynchronously, boolean useProgressDialog, boolean openResult, Runnable successRunnable) {
        this.shell = shell;
        this.runAsynchronously = !executeSynchronously;
        this.useProgressDialog = useProgressDialog;
        this.openResult = openResult;
        this.successRunnable = successRunnable;
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

        if (this.useProgressDialog && this.shell != null) {

            ProgressMonitorDialog dialog = getProgressMonitorDialog();
            try {
                dialog.run(true, true, monitor -> {
                    exportSingle(exporter, monitor, (exp, outputStream, m) -> {
                        try {
                            exp.exportBegin(outputStream);

                            exportLayer(exp, outputStream, m, "", layer, configRegistry, true); //$NON-NLS-1$

                            exp.exportEnd(outputStream);
                        } catch (IOException e) {
                            // exception is handled in the caller
                            throw new RuntimeException(e);
                        }
                    });
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else {

            exportSingle(exporter, null, (exp, outputStream, monitor) -> {
                try {
                    exp.exportBegin(outputStream);

                    exportLayer(exp, outputStream, "", layer, configRegistry); //$NON-NLS-1$

                    exp.exportEnd(outputStream);
                } catch (IOException e) {
                    // exception is handled in the caller
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     *
     * @return The {@link ProgressMonitorDialog} that is used to report the
     *         export progress to a user, in case {@link #useProgressDialog} is
     *         <code>true</code>.
     * @see #useProgressDialog
     *
     * @since 2.3
     */
    protected ProgressMonitorDialog getProgressMonitorDialog() {
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(this.shell) {
            @Override
            protected void configureShell(Shell shell) {
                super.configureShell(shell);
                shell.setText(Messages.getString("NatExporter.exporting")); //$NON-NLS-1$
            }
        };

        return dialog;
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

        if (this.useProgressDialog && this.shell != null) {

            ProgressMonitorDialog dialog = getProgressMonitorDialog();
            try {
                dialog.run(true, true, monitor -> {
                    exportSingle(exporter, monitor, (exp, outputStream, m) -> exportLayer(exp, outputStream, m, layer, configRegistry));
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else {
            exportSingle(exporter, null, (exp, outputStream, m) -> exportLayer(exp, outputStream, m, layer, configRegistry));
        }
    }

    /**
     * Functional interface used to specify how the export should be performed
     * for different exporter interface implementations.
     *
     * @param <T>
     *            The {@link IExporter} to use for exporting.
     * @param <U>
     *            The {@link OutputStream} to write the export to.
     * @param <V>
     *            The {@link IProgressMonitor} used to report the progress.
     */
    private interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    /**
     *
     * @param exporter
     *            The {@link IExporter} to use for exporting.
     * @param executable
     *            The consumer implementation that is used to execute the export
     *            and produce the output to an {@link OutputStream}
     */
    private <T extends IExporter> void exportSingle(final T exporter, final IProgressMonitor monitor, final TriConsumer<T, OutputStream, IProgressMonitor> executable) {

        Runnable exportRunnable = () -> {
            final OutputStream outputStream = getOutputStream(exporter);
            if (outputStream != null) {
                try {
                    executable.accept(exporter, outputStream, monitor);

                    NatExporter.this.exportSucceeded = true;
                } catch (Exception e1) {
                    NatExporter.this.exportSucceeded = false;
                    handleExportException(e1);
                } finally {
                    try {
                        outputStream.close();
                    } catch (IOException e2) {
                        LOG.error("Failed to close the output stream", e2); //$NON-NLS-1$
                    }
                }

                openExport(exporter);
            }
        };

        if (this.shell != null && monitor == null) {
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

        Runnable exportRunnable = () -> {
            final OutputStream outputStream = getOutputStream(exporter);
            if (outputStream != null) {
                try {
                    exporter.exportBegin(outputStream);

                    // ensure that the exporter also respects the
                    // exportOnSameSheet parameter
                    exporter.setExportOnSameSheet(exportOnSameSheet);

                    if (exportOnSameSheet) {
                        exporter.exportLayerBegin(outputStream, sheetName);
                    }

                    if (this.useProgressDialog && this.shell != null) {
                        ProgressMonitorDialog dialog = getProgressMonitorDialog();
                        try {
                            dialog.run(true, true, monitor -> {
                                for (String name : natTablesMap.keySet()) {
                                    NatTable natTable = natTablesMap.get(name);
                                    exportLayer(exporter, outputStream, monitor, name, natTable, natTable.getConfigRegistry(), !exportOnSameSheet);
                                }
                            });
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        for (String name : natTablesMap.keySet()) {
                            NatTable natTable = natTablesMap.get(name);
                            exportLayer(exporter, outputStream, null, name, natTable, natTable.getConfigRegistry(), !exportOnSameSheet);
                        }
                    }

                    if (exportOnSameSheet) {
                        exporter.exportLayerEnd(outputStream, sheetName);
                    }

                    exporter.exportEnd(outputStream);

                    NatExporter.this.exportSucceeded = true;
                } catch (Exception e1) {
                    NatExporter.this.exportSucceeded = false;
                    handleExportException(e1);
                } finally {
                    try {
                        outputStream.close();
                    } catch (IOException e2) {
                        LOG.error("Failed to close the output stream", e2); //$NON-NLS-1$
                    }
                }
            }

            openExport(exporter);
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

        exportLayer(
                exporter,
                outputStream,
                null,
                layerName,
                layer,
                configRegistry,
                initExportLayer);
    }

    /**
     * /** Exports the given layer to the outputStream using the provided
     * exporter. The {@link ILayerExporter#exportBegin(OutputStream)} method
     * should be called before this method is invoked, and
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
     * @param monitor
     *            The {@link IProgressMonitor} used to report the export process
     *            to the user. Can be <code>null</code>.
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
     *
     * @since 2.3
     */
    protected void exportLayer(
            final ILayerExporter exporter,
            final OutputStream outputStream,
            final IProgressMonitor monitor,
            final String layerName,
            final ILayer layer,
            final IConfigRegistry configRegistry,
            final boolean initExportLayer) {

        exportLayer(
                new ITableExporter() {
                    @Override
                    public void exportTable(
                            Shell shell,
                            ProgressBar progressBar,
                            OutputStream outputStream,
                            ILayer layer,
                            IConfigRegistry configRegistry) throws IOException {

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
                                                cell.getConfigLabels());
                                        Object exportDisplayValue = exportFormatter.formatForExport(cell, configRegistry);

                                        exporter.exportCell(outputStream, exportDisplayValue, cell, configRegistry);

                                        if (monitor != null && monitor.isCanceled()) {
                                            break;
                                        }
                                    }
                                }

                                exporter.exportRowEnd(outputStream, rowPosition);

                                if (monitor != null) {
                                    monitor.worked(1);

                                    if (monitor.isCanceled()) {
                                        break;
                                    }
                                }
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
                },
                outputStream,
                monitor,
                layer,
                configRegistry);
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

        exportLayer(
                exporter,
                outputStream,
                null,
                layer,
                configRegistry);
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
     * @param monitor
     *            The {@link IProgressMonitor} used to report the export process
     *            to the user. Can be <code>null</code>.
     * @param layer
     *            The {@link ILayer} that should be exported.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve the export
     *            configurations.
     *
     * @since 2.3
     */
    protected void exportLayer(
            final ITableExporter exporter,
            final OutputStream outputStream,
            final IProgressMonitor monitor,
            final ILayer layer,
            final IConfigRegistry configRegistry) {

        IClientAreaProvider originalClientAreaProvider = layer.getClientAreaProvider();

        ProgressBar progressBar = null;
        try {
            if (this.shell != null && monitor == null) {
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

            if (monitor != null) {
                monitor.beginTask(" ", layer.getRowCount() + 1); //$NON-NLS-1$
                monitor.subTask(getPrepareSubTaskName());
            }

            if (this.shell != null) {
                this.shell.getDisplay().syncExec(() -> {
                    prepareExportProcess(layer, configRegistry);
                });
            } else {
                prepareExportProcess(layer, configRegistry);
            }

            if (monitor != null) {
                monitor.worked(1);

                if (monitor.isCanceled()) {
                    return;
                }
            }

            try {
                if (monitor != null) {
                    monitor.subTask(getExportSubTaskName());
                }
                exporter.exportTable(this.shell, progressBar, outputStream, layer, configRegistry);
                if (monitor != null) {
                    monitor.worked(1);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (monitor != null) {
                    monitor.done();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (this.shell != null) {
                this.shell.getDisplay().syncExec(() -> {
                    finalizeExportProcess(layer, originalClientAreaProvider);
                });
            } else {
                finalizeExportProcess(layer, originalClientAreaProvider);
            }

            if (progressBar != null) {
                Shell childShell = progressBar.getShell();
                progressBar.dispose();
                childShell.dispose();
            }
        }
    }

    /**
     * Prepare the table for the export process. This involves disabling the
     * viewport for example.
     *
     * @param layer
     *            The {@link ILayer} that should be exported.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve the export
     *            configurations.
     * @since 2.3
     */
    protected void prepareExportProcess(ILayer layer, IConfigRegistry configRegistry) {

        if (this.preRender && !PlatformHelper.isRAP()) {
            AutoResizeHelper.autoResize(layer, configRegistry);
        }

        // This needs to be done so that the layer can return all the cells not
        // just the ones visible in the viewport
        layer.doCommand(new TurnViewportOffCommand());
        setClientAreaToMaximum(layer);

        // if a SummaryRowLayer is in the layer stack, we need to ensure that
        // the values are calculated
        layer.doCommand(new CalculateSummaryRowValuesCommand());

        // if a FormulaDataProvider is involved, we need to ensure that the
        // formula evaluation is disabled so the formula itself is exported
        // instead of the calculated value
        layer.doCommand(new DisableFormulaEvaluationCommand());
    }

    /**
     * Reset the table state. This means to set back the state that was changed
     * in {@link #prepareExportProcess(ILayer, IConfigRegistry)}, e.g. enable
     * the viewport for example.
     *
     * @param layer
     *            The {@link ILayer} that should be exported.
     * @param originalClientAreaProvider
     *            The original {@link IClientAreaProvider}, which was replaced
     *            via {@link #setClientAreaToMaximum(ILayer)}.
     * @since 2.3
     */
    protected void finalizeExportProcess(ILayer layer, IClientAreaProvider originalClientAreaProvider) {
        // These must be fired at the end of the thread execution
        layer.setClientAreaProvider(originalClientAreaProvider);
        layer.doCommand(new TurnViewportOnCommand());

        layer.doCommand(new EnableFormulaEvaluationCommand());
    }

    /**
     *
     * @return The name that should be shown for the "prepare" subtask in the
     *         {@link ProgressMonitorDialog}.
     * @see NatExporter#useProgressDialog
     *
     * @since 2.3
     */
    protected String getPrepareSubTaskName() {
        return Messages.getString("NatExporter.subtask.prepare"); //$NON-NLS-1$
    }

    /**
     *
     * @return The name that should be shown for the "export data" subtask in
     *         the {@link ProgressMonitorDialog}.
     * @see NatExporter#useProgressDialog
     *
     * @since 2.3
     */
    protected String getExportSubTaskName() {
        return Messages.getString("NatExporter.subtask.export"); //$NON-NLS-1$
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

        layer.setClientAreaProvider(() -> maxClientArea);

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
        if (this.exportSucceeded) {

            if (this.successRunnable != null) {
                if (this.shell != null) {
                    this.shell.getDisplay().syncExec(() -> {
                        this.successRunnable.run();
                    });
                } else {
                    this.successRunnable.run();
                }
            }

            if (this.openResult
                    && exporter.getResult() != null
                    && exporter.getResult() instanceof File) {

                try {
                    Class<?> program = Class.forName("org.eclipse.swt.program.Program"); //$NON-NLS-1$
                    Method launch = program.getMethod("launch", String.class); //$NON-NLS-1$
                    launch.invoke(null, ((File) exporter.getResult()).getAbsolutePath());
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    LOG.info("Could not open the export because org.eclipse.swt.program.Program, you are probably running a RAP application."); //$NON-NLS-1$
                }
            }
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

    /**
     * @return <code>true</code> if the progress is reported via
     *         {@link ProgressMonitorDialog}, <code>false</code> if a custom
     *         shell with a {@link ProgressBar} will be shown.
     * @since 2.3
     */
    public boolean isUseProgressDialog() {
        return this.useProgressDialog;
    }

    /**
     * Configure how the export progress should be visualized. Will only have an
     * effect if the {@link NatExporter} was created with a {@link Shell}.
     *
     * @param useProgressDialog
     *            <code>true</code> if the progress should be reported via
     *            {@link ProgressMonitorDialog}, <code>false</code> if a custom
     *            shell with a {@link ProgressBar} should be shown.
     * @since 2.3
     */
    public void setUseProgressDialog(boolean useProgressDialog) {
        this.useProgressDialog = useProgressDialog;
    }

    /**
     * Configure a {@link Runnable} that should be executed after a successful
     * export operation. If a {@link #shell} is set, the {@link Runnable} is
     * executed using {@link Display#syncExec(Runnable)}.
     *
     * @param successRunnable
     *            The {@link Runnable} that should be executed after the export
     *            finished successfully. Useful in case {@link #openResult} is
     *            set to <code>false</code> so an alternative for reporting the
     *            export success can be configured.
     * @since 2.6
     */
    public void setSuccessRunnable(Runnable successRunnable) {
        this.successRunnable = successRunnable;
    }
}

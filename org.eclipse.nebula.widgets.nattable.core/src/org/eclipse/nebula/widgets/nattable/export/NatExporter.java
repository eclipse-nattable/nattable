/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
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

public class NatExporter {

    private static final Log log = LogFactory.getLog(NatExporter.class);

    private final Shell shell;
    private boolean openResult = true;
    private boolean exportSucceeded = true;

    /**
     * @since 1.5
     */
    protected boolean preRender = true;

    public NatExporter(Shell shell) {
        this.shell = shell;
    }

    /**
     * Exports a single ILayer using the ILayerExporter registered in the
     * ConfigRegistry.
     *
     * @param layer
     *            The ILayer to export, usually a NatTable instance.
     * @param configRegistry
     *            The ConfigRegistry of the NatTable instance to export, that
     *            contains the necessary export configurations.
     */
    public void exportSingleLayer(
            final ILayer layer,
            final IConfigRegistry configRegistry) {

        final ILayerExporter exporter = configRegistry.getConfigAttribute(
                ExportConfigAttributes.EXPORTER,
                DisplayMode.NORMAL);

        final OutputStream outputStream = getOutputStream(exporter);
        if (outputStream == null) {
            return;
        }

        Runnable exportRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    exporter.exportBegin(outputStream);

                    exportLayer(exporter, outputStream, "", layer, configRegistry); //$NON-NLS-1$

                    exporter.exportEnd(outputStream);

                    NatExporter.this.exportSucceeded = true;
                } catch (Exception e) {
                    NatExporter.this.exportSucceeded = false;
                    handleExportException(e);
                } finally {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        log.error("Failed to close the output stream", e); //$NON-NLS-1$
                    }
                }

                openExport(exporter);
            }
        };

        if (this.shell != null) {
            // Run with the SWT display so that the progress bar can paint
            this.shell.getDisplay().asyncExec(exportRunnable);
        } else {
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

        final OutputStream outputStream = getOutputStream(exporter);
        if (outputStream == null) {
            return;
        }

        Runnable exportRunnable = new Runnable() {
            @Override
            public void run() {
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
                        log.error("Failed to close the output stream", e); //$NON-NLS-1$
                    }
                }

                openExport(exporter);
            }
        };

        if (this.shell != null) {
            // Run with the SWT display so that the progress bar can paint
            this.shell.getDisplay().asyncExec(exportRunnable);
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

            int startRow = 0;
            int endRow = layer.getRowCount() - 1;

            progressBar = new ProgressBar(childShell, SWT.SMOOTH);
            progressBar.setMinimum(startRow);
            progressBar.setMaximum(endRow);
            progressBar.setBounds(0, 0, 400, 25);
            progressBar.setFocus();

            childShell.pack();
            childShell.open();
        }

        try {
            if (initExportLayer) {
                exporter.exportLayerBegin(outputStream, layerName);
            }

            for (int rowPosition = 0; rowPosition < layer.getRowCount(); rowPosition++) {
                exporter.exportRowBegin(outputStream, rowPosition);
                if (progressBar != null) {
                    progressBar.setSelection(rowPosition);
                }

                for (int columnPosition = 0; columnPosition < layer.getColumnCount(); columnPosition++) {
                    ILayerCell cell = layer.getCellByPosition(columnPosition, rowPosition);

                    IExportFormatter exportFormatter = configRegistry.getConfigAttribute(
                            ExportConfigAttributes.EXPORT_FORMATTER,
                            cell.getDisplayMode(),
                            cell.getConfigLabels().getLabels());
                    Object exportDisplayValue = exportFormatter.formatForExport(cell, configRegistry);

                    exporter.exportCell(outputStream, exportDisplayValue, cell, configRegistry);
                }

                exporter.exportRowEnd(outputStream, rowPosition);
            }

            if (initExportLayer) {
                exporter.exportLayerEnd(outputStream, layerName);
            }
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

    private void setClientAreaToMaximum(ILayer layer) {
        final Rectangle maxClientArea = new Rectangle(0, 0, layer.getWidth(), layer.getHeight());

        layer.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return maxClientArea;
            }
        });

        layer.doCommand(new PrintEntireGridCommand());
    }

    private void openExport(ILayerExporter exporter) {
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
    protected OutputStream getOutputStream(ILayerExporter exporter) {
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
        log.error("Failed to export.", e); //$NON-NLS-1$

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

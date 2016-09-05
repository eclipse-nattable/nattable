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
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.summaryrow.command.CalculateSummaryRowValuesCommand;
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

        final OutputStream outputStream = exporter.getOutputStream(this.shell);
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
                } catch (IOException e) {
                    throw new RuntimeException("Failed to export.", e); //$NON-NLS-1$
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

        final OutputStream outputStream = exporter.getOutputStream(this.shell);
        if (outputStream == null) {
            return;
        }

        Runnable exportRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    exporter.exportBegin(outputStream);

                    for (String name : natTablesMap.keySet()) {
                        NatTable natTable = natTablesMap.get(name);
                        exportLayer(exporter, outputStream, name, natTable,
                                natTable.getConfigRegistry());
                    }

                    exporter.exportEnd(outputStream);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to export.", e); //$NON-NLS-1$
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
     * The exporter.exportBegin() method should be called before this method is
     * invoked, and exporter.exportEnd() should be called after this method
     * returns. If multiple layers are being exported as part of a single
     * logical export operation, then exporter.exportBegin() will be called once
     * at the very beginning, followed by n calls to this exportLayer() method,
     * and finally followed by exporter.exportEnd().
     *
     * @param exporter
     * @param outputStream
     * @param layerName
     * @param layer
     * @param configRegistry
     */
    protected void exportLayer(
            final ILayerExporter exporter,
            final OutputStream outputStream,
            final String layerName,
            final ILayer layer,
            final IConfigRegistry configRegistry) {

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
            exporter.exportLayerBegin(outputStream, layerName);

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

            exporter.exportLayerEnd(outputStream, layerName);
        } catch (Exception e) {
            log.error("Export failed", e); //$NON-NLS-1$
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
        if (this.openResult && exporter.getResult() != null
                && exporter.getResult() instanceof File) {
            Program.launch(((File) exporter.getResult()).getAbsolutePath());
        }
    }

    /**
     * Sets the behavior after finishing the export.
     *
     * The default ist opening the created export file with the associated
     * application. You can prevent the opening by setting openResult to false.
     *
     * @param openResult
     *            set to <code>true</code> to open the created export file,
     *            <code>false</false> otherwise
     *
     * @since 1.5
     */
    public void setOpenResult(boolean openResult) {
        this.openResult = openResult;
    }

}
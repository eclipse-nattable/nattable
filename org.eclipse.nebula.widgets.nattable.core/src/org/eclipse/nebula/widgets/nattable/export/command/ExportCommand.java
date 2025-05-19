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
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.export.command;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.export.ILayerExporter;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

/**
 * Command to trigger export functionality.
 *
 * @see ExportCommandHandler
 */
public class ExportCommand extends AbstractContextFreeCommand {

    private final IConfigRegistry configRegistry;
    private final Shell shell;
    private final boolean executeSynchronously;
    private final boolean useProgressDialog;
    private final ILayerExporter exporter;
    private final boolean openResult;
    private final Runnable successRunnable;

    /**
     * Creates a new {@link ExportCommand}.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} that contains the necessary export
     *            configurations.
     * @param shell
     *            The {@link Shell} that should be used to open sub-dialogs and
     *            perform export operations in a background thread. Can be
     *            <code>null</code> which definitely leads to synchronous
     *            execution but could cause errors in case sub-dialogs should be
     *            opened before exporting.
     */
    public ExportCommand(IConfigRegistry configRegistry, Shell shell) {
        this(configRegistry, shell, false);
    }

    /**
     * Creates a new {@link ExportCommand}.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} that contains the necessary export
     *            configurations.
     * @param shell
     *            The {@link Shell} that should be used to open sub-dialogs and
     *            perform export operations in a background thread. Can be
     *            <code>null</code> which definitely leads to synchronous
     *            execution but could cause errors in case sub-dialogs should be
     *            opened before exporting.
     * @param executeSynchronously
     *            Configure if the export should be performed synchronously even
     *            if a {@link Shell} is set.
     *
     * @since 1.6
     */
    public ExportCommand(IConfigRegistry configRegistry, Shell shell, boolean executeSynchronously) {
        this(configRegistry, shell, executeSynchronously, false);
    }

    /**
     * Creates a new {@link ExportCommand}.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} that contains the necessary export
     *            configurations.
     * @param shell
     *            The {@link Shell} that should be used to open sub-dialogs and
     *            perform export operations in a background thread. Can be
     *            <code>null</code> which definitely leads to synchronous
     *            execution but could cause errors in case sub-dialogs should be
     *            opened before exporting.
     * @param executeSynchronously
     *            Configure if the export should be performed synchronously even
     *            if a {@link Shell} is set.
     * @param useProgressDialog
     *            Configure whether the progress should be reported via
     *            {@link ProgressMonitorDialog}. If set to <code>false</code> a
     *            custom shell with a {@link ProgressBar} will be shown if the
     *            shell parameter is not <code>null</code>.
     * @since 2.3
     */
    public ExportCommand(IConfigRegistry configRegistry, Shell shell, boolean executeSynchronously, boolean useProgressDialog) {
        this(configRegistry, shell, executeSynchronously, useProgressDialog, null);
    }

    /**
     * Creates a new {@link ExportCommand}.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} that contains the necessary export
     *            configurations.
     * @param shell
     *            The {@link Shell} that should be used to open sub-dialogs and
     *            perform export operations in a background thread. Can be
     *            <code>null</code> which definitely leads to synchronous
     *            execution but could cause errors in case sub-dialogs should be
     *            opened before exporting.
     * @param executeSynchronously
     *            Configure if the export should be performed synchronously even
     *            if a {@link Shell} is set.
     * @param useProgressDialog
     *            Configure whether the progress should be reported via
     *            {@link ProgressMonitorDialog}. If set to <code>false</code> a
     *            custom shell with a {@link ProgressBar} will be shown if the
     *            shell parameter is not <code>null</code>.
     * @param exporter
     *            The {@link ILayerExporter} that should be used. Can be
     *            <code>null</code>, which causes the usage of the exporter
     *            registered in the {@link IConfigRegistry}.
     * @since 2.3
     */
    public ExportCommand(IConfigRegistry configRegistry, Shell shell, boolean executeSynchronously, boolean useProgressDialog, ILayerExporter exporter) {
        this(configRegistry, shell, executeSynchronously, useProgressDialog, exporter, true, null);
    }

    /**
     * Creates a new {@link ExportCommand}.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} that contains the necessary export
     *            configurations.
     * @param shell
     *            The {@link Shell} that should be used to open sub-dialogs and
     *            perform export operations in a background thread. Can be
     *            <code>null</code> which definitely leads to synchronous
     *            execution but could cause errors in case sub-dialogs should be
     *            opened before exporting.
     * @param executeSynchronously
     *            Configure if the export should be performed synchronously even
     *            if a {@link Shell} is set.
     * @param useProgressDialog
     *            Configure whether the progress should be reported via
     *            {@link ProgressMonitorDialog}. If set to <code>false</code> a
     *            custom shell with a {@link ProgressBar} will be shown if the
     *            shell parameter is not <code>null</code>.
     * @param exporter
     *            The {@link ILayerExporter} that should be used. Can be
     *            <code>null</code>, which causes the usage of the exporter
     *            registered in the {@link IConfigRegistry}.
     * @param openResult
     *            Configure if the created export result should be opened after
     *            the export is finished.
     * @param successRunnable
     *            The {@link Runnable} that should be executed after the export
     *            finished successfully. Useful in case {@link #openResult} is
     *            set to <code>false</code> so an alternative for reporting the
     *            export success can be configured.
     * @since 2.6
     */
    public ExportCommand(
            IConfigRegistry configRegistry,
            Shell shell,
            boolean executeSynchronously,
            boolean useProgressDialog,
            ILayerExporter exporter,
            boolean openResult,
            Runnable successRunnable) {

        this.configRegistry = configRegistry;
        this.shell = shell;
        this.executeSynchronously = executeSynchronously;
        this.useProgressDialog = useProgressDialog;
        this.exporter = exporter;
        this.openResult = openResult;
        this.successRunnable = successRunnable;
    }

    /**
     *
     * @return The {@link IConfigRegistry} that contains the necessary export
     *         configurations.
     */
    public IConfigRegistry getConfigRegistry() {
        return this.configRegistry;
    }

    /**
     *
     * @return The {@link Shell} that should be used to open sub-dialogs and
     *         perform export operations in a background thread. Or
     *         <code>null</code> if the export should be performed synchronously
     *         and no sub-dialogs are needed in the process (e.g. to select the
     *         export destination).
     */
    public Shell getShell() {
        return this.shell;
    }

    /**
     *
     * @return <code>true</code> if the export should be performed synchronously
     *         even if a {@link Shell} is set.
     *
     * @since 1.6
     */
    public boolean isExecuteSynchronously() {
        return this.executeSynchronously;
    }

    /**
     *
     * @return <code>true</code> if the progress should be reported via
     *         {@link ProgressMonitorDialog}.
     *
     * @since 2.3
     */
    public boolean isUseProgressDialog() {
        return this.useProgressDialog;
    }

    /**
     *
     * @return The {@link ILayerExporter} that should be used. Can be
     *         <code>null</code>, which causes the usage of the exporter
     *         registered in the {@link IConfigRegistry}.
     *
     * @since 2.3
     */
    public ILayerExporter getExporter() {
        return this.exporter;
    }

    /**
     *
     * @return <code>true</code> if the created export file should be opened
     *         after the export finished successfully, <code>false</code> if not
     * @since 2.6
     */
    public boolean isOpenResult() {
        return this.openResult;
    }

    /**
     *
     * @return The {@link Runnable} that should be executed after the export
     *         finished successfully. Useful in case {@link #openResult} is set
     *         to <code>false</code> so an alternative for reporting the export
     *         success can be configured.
     * @since 2.6
     */
    public Runnable getSuccessRunnable() {
        return this.successRunnable;
    }
}

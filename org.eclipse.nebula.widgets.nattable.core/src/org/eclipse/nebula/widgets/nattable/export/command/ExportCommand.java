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
package org.eclipse.nebula.widgets.nattable.export.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.swt.widgets.Shell;

/**
 * Command to trigger export functionality.
 *
 * @see ExportCommandHandler
 */
public class ExportCommand extends AbstractContextFreeCommand {

    private IConfigRegistry configRegistry;
    private final Shell shell;
    private final boolean executeSynchronously;

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
        this.configRegistry = configRegistry;
        this.shell = shell;
        this.executeSynchronously = executeSynchronously;
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
}

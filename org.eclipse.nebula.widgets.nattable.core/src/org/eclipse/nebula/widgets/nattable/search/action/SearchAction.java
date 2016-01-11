/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.search.action;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.search.gui.SearchDialog;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;

/**
 * An action for opening a Find dialog on a NatTable. Supports both modal and
 * non-modal (i.e., sharable) Find dialog behavior.
 */
public class SearchAction implements IKeyAction {

    private static class Context {
        NatTable natTable;
        IDialogSettings dialogSettings;
        private boolean modal;

        Context(NatTable natTable, IDialogSettings dialogSettings, boolean modal) {
            this.natTable = natTable;
            this.dialogSettings = dialogSettings;
            this.modal = modal;
        }
    }

    private static Context activeContext;
    private static SearchDialog dialog;

    private Context context;

    /**
     * Constructs an action with a modal Find dialog.
     */
    public SearchAction() {
        this(null, null, true);
    }

    /**
     * Constructs an action with a non-modal (i.e., sharable) Find dialog.
     *
     * @param natTable
     *            The NatTable instance to perform the search action on.
     * @param dialogSettings
     *            The dialog settings that should be used to create the search
     *            dialog.
     */
    public SearchAction(NatTable natTable, IDialogSettings dialogSettings) {
        this(natTable, dialogSettings, false);
        if (natTable == null) {
            throw new IllegalArgumentException();
        }
    }

    private SearchAction(NatTable natTable, IDialogSettings dialogSettings,
            boolean modal) {
        this.context = new Context(natTable, dialogSettings, modal);
        if (natTable != null) {
            natTable.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    setActiveContext();
                }

                @Override
                public void focusLost(FocusEvent e) {}
            });
        }
    }

    protected void setActiveContext() {
        if (dialog != null && !isEquivalentToActiveContext()) {
            dialog.close();
            dialog = null;
        }
        activeContext = this.context;
        if (dialog != null) {
            dialog.setInput(this.context.natTable, this.context.dialogSettings);
        }
    }

    /**
     * Checks if this context is considered equivalent to the active context.
     * For modal equivalence, the contexts must be exactly the same. For
     * non-modal equivalence, the Shell and IDialogSettings must be equivalent.
     *
     * @return whether this context is equivalent to the active context
     */
    private boolean isEquivalentToActiveContext() {
        if (this.context.modal) {
            return this.context.equals(activeContext);
        }
        if (activeContext.modal) {
            return false;
        }
        return !activeContext.natTable.isDisposed()
                && this.context.natTable.getShell().equals(
                        activeContext.natTable.getShell())
                && this.context.dialogSettings.equals(activeContext.dialogSettings);
    }

    @Override
    public void run(final NatTable natTable, KeyEvent event) {
        this.context.natTable = natTable;
        setActiveContext();
        if (dialog == null) {
            dialog = new SearchDialog(this.context.natTable.getShell(),
                    new CellValueAsStringComparator<String>(),
                    this.context.modal ? SWT.NONE : SWT.APPLICATION_MODAL);
            dialog.setInput(this.context.natTable, this.context.dialogSettings);
        }
        dialog.open();
    }

}

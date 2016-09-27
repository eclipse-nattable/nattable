/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;

/**
 * An action for opening a Find dialog on a NatTable. Supports both modal and
 * non-modal (i.e., sharable) Find dialog behavior.
 */
public class SearchAction implements IKeyAction {

    private static SearchDialog dialog;

    private NatTable natTable;
    private IDialogSettings dialogSettings;
    private boolean modal;

    private DisposeListener listener = new DisposeListener() {

        @Override
        public void widgetDisposed(DisposeEvent e) {
            if (dialog != null) {
                if (dialog.isModal()) {
                    dialog.close();
                    dialog = null;
                } else {
                    dialog.setInput(null, null);
                }
            }
        }
    };

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

    private SearchAction(NatTable natTable, IDialogSettings dialogSettings, boolean modal) {
        this.natTable = natTable;
        this.dialogSettings = dialogSettings;
        this.modal = modal;
        if (natTable != null) {
            natTable.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    setActiveContext();
                }
            });
            natTable.addDisposeListener(this.listener);
        }
    }

    protected void setActiveContext() {
        if (dialog != null
                && dialog.getNatTable() != null
                && !isEquivalentToActiveContext()) {
            dialog.close();
            dialog = null;
        }
        if (dialog != null) {
            dialog.setInput(this.natTable, this.dialogSettings);
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
        if (this.modal) {
            return (this.natTable == dialog.getNatTable()
                    && this.dialogSettings == dialog.getOriginalDialogSettings()
                    && this.modal == dialog.isModal());
        }
        if (dialog.isModal()) {
            return false;
        }
        return !this.natTable.isDisposed()
                && (dialog.getNatTable() != null && !dialog.getNatTable().isDisposed())
                && this.natTable.getShell().equals(dialog.getNatTable().getShell())
                && ((this.dialogSettings == null && dialog.getOriginalDialogSettings() == null)
                        || this.dialogSettings.equals(dialog.getOriginalDialogSettings()));
    }

    @Override
    public void run(final NatTable natTable, KeyEvent event) {
        if (this.natTable != natTable) {
            if (this.natTable != null) {
                this.natTable.removeDisposeListener(this.listener);
            }
            this.natTable = natTable;
            this.natTable.addDisposeListener(this.listener);
        }
        setActiveContext();
        if (dialog == null) {
            dialog = new SearchDialog(this.natTable.getShell(),
                    new CellValueAsStringComparator<String>(),
                    this.modal ? SWT.NONE : SWT.APPLICATION_MODAL);
            dialog.setInput(this.natTable, this.dialogSettings);
        }
        dialog.open();
    }

}

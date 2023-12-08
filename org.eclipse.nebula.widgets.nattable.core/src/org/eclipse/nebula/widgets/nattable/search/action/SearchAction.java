/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.search.action;

import java.util.Comparator;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.search.gui.SearchDialog;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * An action for opening a Find dialog on a NatTable. Supports both modal and
 * non-modal (i.e., sharable) Find dialog behavior.
 */
public class SearchAction implements IKeyAction {

    private SearchDialog dialog;

    private NatTable natTable;
    private IDialogSettings dialogSettings;
    private boolean modal;
    private SearchDialogCreator creator;

    private DisposeListener listener = e -> {
        if (this.dialog != null) {
            if (this.dialog.isModal()) {
                this.dialog.close();
                this.dialog = null;
            } else {
                this.dialog.setInput(null, null);
            }
        }
    };

    /**
     * @since 2.3
     */
    public interface SearchDialogCreator {
        SearchDialog create(Shell shell, Comparator<?> comparator, int style);
    }

    /**
     * Constructs an action with a modal Find dialog.
     */
    public SearchAction() {
        this(null, null, true, (n, c, s) -> new SearchDialog(n, c, s));
    }

    /**
     * Constructs an action with a modal Find dialog that is created via the
     * given creator function
     *
     * @param creator
     *            The {@link SearchDialogCreator} used to create the find
     *            dialog.
     * @since 2.3
     */
    public SearchAction(SearchDialogCreator creator) {
        this(null, null, true, creator);
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
        this(natTable, dialogSettings, false, (n, c, s) -> new SearchDialog(n, c, s));
        if (natTable == null) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Constructs an action with a non-modal (i.e., sharable) Find dialog.
     *
     * @param natTable
     *            The NatTable instance to perform the search action on.
     * @param dialogSettings
     *            The dialog settings that should be used to create the search
     *            dialog.
     * @param creator
     *            The {@link SearchDialogCreator} used to create the find
     *            dialog.
     * @since 2.3
     */
    public SearchAction(NatTable natTable, IDialogSettings dialogSettings, SearchDialogCreator creator) {
        this(natTable, dialogSettings, false, (n, c, s) -> new SearchDialog(n, c, s));
        if (natTable == null) {
            throw new IllegalArgumentException();
        }
    }

    private SearchAction(NatTable natTable, IDialogSettings dialogSettings, boolean modal, SearchDialogCreator creator) {
        this.natTable = natTable;
        this.dialogSettings = dialogSettings;
        this.modal = modal;
        this.creator = creator;
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
        if (this.dialog != null
                && this.dialog.getNatTable() != null
                && !isEquivalentToActiveContext()) {
            this.dialog.close();
            this.dialog = null;
        }
        if (this.dialog != null) {
            this.dialog.setInput(this.natTable, this.dialogSettings);
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
            return (this.natTable == this.dialog.getNatTable()
                    && this.dialogSettings == this.dialog.getOriginalDialogSettings()
                    && this.modal == this.dialog.isModal());
        }
        if (this.dialog.isModal()) {
            return false;
        }
        return !this.natTable.isDisposed()
                && (this.dialog.getNatTable() != null && !this.dialog.getNatTable().isDisposed())
                && this.natTable.getShell().equals(this.dialog.getNatTable().getShell())
                && ((this.dialogSettings == null && this.dialog.getOriginalDialogSettings() == null)
                        || (this.dialogSettings != null && this.dialogSettings.equals(this.dialog.getOriginalDialogSettings())));
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
        if (this.dialog == null) {
            this.dialog = this.creator.create(this.natTable.getShell(),
                    new CellValueAsStringComparator<>(),
                    this.modal ? SWT.NONE : SWT.APPLICATION_MODAL);
            this.dialog.setInput(this.natTable, this.dialogSettings);
        }
        this.dialog.open();
    }

}

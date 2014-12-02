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
package org.eclipse.nebula.widgets.nattable.selection;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;

public class RowPostSelectionProvider<T> extends RowSelectionProvider<T>
        implements IPostSelectionProvider {
    private ListenerList postSelectionChangedListeners = new ListenerList();
    private ISelection previousSelection;

    public RowPostSelectionProvider(NatTable natTable,
            SelectionLayer selectionLayer, IRowDataProvider<T> rowDataProvider) {
        super(selectionLayer, rowDataProvider);
        hookControl(natTable);
    }

    public RowPostSelectionProvider(NatTable natTable,
            SelectionLayer selectionLayer, IRowDataProvider<T> rowDataProvider,
            boolean fullySelectedRowsOnly) {
        super(selectionLayer, rowDataProvider, fullySelectedRowsOnly);
        hookControl(natTable);
    }

    protected void hookControl(Control control) {
        OpenStrategy handler = new OpenStrategy(control);
        handler.addPostSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handlePostSelect(e);
            }
        });
    }

    @Override
    public void addPostSelectionChangedListener(
            ISelectionChangedListener listener) {
        this.postSelectionChangedListeners.add(listener);
    }

    @Override
    public void removePostSelectionChangedListener(
            ISelectionChangedListener listener) {
        this.postSelectionChangedListeners.remove(listener);
    }

    protected void handlePostSelect(SelectionEvent e) {
        ISelection selection = getSelection();
        if (!selection.equals(this.previousSelection)) { // OpenStrategy doesn't
                                                    // throttle left/right
                                                    // cursor key presses, so
                                                    // only fire event when row
                                                    // changes
            SelectionChangedEvent event = new SelectionChangedEvent(this,
                    selection);
            firePostSelectionChanged(event);
            this.previousSelection = selection;
        }
    }

    protected void firePostSelectionChanged(final SelectionChangedEvent event) {
        Object[] listeners = this.postSelectionChangedListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                @Override
                public void run() {
                    l.selectionChanged(event);
                }
            });
        }
    }
}

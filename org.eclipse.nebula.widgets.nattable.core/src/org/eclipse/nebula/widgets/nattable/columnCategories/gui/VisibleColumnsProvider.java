/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.columnCategories.gui;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnEntry;

/**
 * Provides visible columns as {@link ColumnEntry} objects.
 */
public class VisibleColumnsProvider extends LabelProvider implements IStructuredContentProvider {

    List<ColumnEntry> visibleColumnsEntries;

    public VisibleColumnsProvider(List<ColumnEntry> visibleColumnsEntries) {
        this.visibleColumnsEntries = visibleColumnsEntries;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return this.visibleColumnsEntries.toArray();
    }

    @Override
    public String getText(Object element) {
        return ((ColumnEntry) element).getLabel();
    }

    @Override
    public void dispose() {
        // No op.
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // No op.
    }

}

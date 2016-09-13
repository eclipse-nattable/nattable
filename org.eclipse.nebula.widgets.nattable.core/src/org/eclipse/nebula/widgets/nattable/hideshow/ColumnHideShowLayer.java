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
package org.eclipse.nebula.widgets.nattable.hideshow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralChangeEventHelper;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;

public class ColumnHideShowLayer extends AbstractColumnHideShowLayer {

    public static final String PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES = ".hiddenColumnIndexes"; //$NON-NLS-1$

    private final Set<Integer> hiddenColumnIndexes;

    public ColumnHideShowLayer(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer);
        this.hiddenColumnIndexes = new TreeSet<Integer>();

        registerCommandHandler(new MultiColumnHideCommandHandler(this));
        registerCommandHandler(new ColumnHideCommandHandler(this));
        registerCommandHandler(new ShowAllColumnsCommandHandler(this));
        registerCommandHandler(new MultiColumnShowCommandHandler(this));
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.isHorizontalStructureChanged()) {
                Collection<StructuralDiff> columnDiffs = structuralChangeEvent.getColumnDiffs();

                if (columnDiffs != null && !columnDiffs.isEmpty()
                        && !StructuralChangeEventHelper.isReorder(columnDiffs)) {
                    StructuralChangeEventHelper.handleColumnDelete(columnDiffs,
                            this.underlyingLayer, this.hiddenColumnIndexes, false);
                    StructuralChangeEventHelper.handleColumnInsert(columnDiffs,
                            this.underlyingLayer, this.hiddenColumnIndexes, false);
                }
            }
        }
        super.handleLayerEvent(event);
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        if (this.hiddenColumnIndexes.size() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            for (Integer index : this.hiddenColumnIndexes) {
                strBuilder.append(index);
                strBuilder.append(IPersistable.VALUE_SEPARATOR);
            }
            properties.setProperty(
                    prefix + PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES,
                    strBuilder.toString());
        } else {
            properties.remove(prefix + PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES);
        }

        super.saveState(prefix, properties);
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        // Bug 396925: always clear the state of the hidden columns, whether
        // there is a state saved or not
        this.hiddenColumnIndexes.clear();
        String property = properties.getProperty(prefix + PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES);
        if (property != null) {
            StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
            while (tok.hasMoreTokens()) {
                String index = tok.nextToken();
                this.hiddenColumnIndexes.add(Integer.valueOf(index));
            }
        }

        super.loadState(prefix, properties);
    }

    // Hide/show

    @Override
    public boolean isColumnIndexHidden(int columnIndex) {
        return this.hiddenColumnIndexes.contains(Integer.valueOf(columnIndex));
    }

    @Override
    public Collection<Integer> getHiddenColumnIndexes() {
        return this.hiddenColumnIndexes;
    }

    public void hideColumnPositions(Collection<Integer> columnPositions) {
        Set<Integer> columnIndexes = new HashSet<Integer>();
        for (Integer columnPosition : columnPositions) {
            columnIndexes.add(getColumnIndexByPosition(columnPosition));
        }
        this.hiddenColumnIndexes.addAll(columnIndexes);
        invalidateCache();
        fireLayerEvent(new HideColumnPositionsEvent(this, columnPositions));
    }

    public void showColumnIndexes(Collection<Integer> columnIndexes) {
        this.hiddenColumnIndexes.removeAll(columnIndexes);
        invalidateCache();
        fireLayerEvent(new ShowColumnPositionsEvent(this, getColumnPositionsByIndexes(columnIndexes)));
    }

    public void showAllColumns() {
        Collection<Integer> hiddenColumns = new ArrayList<Integer>(this.hiddenColumnIndexes);
        this.hiddenColumnIndexes.clear();
        invalidateCache();
        fireLayerEvent(new ShowColumnPositionsEvent(this, hiddenColumns));
    }

}

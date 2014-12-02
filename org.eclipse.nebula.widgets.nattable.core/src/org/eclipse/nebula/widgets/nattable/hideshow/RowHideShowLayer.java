/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
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

import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllRowsCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralChangeEventHelper;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;

public class RowHideShowLayer extends AbstractRowHideShowLayer implements
        IRowHideShowCommandLayer {

    public static final String PERSISTENCE_KEY_HIDDEN_ROW_INDEXES = ".hiddenRowIndexes"; //$NON-NLS-1$

    private final Set<Integer> hiddenRowIndexes;

    public RowHideShowLayer(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer);
        this.hiddenRowIndexes = new TreeSet<Integer>();

        registerCommandHandler(new MultiRowHideCommandHandler(this));
        registerCommandHandler(new RowHideCommandHandler(this));
        registerCommandHandler(new ShowAllRowsCommandHandler(this));
        registerCommandHandler(new MultiRowShowCommandHandler(this));
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.isVerticalStructureChanged()) {
                Collection<StructuralDiff> rowDiffs = structuralChangeEvent
                        .getRowDiffs();
                if (rowDiffs != null && !rowDiffs.isEmpty()
                        && !StructuralChangeEventHelper.isReorder(rowDiffs)) {
                    StructuralChangeEventHelper.handleRowDelete(rowDiffs,
                            this.underlyingLayer, this.hiddenRowIndexes, false);
                    StructuralChangeEventHelper.handleRowInsert(rowDiffs,
                            this.underlyingLayer, this.hiddenRowIndexes, false);
                }
            }
        }
        super.handleLayerEvent(event);
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        if (this.hiddenRowIndexes.size() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            for (Integer index : this.hiddenRowIndexes) {
                strBuilder.append(index);
                strBuilder.append(IPersistable.VALUE_SEPARATOR);
            }
            properties.setProperty(prefix + PERSISTENCE_KEY_HIDDEN_ROW_INDEXES,
                    strBuilder.toString());
        }

        super.saveState(prefix, properties);
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        this.hiddenRowIndexes.clear();
        String property = properties.getProperty(prefix
                + PERSISTENCE_KEY_HIDDEN_ROW_INDEXES);
        if (property != null) {
            StringTokenizer tok = new StringTokenizer(property,
                    IPersistable.VALUE_SEPARATOR);
            while (tok.hasMoreTokens()) {
                String index = tok.nextToken();
                this.hiddenRowIndexes.add(Integer.valueOf(index));
            }
        }

        super.loadState(prefix, properties);
    }

    // Hide/show

    @Override
    public boolean isRowIndexHidden(int rowIndex) {
        return this.hiddenRowIndexes.contains(Integer.valueOf(rowIndex));
    }

    @Override
    public Collection<Integer> getHiddenRowIndexes() {
        return this.hiddenRowIndexes;
    }

    @Override
    public void hideRowPositions(Collection<Integer> rowPositions) {
        Set<Integer> rowIndexes = new HashSet<Integer>();
        for (Integer rowPosition : rowPositions) {
            rowIndexes.add(getRowIndexByPosition(rowPosition));
        }
        this.hiddenRowIndexes.addAll(rowIndexes);
        invalidateCache();
        fireLayerEvent(new HideRowPositionsEvent(this, rowPositions));
    }

    @Override
    public void hideRowIndexes(Collection<Integer> rowIndexes) {
        Set<Integer> rowPositions = new HashSet<Integer>();
        for (Integer rowIndex : rowIndexes) {
            rowPositions.add(getRowPositionByIndex(rowIndex));
        }
        this.hiddenRowIndexes.addAll(rowIndexes);
        invalidateCache();
        fireLayerEvent(new HideRowPositionsEvent(this, rowPositions));
    }

    @Override
    public void showRowIndexes(Collection<Integer> rowIndexes) {
        this.hiddenRowIndexes.removeAll(rowIndexes);
        invalidateCache();
        fireLayerEvent(new ShowRowPositionsEvent(this,
                getRowPositionsByIndexes(rowIndexes)));
    }

    @Override
    public void showAllRows() {
        Collection<Integer> hiddenRows = new ArrayList<Integer>(
                this.hiddenRowIndexes);
        this.hiddenRowIndexes.clear();
        invalidateCache();
        fireLayerEvent(new ShowRowPositionsEvent(this, hiddenRows));
    }
}

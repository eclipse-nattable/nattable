/*******************************************************************************
 * Copyright (c) 2018, 2019 Dirk Fauth.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowPositionHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllRowsCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.indicator.HideIndicatorConstants;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.sort.command.SortColumnCommand;

/**
 * {@link ILayer} that supports hiding of rows based on the row id. This way the
 * hidden row is identified even on sorting and filtering. This is different to
 * the {@link RowHideShowLayer} that handles row hiding via row index.
 *
 * @since 1.6
 */
public class RowIdHideShowLayer<T> extends AbstractRowHideShowLayer implements IRowHideShowLayer {

    public static final String PERSISTENCE_KEY_HIDDEN_ROW_IDS = ".hiddenRowIDs"; //$NON-NLS-1$

    protected final IRowDataProvider<T> rowDataProvider;
    protected final IRowIdAccessor<T> rowIdAccessor;

    protected Map<Serializable, T> hiddenRows = new TreeMap<Serializable, T>();

    protected IDisplayConverter idConverter;

    /**
     *
     * @param underlyingLayer
     *            The underlying layer.
     * @param rowDataProvider
     *            The body data provider needed to get the row object by index
     *            to determine the row id.
     * @param rowIdAccessor
     *            The {@link IRowIdAccessor} needed to extract the row id of a
     *            row object.
     */
    public RowIdHideShowLayer(IUniqueIndexLayer underlyingLayer, IRowDataProvider<T> rowDataProvider, IRowIdAccessor<T> rowIdAccessor) {
        super(underlyingLayer);
        this.rowDataProvider = rowDataProvider;
        this.rowIdAccessor = rowIdAccessor;

        registerCommandHandler(new MultiRowHideCommandHandler(this));
        registerCommandHandler(new RowHideCommandHandler(this));
        registerCommandHandler(new ShowAllRowsCommandHandler(this));
        registerCommandHandler(new MultiRowShowCommandHandler(this));
        registerCommandHandler(new RowPositionHideCommandHandler(this));
        registerCommandHandler(new RowShowCommandHandler(this));
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        // in case we receive a SortColumnCommand we expect that the row
        // ordering changes. We therefore pro-actively invalidate the cache to
        // avoid flickering when the update is triggered with a 100ms delay by
        // the GlazedListsEventLayer.
        if (command instanceof SortColumnCommand) {
            invalidateCache();
        }
        return super.doCommand(command);
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        if (this.hiddenRows.size() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            for (Serializable id : this.hiddenRows.keySet()) {
                if (this.idConverter != null) {
                    strBuilder.append(this.idConverter.canonicalToDisplayValue(id));
                } else {
                    strBuilder.append(id.toString());
                }
                strBuilder.append(IPersistable.VALUE_SEPARATOR);
            }
            properties.setProperty(prefix + PERSISTENCE_KEY_HIDDEN_ROW_IDS, strBuilder.toString());
        }

        super.saveState(prefix, properties);
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        this.hiddenRows.clear();
        String property = properties.getProperty(prefix + PERSISTENCE_KEY_HIDDEN_ROW_IDS);
        if (property != null) {
            StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
            Set<Serializable> ids = new HashSet<Serializable>();
            while (tok.hasMoreTokens()) {
                String id = tok.nextToken();
                if (this.idConverter != null) {
                    ids.add((Serializable) this.idConverter.displayToCanonicalValue(id));
                } else {
                    ids.add(id);
                }
            }

            for (int row = 0; row < this.rowDataProvider.getRowCount(); row++) {
                T rowObject = this.rowDataProvider.getRowObject(row);
                Serializable rowId = this.rowIdAccessor.getRowId(rowObject);
                if (ids.contains(rowId)) {
                    ids.remove(rowId);
                    this.hiddenRows.put(rowId, rowObject);
                }
                if (ids.isEmpty()) {
                    // if no more row ids are hidden we do not need to iterate
                    // to the end
                    break;
                }
            }
        }

        super.loadState(prefix, properties);
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        LabelStack configLabels = super.getConfigLabelsByPosition(columnPosition, rowPosition);

        // we need to check the hidden state of an adjacent position via the
        // underlying layer as in the hide layer the position might be
        // hidden
        int underlyingPosition = localToUnderlyingRowPosition(rowPosition);
        int upRowIndex = this.underlyingLayer.getRowIndexByPosition(underlyingPosition - 1);
        if (isRowIndexHidden(upRowIndex)) {
            configLabels.addLabel(HideIndicatorConstants.ROW_TOP_HIDDEN);
        }

        int downRowIndex = this.underlyingLayer.getRowIndexByPosition(underlyingPosition + 1);
        if (isRowIndexHidden(downRowIndex)) {
            configLabels.addLabel(HideIndicatorConstants.ROW_BOTTOM_HIDDEN);
        }

        return configLabels;
    }

    @Override
    public int getRowCount() {
        // faster implementation than the super implementation because
        // getHiddenRowIndexes() is calculating the row indexes everytime
        return this.underlyingLayer.getRowCount() - this.hiddenRows.size();
    }

    // Hide/show

    @Override
    public boolean isRowIndexHidden(int rowIndex) {
        if (rowIndex >= 0) {
            T rowObject = getRowObjectByIndex(rowIndex);
            return this.hiddenRows.containsKey(this.rowIdAccessor.getRowId(rowObject));
        }
        return false;
    }

    @Override
    public Collection<Integer> getHiddenRowIndexes() {
        Set<Integer> result = new HashSet<Integer>();
        for (Map.Entry<Serializable, T> entry : this.hiddenRows.entrySet()) {
            result.add(getRowIndexById(entry.getKey()));
        }
        return result;
    }

    @Override
    public void hideRowPositions(int... rowPositions) {
        hideRowPositions(Arrays.stream(rowPositions).boxed().collect(Collectors.toList()));
    }

    @Override
    public void hideRowPositions(Collection<Integer> rowPositions) {
        Map<Serializable, T> toHide = new HashMap<Serializable, T>();
        for (Integer rowPosition : rowPositions) {
            T rowObject = getRowObjectByPosition(rowPosition);
            toHide.put(this.rowIdAccessor.getRowId(rowObject), rowObject);
        }
        this.hiddenRows.putAll(toHide);
        invalidateCache();
        fireLayerEvent(new HideRowPositionsEvent(this, rowPositions));
    }

    @Override
    public void hideRowIndexes(int... rowIndexes) {
        hideRowIndexes(Arrays.stream(rowIndexes).boxed().collect(Collectors.toList()));
    }

    @Override
    public void hideRowIndexes(Collection<Integer> rowIndexes) {
        Set<Integer> rowPositions = new HashSet<Integer>();
        for (Integer rowIndex : rowIndexes) {
            rowPositions.add(getRowPositionByIndex(rowIndex));
            T rowObject = getRowObjectByIndex(rowIndex);
            this.hiddenRows.put(this.rowIdAccessor.getRowId(rowObject), rowObject);
        }
        invalidateCache();
        fireLayerEvent(new HideRowPositionsEvent(this, rowPositions));
    }

    @Override
    public void showRowIndexes(int... rowIndexes) {
        showRowIndexes(Arrays.stream(rowIndexes).boxed().collect(Collectors.toList()));
    }

    @Override
    public void showRowIndexes(Collection<Integer> rowIndexes) {
        for (Integer rowIndex : rowIndexes) {
            T rowObject = getRowObjectByIndex(rowIndex);
            this.hiddenRows.remove(this.rowIdAccessor.getRowId(rowObject));
        }
        invalidateCache();
        fireLayerEvent(new ShowRowPositionsEvent(this, getRowPositionsByIndexes(rowIndexes)));
    }

    @Override
    public void showRowPosition(int rowPosition, boolean showToTop, boolean showAll) {
        Set<Integer> rowIndexes = new HashSet<Integer>();
        int underlyingPosition = localToUnderlyingRowPosition(rowPosition);
        if (showToTop) {
            int topRowIndex = this.underlyingLayer.getRowIndexByPosition(underlyingPosition - 1);
            if (showAll) {
                int move = 1;
                while (isRowIndexHidden(topRowIndex)) {
                    rowIndexes.add(topRowIndex);
                    move++;
                    topRowIndex = this.underlyingLayer.getRowIndexByPosition(underlyingPosition - move);
                }
            } else if (isRowIndexHidden(topRowIndex)) {
                rowIndexes.add(topRowIndex);
            }
        } else {
            int bottomRowIndex = this.underlyingLayer.getRowIndexByPosition(underlyingPosition + 1);
            if (showAll) {
                int move = 1;
                while (isRowIndexHidden(bottomRowIndex)) {
                    rowIndexes.add(bottomRowIndex);
                    move++;
                    bottomRowIndex = this.underlyingLayer.getRowIndexByPosition(underlyingPosition + move);
                }
            } else if (isRowIndexHidden(bottomRowIndex)) {
                rowIndexes.add(bottomRowIndex);
            }
        }

        if (!rowIndexes.isEmpty()) {
            showRowIndexes(rowIndexes);
        }
    }

    @Override
    public void showAllRows() {
        Collection<Integer> hiddenRows = new ArrayList<Integer>(getHiddenRowIndexes());
        this.hiddenRows.clear();
        invalidateCache();
        fireLayerEvent(new ShowRowPositionsEvent(this, hiddenRows));
    }

    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> result = super.getProvidedLabels();
        result.add(HideIndicatorConstants.ROW_TOP_HIDDEN);
        result.add(HideIndicatorConstants.ROW_BOTTOM_HIDDEN);
        return result;
    }

    private T getRowObjectByPosition(int rowPosition) {
        int rowIndex = getRowIndexByPosition(rowPosition);
        return getRowObjectByIndex(rowIndex);
    }

    private T getRowObjectByIndex(int rowIndex) {
        if (rowIndex >= 0) {
            try {
                T rowObject = this.rowDataProvider.getRowObject(rowIndex);
                return rowObject;
            } catch (Exception e) {
                // row index is invalid for the data provider
            }
        }

        return null;
    }

    private int getRowIndexById(Serializable rowId) {
        T rowObject = this.hiddenRows.get(rowId);
        int rowIndex = this.rowDataProvider.indexOfRowObject(rowObject);
        if (rowIndex == -1) {
            return -1;
        }
        return rowIndex;
    }

    /**
     *
     * @return The converter used for id conversion.
     */
    public IDisplayConverter getIdConverter() {
        return this.idConverter;
    }

    /**
     * Set the {@link IDisplayConverter} that is used for conversion of the row
     * id needed on {@link #loadState(String, Properties)} and
     * {@link #saveState(String, Properties)}.
     *
     * @param idConverter
     *            The converter that should be used for id conversion.
     */
    public void setIdConverter(IDisplayConverter idConverter) {
        this.idConverter = idConverter;
    }

}

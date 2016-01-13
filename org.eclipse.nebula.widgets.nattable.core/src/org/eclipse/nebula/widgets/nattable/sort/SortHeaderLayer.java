/*******************************************************************************
 * Copyright (c) 2012, 2013, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.sort;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.sort.command.SortCommandHandler;
import org.eclipse.nebula.widgets.nattable.sort.config.DefaultSortConfiguration;

/**
 * Enables sorting of the data. Uses an {@link ISortModel} to do/track the
 * sorting.
 *
 * @param <T>
 *            Type of the Beans in the backing data source.
 *
 * @see DefaultSortConfiguration
 * @see SortStatePersistor
 */
public class SortHeaderLayer<T> extends AbstractLayerTransform implements IPersistable {

    /** Handles the actual sorting of underlying data */
    private final ISortModel sortModel;

    public SortHeaderLayer(ILayer underlyingLayer, ISortModel sortModel) {
        this(underlyingLayer, sortModel, true);
    }

    public SortHeaderLayer(ILayer underlyingLayer, ISortModel sortModel, boolean useDefaultConfiguration) {
        super(underlyingLayer);
        this.sortModel = sortModel;

        registerPersistable(new SortStatePersistor<T>(sortModel));
        registerCommandHandler(new SortCommandHandler<T>(sortModel, this));

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultSortConfiguration());
        }
    }

    /**
     * @return adds a special configuration label to the stack taking into
     *         account the following:
     *         <ol>
     *         <li>Is the column sorted ?</li>
     *         <li>What is the sort order of the column</li>
     *         </ol>
     *         A special painter is registered against the above labels to
     *         render the sort arrows
     */
    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        LabelStack configLabels = super.getConfigLabelsByPosition(columnPosition, rowPosition);

        if (this.sortModel != null) {
            int columnIndex = getColumnIndexByPosition(columnPosition);
            if (this.sortModel.isColumnIndexSorted(columnIndex)) {

                String sortConfig = DefaultSortConfiguration.SORT_SEQ_CONFIG_TYPE + this.sortModel.getSortOrder(columnIndex);
                configLabels.addLabelOnTop(sortConfig);

                SortDirectionEnum sortDirection = this.sortModel.getSortDirection(columnIndex);

                switch (sortDirection) {
                    case ASC:
                        configLabels.addLabelOnTop(DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
                        break;
                    case DESC:
                        configLabels.addLabelOnTop(DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
                        break;
                }

                configLabels.addLabelOnTop(DefaultSortConfiguration.SORT_CONFIG_TYPE);
            }
        }
        return configLabels;
    }

    /**
     * @return The ISortModel that is used to handle the sorting of the
     *         underlying data.
     */
    public ISortModel getSortModel() {
        return this.sortModel;
    }

    /**
     * @since 1.4
     */
    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> labels = super.getProvidedLabels();

        labels.add(DefaultSortConfiguration.SORT_CONFIG_TYPE);
        labels.add(DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
        labels.add(DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
        labels.add(DefaultSortConfiguration.SORT_SEQ_CONFIG_TYPE + "0"); //$NON-NLS-1$
        labels.add(DefaultSortConfiguration.SORT_SEQ_CONFIG_TYPE + "1"); //$NON-NLS-1$
        labels.add(DefaultSortConfiguration.SORT_SEQ_CONFIG_TYPE + "2"); //$NON-NLS-1$

        return labels;
    }

}

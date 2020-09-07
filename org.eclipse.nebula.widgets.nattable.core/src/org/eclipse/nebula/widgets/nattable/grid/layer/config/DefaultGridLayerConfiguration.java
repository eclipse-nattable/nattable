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
package org.eclipse.nebula.widgets.nattable.grid.layer.config;

import org.eclipse.nebula.widgets.nattable.config.AggregateConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditBindings;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditConfiguration;
import org.eclipse.nebula.widgets.nattable.export.config.DefaultExportBindings;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.print.config.DefaultPrintBindings;

/**
 * Sets up features handled at the grid level. Added by {@link GridLayer}
 */
public class DefaultGridLayerConfiguration extends AggregateConfiguration {

    public DefaultGridLayerConfiguration(CompositeLayer gridLayer) {
        addAlternateRowColoringConfig(gridLayer);
        addEditingHandlerConfig();
        addEditingUIConfig();
        addPrintUIBindings();
        addExcelExportUIBindings();
    }

    protected void addExcelExportUIBindings() {
        addConfiguration(new DefaultExportBindings());
    }

    protected void addPrintUIBindings() {
        addConfiguration(new DefaultPrintBindings());
    }

    protected void addEditingUIConfig() {
        addConfiguration(new DefaultEditBindings());
    }

    protected void addEditingHandlerConfig() {
        addConfiguration(new DefaultEditConfiguration());
    }

    protected void addAlternateRowColoringConfig(CompositeLayer gridLayer) {
        addConfiguration(new DefaultRowStyleConfiguration());
        gridLayer.setConfigLabelAccumulatorForRegion(
                GridRegion.BODY,
                new AlternatingRowConfigLabelAccumulator(
                        gridLayer.getChildLayerByRegionName(GridRegion.BODY)));
    }

}

/*******************************************************************************
 * Copyright (c) 2012, 2014 Edwin Park, Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edwin Park - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 448115, 449361
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

public class GroupByDataLayerConfiguration<T> extends AbstractRegistryConfiguration {

    private final GroupByDataLayer<T> groupByDataLayer;

    public GroupByDataLayerConfiguration(GroupByDataLayer<T> groupByDataLayer) {
        this.groupByDataLayer = groupByDataLayer;
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        // register a TextPainter to ensure that the GroupBy objects are
        // rendered as text even if in the first column by default another
        // painter is registered
        // necessary for example if a different painter is registered for a
        // column (e.g. CheckBoxPainter) that needs a special data type
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER,
                new BackgroundPainter(new GroupByCellTextPainter()),
                DisplayMode.NORMAL,
                GroupByDataLayer.GROUP_BY_OBJECT);

        // register a converter for GroupByObjects that also handles GroupBy
        // summary values
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                new GroupByDisplayConverter<T>(this.groupByDataLayer),
                DisplayMode.NORMAL,
                GroupByDataLayer.GROUP_BY_OBJECT);
    }

}

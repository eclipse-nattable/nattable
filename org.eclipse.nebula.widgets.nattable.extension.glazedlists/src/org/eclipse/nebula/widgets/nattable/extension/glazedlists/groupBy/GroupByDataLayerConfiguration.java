/*******************************************************************************
 * Copyright (c) 2012 Edwin Park and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Edwin Park - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary.IGroupBySummaryProvider;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryDisplayConverter;

public class GroupByDataLayerConfiguration extends
        AbstractRegistryConfiguration {

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        // register a TextPainter to ensure that the GroupBy objects are
        // rendered as text
        // even if in the first column by default another painter is registered
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER, new BackgroundPainter(
                        new GroupByCellTextPainter()), DisplayMode.NORMAL,
                GroupByDataLayer.GROUP_BY_OBJECT);

        // register a converter for group by summary values that renders ... in
        // case there is
        // no summary value calculated yet
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                new SummaryDisplayConverter(new DefaultDisplayConverter(),
                        IGroupBySummaryProvider.DEFAULT_SUMMARY_VALUE),
                DisplayMode.NORMAL, GroupByDataLayer.GROUP_BY_SUMMARY);
    }

}

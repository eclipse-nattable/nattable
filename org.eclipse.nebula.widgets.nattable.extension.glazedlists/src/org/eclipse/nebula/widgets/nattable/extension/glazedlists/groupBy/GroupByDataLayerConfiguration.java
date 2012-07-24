package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.swt.graphics.GC;

public class GroupByDataLayerConfiguration extends AbstractRegistryConfiguration {

	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER,
				new BackgroundPainter(new TextPainter() {
					@Override
					protected String getTextToDisplay(ILayerCell cell, GC gc, int availableLength, String text) {
						if (cell.getConfigLabels().getLabels().contains(TreeLayer.TREE_COLUMN_CELL)) {
							return super.getTextToDisplay(cell, gc, availableLength, text);
						} else {
							return ""; //$NON-NLS-1$
						}
					}
				}),
				DisplayMode.NORMAL,
				GroupByDataLayer.GROUP_BY_OBJECT
		);
	}
	
}

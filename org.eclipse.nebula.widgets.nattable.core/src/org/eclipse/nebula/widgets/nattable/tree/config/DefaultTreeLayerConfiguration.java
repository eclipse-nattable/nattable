/*******************************************************************************
 * Copyright (c) Sep 7, 2012 Edwin Park and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Edwin Park - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree.config;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.export.ExportConfigAttributes;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.action.TreeExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;

/**
 * @author Edwin Park
 *
 */
public class DefaultTreeLayerConfiguration implements IConfiguration {
	
	private TreeLayer treeLayer;

	/**
	 * 
	 */
	public DefaultTreeLayerConfiguration(TreeLayer treeLayer) {
		this.treeLayer = treeLayer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.config.IConfiguration#configureLayer(org.eclipse.nebula.widgets.nattable.layer.ILayer)
	 */
	public void configureLayer(ILayer layer) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.config.IConfiguration#configureRegistry(org.eclipse.nebula.widgets.nattable.config.IConfigRegistry)
	 */
	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_STYLE,
				new Style() {{
					setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT,  HorizontalAlignmentEnum.LEFT);
				}},
				DisplayMode.NORMAL,
				TreeLayer.TREE_COLUMN_CELL
		);
		configRegistry.registerConfigAttribute(
				ExportConfigAttributes.EXPORT_FORMATTER,
				new TreeExportFormatter(treeLayer.getModel()),
				DisplayMode.NORMAL,
				TreeLayer.TREE_COLUMN_CELL
		);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.config.IConfiguration#configureUiBindings(org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry)
	 */
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		TreeExpandCollapseAction treeExpandCollapseAction = new TreeExpandCollapseAction();
		CellPainterMouseEventMatcher treeImagePainterMouseEventMatcher = new CellPainterMouseEventMatcher(GridRegion.BODY, MouseEventMatcher.LEFT_BUTTON, treeLayer.getTreeImagePainter());
		
		uiBindingRegistry.registerFirstSingleClickBinding(
				treeImagePainterMouseEventMatcher,
				treeExpandCollapseAction
		);
		
		// Obscure any mouse down bindings for this image painter
		uiBindingRegistry.registerFirstMouseDownBinding(
				treeImagePainterMouseEventMatcher,
				new NoOpMouseAction()
		);
	}

}

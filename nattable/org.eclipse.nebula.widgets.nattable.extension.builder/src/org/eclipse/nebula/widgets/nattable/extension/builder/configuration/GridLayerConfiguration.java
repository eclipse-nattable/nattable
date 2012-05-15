/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.builder.configuration;

import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableStyle;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultGridLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultRowStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;


public class GridLayerConfiguration extends DefaultGridLayerConfiguration {

	private final TableStyle properties;

	public GridLayerConfiguration(GridLayer gridLayer, TableStyle tableStyle) {
		super(gridLayer);
		this.properties = tableStyle;
		alternateRowColorConfig(gridLayer);
	}

	/** Prevent the superclass from setting this */
	@Override
	protected void addAlternateRowColoringConfig(CompositeLayer gridLayer) {}

	private void alternateRowColorConfig(GridLayer gridLayer) {
		DefaultRowStyleConfiguration rowStyleConfig = new DefaultRowStyleConfiguration();
		rowStyleConfig.evenRowBgColor = properties.evenRowColor;
		rowStyleConfig.oddRowBgColor = properties.oddRowColor;
		addConfiguration(rowStyleConfig);

		gridLayer.setConfigLabelAccumulatorForRegion(GridRegion.BODY, new AlternatingRowConfigLabelAccumulator());
	}

}

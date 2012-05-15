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
package org.eclipse.nebula.widgets.nattable.examples.examples._104_Styling;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.PercentageDisplayConverter;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.PercentageBarCellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PercentageBarDecorator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This example demonstrates the use of custom painters.
 * The cells contain data as decimal numbers. This decimal number is converted into a percentage
 * and rendered by the painter as bars in the cell. 
 */
public class _002_Using_a_custom_cell_painter extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new _002_Using_a_custom_cell_painter());
	}

	public Control createExampleControl(Composite parent) {
		// Setup the layer stack
		final MyDataProvider myDataProvider = new MyDataProvider();
		SelectionLayer selectionLayer = new SelectionLayer(new DataLayer(myDataProvider));
		ILayer columnHeaderLayer = new ColumnHeaderLayer(new DataLayer(new DummyColumnHeaderDataProvider(myDataProvider)), selectionLayer, selectionLayer);

		CompositeLayer compositeLayer = new CompositeLayer(1, 2);
		compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER, columnHeaderLayer, 0, 0);
		compositeLayer.setChildLayer(GridRegion.BODY, selectionLayer, 0, 1);

		NatTable natTable = new NatTable(parent, compositeLayer, false);

		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		
		// Add our custom painting configuration
		natTable.addConfiguration(new CustomPaintingConfig());

		natTable.configure();
		return natTable;
	}
	
	class CustomPaintingConfig extends AbstractRegistryConfiguration {

		public void configureRegistry(IConfigRegistry configRegistry) {
			// Register cell style
			Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			
			cellStyle.setAttributeValue(
							PercentageBarDecorator.PERCENTAGE_BAR_COMPLETE_REGION_START_COLOR,
							GUIHelper.getColor(new RGB(251, 149, 123)));
			cellStyle.setAttributeValue(
							PercentageBarDecorator.PERCENTAGE_BAR_COMPLETE_REGION_END_COLOR,
							GUIHelper.getColor(new RGB(248, 253, 219)));
			cellStyle.setAttributeValue(
							PercentageBarDecorator.PERCENTAGE_BAR_INCOMPLETE_REGION_COLOR,
							GUIHelper.getColor(new RGB(236, 217, 255)));
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle);

			// Register custom painter, paints bars
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new PercentageBarCellPainter(),
					DisplayMode.NORMAL, GridRegion.BODY.toString());

			// Register custom converter, converts to percentage
			configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new PercentageDisplayConverter(), 
					DisplayMode.NORMAL, GridRegion.BODY.toString());
		}

	}
	
	/**
	 * Provides decimal numbers < 1 as cell values.
	 */
	class MyDataProvider implements IDataProvider {

		public int getColumnCount() {
			return 10;
		}

		public int getRowCount() {
			return 10;
		}

		public Object getDataValue(int columnIndex, int rowIndex) {
			// Create a decimal value
			return Double.valueOf((rowIndex * 10 + columnIndex) / 100D);
		}

		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
			// Do nothing
		}

	}
}

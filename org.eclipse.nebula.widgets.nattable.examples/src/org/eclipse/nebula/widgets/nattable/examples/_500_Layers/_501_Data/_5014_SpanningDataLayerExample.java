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
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._501_Data;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ISpanningDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditBindings;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditConfiguration;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.SpanningDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.DataCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Example to show the usage of SpanningDataLayer. For this a DummySpanningBodyDataProvider
 * is used that spans several blocks. Editing is also enabled to show and verify that editing
 * of spanned cells is possible.
 *  
 * @author Dirk Fauth
 *
 */
public class _5014_SpanningDataLayerExample extends AbstractNatExample {
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new _5014_SpanningDataLayerExample());
	}

	@Override
	public String getDescription() {
		return "This example shows the usage of the SpanningDataLayer. It uses a ISpanningDataProvider "
				+ "that spans several periodic blocks. Editing is also enabled to show and verify "
				+ "that editing of spanned cells is working correctly.";
	}

	@Override
	public Control createExampleControl(Composite parent) {
		//To make the default edit and selection configurations work correctly, the region label
		//GridRegion.BODY is necessary, which is directly set to the ViewportLayer instance here.
		ViewportLayer layer = new ViewportLayer(
				new SelectionLayer(
						new SpanningDataLayer(new DummySpanningBodyDataProvider(100, 100))));
		layer.setRegionName(GridRegion.BODY);

		NatTable natTable = new NatTable(parent, layer, false);
		
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		
		//add configurations to enable editing
		//this is to verify that spanned cells are also editable and update the data model correctly
		//@see Bug 414754
		layer.addConfiguration(new DefaultEditBindings());
		layer.addConfiguration(new DefaultEditConfiguration());
		layer.addConfiguration(new AbstractRegistryConfiguration() {

			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				configRegistry.registerConfigAttribute(
						EditConfigAttributes.CELL_EDITABLE_RULE, 
						IEditableRule.ALWAYS_EDITABLE);
			}
			
		});
		
		natTable.configure();
		
		return natTable;
	}
	
	class DummySpanningBodyDataProvider implements IDataProvider, ISpanningDataProvider {
		
		private final int columnCount;
		private final int rowCount;

		private Map<Point, Object> values = new HashMap<Point, Object>();

		private static final int BLOCK_SIZE = 4;
		private static final int CELL_SPAN = 2;
		
		public DummySpanningBodyDataProvider(int columnCount, int rowCount) {
			this.columnCount = columnCount;
			this.rowCount = rowCount;
		}
		
		@Override
		public int getColumnCount() {
			return columnCount;
		}

		@Override
		public int getRowCount() {
			return rowCount;
		}

		@Override
		public Object getDataValue(int columnIndex, int rowIndex) {
			Point point = new Point(columnIndex, rowIndex);
			if (values.containsKey(point)) {
				return values.get(point);
			} else {
				return "Col: " + (columnIndex + 1) + ", Row: " + (rowIndex + 1); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		@Override
		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
			values.put(new Point(columnIndex, rowIndex), newValue);
		}
		
		@Override
		public DataCell getCellByPosition(int columnPosition, int rowPosition) {
			int columnBlock = columnPosition / BLOCK_SIZE;
			int rowBlock = rowPosition / BLOCK_SIZE;
			
			boolean isSpanned = isEven(columnBlock + rowBlock) && (columnPosition % BLOCK_SIZE) < CELL_SPAN && (rowPosition % BLOCK_SIZE) < CELL_SPAN;
			int columnSpan = isSpanned ? CELL_SPAN : 1;
			int rowSpan = isSpanned ? CELL_SPAN : 1;
			
			int cellColumnPosition = columnPosition;
			int cellRowPosition = rowPosition;
			
			if (isSpanned) {
				cellColumnPosition -= columnPosition % BLOCK_SIZE;
				cellRowPosition -= rowPosition % BLOCK_SIZE;
			}
			
			return new DataCell(cellColumnPosition, cellRowPosition, columnSpan, rowSpan);
		}
		
		private boolean isEven(int i) {
			return i % 2 == 0;
		}

	}
}

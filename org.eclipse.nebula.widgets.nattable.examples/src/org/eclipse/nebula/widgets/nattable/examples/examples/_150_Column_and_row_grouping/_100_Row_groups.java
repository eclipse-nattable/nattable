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
package org.eclipse.nebula.widgets.nattable.examples.examples._150_Column_and_row_grouping;

import java.util.Map;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.RowGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.model.RowGroup;
import org.eclipse.nebula.widgets.nattable.group.model.RowGroupModel;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowGroupDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowGroupDataListFixture;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _100_Row_groups extends AbstractNatExample {

	public static void main(String[] args) {
		StandaloneNatExampleRunner.run(800, 400, new _100_Row_groups());
	}
	
	@Override
	public String getDescription() {
		return
				"This example demonstrates row grouping functionality:\n" +
				"\n" +
				"* EXPAND/COLLAPSE A ROW GROUP by double-clicking on the row group header.";
	}

	public Control createExampleControl(Composite parent) {
		// Body

		String[] propertyNames = RowGroupDataListFixture.getPropertyNames();
		Map<String, String> propertyToLabelMap = RowGroupDataListFixture.getPropertyToLabelMap();
		
		DefaultBodyDataProvider<RowGroupDataFixture> bodyDataProvider = new DefaultBodyDataProvider<RowGroupDataFixture>(RowGroupDataListFixture.getList(2000), propertyNames);
		DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
		
		ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);
		ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
		
		RowHideShowLayer rowHideShowLayer = new RowHideShowLayer(columnHideShowLayer);		
		RowGroupModel<RowGroupDataFixture> rowGroupModel = new RowGroupModel<RowGroupDataFixture>();
		rowGroupModel.setDataProvider(bodyDataProvider);
		RowGroupExpandCollapseLayer<RowGroupDataFixture> rowExpandCollapseLayer = new RowGroupExpandCollapseLayer<RowGroupDataFixture>(rowHideShowLayer, rowGroupModel);
		
		SelectionLayer selectionLayer = new SelectionLayer(rowExpandCollapseLayer);
		ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);
		
		// Column header

		DefaultColumnHeaderDataProvider defaultColumnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		DefaultColumnHeaderDataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(defaultColumnHeaderDataProvider);
		ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer);
		
		// Row header
		
		DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		
		RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer, false);
		rowHeaderLayer.addConfiguration(new RowHeaderConfiguration());
		
		RowGroupHeaderLayer<RowGroupDataFixture> rowGroupHeaderLayer = new RowGroupHeaderLayer<RowGroupDataFixture>(rowHeaderLayer, selectionLayer, rowGroupModel);
		rowGroupHeaderLayer.setColumnWidth(15);
		
		// Create a group of rows for the model.
		RowGroup<RowGroupDataFixture> rowGroup = new RowGroup<RowGroupDataFixture>(rowGroupModel, "Group 1", true);		
		rowGroup.addMemberRow(bodyDataProvider.getRowObject(1));
		rowGroup.addStaticMemberRow(bodyDataProvider.getRowObject(2));
		rowGroupModel.addRowGroup(rowGroup);
		
		rowGroup = new RowGroup<RowGroupDataFixture>(rowGroupModel, "Group 2", true);		
		rowGroup.addMemberRow(bodyDataProvider.getRowObject(11));
		rowGroup.addMemberRow(bodyDataProvider.getRowObject(12));
		rowGroup.addStaticMemberRow(bodyDataProvider.getRowObject(13));
		rowGroupModel.addRowGroup(rowGroup);
		
		rowGroup = new RowGroup<RowGroupDataFixture>(rowGroupModel, "Group 3", false);		
		rowGroup.addMemberRow(bodyDataProvider.getRowObject(18));
		rowGroup.addMemberRow(bodyDataProvider.getRowObject(19));
		rowGroup.addStaticMemberRow(bodyDataProvider.getRowObject(20));
		rowGroupModel.addRowGroup(rowGroup);
		
		// Corner

		final DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(defaultColumnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowGroupHeaderLayer, columnHeaderLayer);

		// Grid
		GridLayer gridLayer = new GridLayer(
				viewportLayer,
				columnHeaderLayer,
				rowGroupHeaderLayer,
				cornerLayer);


		NatTable natTable = new NatTable(parent, gridLayer, false);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable));

		natTable.configure();
		return natTable;
	}
	
	private class RowHeaderConfiguration extends DefaultRowHeaderLayerConfiguration {		
		@Override
		protected void addRowHeaderUIBindings() {
			// We're suppressing the row resize bindings.
		}
	}

}

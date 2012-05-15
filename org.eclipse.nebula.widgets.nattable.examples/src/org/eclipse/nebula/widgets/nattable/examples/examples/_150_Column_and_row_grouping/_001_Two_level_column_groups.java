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
import org.eclipse.nebula.widgets.nattable.columnChooser.command.DisplayColumnChooserCommandHandler;
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
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.ColumnGroupBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _001_Two_level_column_groups extends AbstractNatExample {

	public static void main(String[] args) {
		StandaloneNatExampleRunner.run(800, 400, new _001_Two_level_column_groups());
	}

	private final ColumnGroupModel columnGroupModel = new ColumnGroupModel();
	private final ColumnGroupModel sndColumnGroupModel = new ColumnGroupModel();
	
	private ColumnHeaderLayer columnHeaderLayer;

	public Control createExampleControl(Composite parent) {
		// Body

		String[] propertyNames = RowDataListFixture.getPropertyNames();
		Map<String, String> propertyToLabelMap = RowDataListFixture.getPropertyToLabelMap();
		DefaultBodyDataProvider<RowDataFixture> bodyDataProvider = new DefaultBodyDataProvider<RowDataFixture>(RowDataListFixture.getList(2000),
				propertyNames);
		ColumnGroupBodyLayerStack bodyLayer = new ColumnGroupBodyLayerStack(new DataLayer(bodyDataProvider), columnGroupModel);

		// Column header

		DefaultColumnHeaderDataProvider defaultColumnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		DefaultColumnHeaderDataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(defaultColumnHeaderDataProvider);
		columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayer, bodyLayer.getSelectionLayer());
		ColumnGroupHeaderLayer columnGroupHeaderLayer = new ColumnGroupHeaderLayer(columnHeaderLayer, bodyLayer.getSelectionLayer(), columnGroupModel);

		columnGroupHeaderLayer.addColumnsIndexesToGroup("Group 1", 1,2);
		columnGroupHeaderLayer.addColumnsIndexesToGroup("UnBreakable group 2", 4, 5,6,7);
		columnGroupHeaderLayer.addColumnsIndexesToGroup("UnBreakable group 3", 8,9,10);
		columnGroupHeaderLayer.addColumnsIndexesToGroup("Group 4", 11,12,13);
		columnGroupHeaderLayer.addColumnsIndexesToGroup("Group 5", 14, 15, 16, 17);
		columnGroupHeaderLayer.setGroupUnbreakable(4);
		columnGroupHeaderLayer.setGroupUnbreakable(8);
		columnGroupHeaderLayer.setGroupAsCollapsed(11);
		
		ColumnGroupGroupHeaderLayer sndGroup = new ColumnGroupGroupHeaderLayer(columnGroupHeaderLayer, bodyLayer.getSelectionLayer(), sndColumnGroupModel);
		
		sndGroup.addColumnsIndexesToGroup("GroupGroup 1", 1,2,3,4,5,6,7);
		sndGroup.addColumnsIndexesToGroup("GroupGroup 2", 11,12,13,14,15,16,17);

		// Row header

		final DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayer, bodyLayer.getSelectionLayer());

		// Corner

		final DefaultCornerDataProvider cornerDataProvider =
			new DefaultCornerDataProvider(defaultColumnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, sndGroup);

		// Grid
		GridLayer gridLayer = new GridLayer(
				bodyLayer,
				sndGroup,
				rowHeaderLayer,
				cornerLayer);


		NatTable natTable = new NatTable(parent, gridLayer, false);

		// Register create column group command handler

		// Register column chooser
		DisplayColumnChooserCommandHandler columnChooserCommandHandler = new DisplayColumnChooserCommandHandler(
				bodyLayer.getSelectionLayer(),
				bodyLayer.getColumnHideShowLayer(),
				columnHeaderLayer,
				columnHeaderDataLayer,
				columnGroupHeaderLayer,
				columnGroupModel);
		bodyLayer.registerCommandHandler(columnChooserCommandHandler);

		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable) {
			@Override
			protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
				return super.createColumnHeaderMenu(natTable).withColumnChooserMenuItem();
			}
		});

		natTable.configure();
		return natTable;
	}

}

/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.export.ExportConfigAttributes;
import org.eclipse.nebula.widgets.nattable.extension.poi.HSSFExcelExporter;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
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
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.config.ColumnGroupMenuItemProviders;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.ColumnGroupBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class _000_Column_groups extends AbstractNatExample {

	public static void main(String[] args) {
		StandaloneNatExampleRunner.run(800, 400, new _000_Column_groups());
	}

	@Override
	public String getDescription() {
		return
				"This example demonstrates column grouping functionality:\n" +
				"\n" +
				"* GROUP SELECTED COLUMNS with ctrl-g.\n" +
				"* UNGROUP SELECTED COLUMNS with ctrl-u.\n" +
				"* EXPAND/COLLAPSE A COLUMN GROUP by double-clicking on the column group header.\n" +
				"* DRAG COLUMNS IN/OUT OF COLUMN GROUPS: If a column is dragged to the beginning of a column group, it is " +
				"included in the column group. If it is dragged to the end of a column group, it is removed from the column group.\n" +
				"* POPUP MENU: Right-clicking on a column in the column header will bring up a popup menu that will allow you to HIDE, " +
				"SHOW or AUTO-RESIZE the selected columns (note: auto-resize takes a while because there are a lot of rows). There is " +
				"also an option to launch the COLUMN CHOOSER dialog.";
	}
	
	private final ColumnGroupModel columnGroupModel = new ColumnGroupModel();
	private ColumnHeaderLayer columnHeaderLayer;

	public Control createExampleControl(Composite parent) {
		// Body

		String[] propertyNames = RowDataListFixture.getPropertyNames();
		Map<String, String> propertyToLabelMap = RowDataListFixture.getPropertyToLabelMap();
		DefaultBodyDataProvider<RowDataFixture> bodyDataProvider = new DefaultBodyDataProvider<RowDataFixture>(RowDataListFixture.getList(200),
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
		columnGroupHeaderLayer.setStaticColumnIndexesByGroup("Group 5", 15, 17);
		columnGroupHeaderLayer.setGroupUnbreakable(4);
		columnGroupHeaderLayer.setGroupUnbreakable(8);
		columnGroupHeaderLayer.setGroupAsCollapsed(11);

		// Row header

		final DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayer, bodyLayer.getSelectionLayer());

		// Corner

		final DefaultCornerDataProvider cornerDataProvider =
			new DefaultCornerDataProvider(defaultColumnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnGroupHeaderLayer);

		// Grid
		GridLayer gridLayer = new GridLayer(
				bodyLayer,
				columnGroupHeaderLayer,
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
		natTable.addConfiguration(new AbstractRegistryConfiguration() {
			public void configureRegistry(IConfigRegistry configRegistry) {
				configRegistry.registerConfigAttribute(ExportConfigAttributes.EXPORTER, new HSSFExcelExporter());
			}
		});
		
		// Column group header menu
		final Menu columnGroupHeaderMenu =
				new PopupMenuBuilder(natTable)
					.withMenuItemProvider(ColumnGroupMenuItemProviders.renameColumnGroupMenuItemProvider())
					.withMenuItemProvider(ColumnGroupMenuItemProviders.removeColumnGroupMenuItemProvider())
					.build();
		
		natTable.addConfiguration(new AbstractUiBindingConfiguration() {
			public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
				uiBindingRegistry.registerFirstMouseDownBinding(
						new MouseEventMatcher(SWT.NONE, GridRegion.COLUMN_GROUP_HEADER, MouseEventMatcher.RIGHT_BUTTON),
						new PopupMenuAction(columnGroupHeaderMenu));
			}
		});

		natTable.configure();
		return natTable;
	}

}

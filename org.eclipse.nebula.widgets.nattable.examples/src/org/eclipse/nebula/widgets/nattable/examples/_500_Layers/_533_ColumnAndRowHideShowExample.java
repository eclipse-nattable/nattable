/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.Person;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.config.DefaultFreezeGridBindings;
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
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.menu.AbstractHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Simple example showing how to add the {@link ColumnHideShowLayer} and the 
 * {@link RowHideShowLayer} to the layer composition of a grid and how to add 
 * the corresponding actions to the header menus.
 * 
 * Also adds the functionality to manage NatTable states to proof that the
 * visibility states are stored and loaded correctly.
 * 
 * @author Dirk Fauth
 *
 */
public class _533_ColumnAndRowHideShowExample extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new _533_ColumnAndRowHideShowExample());
	}

	@Override
	public String getDescription() {
		return "This example shows the usage of the ColumnHideShowLayer and the RowHideShowLayer "
				+ "within a grid and its corresponding actions in the header menus. If you perform "
				+ "a right click on the corresponding header, you are able to hide the current "
				+ "selection or show all columns/rows again.\n"
				+ "The column header menu also gives the opportunity to manage the NatTable states";
	}
	
	@Override
	public Control createExampleControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		
		//property names of the Person class
		String[] propertyNames = {"firstName", "lastName", "gender", "married", "birthday"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("firstName", "Firstname");
		propertyToLabelMap.put("lastName", "Lastname");
		propertyToLabelMap.put("gender", "Gender");
		propertyToLabelMap.put("married", "Married");
		propertyToLabelMap.put("birthday", "Birthday");

		//build the body layer stack 
		//Usually you would create a new layer stack by extending AbstractIndexLayerTransform and
		//setting the ViewportLayer as underlying layer. But in this case using the ViewportLayer
		//directly as body layer is also working.
		IDataProvider bodyDataProvider = new DefaultBodyDataProvider<Person>(PersonService.getPersons(10), propertyNames);
		DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
		ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(bodyDataLayer);
		RowHideShowLayer rowHideShowLayer = new RowHideShowLayer(columnHideShowLayer);
		final SelectionLayer selectionLayer = new SelectionLayer(rowHideShowLayer);
		final ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

		final FreezeLayer freezeLayer = new FreezeLayer(selectionLayer);
	    final CompositeFreezeLayer compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer,viewportLayer, selectionLayer);

		//build the column header layer
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, compositeFreezeLayer, selectionLayer);
		
		//build the row header layer
		IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, compositeFreezeLayer, selectionLayer);
		
		//build the corner layer
		IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);
		
		//build the grid layer
		GridLayer gridLayer = new GridLayer(compositeFreezeLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer);
		
		//turn the auto configuration off as we want to add our header menu configuration
		final NatTable natTable = new NatTable(panel, gridLayer, false);
		
		//as the autoconfiguration of the NatTable is turned off, we have to add the 
		//DefaultNatTableStyleConfiguration manually	
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new DefaultFreezeGridBindings());
		
		//add the header menu configuration for adding the column header menu with hide/show actions
		natTable.addConfiguration(new AbstractHeaderMenuConfiguration(natTable) {
			
			@Override
			protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
				return super.createColumnHeaderMenu(natTable)
							.withHideColumnMenuItem()
							.withShowAllColumnsMenuItem()
							.withStateManagerMenuItemProvider();
			}
			
			@Override
			protected PopupMenuBuilder createRowHeaderMenu(NatTable natTable) {
				return super.createRowHeaderMenu(natTable)
							.withHideRowMenuItem()
							.withShowAllRowsMenuItem()
							.withStateManagerMenuItemProvider();
			}
			
			@Override
			protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
				return super.createCornerMenu(natTable)
							.withShowAllColumnsMenuItem()
							.withShowAllRowsMenuItem()
							.withStateManagerMenuItemProvider();
			}
		});
		natTable.configure();
		
		panel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
		
		gridLayer.registerCommandHandler(new DisplayPersistenceDialogCommandHandler(natTable));
		
		return panel;
	}

}

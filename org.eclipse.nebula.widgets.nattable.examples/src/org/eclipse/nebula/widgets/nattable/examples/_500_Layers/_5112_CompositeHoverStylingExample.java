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

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.Person;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.config.ColumnHeaderHoverLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Simple example showing how to add the {@link HoverLayer} to a layer composition with column header and body.
 * 
 * @author Dirk Fauth
 *
 */
public class _5112_CompositeHoverStylingExample extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 400, new _5112_CompositeHoverStylingExample());
	}

	@Override
	public String getDescription() {
		return "This example shows the usage of the HoverLayer within a layer composition "
				+ "that consists of a column header and a body.";
	}
	
	@Override
	public Control createExampleControl(Composite parent) {
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
		HoverLayer hoverLayer = new HoverLayer(bodyDataLayer);
		SelectionLayer selectionLayer = new SelectionLayer(hoverLayer);
		ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);
		
		//build the column header layer
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		HoverLayer columnHoverLayer = new HoverLayer(columnHeaderDataLayer, false);
		ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHoverLayer, viewportLayer, selectionLayer, false);
		
		//add ColumnHeaderHoverLayerConfiguration to ensure that hover styling and resizing is working together
		columnHeaderLayer.addConfiguration(new ColumnHeaderHoverLayerConfiguration(columnHoverLayer));

		CompositeLayer compLayer = new CompositeLayer(1, 2);
		compLayer.setChildLayer(GridRegion.COLUMN_HEADER, columnHeaderLayer, 0, 0);
		compLayer.setChildLayer(GridRegion.BODY, viewportLayer, 0, 1);
		
		//turn the auto configuration off as we want to add our hover styling configuration
		NatTable natTable = new NatTable(parent, compLayer, false);
		
		//as the autoconfiguration of the NatTable is turned off, we have to add the 
		//DefaultNatTableStyleConfiguration manually	
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		
		//add the style configuration for hover
		natTable.addConfiguration(new AbstractRegistryConfiguration() {
			
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				Style style = new Style();
				style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.getColor(217, 232, 251));
				
				configRegistry.registerConfigAttribute(
						CellConfigAttributes.CELL_STYLE, 
						style, 
						DisplayMode.HOVER);
				
				Image bgImage = new Image(
						Display.getDefault(), 
						getClass().getResourceAsStream("../resources/column_header_bg.png"));
				Image hoverBgImage = new Image(
						Display.getDefault(), 
						getClass().getResourceAsStream("../resources/hovered_column_header_bg.png"));

				TextPainter txtPainter = new TextPainter(false, false);

				ICellPainter bgImagePainter = new BackgroundImagePainter(txtPainter, bgImage, GUIHelper.getColor(192, 192, 192));

				configRegistry.registerConfigAttribute(
						CellConfigAttributes.CELL_PAINTER, 
						bgImagePainter, 
						DisplayMode.NORMAL, 
						GridRegion.COLUMN_HEADER);
				configRegistry.registerConfigAttribute(
						CellConfigAttributes.CELL_PAINTER, 
						bgImagePainter, 
						DisplayMode.NORMAL, 
						GridRegion.CORNER);

				ICellPainter hoveredHeaderPainter = 
						new BackgroundImagePainter(txtPainter, hoverBgImage, GUIHelper.getColor(192, 192, 192));

				configRegistry.registerConfigAttribute(
						CellConfigAttributes.CELL_PAINTER, 
						hoveredHeaderPainter, 
						DisplayMode.HOVER, 
						GridRegion.COLUMN_HEADER);
			}
		});
		
		natTable.configure();
		
		return natTable;
	}

}

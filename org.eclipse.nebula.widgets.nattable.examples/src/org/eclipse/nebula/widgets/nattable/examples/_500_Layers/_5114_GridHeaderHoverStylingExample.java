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
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
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
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.action.ClearHoverStylingAction;
import org.eclipse.nebula.widgets.nattable.hover.config.ColumnHeaderHoverLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.hover.config.RowHeaderHoverLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Simple example showing how to add the {@link HoverLayer} to a grid layer composition.
 * This example only show how to add hover styling on the headers of a grid.
 * 
 * @author Dirk Fauth
 *
 */
public class _5114_GridHeaderHoverStylingExample extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 400, new _5114_GridHeaderHoverStylingExample());
	}

	@Override
	public String getDescription() {
		return "This example shows the usage of the HoverLayer within a grid only for header layers.";
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
		SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer);
		ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

		//build the column header layer
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		HoverLayer columnHoverLayer = new HoverLayer(columnHeaderDataLayer, false);
		ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHoverLayer, viewportLayer, selectionLayer, false);
		
		//add ColumnHeaderHoverLayerConfiguration to ensure that hover styling and resizing is working together
		columnHeaderLayer.addConfiguration(new ColumnHeaderHoverLayerConfiguration(columnHoverLayer));
		
		//build the row header layer
		IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		HoverLayer rowHoverLayer = new HoverLayer(rowHeaderDataLayer, false);
		RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHoverLayer, viewportLayer, selectionLayer, false);
		
		//add RowHeaderHoverLayerConfiguration to ensure that hover styling and resizing is working together
		rowHeaderLayer.addConfiguration(new RowHeaderHoverLayerConfiguration(rowHoverLayer));
		
		//build the corner layer
		IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);
		
		//build the grid layer
		GridLayer gridLayer = new GridLayer(viewportLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer);
		
		//turn the auto configuration off as we want to add our header menu configuration
		NatTable natTable = new NatTable(parent, gridLayer, false);
		
		//as the autoconfiguration of the NatTable is turned off, we have to add the 
		//DefaultNatTableStyleConfiguration manually	
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		
		
		natTable.addConfiguration(new AbstractUiBindingConfiguration() {
			@Override
			public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
				uiBindingRegistry.registerMouseMoveBinding(
						new MouseEventMatcher(GridRegion.BODY), new ClearHoverStylingAction());
			}
		});
		
		//add the style configuration for hover
		natTable.addConfiguration(new AbstractRegistryConfiguration() {
			
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				Style style = new Style();
				style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_RED);
				
				configRegistry.registerConfigAttribute(
						CellConfigAttributes.CELL_STYLE, 
						style, 
						DisplayMode.HOVER,
						GridRegion.ROW_HEADER);
				
				Image bgImage = new Image(
						Display.getDefault(), 
						getClass().getResourceAsStream("../resources/column_header_bg.png"));
				Image hoverBgImage = new Image(
						Display.getDefault(), 
						getClass().getResourceAsStream("../resources/hovered_column_header_bg.png"));
				Image selectedBgImage = new Image(
						Display.getDefault(), 
						getClass().getResourceAsStream("../resources/selected_column_header_bg.png"));

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

				ICellPainter selectedHeaderPainter = 
						new BackgroundImagePainter(txtPainter, selectedBgImage, GUIHelper.getColor(192, 192, 192));

				configRegistry.registerConfigAttribute(
						CellConfigAttributes.CELL_PAINTER, 
						selectedHeaderPainter, 
						DisplayMode.SELECT, 
						GridRegion.COLUMN_HEADER);

			}
		});
		natTable.configure();
		
		return natTable;
	}

}

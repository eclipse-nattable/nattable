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
package org.eclipse.nebula.widgets.nattable.examples._600_AdditionalFunctions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.NumberValues;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.export.ExportConfigAttributes;
import org.eclipse.nebula.widgets.nattable.export.IExportFormatter;
import org.eclipse.nebula.widgets.nattable.export.NatExporter;
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
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * @author Dirk Fauth
 *
 */
public class _664_MultiExportExample extends AbstractNatExample {

	public static String COLUMN_ONE_LABEL = "ColumnOneLabel";
	public static String COLUMN_TWO_LABEL = "ColumnTwoLabel";

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 480, new _664_MultiExportExample());
	}

	@Override
	public String getDescription() {
		return "This example shows how to trigger an export for multiple NatTable instances.\n"
				+ "By pressing the button the two NatTable instances will be exported into one Excel with two sheets.\n"
				+ "This example does not add any further export configuration.";
	}
	
	@Override
	public Control createExampleControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		panel.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);
		
		Composite gridPanel = new Composite(panel, SWT.NONE);
		gridPanel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(gridPanel);
		
		Composite buttonPanel = new Composite(panel, SWT.NONE);
		buttonPanel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(buttonPanel);

		//property names of the Person class
		String[] propertyNames = {"firstName", "lastName", "gender", "married", "birthday"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("firstName", "Firstname");
		propertyToLabelMap.put("lastName", "Lastname");
		propertyToLabelMap.put("gender", "Gender");
		propertyToLabelMap.put("married", "Married");
		propertyToLabelMap.put("birthday", "Birthday");
		
		GridLayer grid = createGrid(propertyNames, propertyToLabelMap, PersonService.getPersons(5));
		final NatTable natTable = new NatTable(gridPanel, 
				grid, false);
		
		//add labels to show that alignment configurations are also exported correctly
		final ColumnOverrideLabelAccumulator columnLabelAccumulator = 
				new ColumnOverrideLabelAccumulator(grid.getBodyLayer());
		((AbstractLayer)grid.getBodyLayer()).setConfigLabelAccumulator(columnLabelAccumulator);
		columnLabelAccumulator.registerColumnOverrides(0, COLUMN_ONE_LABEL);
		columnLabelAccumulator.registerColumnOverrides(1, COLUMN_TWO_LABEL);

		
		natTable.addConfiguration(new AbstractRegistryConfiguration() {
			
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				Style style = new Style();
				style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
				style.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.TOP);
				configRegistry.registerConfigAttribute(
						CellConfigAttributes.CELL_STYLE, 
						style,
						DisplayMode.NORMAL,
						COLUMN_ONE_LABEL);
				
				style = new Style();
				style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
				style.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.BOTTOM);
				configRegistry.registerConfigAttribute(
						CellConfigAttributes.CELL_STYLE, 
						style,
						DisplayMode.NORMAL,
						COLUMN_TWO_LABEL);
			}
		});
		
		//adding this configuration adds the styles and the painters to use
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				configRegistry.registerConfigAttribute(
						ExportConfigAttributes.EXPORTER, new HSSFExcelExporter());

				configRegistry.registerConfigAttribute(
						ExportConfigAttributes.DATE_FORMAT, "dd.MM.yyyy");
			
				//register a custom formatter to the body of the grid
				//you could also implement different formatter for different columns by using the label mechanism
				configRegistry.registerConfigAttribute(
						ExportConfigAttributes.EXPORT_FORMATTER, 
						new ExampleExportFormatter(),
						DisplayMode.NORMAL,
						GridRegion.BODY);

				configRegistry.registerConfigAttribute(
						ExportConfigAttributes.EXPORT_FORMATTER, 
						new IExportFormatter() {
							@Override
							public Object formatForExport(ILayerCell cell, IConfigRegistry configRegistry) {
								//simply return the data value which is an integer for the row header
								//doing this avoids the default conversion to string for export
								return cell.getDataValue();
							}
						},
						DisplayMode.NORMAL,
						GridRegion.ROW_HEADER);
			}
		});
		
		natTable.configure();

		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
		
		//property names of the NumberValues class
		String[] numberPropertyNames = {"columnOneNumber", "columnTwoNumber", "columnThreeNumber", 
				"columnFourNumber",	"columnFiveNumber"};

		//mapping from property to label, needed for column header labels
		Map<String, String> numberPropertyToLabelMap = new HashMap<String, String>();
		numberPropertyToLabelMap.put("columnOneNumber", "Value One");
		numberPropertyToLabelMap.put("columnTwoNumber", "Value Two");
		numberPropertyToLabelMap.put("columnThreeNumber", "Value Three");
		numberPropertyToLabelMap.put("columnFourNumber", "Value Four");
		numberPropertyToLabelMap.put("columnFiveNumber", "Value Five");

		List<NumberValues> valuesToShow = new ArrayList<NumberValues>();
		valuesToShow.add(createNumberValues());
		valuesToShow.add(createNumberValues());
		valuesToShow.add(createNumberValues());
		valuesToShow.add(createNumberValues());
		valuesToShow.add(createNumberValues());
		
		final NatTable numberNatTable = new NatTable(gridPanel, 
				createGrid(numberPropertyNames, numberPropertyToLabelMap, valuesToShow), false);
		
		//adding this configuration adds the styles and the painters to use
		numberNatTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		numberNatTable.addConfiguration(new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
			
				//register a custom formatter to the body of the grid
				//you could also implement different formatter for different columns by using the label mechanism
				configRegistry.registerConfigAttribute(
						ExportConfigAttributes.EXPORT_FORMATTER, 
						new IExportFormatter() {
							@Override
							public Object formatForExport(ILayerCell cell, IConfigRegistry configRegistry) {
								//simply return the data value which is an integer for the row header
								//doing this avoids the default conversion to string for export
								return cell.getDataValue();
							}
						},
						DisplayMode.NORMAL,
						GridRegion.BODY);

				configRegistry.registerConfigAttribute(
						ExportConfigAttributes.EXPORT_FORMATTER, 
						new IExportFormatter() {
							@Override
							public Object formatForExport(ILayerCell cell, IConfigRegistry configRegistry) {
								//simply return the data value which is an integer for the row header
								//doing this avoids the default conversion to string for export
								return cell.getDataValue();
							}
						},
						DisplayMode.NORMAL,
						GridRegion.ROW_HEADER);
			}
		});
		
		numberNatTable.configure();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(numberNatTable);
		
		
		Button addColumnButton = new Button(buttonPanel, SWT.PUSH);
		addColumnButton.setText("Export");
		addColumnButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Map<String, NatTable> export = new HashMap<String, NatTable>();
				export.put("Persons", natTable);
				export.put("Numbers", numberNatTable);
				new NatExporter(Display.getCurrent().getActiveShell())
					.exportMultipleNatTables(new HSSFExcelExporter(), export);
			}
		});

		return panel;
	}
	
	private <T> GridLayer createGrid(String[] propertyNames, Map<String, String> propertyToLabelMap, List<T> values) {
		//build the body layer stack 
		//Usually you would create a new layer stack by extending AbstractIndexLayerTransform and
		//setting the ViewportLayer as underlying layer. But in this case using the ViewportLayer
		//directly as body layer is also working.
		IDataProvider bodyDataProvider = new DefaultBodyDataProvider<T>(values, propertyNames);
		DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
		SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer);
		ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

		//build the column header layer
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer);
		
		//build the row header layer
		IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer);
		
		//build the corner layer
		IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);
		
		//build the grid layer
		return new GridLayer(viewportLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer);
	}
	
	private NumberValues createNumberValues() {
		NumberValues nv = new NumberValues();
		nv.setColumnOneNumber(10);
		nv.setColumnTwoNumber(20);
		nv.setColumnThreeNumber(30);
		nv.setColumnFourNumber(40);
		nv.setColumnFiveNumber(50);
		return nv;
	}
	
	class ExampleExportFormatter implements IExportFormatter {
		@Override
		public Object formatForExport(ILayerCell cell, IConfigRegistry configRegistry) {
			Object data = cell.getDataValue();
			if (data != null) {
				try {
					if (data instanceof Boolean) {
						return ((Boolean)data).booleanValue() ? "X" : ""; //$NON-NLS-1$ //$NON-NLS-2$
					}
					else if (data instanceof Date) {
						//return the Date object directly to ensure Date type in export
						return data;
					}
					else {
						return data.toString();
					}
				}
				catch (Exception e) {
					//if that fails, we simply return the string value
					return data.toString();
				}
			}
			return ""; //$NON-NLS-1$
		}
	}

}

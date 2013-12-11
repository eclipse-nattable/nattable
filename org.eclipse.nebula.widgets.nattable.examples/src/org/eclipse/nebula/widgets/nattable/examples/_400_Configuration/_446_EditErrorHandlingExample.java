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
package org.eclipse.nebula.widgets.nattable.examples._400_Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.validate.DataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.ValidationFailedException;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.config.DialogErrorHandling;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.NumberValues;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class _446_EditErrorHandlingExample extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(1024, 400, new _446_EditErrorHandlingExample());
	}

	@Override
	public String getDescription() {
		return
				"This example demonstrates the modified edit behaviour of the NatTable using error handling strategies. " +
				"The columns are configured differently for handling conversion and validation errors:\n" +
				"\n" +
				"- Column 1:\n" +
				"No error handling for conversion and validation failures registered\n" +
				"\n" +
				"- Column 2:\n" +
				"Dialog error handling for conversion failures registered\n" +
				"No error handling for validation failures registered\n" +
				"\n" +
				"- Column 3:\n" +
				"No error handling for conversion failures registered\n" +
				"Dialog error handling for validation failures registered\n" +
				"\n" +
				"- Column 4:\n" +
				"Dialog error handling for conversion failures registered\n" +
				"Dialog error handling for validation failures registered\n" +
				"\n" +
				"- Column 5:\n" +
				"Will simulate to throw an IllegalArgumentException with default configuration on validation to show that " +
				"other exceptions than the defined ones for conversion and validation failures are logged completely.";
	}
	
	public static String COLUMN_ONE_LABEL = "ColumnOneLabel";
	public static String COLUMN_TWO_LABEL = "ColumnTwoLabel";
	public static String COLUMN_THREE_LABEL = "ColumnThreeLabel";
	public static String COLUMN_FOUR_LABEL = "ColumnFourLabel";
	public static String COLUMN_FIVE_LABEL = "ColumnFiveLabel";
	public static String COLUMN_SIX_LABEL = "ColumnSixLabel";
	public static String COLUMN_SEVEN_LABEL = "ColumnSevenLabel";
	public static String COLUMN_EIGHT_LABEL = "ColumnEightLabel";
	public static String COLUMN_NINE_LABEL = "ColumnNineLabel";
	
	public Control createExampleControl(Composite parent) {
		//property names of the NumberValues class
		String[] propertyNames = {"columnOneNumber", "columnTwoNumber", "columnThreeNumber", "columnFourNumber", 
				"columnFiveNumber"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("columnOneNumber", "Column 1");
		propertyToLabelMap.put("columnTwoNumber", "Column 2");
		propertyToLabelMap.put("columnThreeNumber", "Column 3");
		propertyToLabelMap.put("columnFourNumber", "Column 4");
		propertyToLabelMap.put("columnFiveNumber", "Column 5");

		DefaultGridLayer gridLayer = new DefaultGridLayer(createNumberValuesList(), propertyNames, propertyToLabelMap);
		DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();

		final ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
		registerColumnLabels(columnLabelAccumulator);
		
		NatTable natTable = new NatTable(parent, gridLayer, false);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new TableEditConfiguration());
		natTable.configure();

		return natTable;
	}
	
	
	private List<NumberValues> createNumberValuesList() {
		List<NumberValues> result = new ArrayList<NumberValues>();
		
		NumberValues nv = null;
		for (int i = 0; i < 10; i++) {
			nv = new NumberValues();
			nv.setColumnOneNumber(11111);
			nv.setColumnTwoNumber(22222);
			nv.setColumnThreeNumber(33333);
			nv.setColumnFourNumber(44444);
			nv.setColumnFiveNumber(55555);
			nv.setColumnSixNumber(66666);
			nv.setColumnSevenNumber(77777);
			nv.setColumnEightNumber(88888);
			nv.setColumnNineNumber(99999);
			result.add(nv);
		}
		
		return result;
	}

	
	private void registerColumnLabels(ColumnOverrideLabelAccumulator columnLabelAccumulator) {
		columnLabelAccumulator.registerColumnOverrides(0, COLUMN_ONE_LABEL);
		columnLabelAccumulator.registerColumnOverrides(1, COLUMN_TWO_LABEL);
		columnLabelAccumulator.registerColumnOverrides(2, COLUMN_THREE_LABEL);
		columnLabelAccumulator.registerColumnOverrides(3, COLUMN_FOUR_LABEL);
		columnLabelAccumulator.registerColumnOverrides(4, COLUMN_FIVE_LABEL);
	}
}


class TableEditConfiguration extends AbstractRegistryConfiguration  {

	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE);

		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultIntegerDisplayConverter(), DisplayMode.NORMAL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultIntegerDisplayConverter(), DisplayMode.EDIT);
		
		configRegistry.registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, getExampleValidator(), DisplayMode.EDIT);
		configRegistry.registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, getExceptionValidator(), DisplayMode.EDIT, _446_EditErrorHandlingExample.COLUMN_FIVE_LABEL);
		
		registerErrorHandlingStrategies(configRegistry);
		registerErrorHandlingStyles(configRegistry);
	}

	private void registerErrorHandlingStrategies(IConfigRegistry configRegistry) {
		
		configRegistry.registerConfigAttribute(EditConfigAttributes.CONVERSION_ERROR_HANDLER, 
				new DialogErrorHandling(), DisplayMode.EDIT, _446_EditErrorHandlingExample.COLUMN_TWO_LABEL);
		
		configRegistry.registerConfigAttribute(EditConfigAttributes.VALIDATION_ERROR_HANDLER, 
				new DialogErrorHandling(), DisplayMode.EDIT, _446_EditErrorHandlingExample.COLUMN_THREE_LABEL);

		configRegistry.registerConfigAttribute(EditConfigAttributes.CONVERSION_ERROR_HANDLER, 
				new DialogErrorHandling(), DisplayMode.EDIT, _446_EditErrorHandlingExample.COLUMN_FOUR_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.VALIDATION_ERROR_HANDLER, 
				new DialogErrorHandling(), DisplayMode.EDIT, _446_EditErrorHandlingExample.COLUMN_FOUR_LABEL);

	}
	
	private void registerErrorHandlingStyles(IConfigRegistry configRegistry) {
		//this is needed to support different styling on just in time conversion/validation
		//error rendering in a text editor
		Style conversionErrorStyle = new Style();
		conversionErrorStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_RED);
		conversionErrorStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_WHITE);
		
		configRegistry.registerConfigAttribute(EditConfigAttributes.CONVERSION_ERROR_STYLE, 
				conversionErrorStyle, DisplayMode.EDIT, _446_EditErrorHandlingExample.COLUMN_TWO_LABEL);

		Style validationErrorStyle = new Style();
		validationErrorStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_BLACK);
		validationErrorStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_WHITE);
		
		configRegistry.registerConfigAttribute(EditConfigAttributes.VALIDATION_ERROR_STYLE, 
				validationErrorStyle, DisplayMode.EDIT, _446_EditErrorHandlingExample.COLUMN_TWO_LABEL);
	}
	
	private IDataValidator getExampleValidator() {
		return new DataValidator() {
			
			public boolean validate(int columnIndex, int rowIndex, Object newValue) {
				if (newValue instanceof Integer && ((Integer)newValue).intValue() > 10000) {
					return true;
				}
				else {
					throw new ValidationFailedException("The value has to be bigger than 10000");
				}
			}
		};
	}

	private IDataValidator getExceptionValidator() {
		return new DataValidator() {
			
			public boolean validate(int columnIndex, int rowIndex, Object newValue) {
				throw new IllegalArgumentException("This is an exception throwed because of missing constraint checks!");
			}
		};
	}
}

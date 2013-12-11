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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.ExtendedPersonWithAddress;
import org.eclipse.nebula.widgets.nattable.examples.data.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.GradientBackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.PasswordTextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TableCellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CustomLineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.tooltip.NatTableContentTooltip;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;


public class _4221_CellPainterExample extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(1024, 400, new _4221_CellPainterExample());
	}
	
	@Override
	public String getDescription() {
		return "Example showing the different painters that come with NatTable";
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
	public static String COLUMN_TEN_LABEL = "ColumnTenLabel";
	public static String COLUMN_ELEVEN_LABEL = "ColumnElevenLabel";
	public static String COLUMN_TWELVE_LABEL = "ColumnTwelveLabel";
	
	private NatTable natTable;
	
	@Override
	public Control createExampleControl(Composite parent) {
		//property names of the Person class
		String[] propertyNames = {"firstName", "lastName", "password", "description", "age", "money",
				"married", "gender", "address.street", "address.city", "favouriteFood", "favouriteDrinks"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("firstName", "Firstname");
		propertyToLabelMap.put("lastName", "Lastname");
		propertyToLabelMap.put("password", "Password");
		propertyToLabelMap.put("description", "Description");
		propertyToLabelMap.put("age", "Age");
		propertyToLabelMap.put("money", "Money");
		propertyToLabelMap.put("married", "Married");
		propertyToLabelMap.put("gender", "Gender");
		propertyToLabelMap.put("address.street", "Street");
		propertyToLabelMap.put("address.city", "City");
		propertyToLabelMap.put("favouriteFood", "Food");
		propertyToLabelMap.put("favouriteDrinks", "Drinks");
		
		IDataProvider bodyDataProvider = new ListDataProvider<ExtendedPersonWithAddress>(
				PersonService.getExtendedPersonsWithAddress(10), 
				new ExtendedReflectiveColumnPropertyAccessor<ExtendedPersonWithAddress>(propertyNames));
		
		DefaultGridLayer gridLayer = new DefaultGridLayer(
				bodyDataProvider, new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap));
		
		final DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();

		final ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
		registerColumnLabels(columnLabelAccumulator);
		
		natTable = new NatTable(parent, gridLayer, false);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new PainterConfiguration());
		natTable.configure();
		
		new NatTableContentTooltip(natTable, GridRegion.BODY);
		
		return natTable;
	}
	
	
	private void registerColumnLabels(ColumnOverrideLabelAccumulator columnLabelAccumulator) {
		columnLabelAccumulator.registerColumnOverrides(0, COLUMN_ONE_LABEL);
		columnLabelAccumulator.registerColumnOverrides(1, COLUMN_TWO_LABEL);
		columnLabelAccumulator.registerColumnOverrides(2, COLUMN_THREE_LABEL);
		columnLabelAccumulator.registerColumnOverrides(3, COLUMN_FOUR_LABEL);
		columnLabelAccumulator.registerColumnOverrides(4, COLUMN_FIVE_LABEL);
		columnLabelAccumulator.registerColumnOverrides(5, COLUMN_SIX_LABEL);
		//add this label so the CustomLineBorderDecorator knows where to render the additional border
		columnLabelAccumulator.registerColumnOverrides(5, CustomLineBorderDecorator.RIGHT_LINE_BORDER_LABEL);
		columnLabelAccumulator.registerColumnOverrides(6, COLUMN_SEVEN_LABEL);
		columnLabelAccumulator.registerColumnOverrides(7, COLUMN_EIGHT_LABEL);
		columnLabelAccumulator.registerColumnOverrides(8, COLUMN_NINE_LABEL);
		columnLabelAccumulator.registerColumnOverrides(9, COLUMN_TEN_LABEL);
		columnLabelAccumulator.registerColumnOverrides(10, COLUMN_ELEVEN_LABEL);
		columnLabelAccumulator.registerColumnOverrides(11, COLUMN_TWELVE_LABEL);
	}
	
	
	class PainterConfiguration extends AbstractRegistryConfiguration  {
		
		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			registerPainters(configRegistry);
		}
		
		private void registerPainters(IConfigRegistry configRegistry) {
			//override column header style configuration to make use of the BackgroundImagePainter
			registerColumnHeaderStyle(configRegistry);
			//column one -> default text with no border
			//column two -> default text showing a border
			registerColumnTwoTextPainterStyle(configRegistry);
			//column three -> password painter with gradient background
			registerColumnThreePasswordPainter(configRegistry);
			registerColumnFourPainter(configRegistry);
			registerColumnFivePainter(configRegistry);
			registerColumnSixDoublePainter(configRegistry);
			registerColumnSevenCheckboxPainter(configRegistry);
			registerColumnEightCheckboxPainter(configRegistry);
			registerColumnNineComboBox(configRegistry);
			registerColumnTenComboBoxPainter(configRegistry);
			registerColumnElevenTablePainter(configRegistry);
			registerColumnTwelveComboBox(configRegistry);
		}
		
		private void registerColumnHeaderStyle(IConfigRegistry configRegistry) {
			
			Image bgImage = new Image(
					Display.getDefault(), 
					getClass().getResourceAsStream("../resources/column_header_bg.png"));
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

			ICellPainter selectedHeaderPainter = 
					new BackgroundImagePainter(txtPainter, selectedBgImage, GUIHelper.getColor(192, 192, 192));

			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_PAINTER, 
					selectedHeaderPainter, 
					DisplayMode.SELECT, 
					GridRegion.COLUMN_HEADER);
		}
		
		private void registerColumnTwoTextPainterStyle(IConfigRegistry configRegistry) {
			Style style = new Style();
			style.setAttributeValue(CellStyleAttributes.BORDER_STYLE, new BorderStyle(2, GUIHelper.COLOR_BLUE, LineStyleEnum.DASHDOT));
			
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_STYLE, 
					style, 
					DisplayMode.NORMAL, 
					_4221_CellPainterExample.COLUMN_TWO_LABEL);
		}
		
		private void registerColumnThreePasswordPainter(IConfigRegistry configRegistry) {
			Style style = new Style();
			style.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, GUIHelper.COLOR_WHITE);
			style.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, GUIHelper.COLOR_RED);
			
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_STYLE, 
					style, 
					DisplayMode.NORMAL, 
					_4221_CellPainterExample.COLUMN_THREE_LABEL);
			
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_PAINTER, 
					new GradientBackgroundPainter(new PasswordTextPainter(false, false)), 
					DisplayMode.NORMAL, 
					_4221_CellPainterExample.COLUMN_THREE_LABEL);
		}
		
		private void registerColumnFourPainter(IConfigRegistry configRegistry) {
			Style style = new Style();
			style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_STYLE, 
					style,
					DisplayMode.NORMAL,
					_4221_CellPainterExample.COLUMN_FOUR_LABEL);
			
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_PAINTER, 
					new GradientBackgroundPainter(new TextPainter(false, false, false ,true), true), 
					DisplayMode.NORMAL, 
					_4221_CellPainterExample.COLUMN_FOUR_LABEL);
		}
		
		private void registerColumnFivePainter(IConfigRegistry configRegistry) {
			Style style = new Style();
			style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_STYLE, 
					style,
					DisplayMode.NORMAL,
					_4221_CellPainterExample.COLUMN_FIVE_LABEL);
			
			//don't forget to register the Integer converter!
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.DISPLAY_CONVERTER, 
					new DefaultIntegerDisplayConverter(), 
					DisplayMode.NORMAL,
					_4221_CellPainterExample.COLUMN_FIVE_LABEL);
		}
		
		private void registerColumnSixDoublePainter(IConfigRegistry configRegistry) {
			Style style = new Style();
			style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_STYLE, 
					style,
					DisplayMode.NORMAL,
					_4221_CellPainterExample.COLUMN_SIX_LABEL);
			
			//the CustomLineBorderDecorator needs an additional border label to know where to render a border
			//within the cell
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_PAINTER, 
					new CustomLineBorderDecorator(
							new PaddingDecorator(new TextPainter(), 0, 5, 0, 0),
							new BorderStyle(2, GUIHelper.COLOR_GREEN, LineStyleEnum.SOLID)), 
					DisplayMode.NORMAL, 
					_4221_CellPainterExample.COLUMN_SIX_LABEL);
			
			//don't forget to register the Double converter!
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.DISPLAY_CONVERTER, 
					new DefaultDoubleDisplayConverter(), 
					DisplayMode.NORMAL,
					_4221_CellPainterExample.COLUMN_SIX_LABEL);
		}
		
		private void registerColumnSevenCheckboxPainter(IConfigRegistry configRegistry) {
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_PAINTER, 
					new CheckBoxPainter(), 
					DisplayMode.NORMAL, 
					_4221_CellPainterExample.COLUMN_SEVEN_LABEL);
			
			//using a CheckBoxCellEditor also needs a Boolean conversion to work correctly
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.DISPLAY_CONVERTER, 
					new DefaultBooleanDisplayConverter(), 
					DisplayMode.NORMAL, 
					_4221_CellPainterExample.COLUMN_SEVEN_LABEL);
		}
		
		private void registerColumnEightCheckboxPainter(IConfigRegistry configRegistry) {
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_PAINTER, 
					new CheckBoxPainter(GUIHelper.getImage("arrow_up"), GUIHelper.getImage("arrow_down")), 
					DisplayMode.NORMAL, 
					_4221_CellPainterExample.COLUMN_EIGHT_LABEL);
			
			//using a CheckBoxCellEditor also needs a Boolean conversion to work correctly
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.DISPLAY_CONVERTER, 
					getGenderBooleanConverter(), 
					DisplayMode.NORMAL, 
					_4221_CellPainterExample.COLUMN_EIGHT_LABEL);
		}
		
		private void registerColumnNineComboBox(IConfigRegistry configRegistry) {
		}
		
		private void registerColumnTenComboBoxPainter(IConfigRegistry configRegistry) {
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_PAINTER, 
					new ComboBoxPainter(), 
					DisplayMode.NORMAL, 
					_4221_CellPainterExample.COLUMN_TEN_LABEL);
		}
		
		private void registerColumnElevenTablePainter(IConfigRegistry configRegistry) {
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_PAINTER, 
					new TableCellPainter(), 
					DisplayMode.NORMAL, 
					_4221_CellPainterExample.COLUMN_ELEVEN_LABEL);
			
			//uncomment this to get an idea on how the TableCellEditor works
//			configRegistry.registerConfigAttribute(
//					EditConfigAttributes.CELL_EDITABLE_RULE, 
//					IEditableRule.ALWAYS_EDITABLE, 
//					DisplayMode.EDIT, 
//					_4221_CellPainterExample.COLUMN_ELEVEN_LABEL);
//
//			configRegistry.registerConfigAttribute(
//					EditConfigAttributes.CELL_EDITOR, 
//					new TableCellEditor(), 
//					DisplayMode.NORMAL, 
//					_4221_CellPainterExample.COLUMN_ELEVEN_LABEL);
		}
		
		private void registerColumnTwelveComboBox(IConfigRegistry configRegistry) {
			//TODO add painter for list 
		}
		
		
		/**
		 * @return Returns a simple converter for the gender of a Person.
		 * 			{@link Gender#MALE} will be interpreted as <code>true</code>
		 * 			while {@link Gender#FEMALE} will be interpreted as <code>false</code>
		 */
		private IDisplayConverter getGenderBooleanConverter() {
			return new DisplayConverter() {
				
				@Override
				public Object canonicalToDisplayValue(Object canonicalValue) {
					if (canonicalValue instanceof Gender) {
						return ((Gender)canonicalValue) == Gender.MALE;
					}
					return null;
				}
				
				@Override
				public Object displayToCanonicalValue(Object displayValue) {
					Boolean displayBoolean = Boolean.valueOf(displayValue.toString());
					return displayBoolean ? Gender.MALE : Gender.FEMALE;
				}
				
			};
		}
	}
}



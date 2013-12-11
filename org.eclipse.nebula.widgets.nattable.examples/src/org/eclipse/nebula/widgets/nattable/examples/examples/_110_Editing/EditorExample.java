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
package org.eclipse.nebula.widgets.nattable.examples.examples._110_Editing;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.MultiLineTextCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.PasswordCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.gui.CellEditDialog;
import org.eclipse.nebula.widgets.nattable.edit.gui.FileDialogCellEditor;
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
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.PasswordTextPainter;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.tickupdate.TickUpdateConfigAttributes;
import org.eclipse.nebula.widgets.nattable.tooltip.NatTableContentTooltip;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;


public class EditorExample extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(1024, 400, new EditorExample());
	}
	
	@Override
	public String getDescription() {
		return
				"";
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
	public static String COLUMN_THIRTEEN_LABEL = "ColumnThirteenLabel";
	
	@Override
	public Control createExampleControl(Composite parent) {
		//property names of the Person class
		String[] propertyNames = {"firstName", "lastName", "password", "description", "age", "money",
				"married", "gender", "address.street", "address.city", "favouriteFood", "favouriteDrinks", 
				"filename"};

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
		propertyToLabelMap.put("filename", "Filename");
		
		IDataProvider bodyDataProvider = new ListDataProvider<ExtendedPersonWithAddress>(
				PersonService.getExtendedPersonsWithAddress(10), 
				new ExtendedReflectiveColumnPropertyAccessor<ExtendedPersonWithAddress>(propertyNames));
		
		DefaultGridLayer gridLayer = new DefaultGridLayer(
				bodyDataProvider, new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap));
		
		final DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();

		final ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
		registerColumnLabels(columnLabelAccumulator);
		
		final NatTable natTable = new NatTable(parent, gridLayer, false);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new EditorConfiguration());
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
		columnLabelAccumulator.registerColumnOverrides(6, COLUMN_SEVEN_LABEL);
		columnLabelAccumulator.registerColumnOverrides(7, COLUMN_EIGHT_LABEL);
		columnLabelAccumulator.registerColumnOverrides(8, COLUMN_NINE_LABEL);
		columnLabelAccumulator.registerColumnOverrides(9, COLUMN_TEN_LABEL);
		columnLabelAccumulator.registerColumnOverrides(10, COLUMN_ELEVEN_LABEL);
		columnLabelAccumulator.registerColumnOverrides(11, COLUMN_TWELVE_LABEL);
		columnLabelAccumulator.registerColumnOverrides(12, COLUMN_THIRTEEN_LABEL);
	}
}


class EditorConfiguration extends AbstractRegistryConfiguration  {

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITABLE_RULE, 
				IEditableRule.ALWAYS_EDITABLE);
		
		registerEditors(configRegistry);
	}

	private void registerEditors(IConfigRegistry configRegistry) {
		registerColumnTwoTextEditor(configRegistry);
		registerColumnThreePasswordEditor(configRegistry);
		registerColumnFourMultiLineEditor(configRegistry);
		registerColumnFiveIntegerEditor(configRegistry);
		registerColumnSixDoubleEditor(configRegistry);
		registerColumnSevenCheckbox(configRegistry);
		registerColumnEightCheckbox(configRegistry);
		registerColumnNineComboBox(configRegistry);
		registerColumnTenComboBox(configRegistry);
		registerColumnElevenComboBox(configRegistry);
		registerColumnTwelveComboBox(configRegistry);
		registerColumnThirteenFileDialogEditor(configRegistry);
	}

	private void registerColumnTwoTextEditor(IConfigRegistry configRegistry) {
		//register a TextCellEditor for column two that commits on key up/down
		//moves the selection after commit by enter
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITOR, 
				new TextCellEditor(true, true), 
				DisplayMode.NORMAL, 
				EditorExample.COLUMN_TWO_LABEL);
		
		//configure to open the adjacent editor after commit
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.OPEN_ADJACENT_EDITOR,
				Boolean.TRUE,
				DisplayMode.EDIT,
				EditorExample.COLUMN_TWO_LABEL);
		
		//configure a custom message for the multi edit dialog
		Map<String, Object> editDialogSettings = new HashMap<String, Object>();
		editDialogSettings.put(CellEditDialog.DIALOG_MESSAGE, "Please specify the lastname in here:");
		
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.EDIT_DIALOG_SETTINGS, 
				editDialogSettings,
				DisplayMode.EDIT,
				EditorExample.COLUMN_TWO_LABEL);
	}
	
	private void registerColumnThreePasswordEditor(IConfigRegistry configRegistry) {
		//register a PasswordCellEditor for column three
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITOR, 
				new PasswordCellEditor(), 
				DisplayMode.NORMAL, 
				EditorExample.COLUMN_THREE_LABEL);
		
		//configure the password editor to not support multi editing
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.SUPPORT_MULTI_EDIT, 
				Boolean.FALSE, 
				DisplayMode.EDIT, 
				EditorExample.COLUMN_THREE_LABEL);

		//note that you should also register the corresponding PasswordTextPainter
		//to ensure that the password is not rendered in clear text
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER, 
				new PasswordTextPainter(), 
				DisplayMode.NORMAL, 
				EditorExample.COLUMN_THREE_LABEL);
	}
	
	private void registerColumnFourMultiLineEditor(IConfigRegistry configRegistry) {
		//configure the multi line text editor for column four
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITOR, 
				new MultiLineTextCellEditor(false), 
				DisplayMode.NORMAL, 
				EditorExample.COLUMN_FOUR_LABEL);
		
		//configure the multi line text editor to always open in a subdialog
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.OPEN_IN_DIALOG,
				Boolean.TRUE,
				DisplayMode.EDIT,
				EditorExample.COLUMN_FOUR_LABEL);
		
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_STYLE, 
				cellStyle,
				DisplayMode.NORMAL,
				EditorExample.COLUMN_FOUR_LABEL);
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_STYLE, 
				cellStyle,
				DisplayMode.EDIT,
				EditorExample.COLUMN_FOUR_LABEL);
		
		//configure custom dialog settings
		Display display = Display.getCurrent();
		Map<String, Object> editDialogSettings = new HashMap<String, Object>();
		editDialogSettings.put(CellEditDialog.DIALOG_SHELL_TITLE, "My custom value");
		editDialogSettings.put(CellEditDialog.DIALOG_SHELL_ICON, display.getSystemImage(SWT.ICON_WARNING));
		editDialogSettings.put(CellEditDialog.DIALOG_SHELL_RESIZABLE, Boolean.TRUE);
		
		Point size = new Point(400, 300);
		editDialogSettings.put(CellEditDialog.DIALOG_SHELL_SIZE, size);
		
		int screenWidth = display.getBounds().width;
		int screenHeight = display.getBounds().height;
		Point location = new Point((screenWidth / (2 * display.getMonitors().length)) - (size.x/2), (screenHeight / 2) - (size.y/2));
		editDialogSettings.put(CellEditDialog.DIALOG_SHELL_LOCATION, location);

		//add custum message 
		editDialogSettings.put(CellEditDialog.DIALOG_MESSAGE, "Enter some free text in here:");
		
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.EDIT_DIALOG_SETTINGS, 
				editDialogSettings,
				DisplayMode.EDIT,
				EditorExample.COLUMN_FOUR_LABEL);
	}
	
	private void registerColumnFiveIntegerEditor(IConfigRegistry configRegistry) {
		//register a TextCellEditor for column five that moves the selection after commit
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITOR, 
				new TextCellEditor(false, true), 
				DisplayMode.NORMAL, 
				EditorExample.COLUMN_FIVE_LABEL);
		
		//configure to open the adjacent editor after commit
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.OPEN_ADJACENT_EDITOR,
				Boolean.TRUE,
				DisplayMode.EDIT,
				EditorExample.COLUMN_FIVE_LABEL);
		
		//configure to open always in dialog to show the tick update in normal mode
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.OPEN_IN_DIALOG,
				Boolean.TRUE,
				DisplayMode.EDIT,
				EditorExample.COLUMN_FIVE_LABEL);
		
		//don't forget to register the Integer converter!
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, 
				new DefaultIntegerDisplayConverter(), 
				DisplayMode.NORMAL,
				EditorExample.COLUMN_FIVE_LABEL);
	}
	
	private void registerColumnSixDoubleEditor(IConfigRegistry configRegistry) {
		//register a TextCellEditor for column five that moves the selection after commit
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITOR, 
				new TextCellEditor(false, true), 
				DisplayMode.NORMAL, 
				EditorExample.COLUMN_SIX_LABEL);
		
		//configure to open the adjacent editor after commit
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.OPEN_ADJACENT_EDITOR,
				Boolean.TRUE,
				DisplayMode.EDIT,
				EditorExample.COLUMN_SIX_LABEL);
		
		//configure to open always in dialog to show the tick update in normal mode
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.OPEN_IN_DIALOG,
				Boolean.TRUE,
				DisplayMode.EDIT,
				EditorExample.COLUMN_SIX_LABEL);
		
		//configure the tick update dialog to use the adjust mode
		configRegistry.registerConfigAttribute(
				TickUpdateConfigAttributes.USE_ADJUST_BY,
				Boolean.TRUE,
				DisplayMode.EDIT,
				EditorExample.COLUMN_SIX_LABEL);
		
		//don't forget to register the Double converter!
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, 
				new DefaultDoubleDisplayConverter(), 
				DisplayMode.NORMAL,
				EditorExample.COLUMN_SIX_LABEL);
	}
	
	/**
	 * The following will register a default CheckBoxCellEditor for the column that
	 * carries the married information.
	 * <p>
	 * To register a CheckBoxCellEditor, you need to 
	 * <ol>
	 * <li>Register the editor</li>
	 * <li>Register the painter corresponding to that editor</li>
	 * <li>Register the needed converter</li>
	 * </ol>
	 * @param configRegistry
	 */
	private void registerColumnSevenCheckbox(IConfigRegistry configRegistry) {
		//register a CheckBoxCellEditor for column three
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITOR, 
				new CheckBoxCellEditor(), 
				DisplayMode.EDIT, 
				EditorExample.COLUMN_SEVEN_LABEL);
		
		//if you want to use the CheckBoxCellEditor, you should also consider
		//using the corresponding CheckBoxPainter to show the content like a
		//checkbox in your NatTable
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER, 
				new CheckBoxPainter(), 
				DisplayMode.NORMAL, 
				EditorExample.COLUMN_SEVEN_LABEL);
		
		//using a CheckBoxCellEditor also needs a Boolean conversion to work correctly
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, 
				new DefaultBooleanDisplayConverter(), 
				DisplayMode.NORMAL, 
				EditorExample.COLUMN_SEVEN_LABEL);
	}
	
	/**
	 * The following will register a CheckBoxCellEditor with custom icons for the column that
	 * carries the gender information. As a Gender is not a Boolean, there need to be a special
	 * converter registered. Note that such a converter needs to create a Boolean display value
	 * and create the canonical value out of a Boolean value again.
	 * <p>
	 * To register a CheckBoxCellEditor, you need to 
	 * <ol>
	 * <li>Register the editor</li>
	 * <li>Register the painter corresponding to that editor</li>
	 * <li>Register the needed converter</li>
	 * </ol>
	 * @param configRegistry
	 */
	private void registerColumnEightCheckbox(IConfigRegistry configRegistry) {
		//register a CheckBoxCellEditor for column four
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITOR, 
				new CheckBoxCellEditor(), 
				DisplayMode.EDIT, 
				EditorExample.COLUMN_EIGHT_LABEL);
		
		//if you want to use the CheckBoxCellEditor, you should also consider
		//using the corresponding CheckBoxPainter to show the content like a
		//checkbox in your NatTable
		//in this case we use different icons to show how this works
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER, 
				new CheckBoxPainter(GUIHelper.getImage("arrow_up"), GUIHelper.getImage("arrow_down")), 
				DisplayMode.NORMAL, 
				EditorExample.COLUMN_EIGHT_LABEL);
		
		//using a CheckBoxCellEditor also needs a Boolean conversion to work correctly
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, 
				getGenderBooleanConverter(), 
				DisplayMode.NORMAL, 
				EditorExample.COLUMN_EIGHT_LABEL);
	}
	
	/**
	 * The following will register a ComboBoxCellEditor for the column that
	 * carries the street information.
	 * <p>
	 * To register a ComboBoxCellEditor, you only need to register the editor itself.
	 * On click to the cell you want to edit, the dropdown will open.
	 * <p>
	 * If you want to indicate in the view that this cell is editable by combobox,
	 * you need to register the corresponding painter.
	 * 
	 * @param configRegistry
	 */
	private void registerColumnNineComboBox(IConfigRegistry configRegistry) {
		//register a combobox editor for the street names
		ComboBoxCellEditor comboBoxCellEditor = new ComboBoxCellEditor(Arrays.asList(PersonService.getStreetNames()));
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITOR, 
				comboBoxCellEditor, 
				DisplayMode.EDIT, 
				EditorExample.COLUMN_NINE_LABEL);
	}
	
	/**
	 * The following will register a ComboBoxCellEditor for the column that
	 * carries the city information. The difference to the editor in column six is 
	 * that the text control of the combobox is editable and the combobox 
	 * shows all entries instead of a scrollbar.
	 * 
	 * @param configRegistry
	 */
	private void registerColumnTenComboBox(IConfigRegistry configRegistry) {
		//register a combobox for the city names
		ComboBoxCellEditor comboBoxCellEditor = new ComboBoxCellEditor(Arrays.asList(PersonService.getCityNames()), -1);
		comboBoxCellEditor.setFreeEdit(true);
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITOR, 
				comboBoxCellEditor, 
				DisplayMode.EDIT, 
				EditorExample.COLUMN_TEN_LABEL);
		
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER, 
				new ComboBoxPainter(), 
				DisplayMode.NORMAL, 
				EditorExample.COLUMN_TEN_LABEL);
	}
	
	/**
	 * The following will register a ComboBoxCellEditor for the column that
	 * carries the favourite food information. This ComboBoxCellEditor will
	 * support multiple selection. It also adds a different icon for the combo
	 * in edit mode.
	 * 
	 * @param configRegistry
	 */
	private void registerColumnElevenComboBox(IConfigRegistry configRegistry) {
		//register a combobox for the city names
		ComboBoxCellEditor comboBoxCellEditor = new ComboBoxCellEditor(Arrays.asList(PersonService.getFoodList()), -1);
		comboBoxCellEditor.setMultiselect(true);
		comboBoxCellEditor.setUseCheckbox(true);
		
		//change the multi selection brackets that are added to the String that is shown in the editor
		comboBoxCellEditor.setMultiselectTextBracket("", "");
		//register a special converter that removes the brackets in case the returned value is a Collection
		//this is necessary because editing and displaying are not directly coupled to each other
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, 
				new DefaultDisplayConverter() {
					
					@Override
					public Object canonicalToDisplayValue(Object canonicalValue) {
						if (canonicalValue instanceof Collection) {
							//Collection.toString() will add [ and ] around the values in the Collection
							//So by removing the leading and ending character, we remove the brackets
							String result = canonicalValue.toString();
							result = result.substring(1, result.length()-1);
							return result;
						}
						//if the value is not a Collection we simply let the super class do the conversion
						//this is necessary to show single values in the ComboBox correctly
						return super.canonicalToDisplayValue(canonicalValue);
					}
				}, 
				DisplayMode.NORMAL, 
				EditorExample.COLUMN_ELEVEN_LABEL);
		
		comboBoxCellEditor.setIconImage(GUIHelper.getImage("plus"));
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITOR, 
				comboBoxCellEditor, 
				DisplayMode.EDIT, 
				EditorExample.COLUMN_ELEVEN_LABEL);
	}
	
	/**
	 * The following will register a ComboBoxCellEditor for the column that
	 * carries the favourite drinks information. The difference to the editor in column eight is 
	 * that the text control of the combobox is editable and the combobox 
	 * shows all entries instead of a scrollbar. It also uses a different icon for rendering
	 * the combo in normal mode.
	 * 
	 * @param configRegistry
	 */
	private void registerColumnTwelveComboBox(IConfigRegistry configRegistry) {
		//register a combobox for the city names
		ComboBoxCellEditor comboBoxCellEditor = new ComboBoxCellEditor(Arrays.asList(PersonService.getDrinkList()), -1);
		comboBoxCellEditor.setFreeEdit(true);
		comboBoxCellEditor.setMultiselect(true);
		comboBoxCellEditor.setIconImage(GUIHelper.getImage("plus"));
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITOR, 
				comboBoxCellEditor, 
				DisplayMode.EDIT, 
				EditorExample.COLUMN_TWELVE_LABEL);
		
		
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER, 
				new ComboBoxPainter(GUIHelper.getImage("plus")), 
				DisplayMode.NORMAL, 
				EditorExample.COLUMN_TWELVE_LABEL);
	}
	
	/**
	 * The following will register a {@link FileDialogCellEditor} for the filename column.
	 * This will open the default SWT FileDialog to support file selection.
	 * 
	 * @param configRegistry
	 */
	private void registerColumnThirteenFileDialogEditor(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITOR, 
				new FileDialogCellEditor(), 
				DisplayMode.EDIT, 
				EditorExample.COLUMN_THIRTEEN_LABEL);
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



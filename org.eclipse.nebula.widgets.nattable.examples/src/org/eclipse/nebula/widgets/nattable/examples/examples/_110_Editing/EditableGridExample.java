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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.EditableRule;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.validate.DataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.DefaultNumericDataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.action.ToggleCheckBoxColumnAction;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ColumnHeaderCheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.PricingTypeBean;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class EditableGridExample extends AbstractNatExample {

	public static void main(String[] args) {
		StandaloneNatExampleRunner.run(new EditableGridExample());
	}

	@Override
	public String getDescription() {
		return
				"This example demonstrates various edit and styling functionality. All of the functionality in the DefaultGridExample " +
				"is available here. Also note that:\n" +
				"\n" +
				"* CUSTOM ALIGNMENT: All numeric fields are right-aligned, and the Security Description field is left-aligned.\n" +
				"* CUSTOM FORMATTING: The Bid and Ask fields are formatted to 2 decimal places normally, but in edit mode they are " +
				"formatted to 6 decimal places.\n" +
				"* EDITABLE/NON-EDITABLE FIELDS: Spread is a non-editable calculated field that is formatted to 6 decimal places.\n" +
				"* SINGLE AND MULTI-CELL EDIT for text, checkbox, and combo box fields. Note that in order to invoke multi-cell " +
				"edit, all of the selected cells must have the same editor type and be editable.\n" +
				"* VALIDATION: The ISIN field has validation enabled which requires data to be in the form ABC123 (first three alpha " +
				"characters, afterwards numeric). If the validation rule is violated, the characters are highlighted in red and the " +
				"editor will not allow you to enter the value. Also note that if an ISIN field and a different text field are " +
				"selected for multi-cell edit, the multi-cell editor will require a value that is valid for all selected cells.";
	}
	
	public static final String ASK_PRICE_CONFIG_LABEL = "askPriceConfigLabel";
	public static final String SECURITY_ID_CONFIG_LABEL = "SecurityIdConfigLabel";
	public static final String SECURITY_ID_EDITOR = "SecurityIdEditor";
	public static final String BID_PRICE_CONFIG_LABEL = "bidPriceConfigLabel";
	public static final String LOT_SIZE_CONFIG_LABEL = "lotSizeConfigLabel";
	public static final String SPREAD_CONFIG_LABEL = "spreadConfigLabel";

	public static final String FORMAT_DATE_CONFIG_LABEL = "formatDateConfigLabel";
	public static final String FORMAT_DOUBLE_2_PLACES_CONFIG_LABEL = "formatDouble2PlacesConfigLabel";
	public static final String FORMAT_DOUBLE_6_PLACES_CONFIG_LABEL = "formatDouble6PlacesConfigLabel";
	public static final String FORMAT_IN_MILLIONS_CONFIG_LABEL = "formatInMilliosConfigLabel";
	public static final String FORMAT_PRICING_TYPE_CONFIG_LABEL = "formatPricingTypeConfigLabel";

	public static final String ALIGN_CELL_CONTENTS_LEFT_CONFIG_LABEL = "alignCellContentsLeftConfigLabel";
	public static final String ALIGN_CELL_CONTENTS_RIGHT_CONFIG_LABEL = "alignCellContentsRightConfigLabel";

	public static final String CHECK_BOX_CONFIG_LABEL = "checkBox";
	public static final String CHECK_BOX_EDITOR_CONFIG_LABEL = "checkBoxEditor";
	public static final String COMBO_BOX_CONFIG_LABEL = "comboBox";
	public static final String COMBO_BOX_EDITOR_CONFIG_LABEL = "comboBoxEditor";

	public Control createExampleControl(Composite parent) {
		DefaultGridLayer gridLayer = new DefaultGridLayer(RowDataListFixture.getList(), RowDataListFixture.getPropertyNames(), RowDataListFixture.getPropertyToLabelMap());
		
		DataLayer columnHeaderDataLayer = (DataLayer) gridLayer.getColumnHeaderDataLayer();
		columnHeaderDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
		
		final DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();
		IDataProvider dataProvider = bodyDataLayer.getDataProvider();

		// NOTE: Register the accumulator on the body data layer.
		// This ensures that the labels are bound to the column index and are unaffected by column order.
		final ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);

		NatTable natTable = new NatTable(parent, gridLayer, false);

		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
		natTable.addConfiguration(editableGridConfiguration(columnLabelAccumulator, dataProvider));

		final ColumnHeaderCheckBoxPainter columnHeaderCheckBoxPainter = new ColumnHeaderCheckBoxPainter(bodyDataLayer);
		final ICellPainter column9HeaderPainter = new BeveledBorderDecorator(new CellPainterDecorator(new TextPainter(), CellEdgeEnum.RIGHT, columnHeaderCheckBoxPainter));
		natTable.addConfiguration(new AbstractRegistryConfiguration() {
			public void configureRegistry(IConfigRegistry configRegistry) {
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
						column9HeaderPainter,
						DisplayMode.NORMAL,
						ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 9);
			}
			
			@Override
			public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
				uiBindingRegistry.registerFirstSingleClickBinding(
						new CellPainterMouseEventMatcher(GridRegion.COLUMN_HEADER, MouseEventMatcher.LEFT_BUTTON, columnHeaderCheckBoxPainter),
						new ToggleCheckBoxColumnAction(columnHeaderCheckBoxPainter, bodyDataLayer)
				);
			}
		});
		
		natTable.configure();

		return natTable;
	}

	public static AbstractRegistryConfiguration editableGridConfiguration(
						final ColumnOverrideLabelAccumulator columnLabelAccumulator,
						final IDataProvider dataProvider) {

		return new AbstractRegistryConfiguration() {

			public void configureRegistry(IConfigRegistry configRegistry) {

				EditableGridExample.registerConfigLabelsOnColumns(columnLabelAccumulator);

				registerISINValidator(configRegistry);
				registerAskPriceValidator(configRegistry, dataProvider);
				registerBidPriceValidator(configRegistry);

				registerSecurityDescriptionCellStyle(configRegistry);
				registerPricingCellStyle(configRegistry);

				registerPriceFormatter(configRegistry);
				registerDateFormatter(configRegistry);
				registerLotSizeFormatter(configRegistry);

				registerCheckBoxEditor(configRegistry, new CheckBoxPainter(), new CheckBoxCellEditor());
				registerComboBox(configRegistry,
						new ComboBoxPainter(),
						new ComboBoxCellEditor(Arrays.asList(new PricingTypeBean("MN"), new PricingTypeBean("AT"))));

				registerEditableRules(configRegistry, dataProvider);
			}

		};
	}

	private static void registerConfigLabelsOnColumns(ColumnOverrideLabelAccumulator columnLabelAccumulator) {
		columnLabelAccumulator.registerColumnOverrides(RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.SECURITY_ID_PROP_NAME), SECURITY_ID_EDITOR, SECURITY_ID_CONFIG_LABEL);

		columnLabelAccumulator.registerColumnOverrides(RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.SECURITY_DESCRIPTION_PROP_NAME), ALIGN_CELL_CONTENTS_LEFT_CONFIG_LABEL);

		columnLabelAccumulator.registerColumnOverrides(RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.ISSUE_DATE_PROP_NAME), FORMAT_DATE_CONFIG_LABEL);

		columnLabelAccumulator.registerColumnOverrides(RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.PRICING_TYPE_PROP_NAME), COMBO_BOX_CONFIG_LABEL, COMBO_BOX_EDITOR_CONFIG_LABEL, FORMAT_PRICING_TYPE_CONFIG_LABEL);

		columnLabelAccumulator.registerColumnOverrides(RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.BID_PRICE_PROP_NAME), BID_PRICE_CONFIG_LABEL, FORMAT_DOUBLE_6_PLACES_CONFIG_LABEL, FORMAT_DOUBLE_2_PLACES_CONFIG_LABEL, ALIGN_CELL_CONTENTS_RIGHT_CONFIG_LABEL);

		columnLabelAccumulator.registerColumnOverrides(RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.ASK_PRICE_PROP_NAME), ASK_PRICE_CONFIG_LABEL, FORMAT_DOUBLE_6_PLACES_CONFIG_LABEL, FORMAT_DOUBLE_2_PLACES_CONFIG_LABEL, ALIGN_CELL_CONTENTS_RIGHT_CONFIG_LABEL);

		columnLabelAccumulator.registerColumnOverrides(RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.SPREAD_PROP_NAME), SPREAD_CONFIG_LABEL, FORMAT_DOUBLE_6_PLACES_CONFIG_LABEL, ALIGN_CELL_CONTENTS_RIGHT_CONFIG_LABEL);

		columnLabelAccumulator.registerColumnOverrides(RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.LOT_SIZE_PROP_NAME), LOT_SIZE_CONFIG_LABEL, FORMAT_IN_MILLIONS_CONFIG_LABEL, ALIGN_CELL_CONTENTS_RIGHT_CONFIG_LABEL);

		columnLabelAccumulator.registerColumnOverrides(RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.PUBLISH_FLAG_PROP_NAME), CHECK_BOX_EDITOR_CONFIG_LABEL, CHECK_BOX_CONFIG_LABEL);
	}

	private static void registerSecurityDescriptionCellStyle(IConfigRegistry configRegistry) {
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, ALIGN_CELL_CONTENTS_LEFT_CONFIG_LABEL);
	}

	private static void registerPricingCellStyle(IConfigRegistry configRegistry) {
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, ALIGN_CELL_CONTENTS_RIGHT_CONFIG_LABEL);
	}

	private static void registerCheckBoxEditor(IConfigRegistry configRegistry, ICellPainter checkBoxCellPainter, ICellEditor checkBoxCellEditor) {
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, checkBoxCellPainter, DisplayMode.NORMAL, CHECK_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultBooleanDisplayConverter(), DisplayMode.NORMAL, CHECK_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, checkBoxCellEditor, DisplayMode.NORMAL, CHECK_BOX_EDITOR_CONFIG_LABEL);
	}

	private static void registerComboBox(IConfigRegistry configRegistry, ICellPainter comboBoxCellPainter, ICellEditor comboBoxCellEditor) {
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, comboBoxCellPainter, DisplayMode.NORMAL, COMBO_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, comboBoxCellEditor, DisplayMode.NORMAL, COMBO_BOX_EDITOR_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, comboBoxCellEditor, DisplayMode.EDIT, COMBO_BOX_EDITOR_CONFIG_LABEL);

		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, PricingTypeBean.getDisplayConverter(), DisplayMode.NORMAL, FORMAT_PRICING_TYPE_CONFIG_LABEL);
	}

	private static void registerISINValidator(IConfigRegistry configRegistry) {

		TextCellEditor textCellEditor = new TextCellEditor();
		textCellEditor.setErrorDecorationEnabled(true);
		textCellEditor.setErrorDecorationText("Security Id must be 3 alpha characters optionally followed by numbers");
		textCellEditor.setDecorationPositionOverride(SWT.LEFT | SWT.TOP);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, textCellEditor, DisplayMode.NORMAL, SECURITY_ID_EDITOR);
		
		configRegistry.registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, getSecurtityIdValidator(), DisplayMode.EDIT, SECURITY_ID_CONFIG_LABEL);
	}

	private static void registerDateFormatter(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, getDateFormatter(), DisplayMode.NORMAL, FORMAT_DATE_CONFIG_LABEL);
	}

	private static void registerAskPriceValidator(IConfigRegistry configRegistry, IDataProvider dataProvider) {
		configRegistry.registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, getAskPriceValidator(dataProvider), DisplayMode.EDIT, ASK_PRICE_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, getAskPriceValidator(dataProvider), DisplayMode.NORMAL, ASK_PRICE_CONFIG_LABEL);
	}

	private static void registerBidPriceValidator(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, new DefaultNumericDataValidator(), DisplayMode.EDIT, BID_PRICE_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, new DefaultNumericDataValidator(), DisplayMode.NORMAL, BID_PRICE_CONFIG_LABEL);
	}

	private static void registerPriceFormatter(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, getDoubleDisplayConverter(2), DisplayMode.NORMAL, FORMAT_DOUBLE_2_PLACES_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, getDoubleDisplayConverter(6), DisplayMode.NORMAL, FORMAT_DOUBLE_6_PLACES_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, getDoubleDisplayConverter(6), DisplayMode.EDIT, FORMAT_DOUBLE_6_PLACES_CONFIG_LABEL);
	}

	private static void registerEditableRules(IConfigRegistry configRegistry, IDataProvider dataProvider) {
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, SECURITY_ID_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, COMBO_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, CHECK_BOX_CONFIG_LABEL);

		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, getEditRule(dataProvider), DisplayMode.EDIT, ASK_PRICE_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, getEditRule(dataProvider), DisplayMode.EDIT, BID_PRICE_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, getEditRule(dataProvider), DisplayMode.EDIT, LOT_SIZE_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.NEVER_EDITABLE, DisplayMode.EDIT, SPREAD_CONFIG_LABEL);
	}

	private static void registerLotSizeFormatter(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, getMillionsDisplayConverter(), DisplayMode.NORMAL, FORMAT_IN_MILLIONS_CONFIG_LABEL);
	}

	/**
	 * Edit rule: If publish flag is true - bid, ask and size can be edited.
	 */
	private static IEditableRule getEditRule(final IDataProvider dataProvider) {
		return new EditableRule(){
			public boolean isEditable(int columnIndex, int rowIndex) {
				int columnIndexOfPublishFlag = RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.PUBLISH_FLAG_PROP_NAME);
				return ((Boolean) dataProvider.getDataValue(columnIndexOfPublishFlag, rowIndex)).booleanValue();
			}
		};
	}

	/**
	 * Format: Double to a specified number of decimal places
	 */
	private static IDisplayConverter getDoubleDisplayConverter(final int decimalPlaces) {
		return new DisplayConverter(){
			NumberFormat numberFormatter = NumberFormat.getInstance();

			public Object canonicalToDisplayValue(Object canonicalValue) {
				if(canonicalValue == null){
					return "";
				}
				numberFormatter.setMaximumFractionDigits(decimalPlaces);
				numberFormatter.setMinimumFractionDigits(decimalPlaces);
				return numberFormatter.format(Double.valueOf(canonicalValue.toString()));
			}

			public Object displayToCanonicalValue(Object displayValue) {
				try {
					return numberFormatter.parse((String) displayValue);
				} catch (ParseException e) {
					return null;
				}
			}

		};
	}

	/**
	 * Format: String date value using a SimpleDateFormat
	 */
	private static IDisplayConverter getDateFormatter() {
		return new DisplayConverter(){
			private final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yy");

			public Object canonicalToDisplayValue(Object canonicalValue) {
				return dateFormatter.format((Date) canonicalValue);
			}

			public Object displayToCanonicalValue(Object displayValue) {
				return dateFormatter.parse(displayValue.toString(), new ParsePosition(0));
			}
		};
	}

	/**
	 * Format: Number as millions (with commas). Convert to int.
	 */
	private static IDisplayConverter getMillionsDisplayConverter() {
		return new DisplayConverter(){
			NumberFormat numberFormatter = new DecimalFormat("###,###,###");

			public Object canonicalToDisplayValue(Object canonicalValue) {
				if (canonicalValue == null) {
					return null;
				}
				return numberFormatter.format(Integer.valueOf(canonicalValue.toString()));
			}

			public Object displayToCanonicalValue(Object displayValue) {
				return (numberFormatter.parse(displayValue.toString(), new ParsePosition(0))).intValue();
			}
		};
	}

	/**
	 * Validate: Ask price > Bid Price
	 */
	private static IDataValidator getAskPriceValidator(final IDataProvider dataProvider) {

		return new DataValidator(){

			public boolean validate(int columnIndex, int rowIndex, Object newValue) {
				try {
					int indexOfBidPrice = RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.BID_PRICE_PROP_NAME);
					double bidPrice = ((Double) dataProvider.getDataValue(indexOfBidPrice, rowIndex)).doubleValue();
					double askPrice = Double.valueOf(newValue.toString()).doubleValue();
					return askPrice > bidPrice;
				} catch (Exception ex) {
					return false;
				}
			}
		};
	}

	/**
	 * Validate:
	 * 	First three chars must be alphabetic and the following must be numeric
	 */
	private static IDataValidator getSecurtityIdValidator() {
		return new DataValidator(){

			public boolean validate(int columnIndex, int rowIndex, Object newValue) {
				if (newValue == null){
					return false;
				}
				String value = (String) newValue;
				if(value.length() > 3){
					String alphabeticPart = value.substring(0, 2);
					String numericPart = value.substring(3, value.length());
					return StringUtils.isAlpha(alphabeticPart) && StringUtils.isNumeric(numericPart);
				}else{
					String alphabeticPart = value.substring(0, value.length());
					return StringUtils.isAlpha(alphabeticPart);
				}
			}
		};
	}

}

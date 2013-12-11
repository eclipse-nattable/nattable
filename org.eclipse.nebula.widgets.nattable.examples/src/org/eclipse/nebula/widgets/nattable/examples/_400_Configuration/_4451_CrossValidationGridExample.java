/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._400_Configuration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.validate.DataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.ValidationFailedException;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.config.DialogErrorHandling;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.EventData;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.AbstractOverrider;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

/**
 * Example that demonstrates how to implement cross validation in a NatTable.
 * 
 * @author Dirk Fauth
 *
 */
public class _4451_CrossValidationGridExample extends AbstractNatExample {

	public static String DATE_LABEL 	= "DateLabel";
	public static String INVALID_LABEL 	= "InvalidLabel";
	
	private EventList<EventData> valuesToShow = GlazedLists.eventList(new ArrayList<EventData>());
	
	public static void main(String[] args) throws Exception {
		Locale.setDefault(Locale.ENGLISH);
		StandaloneNatExampleRunner.run(new _4451_CrossValidationGridExample());
	}

	/**	
	 * @Override
	 */
	@Override
	public String getDescription() {
		return "Demonstrates how to implement an editable grid with cross validation.";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.examples.INatExample#createExampleControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Control createExampleControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);
		
		Composite gridPanel = new Composite(panel, SWT.NONE);
		gridPanel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(gridPanel);
		
		Composite buttonPanel = new Composite(panel, SWT.NONE);
		buttonPanel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(buttonPanel);

		//property names of the EventData class
		String[] propertyNames = {"title", "description", "where", "fromDate", "toDate"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("title", "Title");
		propertyToLabelMap.put("description", "Description");
		propertyToLabelMap.put("where", "Where");
		propertyToLabelMap.put("fromDate", "From");
		propertyToLabelMap.put("toDate", "To");

		valuesToShow.addAll(createEventData());
		
		ConfigRegistry configRegistry = new ConfigRegistry();
		DefaultGridLayer gridLayer = new DefaultGridLayer(valuesToShow, propertyNames, propertyToLabelMap);
		DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();
		
		IRowDataProvider<EventData> bodyDataProvider = (IRowDataProvider<EventData>)bodyDataLayer.getDataProvider();
		bodyDataLayer.setConfigLabelAccumulator(new CrossValidationLabelAccumulator(bodyDataProvider));
		
		final NatTable natTable = new NatTable(gridPanel, gridLayer, false);
		natTable.setConfigRegistry(configRegistry);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new CrossValidationEditConfiguration(bodyDataProvider));
		natTable.configure();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

		return panel;
	}
	
	
	private List<EventData> createEventData() {
		List<EventData> result = new ArrayList<EventData>();
		
		EventData ed = new EventData();
		ed.setTitle("My correct event");
		ed.setDescription("My event that validates correctly");
		ed.setWhere("Somewhere");
		ed.setFromDate(new GregorianCalendar(2013, 2, 1).getTime());
		ed.setToDate(new GregorianCalendar(2013, 2, 3).getTime());
		result.add(ed);
		
		ed = new EventData();
		ed.setTitle("My wrong event");
		ed.setDescription("My event where validation fails");
		ed.setWhere("Somewhere else");
		ed.setFromDate(new GregorianCalendar(2013, 2, 3).getTime());
		ed.setToDate(new GregorianCalendar(2013, 2, 1).getTime());
		result.add(ed);
		
		return result;
	}

	
	public static boolean isEventDataValid(EventData event) {
		return (event.getFromDate().before(event.getToDate()));
	}
	
	public static boolean isEventDataValid(Date fromDate, Date toDate) {
		return fromDate.before(toDate);
	}
}

/**
 * {@link IConfigLabelAccumulator} that adds labels to the columns that show
 * date values and labels to indicate invalid data.
 */
class CrossValidationLabelAccumulator extends AbstractOverrider {

	private IRowDataProvider<EventData> bodyDataProvider;
	
	CrossValidationLabelAccumulator(IRowDataProvider<EventData> bodyDataProvider) {
		this.bodyDataProvider = bodyDataProvider;
	}
	
	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		//get the row object out of the dataprovider
		EventData rowObject = this.bodyDataProvider.getRowObject(rowPosition);
		
		//in column 3 and 4 there are the values that are cross validated
		if (columnPosition == 3 || columnPosition == 4) {
			configLabels.addLabel(_4451_CrossValidationGridExample.DATE_LABEL);
			
			if (!_4451_CrossValidationGridExample.isEventDataValid(rowObject)) {
				configLabels.addLabel(_4451_CrossValidationGridExample.INVALID_LABEL);
			}
		}
	}
}

/**
 * Configuration for enabling and configuring edit behaviour.
 */
class CrossValidationEditConfiguration extends AbstractRegistryConfiguration  {
	private IRowDataProvider<EventData> bodyDataProvider;
	
	CrossValidationEditConfiguration(IRowDataProvider<EventData> bodyDataProvider) {
		this.bodyDataProvider = bodyDataProvider;
	}

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE);

		configRegistry.registerConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDateDisplayConverter(), 
				DisplayMode.NORMAL, _4451_CrossValidationGridExample.DATE_LABEL);
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDateDisplayConverter(), 
				DisplayMode.EDIT, _4451_CrossValidationGridExample.DATE_LABEL);
		
		//configure the validation error style
		IStyle validationErrorStyle = new Style();
		validationErrorStyle.setAttributeValue(
				CellStyleAttributes.BACKGROUND_COLOR, 
				GUIHelper.COLOR_RED);
		validationErrorStyle.setAttributeValue(
				CellStyleAttributes.FOREGROUND_COLOR, 
				GUIHelper.COLOR_WHITE);
		
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_STYLE, 
				validationErrorStyle,
				DisplayMode.NORMAL,
				_4451_CrossValidationGridExample.INVALID_LABEL);
		
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.DATA_VALIDATOR, 
				new EventDataValidator(bodyDataProvider),
				DisplayMode.EDIT,
				_4451_CrossValidationGridExample.DATE_LABEL);

		configRegistry.registerConfigAttribute(
				EditConfigAttributes.VALIDATION_ERROR_HANDLER, 
				new DialogErrorHandling(true),
				DisplayMode.EDIT,
				_4451_CrossValidationGridExample.DATE_LABEL);
	}
}

class EventDataValidator extends DataValidator {
	private IRowDataProvider<EventData> bodyDataProvider;
	
	EventDataValidator(IRowDataProvider<EventData> bodyDataProvider) {
		this.bodyDataProvider = bodyDataProvider;
	}
	
	@Override
	public boolean validate(int columnIndex, int rowIndex, Object newValue) {
		//get the row object out of the dataprovider
		EventData rowObject = this.bodyDataProvider.getRowObject(rowIndex);
		
		//as the object itself is not yet updated, we need to validate against
		//the given new value
		Date fromDate = rowObject.getFromDate();
		Date toDate = rowObject.getToDate();
		if (columnIndex == 3) {
			fromDate = (Date) newValue;
		}
		else if (columnIndex == 4) {
			toDate = (Date) newValue;
		}
		if (!_4451_CrossValidationGridExample.isEventDataValid(fromDate, toDate)) {
			throw new ValidationFailedException("fromDate is not before toDate");
		}
		return true;
	}
	
}

class CrossValidationDialogErrorHandling extends DialogErrorHandling {
	
	@Override
	protected void showWarningDialog(String dialogMessage, String dialogTitle) {
		if (!isWarningDialogActive()) {
			//conversion/validation failed - so open dialog with error message
			
			if (dialogMessage != null) {
				MessageDialog warningDialog = new MessageDialog(
						Display.getCurrent().getActiveShell(), 
						dialogTitle, 
						null, 
						dialogMessage, 
						MessageDialog.WARNING, 
						new String[] {
							getChangeButtonLabel(),
							getDiscardButtonLabel(),
							"Commit"}, 
						0);
				
				//if discard was selected close the editor
				int returnCode = warningDialog.open();
				if (returnCode == 1) {
					this.editor.close();
				}
				else if (returnCode == 2) {
					this.editor.commit(MoveDirectionEnum.NONE, true, true);
				}
			}
		}
	}

}

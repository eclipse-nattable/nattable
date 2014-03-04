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
package org.eclipse.nebula.widgets.nattable.examples._400_Configuration;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.Person;
import org.eclipse.nebula.widgets.nattable.examples.data.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
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
import org.eclipse.nebula.widgets.nattable.hover.config.ColumnHeaderHoverLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.hover.config.RowHeaderHoverLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.NatTableBorderOverlayPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.theme.DefaultNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.style.theme.ThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Example showing how to use {@link ThemeConfiguration}s in a NatTable.
 * It shows how to set and how to switch themes at runtime.
 * Note that there are several things that are not controllable via themes
 * like row height, column width or ILayerPainter. The later are tight
 * connected to the ILayer themselves.
 * 
 * @author Dirk Fauth
 *
 */
public class _423_ThemeStylingExample extends AbstractNatExample {

	public static final String FEMALE_LABEL = "FemaleLabel";
	public static final String MALE_LABEL = "MaleLabel";
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(650, 400, new _423_ThemeStylingExample());
	}

	@Override
	public String getDescription() {
		return "This example shows the usage of themes for styling a NatTable and how"
				+ " to switch themes at runtime.  It also shows how to deal with attributes"
				+ " that are not configurable via themes like row heights, column widths and"
				+ " ILayerPainter.";
	}
	
	@Override
	public Control createExampleControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());

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
		final ListDataProvider<Person> bodyDataProvider = new DefaultBodyDataProvider<Person>(PersonService.getPersons(10), propertyNames);
		final DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
		HoverLayer bodyHoverLayer = new HoverLayer(bodyDataLayer);
		SelectionLayer selectionLayer = new SelectionLayer(bodyHoverLayer);
		ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

		//add labels to provider conditional styling
		bodyDataLayer.setConfigLabelAccumulator(new IConfigLabelAccumulator() {
			@Override
			public void accumulateConfigLabels(LabelStack configLabels,	int columnPosition, int rowPosition) {
				Person p = bodyDataProvider.getRowObject(rowPosition);
				if (p != null) {
					configLabels.addLabel(p.getGender().equals(Gender.FEMALE) ? FEMALE_LABEL : MALE_LABEL);
				}
			}
		});
		
		//build the column header layer
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		final DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		HoverLayer columnHoverLayer = new HoverLayer(columnHeaderDataLayer, false);
		final ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHoverLayer, viewportLayer, selectionLayer, false);
		
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
		final CornerLayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);
		
		//build the grid layer
		final GridLayer gridLayer = new GridLayer(viewportLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer);
		
		final NatTable natTable = new NatTable(container, gridLayer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
		
		//adding a full border
		natTable.addOverlayPainter(new NatTableBorderOverlayPainter(natTable.getConfigRegistry()));
		
		Composite buttonPanel = new Composite(container, SWT.NONE);
		buttonPanel.setLayout(new RowLayout());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);
		
		final ThemeConfiguration defaultTheme = new DefaultNatTableThemeConfiguration();
		final ThemeConfiguration hoverTheme = new HoverThemeConfiguration();
		final ThemeConfiguration modernTheme = new ModernNatTableThemeConfiguration();
		final ThemeConfiguration conditionalTheme = new ConditionalStylingThemeConfiguration();
		final ThemeConfiguration fontTheme = new FontStylingThemeConfiguration();
		
		Button defaultThemeButton = new Button(buttonPanel, SWT.PUSH);
		defaultThemeButton.setText("NatTable Default Theme");
		defaultThemeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				natTable.setTheme(defaultTheme);
				
				//reset to default state
				cleanupNonThemeSettings(gridLayer, bodyDataLayer, columnHeaderDataLayer);
			}
		});
		
		Button windowsThemeButton = new Button(buttonPanel, SWT.PUSH);
		windowsThemeButton.setText("NatTable Modern Theme");
		windowsThemeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				natTable.setTheme(modernTheme);
				
				//reset to default state
				cleanupNonThemeSettings(gridLayer, bodyDataLayer, columnHeaderDataLayer);
			}
		});
		
		Button hoverThemeButton = new Button(buttonPanel, SWT.PUSH);
		hoverThemeButton.setText("Hover Theme");
		hoverThemeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				natTable.setTheme(hoverTheme);
				
				//reset to default state
				cleanupNonThemeSettings(gridLayer, bodyDataLayer, columnHeaderDataLayer);
			}
		});
		
		Button conditionalThemeButton = new Button(buttonPanel, SWT.PUSH);
		conditionalThemeButton.setText("Conditional Theme");
		conditionalThemeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				natTable.setTheme(conditionalTheme);
				
				//reset to default state
				cleanupNonThemeSettings(gridLayer, bodyDataLayer, columnHeaderDataLayer);
			}
		});
		
		Button fontThemeButton = new Button(buttonPanel, SWT.PUSH);
		fontThemeButton.setText("Increased Font Theme");
		fontThemeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				natTable.setTheme(fontTheme);
				
				//reset to default state
				cleanupNonThemeSettings(gridLayer, bodyDataLayer, columnHeaderDataLayer);
			}
		});
		
		return container;
	}

	/**
	 * Resets row heights and column widths to default settings.
	 */
	private void cleanupNonThemeSettings(GridLayer gridLayer, 
			DataLayer bodyDataLayer, DataLayer columnHeaderDataLayer) {
		
		columnHeaderDataLayer.setRowHeightByPosition(0, 20);
		
		for (int i = 0; i < gridLayer.getRowCount(); i++) {
			bodyDataLayer.setRowHeightByPosition(i, 20);
		}
		
		for (int i = 0; i < gridLayer.getColumnCount(); i++) {
			bodyDataLayer.setColumnWidthByPosition(i, 100);
		}
	}
	
	/**
	 * ThemeConfiguration that adds hover styling. 
	 * Note that the stylings are only interpreted because the HoverLayer is involved in the layer stacks. 
	 */
	class HoverThemeConfiguration extends DefaultNatTableThemeConfiguration {
		{
			this.bodyHoverBgColor = GUIHelper.COLOR_YELLOW;
			
			this.rHeaderHoverBgColor = GUIHelper.COLOR_RED;
			
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

			ICellPainter bgImagePainter = new BackgroundImagePainter(txtPainter, bgImage);
			
			this.cHeaderCellPainter = bgImagePainter;
			this.cornerCellPainter = bgImagePainter;
			
			this.cHeaderSelectionCellPainter = new BackgroundImagePainter(txtPainter, selectedBgImage);
			this.cHeaderHoverCellPainter = new BackgroundImagePainter(txtPainter, hoverBgImage);

			this.renderCornerGridLines = true;
			this.renderColumnHeaderGridLines = true;
		}
	}
	
	/**
	 * ThemeConfiguration that shows how to create a custom theme with conditional styling.
	 */
	class ConditionalStylingThemeConfiguration extends DefaultNatTableThemeConfiguration {
		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			super.configureRegistry(configRegistry);
			
			//add custom styling
			IStyle femaleStyle = new Style();
			femaleStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_YELLOW);
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_STYLE, 
					femaleStyle,
					DisplayMode.NORMAL,
					FEMALE_LABEL);
		}
		
		@Override
		public void unregisterThemeStyleConfigurations(IConfigRegistry configRegistry) {
			super.unregisterThemeStyleConfigurations(configRegistry);
			
			//unregister custom styling
			configRegistry.unregisterConfigAttribute(
					CellConfigAttributes.CELL_STYLE, 
					DisplayMode.NORMAL,
					FEMALE_LABEL);
		}
	}

	/**
	 * ThemeConfiguration that sets different fonts which has impact on the row heights and columns widths.
	 * The automatic resizing is done via specially configured TextPainter instances.
	 */
	class FontStylingThemeConfiguration extends ModernNatTableThemeConfiguration {
		{
			this.defaultFont = GUIHelper.getFont(new FontData("Arial", 15, SWT.NORMAL));
			this.defaultSelectionFont = GUIHelper.getFont(new FontData("Arial", 15, SWT.NORMAL));

			this.cHeaderFont = GUIHelper.getFont(new FontData("Arial", 18, SWT.NORMAL));
			this.cHeaderSelectionFont = GUIHelper.getFont(new FontData("Arial", 18, SWT.NORMAL));
			
			this.rHeaderFont = GUIHelper.getFont(new FontData("Arial", 18, SWT.NORMAL));
			this.rHeaderSelectionFont = GUIHelper.getFont(new FontData("Arial", 18, SWT.NORMAL));

			//configure painter that automatically increase the row height
			this.defaultCellPainter = new BackgroundPainter(new PaddingDecorator(new TextPainter(false, true, true), 0, 0, 0, 5));
			this.cHeaderCellPainter = new BackgroundPainter(new PaddingDecorator(new TextPainter(false, true, true), 0, 0, 0, 5));
			this.rHeaderCellPainter = new TextPainter(false, true, true);

			this.renderCornerGridLines = true;
			this.renderColumnHeaderGridLines = true;
		}
	}
}

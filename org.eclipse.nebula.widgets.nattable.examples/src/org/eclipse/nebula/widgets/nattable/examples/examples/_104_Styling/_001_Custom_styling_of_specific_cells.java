/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.examples._104_Styling;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.Person;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CustomLineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.TextDecorationEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _001_Custom_styling_of_specific_cells extends AbstractNatExample {
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(800, 600, new _001_Custom_styling_of_specific_cells());
	}
	
	@Override
	public String getDescription() {
		return
				"NatTable has a flexible mechanism for customizing styles for specific cells.\n" +
				"First an IConfigLabelAccumulator is used to tag the cells you want to customize with a custom label.\n" +
				"Then a new style is registered in the config registry for the custom label.\n" +
				"\n" +
				"This example shows a trivial example that simply changes the background color for the cell at column, row index (1, 5).\n" +
				"It also shows decorating text underlined and strikethrough for the cell at column, row index (1, 10),\n" +
				"and looking at row 13 you will see how to use the CustomLineBorderDecorator to add a border around multiple cells.\n" +
				"You can change the IConfigLabelAccumulator to target arbitrary other cells, and you can also modify any other style\n" +
				"attributes you wish. You can also register custom display converters, editable rules, etc. in the same way.";
	}
	
	private static final String FOO_LABEL = "FOO";
	private static final String BAR_LABEL = "BAR";
	
	public Control createExampleControl(Composite parent) {
		List<Person> myList = new ArrayList<Person>();
		for (int i = 1; i <= 100; i++) {
			myList.add(new Person(i, "Joe" + i, new Date()));
		}
		
		String[] propertyNames = {
				"id",
				"name",
				"birthDate"
		};
		
		IColumnPropertyAccessor<Person> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<Person>(propertyNames);
		ListDataProvider<Person> listDataProvider = new ListDataProvider<Person>(myList, columnPropertyAccessor);
		DefaultGridLayer gridLayer = new DefaultGridLayer(listDataProvider, new DummyColumnHeaderDataProvider(listDataProvider));
		final DefaultBodyLayerStack bodyLayer = gridLayer.getBodyLayer();
		
		// Custom label "FOO" for cell at column, row index (1, 5)
		IConfigLabelAccumulator cellLabelAccumulator = new IConfigLabelAccumulator() {
			public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
				int columnIndex = bodyLayer.getColumnIndexByPosition(columnPosition);
				int rowIndex = bodyLayer.getRowIndexByPosition(rowPosition);
				if (columnIndex == 1 && rowIndex == 5) {
					configLabels.addLabel(FOO_LABEL);
				}
				if (columnIndex == 1 && rowIndex == 10) {
					configLabels.addLabel(BAR_LABEL);
				}
				
				//add labels for surrounding borders
				if (rowIndex == 13) {
					configLabels.addLabel(CustomLineBorderDecorator.TOP_LINE_BORDER_LABEL);
					configLabels.addLabel(CustomLineBorderDecorator.BOTTOM_LINE_BORDER_LABEL);
					
					if (columnIndex == 0) {
						configLabels.addLabel(CustomLineBorderDecorator.LEFT_LINE_BORDER_LABEL);
					}
					if (columnIndex == 2) {
						configLabels.addLabel(CustomLineBorderDecorator.RIGHT_LINE_BORDER_LABEL);
					}
				}
			}
		};
		bodyLayer.setConfigLabelAccumulator(cellLabelAccumulator);
		
		NatTable natTable = new NatTable(parent, gridLayer, false);
		
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration() {
			{
				//override the LineBorderDecorator here to show how to paint borders on single sides of a cell
				cellPainter = new CustomLineBorderDecorator(new TextPainter());
				//set a border style
				borderStyle = new BorderStyle(2, GUIHelper.COLOR_BLUE, LineStyleEnum.DASHDOT);
			}
		});
		// Custom style for label "FOO"
		natTable.addConfiguration(new AbstractRegistryConfiguration() {
			public void configureRegistry(IConfigRegistry configRegistry) {
				Style cellStyle = new Style();
				cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_GREEN);
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, FOO_LABEL);

				cellStyle = new Style();
				cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, TextDecorationEnum.UNDERLINE_STRIKETHROUGH);
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, BAR_LABEL);
			}
		});
		natTable.configure();
		
		return natTable;
	}
	
}

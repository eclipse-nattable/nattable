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
package org.eclipse.nebula.widgets.nattable.examples.examples._102_Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.NumberValues;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CellPainterDecorator_Example extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new CellPainterDecorator_Example());
	}

	@Override
	public String getDescription() {
		return
				"This example demonstrates the different possibilities to add decoration to a cell.\n" +
				"The first NatTable instance shows how to use decoration dependent rendering, which means that the " +
				"base painter and the decoration painter are sharing the cell as equal partners. So the base painter has less " +
				"space.\n" + 
				"The second NatTable instance shows how to decorate a cell without implications to the base painter. This means " +
				"the base painter renders normally while the decorator will simply paint over the base painter.";
	}
	
	public static final String COLUMN_ONE_LABEL = "ColumnOneLabel";
	public static final String COLUMN_TWO_LABEL = "ColumnTwoLabel";
	public static final String COLUMN_THREE_LABEL = "ColumnThreeLabel";
	public static final String COLUMN_FOUR_LABEL = "ColumnFourLabel";
	public static final String COLUMN_FIVE_LABEL = "ColumnFiveLabel";
	public static final String COLUMN_SIX_LABEL = "ColumnSixLabel";
	public static final String COLUMN_SEVEN_LABEL = "ColumnSevenLabel";
	public static final String COLUMN_EIGHT_LABEL = "ColumnEightLabel";
	
	public Control createExampleControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
		
		Composite tableContainer = new Composite(parent, SWT.NONE);
		
		tableContainer.setLayout(new GridLayout(1, true));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableContainer);
		
		createNatTable(tableContainer, true);
		createNatTable(tableContainer, false);
		
		return tableContainer;
	}
	
	
	private void createNatTable(Composite parent, boolean paintDecorationDependent) {
		String[] propertyNames = {"columnOneNumber", "columnTwoNumber", "columnThreeNumber", "columnFourNumber", 
				"columnFiveNumber", "columnSixNumber", "columnSevenNumber", "columnEightNumber"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("columnOneNumber", "C 1");
		propertyToLabelMap.put("columnTwoNumber", "C 2");
		propertyToLabelMap.put("columnThreeNumber", "C 3");
		propertyToLabelMap.put("columnFourNumber", "C 4");
		propertyToLabelMap.put("columnFiveNumber", "C 5");
		propertyToLabelMap.put("columnSixNumber", "C 6");
		propertyToLabelMap.put("columnSevenNumber", "C 7");
		propertyToLabelMap.put("columnEightNumber", "C 8");

		DefaultGridLayer gridLayer = new DefaultGridLayer(createNumberValuesList(), propertyNames, propertyToLabelMap);
		DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();
		bodyDataLayer.setDefaultRowHeight(40);
		bodyDataLayer.setDefaultColumnWidth(40);

		final ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
		registerColumnLabels(columnLabelAccumulator);
		
		NatTable natTable = new NatTable(parent, gridLayer, false);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new TableDecorationConfiguration(paintDecorationDependent));
		natTable.configure();

		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
	}

	
	private List<NumberValues> createNumberValuesList() {
		List<NumberValues> result = new ArrayList<NumberValues>();
		
		NumberValues nv = null;
		for (int i = 0; i < 3; i++) {
			nv = new NumberValues();
			nv.setColumnOneNumber(111);
			nv.setColumnTwoNumber(222);
			nv.setColumnThreeNumber(333);
			nv.setColumnFourNumber(444);
			nv.setColumnFiveNumber(555);
			nv.setColumnSixNumber(666);
			nv.setColumnSevenNumber(777);
			nv.setColumnEightNumber(888);
			nv.setColumnNineNumber(999);
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
		columnLabelAccumulator.registerColumnOverrides(5, COLUMN_SIX_LABEL);
		columnLabelAccumulator.registerColumnOverrides(6, COLUMN_SEVEN_LABEL);
		columnLabelAccumulator.registerColumnOverrides(7, COLUMN_EIGHT_LABEL);
	}
}


class TableDecorationConfiguration extends AbstractRegistryConfiguration  {

	private boolean paintDecorationdepentend;
	
	public TableDecorationConfiguration(boolean paintDecorationDependent) {
		this.paintDecorationdepentend = paintDecorationDependent;
	}
	
	public void configureRegistry(IConfigRegistry configRegistry) {
		
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER, 
				new CellPainterDecorator(
						new TextPainter(), 
						CellEdgeEnum.TOP, 
						new ImagePainter(GUIHelper.getImage("plus")), 
						this.paintDecorationdepentend),
				DisplayMode.NORMAL, CellPainterDecorator_Example.COLUMN_ONE_LABEL);
		
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER, 
				new CellPainterDecorator(
						new TextPainter(), 
						CellEdgeEnum.BOTTOM, 
						new ImagePainter(GUIHelper.getImage("plus")), 
						this.paintDecorationdepentend),
				DisplayMode.NORMAL, CellPainterDecorator_Example.COLUMN_TWO_LABEL);
		
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER, 
				new CellPainterDecorator(
						new TextPainter(), 
						CellEdgeEnum.LEFT, 
						new ImagePainter(GUIHelper.getImage("plus")), 
						this.paintDecorationdepentend),
				DisplayMode.NORMAL, CellPainterDecorator_Example.COLUMN_THREE_LABEL);
		
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER, 
				new CellPainterDecorator(
						new TextPainter(), 
						CellEdgeEnum.RIGHT, 
						new ImagePainter(GUIHelper.getImage("plus")), 
						this.paintDecorationdepentend),
				DisplayMode.NORMAL, CellPainterDecorator_Example.COLUMN_FOUR_LABEL);
		
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER, 
				new CellPainterDecorator(
						new TextPainter(), 
						CellEdgeEnum.TOP_LEFT, 
						new ImagePainter(GUIHelper.getImage("plus")), 
						this.paintDecorationdepentend),
				DisplayMode.NORMAL, CellPainterDecorator_Example.COLUMN_FIVE_LABEL);
		
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER, 
				new CellPainterDecorator(
						new TextPainter(), 
						CellEdgeEnum.TOP_RIGHT, 
						new ImagePainter(GUIHelper.getImage("plus")), 
						this.paintDecorationdepentend),
				DisplayMode.NORMAL, CellPainterDecorator_Example.COLUMN_SIX_LABEL);
		
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER, 
				new CellPainterDecorator(
						new TextPainter(), 
						CellEdgeEnum.BOTTOM_LEFT, 
						new ImagePainter(GUIHelper.getImage("plus")), 
						this.paintDecorationdepentend),
				DisplayMode.NORMAL, CellPainterDecorator_Example.COLUMN_SEVEN_LABEL);
		
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER, 
				new CellPainterDecorator(
						new TextPainter(), 
						CellEdgeEnum.BOTTOM_RIGHT, 
						new ImagePainter(GUIHelper.getImage("plus")), 
						this.paintDecorationdepentend),
				DisplayMode.NORMAL, CellPainterDecorator_Example.COLUMN_EIGHT_LABEL);
		

	}

}

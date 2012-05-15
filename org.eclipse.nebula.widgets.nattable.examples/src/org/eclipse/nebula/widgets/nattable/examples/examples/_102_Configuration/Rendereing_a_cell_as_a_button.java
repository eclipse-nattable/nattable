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
package org.eclipse.nebula.widgets.nattable.examples.examples._102_Configuration;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.SelectionExampleGridLayer;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.ButtonCellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.DebugMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Rendereing_a_cell_as_a_button extends AbstractNatExample {
	public static final String CUSTOM_CELL_LABEL = "Cell_LABEL";

	private ButtonCellPainter buttonPainter;
	private SelectionExampleGridLayer gridLayer;

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 400, new Rendereing_a_cell_as_a_button());
	}
	
	@Override
	public String getDescription() {
		return
				"Demonstrates rendering the cell as a button. Custom actions can be triggered on button click.\n" +
				"\n" +
				"Note: The button is 'drawn' using a custom painter. This is more efficient than using a Button widget.";
	}

	public Control createExampleControl(Composite parent) {
		gridLayer = new SelectionExampleGridLayer();
		NatTable natTable = new NatTable(parent, gridLayer, false);
		IConfigRegistry configRegistry = new ConfigRegistry();

		DataLayer bodyDataLayer = gridLayer.getBodyDataLayer();

		// Step 1: Create a label accumulator - adds custom labels to all cells which we
		// wish to render differently. In this case render as a button.
		ColumnOverrideLabelAccumulator cellLabelAccumulator =	new ColumnOverrideLabelAccumulator(bodyDataLayer);
		cellLabelAccumulator.registerColumnOverrides(2, CUSTOM_CELL_LABEL);

		// Step 2: Register label accumulator
		bodyDataLayer.setConfigLabelAccumulator(cellLabelAccumulator);

		// Step 3: Register your custom cell painter, cell style, against the label applied to the cell.
		addButtonToColumn(configRegistry, natTable);
		natTable.addConfiguration(new ButtonClickConfiguration<RowDataFixture>(buttonPainter));

		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new DebugMenuConfiguration(natTable));

		natTable.setConfigRegistry(configRegistry);
		natTable.configure();

		// Layout SWT widgets. Not relevant to example code.
		parent.setLayout(new GridLayout(1, true));
		natTable.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		setupTextArea(parent);

		return natTable;
	}

	private void addButtonToColumn(IConfigRegistry configRegistry, Composite parent) {
		buttonPainter = new ButtonCellPainter(
				new CellPainterDecorator(new TextPainter(), CellEdgeEnum.RIGHT, new ImagePainter(GUIHelper.getImage("preferences"))));

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
				buttonPainter,
				DisplayMode.NORMAL,
				CUSTOM_CELL_LABEL);

		// Add your listener to the button
		buttonPainter.addClickListener(new MyMouseAction());

		// Set the color of the cell. This is picked up by the button painter to style the button
		Style style = new Style();
		style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_WHITE);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,	style, DisplayMode.NORMAL, CUSTOM_CELL_LABEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,	style, DisplayMode.SELECT, CUSTOM_CELL_LABEL);
	}

	/**
	 * Sample action to execute when the button is clicked.
	 */
	class MyMouseAction implements IMouseAction{

		public void run(NatTable natTable, MouseEvent event) {
			NatEventData eventData = NatEventData.createInstanceFromEvent(event);
			int rowIndex = natTable.getRowIndexByPosition(eventData.getRowPosition());
			int columnIndex = natTable.getColumnIndexByPosition(eventData.getColumnPosition());

			ListDataProvider<RowDataFixture> dataProvider = gridLayer.getBodyDataProvider();

			Object rowObject = dataProvider.getRowObject(rowIndex);
			Object cellData = dataProvider.getDataValue(columnIndex, rowIndex);

			log("Clicked on cell: " + cellData);
			log("Clicked on row: " + rowObject + "\n");
		}
	}

}

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
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.SelectionExampleGridLayer;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.CellOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Applying_style_to_a_cell extends AbstractNatExample {

	private static final String CELL_LABEL = "Cell_LABEL";

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 400, new Applying_style_to_a_cell());
	}

	@Override
	public String getDescription() {
		return
				"This example shows how to style cell(s) depending on the their data value.\n" +
				"\n" +
				"The basic approach is to apply 'labels' to cells. Once a label is applied, " +
				"you can register various config attributes against the label. During rendering, NatTable " +
				"will apply all the registered attributes to the cells with matching labels.\n" +
				"\n" +
				"You can apply labels in any fashion you like by implementing the IConfigLabelAccumulator interface. " +
				"Out of the box, labels can be applied to whole columns, rows and cells.";
	}
	
	public Control createExampleControl(Composite parent) {
		SelectionExampleGridLayer gridLayer = new SelectionExampleGridLayer();
		NatTable natTable = new NatTable(parent, gridLayer, false);

		DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();

		// Label accumulator - adds labels to all cells with the given data value
		CellOverrideLabelAccumulator<RowDataFixture> cellLabelAccumulator =
			new CellOverrideLabelAccumulator<RowDataFixture>(gridLayer.getBodyDataProvider());
		cellLabelAccumulator.registerOverride("AAA", 2, CELL_LABEL);

		// Register your cell style, against the label applied to the cell
		// Other configuration which can be added (apart from style) include
		// CellConfigAttributes, EditConfigAttributes, SortConfigAttributes etc.
		IConfigRegistry configRegistry = new ConfigRegistry();
		addColumnHighlight(configRegistry);

		// Register label accumulator
		bodyDataLayer.setConfigLabelAccumulator(cellLabelAccumulator);
		gridLayer.getSelectionLayer().addConfiguration(new DefaultSelectionLayerConfiguration());

		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.setConfigRegistry(configRegistry);

		natTable.configure();
		return natTable;
	}
	
	private void addColumnHighlight(IConfigRegistry configRegistry) {
		Style style = new Style();
		style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_RED);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, // attribute to apply
		                                       style, 							// value of the attribute
		                                       DisplayMode.NORMAL, 				// apply during normal rendering i.e not during selection or edit
		                                       CELL_LABEL); 					// apply the above for all cells with this label

		// Override the selection style on the highlighted cells.
		// Note: This is achieved by specifying the display mode.
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT, CELL_LABEL); 					
	}

}

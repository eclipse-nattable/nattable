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


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.PersistentNatExampleWrapper;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.StyledColumnHeaderConfiguration;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.StyledRowHeaderConfiguration;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultRowStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregrateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.editor.command.DisplayColumnStyleEditorCommandHandler;
import org.eclipse.nebula.widgets.nattable.ui.menu.DebugMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _000_Styled_grid extends AbstractNatExample {

	private static final String COLUMN_LABEL_1 = "ColumnLabel_1";
	private static final String BODY_LABEL_1 = "BodyLabel_1";


	public static void main(String[] args) {
		StandaloneNatExampleRunner.run(700, 400, new PersistentNatExampleWrapper(new _000_Styled_grid()));
	}

	@Override
	public String getDescription() {
		return
				"Grid demonstrates how to setup basic colors/font/border for the various regions in NatTable.\n" +
				"\n" +
				"Features:\n" +
				"Different styles can be specified for:\n" +
				"	Body\n" +
				"	Column header\n" +
				"	Row header\n" +
				"	Different states of the cell normal/selection/edit\n" +
				" 	Specific styles can be applied to arbitrary selection of cells (applied to 'Column 2' in example)\n" +
				"\n" +
				"Key Bindings:\n" +
				"	Selection styles can be seen by selecting a region on the table\n" +
				"	Styles can be edited by choosing the 'Column style editor' from the right click menu\n" +
				"\n" +
				"Technical information:\n" +
				"	Support is provided for automatic creation and disposal for SWT colors/fonts (see GUIHelper)";
	}
	
	public Control createExampleControl(Composite parent) {
		NatTable natTable = setup(parent);

		addCustomStyling(natTable);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		addColumnHighlight(natTable.getConfigRegistry());

		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

		natTable.addConfiguration(new DebugMenuConfiguration(natTable));
		natTable.configure();
		return natTable;
	}

	private NatTable setup(Composite parent){
		DummyGridLayerStack gridLayer = new DummyGridLayerStack();
		final NatTable natTable = new NatTable(parent, gridLayer, false);
		DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();

		// Add an AggregrateConfigLabelAccumulator - we can add other accumulators to this as required
		AggregrateConfigLabelAccumulator aggregrateConfigLabelAccumulator = new AggregrateConfigLabelAccumulator();
		bodyDataLayer.setConfigLabelAccumulator(aggregrateConfigLabelAccumulator);

		ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		ColumnOverrideLabelAccumulator bodyLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);

		aggregrateConfigLabelAccumulator.add(bodyLabelAccumulator);
		aggregrateConfigLabelAccumulator.add(columnLabelAccumulator);

		// Add a label for the highlighted column
		// We will add a style for this label to the config registry in a bit
		bodyLabelAccumulator.registerColumnOverrides(2, BODY_LABEL_1);
		columnLabelAccumulator.registerColumnOverrides(2, COLUMN_LABEL_1);

		// Register a command handler for the StyleEditorDialog
		DisplayColumnStyleEditorCommandHandler styleChooserCommandHandler =
			new DisplayColumnStyleEditorCommandHandler(gridLayer.getBodyLayer().getSelectionLayer(), columnLabelAccumulator, natTable.getConfigRegistry());

		DefaultBodyLayerStack bodyLayer = gridLayer.getBodyLayer();
		bodyLayer.registerCommandHandler(styleChooserCommandHandler);

		// Register the style editor as persistable
		// This will persist the style applied to the columns when NatTable#saveState is invoked
		bodyLayer.registerPersistable(styleChooserCommandHandler);
		bodyLayer.registerPersistable(columnLabelAccumulator);

		return natTable;
	}

	/**
	 * Register an attribute to be applied to all cells with the highlight label.
	 * A similar approach can be used to bind styling to an arbitrary group of cells
	 */
	private void addColumnHighlight(IConfigRegistry configRegistry) {
		Style style = new Style();
		style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_BLUE);
		style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, // attribute to apply
		                                       style, 							// value of the attribute
		                                       DisplayMode.NORMAL, 				// apply during normal rendering i.e not during selection or edit
		                                       COLUMN_LABEL_1); 				// apply the above for all cells with this label
	}

	private void addCustomStyling(NatTable natTable) {
		// Setup NatTable default styling

		// NOTE: Getting the colors and fonts from the GUIHelper ensures that
		// they are disposed properly (required by SWT)
		DefaultNatTableStyleConfiguration natTableConfiguration = new DefaultNatTableStyleConfiguration();
		natTableConfiguration.bgColor = GUIHelper.getColor(249, 172, 7);
		natTableConfiguration.fgColor = GUIHelper.getColor(30, 76, 19);
		natTableConfiguration.hAlign = HorizontalAlignmentEnum.LEFT;
		natTableConfiguration.vAlign = VerticalAlignmentEnum.TOP;

		// A custom painter can be plugged in to paint the cells differently
		natTableConfiguration.cellPainter = new PaddingDecorator(new TextPainter(), 1);

		// Setup even odd row colors - row colors override the NatTable default colors
		DefaultRowStyleConfiguration rowStyleConfiguration = new DefaultRowStyleConfiguration();
		rowStyleConfiguration.oddRowBgColor = GUIHelper.getColor(254, 251, 243);
		rowStyleConfiguration.evenRowBgColor = GUIHelper.COLOR_WHITE;

		// Setup selection styling
		DefaultSelectionStyleConfiguration selectionStyle = new DefaultSelectionStyleConfiguration();
		selectionStyle.selectionFont = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL));
		selectionStyle.selectionBgColor = GUIHelper.getColor(217, 232, 251);
		selectionStyle.selectionFgColor = GUIHelper.COLOR_BLACK;
		selectionStyle.anchorBorderStyle = new BorderStyle(1, GUIHelper.COLOR_DARK_GRAY, LineStyleEnum.SOLID);
		selectionStyle.anchorBgColor = GUIHelper.getColor(65, 113, 43);
		selectionStyle.selectedHeaderBgColor = GUIHelper.getColor(156, 209, 103);

		// Add all style configurations to NatTable
		natTable.addConfiguration(natTableConfiguration);
		natTable.addConfiguration(rowStyleConfiguration);
		natTable.addConfiguration(selectionStyle);

		// 	Column/Row header style and custom painters
		natTable.addConfiguration(new StyledRowHeaderConfiguration());
		natTable.addConfiguration(new StyledColumnHeaderConfiguration());

		// Add popup menu - build your own popup menu using the PopupMenuBuilder
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
	}
}

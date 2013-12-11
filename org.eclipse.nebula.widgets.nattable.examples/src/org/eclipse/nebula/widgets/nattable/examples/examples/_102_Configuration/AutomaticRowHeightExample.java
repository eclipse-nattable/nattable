/*******************************************************************************
 * Copyright (c) 2012, 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.examples._102_Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.AutomaticRowHeightTextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.LineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * @author Dirk Fauth
 *
 */
public class AutomaticRowHeightExample extends AbstractNatExample {

	private static final Display DISPLAY = Display.getDefault();  

	private final List<LogRecord> logMessages = new ArrayList<LogRecord>();
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new AutomaticRowHeightExample());
	}

	/**	
	 * @Override
	 */
	public String getDescription() {
		return "Demonstrates how to implement a log viewer using NatTable with the percentage " +
				"sizing and the automatic row height calculation feature. If you resize the " +
				"window you will see the rows growing/shrinking to always show the whole content " +
				"by wrapping the text and resizing the row heights.";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.examples.INatExample#createExampleControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createExampleControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);

		GridData layoutData = GridDataFactory.fillDefaults().grab(true, true).create();
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		panel.setLayout(layout);
		panel.setLayoutData(layoutData);
		
		loadMessages();

		ListDataProvider<LogRecord> dataProvider = new ListDataProvider<LogRecord>(
				logMessages, new ReflectiveColumnPropertyAccessor<LogRecord>(new String[] {"message"})); //$NON-NLS-1$
		DataLayer dataLayer = new DataLayer(dataProvider);
		dataLayer.setColumnPercentageSizing(true);
		dataLayer.setColumnWidthPercentageByPosition(0, 100);
		dataLayer.setConfigLabelAccumulator(new ValidatorMessageLabelAccumulator());
		
		ViewportLayer layer = new ViewportLayer(dataLayer);
		layer.setRegionName(GridRegion.BODY);
		
		NatTable natTable = new NatTable(panel, NatTable.DEFAULT_STYLE_OPTIONS | SWT.BORDER, layer, false);
		natTable.addConfiguration(new ValidationMessageTableStyleConfiguration());

		natTable.configure();
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
		
		return panel;
	}

	private void loadMessages() {
		String text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " + 
				"sed diam nonumy eirmod tempor invidunt ut labore et dolore " + 
				"magna aliquyam erat, sed diam voluptua.";
		
		Level[] levels = new Level[] {Level.SEVERE, Level.WARNING, Level.INFO};
		String[] words = text.split(" ");
		
		Random levelRandom = new Random();
		Random wordRandom = new Random();
		for (int i = 0; i < 100; i++) {
			int randWords = wordRandom.nextInt(words.length);
			String msg = "";
			for (int j = 0; j < randWords; j++) {
				msg += words[j] + " ";
			}
			logMessages.add(new LogRecord(levels[levelRandom.nextInt(levels.length)], msg));
		}
	}
	
	public class ValidatorMessageLabelAccumulator implements IConfigLabelAccumulator {
		public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
			LogRecord vm = logMessages.get(rowPosition);
			configLabels.addLabel(vm.getLevel().toString());
		}
	}
	
	public class ValidationMessageTableStyleConfiguration extends DefaultNatTableStyleConfiguration {
		private int IMAGE_SIZE= 16;
		private final Image ERROR_IMAGE = new Image(DISPLAY, DISPLAY.getSystemImage(SWT.ICON_ERROR)
				.getImageData().scaledTo(IMAGE_SIZE, IMAGE_SIZE));
		private final Image WARNING_IMAGE = new Image(DISPLAY, DISPLAY.getSystemImage(SWT.ICON_WARNING)
				.getImageData().scaledTo(IMAGE_SIZE, IMAGE_SIZE));
		private final Image INFORMATION_IMAGE = new Image(DISPLAY, DISPLAY.getSystemImage(SWT.ICON_INFORMATION)
				.getImageData().scaledTo(IMAGE_SIZE, IMAGE_SIZE));
		{
			hAlign = HorizontalAlignmentEnum.LEFT;
			cellPainter = new LineBorderDecorator(new PaddingDecorator(
					new CellPainterDecorator(
							new AutomaticRowHeightTextPainter(2), CellEdgeEnum.LEFT, new ImagePainter())
					, 0, 2, 0, 2));
		}

		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			super.configureRegistry(configRegistry);
			
			Style errorStyle = new Style();
			errorStyle.setAttributeValue(CellStyleAttributes.IMAGE, ERROR_IMAGE);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, errorStyle,
					DisplayMode.NORMAL, Level.SEVERE.toString());
			
			Style warningStyle = new Style();
			warningStyle.setAttributeValue(CellStyleAttributes.IMAGE, WARNING_IMAGE);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, warningStyle,
					DisplayMode.NORMAL, Level.WARNING.toString());
			
			Style informationStyle = new Style();
			informationStyle.setAttributeValue(CellStyleAttributes.IMAGE, INFORMATION_IMAGE);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, informationStyle,
					DisplayMode.NORMAL, Level.INFO.toString());
		}
	}
}

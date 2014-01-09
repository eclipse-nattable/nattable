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
package org.eclipse.nebula.widgets.nattable.examples._700_Integration;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonWithAddress;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.GridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.print.command.MultiTurnViewportOffCommandHandler;
import org.eclipse.nebula.widgets.nattable.print.command.MultiTurnViewportOnCommandHandler;
import org.eclipse.nebula.widgets.nattable.util.ClientAreaAdapter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.SliderScroller;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Slider;

/**
 * Example showing how to implement NatTable that contains two vertical split viewports.
 * 
 * @author Dirk Fauth
 *
 */
public class _704_VerticalSplitViewportExample extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 400, new _704_VerticalSplitViewportExample());
	}

	@Override
	public String getDescription() {
		return "This example shows a NatTable that contains two separately scrollable "
				+ "vertical split viewports.";
	}
	
	@Override
	public Control createExampleControl(Composite parent) {
		//property names of the Person class
		String[] propertyNames = {"firstName", "lastName", "gender", "married", "birthday", 
				"address.street", "address.housenumber", "address.postalCode", "address.city"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("firstName", "Firstname");
		propertyToLabelMap.put("lastName", "Lastname");
		propertyToLabelMap.put("gender", "Gender");
		propertyToLabelMap.put("married", "Married");
		propertyToLabelMap.put("birthday", "Birthday");
		propertyToLabelMap.put("address.street", "Street");
		propertyToLabelMap.put("address.housenumber", "Housenumber");
		propertyToLabelMap.put("address.postalCode", "Postal Code");
		propertyToLabelMap.put("address.city", "City");

		IColumnPropertyAccessor<PersonWithAddress> columnPropertyAccessor = 
				new ExtendedReflectiveColumnPropertyAccessor<PersonWithAddress>(propertyNames);
		
		IDataProvider bodyDataProvider = new ListDataProvider<PersonWithAddress>(
				PersonService.getPersonsWithAddress(50), columnPropertyAccessor);
		DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
		
		//use a cell layer painter that is configured for left clipping
		//this ensures that the rendering works correctly for split viewports
		bodyDataLayer.setLayerPainter(new GridLineCellLayerPainter(false, true));

		//create a ViewportLayer for the top part of the table and configure it to only contain 
		//the first 10 rows
		final ViewportLayer viewportLayerTop = new ViewportLayer(bodyDataLayer);
		viewportLayerTop.setMaxRowPosition(10);
		
		//create a ViewportLayer for the bottom part of the table and configure it to only contain 
		//the last rows
		ViewportLayer viewportLayerBottom = new ViewportLayer(bodyDataLayer);
		viewportLayerBottom.setMinRowPosition(10);

		//create a CompositeLayer that contains both ViewportLayers
		CompositeLayer compositeLayer = new CompositeLayer(1, 2);
		compositeLayer.setChildLayer("REGION_A", viewportLayerTop, 0, 0);
		compositeLayer.setChildLayer("REGION_B", viewportLayerBottom, 0, 1);

		//in order to make printing and exporting work correctly you need to register the following
		//command handlers
		//although in this example printing and exporting is not enabled, we show the registering
		compositeLayer.registerCommandHandler(new MultiTurnViewportOnCommandHandler(
				viewportLayerTop, viewportLayerBottom));
		compositeLayer.registerCommandHandler(new MultiTurnViewportOffCommandHandler(
				viewportLayerTop, viewportLayerBottom));

		//set the height of the top viewport to only showing 2 rows at the same time
		int topHeight = bodyDataLayer.getStartYOfRowPosition(2);
		
		//as the CompositeLayer is setting a IClientAreaProvider for the composition
		//we need to set a special ClientAreaAdapter after the creation of the CompositeLayer 
		//to support split viewports
		ClientAreaAdapter topClientAreaAdapter = new ClientAreaAdapter(viewportLayerTop.getClientAreaProvider());
		topClientAreaAdapter.setHeight(topHeight);
		viewportLayerTop.setClientAreaProvider(topClientAreaAdapter);
		
		// Wrap NatTable in composite so we can slap on the external vertical sliders
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);
		
		NatTable natTable = new NatTable(composite, compositeLayer);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		natTable.setLayoutData(gridData);

		createSplitSliders(composite, viewportLayerTop, viewportLayerBottom);
		
		//add an IOverlayPainter to ensure the right border of the left viewport always
		//this is necessary because the left border of layer stacks is not rendered by default
		natTable.addOverlayPainter(new IOverlayPainter() {
			
			@Override
			public void paintOverlay(GC gc, ILayer layer) {
				Color beforeColor = gc.getForeground();
				gc.setForeground(GUIHelper.COLOR_GRAY);
				int viewportBorderY = viewportLayerTop.getHeight() - 1;
				gc.drawLine(0, viewportBorderY, layer.getWidth()-1, viewportBorderY);
				gc.setForeground(beforeColor);
			}
		});
		
		return composite;
	}

	
	private void createSplitSliders(Composite natTableParent, final ViewportLayer top, final ViewportLayer bottom) {
		Composite sliderComposite = new Composite(natTableParent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = true;
		gridData.widthHint = 15;
		sliderComposite.setLayoutData(gridData);
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		sliderComposite.setLayout(gridLayout);
		
		// Slider Left
		// Need a composite here to set preferred size because Slider can't be subclassed.
		Composite sliderTopComposite = new Composite(sliderComposite, SWT.NONE) {
			@Override
			public Point computeSize(int wHint, int hHint, boolean changed) {
				int height = ((ClientAreaAdapter)top.getClientAreaProvider()).getHeight();
				return new Point(15, height);
			}
		};
		sliderTopComposite.setLayout(new FillLayout());
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.verticalAlignment = GridData.BEGINNING;
		sliderTopComposite.setLayoutData(gridData);
		
		Slider sliderTop = new Slider(sliderTopComposite, SWT.VERTICAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		sliderTop.setLayoutData(gridData);
		
		top.setVerticalScroller(new SliderScroller(sliderTop));
		
		// Slider Right
		Slider sliderBottom = new Slider(sliderComposite, SWT.VERTICAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = true;
		sliderBottom.setLayoutData(gridData);
		
		bottom.setVerticalScroller(new SliderScroller(sliderBottom));
	}
}

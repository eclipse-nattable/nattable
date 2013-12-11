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
package org.eclipse.nebula.widgets.nattable.examples._300_Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.VisualRefreshCommand;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.AutomaticSpanningDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.NumberValues;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.SpanningDataLayer;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Example to show the SpanningDataLayer for automatic spanning of equal shown data.
 * 
 * @author Dirk Fauth
 *
 */
public class _342_AutomaticDataSpanningExample extends AbstractNatExample {
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 300, new _342_AutomaticDataSpanningExample());
	}

	@Override
	public Control createExampleControl(Composite parent) {
		//property names of the NumberValues class
		String[] propertyNames = {"columnOneNumber", "columnTwoNumber", "columnThreeNumber", "columnFourNumber", 
				"columnFiveNumber"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("columnOneNumber", "Column 1");
		propertyToLabelMap.put("columnTwoNumber", "Column 2");
		propertyToLabelMap.put("columnThreeNumber", "Column 3");
		propertyToLabelMap.put("columnFourNumber", "Column 4");
		propertyToLabelMap.put("columnFiveNumber", "Column 5");

		IColumnPropertyAccessor<NumberValues> cpa = new ReflectiveColumnPropertyAccessor<NumberValues>(propertyNames);
		IDataProvider dataProvider = new ListDataProvider<NumberValues>(createNumberValueList(), cpa);
		AutomaticSpanningDataProvider spanningDataProvider = 
				new AutomaticSpanningDataProvider(dataProvider, true, false);
		
//		spanningDataProvider.addAutoSpanningColumnPositions(0, 1, 2);
//		spanningDataProvider.addAutoSpanningColumnPositions(2, 3, 4);
//		spanningDataProvider.addAutoSpanningColumnPositions(0, 1, 3, 4);
//		spanningDataProvider.addAutoSpanningRowPositions(0, 1, 2);
//		spanningDataProvider.addAutoSpanningRowPositions(2, 3, 4);
//		spanningDataProvider.addAutoSpanningRowPositions(0, 1, 3, 4);
		
		NatTable natTable = new NatTable(parent, 
				new ViewportLayer(new SelectionLayer(new SpanningDataLayer(spanningDataProvider))), false);
		
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

		natTable.addConfiguration(new BodyMenuConfiguration(natTable, spanningDataProvider));
		
		natTable.configure();
		
		natTable.registerCommandHandler(new DisplayPersistenceDialogCommandHandler());
		
		return natTable;
	}
	
	private List<NumberValues> createNumberValueList() {
		List<NumberValues> result = new ArrayList<NumberValues>();
		
		NumberValues nv = new NumberValues();
		nv.setColumnOneNumber(5);
		nv.setColumnTwoNumber(4);
		nv.setColumnThreeNumber(3);
		nv.setColumnFourNumber(1);
		nv.setColumnFiveNumber(1);
		result.add(nv);
		
		nv = new NumberValues();
		nv.setColumnOneNumber(1);
		nv.setColumnTwoNumber(1);
		nv.setColumnThreeNumber(2);
		nv.setColumnFourNumber(2);
		nv.setColumnFiveNumber(3);
		result.add(nv);
		
		nv = new NumberValues();
		nv.setColumnOneNumber(1);
		nv.setColumnTwoNumber(2);
		nv.setColumnThreeNumber(2);
		nv.setColumnFourNumber(3);
		nv.setColumnFiveNumber(3);
		result.add(nv);
		
		nv = new NumberValues();
		nv.setColumnOneNumber(1);
		nv.setColumnTwoNumber(2);
		nv.setColumnThreeNumber(4);
		nv.setColumnFourNumber(4);
		nv.setColumnFiveNumber(3);
		result.add(nv);
		
		nv = new NumberValues();
		nv.setColumnOneNumber(5);
		nv.setColumnTwoNumber(4);
		nv.setColumnThreeNumber(4);
		nv.setColumnFourNumber(4);
		nv.setColumnFiveNumber(7);
		result.add(nv);
		return result;
	}

	
	class BodyMenuConfiguration extends AbstractUiBindingConfiguration {
		private Menu bodyMenu;
		private AutomaticSpanningDataProvider dataProvider;
		
		public BodyMenuConfiguration(NatTable natTable, AutomaticSpanningDataProvider dataProvider) {
			this.bodyMenu = createBodyMenu(natTable).build();
			this.dataProvider = dataProvider;
			
			natTable.addDisposeListener(new DisposeListener() {

				@Override
				public void widgetDisposed(DisposeEvent e) {
					if (bodyMenu != null)
						bodyMenu.dispose();
				}

			});
		}

		protected PopupMenuBuilder createBodyMenu(final NatTable natTable) {
			return new PopupMenuBuilder(natTable)
					.withMenuItemProvider(new IMenuItemProvider() {
						@Override
						public void addMenuItem(final NatTable natTable, Menu popupMenu) {
							MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
							menuItem.setText("Toggle auto spanning");
							menuItem.setEnabled(true);

							menuItem.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent event) {
									if (dataProvider.isAutoColumnSpan()) {
										dataProvider.setAutoColumnSpan(false);
										dataProvider.setAutoRowSpan(true);
									}
									else if (dataProvider.isAutoRowSpan()) {
										dataProvider.setAutoRowSpan(false);
									}
									else {
										dataProvider.setAutoColumnSpan(true);
									}
									natTable.doCommand(new VisualRefreshCommand());
								}
							});
						}
					})
					.withStateManagerMenuItemProvider();
		}
		
		@Override
		public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
			if (this.bodyMenu != null) {
				uiBindingRegistry.registerMouseDownBinding(
						new MouseEventMatcher(SWT.NONE, null, MouseEventMatcher.RIGHT_BUTTON),
						new PopupMenuAction(this.bodyMenu));
			}
		}
	}
}

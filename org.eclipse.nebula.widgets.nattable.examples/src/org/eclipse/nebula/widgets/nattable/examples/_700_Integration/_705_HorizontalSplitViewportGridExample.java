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
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonWithAddress;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.CellLayerPainter;
import org.eclipse.nebula.widgets.nattable.print.command.MultiTurnViewportOffCommandHandler;
import org.eclipse.nebula.widgets.nattable.print.command.MultiTurnViewportOnCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.action.ColumnReorderDragMode;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayerPainter;
import org.eclipse.nebula.widgets.nattable.ui.action.AggregateDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.CellDragMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.util.ClientAreaAdapter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.SliderScroller;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Slider;

/**
 * Example showing how to implement NatTable that contains two horizontal split
 * viewports in a grid.
 * 
 * @author Dirk Fauth
 * 
 */
public class _705_HorizontalSplitViewportGridExample extends AbstractNatExample {

	public static final int SPLIT_COLUMN_INDEX = 5;

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 400,
				new _705_HorizontalSplitViewportGridExample());
	}

	@Override
	public String getDescription() {
		return "This example shows a NatTable that contains two separately scrollable "
				+ "horzizontal split viewports in a grid.";
	}

	@Override
	public Control createExampleControl(Composite parent) {
		// property names of the Person class
		String[] propertyNames = { "firstName", "lastName", "gender",
				"married", "birthday", "address.street", "address.housenumber",
				"address.postalCode", "address.city" };

		// mapping from property to label, needed for column header labels
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

		IColumnPropertyAccessor<PersonWithAddress> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<PersonWithAddress>(
				propertyNames);

		final BodyLayerStack<PersonWithAddress> bodyLayer = new BodyLayerStack<PersonWithAddress>(
				PersonService.getPersonsWithAddress(50), columnPropertyAccessor);
		
		// build the row header layer
		IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(
				bodyLayer.getBodyDataProvider());
		DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(
				rowHeaderDataProvider);
		final ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer,
				bodyLayer, bodyLayer.getSelectionLayer());

		// build the column header layer
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(
				propertyNames, propertyToLabelMap);
		final DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(
				columnHeaderDataProvider);
		final AbstractLayer columnHeaderLayer = new ColumnHeaderLayer(
				columnHeaderDataLayer, bodyLayer, bodyLayer.getSelectionLayer());
		
		//Use this special layer painter that supports rendering of split viewports although
		//the ColumnHeaderLayer is not split. Here is some custom calculation included that
		//might not work correctly in case there are column groups or other spanning involved.
		columnHeaderLayer.setLayerPainter(new CellLayerPainter() {
			@Override
			protected boolean isClipLeft(int position) {
				int index = LayerUtil.convertColumnPosition(columnHeaderLayer, position, columnHeaderDataLayer);
				return (index > SPLIT_COLUMN_INDEX);
			}
			
			@Override
			protected void paintCell(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
				ILayer layer = cell.getLayer();
				int columnPosition = cell.getColumnPosition();
				int rowPosition = cell.getRowPosition();
				ICellPainter cellPainter = layer.getCellPainter(columnPosition, rowPosition, cell, configRegistry);
				Rectangle adjustedCellBounds = layer.getLayerPainter().adjustCellBounds(columnPosition, rowPosition, cell.getBounds());
				if (cellPainter != null) {
					Rectangle originalClipping = gc.getClipping();
					
					int startX = getStartXOfColumnPosition(columnPosition);
					int startY = getStartYOfRowPosition(rowPosition);
					
					int endX = getStartXOfColumnPosition(cell.getOriginColumnPosition() + cell.getColumnSpan());
					int endY = getStartYOfRowPosition(cell.getOriginRowPosition() + cell.getRowSpan());

					//correct position of first column in right region
					//find the last visible column in left region
					int viewportBorderX = bodyLayer.getViewportLayerLeft().getClientAreaWidth() + rowHeaderLayer.getWidth();
					if (isClipLeft(columnPosition) && startX < viewportBorderX) {
						startX = viewportBorderX;
					}
					if (!isClipLeft(columnPosition-1) && startX > viewportBorderX) {
						startX = viewportBorderX;
					}
					if (isClipLeft(cell.getOriginColumnPosition() + cell.getColumnSpan()) && endX < viewportBorderX) {
						endX = viewportBorderX;
					}
					
					Rectangle cellClipBounds = originalClipping.intersection(new Rectangle(startX, startY, endX - startX, endY - startY));
					gc.setClipping(cellClipBounds.intersection(adjustedCellBounds));
					
					cellPainter.paintCell(cell, gc, adjustedCellBounds, configRegistry);
					
					gc.setClipping(originalClipping);
				}
			}
		});

		// build the corner layer
		IDataProvider cornerDataProvider = new DefaultCornerDataProvider(
				columnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		final ILayer cornerLayer = new CornerLayer(cornerDataLayer,
				rowHeaderLayer, columnHeaderLayer);

		// build the grid layer
		GridLayer gridLayer = new GridLayer(bodyLayer, columnHeaderLayer,
				rowHeaderLayer, cornerLayer);

		//in order to make printing and exporting work correctly you need to register the following
		//command handlers
		gridLayer.registerCommandHandler(new MultiTurnViewportOnCommandHandler(
				bodyLayer.getViewportLayerLeft(), bodyLayer.getViewportLayerRight()));
		gridLayer.registerCommandHandler(new MultiTurnViewportOffCommandHandler(
				bodyLayer.getViewportLayerLeft(), bodyLayer.getViewportLayerRight()));

		// Wrap NatTable in composite so we can slap on the external horizontal
		// sliders
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);

		NatTable natTable = new NatTable(composite, gridLayer, false);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		natTable.setLayoutData(gridData);

		createSplitSliders(composite, rowHeaderLayer,
				bodyLayer.getViewportLayerLeft(),
				bodyLayer.getViewportLayerRight());

		// add an IOverlayPainter to ensure the right border of the left
		// viewport always
		// this is necessary because the left border of layer stacks is not
		// rendered by default
		natTable.addOverlayPainter(new IOverlayPainter() {

			@Override
			public void paintOverlay(GC gc, ILayer layer) {
				Color beforeColor = gc.getForeground();
				gc.setForeground(GUIHelper.COLOR_GRAY);
				int viewportBorderX = bodyLayer.getViewportLayerLeft()
						.getWidth() + rowHeaderLayer.getWidth() - 1;
				gc.drawLine(viewportBorderX, 0, viewportBorderX,
						layer.getHeight() - 1);
				gc.setForeground(beforeColor);
			}
		});

		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
		natTable.configure();

		return composite;
	}

	private void createSplitSliders(Composite natTableParent,
			final ILayer rowHeaderLayer, final ViewportLayer left,
			final ViewportLayer right) {
		Composite sliderComposite = new Composite(natTableParent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.heightHint = 15;
		sliderComposite.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		sliderComposite.setLayout(gridLayout);

		// Slider Left
		// Need a composite here to set preferred size because Slider can't be subclassed.
		Composite sliderLeftComposite = new Composite(sliderComposite, SWT.NONE) {
			@Override
			public Point computeSize(int wHint, int hHint, boolean changed) {
				int width = ((ClientAreaAdapter) left.getClientAreaProvider()).getWidth();
				width += rowHeaderLayer.getWidth();
				return new Point(width, 15);
			}
		};
		sliderLeftComposite.setLayout(new FillLayout());
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.verticalAlignment = GridData.BEGINNING;
		sliderLeftComposite.setLayoutData(gridData);

		Slider sliderLeft = new Slider(sliderLeftComposite, SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		sliderLeft.setLayoutData(gridData);

		left.setHorizontalScroller(new SliderScroller(sliderLeft));

		// Slider Right
		Slider sliderRight = new Slider(sliderComposite, SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		sliderRight.setLayoutData(gridData);

		right.setHorizontalScroller(new SliderScroller(sliderRight));
	}

	/**
	 * The body layer stack that supports column hide/show, column reordering,
	 * selection and split viewports.
	 */
	class BodyLayerStack<T> extends AbstractLayerTransform {

		private final IDataProvider bodyDataProvider;
		private final ColumnHideShowLayer columnHideShowLayer;
		private final SelectionLayer selectionLayer;
		private final ViewportLayer viewportLayerLeft;
		private final ViewportLayer viewportLayerRight;

		public BodyLayerStack(List<T> values, IColumnPropertyAccessor<T> columnPropertyAccessor) {
			this.bodyDataProvider = new ListDataProvider<T>(values, columnPropertyAccessor);
			DataLayer bodyDataLayer = new DataLayer(getBodyDataProvider());

			// use our custom reorder drag mode configuration instead of the default to
			// suppress the ability to move a column from one viewport to the other
			ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(bodyDataLayer, false);
			columnReorderLayer.addConfiguration(new AbstractUiBindingConfiguration() {

				@Override
				public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
					uiBindingRegistry.registerMouseDragMode(MouseEventMatcher.columnHeaderLeftClick(SWT.NONE),
							new AggregateDragMode(new CellDragMode(), new SplitViewportColumnReorderDragMode()));
				}
			});

			this.columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
			this.selectionLayer = new SelectionLayer(columnHideShowLayer);

			// use a cell layer painter that is configured for left clipping
			// this ensures that the rendering works correctly for split
			// viewports
			this.selectionLayer.setLayerPainter(new SelectionLayerPainter(true,	false));

			// create a ViewportLayer for the left part of the table and
			// configure it to only contain
			// the first 5 columns
			viewportLayerLeft = new ViewportLayer(this.selectionLayer) {
				@Override
				public int getMaxColumnPosition() {
					return getNumberOfLeftColumns();
				}
			};

			// create a ViewportLayer for the right part of the table and
			// configure it to only contain
			// the last 4 columns
			viewportLayerRight = new ViewportLayer(this.selectionLayer) {
				@Override
				public int getMinColumnPosition() {
					return getNumberOfLeftColumns();
				}
			};
			// as the min column position is calculated dynamically we need to
			// set the minimum origin manually
			int newMinOriginX = this.selectionLayer.getStartXOfColumnPosition(getNumberOfLeftColumns());
			viewportLayerRight.setMinimumOriginX(newMinOriginX);
			
			// create a CompositeLayer that contains both ViewportLayers
			CompositeLayer compositeLayer = new CompositeLayer(2, 1);
			compositeLayer.setChildLayer("REGION_A", getViewportLayerLeft(), 0,	0);
			compositeLayer.setChildLayer("REGION_B", getViewportLayerRight(), 1, 0);
			
			// set the width of the left viewport to only showing 2 columns at
			// the same time
			int leftWidth = bodyDataLayer.getStartXOfColumnPosition(2);

			// as the CompositeLayer is setting a IClientAreaProvider for the composition
			// we need to set a special ClientAreaAdapter after the creation of the CompositeLayer
			// to support split viewports
			ClientAreaAdapter leftClientAreaAdapter = new ClientAreaAdapter(getViewportLayerLeft().getClientAreaProvider());
			leftClientAreaAdapter.setWidth(leftWidth);
			getViewportLayerLeft().setClientAreaProvider(leftClientAreaAdapter);

			// register configuration to avoid reordering of columns between the split viewports
			registerCommandHandler(new SplitViewportColumnReorderCommandHandler(getViewportLayerLeft()));
			
			setUnderlyingLayer(compositeLayer);
		}

		/**
		 * To support hide/show correctly the min/max column positions in the
		 * split viewports need to be calculated regarding the hide state.
		 * 
		 * @return The number of visible columns in the left viewport.
		 */
		private int getNumberOfLeftColumns() {
			int fixedColumns = SPLIT_COLUMN_INDEX;
			for (int i = 0; i < (SPLIT_COLUMN_INDEX); i++) {
				if (this.columnHideShowLayer.isColumnIndexHidden(i)) {
					fixedColumns--;
				}
			}
			return fixedColumns;
		}

		public IDataProvider getBodyDataProvider() {
			return bodyDataProvider;
		}

		public SelectionLayer getSelectionLayer() {
			return selectionLayer;
		}

		public ViewportLayer getViewportLayerLeft() {
			return viewportLayerLeft;
		}

		public ViewportLayer getViewportLayerRight() {
			return viewportLayerRight;
		}
	}

	/**
	 * ILayerCommandHandler for ColumnReorderCommands that ensures that no
	 * reordering can be performed programmatically or via dialog, that would
	 * reorder columns between the split viewports.
	 */
	class SplitViewportColumnReorderCommandHandler extends
			AbstractLayerCommandHandler<ColumnReorderCommand> {

		private ViewportLayer viewportLeft;

		public SplitViewportColumnReorderCommandHandler(ViewportLayer viewportLeft) {
			this.viewportLeft = viewportLeft;
		}

		@Override
		protected boolean doCommand(ColumnReorderCommand command) {
			if ((command.getFromColumnPosition() < this.viewportLeft.getColumnCount()) 
					!= (command.getToColumnPosition() < this.viewportLeft.getColumnCount())) {
				// Bail out if trying to reorder from region A to B or vice versa
				return true;
			}
			return false;
		}

		@Override
		public Class<ColumnReorderCommand> getCommandClass() {
			return ColumnReorderCommand.class;
		}

	}

	/**
	 * Reorder drag mode that avoids reordering between split viewports via
	 * dragging.
	 */
	class SplitViewportColumnReorderDragMode extends ColumnReorderDragMode {

		@Override
		protected boolean isValidTargetColumnPosition(ILayer natLayer,
				int dragFromGridColumnPosition, int dragToGridColumnPosition) {

			if (((NatTable) natLayer).getCursor() == null) {
				int fromColumnIndex = natLayer
						.getColumnIndexByPosition(dragFromGridColumnPosition);
				int toColumnIndex = natLayer
						.getColumnIndexByPosition(dragToGridColumnPosition);

				// ensure that dragging over split viewport borders is not
				// allowed
				if ((fromColumnIndex < SPLIT_COLUMN_INDEX && toColumnIndex < SPLIT_COLUMN_INDEX)
						|| (fromColumnIndex >= (SPLIT_COLUMN_INDEX) && toColumnIndex >= (SPLIT_COLUMN_INDEX))) {

					return super.isValidTargetColumnPosition(natLayer,
							dragFromGridColumnPosition,
							dragToGridColumnPosition);
				}
			}

			return false;
		}
	}
}

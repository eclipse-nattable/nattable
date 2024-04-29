/*******************************************************************************
 * Copyright (c) 2015, 2024 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._504_Viewport;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.CellLayerPainter;
import org.eclipse.nebula.widgets.nattable.resize.action.VerticalResizeCursorAction;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayerPainter;
import org.eclipse.nebula.widgets.nattable.ui.action.ClearCursorAction;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.ClientAreaAdapter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.SliderScroller;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.nebula.widgets.nattable.viewport.command.RecalculateScrollBarsCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
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
 *
 */
public class _5045_ScrollableRowHeaderExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400, new _5045_ScrollableRowHeaderExample());
    }

    @Override
    public String getDescription() {
        return "This example shows a scrollable row header.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender", "married", "birthday" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");

        IColumnPropertyAccessor<Person> columnPropertyAccessor =
                new ReflectiveColumnPropertyAccessor<>(propertyNames);

        IDataProvider bodyDataProvider =
                new ListDataProvider<>(PersonService.getPersons(10), columnPropertyAccessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        DataLayer rowDataLayer = new DataLayer(new IDataProvider() {

            @Override
            public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            }

            @Override
            public int getRowCount() {
                return bodyDataProvider.getRowCount();
            }

            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                return "Lorem ipsum dolor";
            }

            @Override
            public int getColumnCount() {
                return 1;
            }
        });
        rowDataLayer.setDefaultColumnWidth(150);
        ViewportLayer rowViewport = new ViewportLayer(rowDataLayer);
        RowHeaderLayer scrollableRowHeaderLayer = new RowHeaderLayer(rowViewport, viewportLayer, selectionLayer);

        RowHeaderLayer rowHeaderLayer =
                new RowHeaderLayer(
                        new DefaultRowHeaderDataLayer(
                                new DefaultRowHeaderDataProvider(bodyDataProvider)),
                        viewportLayer,
                        selectionLayer);

        CompositeLayer compositeLayer = new CompositeLayer(2, 1);
        compositeLayer.setChildLayer(GridRegion.ROW_HEADER, rowHeaderLayer, 0, 0);
        compositeLayer.setChildLayer(GridRegion.ROW_HEADER, scrollableRowHeaderLayer, 1, 0);

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        final DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        final AbstractLayer columnHeaderLayer =
                new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer);

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(columnHeaderDataProvider, rowDataLayer.getDataProvider());
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        final ILayer cornerLayer =
                new CornerLayer(cornerDataLayer, compositeLayer, columnHeaderLayer);

        GridLayer gridLayer = new GridLayer(
                viewportLayer, columnHeaderLayer, compositeLayer, cornerLayer);

        // MULTI-VIEWPORT-CONFIGURATION

        // use a cell layer painter that is configured for left clipping
        // this ensures that the rendering works correctly for split
        // viewports
        selectionLayer.setLayerPainter(new SelectionLayerPainter(true, false));

        columnHeaderLayer.setLayerPainter(new CellLayerPainter(true, false));

        // as the CompositeLayer is setting a IClientAreaProvider for the
        // composition we need to set a special ClientAreaAdapter after the
        // creation of the CompositeLayer to support split viewports
        int leftWidth = 100;
        ClientAreaAdapter leftClientAreaAdapter =
                new ClientAreaAdapter(rowViewport.getClientAreaProvider());
        leftClientAreaAdapter.setWidth(leftWidth);
        rowViewport.setClientAreaProvider(leftClientAreaAdapter);

        // Wrap NatTable in composite so we can slap on the external horizontal
        // sliders
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        composite.setLayout(gridLayout);

        NatTable natTable = new NatTable(composite, gridLayer);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        natTable.setLayoutData(gridData);

        createSplitSliders(composite, rowViewport, rowHeaderLayer.getWidth(), viewportLayer);

        // add an IOverlayPainter to render the split viewport border
        natTable.addOverlayPainter(new IOverlayPainter() {

            @Override
            public void paintOverlay(GC gc, ILayer layer) {
                Color beforeColor = gc.getForeground();
                gc.setForeground(GUIHelper.COLOR_GRAY);
                int viewportBorderX = compositeLayer.getWidth() - 1;
                gc.drawLine(viewportBorderX, 0, viewportBorderX, layer.getHeight() - 1);
                gc.setForeground(beforeColor);
            }
        });

        // Mouse move - Show resize cursor
        natTable.getUiBindingRegistry().registerFirstMouseMoveBinding(
                new ClientAreaResizeMatcher(compositeLayer),
                new VerticalResizeCursorAction(),
                new ClearCursorAction());

        natTable.getUiBindingRegistry().registerFirstMouseDragMode(
                new ClientAreaResizeMatcher(compositeLayer),
                new ClientAreaResizeDragMode(compositeLayer, rowDataLayer, leftClientAreaAdapter, rowViewport, viewportLayer));

        return natTable;
    }

    private void createSplitSliders(
            Composite natTableParent, final ViewportLayer left, int fixedHeaderWidth, final ViewportLayer right) {
        Composite sliderComposite = new Composite(natTableParent, SWT.NONE);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = false;
        gridData.heightHint = 17;
        sliderComposite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        sliderComposite.setLayout(gridLayout);

        // Slider Left
        // Need a composite here to set preferred size because Slider can't be
        // subclassed.
        Composite sliderLeftComposite = new Composite(sliderComposite, SWT.NONE) {
            @Override
            public Point computeSize(int wHint, int hHint, boolean changed) {
                int width = ((ClientAreaAdapter) left.getClientAreaProvider()).getWidth() + fixedHeaderWidth;
                return new Point(width, 17);
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

    class ClientAreaResizeMatcher extends MouseEventMatcher {

        ILayer rowHeaderLayer;

        public ClientAreaResizeMatcher(ILayer rowHeaderLayer) {
            this.rowHeaderLayer = rowHeaderLayer;
        }

        @Override
        public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
            int adjust = GUIHelper.convertHorizontalPixelToDpi(5);
            if (regionLabels != null && regionLabels.hasLabel(GridRegion.COLUMN_HEADER)
                    && (event.x > this.rowHeaderLayer.getWidth() - adjust && event.x < this.rowHeaderLayer.getWidth() + adjust)) {
                return true;
            }
            return false;
        }
    }

    class ClientAreaResizeDragMode implements IDragMode {

        ILayer baseLayer;
        ClientAreaAdapter clientAreaAdapter;
        ViewportLayer[] viewportLayer;

        int diff = 0;

        public ClientAreaResizeDragMode(ILayer resizable, ILayer baseLayer,
                ClientAreaAdapter clientAreaAdapter, ViewportLayer... viewportLayer) {
            this.baseLayer = baseLayer;
            this.clientAreaAdapter = clientAreaAdapter;
            this.viewportLayer = viewportLayer;
            this.diff = resizable.getWidth() - clientAreaAdapter.getWidth();
        }

        @Override
        public void mouseDown(NatTable natTable, MouseEvent event) {
        }

        @Override
        public void mouseMove(NatTable natTable, MouseEvent event) {
            // TODO overlay support
        }

        @Override
        public void mouseUp(NatTable natTable, MouseEvent event) {
            int newWidth = event.x - this.diff;
            if (newWidth < 0) {
                newWidth = 1;
            } else if (newWidth > this.baseLayer.getWidth()) {
                newWidth = this.baseLayer.getWidth();
            }
            this.clientAreaAdapter.setWidth(newWidth);
            for (ViewportLayer vp : this.viewportLayer) {
                vp.invalidateHorizontalStructure();

                vp.doCommand(new RecalculateScrollBarsCommand());
            }
            natTable.redraw();

            natTable.getParent().layout(true, true);
        }

    }
}

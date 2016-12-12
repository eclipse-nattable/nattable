/*******************************************************************************
 * Copyright (c) 2016 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._504_Viewport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonWithAddress;
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
import org.eclipse.nebula.widgets.nattable.grid.layer.DimensionallyDependentIndexLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.CellLayerPainter;
import org.eclipse.nebula.widgets.nattable.resize.action.VerticalResizeCursorAction;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.command.ClearAllSelectionsCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectAllCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.ClientAreaAdapter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.SliderScroller;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.nebula.widgets.nattable.viewport.command.RecalculateScrollBarsCommand;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectRowCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
public class _5046_MultiScrollExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400, new _5046_MultiScrollExample());
    }

    @Override
    public String getDescription() {
        return "This example shows multiple scrollable regions.";
    }

    @Override
    public Control createExampleControl(Composite parent) {

        List<PersonWithAddress> values = PersonService.getPersonsWithAddress(10);

        ContentBodyLayerStack contentBodyLayer = new ContentBodyLayerStack(values);
        StructureBodyLayerStack structureBodyLayer = new StructureBodyLayerStack(values, contentBodyLayer);

        // build the column header layer
        IDataProvider contentHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(contentBodyLayer.propertyNames, contentBodyLayer.propertyToLabelMap);
        DataLayer contentHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(contentHeaderDataProvider);
        AbstractLayer contentColumnHeaderLayer =
                new ColumnHeaderLayer(contentHeaderDataLayer, contentBodyLayer, contentBodyLayer.getSelectionLayer());

        IDataProvider structureHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(structureBodyLayer.propertyNames, structureBodyLayer.propertyToLabelMap);
        DataLayer structureHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(structureHeaderDataProvider);
        ColumnHeaderLayer structureColumnHeaderLayer =
                new ColumnHeaderLayer(structureHeaderDataLayer, structureBodyLayer, structureBodyLayer.selectionLayer);
        structureColumnHeaderLayer.setVerticalLayerDependency(contentColumnHeaderLayer);

        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(new DefaultRowHeaderDataProvider(contentBodyLayer.bodyDataProvider));
        ILayer rowHeaderLayer =
                new RowHeaderLayer(rowHeaderDataLayer, contentBodyLayer, (SelectionLayer) null);

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(contentHeaderDataProvider, rowHeaderDataLayer.getDataProvider());
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(cornerDataLayer, rowHeaderLayer, contentColumnHeaderLayer);

        ExtendedGridLayer gridLayer = new ExtendedGridLayer(
                contentBodyLayer, contentColumnHeaderLayer,
                structureBodyLayer, structureColumnHeaderLayer,
                rowHeaderLayer, cornerLayer);

        // MULTI-VIEWPORT-CONFIGURATION

        // as the CompositeLayer is setting a IClientAreaProvider for the
        // composition we need to set a special ClientAreaAdapter after the
        // creation of the CompositeLayer to support split viewports
        int leftWidth = 80;
        ClientAreaAdapter leftClientAreaAdapter =
                new ClientAreaAdapter(structureBodyLayer.getViewportLayer().getClientAreaProvider());
        leftClientAreaAdapter.setWidth(leftWidth);
        structureBodyLayer.getViewportLayer().setClientAreaProvider(leftClientAreaAdapter);

        structureBodyLayer.getViewportLayer().setVerticalScrollbarEnabled(false);

        // use a cell layer painter that is configured for left clipping
        // this ensures that the rendering works correctly for split
        // viewports
        contentBodyLayer.getSelectionLayer().setLayerPainter(new SelectionLayerPainter(true, false));

        contentColumnHeaderLayer.setLayerPainter(new CellLayerPainter(true, false));

        ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);

        // Wrap NatTable in composite so we can slap on the external horizontal
        // sliders
        Composite composite = new Composite(sc, SWT.NONE);
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

        createSplitSliders(composite, gridLayer, rowHeaderLayer.getWidth());

        sc.setContent(composite);

        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        updateScrolledCompositeSize(sc, gridLayer);

        // add an IOverlayPainter to render the split viewport border
        natTable.addOverlayPainter(new IOverlayPainter() {

            @Override
            public void paintOverlay(GC gc, ILayer layer) {
                Color beforeColor = gc.getForeground();
                gc.setForeground(GUIHelper.COLOR_GRAY);
                int viewportBorderX = rowHeaderLayer.getWidth() + gridLayer.getStructureBody().getWidth() - 1;
                gc.drawLine(viewportBorderX, 0, viewportBorderX, layer.getHeight() - 1);
                gc.setForeground(beforeColor);
            }
        });

        // Mouse move - Show resize cursor
        natTable.getUiBindingRegistry().registerFirstMouseMoveBinding(
                new ClientAreaResizeMatcher(gridLayer),
                new VerticalResizeCursorAction());

        natTable.getUiBindingRegistry().registerFirstMouseDragMode(
                new ClientAreaResizeMatcher(gridLayer),
                new ClientAreaResizeDragMode(gridLayer, sc));

        return natTable;
    }

    /**
     *
     * @param sc
     * @param gridLayer
     */
    private void updateScrolledCompositeSize(ScrolledComposite sc, ExtendedGridLayer gridLayer) {
        sc.setMinSize(
                gridLayer.getRowHeaderLayer().getWidth() + gridLayer.getStructureBody().getWidth() + 100,
                gridLayer.getContentHeader().getHeight() + 20);
    }

    /**
     *
     * @param natTableParent
     * @param gridLayer
     * @param fixedHeaderWidth
     */
    private void createSplitSliders(
            Composite natTableParent, ExtendedGridLayer gridLayer, int fixedHeaderWidth) {
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
                int width = ((ClientAreaAdapter) gridLayer.getStructureBody().getViewportLayer().getClientAreaProvider()).getWidth() + fixedHeaderWidth;
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

        gridLayer.getStructureBody().getViewportLayer().setHorizontalScroller(new SliderScroller(sliderLeft));

        // Slider Right
        Slider sliderRight = new Slider(sliderComposite, SWT.HORIZONTAL);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.BEGINNING;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = false;
        sliderRight.setLayoutData(gridData);

        gridLayer.getContentBody().getViewportLayer().setHorizontalScroller(new SliderScroller(sliderRight));
    }

    /**
     *
     */
    class ExtendedGridLayer extends CompositeLayer {

        public ExtendedGridLayer(
                ContentBodyLayerStack contentBodyLayer, ILayer contentColumnHeaderLayer,
                StructureBodyLayerStack structureBodyLayer, ILayer structureColumnHeaderLayer,
                ILayer rowHeaderLayer, ILayer cornerLayer) {

            super(3, 2);

            setChildLayer(GridRegion.CORNER, cornerLayer, 0, 0);
            setChildLayer(GridRegion.ROW_HEADER, rowHeaderLayer, 0, 1);
            setChildLayer(GridRegion.COLUMN_HEADER, structureColumnHeaderLayer, 1, 0);
            setChildLayer(GridRegion.BODY, structureBodyLayer, 1, 1);
            setChildLayer(GridRegion.COLUMN_HEADER, contentColumnHeaderLayer, 2, 0);
            setChildLayer(GridRegion.BODY, contentBodyLayer, 2, 1);
        }

        public StructureBodyLayerStack getStructureBody() {
            return (StructureBodyLayerStack) getChildLayerByLayoutCoordinate(1, 1);
        }

        public ContentBodyLayerStack getContentBody() {
            return (ContentBodyLayerStack) getChildLayerByLayoutCoordinate(2, 1);
        }

        public ILayer getStructureHeader() {
            return getChildLayerByLayoutCoordinate(1, 0);
        }

        public ILayer getContentHeader() {
            return getChildLayerByLayoutCoordinate(2, 0);
        }

        public ILayer getRowHeaderLayer() {
            return getChildLayerByLayoutCoordinate(0, 1);
        }

        @Override
        protected boolean doCommandOnChildLayers(ILayerCommand command) {
            if (doCommandOnChildLayer(command, getContentBody())) {
                return true;
            } else if (doCommandOnChildLayer(command, getStructureBody())) {
                return true;
            } else if (doCommandOnChildLayer(command, getContentHeader())) {
                return true;
            } else if (doCommandOnChildLayer(command, getStructureHeader())) {
                return true;
            } else if (doCommandOnChildLayer(command, getRowHeaderLayer())) {
                return true;
            } else {
                return doCommandOnChildLayer(command, getChildLayerByLayoutCoordinate(0, 0));
            }
        }

        private boolean doCommandOnChildLayer(ILayerCommand command, ILayer childLayer) {
            ILayerCommand childCommand = command.cloneCommand();
            return childLayer.doCommand(childCommand);
        }

        @Override
        public boolean doCommand(ILayerCommand command) {
            if (command instanceof ViewportSelectRowCommand
                    || command instanceof ClearAllSelectionsCommand
                    || command instanceof SelectAllCommand) {
                doCommandOnChildLayer(command, getContentBody());
                doCommandOnChildLayer(command, getStructureBody());
                return true;
            } else if (command instanceof SelectCellCommand) {
                int layout = getLayoutXByColumnPosition(((SelectCellCommand) command).getColumnPosition());
                if (layout == 2) {
                    doCommandOnChildLayer(new ClearAllSelectionsCommand(), getStructureBody());
                } else {
                    doCommandOnChildLayer(new ClearAllSelectionsCommand(), getContentBody());
                }
            } else if (command instanceof ViewportSelectColumnCommand) {
                int layout = getLayoutXByColumnPosition(((ViewportSelectColumnCommand) command).getColumnPosition());
                if (layout == 2) {
                    doCommandOnChildLayer(new ClearAllSelectionsCommand(), getStructureBody());
                } else {
                    doCommandOnChildLayer(new ClearAllSelectionsCommand(), getContentBody());
                }
            }
            return super.doCommand(command);
        }
    }

    /**
     *
     */
    class ContentBodyLayerStack extends AbstractIndexLayerTransform {

        // property names of the Person class
        public final String[] propertyNames = { "firstName", "lastName", "gender", "married", "birthday" };

        // mapping from property to label, needed for column header labels
        public final Map<String, String> propertyToLabelMap = new HashMap<>();

        private final IDataProvider bodyDataProvider;
        private final DataLayer bodyDataLayer;
        private final SelectionLayer selectionLayer;
        private final ViewportLayer viewportLayer;

        public ContentBodyLayerStack(List<PersonWithAddress> values) {
            this.propertyToLabelMap.put("firstName", "Firstname");
            this.propertyToLabelMap.put("lastName", "Lastname");
            this.propertyToLabelMap.put("gender", "Gender");
            this.propertyToLabelMap.put("married", "Married");
            this.propertyToLabelMap.put("birthday", "Birthday");

            IColumnPropertyAccessor<PersonWithAddress> columnPropertyAccessor =
                    new ReflectiveColumnPropertyAccessor<>(this.propertyNames);

            this.bodyDataProvider = new ListDataProvider<>(values, columnPropertyAccessor);
            this.bodyDataLayer = new DataLayer(this.bodyDataProvider);
            this.selectionLayer = new SelectionLayer(this.bodyDataLayer);
            this.viewportLayer = new ViewportLayer(this.selectionLayer);

            setUnderlyingLayer(this.viewportLayer);
        }

        public ViewportLayer getViewportLayer() {
            return this.viewportLayer;
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }
    }

    /**
     *
     */
    class StructureBodyLayerStack extends AbstractIndexLayerTransform {

        // property names of the Address class
        public final String[] propertyNames = { "address.street", "address.housenumber", "address.postalCode", "address.city" };

        // mapping from property to label, needed for column header labels
        public final Map<String, String> propertyToLabelMap = new HashMap<>();

        private final IDataProvider bodyDataProvider;
        private final DataLayer bodyDataLayer;
        private final SelectionLayer selectionLayer;
        private final ViewportLayer viewportLayer;

        public StructureBodyLayerStack(List<PersonWithAddress> values, IUniqueIndexLayer verticalDependency) {
            this.propertyToLabelMap.put("address.street", "Street");
            this.propertyToLabelMap.put("address.housenumber", "Housenumber");
            this.propertyToLabelMap.put("address.postalCode", "Postal Code");
            this.propertyToLabelMap.put("address.city", "City");

            IColumnPropertyAccessor<PersonWithAddress> columnPropertyAccessor =
                    new ExtendedReflectiveColumnPropertyAccessor<>(this.propertyNames);

            this.bodyDataProvider = new ListDataProvider<>(values, columnPropertyAccessor);
            this.bodyDataLayer = new DataLayer(this.bodyDataProvider);
            this.selectionLayer = new SelectionLayer(this.bodyDataLayer);
            this.viewportLayer = new ViewportLayer(this.selectionLayer);

            setUnderlyingLayer(new DimensionallyDependentIndexLayer(this.viewportLayer, this.viewportLayer, verticalDependency));
        }

        public ViewportLayer getViewportLayer() {
            return this.viewportLayer;
        }
    }

    /**
     *
     */
    class ClientAreaResizeMatcher extends MouseEventMatcher {

        ExtendedGridLayer gridLayer;

        public ClientAreaResizeMatcher(ExtendedGridLayer gridLayer) {
            this.gridLayer = gridLayer;
        }

        @Override
        public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
            int viewportBorderX = this.gridLayer.getRowHeaderLayer().getWidth() + this.gridLayer.getStructureBody().getWidth();
            if (regionLabels != null && regionLabels.hasLabel(GridRegion.COLUMN_HEADER)
                    && (event.x >= viewportBorderX && event.x <= viewportBorderX + 4)) {
                return true;
            }
            return false;
        }
    }

    /**
     *
     */
    class ClientAreaResizeDragMode implements IDragMode {

        ExtendedGridLayer gridLayer;
        ScrolledComposite sc;

        public ClientAreaResizeDragMode(ExtendedGridLayer gridLayer, ScrolledComposite sc) {
            this.gridLayer = gridLayer;
            this.sc = sc;
        }

        @Override
        public void mouseDown(NatTable natTable, MouseEvent event) {}

        @Override
        public void mouseMove(NatTable natTable, MouseEvent event) {
            // TODO overlay support
        }

        @Override
        public void mouseUp(NatTable natTable, MouseEvent event) {
            int baseWidth = this.gridLayer.getStructureBody().bodyDataLayer.getWidth();
            int newWidth = event.x - this.gridLayer.getRowHeaderLayer().getWidth();
            if (newWidth < 0) {
                newWidth = 1;
            } else if (newWidth > baseWidth) {
                newWidth = baseWidth;
            }
            ((ClientAreaAdapter) this.gridLayer.getStructureBody().getViewportLayer().getClientAreaProvider()).setWidth(newWidth);

            this.gridLayer.getStructureBody().getViewportLayer().invalidateHorizontalStructure();
            this.gridLayer.getStructureBody().getViewportLayer().doCommand(new RecalculateScrollBarsCommand());

            this.gridLayer.getContentBody().getViewportLayer().invalidateHorizontalStructure();
            this.gridLayer.getContentBody().getViewportLayer().doCommand(new RecalculateScrollBarsCommand());

            natTable.redraw();
            natTable.getParent().layout(true, true);

            updateScrolledCompositeSize(this.sc, this.gridLayer);
        }

    }
}

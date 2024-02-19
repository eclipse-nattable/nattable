/*******************************************************************************
 * Copyright (c) 2024 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._508_Reorder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.AbstractLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.reorder.action.RowReorderDragMode;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiRowReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.action.SelectRowAction;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultRowSelectionLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.action.AggregateDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.ClearCursorAction;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.action.RowDragMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Example that shows how to implement row reordering that changes the
 * underlying data structure instead of a visual transformation. Additionally it
 * shows how to configure the handlers and the stack in a composition of a
 * composition that only has a column header and a body region.
 */
public class _5084_StructuralRowReorderWithoutRowHeaderExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(800, 400, new _5084_StructuralRowReorderWithoutRowHeaderExample());
    }

    @Override
    public String getDescription() {
        return "This example shows how to implement row reordering that changes the"
                + " underlying data structure instead of a visual transformation like the RowReorderLayer."
                + " Additionally it shows how to configure the handlers and the stack in a composition of a"
                + " composition that only has a column header and a body region.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // property names of the Person class
        String[] propertyNames = {
                "firstName",
                "lastName",
                "gender",
                "married",
                "birthday" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");

        // build the body layer stack
        // Usually you would create a new layer stack by extending
        // AbstractIndexLayerTransform and setting the ViewportLayer as
        // underlying layer. But in this case using the ViewportLayer directly
        // as body layer is also working.
        List<Person> contents = PersonService.getPersons(10);
        IRowDataProvider<Person> bodyDataProvider =
                new DefaultBodyDataProvider<>(contents, propertyNames);
        DataLayer bodyDataLayer =
                new DataLayer(bodyDataProvider);

        // add a config label accumulator, so we can change the rendering per
        // column
        bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

        // create a SelectionLayer without the default configuration to be able
        // to enable row only selection behavior
        SelectionLayer selectionLayer =
                new SelectionLayer(bodyDataLayer, false);

        // add the custom configuration to enable the structural row reordering
        bodyDataLayer.addConfiguration(new StructuralRowReorderConfiguration(selectionLayer));

        // use a RowSelectionModel that will perform row selections and is able
        // to identify a row via unique ID
        selectionLayer.setSelectionModel(new RowSelectionModel<Person>(
                selectionLayer, bodyDataProvider, new IRowIdAccessor<Person>() {

                    @Override
                    public Serializable getRowId(Person rowObject) {
                        return rowObject.getId();
                    }

                }));

        // register the DefaultRowSelectionLayerConfiguration that contains the
        // default styling and functionality bindings (search, tick update)
        // and different configurations for a move command handler that always
        // moves by a row and row only selection bindings
        selectionLayer.addConfiguration(new DefaultRowSelectionLayerConfiguration());

        ViewportLayer viewportLayer =
                new ViewportLayer(selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer);

        // set the region labels to make default configurations work, e.g.
        // selection
        CompositeLayer compositeLayer = new CompositeLayer(1, 2);
        compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER, columnHeaderLayer, 0, 0);
        compositeLayer.setChildLayer(GridRegion.BODY, viewportLayer, 0, 1);

        NatTable natTable = new NatTable(parent, compositeLayer);
        return natTable;
    }

    /**
     * Custom configuration to enable structural row reordering.
     */
    class StructuralRowReorderConfiguration extends AbstractLayerConfiguration<DataLayer> {

        private ImagePainter dragHandlePainter =
                new ImagePainter(GUIHelper.getImage("semichecked"));
        private ICellPainter cellPainter =
                new CellPainterDecorator(
                        new TextPainter(),
                        CellEdgeEnum.LEFT,
                        this.dragHandlePainter);

        private final SelectionLayer selectionLayer;

        public StructuralRowReorderConfiguration(SelectionLayer selectionLayer) {
            this.selectionLayer = selectionLayer;
        }

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {
            // configure a custom painter on the first column to simulate a
            // "drag handle"
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    this.cellPainter,
                    DisplayMode.NORMAL,
                    ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + "0");
        }

        @Override
        public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {

            // Move mouse over "drag handle" and show different cursor
            uiBindingRegistry.registerFirstMouseMoveBinding(
                    new CellPainterMouseEventMatcher(
                            GridRegion.BODY,
                            0,
                            this.dragHandlePainter),
                    new HandCursorAction());
            uiBindingRegistry.registerMouseMoveBinding(
                    new MouseEventMatcher(),
                    new ClearCursorAction());

            // Override the mouse down binding to avoid a row selection if
            // multiple rows are already selected and the mouse is down on one
            // of these selected rows. Otherwise a already existing
            // multi selection would be changed to a single selection.
            uiBindingRegistry.registerFirstMouseDownBinding(
                    new CellPainterMouseEventMatcher(
                            GridRegion.BODY,
                            0,
                            this.dragHandlePainter) {

                        @Override
                        public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
                            // if we have a mouse down on the drag handle and
                            // currently multiple rows are selected, we trigger
                            // the NoOpMouseAction to avoid a single row
                            // selection
                            if (super.matches(natTable, event, regionLabels)) {
                                int rowPosition = natTable.getRowPositionByY(event.y);
                                return isRowPartOfMultiselection(natTable, rowPosition);
                            }
                            return false;
                        }
                    },
                    new NoOpMouseAction());

            // register drag mode binding on left click on the "drag handle" in
            // the first column
            uiBindingRegistry.registerFirstMouseDragMode(
                    new CellPainterMouseEventMatcher(
                            GridRegion.BODY,
                            MouseEventMatcher.LEFT_BUTTON,
                            this.dragHandlePainter),
                    new AggregateDragMode(new RowDragMode(), new MultiRowReorderDragMode(this.selectionLayer)));

            // register drag mode binding on right click in any cell of the body
            uiBindingRegistry.registerMouseDragMode(
                    MouseEventMatcher.bodyRightClick(SWT.NONE),
                    new AggregateDragMode(new RowDragMode(), new MultiRowReorderDragMode(this.selectionLayer)));

            // register binding to select a row on right mouse down
            // simply to select the row so it is highlighted for the reorder
            // overlay painting
            uiBindingRegistry.registerFirstMouseDownBinding(
                    MouseEventMatcher.bodyRightClick(SWT.NONE), new SelectRowAction() {
                        @Override
                        public void run(NatTable natTable, MouseEvent event) {
                            if (!isRowPartOfMultiselection(natTable, natTable.getRowPositionByY(event.y))) {
                                super.run(natTable, event);
                            }
                        }
                    });
        }

        private boolean isRowPartOfMultiselection(NatTable natTable, int rowPosition) {
            if (this.selectionLayer.getSelectedRowCount() > 1) {

                // convert row position in grid to SelectionLayer
                int selectionLayerRowPos = LayerUtil.convertRowPosition(natTable, rowPosition, this.selectionLayer);

                // only match if the position is already selected
                return this.selectionLayer.getSelectedRowPositions()
                        .stream()
                        .anyMatch(r -> r.contains(selectionLayerRowPos));
            }
            return false;
        }

        @Override
        public void configureTypedLayer(DataLayer layer) {
            // Register custom command handler for the row reorder commands
            // directly on the body DataLayer. As we want to change the data
            // structure instead of only a visual transformation, we need to be
            // on the lowest level of the stack.

            StructuralMultiRowReorderCommandHandler<Person> multiReorderCommandHandler =
                    new StructuralMultiRowReorderCommandHandler<Person>(layer);

            layer.registerCommandHandler(multiReorderCommandHandler);
        }
    }

    /**
     * Action to show a hand cursor. Used when moving the mouse of the "drag
     * handle".
     */
    class HandCursorAction implements IMouseAction {

        private Cursor handCursor;

        @Override
        public void run(NatTable natTable, MouseEvent event) {
            if (this.handCursor == null) {
                this.handCursor = new Cursor(Display.getDefault(), SWT.CURSOR_HAND);

                natTable.addDisposeListener(e -> HandCursorAction.this.handCursor.dispose());
            }
            natTable.setCursor(this.handCursor);
        }
    }

    /*
     * Note: The example only supports row reordering via drag & drop. If
     * programmatic reordering via RowReorderCommand should be supported, there
     * also needs to be a custom StructuralRowReorderCommandHandler.
     */

    /**
     * Extended {@link RowReorderDragMode} that supports multi row reordering.
     */
    class MultiRowReorderDragMode extends RowReorderDragMode {

        private SelectionLayer selectionLayer;
        private int[] selectedRowPositions;

        public MultiRowReorderDragMode(SelectionLayer selectionLayer) {
            super();
            this.selectionLayer = selectionLayer;
        }

        @Override
        public void mouseDown(NatTable natTable, MouseEvent event) {
            this.natTable = natTable;
            this.initialEvent = event;
            this.currentEvent = this.initialEvent;
            this.dragFromGridRowPosition = getDragFromGridRowPosition();

            this.selectedRowPositions = PositionUtil.getPositions(this.selectionLayer.getSelectedRowPositions());

            natTable.addOverlayPainter(this.targetOverlayPainter);

            // natTable.doCommand(new ClearAllSelectionsCommand());
        }

        @Override
        protected int getDragFromGridRowPosition() {
            int rowPosition = super.getDragFromGridRowPosition();
            // convert to SelectionLayer
            return LayerUtil.convertRowPosition(this.natTable, rowPosition, this.selectionLayer);
        }

        @Override
        protected int getDragToGridRowPosition(CellEdgeEnum moveDirection, int gridRowPosition) {
            int dragToGridRowPosition = -1;

            if (moveDirection != null) {
                dragToGridRowPosition = LayerUtil.convertRowPosition(this.natTable, gridRowPosition, this.selectionLayer);
                if (moveDirection == CellEdgeEnum.BOTTOM) {
                    dragToGridRowPosition += 1;
                }
            }

            return dragToGridRowPosition;
        }

        /**
         *
         * @param natLayer
         *            The layer the positions are related to
         * @param dragFromGridRowPosition
         *            The row position of the row that is dragged
         * @param dragToGridRowPosition
         *            The row position where the row is dropped
         * @return <code>true</code> if the drop position is valid,
         *         <code>false</code> if not
         */
        @Override
        protected boolean isValidTargetRowPosition(ILayer natLayer, int dragFromGridRowPosition, int dragToGridRowPosition) {
            return dragFromGridRowPosition >= 0
                    && dragToGridRowPosition >= 0
                    && (dragToGridRowPosition <= this.selectedRowPositions[0]
                            || dragToGridRowPosition >= this.selectedRowPositions[this.selectedRowPositions.length - 1]);
        }

        /**
         * Executes the command to indicate row reorder ending.
         *
         * @param natTable
         *            The NatTable instance on which the command should be
         *            executed
         * @param dragToGridRowPosition
         *            The position of the row to which the dragged row should be
         *            dropped
         */
        @Override
        protected void fireMoveEndCommand(NatTable natTable, int dragToGridRowPosition) {
            natTable.doCommand(new MultiRowReorderCommand(this.selectionLayer, this.selectedRowPositions, dragToGridRowPosition));
        }
    }

    /**
     * Command handler for the {@link MultiRowReorderCommand} triggered by
     * {@link MultiRowReorderDragMode#mouseUp(NatTable, org.eclipse.swt.events.MouseEvent)}.
     * Performs the structural reordering of elements in the data list.
     */
    class StructuralMultiRowReorderCommandHandler<T> extends AbstractLayerCommandHandler<MultiRowReorderCommand> {

        private final List<T> data;
        private final DataLayer dataLayer;

        @SuppressWarnings("unchecked")
        public StructuralMultiRowReorderCommandHandler(DataLayer dataLayer) {

            this.dataLayer = dataLayer;

            if (dataLayer.getDataProvider() instanceof ListDataProvider) {
                this.data = ((ListDataProvider<T>) dataLayer.getDataProvider()).getList();
            } else {
                throw new IllegalArgumentException("IDataProvider is not of type ListDataProvider");
            }
        }

        public StructuralMultiRowReorderCommandHandler(
                List<T> data,
                DataLayer dataLayer) {

            this.dataLayer = dataLayer;
            this.data = data;
        }

        @Override
        protected boolean doCommand(MultiRowReorderCommand command) {
            int toRowPosition = command.getToRowPosition();
            boolean reorderToTopEdge = command.isReorderToTopEdge();

            if (!reorderToTopEdge) {
                toRowPosition++;
            }

            int[] fromRowPositions = command.getFromRowPositionsArray();

            if (fromRowPositions[fromRowPositions.length - 1] < toRowPosition) {
                toRowPosition -= fromRowPositions.length;
            }

            List<T> remove = new ArrayList<>();
            for (int i : fromRowPositions) {
                remove.add(this.data.get(i));
            }

            this.data.removeAll(remove);
            this.data.addAll(toRowPosition, remove);

            this.dataLayer.fireLayerEvent(new StructuralRefreshEvent(this.dataLayer));

            return true;
        }

        @Override
        public Class<MultiRowReorderCommand> getCommandClass() {
            return MultiRowReorderCommand.class;
        }
    }
}

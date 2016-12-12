/*******************************************************************************
 * Copyright (c) 2014, 2015 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *    Janos Binder <janos.binder@openchrom.net> - position is stored
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._700_AdditionalFunctions;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.NatTableBorderOverlayPainter;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _781_DragAndDropExample extends AbstractNatExample {

    private NatTable firstNatTable;
    private NatTable secondNatTable;

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400, new _781_DragAndDropExample());
    }

    @Override
    public String getDescription() {
        return "This example shows how add Drag&Drop support to NatTable instances.\n"
                + "You can drag rows from one NatTable instance to the other.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, true));

        // create two data lists with consecutive id's
        List<Person> data = PersonService.getPersons(20);
        List<Person> subData1 = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Person p = data.get(i);
            subData1.add(p);
        }
        List<Person> subData2 = new ArrayList<>();
        for (int i = 10; i < 20; i++) {
            Person p = data.get(i);
            subData2.add(p);
        }

        this.firstNatTable = createTable(container, subData1);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(this.firstNatTable);

        this.secondNatTable = createTable(container, subData2);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(this.secondNatTable);

        return container;
    }

    private NatTable createTable(Composite parent, List<Person> data) {
        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");

        IColumnPropertyAccessor<Person> columnPropertyAccessor =
                new ReflectiveColumnPropertyAccessor<>(propertyNames);

        IRowDataProvider<Person> bodyDataProvider =
                new ListDataProvider<>(data, columnPropertyAccessor);
        final DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        ColumnReorderLayer reorderLayer = new ColumnReorderLayer(bodyDataLayer);
        final SelectionLayer selectionLayer = new SelectionLayer(reorderLayer);

        // set row selection model with single selection enabled
        selectionLayer.setSelectionModel(new RowSelectionModel<>(
                selectionLayer,
                bodyDataProvider,
                new IRowIdAccessor<Person>() {

                    @Override
                    public Serializable getRowId(Person rowObject) {
                        return rowObject.getId();
                    }

                },
                false));

        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(
                        new DataLayer(
                                new DummyColumnHeaderDataProvider(bodyDataProvider)),
                        viewportLayer,
                        selectionLayer);

        CompositeLayer compositeLayer = new CompositeLayer(1, 2);
        compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER, columnHeaderLayer, 0, 0);
        compositeLayer.setChildLayer(GridRegion.BODY, viewportLayer, 0, 1);

        NatTable natTable = new NatTable(parent, compositeLayer);

        // add DnD support
        DragAndDropSupport dndSupport =
                new DragAndDropSupport(natTable, selectionLayer, data);
        Transfer[] transfer = { TextTransfer.getInstance() };
        natTable.addDragSupport(DND.DROP_COPY, transfer, dndSupport);
        natTable.addDropSupport(DND.DROP_COPY, transfer, dndSupport);

        // adding a full border
        natTable.addOverlayPainter(
                new NatTableBorderOverlayPainter(natTable.getConfigRegistry()));

        return natTable;
    }

    class DragAndDropSupport implements DragSourceListener, DropTargetListener {

        private final NatTable natTable;
        private final SelectionLayer selectionLayer;
        private final List<Person> data;

        private Person draggedPerson;

        private static final String DATA_SEPARATOR = "|";
        private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        public DragAndDropSupport(NatTable natTable, SelectionLayer selectionLayer, List<Person> data) {
            this.natTable = natTable;
            this.selectionLayer = selectionLayer;
            this.data = data;
        }

        @Override
        public void dragStart(DragSourceEvent event) {
            if (this.selectionLayer.getSelectedRowCount() == 0) {
                event.doit = false;
            } else if (!this.natTable.getRegionLabelsByXY(event.x, event.y).hasLabel(GridRegion.BODY)) {
                event.doit = false;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void dragSetData(DragSourceEvent event) {
            // we know that we use the RowSelectionModel with single selection
            List<Person> selection = ((RowSelectionModel<Person>) this.selectionLayer.getSelectionModel()).getSelectedRowObjects();

            if (!selection.isEmpty()) {
                this.draggedPerson = selection.get(0);
                StringBuilder builder = new StringBuilder();
                builder.append(this.draggedPerson.getId())
                        .append(DATA_SEPARATOR)
                        .append(this.draggedPerson.getFirstName())
                        .append(DATA_SEPARATOR)
                        .append(this.draggedPerson.getLastName())
                        .append(DATA_SEPARATOR)
                        .append(this.draggedPerson.getGender())
                        .append(DATA_SEPARATOR)
                        .append(this.draggedPerson.isMarried())
                        .append(DATA_SEPARATOR)
                        .append(this.sdf.format(this.draggedPerson.getBirthday()));
                event.data = builder.toString();
            }
        }

        @Override
        public void dragFinished(DragSourceEvent event) {
            this.data.remove(this.draggedPerson);
            this.draggedPerson = null;

            // clear selection
            this.selectionLayer.clear();

            this.natTable.refresh();
        }

        @Override
        public void dragEnter(DropTargetEvent event) {
            event.detail = DND.DROP_COPY;
        }

        @Override
        public void dragLeave(DropTargetEvent event) {}

        @Override
        public void dragOperationChanged(DropTargetEvent event) {}

        @Override
        public void dragOver(DropTargetEvent event) {}

        @Override
        public void drop(DropTargetEvent event) {
            String[] data = (event.data != null ? event.data.toString().split(
                    "\\" + DATA_SEPARATOR) : new String[] {});
            if (data.length > 0) {
                Person p = new Person(Integer.valueOf(data[0]));
                p.setFirstName(data[1]);
                p.setLastName(data[2]);
                p.setGender(Gender.valueOf(data[3]));
                p.setMarried(Boolean.valueOf(data[4]));
                try {
                    p.setBirthday(this.sdf.parse(data[5]));
                } catch (ParseException e) {}

                int rowPosition = getRowPosition(event);
                if (rowPosition > 0) {
                    this.data.add(rowPosition - 1, p);
                } else {
                    this.data.add(p);
                }
                this.natTable.refresh();
            }
        }

        @Override
        public void dropAccept(DropTargetEvent event) {}

        private int getRowPosition(DropTargetEvent event) {
            Point pt = event.display.map(null, this.natTable, event.x, event.y);
            int position = this.natTable.getRowPositionByY(pt.y);
            return position;
        }
    }
}

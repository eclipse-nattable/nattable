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
package org.eclipse.nebula.widgets.nattable.examples._600_GlazedLists._603_Filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonWithAddress;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.nebula.richtext.MarkupDisplayConverter;
import org.eclipse.nebula.widgets.nattable.extension.nebula.richtext.RegexMarkupValue;
import org.eclipse.nebula.widgets.nattable.extension.nebula.richtext.RichTextCellPainter;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.NatTableBorderOverlayPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

public class _6036_SingleFieldFilterExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(1000, 600, new _6036_SingleFieldFilterExample());
    }

    @Override
    public String getDescription() {
        return "This example shows how to create a table with a single input field filter, "
                + "similar to full-text-search engines on the web. It uses a TextMatcherEditor "
                + "on a FilterList for filtering and the RichTextCellPainter for highlighting "
                + "the search value in the table content.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout());

        Text input = new Text(panel, SWT.SINGLE | SWT.SEARCH | SWT.ICON_CANCEL);
        input.setMessage("type filter text");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(input);

        // property names of the Person class
        String[] propertyNames = {
                "firstName", "lastName", "gender", "married", "birthday",
                "address.street", "address.housenumber", "address.postalCode", "address.city" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
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
                new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);

        BodyLayerStack<PersonWithAddress> bodyLayerStack =
                new BodyLayerStack<>(
                        PersonService.getPersonsWithAddress(10000),
                        columnPropertyAccessor);

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(
                        columnHeaderDataLayer,
                        bodyLayerStack,
                        (SelectionLayer) null);

        CompositeLayer composite = new CompositeLayer(1, 2);
        composite.setChildLayer(GridRegion.COLUMN_HEADER, columnHeaderLayer, 0, 0);
        composite.setChildLayer(GridRegion.BODY, bodyLayerStack, 0, 1);

        RegexMarkupValue regexMarkup = new RegexMarkupValue("",
                "<span style=\"background-color:rgb(255, 255, 0)\">",
                "</span>");

        NatTable natTable = new NatTable(panel, composite, false);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration() {
            {
                this.cellPainter = new BackgroundPainter(
                        new PaddingDecorator(new RichTextCellPainter(), 2));
            }

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                super.configureRegistry(configRegistry);

                // markup for highlighting
                MarkupDisplayConverter markupConverter = new MarkupDisplayConverter();
                markupConverter.registerMarkup("highlight", regexMarkup);
                // register markup display converter for normal displaymode in
                // the body
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.DISPLAY_CONVERTER,
                        markupConverter,
                        DisplayMode.NORMAL,
                        GridRegion.BODY);
            }
        });
        natTable.configure();

        natTable.addOverlayPainter(new NatTableBorderOverlayPainter());

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // define a TextMatcherEditor and set it to the FilterList
        TextMatcherEditor<PersonWithAddress> matcherEditor = new TextMatcherEditor<>(new TextFilterator<PersonWithAddress>() {

            @Override
            public void getFilterStrings(List<String> baseList, PersonWithAddress element) {
                // add all values that should be included in filtering
                // Note:
                // if special converters are involved in rendering,
                // consider using them for adding the String values
                baseList.add(element.getFirstName());
                baseList.add(element.getLastName());
                baseList.add("" + element.getGender());
                baseList.add("" + element.isMarried());
                baseList.add("" + element.getBirthday());
                baseList.add(element.getAddress().getStreet());
                baseList.add("" + element.getAddress().getHousenumber());
                baseList.add("" + element.getAddress().getPostalCode());
                baseList.add(element.getAddress().getCity());
            }
        });
        matcherEditor.setMode(TextMatcherEditor.CONTAINS);
        bodyLayerStack.getFilterList().setMatcherEditor(matcherEditor);

        // connect the input field with the matcher
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
                    String text = input.getText();
                    matcherEditor.setFilterText(new String[] { text });
                    regexMarkup.setRegexValue(text.isEmpty() ? "" : "(" + text + ")");
                    natTable.refresh(false);
                }
            }
        });

        return natTable;
    }

    /**
     * The body layer stack.
     *
     * @param <T>
     */
    class BodyLayerStack<T> extends AbstractLayerTransform {

        private final FilterList<T> filterList;

        public BodyLayerStack(List<T> values, IColumnPropertyAccessor<T> columnPropertyAccessor) {
            EventList<T> eventList = GlazedLists.eventList(values);
            TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);

            this.filterList = new FilterList<>(rowObjectsGlazedList);

            IDataProvider bodyDataProvider = new ListDataProvider<>(this.filterList, columnPropertyAccessor);
            DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

            bodyDataLayer.setColumnWidthByPosition(2, 70);
            bodyDataLayer.setColumnWidthByPosition(3, 70);
            bodyDataLayer.setColumnWidthByPosition(4, 200);
            bodyDataLayer.setColumnWidthByPosition(8, 110);

            GlazedListsEventLayer<T> glazedListsEventLayer =
                    new GlazedListsEventLayer<>(bodyDataLayer, this.filterList);

            ViewportLayer viewportLayer = new ViewportLayer(glazedListsEventLayer);

            setUnderlyingLayer(viewportLayer);
        }

        public FilterList<T> getFilterList() {
            return this.filterList;
        }

    }
}

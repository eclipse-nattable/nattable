/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.e4.part;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.EditableRule;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.validate.DataValidator;
import org.eclipse.nebula.widgets.nattable.dataset.person.ExtendedPersonWithAddress;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditBindings;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditConfiguration;
import org.eclipse.nebula.widgets.nattable.examples.e4.AbstractE4NatExamplePart;
import org.eclipse.nebula.widgets.nattable.extension.e4.css.CSSConfigureScalingCommandHandler;
import org.eclipse.nebula.widgets.nattable.extension.e4.painterfactory.CellPainterFactory;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CustomLineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.ui.scaling.ScalingUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

@SuppressWarnings("restriction")
public class CSSExample extends AbstractE4NatExamplePart {

    @Inject
    EMenuService menuService;

    @PostConstruct
    public void postConstruct(Composite parent, Shell shell) {
        parent.setLayout(new GridLayout());

        // property names of the Person class
        String[] propertyNames = {
                "firstName",
                "lastName",
                "password",
                "description",
                "age",
                "money",
                "married",
                "gender",
                "address.street",
                "address.city",
                "favouriteFood",
                "favouriteDrinks" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("password", "Password");
        propertyToLabelMap.put("description", "Description");
        propertyToLabelMap.put("age", "Age");
        propertyToLabelMap.put("money", "Money");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("address.street", "Street");
        propertyToLabelMap.put("address.city", "City");
        propertyToLabelMap.put("favouriteFood", "Food");
        propertyToLabelMap.put("favouriteDrinks", "Drinks");

        IDataProvider bodyDataProvider =
                new ListDataProvider<>(
                        PersonService.getExtendedPersonsWithAddress(10),
                        new ExtendedReflectiveColumnPropertyAccessor<ExtendedPersonWithAddress>(propertyNames));

        DefaultGridLayer gridLayer =
                new DefaultGridLayer(bodyDataProvider,
                        new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap));
        // unregister the default registered commandhandler to make the fill
        // handle work here for the age column
        gridLayer.unregisterCommandHandler(CopyDataToClipboardCommand.class);
        gridLayer.getBodyLayer().unregisterCommandHandler(CopyDataToClipboardCommand.class);

        final DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();

        AggregateConfigLabelAccumulator accumulator = new AggregateConfigLabelAccumulator();
        // create the ColumnLabelAccumulator with IDataProvider to be able to
        // tell the CSS engine about the added labels
        accumulator.add(new ColumnLabelAccumulator(bodyDataProvider));
        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
        columnLabelAccumulator.registerColumnOverrides(5, CustomLineBorderDecorator.RIGHT_LINE_BORDER_LABEL);

        accumulator.add(columnLabelAccumulator);
        bodyDataLayer.setConfigLabelAccumulator(accumulator);

        NatTable natTable = new NatTable(parent, gridLayer, false);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new FillHandleConfiguration(gridLayer.getBodyLayer().getSelectionLayer()));

        gridLayer.addConfiguration(new DefaultEditConfiguration());
        gridLayer.addConfiguration(new DefaultEditBindings());

        natTable.addConfiguration(new ScalingUiBindingConfiguration(natTable));

        natTable.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITABLE_RULE,
                        EditableRule.ALWAYS_EDITABLE,
                        DisplayMode.NORMAL,
                        "COLUMN_4");

                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.DATA_VALIDATOR,
                        new DataValidator() {

                            @Override
                            public boolean validate(int columnIndex, int rowIndex, Object newValue) {
                                if (newValue instanceof Integer && ((Integer) newValue).intValue() > 100) {
                                    return false;
                                }
                                return true;
                            }
                        },
                        DisplayMode.NORMAL,
                        "COLUMN_4");
            }
        });

        natTable.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "basic");

        natTable.configure();

        natTable.registerCommandHandler(new CSSConfigureScalingCommandHandler(natTable));

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // add a custom painter for key errortext
        int[] yErrorOffsets = { 0, 1, 2, 1 };
        CellPainterFactory.getInstance().registerContentPainter("errortext", (properties, underlying) -> {
            return new TextPainter(true, true, false) {
                @Override
                protected void paintDecoration(
                        IStyle cellStyle, GC gc, int x, int y, int length, int fontHeight) {
                    int underlineY = y + fontHeight - (gc.getFontMetrics().getDescent() / 2);

                    Color previousColor = gc.getForeground();
                    gc.setForeground(GUIHelper.COLOR_RED);
                    int startX = x;
                    underlineY--;
                    int index = 0;
                    while (startX <= (x + length)) {
                        gc.drawPoint(startX, underlineY + yErrorOffsets[(index % 4)]);
                        index++;
                        startX++;
                    }
                    gc.setForeground(previousColor);
                }
            };
        });

        showSourceLinks(parent, getClass().getName());
    }

}
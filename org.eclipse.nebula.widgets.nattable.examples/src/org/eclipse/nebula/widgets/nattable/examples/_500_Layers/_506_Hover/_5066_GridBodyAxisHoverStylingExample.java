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
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._506_Hover;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.config.ColumnHeaderHoverLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.hover.config.RowHeaderHoverLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnVisualUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowVisualUpdateEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.theme.IThemeExtension;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Example that shows how to add the {@link HoverLayer} to a grid layer
 * composition. This example adds options to enabled additional row/column
 * highlighting and reflection in the headers to visualized the hover in the
 * body region.
 */
public class _5066_GridBodyAxisHoverStylingExample extends AbstractNatExample {

    public static final String AXIS_HOVER_LABEL = "AXIS_HOVER";
    public static final String HEADER_HOVER_HIGHLIGHT_LABEL = "HEADER_HOVER_HIGHLIGHT";

    private boolean headerHoverHighlightEnabled = false;

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400,
                new _5066_GridBodyAxisHoverStylingExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the HoverLayer to highlight a row while hovering over the body region.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

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
        IDataProvider bodyDataProvider =
                new DefaultBodyDataProvider<>(
                        PersonService.getPersons(10),
                        propertyNames);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        HoverLayer hoverLayer = new HoverLayer(bodyDataLayer);
        hoverLayer.setFireRowUpdates(true);
        SelectionLayer selectionLayer = new SelectionLayer(hoverLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(
                        propertyNames,
                        propertyToLabelMap);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ColumnHeaderLayer columnHeaderLayer =
                new ColumnHeaderLayer(
                        columnHeaderDataLayer,
                        viewportLayer,
                        selectionLayer,
                        false);

        // add ColumnHeaderHoverLayerConfiguration to ensure that hover styling
        // and resizing is working together
        columnHeaderLayer.addConfiguration(
                new ColumnHeaderHoverLayerConfiguration(null));

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyDataProvider);
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        RowHeaderLayer rowHeaderLayer =
                new RowHeaderLayer(
                        rowHeaderDataLayer,
                        viewportLayer,
                        selectionLayer,
                        false);

        // add RowHeaderHoverLayerConfiguration to ensure that hover styling and
        // resizing is working together
        rowHeaderLayer.addConfiguration(
                new RowHeaderHoverLayerConfiguration(null));

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(
                        columnHeaderDataProvider,
                        rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(
                        cornerDataLayer,
                        rowHeaderLayer,
                        columnHeaderLayer);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(
                        viewportLayer,
                        columnHeaderLayer,
                        rowHeaderLayer,
                        cornerLayer);

        // turn the auto configuration off as we want to add our header menu
        // configuration
        NatTable natTable = new NatTable(container, gridLayer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // add a label to the whole row or the whole column if a cell in that
        // row or column is currently hovered
        hoverLayer.setConfigLabelAccumulator(new IConfigLabelAccumulator() {

            @Override
            public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
                boolean rowHover = hoverLayer.isFireRowUpdates() && hoverLayer.isRowPositionHovered(rowPosition);
                boolean columnHover = hoverLayer.isFireColumnUpdates() && hoverLayer.isColumnPositionHovered(columnPosition);
                if (rowHover || columnHover) {
                    configLabels.add(AXIS_HOVER_LABEL);
                }
            }
        });

        // add a label to the row or column header cell if a cell in that row or
        // column is currently hovered
        rowHeaderLayer.setConfigLabelAccumulator(new IConfigLabelAccumulator() {

            @Override
            public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
                if (_5066_GridBodyAxisHoverStylingExample.this.headerHoverHighlightEnabled) {
                    int position = LayerUtil.convertRowPosition(rowHeaderLayer, rowPosition, hoverLayer);
                    if (hoverLayer.isRowPositionHovered(position)) {
                        configLabels.add(HEADER_HOVER_HIGHLIGHT_LABEL);
                    }
                }
            }
        });
        columnHeaderLayer.setConfigLabelAccumulator(new IConfigLabelAccumulator() {

            @Override
            public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
                if (_5066_GridBodyAxisHoverStylingExample.this.headerHoverHighlightEnabled) {
                    int position = LayerUtil.convertColumnPosition(columnHeaderLayer, columnPosition, hoverLayer);
                    if (hoverLayer.isColumnPositionHovered(position)) {
                        configLabels.add(HEADER_HOVER_HIGHLIGHT_LABEL);
                    }
                }
            }
        });

        // we need to add a listener to the NatTable to also refresh the headers
        // in case of visual refresh events
        natTable.addLayerListener(new ILayerListener() {

            @Override
            public void handleLayerEvent(ILayerEvent event) {
                if (_5066_GridBodyAxisHoverStylingExample.this.headerHoverHighlightEnabled
                        && (event instanceof CellVisualUpdateEvent
                                || event instanceof RowVisualUpdateEvent
                                || event instanceof ColumnVisualUpdateEvent)) {

                    // repaint the column and row header additionally
                    natTable.repaintRow(0);
                    natTable.repaintColumn(0);
                }
            }
        });

        ModernNatTableThemeConfiguration theme = new ModernNatTableThemeConfiguration();
        theme.addThemeExtension(new IThemeExtension() {

            @Override
            public void registerStyles(IConfigRegistry configRegistry) {
                Style hoverStyle = new Style();
                hoverStyle.setAttributeValue(
                        CellStyleAttributes.BACKGROUND_COLOR,
                        GUIHelper.getColor(51, 196, 255));

                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        hoverStyle,
                        DisplayMode.HOVER);

                Style axisHoverStyle = new Style();
                axisHoverStyle.setAttributeValue(
                        CellStyleAttributes.BACKGROUND_COLOR,
                        GUIHelper.getColor(196, 238, 254));

                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        axisHoverStyle,
                        DisplayMode.NORMAL,
                        AXIS_HOVER_LABEL);

                Style headerHoverHighlightStyle = new Style();
                headerHoverHighlightStyle.setAttributeValue(
                        CellStyleAttributes.BACKGROUND_COLOR,
                        GUIHelper.COLOR_DARK_GRAY);
                headerHoverHighlightStyle.setAttributeValue(
                        CellStyleAttributes.FOREGROUND_COLOR,
                        GUIHelper.COLOR_WHITE);

                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        headerHoverHighlightStyle,
                        DisplayMode.NORMAL,
                        HEADER_HOVER_HIGHLIGHT_LABEL);
            }

            @Override
            public void unregisterStyles(IConfigRegistry configRegistry) {
                configRegistry.unregisterConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        DisplayMode.HOVER);

                configRegistry.unregisterConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        DisplayMode.NORMAL,
                        AXIS_HOVER_LABEL);

                configRegistry.unregisterConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        DisplayMode.NORMAL,
                        HEADER_HOVER_HIGHLIGHT_LABEL);
            }
        });
        natTable.setTheme(theme);

        Composite buttonPanel = new Composite(container, SWT.NONE);
        buttonPanel.setLayout(new RowLayout());

        Button rowHoverButton = new Button(buttonPanel, SWT.PUSH);
        rowHoverButton.setText("Enable Row Hover");
        rowHoverButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                hoverLayer.setFireRowUpdates(true);
                hoverLayer.setFireColumnUpdates(false);
            }
        });

        Button columnHoverButton = new Button(buttonPanel, SWT.PUSH);
        columnHoverButton.setText("Enable Column Hover");
        columnHoverButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                hoverLayer.setFireRowUpdates(false);
                hoverLayer.setFireColumnUpdates(true);
            }
        });

        Button allHoverButton = new Button(buttonPanel, SWT.PUSH);
        allHoverButton.setText("Enable Table Hover");
        allHoverButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                hoverLayer.setFireRowUpdates(true);
                hoverLayer.setFireColumnUpdates(true);
            }
        });

        Button cellHoverButton = new Button(buttonPanel, SWT.PUSH);
        cellHoverButton.setText("Enable Cell Hover");
        cellHoverButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                hoverLayer.setFireRowUpdates(false);
                hoverLayer.setFireColumnUpdates(false);
            }
        });

        Button enableHeaderHoverHighlightButton = new Button(buttonPanel, SWT.PUSH);
        enableHeaderHoverHighlightButton.setText("Enable Header Hover Highlight");
        enableHeaderHoverHighlightButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!_5066_GridBodyAxisHoverStylingExample.this.headerHoverHighlightEnabled) {
                    _5066_GridBodyAxisHoverStylingExample.this.headerHoverHighlightEnabled = true;
                    enableHeaderHoverHighlightButton.setText("Disable Header Hover Highlight");
                } else {
                    _5066_GridBodyAxisHoverStylingExample.this.headerHoverHighlightEnabled = false;
                    enableHeaderHoverHighlightButton.setText("Enable Header Hover Highlight");
                }
            }
        });

        return container;
    }

}

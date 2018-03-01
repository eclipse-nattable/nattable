/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.examples._102_Configuration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.SelectionExampleGridLayer;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.ButtonCellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.TextDecorationEnum;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellLabelMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.DebugMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Rendering_cells_as_a_link_and_button extends AbstractNatExample {
    public static final String LINK_CELL_LABEL = "LINK_CELL_LABEL";
    public static final String BUTTON_CELL_LABEL = "BUTTON_CELL_LABEL";

    private ButtonCellPainter buttonPainter;
    private SelectionExampleGridLayer gridLayer;

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400, new Rendering_cells_as_a_link_and_button());
    }

    @Override
    public String getDescription() {
        return "Demonstrates rendering the cell as a button and as a link. Custom actions can be triggered on button/link click.\n"
                + "\n"
                + "Note: The button is 'drawn' using a custom painter. This is more efficient than using a Button widget.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        this.gridLayer = new SelectionExampleGridLayer();
        NatTable natTable = new NatTable(parent, this.gridLayer, false);
        IConfigRegistry configRegistry = new ConfigRegistry();

        DataLayer bodyDataLayer = this.gridLayer.getBodyDataLayer();

        // Step 1: Create a label accumulator - adds custom labels to all cells
        // which we wish to render differently. In this case render as a button.
        ColumnOverrideLabelAccumulator cellLabelAccumulator =
                new ColumnOverrideLabelAccumulator(bodyDataLayer);
        cellLabelAccumulator.registerColumnOverrides(0, LINK_CELL_LABEL);
        cellLabelAccumulator.registerColumnOverrides(2, BUTTON_CELL_LABEL);

        // Step 2: Register label accumulator
        bodyDataLayer.setConfigLabelAccumulator(cellLabelAccumulator);

        // Step 3: Register your custom cell style and , against the
        // label applied to the link cell.
        LinkClickConfiguration<RowDataFixture> linkClickConfiguration = new LinkClickConfiguration<>();
        addLinkToColumn(configRegistry, natTable.getDisplay().getSystemColor(SWT.COLOR_BLUE), linkClickConfiguration);
        natTable.addConfiguration(linkClickConfiguration);

        // Step 4: Register your custom cell painter, cell style, against the
        // label applied to the button cell.
        addButtonToColumn(configRegistry);
        natTable.addConfiguration(new ButtonClickConfiguration<RowDataFixture>(this.buttonPainter));

        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new DebugMenuConfiguration(natTable));

        natTable.setConfigRegistry(configRegistry);
        natTable.configure();

        // Layout SWT widgets. Not relevant to example code.
        parent.setLayout(new GridLayout(1, true));
        natTable.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        setupTextArea(parent);

        return natTable;
    }

    private void addButtonToColumn(IConfigRegistry configRegistry) {
        this.buttonPainter = new ButtonCellPainter(
                new CellPainterDecorator(
                        new TextPainter(), CellEdgeEnum.RIGHT, new ImagePainter(GUIHelper.getImage("preferences"))));

        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER,
                this.buttonPainter,
                DisplayMode.NORMAL,
                BUTTON_CELL_LABEL);

        // Add your listener to the button
        this.buttonPainter.addClickListener(new MyMouseAction());

        // Set the color of the cell. This is picked up by the button painter to
        // style the button
        Style style = new Style();
        style.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                GUIHelper.COLOR_WHITE);

        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                style,
                DisplayMode.NORMAL,
                BUTTON_CELL_LABEL);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                style,
                DisplayMode.SELECT,
                BUTTON_CELL_LABEL);
    }

    private void addLinkToColumn(IConfigRegistry configRegistry, Color linkColor, LinkClickConfiguration<RowDataFixture> linkClickConfiguration) {
        // Add your listener to the button
        linkClickConfiguration.addClickListener(new MyMouseAction());

        Style linkStyle = new Style();
        linkStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
                linkColor);
        linkStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, TextDecorationEnum.UNDERLINE);

        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                linkStyle,
                DisplayMode.NORMAL,
                LINK_CELL_LABEL);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                linkStyle,
                DisplayMode.SELECT,
                LINK_CELL_LABEL);
    }

    /**
     * Sample action to execute when the button is clicked.
     */
    class MyMouseAction implements IMouseAction {

        @Override
        public void run(NatTable natTable, MouseEvent event) {
            NatEventData eventData = NatEventData.createInstanceFromEvent(event);
            int rowIndex = natTable.getRowIndexByPosition(eventData.getRowPosition());
            int columnIndex = natTable.getColumnIndexByPosition(eventData.getColumnPosition());

            ListDataProvider<RowDataFixture> dataProvider =
                    Rendering_cells_as_a_link_and_button.this.gridLayer.getBodyDataProvider();

            Object rowObject = dataProvider.getRowObject(rowIndex);
            Object cellData = dataProvider.getDataValue(columnIndex, rowIndex);

            log("Clicked on cell: " + cellData);
            log("Clicked on row: " + rowObject + "\n");
        }
    }

    class ButtonClickConfiguration<T> extends AbstractUiBindingConfiguration {

        private final ButtonCellPainter buttonCellPainter;

        public ButtonClickConfiguration(ButtonCellPainter buttonCellPainter) {
            this.buttonCellPainter = buttonCellPainter;
        }

        /**
         * Configure the UI bindings for the mouse click
         */
        @Override
        public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
            // Match a mouse event on the body, when the left button is clicked
            // and the custom cell label is present
            CellLabelMouseEventMatcher mouseEventMatcher =
                    new CellLabelMouseEventMatcher(
                            GridRegion.BODY,
                            MouseEventMatcher.LEFT_BUTTON,
                            Rendering_cells_as_a_link_and_button.BUTTON_CELL_LABEL);

            // Inform the button painter of the click.
            uiBindingRegistry.registerMouseDownBinding(mouseEventMatcher, this.buttonCellPainter);
        }

    }

    class LinkClickConfiguration<T> extends AbstractUiBindingConfiguration implements IMouseAction {

        private final List<IMouseAction> clickListeners = new ArrayList<>();

        /**
         * Configure the UI bindings for the mouse click
         */
        @Override
        public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
            // Match a mouse event on the body, when the left button is clicked
            // and the custom cell label is present
            CellLabelMouseEventMatcher mouseEventMatcher =
                    new CellLabelMouseEventMatcher(
                            GridRegion.BODY,
                            MouseEventMatcher.LEFT_BUTTON,
                            Rendering_cells_as_a_link_and_button.LINK_CELL_LABEL);

            CellLabelMouseEventMatcher mouseHoverMatcher =
                    new CellLabelMouseEventMatcher(GridRegion.BODY, 0, Rendering_cells_as_a_link_and_button.LINK_CELL_LABEL);

            // Inform the button painter of the click.
            uiBindingRegistry.registerMouseDownBinding(mouseEventMatcher, this);

            // show hand cursor, which is usually used for links
            uiBindingRegistry.registerFirstMouseMoveBinding(mouseHoverMatcher, (natTable, event) -> {
                natTable.setCursor(natTable.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
            });

        }

        @Override
        public void run(final NatTable natTable, MouseEvent event) {
            for (IMouseAction listener : this.clickListeners) {
                listener.run(natTable, event);
            }
        }

        public void addClickListener(IMouseAction mouseAction) {
            this.clickListeners.add(mouseAction);
        }

        public void removeClickListener(IMouseAction mouseAction) {
            this.clickListeners.remove(mouseAction);
        }
    }

}

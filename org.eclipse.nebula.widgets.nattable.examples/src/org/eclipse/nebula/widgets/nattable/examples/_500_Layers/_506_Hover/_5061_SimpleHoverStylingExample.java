/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
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

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.config.SimpleHoverStylingBindings;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Simple example showing how to add the {@link HoverLayer} to a simple layer
 * composition.
 */
public class _5061_SimpleHoverStylingExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400, new _5061_SimpleHoverStylingExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the HoverLayer within a simple layer composition.";
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

        HoverLayer hoverLayer = new HoverLayer(bodyDataLayer, false);
        // we need to ensure that the hover styling is removed when the mouse
        // cursor moves out of the cell area
        hoverLayer.addConfiguration(new SimpleHoverStylingBindings(hoverLayer));

        SelectionLayer selectionLayer = new SelectionLayer(hoverLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        viewportLayer.setRegionName(GridRegion.BODY);

        // turn the auto configuration off as we want to add our hover styling
        // configuration
        NatTable natTable = new NatTable(parent, viewportLayer, false);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration manually
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

        // add the style configuration for hover
        natTable.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                // style that is applied when cells are hovered
                Style style = new Style();
                style.setAttributeValue(
                        CellStyleAttributes.BACKGROUND_COLOR,
                        GUIHelper.COLOR_YELLOW);

                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        style,
                        DisplayMode.HOVER);

                // style that is applied when selected cells are hovered
                style = new Style();
                style.setAttributeValue(
                        CellStyleAttributes.BACKGROUND_COLOR,
                        GUIHelper.COLOR_GREEN);

                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        style,
                        DisplayMode.SELECT_HOVER);
            }
        });

        natTable.configure();

        return natTable;
    }

}

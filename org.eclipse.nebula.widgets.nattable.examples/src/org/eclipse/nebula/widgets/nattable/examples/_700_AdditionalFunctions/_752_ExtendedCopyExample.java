/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._700_AdditionalFunctions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.copy.action.ClearClipboardAction;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.copy.command.InternalCopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.formula.CopySelectionLayerPainter;
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
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemState;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class _752_ExtendedCopyExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(new _752_ExtendedCopyExample());
    }

    @Override
    public String getDescription() {
        return "This example is an extension to the CopyExample and adds a painter to indicate the "
                + "currently copied cells and a context menu for copy operations.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        panel.setLayout(layout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        Composite gridPanel = new Composite(panel, SWT.NONE);
        gridPanel.setLayout(layout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(gridPanel);

        Composite buttonPanel = new Composite(panel, SWT.NONE);
        buttonPanel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);

        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender", "married", "birthday" };

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
                new DefaultBodyDataProvider<>(PersonService.getPersons(100), propertyNames);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(
                        columnHeaderDataLayer,
                        viewportLayer,
                        selectionLayer);

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyDataProvider);
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(
                        rowHeaderDataLayer,
                        viewportLayer,
                        selectionLayer);

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

        final NatTable natTable = new NatTable(gridPanel, gridLayer, false);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        // add context menu
        natTable.addConfiguration(new BodyMenuConfiguration(natTable, selectionLayer));

        natTable.configure();

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // register a CopyDataCommandHandler that also copies the headers and
        // uses the configured IDisplayConverters
        CopyDataCommandHandler copyHandler =
                new InternalCopyDataCommandHandler(selectionLayer, natTable.getInternalCellClipboard());
        copyHandler.setCopyFormattedText(true);
        selectionLayer.registerCommandHandler(copyHandler);

        // add copy border
        selectionLayer.setLayerPainter(new CopySelectionLayerPainter(natTable.getInternalCellClipboard()));

        Button addColumnButton = new Button(buttonPanel, SWT.PUSH);
        addColumnButton.setText("Copy"); //$NON-NLS-1$
        addColumnButton.addSelectionListener(createCopySelectionListener(natTable));

        return panel;
    }

    private static class BodyMenuConfiguration extends AbstractUiBindingConfiguration {
        private static final String id = "copy";

        private SelectionLayer selectionLayer;
        private InternalCellClipboard clipboard;

        private Menu bodyMenu;

        public BodyMenuConfiguration(NatTable natTable, SelectionLayer selectionLayer) {
            this.bodyMenu = createBodyMenu(natTable).build();
            this.selectionLayer = selectionLayer;
            this.clipboard = natTable.getInternalCellClipboard();
        }

        protected PopupMenuBuilder createBodyMenu(final NatTable natTable) {
            return new PopupMenuBuilder(natTable).withMenuItemProvider(id, new IMenuItemProvider() {
                @Override
                public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                    MenuItem copyItem = new MenuItem(popupMenu, SWT.PUSH);
                    copyItem.setText("Copy"); //$NON-NLS-1$
                    copyItem.setEnabled(true);
                    copyItem.addSelectionListener(createCopySelectionListener(natTable));
                }
            }).withEnabledState(id, new IMenuItemState() {
                @Override
                public boolean isActive(NatEventData natEventData) {
                    // enabled on selection available
                    return !BodyMenuConfiguration.this.selectionLayer.getSelectionModel().isEmpty();
                }
            });
        }

        @Override
        public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
            if (this.bodyMenu != null) {
                uiBindingRegistry.registerMouseDownBinding(
                        new MouseEventMatcher(SWT.NONE, null, MouseEventMatcher.RIGHT_BUTTON),
                        new PopupMenuAction(this.bodyMenu));
            }

            // ui binding to clear the InternalCellClipboard
            uiBindingRegistry.registerFirstKeyBinding(
                    new KeyEventMatcher(SWT.NONE, SWT.ESC),
                    new ClearClipboardAction(this.clipboard));

        }
    }

    private static SelectionAdapter createCopySelectionListener(final NatTable natTable) {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                natTable.doCommand(
                        new CopyDataToClipboardCommand("\t", //$NON-NLS-1$
                                System.lineSeparator(),
                                natTable.getConfigRegistry()));
            }
        };
    }
}
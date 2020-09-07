/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._511_Grouping;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.indicator.HideIndicatorOverlayPainter;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.VerticalTextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.AbstractHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.ui.menu.VisibleColumnsRemaining;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowColumnInViewportCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * This example shows the usage of the performance
 * {@link ColumnGroupHeaderLayer} in a layer composition of a grid for a huge
 * number of columns that are grouped.
 */
public class _5115_HugeColumnGroupingExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(1010, 500, new _5115_HugeColumnGroupingExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the performance {@link ColumnGroupHeaderLayer} in a "
                + "layer composition of a grid for a huge number of columns that are grouped.";
    }

    @Override
    public Control createExampleControl(Composite parent) {

        int rowCount = 20;
        int columnCount = 900000;
        int groupWidth = columnCount / 3;

        // build the body layer stack
        // Usually you would create a new layer stack by extending
        // AbstractIndexLayerTransform and setting the ViewportLayer as
        // underlying layer. But in this case using the ViewportLayer
        // directly as body layer is also working.
        IDataProvider bodyDataProvider = new HugeBodyDataProvider(rowCount, columnCount);

        DataLayer bodyDataLayer =
                new DataLayer(bodyDataProvider);

        RowHideShowLayer rowHideShowLayer =
                new RowHideShowLayer(bodyDataLayer);

        ColumnReorderLayer columnReorderLayer =
                new ColumnReorderLayer(rowHideShowLayer);

        ColumnHideShowLayer columnHideShowLayer =
                new ColumnHideShowLayer(columnReorderLayer);

        ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer =
                new ColumnGroupExpandCollapseLayer(columnHideShowLayer);

        final SelectionLayer selectionLayer =
                new SelectionLayer(columnGroupExpandCollapseLayer);
        ViewportLayer viewportLayer =
                new ViewportLayer(selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider = new HugeColumnHeaderDataProvider(columnCount);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ColumnHeaderLayer columnHeaderLayer =
                new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer);
        ColumnGroupHeaderLayer columnGroupHeaderLayer =
                new ColumnGroupHeaderLayer(columnHeaderLayer, selectionLayer);

        columnGroupHeaderLayer.setShowAlwaysGroupNames(true);

        // configure the column groups
        columnGroupHeaderLayer.setDefaultUnbreakable(true);
        columnGroupHeaderLayer.addGroup("First", 0, groupWidth);
        columnGroupHeaderLayer.addGroup("Second", groupWidth, groupWidth);
        columnGroupHeaderLayer.addGroup("Third", (groupWidth * 2), groupWidth);

        // columnGroupHeaderLayer.addGroupingLevel();
        // columnGroupHeaderLayer.addGroup(1, "Test", 0, 7);

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyDataProvider);
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer);

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(cornerDataLayer, rowHeaderLayer, columnGroupHeaderLayer);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(viewportLayer, columnGroupHeaderLayer, rowHeaderLayer, cornerLayer);

        // turn the auto configuration off as we want to add our header menu
        // configuration
        NatTable natTable = new NatTable(parent, gridLayer, false);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration manually
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

        natTable.addConfiguration(new AbstractHeaderMenuConfiguration(natTable) {
            @Override
            protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
                PopupMenuBuilder builder = super.createColumnHeaderMenu(natTable)
                        .withHideColumnMenuItem()
                        .withShowAllColumnsMenuItem()
                        // the performance column group menu, not the old one
                        .withCreateColumnGroupMenuItem()
                        .withUngroupColumnsMenuItem()
                        .withAutoResizeSelectedColumnsMenuItem()
                        .withColumnRenameDialog()
                        .withColumnChooserMenuItem()
                        .withMenuItemProvider(new IMenuItemProvider() {

                            @Override
                            public void addMenuItem(NatTable natTable, Menu popupMenu) {
                                MenuItem scroll = new MenuItem(popupMenu, SWT.PUSH);
                                scroll.setText("Scroll");
                                scroll.setEnabled(true);

                                scroll.addSelectionListener(new SelectionAdapter() {
                                    @Override
                                    public void widgetSelected(SelectionEvent e) {
                                        natTable.doCommand(
                                                new ShowColumnInViewportCommand(11));
                                    }
                                });
                            }
                        });
                builder.withEnabledState(
                        PopupMenuBuilder.HIDE_COLUMN_MENU_ITEM_ID,
                        new VisibleColumnsRemaining(selectionLayer));
                return builder;
            }

            @Override
            protected PopupMenuBuilder createRowHeaderMenu(NatTable natTable) {
                return super.createRowHeaderMenu(natTable)
                        .withAutoResizeSelectedRowsMenuItem();
            }

            @Override
            protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
                return super.createCornerMenu(natTable).withShowAllColumnsMenuItem();
            }
        });

        // Column group header menu
        final Menu columnGroupHeaderMenu = new PopupMenuBuilder(natTable)
                .withRenameColumnGroupMenuItem()
                .withRemoveColumnGroupMenuItem()
                .build();

        natTable.addConfiguration(new AbstractUiBindingConfiguration() {
            @Override
            public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
                uiBindingRegistry.registerFirstMouseDownBinding(
                        new MouseEventMatcher(
                                SWT.NONE,
                                GridRegion.COLUMN_GROUP_HEADER,
                                MouseEventMatcher.RIGHT_BUTTON),
                        new PopupMenuAction(columnGroupHeaderMenu));
            }
        });

        // TODO additional configs
        natTable.addConfiguration(new FillHandleConfiguration(selectionLayer));

        HideIndicatorOverlayPainter overlayPainter =
                new HideIndicatorOverlayPainter(columnHeaderLayer, rowHeaderLayer);
        natTable.addOverlayPainter(overlayPainter);

        // enable this configuration to verify the automatic height calculation
        // when using vertical text painter
        natTable.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                ICellPainter cellPainter = new BeveledBorderDecorator(new VerticalTextPainter(false, true, 5, true, true));
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER, cellPainter,
                        DisplayMode.NORMAL,
                        GridRegion.COLUMN_HEADER);
            }
        });

        // TODO
        // Register column chooser
        // DisplayColumnChooserCommandHandler columnChooserCommandHandler =
        // new DisplayColumnChooserCommandHandler(
        // selectionLayer,
        // columnHideShowLayer,
        // columnHeaderLayer,
        // columnHeaderDataLayer,
        // columnGroupHeaderLayer,
        // columnGroupModel,
        // false,
        // true);
        //
        // viewportLayer.registerCommandHandler(columnChooserCommandHandler);

        natTable.configure();

        return natTable;
    }

    private class HugeColumnHeaderDataProvider implements IDataProvider {

        private final int columnCount;

        public HugeColumnHeaderDataProvider(int columnCount) {
            this.columnCount = columnCount;
        }

        @Override
        public Object getDataValue(int columnIndex, int rowIndex) {
            return "Column Header" + columnIndex;
        }

        @Override
        public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getColumnCount() {
            return this.columnCount;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

    }

    private class HugeBodyDataProvider implements IDataProvider {

        private final int rowCount;
        private final int columnCount;

        public HugeBodyDataProvider(int rowCount, int columnCount) {
            this.rowCount = rowCount;
            this.columnCount = columnCount;
        }

        @Override
        public Object getDataValue(int columnIndex, int rowIndex) {
            return "Body data " + columnIndex + "/" + rowIndex;
        }

        @Override
        public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getColumnCount() {
            return this.columnCount;
        }

        @Override
        public int getRowCount() {
            return this.rowCount;
        }

    }

    // private class HugeListDataProvider extends ListDataProvider<T>
}

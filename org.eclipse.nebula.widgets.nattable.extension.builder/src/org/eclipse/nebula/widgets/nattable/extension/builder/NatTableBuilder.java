/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.builder;

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotNull;

import java.util.Comparator;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.columnCategories.ChooseColumnsFromCategoriesCommandHandler;
import org.eclipse.nebula.widgets.nattable.columnChooser.command.DisplayColumnChooserCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.NullComparator;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.extension.builder.configuration.GridLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.builder.configuration.RightClickColumnHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.builder.configuration.RowSelectionUIBindings;
import org.eclipse.nebula.widgets.nattable.extension.builder.configuration.SelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.builder.configuration.TableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.builder.layers.BodyLayerStack;
import org.eclipse.nebula.widgets.nattable.extension.builder.layers.ColumnHeaderLayerStack;
import org.eclipse.nebula.widgets.nattable.extension.builder.layers.CornerLayerStack;
import org.eclipse.nebula.widgets.nattable.extension.builder.layers.RowHeaderLayerStack;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.ColumnStyle;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.IEditor;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableColumn;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableModel;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableRow;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableStyle;
import org.eclipse.nebula.widgets.nattable.extension.builder.util.ColumnAccessor;
import org.eclipse.nebula.widgets.nattable.extension.builder.util.ColumnCategoriesModelAssembler;
import org.eclipse.nebula.widgets.nattable.extension.builder.util.ColumnGroupModelAssembler;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.CellOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.editor.command.DisplayColumnStyleEditorCommandHandler;
import org.eclipse.nebula.widgets.nattable.ui.menu.DebugMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.widgets.Composite;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

/**
 * Convenience class to configure the grid with the most commonly used features.
 *
 * @param <T> Type of the object in the underlying list
 */
public class NatTableBuilder<T extends TableRow> {

	public static final String COLUMN_HEADER_COLUMN_LABEL_PREFIX = "columnHeaderColumnLabel_";
	public static final String BODY_COLUMN_LABEL_PREFIX = "bodyColumnLabel_";

	private final TableStyle tableStyle;
	private final TableModel tableModel;
	private final TableColumn[] columns;
	private final IRowIdAccessor<T> rowIdAccessor;

	private final Composite parent;
	private NatTable natTable;

	private final EventList<T> eventList;
	private FilterList<T> filterList;
	private ColumnOverrideLabelAccumulator columnLabelAccumulator;
	private IConfigRegistry configRegistry;

	// Layers
	private BodyLayerStack<T> bodyLayer;
	private ColumnHeaderLayerStack<T> columnHeaderLayer;
	private RowHeaderLayerStack<T> rowHeaderLayer;
	private CornerLayerStack<T> cornerLayer;
	private GridLayer gridLayer;

	public NatTableBuilder(Composite parent, TableModel tableModel, EventList<T> eventList, IRowIdAccessor<T> rowIdAccessor) {
		this.parent = parent;
		this.tableModel = tableModel;
		this.rowIdAccessor = rowIdAccessor;
		this.tableStyle = tableModel.tableStyle;
		this.columns = tableModel.columnProperties;
		this.eventList = GlazedLists.threadSafeList(eventList);
	}

	public NatTable setupLayerStacks(){
		IColumnPropertyAccessor<T> columnAccessor = new ColumnAccessor<T>(columns);
		configRegistry = new ConfigRegistry();
		SortedList<T> sortedRows = new SortedList<T>(eventList, null);
		filterList = new FilterList<T>(sortedRows);

		setUpColumnGroupModel();
		setUpColumnCategoriesModel();

		// Body
		bodyLayer = new BodyLayerStack<T>(tableModel, filterList);

		// Column header
		columnHeaderLayer = new ColumnHeaderLayerStack<T>(sortedRows, filterList, tableModel,	bodyLayer, columnAccessor, configRegistry);

		// Row header
		rowHeaderLayer = new RowHeaderLayerStack<T>(bodyLayer, tableModel);

		// Corner
		cornerLayer = new CornerLayerStack<T>(columnHeaderLayer, rowHeaderLayer);

		// Grid
		gridLayer = new GridLayer(bodyLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer, false);
		natTable = new NatTable(parent, gridLayer, false);
		natTable.setConfigRegistry(configRegistry);
		natTable.setBackground(tableStyle.tableBgColor);

		// Configuration
		natTable.addConfiguration(new TableStyleConfiguration(tableStyle));
		configureColumnProperties();
		configureSorting();
		configureEditing();
		configureFiltering();
		configureColumnChooser();
		configureCategoriesBasedColumnChooser();
		configureColumnStyleCustomization();
		configureGridLayer();
		configureSelectionStyle();
		configureColumnHeaderRightClickMenu();

		return natTable;
	}

	private void setUpColumnGroupModel() {
		if(tableModel.enableColumnGroups){
			tableModel.columnGroupModel = ColumnGroupModelAssembler.setupColumnGroups(tableModel.columnProperties);
		}
	}

	private void setUpColumnCategoriesModel() {
		tableModel.columnCategoriesModel = ColumnCategoriesModelAssembler.setupColumnCategories(tableModel.columnProperties);
	}

	public NatTable build(){
		natTable.configure();
		return natTable;
	}

	protected void configureColumnStyleCustomization() {
		if(!tableModel.enableColumnStyleCustomization) {
			return;
		}
		// Register a command handler for the StyleEditorDialog
		DisplayColumnStyleEditorCommandHandler styleChooserCommandHandler =
			new DisplayColumnStyleEditorCommandHandler(bodyLayer.getSelectionLayer(), columnLabelAccumulator, configRegistry);
		bodyLayer.registerCommandHandler(styleChooserCommandHandler);

		// Register the style editor as persistable
		// This will persist the style applied to the columns when NatTable#saveState is invoked
		bodyLayer.registerPersistable(styleChooserCommandHandler);
		bodyLayer.registerPersistable(columnLabelAccumulator);
	}

	protected void configureColumnHeaderRightClickMenu() {
		if(tableModel.enableColumnHeaderRightClickMenu){
			natTable.addConfiguration(new RightClickColumnHeaderMenuConfiguration(natTable, tableModel));
		}

		natTable.addConfiguration(new DebugMenuConfiguration(natTable));
	}

	/**
	 * Set up selection behavior. Select cells vs select rows.
	 */
	protected void configureSelectionStyle() {
		SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();
		if (tableModel.enableFullRowSelection) {
			selectionLayer.addConfiguration(new RowOnlySelectionConfiguration<T>());
			natTable.addConfiguration(new RowSelectionUIBindings());
			selectionLayer.setSelectionModel(new RowSelectionModel<T>(selectionLayer, bodyLayer.getDataProvider(), rowIdAccessor));
		} else {
			selectionLayer.addConfiguration(new DefaultSelectionLayerConfiguration());
		}
		natTable.addConfiguration(new SelectionStyleConfiguration(tableStyle));
	}

	/**
	 * Configure the Alternate row color
	 */
	protected void configureGridLayer() {
		gridLayer.addConfiguration(new GridLayerConfiguration(gridLayer, tableStyle));
	}

	protected void configureColumnChooser() {
		bodyLayer.registerCommandHandler(new DisplayColumnChooserCommandHandler(
				bodyLayer.getSelectionLayer(),
				bodyLayer.getColumnHideShowLayer(),
				columnHeaderLayer.getColumnHeaderLayer(),
				columnHeaderLayer.getDataLayer(),
				columnHeaderLayer.getColumnGroupHeaderLayer(),
				tableModel.columnGroupModel));
	}

	protected void configureCategoriesBasedColumnChooser() {
		if (ObjectUtils.isNull(tableModel.columnCategoriesModel)) {
			return;
		}
		bodyLayer.registerCommandHandler(new ChooseColumnsFromCategoriesCommandHandler(
				bodyLayer.getColumnHideShowLayer(),
				columnHeaderLayer.getColumnHeaderLayer(),
				columnHeaderLayer.getDataLayer(),
				tableModel.columnCategoriesModel));
	}

	protected void configureColumnProperties() {
		columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyLayer.getDataLayer());
		bodyLayer.addLabelAccumulator(columnLabelAccumulator);

		for (int colIndex = 0; colIndex < columns.length; colIndex++) {
			Integer width = columns[colIndex].width;
			if(isNotNull(width)){
				bodyLayer.getDataLayer().setColumnWidthByPosition(colIndex, width);
			}

			// Register a label on each column
			String columnLabel = BODY_COLUMN_LABEL_PREFIX + colIndex;
			columnLabelAccumulator.registerColumnOverrides(colIndex, columnLabel);

			// Add the display converter/formatter to the column
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.DISPLAY_CONVERTER,
					columns[colIndex].displayConverter, DisplayMode.NORMAL, columnLabel);

			// Add column visual style
			Style style = new Style();
			ColumnStyle columnStyle = columns[colIndex].style;
			if(ObjectUtils.isNotNull(columnStyle)){
				style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, columnStyle.bgColor);
				style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, columnStyle.fgColor);

				style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, columnStyle.hAlign);
				style.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, columnStyle.vAlign);
				style.setAttributeValue(CellStyleAttributes.FONT, columnStyle.font);

				style.setAttributeValue(CellStyleAttributes.BORDER_STYLE, columnStyle.borderStyle);
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL, columnLabel);
			}
		}
	}

	protected void configureSorting(){
		ColumnOverrideLabelAccumulator columnHeaderLabelAccumulator = new ColumnOverrideLabelAccumulator(columnHeaderLayer.getDataLayer());
		columnHeaderLayer.addLabelAccumulator(columnHeaderLabelAccumulator);

		for (int colIndex = 0; colIndex < columns.length; colIndex++) {
			String columnHeaderLabel = COLUMN_HEADER_COLUMN_LABEL_PREFIX + colIndex;

			// Register an accumulator on the Column Header layer (since it triggers sort)
			columnHeaderLabelAccumulator.registerColumnOverrides(colIndex, columnHeaderLabel);

			if (columns[colIndex].isSortable) {
				Comparator<?> comparator = columns[colIndex].comparator;
				configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR, comparator, DisplayMode.NORMAL, columnHeaderLabel);
			} else {
				configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR, new NullComparator(), DisplayMode.NORMAL, columnHeaderLabel);
			}
		}
	}

	protected void configureFiltering() {
		if(!tableModel.enableFilterRow){
			return;
		}
		for (int colIndex = 0; colIndex < columns.length; colIndex++) {

			String filterRowLabel = FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + colIndex;
			TableColumn column = columns[colIndex];

			configRegistry.registerConfigAttribute(FilterRowConfigAttributes.FILTER_COMPARATOR, column.comparator, DisplayMode.NORMAL, filterRowLabel);
			configRegistry.registerConfigAttribute(FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER, column.filterRowDisplayConverter, DisplayMode.NORMAL, filterRowLabel);
			configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, column.filterRowEditor.getCellEditor(), DisplayMode.NORMAL, filterRowLabel);
		}

		// Filter row style
		Style style = new Style();
		style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, tableStyle.filterRowBGColor);
		style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, tableStyle.filterRowFGColor);
		style.setAttributeValue(CellStyleAttributes.FONT, tableStyle.filterRowFont);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL, GridRegion.FILTER_ROW);
	}

	protected void configureEditing(){
		for (int colIndex = 0; colIndex < columns.length; colIndex++) {
			TableColumn column = columns[colIndex];

			if(column.isEditable){
				IEditor editor = column.editor;

				// Column label has been registered already
				String columnLabel = BODY_COLUMN_LABEL_PREFIX + colIndex;

				configRegistry.registerConfigAttribute(
						EditConfigAttributes.CELL_EDITABLE_RULE,
						editor.getEditableRule(), DisplayMode.EDIT, columnLabel);

				configRegistry.registerConfigAttribute(
						EditConfigAttributes.DATA_VALIDATOR,
						editor.getValidator(), DisplayMode.EDIT, columnLabel);

				switch (editor.getType()) {
				case CHECKBOX:
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new CheckBoxPainter(), DisplayMode.NORMAL, columnLabel);
					configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultBooleanDisplayConverter(), DisplayMode.NORMAL, columnLabel);
					configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, editor.getCellEditor(), DisplayMode.NORMAL, columnLabel);
					break;
				case COMBO:
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new ComboBoxPainter(), DisplayMode.NORMAL, columnLabel);
					configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, editor.getCellEditor(), DisplayMode.NORMAL, columnLabel);
					configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, editor.getCellEditor(), DisplayMode.EDIT, columnLabel);
					break;
				case TEXT:
					configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new TextCellEditor());
					break;
				default:
					break;
				}
			}
		}
	}

	// Accessors for layers

	public BodyLayerStack<T> getBodyLayerStack() {
		return bodyLayer;
	}

	public ColumnHeaderLayerStack<T> getColumnHeaderLayerStack() {
		return columnHeaderLayer;
	}

	public RowHeaderLayerStack<T> getRowHeaderLayer() {
		return rowHeaderLayer;
	}

	public CornerLayerStack<T> getCornerLayer() {
		return cornerLayer;
	}

	public GridLayer getGridLayer() {
		return gridLayer;
	}

	public TableModel getTableModel() {
		return tableModel;
	}

	public EventList<T> getEventList() {
		return eventList;
	}

	public FilterList<T> getFilterList() {
		return filterList;
	}

	public NatTable getNatTable() {
		return natTable;
	}

	public void addCellLabelsToBody(CellOverrideLabelAccumulator<? extends TableRow> myAccumulator) {
		bodyLayer.addLabelAccumulator(myAccumulator);
	}

}

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
package org.eclipse.nebula.widgets.nattable.extension.builder.model;

import java.util.Comparator;

import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;


/**
 * Encapsulation of the properties of a column in the table
 */
public class TableColumn {

	public int index;
	public String rowObjectPropertyName;

	/** The column will be added to a group of the following name */
	public String groupName;

	/** The column will be added to a category of the following name */
	public String categoryName;

	/** Name of the column as it appears in the column header */
	public String displayName;

	/** Name of the class of the underlying row object property, Example "Double", "String" etc. */
	public String dataTypeClassName = "java.lang.String";

	/** Is the user allowed to edit this column */
	public boolean isEditable;
	/** The type of editor */
	public IEditor editor = Editors.getTextEditor();

	/** If a column specific style is set here, it will override the overall table style */
	public ColumnStyle style = null;

	/** If a column specific width is set here, it will override the default column width from the table style */
	public Integer width = null;

	/** Formatter used to covert the underlying data to a display string and vice versa */
	public IDisplayConverter displayConverter = new DefaultDisplayConverter();

	/** Column sorting */
	public boolean isSortable = true;
	public Comparator<?> comparator = new DefaultComparator();

	/**
	  Display converter to translate the strings typed by the user in the filter row.
	  Defaults to the display converter used by the column
	*/
	public IDisplayConverter filterRowDisplayConverter = displayConverter;
	/** Editor to be used in the filter row cell */
	public IEditor filterRowEditor = Editors.getTextEditor();

	public TableColumn(int index, String rowObjectPropertyName) {
		this(index, rowObjectPropertyName, rowObjectPropertyName);
	}

	public TableColumn(int index, String rowObjectPropertyName, String displayName) {
		this.index = index;
		this.rowObjectPropertyName = rowObjectPropertyName;
		this.displayName = displayName;
	}


	public TableColumn setStyle(ColumnStyle columnStyle) {
		this.style = columnStyle;
		return this;
	}

	public TableColumn setWidth(int width) {
		this.width = width;
		return this;
	}

	public TableColumn setComparator(Comparator<?> comparator) {
		this.comparator = comparator;
		return this;
	}

	public TableColumn setEditor(IEditor editor) {
		this.editor = editor;
		return this;
	}

	public TableColumn setDisplayConverter(IDisplayConverter displayConverter) {
		this.displayConverter = displayConverter;
		setFilterRowDisplayConverter(displayConverter);
		return this;
	}

	public TableColumn setGroupName(String groupName) {
		this.groupName = groupName;
		return this;
	}

	public TableColumn setCategory(String categoryName) {
		this.categoryName = categoryName;
		return this;
	}

	public TableColumn setFilterRowEditor(IEditor editor) {
		this.filterRowEditor = editor;
		return this;
	}

	public TableColumn setFilterRowDisplayConverter(IDisplayConverter displayConverter) {
		this.filterRowDisplayConverter = displayConverter;
		return this;
	}

}

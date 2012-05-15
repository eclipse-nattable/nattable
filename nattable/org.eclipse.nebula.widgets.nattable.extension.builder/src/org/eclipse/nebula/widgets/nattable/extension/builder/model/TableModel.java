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

import org.eclipse.nebula.widgets.nattable.columnCategories.ColumnCategoriesModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;

public class TableModel {

	public TableStyle tableStyle = new TableStyle();
	public TableColumn[] columnProperties;

	public boolean enableFullRowSelection = true;
	public boolean enableColumnGroups = true;
	public boolean enableColumnCategories = true;
	public boolean enableColumnStyleCustomization = true;
	public boolean enableColumnHeaderRightClickMenu = true;
	public boolean enableFilterRow = true;

	public TableModel(TableColumn[] columnProperties) {
		this.columnProperties = columnProperties;
	}

	/** You do not have to set these up, the builder will build this from the column data */
	public ColumnGroupModel columnGroupModel;
	public ColumnCategoriesModel columnCategoriesModel;

}

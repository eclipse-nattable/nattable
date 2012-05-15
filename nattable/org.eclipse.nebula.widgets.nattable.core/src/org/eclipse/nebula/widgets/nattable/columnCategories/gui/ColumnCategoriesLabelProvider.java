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
package org.eclipse.nebula.widgets.nattable.columnCategories.gui;

import java.util.List;


import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.columnCategories.Node;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnChooserUtils;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnEntry;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;

public class ColumnCategoriesLabelProvider extends LabelProvider {

	List<ColumnEntry> hiddenEntries;
	
	public ColumnCategoriesLabelProvider(List<ColumnEntry> hiddenEntries) {
		this.hiddenEntries = hiddenEntries;
	}

	@Override
	public String getText(Object element) {
		Node node = (Node) element;
		switch (node.getType()) {
		case CATEGORY:
			return node.getData();
		case COLUMN:
			int index = Integer.parseInt(node.getData());
			ColumnEntry columnEntry = ColumnChooserUtils.find(hiddenEntries, index);
			if(ObjectUtils.isNull(columnEntry)){
				System.err.println(
						"Column index " + index + " is present " + //$NON-NLS-1$ //$NON-NLS-2$
						"in the Column Categories model, " + //$NON-NLS-1$
						"but not in the underlying data"); //$NON-NLS-1$
				return String.valueOf(index);
			}
			return columnEntry.getLabel();
		default:
			return Messages.getString("Unknown"); //$NON-NLS-1$
		}
	}
}

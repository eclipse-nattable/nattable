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

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNull;

import java.util.List;


import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.nebula.widgets.nattable.columnCategories.ColumnCategoriesModel;
import org.eclipse.nebula.widgets.nattable.columnCategories.Node;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnEntry;
import org.eclipse.nebula.widgets.nattable.util.ObjectCloner;

/**
 * Provides data to the tree viewer representation of Column categories.<br/>
 * Data is in the form of {@link Node} objects exposed from the {@link ColumnCategoriesModel}<br/>
 */
public class AvailableColumnCategoriesProvider implements ITreeContentProvider {

	private final ColumnCategoriesModel model;

	public AvailableColumnCategoriesProvider(ColumnCategoriesModel model) {
		this.model = (ColumnCategoriesModel) ObjectCloner.deepCopy(model);
	}

	/**
	 * Hide the given {@link ColumnEntry} (ies) i.e. do not show them in the viewer. 
	 */
	public void hideEntries(List<ColumnEntry> entriesToHide) {
		for (ColumnEntry hiddenColumnEntry : entriesToHide) {
			model.removeColumnIndex(hiddenColumnEntry.getIndex());
		}
	}

	public Object[] getChildren(Object parentElement) {
		return castToNode(parentElement).getChildren().toArray();
	}

	public Object getParent(Object element) {
		return castToNode(element).getParent();
	}

	public boolean hasChildren(Object element) {
		return castToNode(element).getNumberOfChildren() > 0;
	}

	public Object[] getElements(Object inputElement) {
		return isNull(model.getRootCategory()) 
					? new Object[]{} 
					: model.getRootCategory().getChildren().toArray();
	}

	private Node castToNode(Object element) {
		return (Node) element;
	}

	public void dispose() {
		// No op.
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// No op.
	}

}

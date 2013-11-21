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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;


public class GroupByModel extends Observable implements IPersistable {

	public static final String PERSISTENCE_KEY_GROUP_BY_COLUMN_INDEXES = ".groupByColumnIndexes"; //$NON-NLS-1$
	
	private List<Integer> groupByColumnIndexes = new ArrayList<Integer>();
	
	public boolean addGroupByColumnIndex(int columnIndex) {
		if (!groupByColumnIndexes.contains(columnIndex)) {
			groupByColumnIndexes.add(columnIndex);
			setChanged();
			notifyObservers();
			return true;
		} else {
			// unchanged
			return false;
		}
	}
	
	public boolean removeGroupByColumnIndex(int columnIndex) {
		if (groupByColumnIndexes.contains(columnIndex)) {
			groupByColumnIndexes.remove(Integer.valueOf(columnIndex));
			setChanged();
			notifyObservers();
			return true;
		} else {
			// unchanged;
			return false;
		}
	}
	
	public void clearGroupByColumnIndexes() {
		groupByColumnIndexes.clear();
		setChanged();
		notifyObservers();
	}

	public List<Integer> getGroupByColumnIndexes() {
		return groupByColumnIndexes;
	}
	
	@Override
	public void saveState(String prefix, Properties properties) {
		if (groupByColumnIndexes.size() > 0) {
			StringBuilder strBuilder = new StringBuilder();
			for (Integer index : groupByColumnIndexes) {
				strBuilder.append(index);
				strBuilder.append(IPersistable.VALUE_SEPARATOR);
			}
			properties.setProperty(prefix + PERSISTENCE_KEY_GROUP_BY_COLUMN_INDEXES, strBuilder.toString());
		}
	}

	@Override
	public void loadState(String prefix, Properties properties) {
		groupByColumnIndexes.clear();
		String property = properties.getProperty(prefix + PERSISTENCE_KEY_GROUP_BY_COLUMN_INDEXES);
		if (property != null) {
			StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
			while (tok.hasMoreTokens()) {
				String index = tok.nextToken();
				groupByColumnIndexes.add(Integer.valueOf(index));
			}
		}

		setChanged();
		notifyObservers();
	}
	
}

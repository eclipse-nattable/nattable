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
package org.eclipse.nebula.widgets.nattable.extension.builder.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableColumn;


public class TableColumnUtils {

	public static String[] getPropertyNames(TableColumn[] columns) {
		String[] propertyNames = new String[columns.length];

		for (int j = 0; j < columns.length; j++) {
			propertyNames[j] = columns[j].rowObjectPropertyName;
		}

		return propertyNames;
	}

	public static Map<String, String> getPropertyToLabelMap(TableColumn[] columns) {
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();

		for (int j = 0; j < columns.length; j++) {
			propertyToLabelMap.put(columns[j].rowObjectPropertyName, columns[j].displayName);
		}

		return propertyToLabelMap;
	}
}

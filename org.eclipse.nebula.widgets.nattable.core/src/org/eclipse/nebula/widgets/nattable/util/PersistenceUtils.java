/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.util;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class PersistenceUtils {

	/**
	 * Parse the persisted property and create a TreeMap&lt;Integer, String&gt; from it.
	 * Works in conjunction with the {@link PersistenceUtils#mapAsString(Map)}.
	 * 
	 * @param property from the properties file.
	 */
	public static Map<Integer, String> parseString(Object property) {
		TreeMap<Integer, String> map = new TreeMap<Integer, String>();
		
		if (property != null) {
			String value = (String) property;
			String[] renamedColumns = value.split("\\|"); //$NON-NLS-1$
	
			for (String token : renamedColumns) {
				String[] split = token.split(":"); //$NON-NLS-1$
				String index = split[0];
				String label = split[1];
				map.put(Integer.valueOf(index), label);
			}
		}
		return map;
	}

	/**
	 * Convert the Map to a String suitable for persisting in the Properties file.
	 * {@link PersistenceUtils#parseString(Object)} can be used to reconstruct this Map object from the String.
	 */
	public static String mapAsString(Map<Integer, String> map) {
		StringBuffer buffer = new StringBuffer();
		for (Entry<Integer, String> entry : map.entrySet()) {
			buffer.append(entry.getKey() + ":" + entry.getValue() + "|"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return buffer.toString();
	}

}

/*******************************************************************************
 * Copyright (c) 2012, 2021 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public final class PersistenceUtils {

    private PersistenceUtils() {
        // private default constructor for helper class
    }

    /**
     * The character that is used to separate the column and the value that
     * should be stored for the column.
     */
    public static final String COLUMN_VALUE_SEPARATOR = ":"; //$NON-NLS-1$

    /**
     * Parse the persisted property and create a TreeMap&lt;Integer, String&gt;
     * from it. Works in conjunction with the
     * {@link PersistenceUtils#mapAsString(Map)}.
     *
     * @param property
     *            from the properties file.
     */
    public static Map<Integer, String> parseString(Object property) {
        TreeMap<Integer, String> map = new TreeMap<>();

        if (property != null) {
            String value = (String) property;
            String[] renamedColumns = value.split("\\|"); //$NON-NLS-1$

            for (String token : renamedColumns) {
                String[] split = token.split(COLUMN_VALUE_SEPARATOR, 2);
                String index = split[0];
                String label = split[1];

                map.put(Integer.valueOf(index), label);
            }
        }
        return map;
    }

    /**
     * Convert the Map to a String suitable for persisting in the Properties
     * file. {@link PersistenceUtils#parseString(Object)} can be used to
     * reconstruct this Map object from the String.
     */
    public static String mapAsString(Map<Integer, String> map) {
        StringBuilder builder = new StringBuilder();
        for (Entry<Integer, String> entry : map.entrySet()) {
            builder.append(entry.getKey() + COLUMN_VALUE_SEPARATOR
                    + entry.getValue() + "|"); //$NON-NLS-1$
        }
        return builder.toString();
    }

}

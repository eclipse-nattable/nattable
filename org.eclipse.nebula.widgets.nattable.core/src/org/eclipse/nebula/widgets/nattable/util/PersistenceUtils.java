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
import java.util.Map.Entry;
import java.util.TreeMap;

public class PersistenceUtils {

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
        TreeMap<Integer, String> map = new TreeMap<Integer, String>();

        if (property != null) {
            String value = (String) property;
            String[] renamedColumns = value.split("\\|"); //$NON-NLS-1$

            for (String token : renamedColumns) {
                String[] split = token.split(COLUMN_VALUE_SEPARATOR);
                String index = split[0];
                String label = split[1];

                // if the value also contains colons, the split before will not
                // return the
                // correct results, this is for example true for date/time
                // values
                // we use this kind of workaround here for backwards
                // compatibility, in
                // case there are already stored states. Usually we should use
                // different
                // characters or regular expressions
                if (split.length > 2) {
                    for (int i = 2; i < split.length; i++) {
                        label += COLUMN_VALUE_SEPARATOR + split[i];
                    }
                }

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
        StringBuffer buffer = new StringBuffer();
        for (Entry<Integer, String> entry : map.entrySet()) {
            buffer.append(entry.getKey() + COLUMN_VALUE_SEPARATOR
                    + entry.getValue() + "|"); //$NON-NLS-1$
        }
        return buffer.toString();
    }

}

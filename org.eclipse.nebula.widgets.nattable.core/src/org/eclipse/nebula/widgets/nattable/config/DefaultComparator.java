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
package org.eclipse.nebula.widgets.nattable.config;

import java.util.Comparator;

@SuppressWarnings("unchecked")
public class DefaultComparator implements Comparator<Object> {

    private static DefaultComparator singleton;

    public static final DefaultComparator getInstance() {
        if (singleton == null) {
            singleton = new DefaultComparator();
        }
        return singleton;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public int compare(final Object o1, final Object o2) {
        if (o1 == null) {
            if (o2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (o2 == null) {
            return 1;
        } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
            return ((Comparable) o1).compareTo(o2);
        } else {
            return o1.toString().compareTo(o2.toString());
        }
    }

}

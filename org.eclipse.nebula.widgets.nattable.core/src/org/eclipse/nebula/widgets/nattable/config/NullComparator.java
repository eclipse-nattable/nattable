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

import java.io.Serializable;
import java.util.Comparator;

/**
 * GlazedLists require that the comparator be set to 'null' if a column is not
 * sortable. If a null value is set in the {@link IConfigRegistry} it will
 * attempt to find other matching values. This comparator can be set in the
 * {@link ConfigRegistry} to indicate that the column can not be sorted.
 * <p>
 * See SortableGridExample
 */
public class NullComparator implements Comparator<Object>, Serializable {

    private static final long serialVersionUID = -6945858872109267371L;

    @Override
    public int compare(Object o1, Object o2) {
        return 0;
    }

}

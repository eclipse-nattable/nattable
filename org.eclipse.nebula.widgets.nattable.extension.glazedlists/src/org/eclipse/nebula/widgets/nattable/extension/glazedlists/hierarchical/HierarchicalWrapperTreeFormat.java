/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.hierarchical;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalHelper;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalWrapper;

import ca.odell.glazedlists.TreeList;

/**
 * {@link TreeList.Format} implementation for dealing with HierarchicalWrapper
 * objects.
 *
 * @since 1.6
 */
public class HierarchicalWrapperTreeFormat implements TreeList.Format<HierarchicalWrapper> {

    private int levels = 0;
    private List<Map<Object, HierarchicalWrapper>> parentMapping;

    public HierarchicalWrapperTreeFormat(String... propertyNames) {
        // identify the number of levels
        for (String property : propertyNames) {
            this.levels = Math.max(this.levels, property.split(HierarchicalHelper.PROPERTY_SEPARATOR_REGEX).length);
        }

        this.parentMapping = new ArrayList<Map<Object, HierarchicalWrapper>>(this.levels);
        for (int i = 0; i < this.levels; i++) {
            this.parentMapping.add(new HashMap<Object, HierarchicalWrapper>());
        }
    }

    @Override
    public void getPath(List<HierarchicalWrapper> path, HierarchicalWrapper element) {
        for (int i = 0; i < (this.levels - 1); i++) {
            Object levelObject = element.getObject(i);
            if (levelObject != null) {
                HierarchicalWrapper parent = this.parentMapping.get(i).get(levelObject);
                // building up the parent mapping with the first de-normalized
                // object should not
                // lead to containing the same object multiple times
                if (parent != null && !path.contains(parent)) {
                    path.add(parent);
                } else {
                    this.parentMapping.get(i).put(levelObject, element);
                }
            }
        }
        path.add(element);
    }

    @Override
    public boolean allowsChildren(HierarchicalWrapper element) {
        return true;
    }

    @Override
    public Comparator<? super HierarchicalWrapper> getComparator(int depth) {
        // TODO take SortModel into account (double check GroupBy)

        // first sort by root object

        // then sort down the hierarchy levels

        return null;
    }

}
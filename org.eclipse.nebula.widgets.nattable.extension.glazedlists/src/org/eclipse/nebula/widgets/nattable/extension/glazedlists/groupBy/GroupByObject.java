/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 455327, 444839, 453885
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is used to add tree items that are added to the tree path for
 * grouping purposes. Contains the value that is used for grouping and the
 * grouping index to ensure the correct ordering.
 */
public class GroupByObject {

    /** The columnIndex -&gt; value */
    private final Map<Integer, Object> descriptor;

    /**
     * The value that is used for grouping.
     */
    private final Object value;

    /**
     * @param value
     *            The value that is used for grouping.
     * @param descriptor
     *            The description of the grouping (index -&gt; value).<br>
     *            <b>Note:</b> The map needs to be an ordered map to work
     *            correctly, e.g. {@link LinkedHashMap}
     */
    public GroupByObject(Object value, Map<Integer, Object> descriptor) {
        this.value = value;
        this.descriptor = descriptor;
    }

    /**
     * @return The value that is used for grouping.
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * @return The description of the grouping (index -&gt; value)
     */
    public Map<Integer, Object> getDescriptor() {
        return this.descriptor;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((this.descriptor == null) ? 0 : this.descriptor.hashCode());
        result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroupByObject other = (GroupByObject) obj;
        if (this.descriptor == null) {
            if (other.descriptor != null)
                return false;
        } else if (!this.descriptor.equals(other.descriptor))
            return false;
        if (this.value == null) {
            if (other.value != null)
                return false;
        } else if (!this.value.equals(other.value))
            return false;
        return true;
    }

}

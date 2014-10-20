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
package org.eclipse.nebula.widgets.nattable.layer.event;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;

public class StructuralDiff {

    public enum DiffTypeEnum {
        ADD, CHANGE, DELETE;
    }

    private DiffTypeEnum diffType;

    private Range beforePositionRange;

    private Range afterPositionRange;

    public StructuralDiff(DiffTypeEnum diffType, Range beforePositionRange,
            Range afterPositionRange) {
        this.diffType = diffType;
        this.beforePositionRange = beforePositionRange;
        this.afterPositionRange = afterPositionRange;
    }

    public DiffTypeEnum getDiffType() {
        return this.diffType;
    }

    public Range getBeforePositionRange() {
        return this.beforePositionRange;
    }

    public Range getAfterPositionRange() {
        return this.afterPositionRange;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StructuralDiff other = (StructuralDiff) obj;
        if (this.afterPositionRange == null) {
            if (other.afterPositionRange != null)
                return false;
        } else if (!this.afterPositionRange.equals(other.afterPositionRange))
            return false;
        if (this.beforePositionRange == null) {
            if (other.beforePositionRange != null)
                return false;
        } else if (!this.beforePositionRange.equals(other.beforePositionRange))
            return false;
        if (this.diffType != other.diffType)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.afterPositionRange == null) ? 0 : this.afterPositionRange.hashCode());
        result = prime * result + ((this.beforePositionRange == null) ? 0 : this.beforePositionRange.hashCode());
        result = prime * result + ((this.diffType == null) ? 0 : this.diffType.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + this.diffType //$NON-NLS-1$
                + " before: " + this.beforePositionRange //$NON-NLS-1$
                + " after: " + this.afterPositionRange; //$NON-NLS-1$
    }

}

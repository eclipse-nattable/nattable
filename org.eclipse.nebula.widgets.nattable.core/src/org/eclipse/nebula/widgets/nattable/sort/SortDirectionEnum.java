/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.sort;

public enum SortDirectionEnum {
    ASC("Ascending"), //$NON-NLS-1$
    DESC("Descending"), //$NON-NLS-1$
    NONE("Unsorted"); //$NON-NLS-1$

    private final String description;

    private SortDirectionEnum(String description) {
        this.description = description;
    }

    /**
     * @return the sorting state to go to from the current one.
     */
    public SortDirectionEnum getNextSortDirection() {
        switch (this) {
            case NONE:
                return SortDirectionEnum.ASC;
            case ASC:
                return SortDirectionEnum.DESC;
            case DESC:
                return SortDirectionEnum.NONE;
            default:
                return SortDirectionEnum.NONE;
        }
    }

    public String getDescription() {
        return this.description;
    }
}

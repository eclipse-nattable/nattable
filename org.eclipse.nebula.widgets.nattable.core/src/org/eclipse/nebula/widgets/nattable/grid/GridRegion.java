/*******************************************************************************
 * Copyright (c) 2012, 2025 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.grid;

/**
 * A region is simply an area on the Grid. Diving the table/grid into regions
 * makes it easier to manage areas with similar behavior.
 *
 * For example all the cells in the column header are painted differently and
 * can respond to sorting actions.
 */
public final class GridRegion {

    private GridRegion() {
        // private default constructor for constants class
    }

    public static final String CORNER = "CORNER"; //$NON-NLS-1$
    public static final String COLUMN_HEADER = "COLUMN_HEADER"; //$NON-NLS-1$
    public static final String COLUMN_GROUP_HEADER = "COLUMN_GROUP_HEADER"; //$NON-NLS-1$
    public static final String ROW_HEADER = "ROW_HEADER"; //$NON-NLS-1$
    public static final String ROW_GROUP_HEADER = "ROW_GROUP_HEADER"; //$NON-NLS-1$
    public static final String BODY = "BODY"; //$NON-NLS-1$
    public static final String DATAGRID = "DATAGRID"; //$NON-NLS-1$
    public static final String FILTER_ROW = "FILTER_ROW"; //$NON-NLS-1$
    /**
     * @since 2.6
     */
    public static final String GROUP_BY_REGION = "GROUP_BY_REGION"; //$NON-NLS-1$
}

/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.e4.css;

import org.eclipse.nebula.widgets.nattable.NatTable;

public class NatTableWrapper {

    private NatTable natTable;
    private String label;

    public NatTableWrapper(NatTable natTable, String label) {
        this.natTable = natTable;
        this.label = label;
    }

    public NatTable getNatTable() {
        return this.natTable;
    }

    public String getLabel() {
        return this.label;
    }

    /**
     * @since 2.0
     */
    public void dispose() {
        // null the NatTable reference to avoid memory leaks
        this.natTable = null;
    }
}

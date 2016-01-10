/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
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
}

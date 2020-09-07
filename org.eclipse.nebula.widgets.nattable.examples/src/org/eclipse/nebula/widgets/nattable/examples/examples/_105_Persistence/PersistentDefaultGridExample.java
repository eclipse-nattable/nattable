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
package org.eclipse.nebula.widgets.nattable.examples.examples._105_Persistence;

import org.eclipse.nebula.widgets.nattable.examples.PersistentNatExampleWrapper;
import org.eclipse.nebula.widgets.nattable.examples.examples._000_Default_NatTable;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;

public class PersistentDefaultGridExample extends PersistentNatExampleWrapper {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(new PersistentDefaultGridExample());
    }

    public PersistentDefaultGridExample() {
        super(new _000_Default_NatTable());
    }

}

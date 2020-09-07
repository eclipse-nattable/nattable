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
package org.eclipse.nebula.widgets.nattable.examples.e4.handler;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;

public class ToggleEnabledHandler {

    @Execute
    public void execute(IEclipseContext context) {
        Boolean enabled = (Boolean) context.get("enabled");
        if (enabled == null) {
            enabled = Boolean.FALSE;
            context.declareModifiable("enabled");
        }
        context.modify("enabled", !enabled);
    }

}
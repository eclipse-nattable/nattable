/*****************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.examples.e4.handler;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Evaluate;

/**
 * Simple expression that evaluates the boolean context value with the name
 * <code>enabled</code>.
 */
public class ContextEnabledExpression {
    @Evaluate
    public boolean evaluate(IEclipseContext context) {
        Boolean enabled = (Boolean) context.get("enabled");
        if (enabled == null) {
            enabled = Boolean.FALSE;
        }
        return enabled;
    }
}

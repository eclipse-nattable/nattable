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
package org.eclipse.nebula.widgets.nattable.ui.util;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class CancelableRunnable implements Runnable {

    private final AtomicBoolean cancel = new AtomicBoolean(false);

    public void cancel() {
        this.cancel.set(true);
    }

    protected final boolean isCancelled() {
        return this.cancel.get();
    }

}

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

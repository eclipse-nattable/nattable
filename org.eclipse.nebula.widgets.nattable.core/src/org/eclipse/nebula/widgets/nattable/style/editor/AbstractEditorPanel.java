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
package org.eclipse.nebula.widgets.nattable.style.editor;

import org.eclipse.swt.widgets.Composite;

/**
 * SWT Panel to edit object of type T
 */
public abstract class AbstractEditorPanel<T> extends Composite {

    public AbstractEditorPanel(Composite parent, int style) {
        super(parent, style);
    }

    /**
     * Initialize UI widgets to match the initial state of T
     */
    public abstract void edit(T t) throws Exception;

    /**
     * Get the new value of T with the user modifications
     */
    public abstract T getNewValue();

    /**
     * Use friendly name for this editor (used as tab labels).
     */
    public abstract String getEditorName();

}

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
     *
     * @param t
     *            the object to edit
     * @throws Exception
     *             if an error occurs
     */
    public abstract void edit(T t) throws Exception;

    /**
     * Get the new value of T with the user modifications
     *
     * @return the edited value
     */
    public abstract T getNewValue();

    /**
     * User friendly name for this editor (used as tab labels).
     *
     * @return the user friendly name of this editor
     */
    public abstract String getEditorName();

}

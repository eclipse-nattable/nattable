/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.widget;

/**
 * Enumeration for edit mode. Only specifies two possible values for editing
 * inline or in a dialog. It is needed to be able to add specific behaviour or
 * rendering dependent on the edit mode, e.g. adding a border to editor controls
 * showed in a dialog or adding move after commit to inline editors.
 */
public enum EditModeEnum {

    /**
     * Edit mode for editing cells inline
     */
    INLINE,
    /**
     * Edit mode for editing cells in a subdialog
     */
    DIALOG

}

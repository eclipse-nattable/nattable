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
package org.eclipse.nebula.widgets.nattable.edit.editor;

/**
 * Enumeration for selection mode within a TextCellEditor.
 */
public enum EditorSelectionEnum {

    /**
     * Select the whole text contained in the text editor control.
     */
    ALL,
    /**
     * In fact selects nothing, simply sets the cursor at the beginning of the
     * contained text in the text editor control.
     */
    START,
    /**
     * In fact selects nothing, simply sets the cursor at the end of the
     * contained text in the text editor control.
     */
    END

}

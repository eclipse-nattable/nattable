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
package org.eclipse.nebula.widgets.nattable.style;

/**
 * The various modes the table can be under.
 * <ol>
 * <li>During normal display a cell is in NORMAL mode.</li>
 * <li>If the contents of the cell are being edited, its in EDIT mode.</li>
 * <li>If a cell has been selected, its in SELECT mode.</li>
 * </ol>
 *
 * These modes are used to bind different settings to different modes. For
 * example, a different style can be registered for a cell when it is in SELECT
 * mode.
 *
 */
public enum DisplayMode {

    /**
     * The normal state a cell is in if no other state applies.
     */
    NORMAL,
    /**
     * The state that shows that a cell is currently selected.
     */
    SELECT,
    /**
     * The state that shows that a cell is currently edited. Never applied to a
     * cell, only used for configuration purposes.
     */
    EDIT,
    /**
     * The state that shows that currently the mouse hovers over the cell.
     */
    HOVER,
    /**
     * The state that shows that currently the mouse hovers over the cell that
     * is currently selected.
     */
    SELECT_HOVER

}

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
public interface DisplayMode {

    String NORMAL = "NORMAL"; //$NON-NLS-1$
    String SELECT = "SELECT"; //$NON-NLS-1$
    String EDIT = "EDIT"; //$NON-NLS-1$
    String HOVER = "HOVER"; //$NON-NLS-1$
    String SELECT_HOVER = "SELECT_HOVER"; //$NON-NLS-1$
}

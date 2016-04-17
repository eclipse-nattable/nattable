/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.swt.events.MouseEvent;

public interface IMouseEventMatcher {

    /**
     * Figures out if the mouse event occurred in the supplied region.
     *
     * @param natTable
     *            The NatTable on which the {@link MouseEvent} occured.
     * @param event
     *            The SWT {@link MouseEvent}.
     * @param regionLabels
     *            The {@link LabelStack} with the region labels of the
     *            {@link MouseEvent} coordinates.
     * @return <code>true</code> if the {@link MouseEvent} matches this
     *         {@link IMouseEventMatcher} and should therefore be handled,
     *         <code>false</code> if not
     */
    public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels);

}

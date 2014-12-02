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
package org.eclipse.nebula.widgets.nattable.ui.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.events.MouseEvent;

public interface IDragMode {

    public void mouseDown(NatTable natTable, MouseEvent event);

    public void mouseMove(NatTable natTable, MouseEvent event);

    public void mouseUp(NatTable natTable, MouseEvent event);

}

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
package org.eclipse.nebula.widgets.nattable.selection.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.ScrollSelectionCommand;
import org.eclipse.swt.events.KeyEvent;

public class PageUpAction extends AbstractKeySelectAction {

    public PageUpAction() {
        super(MoveDirectionEnum.UP);
    }

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        super.run(natTable, event);
        natTable.doCommand(new ScrollSelectionCommand(MoveDirectionEnum.UP,
                isShiftMask(), isControlMask()));
    }

}

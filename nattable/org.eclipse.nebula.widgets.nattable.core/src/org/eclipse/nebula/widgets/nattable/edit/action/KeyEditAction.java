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
package org.eclipse.nebula.widgets.nattable.edit.action;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.edit.command.EditSelectionCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.LetterOrDigitKeyEventMatcher;
import org.eclipse.swt.events.KeyEvent;

public class KeyEditAction implements IKeyAction {
    
    private final boolean useAdjustOnMultiCellEdit;

    public KeyEditAction() {
        this(false);
    }

	public KeyEditAction(boolean useAdjustOnMultiCellEdit) {
        this.useAdjustOnMultiCellEdit = useAdjustOnMultiCellEdit;
    }

    public void run(NatTable natTable, KeyEvent event) {
		natTable.doCommand(new EditSelectionCommand(natTable, natTable.getConfigRegistry(), convertCharToCharacterObject(event), useAdjustOnMultiCellEdit));
	}

    protected Character convertCharToCharacterObject(KeyEvent event) {
        Character character = null;
		if (LetterOrDigitKeyEventMatcher.isLetterOrDigit(event.character)) {
			character = Character.valueOf(event.character);
		}
        return character;
    }
}

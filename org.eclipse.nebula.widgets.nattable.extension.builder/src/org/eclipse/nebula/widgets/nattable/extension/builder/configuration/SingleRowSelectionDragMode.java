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
package org.eclipse.nebula.widgets.nattable.extension.builder.configuration;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.selection.action.RowSelectionDragMode;
import org.eclipse.nebula.widgets.nattable.selection.command.ClearAllSelectionsCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.swt.events.MouseEvent;

public class SingleRowSelectionDragMode extends RowSelectionDragMode implements IDragMode {

	@Override
	public void mouseMove(NatTable natTable, MouseEvent event) {
		natTable.doCommand(new ClearAllSelectionsCommand());

		if (event.x > natTable.getWidth()) {
			return;
		}
		int selectedColumnPosition = natTable.getColumnPositionByX(event.x);
		int selectedRowPosition = natTable.getRowPositionByY(event.y);

		if (selectedColumnPosition > -1 && selectedRowPosition > -1) {
			fireSelectionCommand(natTable,
									selectedColumnPosition,
									selectedRowPosition,
									false, false);
		}
	}
}

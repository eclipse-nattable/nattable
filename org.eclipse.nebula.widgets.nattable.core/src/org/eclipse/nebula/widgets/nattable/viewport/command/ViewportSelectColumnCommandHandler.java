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
package org.eclipse.nebula.widgets.nattable.viewport.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class ViewportSelectColumnCommandHandler extends AbstractLayerCommandHandler<ViewportSelectColumnCommand> {

	private final ViewportLayer viewportLayer;

	public ViewportSelectColumnCommandHandler(ViewportLayer viewportLayer) {
		this.viewportLayer = viewportLayer;
		
	}
	
	public Class<ViewportSelectColumnCommand> getCommandClass() {
		return ViewportSelectColumnCommand.class;
	}

	@Override
	protected boolean doCommand(ViewportSelectColumnCommand command) {
		IUniqueIndexLayer scrollableLayer = viewportLayer.getScrollableLayer();
		int scrollableColumnPosition = viewportLayer.localToUnderlyingColumnPosition(command.getColumnPosition());
		int scrollableRowPosition = viewportLayer.getOriginRowPosition();
		
		scrollableLayer.doCommand(new SelectColumnCommand(scrollableLayer, scrollableColumnPosition, scrollableRowPosition, command.isWithShiftMask(), command.isWithControlMask()));
		return true;
	}

}

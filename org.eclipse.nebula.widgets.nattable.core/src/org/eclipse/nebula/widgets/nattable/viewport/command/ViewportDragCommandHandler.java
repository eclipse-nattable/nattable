package org.eclipse.nebula.widgets.nattable.viewport.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class ViewportDragCommandHandler extends AbstractLayerCommandHandler<ViewportDragCommand> {

	private ViewportLayer viewportLayer;

	public ViewportDragCommandHandler(ViewportLayer viewportLayer) {
		this.viewportLayer = viewportLayer;
	}

	public Class<ViewportDragCommand> getCommandClass() {
		return ViewportDragCommand.class;
	}

	@Override
	protected boolean doCommand(ViewportDragCommand command) {
		viewportLayer.drag(command.getX(), command.getY());
		return true;
	}

}

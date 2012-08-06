package org.eclipse.nebula.widgets.nattable.viewport.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class ViewportDragCommand implements ILayerCommand {

	private int x;
	private int y;

	public ViewportDragCommand(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public boolean convertToTargetLayer(ILayer targetLayer) {
		return true;
	}

	public ILayerCommand cloneCommand() {
		return new ViewportDragCommand(x, y);
	}

}

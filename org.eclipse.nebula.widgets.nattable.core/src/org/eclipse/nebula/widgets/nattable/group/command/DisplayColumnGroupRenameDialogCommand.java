package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.swt.graphics.Point;

public class DisplayColumnGroupRenameDialogCommand extends AbstractColumnCommand implements IColumnGroupCommand {

	private final NatTable natTable;

    /**
	 * @param columnPosition of the column group to be renamed 
	 */
	public DisplayColumnGroupRenameDialogCommand(NatTable natTable, int columnPosition) {
		super(natTable, columnPosition);
        this.natTable = natTable;
	}

	public Point toDisplayCoordinates(Point point) {
	    return natTable.toDisplay(point);
	}
	
	public ILayerCommand cloneCommand() {
		return new DisplayColumnGroupRenameDialogCommand((NatTable) getLayer(), getColumnPosition());
	}
	
}

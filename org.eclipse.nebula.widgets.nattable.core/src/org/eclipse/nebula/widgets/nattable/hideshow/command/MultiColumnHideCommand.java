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
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.HORIZONTAL;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.command.AbstractDimPositionsCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.coordinate.RangeList;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;


public class MultiColumnHideCommand extends AbstractDimPositionsCommand {
	
	
	public MultiColumnHideCommand(ILayer layer, int columnPosition) {
		this(layer, new RangeList(columnPosition));
	}
	
	public MultiColumnHideCommand(ILayer layer, Collection<Range> columnPositions) {
		super(layer.getDim(HORIZONTAL), columnPositions);
	}
	
	protected MultiColumnHideCommand(MultiColumnHideCommand command) {
		super(command);
	}
	
	@Override
	public MultiColumnHideCommand cloneCommand() {
		return new MultiColumnHideCommand(this);
	}
	
}

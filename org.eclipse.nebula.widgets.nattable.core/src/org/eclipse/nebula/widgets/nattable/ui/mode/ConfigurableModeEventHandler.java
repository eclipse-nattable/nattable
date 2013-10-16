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
package org.eclipse.nebula.widgets.nattable.ui.mode;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.edit.command.EditUtils;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

public class ConfigurableModeEventHandler extends AbstractModeEventHandler {

	private final NatTable natTable;
		
	public ConfigurableModeEventHandler(ModeSupport modeSupport, NatTable natTable) {
		super(modeSupport);
		
		this.natTable = natTable;
	}
	
	// Event handling /////////////////////////////////////////////////////////
	
	@Override
	public void keyPressed(KeyEvent event) {
		IKeyAction keyAction = natTable.getUiBindingRegistry().getKeyEventAction(event);
		if (keyAction != null) {
			natTable.forceFocus();
			keyAction.run(natTable, event);
		}
	}
	
	@Override
	public void mouseDown(MouseEvent event) {
		if (EditUtils.commitAndCloseActiveEditor()) {
			IMouseAction mouseDownAction = natTable.getUiBindingRegistry().getMouseDownAction(event);
			if (mouseDownAction != null) {
				event.data = NatEventData.createInstanceFromEvent(event);
				mouseDownAction.run(natTable, event);
			}
			
			IMouseAction singleClickAction = getUiBindingRegistry().getSingleClickAction(event);
			IMouseAction doubleClickAction = getUiBindingRegistry().getDoubleClickAction(event);
			IDragMode dragMode = natTable.getUiBindingRegistry().getDragMode(event);
			
			if (singleClickAction != null || doubleClickAction != null || dragMode != null) {
				switchMode(new MouseModeEventHandler(getModeSupport(), natTable, event, singleClickAction, doubleClickAction, dragMode));
			}
		}
	}

	@Override
	public synchronized void mouseMove(MouseEvent event) {
		if (event.x >= 0 && event.y >= 0) {
			IMouseAction mouseMoveAction = getUiBindingRegistry().getMouseMoveAction(event);
			if (mouseMoveAction != null) {
				event.data = NatEventData.createInstanceFromEvent(event);
				mouseMoveAction.run(natTable, event);
			} else {
				natTable.setCursor(null);
			}
		}
	}

	@Override
	public synchronized void mouseHover(MouseEvent event) {
		if (event.x >= 0 && event.y >= 0) {
			IMouseAction mouseHoverAction = getUiBindingRegistry().getMouseHoverAction(event);
			if (mouseHoverAction != null) {
				event.data = NatEventData.createInstanceFromEvent(event);
				mouseHoverAction.run(natTable, event);
			}
		}
	}

	@Override
	public synchronized void mouseEnter(MouseEvent event) {
		if (event.x >= 0 && event.y >= 0) {
			IMouseAction mouseEnterAction = getUiBindingRegistry().getMouseEnterAction(event);
			if (mouseEnterAction != null) {
				event.data = NatEventData.createInstanceFromEvent(event);
				mouseEnterAction.run(natTable, event);
			} else {
				natTable.setCursor(null);
			}
		}
	}

	@Override
	public synchronized void mouseExit(MouseEvent event) {
		if (event.x >= 0 && event.y >= 0) {
			IMouseAction mouseExitAction = getUiBindingRegistry().getMouseExitAction(event);
			if (mouseExitAction != null) {
				event.data = NatEventData.createInstanceFromEvent(event);
				mouseExitAction.run(natTable, event);
			} else {
				natTable.setCursor(null);
			}
		}
	}
	
	private UiBindingRegistry getUiBindingRegistry() {
		return natTable.getUiBindingRegistry();
	}

}

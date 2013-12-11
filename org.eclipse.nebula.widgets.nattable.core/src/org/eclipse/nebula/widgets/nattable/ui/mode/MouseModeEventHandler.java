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
import org.eclipse.nebula.widgets.nattable.ui.action.DragModeEventHandler;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseClickAction;
import org.eclipse.swt.events.MouseEvent;

public class MouseModeEventHandler extends AbstractModeEventHandler {
	
	private final NatTable natTable;
	
	private MouseEvent initialMouseDownEvent;
	
	private IMouseAction singleClickAction;
	
	private IMouseAction doubleClickAction;
	
	private boolean mouseDown;
	
	private IDragMode dragMode;
	
	private boolean doubleClick;
	
	public MouseModeEventHandler(ModeSupport modeSupport, NatTable natTable, MouseEvent initialMouseDownEvent, IMouseAction singleClickAction, IMouseAction doubleClickAction, IDragMode dragMode) {
		super(modeSupport);
		
		this.natTable = natTable;
		
		this.mouseDown = true;
		
		this.initialMouseDownEvent = initialMouseDownEvent;
		
		this.singleClickAction = singleClickAction;
		this.doubleClickAction = doubleClickAction;
		this.dragMode = dragMode;
	}
	
	@Override
	public void mouseUp(final MouseEvent event) {
		mouseDown = false;
		doubleClick = false;
		
		if (singleClickAction != null) {
			//convert/validate/commit/close possible open editor
			//needed in case of conversion/validation errors to cancel any action
			if (EditUtils.commitAndCloseActiveEditor()) {
				if (doubleClickAction != null &&
						(isActionExclusive(singleClickAction) || isActionExclusive(doubleClickAction))) {
					//If a doubleClick action is registered and either the single click or the double
					//click action is exclusive, wait to see if this mouseUp is part of a doubleClick or not.
					event.display.timerExec(event.display.getDoubleClickTime(), new Runnable() {
						@Override
						public void run() {
							if (!doubleClick) {
								executeClickAction(singleClickAction, event);
							}
						}
					});
				} else {
					executeClickAction(singleClickAction, event);
				}
			}
		} 
		else if (doubleClickAction == null) {
			//No single or double click action registered when mouseUp detected. Switch back to normal mode.
			switchMode(Mode.NORMAL_MODE);
		}
	}
	
	@Override
	public void mouseDoubleClick(MouseEvent event) {
		//double click event is fired after second mouse up event, so it needs to be set to true here
		//this way the exclusive single click action knows that it should not execute as a double click was performed
		doubleClick = true;
		
		executeClickAction(doubleClickAction, event);
	}
	
	@Override
	public synchronized void mouseMove(MouseEvent event) {
		if (mouseDown && dragMode != null) {
			if (EditUtils.commitAndCloseActiveEditor()) {
				dragMode.mouseDown(natTable, initialMouseDownEvent);
				switchMode(new DragModeEventHandler(getModeSupport(), natTable, dragMode));
			}
			else {
				switchMode(Mode.NORMAL_MODE);
			}
		} else {
			// No drag mode registered when mouseMove detected. Switch back to normal mode.
			switchMode(Mode.NORMAL_MODE);
		}
	}

	/**
	 * Executes the given IMouseAction and switches the DisplayMode back to normal.
	 * @param action The IMouseAction that should be executed.
	 * @param event The MouseEvent that triggers the action
	 */
	private void executeClickAction(IMouseAction action, MouseEvent event) {
		//convert/validate/commit/close possible open editor
		//needed in case of conversion/validation errors to cancel any action
		if (EditUtils.commitAndCloseActiveEditor()) {
			if (action != null && event != null) {
				event.data = NatEventData.createInstanceFromEvent(event);
				action.run(natTable, event);
				// Single click action complete. Switch back to normal mode.
				switchMode(Mode.NORMAL_MODE);
			}
		}
	}
	
	/**
	 * Checks whether the given IMouseAction should be performed exclusive or not.
	 * If there is a single and a double click action configured, by default both
	 * the single and the double click will be performed. This behaviour can be
	 * modified if the given action is of type IMouseClickAction and configured to
	 * be exclusive. In this case the single or the double click action will
	 * be performed.
	 * @param action The IMouseAction to check
	 * @return <code>true</code> if the given IMouseAction should be called exclusively, 
	 * 			<code>false</code> if not.
	 */
	private boolean isActionExclusive(IMouseAction action) {
		if (action instanceof IMouseClickAction) {
			return ((IMouseClickAction)action).isExclusive();
		}
		return false;
	}
	
}

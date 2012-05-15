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
package org.eclipse.nebula.widgets.nattable.ui.mode;


import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

public class AbstractModeEventHandler implements IModeEventHandler {

	private ModeSupport modeSupport;
	
	public AbstractModeEventHandler(ModeSupport modeSupport) {
		this.modeSupport = modeSupport;
	}
	
	protected ModeSupport getModeSupport() {
		return modeSupport;
	}
	
	protected void switchMode(String mode) {
		modeSupport.switchMode(mode);
	}
	
	protected void switchMode(IModeEventHandler modeEventHandler) {
		modeSupport.switchMode(modeEventHandler);
	}
	
	public void cleanup() {
	}
	
	public void keyPressed(KeyEvent event) {
	}

	public void keyReleased(KeyEvent event) {
	}

	public void mouseDoubleClick(MouseEvent event) {
	}

	public void mouseDown(MouseEvent event) {
	}

	public void mouseUp(MouseEvent event) {
	}

	public void mouseMove(MouseEvent event) {
	}

	public void focusGained(FocusEvent event) {
	}

	public void focusLost(FocusEvent event) {
		switchMode(Mode.NORMAL_MODE);
	}

}

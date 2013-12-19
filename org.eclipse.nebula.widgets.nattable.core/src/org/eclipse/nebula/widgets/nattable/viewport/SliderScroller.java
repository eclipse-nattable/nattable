/*******************************************************************************
 * Copyright (c) Sep 3, 2013 esp and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    esp - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport;

import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;

public class SliderScroller implements IScroller<Slider> {

	private Slider slider;

	public SliderScroller(Slider slider) {
		this.slider = slider;
	}
	
	@Override
	public Slider getUnderlying() {
		return slider;
	}
	
	@Override
	public boolean isDisposed() {
		return slider.isDisposed();
	}

	@Override
	public void addListener(int eventType, Listener listener) {
		slider.addListener(eventType, listener);
	}

	@Override
	public void removeListener(int eventType, Listener listener) {
		slider.removeListener(eventType, listener);
	}

	@Override
	public int getSelection() {
		return slider.getSelection();
	}
	
	@Override
	public void setSelection(int value) {
		slider.setSelection(value);
	}
	
	@Override
	public int getMaximum() {
		return slider.getMaximum();
	}

	@Override
	public void setMaximum(int value) {
		slider.setMaximum(value);
		slider.update();
	}

	@Override
	public int getPageIncrement() {
		return slider.getPageIncrement();
	}
	
	@Override
	public void setPageIncrement(int value) {
		slider.setPageIncrement(value);
	}
	
	@Override
	public int getThumb() {
		return slider.getThumb();
	}

	@Override
	public void setThumb(int value) {
		slider.setThumb(value);
	}

	@Override
	public int getIncrement() {
		return slider.getIncrement();
	}
	
	@Override
	public void setIncrement(int value) {
		slider.setIncrement(value);
	}

	@Override
	public boolean getEnabled() {
		return slider.getEnabled();
	}

	@Override
	public void setEnabled(boolean b) {
		slider.setEnabled(b);
	}
	
	@Override
	public boolean getVisible() {
		return slider.getVisible();
	}

	@Override
	public void setVisible(boolean b) {
		slider.setVisible(b);
	}

}

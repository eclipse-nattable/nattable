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
        return this.slider;
    }

    @Override
    public boolean isDisposed() {
        return this.slider.isDisposed();
    }

    @Override
    public void addListener(int eventType, Listener listener) {
        this.slider.addListener(eventType, listener);
    }

    @Override
    public void removeListener(int eventType, Listener listener) {
        this.slider.removeListener(eventType, listener);
    }

    @Override
    public int getSelection() {
        return this.slider.getSelection();
    }

    @Override
    public void setSelection(int value) {
        this.slider.setSelection(value);
    }

    @Override
    public int getMaximum() {
        return this.slider.getMaximum();
    }

    @Override
    public void setMaximum(int value) {
        this.slider.setMaximum(value);
        this.slider.update();
    }

    @Override
    public int getPageIncrement() {
        return this.slider.getPageIncrement();
    }

    @Override
    public void setPageIncrement(int value) {
        this.slider.setPageIncrement(value);
    }

    @Override
    public int getThumb() {
        return this.slider.getThumb();
    }

    @Override
    public void setThumb(int value) {
        this.slider.setThumb(value);
    }

    @Override
    public int getIncrement() {
        return this.slider.getIncrement();
    }

    @Override
    public void setIncrement(int value) {
        this.slider.setIncrement(value);
    }

    @Override
    public boolean getEnabled() {
        return this.slider.getEnabled();
    }

    @Override
    public void setEnabled(boolean b) {
        this.slider.setEnabled(b);
    }

    @Override
    public boolean getVisible() {
        return this.slider.getVisible();
    }

    @Override
    public void setVisible(boolean b) {
        this.slider.setVisible(b);
    }

}

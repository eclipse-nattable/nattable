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
package org.eclipse.nebula.widgets.nattable.painter;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.GC;

/**
 * An overlay painter is given a chance to paint the canvas once the layers have
 * finished rendering.
 *
 * @see NatTable#addOverlayPainter(IOverlayPainter)
 */
public interface IOverlayPainter {

    public void paintOverlay(GC gc, ILayer layer);

}

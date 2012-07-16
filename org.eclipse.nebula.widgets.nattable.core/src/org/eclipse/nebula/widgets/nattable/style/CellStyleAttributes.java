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
package org.eclipse.nebula.widgets.nattable.style;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public interface CellStyleAttributes {
    
    public static final ConfigAttribute<Color> BACKGROUND_COLOR = new ConfigAttribute<Color>();
    
    public static final ConfigAttribute<Color> FOREGROUND_COLOR = new ConfigAttribute<Color>();
    
    /**
     * Attribute for configuring the gradient sweeping background color.
     * Is used by the GradientBackgroundPainter.
     */
    public static final ConfigAttribute<Color> GRADIENT_BACKGROUND_COLOR = new ConfigAttribute<Color>();
    
    /**
     * Attribute for configuring the gradient sweeping foreground color.
     * Is used by the GradientBackgroundPainter.
     */
    public static final ConfigAttribute<Color> GRADIENT_FOREGROUND_COLOR = new ConfigAttribute<Color>();
    
    public static final ConfigAttribute<HorizontalAlignmentEnum> HORIZONTAL_ALIGNMENT = new ConfigAttribute<HorizontalAlignmentEnum>();
    
    public static final ConfigAttribute<VerticalAlignmentEnum> VERTICAL_ALIGNMENT = new ConfigAttribute<VerticalAlignmentEnum>();
    
    public static final ConfigAttribute<Font> FONT = new ConfigAttribute<Font>();
    
    public static final ConfigAttribute<Image> IMAGE = new ConfigAttribute<Image>();
    
    public static final ConfigAttribute<BorderStyle> BORDER_STYLE = new ConfigAttribute<BorderStyle>();
    
    /**
     * Attribute for configuring the echo character that should be used by PasswordTextPainter and
     * PassowrdCellEditor.
     */
	public static final ConfigAttribute<Character> PASSWORD_ECHO_CHAR = new ConfigAttribute<Character>();
}

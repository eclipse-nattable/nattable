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

    /**
     * Attribute for configuring the background color of a cell.
     */
    public static final ConfigAttribute<Color> BACKGROUND_COLOR = new ConfigAttribute<Color>();

    /**
     * Attribute for configuring the foreground color of a cell.
     */
    public static final ConfigAttribute<Color> FOREGROUND_COLOR = new ConfigAttribute<Color>();

    /**
     * Attribute for configuring the gradient sweeping background color. Is used
     * by the GradientBackgroundPainter.
     */
    public static final ConfigAttribute<Color> GRADIENT_BACKGROUND_COLOR = new ConfigAttribute<Color>();

    /**
     * Attribute for configuring the gradient sweeping foreground color. Is used
     * by the GradientBackgroundPainter.
     */
    public static final ConfigAttribute<Color> GRADIENT_FOREGROUND_COLOR = new ConfigAttribute<Color>();

    /**
     * Attribute for configuring the horizontal alignment of a cell.
     */
    public static final ConfigAttribute<HorizontalAlignmentEnum> HORIZONTAL_ALIGNMENT = new ConfigAttribute<HorizontalAlignmentEnum>();

    /**
     * Attribute for configuring the vertical alignment of a cell.
     */
    public static final ConfigAttribute<VerticalAlignmentEnum> VERTICAL_ALIGNMENT = new ConfigAttribute<VerticalAlignmentEnum>();

    /**
     * Attribute for configuring the font to be used on rendering text. Is used
     * by all specialisations of the AbstractTextPainter.
     */
    public static final ConfigAttribute<Font> FONT = new ConfigAttribute<Font>();

    /**
     * Attribute for configuring the image to rendered. Is used by the
     * ImagePainter to determine the image to render dynamically.
     */
    public static final ConfigAttribute<Image> IMAGE = new ConfigAttribute<Image>();

    /**
     * Attribute for configuring the border style. Is used by the
     * LineBorderDecorator.
     */
    public static final ConfigAttribute<BorderStyle> BORDER_STYLE = new ConfigAttribute<BorderStyle>();

    /**
     * Attribute for configuring the echo character that should be used by
     * PasswordTextPainter and PasswordCellEditor.
     */
    public static final ConfigAttribute<Character> PASSWORD_ECHO_CHAR = new ConfigAttribute<Character>();

    /**
     * Attribute for configuring the text decoration (underline and/or
     * strikethrough). Is used by all specialisations of the AbstractTextPainter
     */
    public static final ConfigAttribute<TextDecorationEnum> TEXT_DECORATION = new ConfigAttribute<TextDecorationEnum>();
}

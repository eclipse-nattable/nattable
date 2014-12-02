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
package org.eclipse.nebula.widgets.nattable.persistence;

import static org.eclipse.nebula.widgets.nattable.persistence.IPersistable.DOT;

import java.util.Properties;

import org.eclipse.jface.resource.DataFormatException;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ColorPersistor {

    public static final String STYLE_PERSISTENCE_PREFIX = "color"; //$NON-NLS-1$
    public static final Color DEFAULT_COLOR = Display.getDefault()
            .getSystemColor(SWT.COLOR_WHITE);

    public static void saveColor(String prefix, Properties properties,
            Color color) {
        prefix = prefix + DOT + STYLE_PERSISTENCE_PREFIX;

        if (color == null) {
            return;
        }
        properties.setProperty(prefix, asString(color));
    }

    public static Color loadColor(String prefix, Properties properties) {
        prefix = prefix + DOT + STYLE_PERSISTENCE_PREFIX;

        String colorAsString = properties.getProperty(prefix);
        if (colorAsString == null) {
            return null;
        } else {
            return asColor(colorAsString);
        }
    }

    /**
     * Create a String representation of the SWT Color
     */
    public static String asString(Color color) {
        return StringConverter.asString(color.getRGB());
    }

    /**
     * Create a Color instance using the String created by
     * {@link ColorPersistor#asColor(String)}
     */
    public static Color asColor(String colorAsString) {
        try {
            return GUIHelper.getColor(StringConverter.asRGB(colorAsString));
        } catch (DataFormatException e) {
            return DEFAULT_COLOR;
        }
    }
}

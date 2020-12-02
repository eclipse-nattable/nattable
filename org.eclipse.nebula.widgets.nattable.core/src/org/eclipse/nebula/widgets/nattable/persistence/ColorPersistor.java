/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

public final class ColorPersistor {

    private ColorPersistor() {
        // private default constructor for helper class
    }

    public static final String STYLE_PERSISTENCE_PREFIX = "color"; //$NON-NLS-1$
    public static final Color DEFAULT_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

    public static void saveColor(String prefix, Properties properties, Color color) {
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
     *
     * @param color
     *            The {@link Color} for which the String representation is
     *            requested.
     * @return The String representation of the provided SWT {@link Color}.
     */
    public static String asString(Color color) {
        return StringConverter.asString(color.getRGB());
    }

    /**
     * Create a Color instance using the String created by
     * {@link ColorPersistor#asColor(String)}.
     *
     * @param colorAsString
     *            The String representation of a SWT {@link Color}.
     * @return The SWT {@link Color} for the given String.
     */
    public static Color asColor(String colorAsString) {
        try {
            return GUIHelper.getColor(StringConverter.asRGB(colorAsString));
        } catch (DataFormatException e) {
            return DEFAULT_COLOR;
        }
    }
}

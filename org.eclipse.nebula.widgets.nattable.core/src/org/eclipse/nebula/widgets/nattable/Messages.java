/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 * Class that is used to get the NatTable internal localized messages.
 */
public class Messages {

    private static final String BUNDLE_NAME = "org.eclipse.nebula.widgets.nattable.messages"; //$NON-NLS-1$
    private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {}

    /**
     * Returns the translation for the given key according to the current set
     * {@link Locale}.
     *
     * @param key
     *            The NatTable internal translation key.
     * @return The localized String for the given key and the current set
     *         {@link Locale}.
     */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    /**
     * Returns the translation for the given key according to the current set
     * {@link Locale}. It uses {@link MessageFormat} to replace placeholders
     * with the given parameter values.
     *
     * @param key
     *            The NatTable internal translation key.
     * @param args
     *            The parameters that should be used to replace placeholders.
     * @return The localized String for the given key and the current set
     *         {@link Locale} with replaced placeholders.
     */
    public static String getString(String key, Object... args) {
        return MessageFormat.format(RESOURCE_BUNDLE.getString(key), args);
    }

    /**
     * Checks whether the given message starts with a % sign. If it starts with
     * a percentage sign, the message is treated as properties key and it is
     * tried to retrieve the localization. Otherwise the given message is
     * returned.
     * <p>
     * This method returns <code>null</code> in case the given message parameter
     * is <code>null</code>.
     * </p>
     *
     * @param message
     *            The message for which the localized version is requested
     * @return The localized message if the given value starts with % or the
     *         given value itself
     *
     * @since 1.4
     */
    public static String getLocalizedMessage(String message) {
        if (message != null && message.startsWith("%")) { //$NON-NLS-1$
            return Messages.getString(message.substring(1));
        }
        return message;
    }

    /**
     * Updates the internal used {@link ResourceBundle} for the given
     * {@link Locale}.
     *
     * @param locale
     *            The {@link Locale} that should be used by NatTable internally.
     * @since 1.4
     */
    public static void changeLocale(Locale locale) {
        RESOURCE_BUNDLE = ResourceBundle.getBundle(
                BUNDLE_NAME,
                locale,
                ResourceBundle.Control.getNoFallbackControl(Control.FORMAT_DEFAULT));
    }
}

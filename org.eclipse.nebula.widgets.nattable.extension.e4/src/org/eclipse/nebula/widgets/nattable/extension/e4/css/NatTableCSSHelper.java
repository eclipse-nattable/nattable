/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.e4.css;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.ui.css.core.css2.CSS2ColorHelper;
import org.eclipse.e4.ui.css.core.css2.CSS2FontHelper;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.engine.CSSElementContext;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBigDecimalDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBigIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultByteDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultCharacterDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultFloatDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultLongDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultShortDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.PercentageDisplayConverter;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.BorderModeEnum;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleProxy;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DefaultDisplayModeOrdering;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IDisplayModeOrdering;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

/**
 * Helper class for converting and applying CSS styles.
 */
@SuppressWarnings("restriction")
public final class NatTableCSSHelper {

    private NatTableCSSHelper() {
        // private default constructor for helper class
    }

    private static final IDisplayModeOrdering displayModeOrdering = new DefaultDisplayModeOrdering();

    /**
     * Return the <code>boolean</code> value that represents the given
     * {@link CSSValue}.
     *
     * @param value
     *            The value for which the boolean value should be returned.
     * @param defaultValue
     *            The default value to return if the given value can not be
     *            converted to a boolean.
     * @return The boolean representation of the given value or the default
     *         value if it can not be converted.
     */
    public static boolean getBoolean(CSSValue value, boolean defaultValue) {
        String stringValue = value.getCssText().toLowerCase();
        Boolean result = defaultValue;
        if ("true".equals(stringValue)) {
            result = true;
        } else if ("false".equals(stringValue)) {
            result = false;
        }
        return result;
    }

    /**
     * Returns the NatTable {@link DisplayMode} for the given pseudo class.
     *
     * @param pseudo
     *            The pseudo class.
     * @return The {@link DisplayMode} value for the given pseudo class.
     */
    public static String getDisplayMode(String pseudo) {
        if (pseudo != null) {
            if ("select".equals(pseudo)) {
                return DisplayMode.SELECT;
            } else if ("edit".equals(pseudo)) {
                return DisplayMode.EDIT;
            } else if ("hover".equals(pseudo)) {
                return DisplayMode.HOVER;
            } else if ("select-hover".equals(pseudo)) {
                return DisplayMode.SELECT_HOVER;
            }
        }
        return DisplayMode.NORMAL;
    }

    /**
     * Returns the NatTable {@link CellEdgeEnum} for the given string
     * representation.
     *
     * @param value
     *            The string representation of the {@link CellEdgeEnum}
     * @return The {@link CellEdgeEnum} for the string representation.
     */
    public static CellEdgeEnum getCellEdgeEnum(String value) {
        if ("top".equalsIgnoreCase(value)) {
            return CellEdgeEnum.TOP;
        } else if ("bottom".equalsIgnoreCase(value)) {
            return CellEdgeEnum.BOTTOM;
        } else if ("right".equalsIgnoreCase(value)) {
            return CellEdgeEnum.RIGHT;
        } else if ("left".equalsIgnoreCase(value)) {
            return CellEdgeEnum.LEFT;
        } else if ("top-right".equalsIgnoreCase(value)) {
            return CellEdgeEnum.TOP_RIGHT;
        } else if ("top-left".equalsIgnoreCase(value)) {
            return CellEdgeEnum.TOP_LEFT;
        } else if ("bottom-right".equalsIgnoreCase(value)) {
            return CellEdgeEnum.BOTTOM_RIGHT;
        } else if ("bottom-left".equalsIgnoreCase(value)) {
            return CellEdgeEnum.BOTTOM_LEFT;
        }

        return CellEdgeEnum.NONE;
    }

    /**
     * Returns the NatTable {@link IDisplayConverter} for the given string
     * representation.
     *
     * @param value
     *            The string representation of the {@link IDisplayConverter}
     * @param format
     *            flag for number display converters to specify whether
     *            {@link NumberFormat} should be used to format or not.
     * @param minFractionDigits
     *            number of minimum fraction digits to use in case a
     *            {@link NumberFormat} is used for formatting a number value
     * @param maxFractionDigits
     *            number of maximum fraction digits to use in case a
     *            {@link NumberFormat} is used for formatting a number value
     * @param datePattern
     *            The pattern to use for formatting a date value when a date
     *            display converter requested
     * @return The {@link IDisplayConverter} for the string representation.
     */
    public static IDisplayConverter getDisplayConverter(
            String value,
            boolean format,
            Integer minFractionDigits,
            Integer maxFractionDigits,
            String datePattern) {

        if ("boolean".equalsIgnoreCase(value)) {
            return new DefaultBooleanDisplayConverter();
        } else if ("character".equalsIgnoreCase(value)) {
            return new DefaultCharacterDisplayConverter();
        } else if ("date".equalsIgnoreCase(value)) {
            return new DefaultDateDisplayConverter(datePattern);
        } else if ("default".equalsIgnoreCase(value)) {
            return new DefaultDisplayConverter();
        } else if ("percentage".equalsIgnoreCase(value)) {
            return new PercentageDisplayConverter();
        } else if ("byte".equalsIgnoreCase(value)) {
            return new DefaultByteDisplayConverter();
        } else if ("short".equalsIgnoreCase(value)) {
            return new DefaultShortDisplayConverter(format);
        } else if ("int".equalsIgnoreCase(value)) {
            return new DefaultIntegerDisplayConverter(format);
        } else if ("long".equalsIgnoreCase(value)) {
            return new DefaultLongDisplayConverter(format);
        } else if ("big-int".equalsIgnoreCase(value)) {
            return new DefaultBigIntegerDisplayConverter();
        } else if ("float".equalsIgnoreCase(value)) {
            DefaultFloatDisplayConverter result = new DefaultFloatDisplayConverter(format);
            if (minFractionDigits != null) {
                result.getNumberFormat().setMinimumFractionDigits(minFractionDigits);
            }
            if (maxFractionDigits != null) {
                result.getNumberFormat().setMaximumFractionDigits(maxFractionDigits);
            }
            return result;
        } else if ("double".equalsIgnoreCase(value)) {
            DefaultDoubleDisplayConverter result = new DefaultDoubleDisplayConverter(format);
            if (minFractionDigits != null) {
                result.getNumberFormat().setMinimumFractionDigits(minFractionDigits);
            }
            if (maxFractionDigits != null) {
                result.getNumberFormat().setMaximumFractionDigits(maxFractionDigits);
            }
            return result;
        } else if ("big-decimal".equalsIgnoreCase(value)) {
            DefaultBigDecimalDisplayConverter result = new DefaultBigDecimalDisplayConverter();
            if (minFractionDigits != null) {
                result.getNumberFormat().setMinimumFractionDigits(minFractionDigits);
            }
            if (maxFractionDigits != null) {
                result.getNumberFormat().setMaximumFractionDigits(maxFractionDigits);
            }
            return result;
        }

        return new DefaultDateDisplayConverter();
    }

    /**
     *
     * @param converter
     *            The {@link IDisplayConverter} for which the String
     *            representation is requested.
     * @return The String representation for the given
     *         {@link IDisplayConverter}.
     */
    public static String getDisplayConverterString(IDisplayConverter converter) {
        if (converter instanceof DefaultBooleanDisplayConverter) {
            return "boolean";
        } else if (converter instanceof DefaultCharacterDisplayConverter) {
            return "character";
        } else if (converter instanceof DefaultDateDisplayConverter) {
            return "date";
        } else if (converter instanceof PercentageDisplayConverter) {
            return "percentage";
        } else if (converter instanceof DefaultByteDisplayConverter) {
            return "byte";
        } else if (converter instanceof DefaultShortDisplayConverter) {
            return "short";
        } else if (converter instanceof DefaultIntegerDisplayConverter) {
            return "int";
        } else if (converter instanceof DefaultLongDisplayConverter) {
            return "long";
        } else if (converter instanceof DefaultBigIntegerDisplayConverter) {
            return "big-int";
        } else if (converter instanceof DefaultFloatDisplayConverter) {
            return "float";
        } else if (converter instanceof DefaultDoubleDisplayConverter) {
            return "double";
        } else if (converter instanceof DefaultBigDecimalDisplayConverter) {
            return "big-decimal";
        }

        return "default";
    }

    /**
     *
     * @param value
     *            The value to check.
     * @return <code>true</code> if the given value represents a valid converter
     *         key, <code>false</code> if not.
     */
    public static boolean isConverterString(String value) {
        return ("boolean".equalsIgnoreCase(value)
                || "character".equalsIgnoreCase(value)
                || "date".equalsIgnoreCase(value)
                || "default".equalsIgnoreCase(value)
                || "percentage".equalsIgnoreCase(value)
                || "byte".equalsIgnoreCase(value)
                || "short".equalsIgnoreCase(value)
                || "int".equalsIgnoreCase(value)
                || "long".equalsIgnoreCase(value)
                || "big-int".equalsIgnoreCase(value)
                || "float".equalsIgnoreCase(value)
                || "double".equalsIgnoreCase(value)
                || "big-decimal".equalsIgnoreCase(value));
    }

    /**
     * Retrieves the style attribute for the given display mode and config
     * labels out of the NatTable configuration. Uses the NatTable internal
     * inheritance model to always retrieve a style configuration attribute if
     * there is one configured at any level.
     *
     * @param <T>
     *            The type of the configuration attribute.
     * @param natTable
     *            The NatTable whose {@link ConfigRegistry} should be checked
     *            for the style configuration.
     * @param styleConfig
     *            The style {@link ConfigAttribute} that is requested.
     * @param displayMode
     *            The {@link DisplayMode} for which the configuration is
     *            requested.
     * @param configLabels
     *            The config labels for which the configuration is requested.
     * @return The style attribute for the given display mode and config labels
     *         out of the NatTable configuration.
     */
    public static <T> T getNatTableStyle(NatTable natTable, ConfigAttribute<T> styleConfig, String displayMode, String... configLabels) {
        IConfigRegistry configRegistry = natTable.getConfigRegistry();

        CellStyleProxy style = new CellStyleProxy(configRegistry, displayMode, Arrays.asList(configLabels));

        return style.getAttributeValue(styleConfig, CellStyleAttributes.FONT.equals(styleConfig));
    }

    /**
     * Apply a style attribute value for the {@link IStyle} registered for
     * {@link CellConfigAttributes#CELL_STYLE}.
     *
     * @param <T>
     *            The type of the configuration attribute.
     * @param natTable
     *            The NatTable to apply the style configuration to.
     * @param styleConfig
     *            The style {@link ConfigAttribute} that should be applied.
     * @param value
     *            The value to apply.
     * @param displayMode
     *            The {@link DisplayMode} for which the configuration should be
     *            applied.
     * @param configLabel
     *            The label for which the configuration should be applied.
     */
    public static <T> void applyNatTableStyle(
            NatTable natTable,
            ConfigAttribute<T> styleConfig,
            T value,
            String displayMode,
            String configLabel) {

        applyNatTableStyle(natTable, CellConfigAttributes.CELL_STYLE, styleConfig, value, displayMode, configLabel);
    }

    /**
     * Apply a style attribute value for the {@link IStyle} registered for the
     * given styleAttribute.
     *
     * @param <T>
     *            The type of the configuration attribute.
     * @param natTable
     *            The NatTable to apply the style configuration to.
     * @param styleAttribute
     *            The {@link ConfigAttribute} that points to the {@link IStyle}
     *            that should be configured.
     * @param styleConfig
     *            The style {@link ConfigAttribute} that should be applied.
     * @param value
     *            The value to apply.
     * @param displayMode
     *            The {@link DisplayMode} for which the configuration should be
     *            applied.
     * @param configLabel
     *            The label for which the configuration should be applied.
     */
    public static <T> void applyNatTableStyle(
            NatTable natTable,
            ConfigAttribute<IStyle> styleAttribute,
            ConfigAttribute<T> styleConfig,
            T value,
            String displayMode,
            String configLabel) {

        IConfigRegistry configRegistry = natTable.getConfigRegistry();

        // retrieve the style object for the given selector
        IStyle style = configRegistry.getSpecificConfigAttribute(
                styleAttribute,
                displayMode,
                configLabel);

        if (style == null) {
            style = new Style();
            if (configLabel != null) {
                configRegistry.registerConfigAttribute(
                        styleAttribute,
                        style,
                        displayMode,
                        configLabel);
            } else {
                configRegistry.registerConfigAttribute(
                        styleAttribute,
                        style,
                        displayMode);
            }
        }

        // set the value to the style object
        style.setAttributeValue(
                styleConfig,
                value);
    }

    /**
     * Returns the {@link CSS2FontProperties} out of the given
     * {@link CSSElementContext} for the given parameters. If no
     * {@link CSS2FontProperties} exist, a new instance will be created and
     * pre-filled with font values based on inheritance.
     *
     * @param context
     *            The {@link CSSElementContext} to search for the context value.
     * @param contextKey
     *            The context key under which the font properties are stored.
     * @param natTable
     *            The NatTable instance to apply the styles to
     * @param displayMode
     *            The target {@link DisplayMode} to check for the value.
     * @param label
     *            The label for which the font properties are requested.
     * @return The {@link CSS2FontProperties} for the given attributes.
     */
    public static CSS2FontProperties getFontProperties(
            CSSElementContext context, String contextKey,
            NatTable natTable, String displayMode, String label) {
        // check if there are font properties already registered
        CSS2FontProperties fontProperties = (CSS2FontProperties) getContextValue(context, displayMode, contextKey);
        if (fontProperties == null) {
            // check if there is a font registered in the hierarchy
            Font font = getNatTableStyle(natTable, CellStyleAttributes.FONT, displayMode, label);

            if (font == null) {
                // if there are no font properties already and no font, use the
                // default font
                font = GUIHelper.DEFAULT_FONT;
            }

            fontProperties = CSSSWTFontHelper.getCSS2FontProperties(font);
            // store the font properties for further use
            storeContextValue(context, displayMode, contextKey, fontProperties);
        }
        return fontProperties;
    }

    /**
     * Sets the values from the given {@link CSSValueList} to the given
     * {@link CSS2FontProperties}CSS2FontProperties.
     *
     * @param valueList
     *            The value list containing the css property values
     * @param font
     *            The font properties to set the values to
     */
    public static void setFontProperties(CSSValueList valueList, CSS2FontProperties font) {
        int length = valueList.getLength();
        for (int i = 0; i < length; i++) {
            CSSValue value2 = valueList.item(i);
            if (value2.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                String fontProperty = CSS2FontHelper.getCSSFontPropertyName((CSSPrimitiveValue) value2);
                if (fontProperty != null) {
                    if (NatTableCSSConstants.FONT_FAMILY.equalsIgnoreCase(fontProperty)) {
                        font.setFamily((CSSPrimitiveValue) value2);
                    } else if (NatTableCSSConstants.FONT_SIZE.equalsIgnoreCase(fontProperty)) {
                        font.setSize((CSSPrimitiveValue) value2);
                    } else if (NatTableCSSConstants.FONT_STYLE.equalsIgnoreCase(fontProperty)) {
                        font.setStyle((CSSPrimitiveValue) value2);
                    } else if (NatTableCSSConstants.FONT_WEIGHT.equalsIgnoreCase(fontProperty)) {
                        font.setWeight((CSSPrimitiveValue) value2);
                    }
                }
            }
        }
    }

    /**
     * Returns the {@link BorderStyle} out of the given
     * {@link CSSElementContext} for the given {@link DisplayMode}.
     *
     * @param context
     *            The {@link CSSElementContext} to search for the context value.
     * @param displayMode
     *            The {@link DisplayMode} for which the value should be stored.
     * @return The {@link BorderStyle}
     */
    public static BorderStyle getBorderStyle(CSSElementContext context, String displayMode) {
        BorderStyle borderStyle = (BorderStyle) getContextValue(context, displayMode, NatTableCSSConstants.CV_BORDER_CONFIGURATION);

        if (borderStyle == null) {
            borderStyle = new BorderStyle();
            storeContextValue(context, displayMode, NatTableCSSConstants.CV_BORDER_CONFIGURATION, borderStyle);
        }

        return borderStyle;
    }

    /**
     * Convert and store the values of the given {@link CSSValueList} to the
     * given {@link BorderStyle}.
     *
     * @param valueList
     *            The {@link CSSValueList} with the values to convert.
     * @param borderStyle
     *            The {@link BorderStyle} to store the converted values to.
     * @param engine
     *            The {@link CSSEngine} needed for conversion.
     * @param display
     *            The display needed for color conversion.
     * @throws Exception
     *             if the value conversion fails
     */
    public static void storeBorderStyle(
            CSSValueList valueList,
            BorderStyle borderStyle,
            CSSEngine engine,
            Display display) throws Exception {

        int length = valueList.getLength();
        for (int i = 0; i < length; i++) {
            CSSValue value2 = valueList.item(i);
            if (value2.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value2;
                short type = primitiveValue.getPrimitiveType();
                switch (type) {
                    case CSSPrimitiveValue.CSS_IDENT:
                        if (CSS2ColorHelper.isColorName(primitiveValue.getStringValue())) {
                            borderStyle.setColor(
                                    (Color) engine.convert(value2, Color.class, display));
                        } else if (isLineStyle(primitiveValue.getStringValue().toUpperCase())) {
                            borderStyle.setLineStyle(
                                    LineStyleEnum.valueOf(primitiveValue.getStringValue().toUpperCase()));
                        } else {
                            borderStyle.setBorderMode(
                                    BorderModeEnum.valueOf(primitiveValue.getStringValue().toUpperCase()));
                        }
                        break;
                    case CSSPrimitiveValue.CSS_RGBCOLOR:
                        borderStyle.setColor(
                                (Color) engine.convert(value2, Color.class, display));
                        break;
                    case CSSPrimitiveValue.CSS_PT:
                    case CSSPrimitiveValue.CSS_NUMBER:
                    case CSSPrimitiveValue.CSS_PX:
                        borderStyle.setThickness(
                                (int) ((CSSPrimitiveValue) value2).getFloatValue(CSSPrimitiveValue.CSS_PT));
                        break;
                }
            }
        }
    }

    /**
     *
     * @param style
     *            The String value that should be checked if it is a
     *            {@link LineStyleEnum} value.
     * @return <code>true</code> if the given String is a {@link LineStyleEnum},
     *         <code>false</code> if not.
     */
    private static boolean isLineStyle(String style) {
        try {
            LineStyleEnum.valueOf(style);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /**
     * Convert and store the padding value for the given key in the painter
     * properties map.
     *
     * @param paddingKey
     *            The key of the padding property.
     * @param value
     *            The {@link CSSValue} of the padding that should be converted
     *            and stored.
     * @param context
     *            The {@link CSSElementContext} to search for the configuration
     *            value.
     * @param displayMode
     *            The target {@link DisplayMode} to check for the value.
     */
    public static void storePadding(
            String paddingKey,
            CSSValue value,
            CSSElementContext context,
            String displayMode) {

        if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
            CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value;
            short type = primitiveValue.getPrimitiveType();

            if (type == CSSPrimitiveValue.CSS_PT
                    || type == CSSPrimitiveValue.CSS_NUMBER
                    || type == CSSPrimitiveValue.CSS_PX) {

                int padding = (int) primitiveValue.getFloatValue(CSSPrimitiveValue.CSS_PT);
                getPainterProperties(context, displayMode)
                        .put(paddingKey, padding);
            }
        }
    }

    /**
     * Check if the automatic painter resolution is enabled or disabled via CSS
     * property.
     *
     * @param context
     *            The {@link CSSElementContext} to search for the configuration
     *            value.
     * @param natTableContext
     *            The {@link CSSElementContext} of the parent NatTable or
     *            <code>null</code> in case the context parameter is already
     *            from the NatTable itself.
     * @param displayMode
     *            The target {@link DisplayMode} to check for the value.
     * @return <code>true</code> if the painter to use should be automatically
     *         resolved, <code>false</code> if the painter is configured
     *         elsewhere and therefore no automatic resolution should be
     *         performed. Default is <code>true</code>.
     */
    public static boolean resolvePainter(
            CSSElementContext context, CSSElementContext natTableContext, String displayMode) {

        Object cv = getContextValueInherited(
                context,
                natTableContext,
                displayMode,
                NatTableCSSConstants.PAINTER_RESOLUTION);

        return (cv != null) ? (Boolean) cv : Boolean.TRUE;
    }

    /**
     * Returns the string representation list of cell painter decorators that
     * should be used to build up the cell painter for rendering.
     *
     * @param context
     *            The {@link CSSElementContext} to search for the context value.
     * @param displayMode
     *            The {@link DisplayMode} for which the value should be stored.
     * @return The string representation of decorator painter that should be
     *         used for rendering.
     */
    public static List<String> getDecoratorPainter(CSSElementContext context, String displayMode) {
        @SuppressWarnings("unchecked")
        List<String> decorator = (List<String>) getContextValue(context, displayMode, NatTableCSSConstants.CV_DECORATOR_PAINTER);

        if (decorator == null) {
            decorator = new ArrayList<>();
            storeContextValue(context, displayMode, NatTableCSSConstants.CV_DECORATOR_PAINTER, decorator);
        }

        return decorator;
    }

    /**
     * Returns the painter properties out of the given {@link CSSElementContext}
     * for the given {@link DisplayMode}.
     *
     * @param context
     *            The {@link CSSElementContext} to search for the context value.
     * @param displayMode
     *            The {@link DisplayMode} for which the value should be stored.
     * @return The properties that should be used to create content painter.
     */
    public static Map<String, Object> getPainterProperties(CSSElementContext context, String displayMode) {
        @SuppressWarnings("unchecked")
        Map<String, Object> painterProperties =
                (Map<String, Object>) getContextValue(context, displayMode, NatTableCSSConstants.CV_PAINTER_CONFIGURATION);

        if (painterProperties == null) {
            painterProperties = new HashMap<>();
            storeContextValue(context, displayMode, NatTableCSSConstants.CV_PAINTER_CONFIGURATION, painterProperties);
        }

        return painterProperties;
    }

    /**
     * Returns the painter properties out of the given {@link CSSElementContext}
     * for the given {@link DisplayMode} enriched with the painter property
     * values out of inheritance.
     *
     * @param context
     *            The {@link CSSElementContext} to search for the context value.
     * @param natTableContext
     *            The {@link CSSElementContext} of the parent NatTable or
     *            <code>null</code> in case the context parameter is already
     *            from the NatTable itself.
     * @param targetDisplayMode
     *            The target {@link DisplayMode} to check for the value.
     * @return The painter properties with values out of inheritance
     */
    public static Map<String, Object> getPainterPropertiesInherited(
            CSSElementContext context, CSSElementContext natTableContext, String targetDisplayMode) {

        Map<String, Object> painterProperties = new HashMap<>();

        List<String> displayModes = displayModeOrdering.getDisplayModeOrdering(targetDisplayMode);
        String displayMode = null;

        if (natTableContext != null) {
            // first get the painter properties from tbe NatTable in reverse
            // displaymode ordering
            for (int i = displayModes.size() - 1; i >= 0; i--) {
                displayMode = displayModes.get(i);
                painterProperties.putAll(
                        getPainterProperties(natTableContext, displayMode));
            }
        }

        // then do the same for the element context
        for (int i = displayModes.size() - 1; i >= 0; i--) {
            displayMode = displayModes.get(i);
            painterProperties.putAll(
                    getPainterProperties(context, displayMode));
        }

        return painterProperties;
    }

    /**
     * Resolves the painter representations out of the given {@link CSSValue}.
     *
     * @param value
     *            The {@link CSSValue} to resolve.
     * @return The list of string representations for painters.
     */
    public static List<String> resolvePainterRepresentation(CSSValue value) {
        List<String> painterValues = new ArrayList<>();
        if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
            CSSValueList valueList = (CSSValueList) value;
            int length = valueList.getLength();
            for (int i = 0; i < length; i++) {
                CSSValue value2 = valueList.item(i);
                if (value2.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                    painterValues.add(value2.getCssText().toLowerCase());
                }
            }
        } else if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
            painterValues.add(value.getCssText().toLowerCase());
        }
        return painterValues;
    }

    /**
     * Put a value for the given key and {@link DisplayMode} to the given
     * {@link CSSElementContext}.
     *
     * @param context
     *            The {@link CSSElementContext} to put the value to.
     * @param displayMode
     *            The {@link DisplayMode} for which the value should be stored.
     * @param key
     *            The key for which the value should be stored.
     * @param value
     *            The value to store.
     */
    public static void storeContextValue(CSSElementContext context, String displayMode, Object key, Object value) {
        Object subContext = context.getData(displayMode);
        if (subContext == null) {
            subContext = new HashMap<>();
            context.setData(displayMode, subContext);
        }

        @SuppressWarnings("unchecked")
        Map<Object, Object> displayModeContext = (Map<Object, Object>) subContext;
        displayModeContext.put(key, value);
    }

    /**
     * Search the value for the given key and {@link DisplayMode} out of the
     * given {@link CSSElementContext}.
     *
     * @param context
     *            The {@link CSSElementContext} out of which the value should be
     *            retrieved.
     * @param displayMode
     *            The {@link DisplayMode} for which the value is requested.
     * @param key
     *            The key for which the value is requested.
     * @return The value for the given {@link DisplayMode} and key out of the
     *         given {@link CSSElementContext}. If no value is found for the
     *         given {@link DisplayMode}, the value out of the given
     *         {@link CSSElementContext} directly is returned. Can be
     *         <code>null</code>.
     */
    public static Object getContextValue(CSSElementContext context, String displayMode, Object key) {
        Object subContext = context.getData(displayMode);
        if (subContext != null) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> displayModeContext = (Map<Object, Object>) subContext;
            return displayModeContext.get(key);
        }
        return context.getData(key);
    }

    /**
     * Search the value for the given key and {@link DisplayMode} out of the
     * given {@link CSSElementContext}. Will search for the value also via
     * inheritance, that means first it will check the
     * {@link IDisplayModeOrdering} and if there is no value found, it searches
     * in the parent NatTable context.
     *
     * @param context
     *            The {@link CSSElementContext} to search for the context value.
     * @param natTableContext
     *            The {@link CSSElementContext} of the parent NatTable or
     *            <code>null</code> in case the context parameter is already
     *            from the NatTable itself.
     * @param targetDisplayMode
     *            The target {@link DisplayMode} to check for the value.
     * @param key
     *            The key of the context value to search for.
     * @return The context value for the given key.
     */
    public static Object getContextValueInherited(
            CSSElementContext context, CSSElementContext natTableContext,
            String targetDisplayMode, Object key) {

        Object cv = null;

        for (String displayMode : displayModeOrdering.getDisplayModeOrdering(targetDisplayMode)) {
            cv = getContextValue(context, displayMode, key);
            if (cv != null) {
                break;
            }
        }

        // not found for any displaymode in context
        // search in parent context
        if (cv == null && natTableContext != null) {
            cv = getContextValueInherited(natTableContext, null, targetDisplayMode, key);
        }

        return cv;
    }
}

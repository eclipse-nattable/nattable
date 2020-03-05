/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.e4.css;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.ui.css.core.css2.CSS2FontPropertiesHelpers;
import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler2;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverter;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.engine.CSSElementContext;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.util.resources.IResourcesLocatorManager;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper;
import org.eclipse.e4.ui.css.swt.properties.converters.CSSValueSWTGradientConverterImpl;
import org.eclipse.e4.ui.css.swt.properties.css2.CSSPropertyBackgroundSWTHandler;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.extension.e4.painterfactory.CellPainterFactory;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfigAttributes;
import org.eclipse.nebula.widgets.nattable.freeze.IFreezeConfigAttributes;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.painter.NatTableBorderOverlayPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.AbstractTextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.VerticalTextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PercentageBarDecorator;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnSizeConfigurationCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowSizeConfigurationCommand;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.BorderModeEnum;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.TextDecorationEnum;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.tree.config.TreeConfigAttributes;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

@SuppressWarnings("restriction")
public class NatTableCSSHandler implements ICSSPropertyHandler, ICSSPropertyHandler2 {

    @Override
    public boolean applyCSSProperty(Object element, String property, CSSValue value, String pseudo, CSSEngine engine)
            throws Exception {

        Object control = null;

        if (element instanceof CSSStylableElement) {
            CSSStylableElement elt = (CSSStylableElement) element;
            control = elt.getNativeWidget();
        }

        NatTable natTable = null;
        String label = null;
        String displayMode = NatTableCSSHelper.getDisplayMode(pseudo);

        CSSElementContext context = null;
        CSSElementContext natTableContext = null;

        if (control instanceof NatTable) {
            natTable = (NatTable) control;
        } else if (control instanceof NatTableWrapper) {
            natTable = ((NatTableWrapper) control).getNatTable();
            label = ((NatTableWrapper) control).getLabel();
            natTableContext = engine.getCSSElementContext(natTable);
        }

        if (natTable != null) {
            context = engine.getCSSElementContext(control);
            Boolean resolvePainter = NatTableCSSHelper.resolvePainter(context, natTableContext, displayMode);

            // check property
            if (NatTableCSSConstants.PAINTER_RESOLUTION.equalsIgnoreCase(property)) {
                NatTableCSSHelper.storeContextValue(
                        context,
                        displayMode,
                        NatTableCSSConstants.PAINTER_RESOLUTION,
                        NatTableCSSHelper.getBoolean(value, true));
            } else if (NatTableCSSConstants.PAINTER.equalsIgnoreCase(property)) {
                NatTableCSSHelper.storeContextValue(
                        context,
                        displayMode,
                        NatTableCSSConstants.PAINTER,
                        NatTableCSSHelper.resolvePainterRepresentation(value));
            } else if (NatTableCSSConstants.BACKGROUND_COLOR.equalsIgnoreCase(property)) {
                CSSPropertyBackgroundSWTHandler.INSTANCE.applyCSSPropertyBackgroundColor(element, value, pseudo, engine);
            } else if (NatTableCSSConstants.BACKGROUND_IMAGE.equalsIgnoreCase(property)) {
                CSSPropertyBackgroundSWTHandler.INSTANCE.applyCSSPropertyBackgroundImage(element, value, pseudo, engine);
            } else if (NatTableCSSConstants.CELL_BACKGROUND_COLOR.equalsIgnoreCase(property)) {
                if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                    Color newColor = (Color) engine.convert(value, Color.class, natTable.getDisplay());
                    NatTableCSSHelper.applyNatTableStyle(
                            natTable,
                            CellStyleAttributes.BACKGROUND_COLOR,
                            newColor,
                            displayMode,
                            label);

                    if (resolvePainter) {
                        NatTableCSSHelper.storeContextValue(
                                context,
                                displayMode,
                                NatTableCSSConstants.CV_BACKGROUND_PAINTER,
                                CellPainterFactory.BACKGROUND_PAINTER_KEY);
                    }
                } else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
                    Gradient grad = (Gradient) CSSValueSWTGradientConverterImpl.INSTANCE.convert(value, engine, natTable.getDisplay());
                    if (grad != null) {
                        List<?> values = grad.getValues();
                        if (values.size() == 2) {
                            Color background = (Color) engine.convert(
                                    (CSSPrimitiveValue) values.get(0), Color.class, natTable.getDisplay());
                            NatTableCSSHelper.applyNatTableStyle(
                                    natTable,
                                    CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                                    background,
                                    displayMode,
                                    label);
                            Color foreground = (Color) engine.convert(
                                    (CSSPrimitiveValue) values.get(1), Color.class, natTable.getDisplay());
                            NatTableCSSHelper.applyNatTableStyle(
                                    natTable,
                                    CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                                    foreground,
                                    displayMode,
                                    label);

                            NatTableCSSHelper
                                    .getPainterProperties(context, displayMode)
                                    .put(NatTableCSSConstants.GRADIENT_BACKGROUND_VERTICAL, grad.getVerticalGradient());

                            if (resolvePainter) {
                                NatTableCSSHelper.storeContextValue(
                                        context,
                                        displayMode,
                                        NatTableCSSConstants.CV_BACKGROUND_PAINTER,
                                        CellPainterFactory.GRADIENT_BACKGROUND_PAINTER_KEY);
                            }
                        }
                    }
                }
            } else if (NatTableCSSConstants.CELL_BACKGROUND_IMAGE.equalsIgnoreCase(property)) {
                Image image = (Image) engine.convert(value, Image.class, natTable.getDisplay());
                if (image != null) {
                    NatTableCSSHelper
                            .getPainterProperties(context, displayMode)
                            .put(NatTableCSSConstants.CELL_BACKGROUND_IMAGE, image);

                    if (resolvePainter) {
                        NatTableCSSHelper.storeContextValue(
                                context,
                                displayMode,
                                NatTableCSSConstants.CV_BACKGROUND_PAINTER,
                                CellPainterFactory.BACKGROUND_IMAGE_PAINTER_KEY);
                    }
                }
            } else if (NatTableCSSConstants.FOREGROUND_COLOR.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                Color newColor = (Color) engine.convert(value, Color.class, natTable.getDisplay());
                NatTableCSSHelper.applyNatTableStyle(
                        natTable,
                        CellStyleAttributes.FOREGROUND_COLOR,
                        newColor,
                        displayMode,
                        label);
            } else if (NatTableCSSConstants.HORIZONTAL_ALIGNMENT.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                String stringValue = value.getCssText().toLowerCase();
                HorizontalAlignmentEnum align = HorizontalAlignmentEnum.CENTER;
                if ("left".equals(stringValue)
                        || "lead".equals(stringValue)) {
                    align = HorizontalAlignmentEnum.LEFT;
                } else if ("right".equals(stringValue)
                        || "trail".equals(stringValue)) {
                    align = HorizontalAlignmentEnum.RIGHT;
                } else if ("center".equals(stringValue)) {
                    align = HorizontalAlignmentEnum.CENTER;
                }
                NatTableCSSHelper.applyNatTableStyle(
                        natTable,
                        CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                        align,
                        displayMode,
                        label);
            } else if (NatTableCSSConstants.VERTICAL_ALIGNMENT.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                String stringValue = value.getCssText().toLowerCase();
                VerticalAlignmentEnum align = VerticalAlignmentEnum.MIDDLE;
                if ("top".equals(stringValue)
                        || "up".equals(stringValue)) {
                    align = VerticalAlignmentEnum.TOP;
                } else if ("bottom".equals(stringValue)
                        || "down".equals(stringValue)) {
                    align = VerticalAlignmentEnum.BOTTOM;
                } else if ("middle".equals(stringValue)) {
                    align = VerticalAlignmentEnum.MIDDLE;
                }
                NatTableCSSHelper.applyNatTableStyle(
                        natTable,
                        CellStyleAttributes.VERTICAL_ALIGNMENT,
                        align,
                        displayMode,
                        label);
            } else if (NatTableCSSConstants.FONT.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_VALUE_LIST)) {
                NatTableCSSHelper.setFontProperties(
                        (CSSValueList) value,
                        NatTableCSSHelper.getFontProperties(context, CSS2FontPropertiesHelpers.CSS2FONT_KEY, natTable, displayMode, label));
            } else if (NatTableCSSConstants.FONT_FAMILY.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                CSS2FontProperties font =
                        NatTableCSSHelper.getFontProperties(context, CSS2FontPropertiesHelpers.CSS2FONT_KEY, natTable, displayMode, label);
                font.setFamily((CSSPrimitiveValue) value);
            } else if (NatTableCSSConstants.FONT_SIZE.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                CSS2FontProperties font =
                        NatTableCSSHelper.getFontProperties(context, CSS2FontPropertiesHelpers.CSS2FONT_KEY, natTable, displayMode, label);
                font.setSize((CSSPrimitiveValue) value);
            } else if (NatTableCSSConstants.FONT_STYLE.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                CSS2FontProperties font =
                        NatTableCSSHelper.getFontProperties(context, CSS2FontPropertiesHelpers.CSS2FONT_KEY, natTable, displayMode, label);
                font.setStyle((CSSPrimitiveValue) value);
            } else if (NatTableCSSConstants.FONT_WEIGHT.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                CSS2FontProperties font =
                        NatTableCSSHelper.getFontProperties(context, CSS2FontPropertiesHelpers.CSS2FONT_KEY, natTable, displayMode, label);
                font.setWeight((CSSPrimitiveValue) value);
            } else if (NatTableCSSConstants.IMAGE.equalsIgnoreCase(property)) {
                Image image = (Image) engine.convert(value, Image.class, natTable.getDisplay());
                NatTableCSSHelper.applyNatTableStyle(
                        natTable,
                        CellStyleAttributes.IMAGE,
                        image,
                        displayMode,
                        label);

                if (resolvePainter) {
                    NatTableCSSHelper.storeContextValue(
                            context,
                            displayMode,
                            NatTableCSSConstants.CV_CONTENT_PAINTER,
                            CellPainterFactory.IMAGE_PAINTER_KEY);
                }
            } else if (NatTableCSSConstants.BORDER.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_VALUE_LIST)) {
                CSSValueList valueList = (CSSValueList) value;
                BorderStyle borderStyle = NatTableCSSHelper.getBorderStyle(context, displayMode);
                NatTableCSSHelper.storeBorderStyle(valueList, borderStyle, engine, natTable.getDisplay());
            } else if (NatTableCSSConstants.BORDER_COLOR.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                BorderStyle borderStyle = NatTableCSSHelper.getBorderStyle(context, displayMode);
                borderStyle.setColor((Color) engine.convert(value, Color.class, natTable.getDisplay()));
            } else if (NatTableCSSConstants.BORDER_STYLE.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                BorderStyle borderStyle = NatTableCSSHelper.getBorderStyle(context, displayMode);
                CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value;
                borderStyle.setLineStyle(
                        LineStyleEnum.valueOf(primitiveValue.getStringValue().toUpperCase()));
            } else if (NatTableCSSConstants.BORDER_WIDTH.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                BorderStyle borderStyle = NatTableCSSHelper.getBorderStyle(context, displayMode);
                borderStyle.setThickness(
                        (int) ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_PT));
            } else if (NatTableCSSConstants.BORDER_MODE.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                BorderStyle borderStyle = NatTableCSSHelper.getBorderStyle(context, displayMode);
                CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value;
                borderStyle.setBorderMode(
                        BorderModeEnum.valueOf(primitiveValue.getStringValue().toUpperCase()));
            } else if (NatTableCSSConstants.PASSWORD_ECHO_CHAR.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                String stringValue = value.getCssText();
                NatTableCSSHelper.applyNatTableStyle(
                        natTable,
                        CellStyleAttributes.PASSWORD_ECHO_CHAR,
                        stringValue.charAt(0),
                        displayMode,
                        label);
            } else if (NatTableCSSConstants.TEXT_DECORATION.equalsIgnoreCase(property)) {
                if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                    String stringValue = value.getCssText();
                    if ("none".equals(stringValue)) {
                        NatTableCSSHelper.applyNatTableStyle(
                                natTable,
                                CellStyleAttributes.TEXT_DECORATION,
                                null,
                                displayMode,
                                label);
                    } else if ("underline".equals(stringValue)) {
                        NatTableCSSHelper.applyNatTableStyle(
                                natTable,
                                CellStyleAttributes.TEXT_DECORATION,
                                TextDecorationEnum.UNDERLINE,
                                displayMode,
                                label);
                    } else if ("line-through".equals(stringValue)) {
                        NatTableCSSHelper.applyNatTableStyle(
                                natTable,
                                CellStyleAttributes.TEXT_DECORATION,
                                TextDecorationEnum.STRIKETHROUGH,
                                displayMode,
                                label);
                    }
                } else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
                    CSSValueList valueList = (CSSValueList) value;
                    int length = valueList.getLength();
                    boolean strikethrough = false;
                    boolean underline = false;
                    for (int i = 0; i < length; i++) {
                        CSSValue value2 = valueList.item(i);
                        if (value2.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                            String stringValue = value2.getCssText();
                            if ("none".equals(stringValue)) {
                                strikethrough = false;
                                underline = false;
                                break;
                            } else if ("underline".equals(stringValue)) {
                                underline = true;
                            } else if ("line-through".equals(stringValue)) {
                                strikethrough = true;
                            }
                        }
                    }

                    if (!strikethrough && !underline) {
                        NatTableCSSHelper.applyNatTableStyle(
                                natTable,
                                CellStyleAttributes.TEXT_DECORATION,
                                null,
                                displayMode,
                                label);
                    } else if (!strikethrough && underline) {
                        NatTableCSSHelper.applyNatTableStyle(
                                natTable,
                                CellStyleAttributes.TEXT_DECORATION,
                                TextDecorationEnum.UNDERLINE,
                                displayMode,
                                label);
                    } else if (strikethrough && !underline) {
                        NatTableCSSHelper.applyNatTableStyle(
                                natTable,
                                CellStyleAttributes.TEXT_DECORATION,
                                TextDecorationEnum.STRIKETHROUGH,
                                displayMode,
                                label);
                    } else if (strikethrough && underline) {
                        NatTableCSSHelper.applyNatTableStyle(
                                natTable,
                                CellStyleAttributes.TEXT_DECORATION,
                                TextDecorationEnum.UNDERLINE_STRIKETHROUGH,
                                displayMode,
                                label);
                    }
                }
            } else if (NatTableCSSConstants.FREEZE_SEPARATOR_COLOR.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                natTable.getConfigRegistry().registerConfigAttribute(
                        IFreezeConfigAttributes.SEPARATOR_COLOR,
                        (Color) engine.convert(value, Color.class, natTable.getDisplay()),
                        displayMode,
                        label);
            } else if (NatTableCSSConstants.FREEZE_SEPARATOR_WIDTH.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                natTable.getConfigRegistry().registerConfigAttribute(
                        IFreezeConfigAttributes.SEPARATOR_WIDTH,
                        (int) ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_PT),
                        displayMode,
                        label);
            } else if (NatTableCSSConstants.GRID_LINE_COLOR.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                natTable.getConfigRegistry().registerConfigAttribute(
                        CellConfigAttributes.GRID_LINE_COLOR,
                        (Color) engine.convert(value, Color.class, natTable.getDisplay()),
                        displayMode,
                        label);
            } else if (NatTableCSSConstants.GRID_LINE_WIDTH.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                natTable.getConfigRegistry().registerConfigAttribute(
                        CellConfigAttributes.GRID_LINE_WIDTH,
                        (int) ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_PT),
                        displayMode,
                        label);
            } else if (NatTableCSSConstants.RENDER_GRID_LINES.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                natTable.getConfigRegistry().registerConfigAttribute(
                        CellConfigAttributes.RENDER_GRID_LINES,
                        NatTableCSSHelper.getBoolean(value, true),
                        displayMode,
                        label);
            } else if (NatTableCSSConstants.CONVERSION_ERROR_FONT.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_VALUE_LIST)) {
                NatTableCSSHelper.setFontProperties(
                        (CSSValueList) value,
                        NatTableCSSHelper.getFontProperties(
                                context,
                                NatTableCSSConstants.CV_CONVERSION_ERROR_FONT_PROPERTIES,
                                natTable,
                                displayMode,
                                label));
            } else if (NatTableCSSConstants.CONVERSION_ERROR_FONT_FAMILY.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                CSS2FontProperties font = NatTableCSSHelper.getFontProperties(
                        context,
                        NatTableCSSConstants.CV_CONVERSION_ERROR_FONT_PROPERTIES,
                        natTable,
                        displayMode,
                        label);
                font.setFamily((CSSPrimitiveValue) value);
            } else if (NatTableCSSConstants.CONVERSION_ERROR_FONT_SIZE.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                CSS2FontProperties font = NatTableCSSHelper.getFontProperties(
                        context,
                        NatTableCSSConstants.CV_CONVERSION_ERROR_FONT_PROPERTIES,
                        natTable,
                        displayMode,
                        label);
                font.setSize((CSSPrimitiveValue) value);
            } else if (NatTableCSSConstants.CONVERSION_ERROR_FONT_STYLE.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                CSS2FontProperties font = NatTableCSSHelper.getFontProperties(
                        context,
                        NatTableCSSConstants.CV_CONVERSION_ERROR_FONT_PROPERTIES,
                        natTable,
                        displayMode,
                        label);
                font.setStyle((CSSPrimitiveValue) value);
            } else if (NatTableCSSConstants.CONVERSION_ERROR_FONT_WEIGHT.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                CSS2FontProperties font = NatTableCSSHelper.getFontProperties(
                        context,
                        NatTableCSSConstants.CV_CONVERSION_ERROR_FONT_PROPERTIES,
                        natTable,
                        displayMode,
                        label);
                font.setWeight((CSSPrimitiveValue) value);
            } else if (NatTableCSSConstants.CONVERSION_ERROR_BACKGROUND_COLOR.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                Color newColor = (Color) engine.convert(value, Color.class, natTable.getDisplay());
                NatTableCSSHelper.applyNatTableStyle(
                        natTable,
                        EditConfigAttributes.CONVERSION_ERROR_STYLE,
                        CellStyleAttributes.BACKGROUND_COLOR,
                        newColor,
                        displayMode,
                        label);
            } else if (NatTableCSSConstants.CONVERSION_ERROR_FOREGROUND_COLOR.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                Color newColor = (Color) engine.convert(value, Color.class, natTable.getDisplay());
                NatTableCSSHelper.applyNatTableStyle(
                        natTable,
                        EditConfigAttributes.CONVERSION_ERROR_STYLE,
                        CellStyleAttributes.FOREGROUND_COLOR,
                        newColor,
                        displayMode,
                        label);
            } else if (NatTableCSSConstants.VALIDATION_ERROR_FONT.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_VALUE_LIST)) {
                NatTableCSSHelper.setFontProperties(
                        (CSSValueList) value,
                        NatTableCSSHelper.getFontProperties(
                                context,
                                NatTableCSSConstants.CV_VALIDATION_ERROR_FONT_PROPERTIES,
                                natTable,
                                displayMode,
                                label));
            } else if (NatTableCSSConstants.VALIDATION_ERROR_FONT_FAMILY.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                CSS2FontProperties font = NatTableCSSHelper.getFontProperties(
                        context,
                        NatTableCSSConstants.CV_VALIDATION_ERROR_FONT_PROPERTIES,
                        natTable,
                        displayMode,
                        label);
                font.setFamily((CSSPrimitiveValue) value);
            } else if (NatTableCSSConstants.VALIDATION_ERROR_FONT_SIZE.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                CSS2FontProperties font = NatTableCSSHelper.getFontProperties(
                        context,
                        NatTableCSSConstants.CV_VALIDATION_ERROR_FONT_PROPERTIES,
                        natTable,
                        displayMode,
                        label);
                font.setSize((CSSPrimitiveValue) value);
            } else if (NatTableCSSConstants.VALIDATION_ERROR_FONT_STYLE.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                CSS2FontProperties font = NatTableCSSHelper.getFontProperties(
                        context,
                        NatTableCSSConstants.CV_VALIDATION_ERROR_FONT_PROPERTIES,
                        natTable,
                        displayMode,
                        label);
                font.setStyle((CSSPrimitiveValue) value);
            } else if (NatTableCSSConstants.VALIDATION_ERROR_FONT_WEIGHT.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                CSS2FontProperties font = NatTableCSSHelper.getFontProperties(
                        context,
                        NatTableCSSConstants.CV_VALIDATION_ERROR_FONT_PROPERTIES,
                        natTable,
                        displayMode,
                        label);
                font.setWeight((CSSPrimitiveValue) value);
            } else if (NatTableCSSConstants.VALIDATION_ERROR_BACKGROUND_COLOR.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                Color newColor = (Color) engine.convert(value, Color.class, natTable.getDisplay());
                NatTableCSSHelper.applyNatTableStyle(
                        natTable,
                        EditConfigAttributes.VALIDATION_ERROR_STYLE,
                        CellStyleAttributes.BACKGROUND_COLOR,
                        newColor,
                        displayMode,
                        label);
            } else if (NatTableCSSConstants.VALIDATION_ERROR_FOREGROUND_COLOR.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                Color newColor = (Color) engine.convert(value, Color.class, natTable.getDisplay());
                NatTableCSSHelper.applyNatTableStyle(
                        natTable,
                        EditConfigAttributes.VALIDATION_ERROR_STYLE,
                        CellStyleAttributes.FOREGROUND_COLOR,
                        newColor,
                        displayMode,
                        label);
            } else if (NatTableCSSConstants.WORD_WRAP.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                NatTableCSSHelper
                        .getPainterProperties(context, displayMode)
                        .put(NatTableCSSConstants.WORD_WRAP, NatTableCSSHelper.getBoolean(value, false));
            } else if (NatTableCSSConstants.TEXT_WRAP.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                NatTableCSSHelper
                        .getPainterProperties(context, displayMode)
                        .put(NatTableCSSConstants.TEXT_WRAP, NatTableCSSHelper.getBoolean(value, false));
            } else if (NatTableCSSConstants.TEXT_TRIM.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                NatTableCSSHelper
                        .getPainterProperties(context, displayMode)
                        .put(NatTableCSSConstants.TEXT_TRIM, NatTableCSSHelper.getBoolean(value, true));
            } else if (NatTableCSSConstants.TEXT_DIRECTION.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                NatTableCSSHelper
                        .getPainterProperties(context, displayMode)
                        .put(NatTableCSSConstants.TEXT_DIRECTION, value.getCssText());
            } else if (NatTableCSSConstants.LINE_SPACING.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                NatTableCSSHelper
                        .getPainterProperties(context, displayMode)
                        .put(NatTableCSSConstants.LINE_SPACING, (int) ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_PT));
            } else if (NatTableCSSConstants.COLUMN_WIDTH.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value;
                short type = primitiveValue.getPrimitiveType();
                switch (type) {
                    case CSSPrimitiveValue.CSS_IDENT:
                        if ("auto".equalsIgnoreCase(value.getCssText())) {
                            NatTableCSSHelper
                                    .getPainterProperties(context, displayMode)
                                    .put(NatTableCSSConstants.CALCULATE_CELL_WIDTH, true);
                        } else if ("percentage".equalsIgnoreCase(value.getCssText())) {
                            // set column percentage sizing
                            natTable.doCommand(new ColumnSizeConfigurationCommand(label, null, true));
                        }
                        break;
                    case CSSPrimitiveValue.CSS_PERCENTAGE:
                        // set column percentage sizing
                        int percentage = (int) ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE);
                        natTable.doCommand(new ColumnSizeConfigurationCommand(label, percentage, true));
                        break;
                    case CSSPrimitiveValue.CSS_PT:
                    case CSSPrimitiveValue.CSS_NUMBER:
                    case CSSPrimitiveValue.CSS_PX:
                        // set column width by label
                        int width = (int) ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_PT);
                        natTable.doCommand(new ColumnSizeConfigurationCommand(label, width, false));
                        break;
                }
            } else if (NatTableCSSConstants.ROW_HEIGHT.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value;
                short type = primitiveValue.getPrimitiveType();
                switch (type) {
                    case CSSPrimitiveValue.CSS_IDENT:
                        if ("auto".equalsIgnoreCase(value.getCssText())) {
                            NatTableCSSHelper
                                    .getPainterProperties(context, displayMode)
                                    .put(NatTableCSSConstants.CALCULATE_CELL_HEIGHT, true);
                        } else if ("percentage".equalsIgnoreCase(value.getCssText())) {
                            // set column percentage sizing
                            natTable.doCommand(new RowSizeConfigurationCommand(label, null, true));
                        }
                        break;
                    case CSSPrimitiveValue.CSS_PERCENTAGE:
                        // set column percentage sizing
                        int percentage = (int) ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE);
                        natTable.doCommand(new RowSizeConfigurationCommand(label, percentage, true));
                        break;
                    case CSSPrimitiveValue.CSS_PT:
                    case CSSPrimitiveValue.CSS_NUMBER:
                    case CSSPrimitiveValue.CSS_PX:
                        // set column width by label
                        int width = (int) ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_PT);
                        natTable.doCommand(new RowSizeConfigurationCommand(label, width, false));
                        break;
                }
            } else if (NatTableCSSConstants.CONVERTER.equalsIgnoreCase(property)) {
                if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                    if (NatTableCSSHelper.isConverterString(value.getCssText())) {
                        natTable.getConfigRegistry().registerConfigAttribute(
                                CellConfigAttributes.DISPLAY_CONVERTER,
                                NatTableCSSHelper.getDisplayConverter(
                                        value.getCssText(),
                                        true,
                                        null,
                                        null,
                                        null),
                                displayMode,
                                label);
                    }
                } else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
                    String converterKey = null;
                    boolean format = true;
                    Integer minFractionDigits = null;
                    Integer maxFractionDigits = null;
                    String pattern = null;

                    CSSValueList valueList = (CSSValueList) value;
                    int length = valueList.getLength();
                    for (int i = 0; i < length; i++) {
                        CSSValue value2 = valueList.item(i);
                        if (value2.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                            CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value2;
                            short type = primitiveValue.getPrimitiveType();
                            switch (type) {
                                case CSSPrimitiveValue.CSS_IDENT:
                                    if (NatTableCSSHelper.isConverterString(primitiveValue.getCssText())) {
                                        converterKey = primitiveValue.getCssText();
                                    } else if ("true".equals(primitiveValue.getCssText())
                                            || "false".equals(primitiveValue.getCssText())) {
                                        format = NatTableCSSHelper.getBoolean(primitiveValue, true);
                                    }
                                    break;
                                case CSSPrimitiveValue.CSS_STRING:
                                    pattern = primitiveValue.getCssText();
                                    break;
                                case CSSPrimitiveValue.CSS_PT:
                                case CSSPrimitiveValue.CSS_NUMBER:
                                case CSSPrimitiveValue.CSS_PX:
                                    if (minFractionDigits == null) {
                                        minFractionDigits = (int) primitiveValue.getFloatValue(CSSPrimitiveValue.CSS_PT);
                                    } else {
                                        maxFractionDigits = (int) primitiveValue.getFloatValue(CSSPrimitiveValue.CSS_PT);
                                    }
                                    break;
                            }
                        }
                    }

                    if (converterKey != null) {
                        natTable.getConfigRegistry().registerConfigAttribute(
                                CellConfigAttributes.DISPLAY_CONVERTER,
                                NatTableCSSHelper.getDisplayConverter(
                                        converterKey,
                                        format,
                                        minFractionDigits,
                                        maxFractionDigits,
                                        pattern),
                                displayMode,
                                label);
                    }
                }
            } else if (NatTableCSSConstants.PADDING.equalsIgnoreCase(property)) {
                if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                    NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_TOP, value, context, displayMode);
                    NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_RIGHT, value, context, displayMode);
                    NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_BOTTOM, value, context, displayMode);
                    NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_LEFT, value, context, displayMode);
                } else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
                    CSSValueList valueList = (CSSValueList) value;
                    int length = valueList.getLength();
                    if (length == 4) {
                        NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_TOP, valueList.item(0), context, displayMode);
                        NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_RIGHT, valueList.item(1), context, displayMode);
                        NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_BOTTOM, valueList.item(2), context, displayMode);
                        NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_LEFT, valueList.item(3), context, displayMode);
                    } else if (length == 3) {
                        NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_TOP, valueList.item(0), context, displayMode);
                        NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_RIGHT, valueList.item(1), context, displayMode);
                        NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_BOTTOM, valueList.item(2), context, displayMode);
                        NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_LEFT, valueList.item(1), context, displayMode);
                    } else if (length == 2) {
                        NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_TOP, valueList.item(0), context, displayMode);
                        NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_RIGHT, valueList.item(1), context, displayMode);
                        NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_BOTTOM, valueList.item(0), context, displayMode);
                        NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_LEFT, valueList.item(1), context, displayMode);
                    }
                }
            } else if (NatTableCSSConstants.PADDING_TOP.equalsIgnoreCase(property)) {
                NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_TOP, value, context, displayMode);
            } else if (NatTableCSSConstants.PADDING_RIGHT.equalsIgnoreCase(property)) {
                NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_RIGHT, value, context, displayMode);
            } else if (NatTableCSSConstants.PADDING_BOTTOM.equalsIgnoreCase(property)) {
                NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_BOTTOM, value, context, displayMode);
            } else if (NatTableCSSConstants.PADDING_LEFT.equalsIgnoreCase(property)) {
                NatTableCSSHelper.storePadding(NatTableCSSConstants.PADDING_LEFT, value, context, displayMode);
            } else if (NatTableCSSConstants.TABLE_BORDER_COLOR.equals(property)
                    && value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {

                // remove any prior configured NatTableBorderOverlayPainter
                for (Iterator<IOverlayPainter> it = natTable.getOverlayPainters().iterator(); it.hasNext();) {
                    IOverlayPainter painter = it.next();
                    if (painter instanceof NatTableBorderOverlayPainter) {
                        it.remove();
                    }
                }

                // add the new NatTableBorderOverlayPainter
                if ("auto".equalsIgnoreCase(value.getCssText())) {
                    natTable.addOverlayPainter(new NatTableBorderOverlayPainter(true, natTable.getConfigRegistry()));
                } else {
                    Color newColor = (Color) engine.convert(value, Color.class, natTable.getDisplay());
                    natTable.addOverlayPainter(new NatTableBorderOverlayPainter(newColor, true));
                }
            } else if (NatTableCSSConstants.FILL_HANDLE_BORDER.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_VALUE_LIST)) {
                CSSValueList valueList = (CSSValueList) value;
                BorderStyle borderStyle = new BorderStyle();
                NatTableCSSHelper.storeBorderStyle(valueList, borderStyle, engine, natTable.getDisplay());
                natTable.getConfigRegistry().registerConfigAttribute(
                        FillHandleConfigAttributes.FILL_HANDLE_BORDER_STYLE,
                        borderStyle);
            } else if (NatTableCSSConstants.FILL_HANDLE_COLOR.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                natTable.getConfigRegistry().registerConfigAttribute(
                        FillHandleConfigAttributes.FILL_HANDLE_COLOR,
                        (Color) engine.convert(value, Color.class, natTable.getDisplay()));
            } else if (NatTableCSSConstants.FILL_REGION_BORDER.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_VALUE_LIST)) {
                CSSValueList valueList = (CSSValueList) value;
                BorderStyle borderStyle = new BorderStyle();
                NatTableCSSHelper.storeBorderStyle(valueList, borderStyle, engine, natTable.getDisplay());
                natTable.getConfigRegistry().registerConfigAttribute(
                        FillHandleConfigAttributes.FILL_HANDLE_REGION_BORDER_STYLE,
                        borderStyle);
            } else if (NatTableCSSConstants.DECORATION.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_VALUE_LIST)) {
                CSSValueList valueList = (CSSValueList) value;
                int length = valueList.getLength();
                for (int i = 0; i < length; i++) {
                    CSSValue value2 = valueList.item(i);
                    if (value2.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                        CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value2;
                        short type = primitiveValue.getPrimitiveType();
                        switch (type) {
                            case CSSPrimitiveValue.CSS_IDENT:
                                if ("true".equalsIgnoreCase(value2.getCssText())
                                        || "false".equalsIgnoreCase(value2.getCssText())) {
                                    NatTableCSSHelper
                                            .getPainterProperties(context, displayMode)
                                            .put(NatTableCSSConstants.PAINT_DECORATION_DEPENDENT,
                                                    NatTableCSSHelper.getBoolean(value2, true));
                                } else {
                                    CellEdgeEnum edge = NatTableCSSHelper.getCellEdgeEnum(value2.getCssText());
                                    NatTableCSSHelper
                                            .getPainterProperties(context, displayMode)
                                            .put(NatTableCSSConstants.DECORATOR_EDGE, edge);
                                }

                                break;
                            case CSSPrimitiveValue.CSS_URI:
                                // first try to resolve the image using NatTable
                                // GUIHelper to support scaling
                                Image image = null;
                                try {
                                    IResourcesLocatorManager manager = engine.getResourcesLocatorManager();
                                    String resolved = manager.resolve(primitiveValue.getStringValue());
                                    image = GUIHelper.getImageByURL(new URI(resolved).toURL());
                                } catch (Exception e) {
                                    // something went wrong with loading the
                                    // image by URL try to use the CSSEngine
                                    image = (Image) engine.convert(value2, Image.class, natTable.getDisplay());
                                }

                                if (image != null) {
                                    NatTableCSSHelper
                                            .getPainterProperties(context, displayMode)
                                            .put(NatTableCSSConstants.DECORATOR_IMAGE, image);
                                }
                                break;
                            case CSSPrimitiveValue.CSS_PT:
                            case CSSPrimitiveValue.CSS_NUMBER:
                            case CSSPrimitiveValue.CSS_PX:
                                int spacing = (int) ((CSSPrimitiveValue) value2).getFloatValue(CSSPrimitiveValue.CSS_PT);
                                NatTableCSSHelper
                                        .getPainterProperties(context, displayMode)
                                        .put(NatTableCSSConstants.DECORATOR_SPACING, spacing);
                                break;
                        }
                    }
                }

                List<String> decoratorKeys = NatTableCSSHelper.getDecoratorPainter(context, displayMode);
                if (!decoratorKeys.contains(CellPainterFactory.DECORATOR_KEY)) {
                    decoratorKeys.add(CellPainterFactory.DECORATOR_KEY);
                }
            } else if (NatTableCSSConstants.INVERT_ICONS.equalsIgnoreCase(property)
                    && (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
                NatTableCSSHelper
                        .getPainterProperties(context, displayMode)
                        .put(NatTableCSSConstants.INVERT_ICONS, NatTableCSSHelper.getBoolean(value, false));
            } else if (NatTableCSSConstants.PERCENTAGE_DECORATOR_COLORS.equalsIgnoreCase(property)) {
                if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                    Color newColor = (Color) engine.convert(value, Color.class, natTable.getDisplay());
                    NatTableCSSHelper.applyNatTableStyle(
                            natTable,
                            PercentageBarDecorator.PERCENTAGE_BAR_COMPLETE_REGION_START_COLOR,
                            newColor,
                            displayMode,
                            label);
                } else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
                    CSSValueList valueList = (CSSValueList) value;
                    int length = valueList.getLength();
                    for (int i = 0; i < length; i++) {
                        CSSValue value2 = valueList.item(i);
                        if (value2.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                            Color newColor = (Color) engine.convert(value2, Color.class, natTable.getDisplay());
                            ConfigAttribute<Color> percentageConfig = null;
                            switch (i) {
                                case 0:
                                    percentageConfig = PercentageBarDecorator.PERCENTAGE_BAR_COMPLETE_REGION_START_COLOR;
                                    break;
                                case 1:
                                    percentageConfig = PercentageBarDecorator.PERCENTAGE_BAR_COMPLETE_REGION_END_COLOR;
                                    break;
                                case 2:
                                    percentageConfig = PercentageBarDecorator.PERCENTAGE_BAR_INCOMPLETE_REGION_COLOR;
                                    break;
                            }

                            if (percentageConfig != null) {
                                NatTableCSSHelper.applyNatTableStyle(
                                        natTable,
                                        percentageConfig,
                                        newColor,
                                        displayMode,
                                        label);
                            }
                        }
                    }
                }
            } else if (NatTableCSSConstants.TREE_STRUCTURE_PAINTER.equalsIgnoreCase(property)) {
                List<String> painterValues = NatTableCSSHelper.resolvePainterRepresentation(value);
                Map<String, Object> painterProperties =
                        NatTableCSSHelper.getPainterPropertiesInherited(context, natTableContext, displayMode);

                // ensure that the last value in the element is not a content
                // painter
                if (CellPainterFactory.getInstance().isContentPainterKey(painterValues.get(painterValues.size() - 1))) {
                    painterValues.remove(painterValues.size() - 1);
                }

                // add none to the end to ensure that the content painter is
                // resolved dynamically by the TreeLayer
                painterValues.add(CellPainterFactory.NONE);

                ICellPainter painter = CellPainterFactory.getInstance().getCellPainter(painterValues, painterProperties);

                if (painter != null) {
                    natTable.getConfigRegistry().registerConfigAttribute(
                            TreeConfigAttributes.TREE_STRUCTURE_PAINTER,
                            painter,
                            displayMode);
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public String retrieveCSSProperty(Object control, String property, String pseudo, CSSEngine engine)
            throws Exception {

        NatTable natTable = null;
        String label = null;
        String displayMode = NatTableCSSHelper.getDisplayMode(pseudo);

        if (control instanceof NatTable) {
            natTable = (NatTable) control;
        } else if (control instanceof NatTableWrapper) {
            natTable = ((NatTableWrapper) control).getNatTable();
            label = ((NatTableWrapper) control).getLabel();
        }

        if (natTable != null) {
            // check property
            if (NatTableCSSConstants.BACKGROUND_COLOR.equalsIgnoreCase(property)) {
                return CSSPropertyBackgroundSWTHandler.INSTANCE.retrieveCSSPropertyBackgroundColor(control, pseudo, engine);
            } else if (NatTableCSSConstants.BACKGROUND_IMAGE.equalsIgnoreCase(property)) {
                return CSSPropertyBackgroundSWTHandler.INSTANCE.retrieveCSSPropertyBackgroundImage(control, pseudo, engine);
            } else if (NatTableCSSConstants.CELL_BACKGROUND_COLOR.equalsIgnoreCase(property)) {
                ICSSValueConverter cssValueConverter = engine.getCSSValueConverter(String.class);
                return cssValueConverter.convert(
                        NatTableCSSHelper.getNatTableStyle(
                                natTable,
                                CellStyleAttributes.BACKGROUND_COLOR,
                                displayMode,
                                label),
                        engine,
                        null);
            } else if (NatTableCSSConstants.CELL_BACKGROUND_IMAGE.equalsIgnoreCase(property)) {
                // TODO : manage path of Image.
                return "none";
            } else if (NatTableCSSConstants.FOREGROUND_COLOR.equalsIgnoreCase(property)) {
                ICSSValueConverter cssValueConverter = engine.getCSSValueConverter(String.class);
                return cssValueConverter.convert(
                        NatTableCSSHelper.getNatTableStyle(
                                natTable,
                                CellStyleAttributes.FOREGROUND_COLOR,
                                displayMode,
                                label),
                        engine,
                        null);
            } else if (NatTableCSSConstants.HORIZONTAL_ALIGNMENT.equalsIgnoreCase(property)) {
                HorizontalAlignmentEnum align = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                        displayMode,
                        label);
                switch (align) {
                    case CENTER:
                        return "center";
                    case LEFT:
                        return "left";
                    case RIGHT:
                        return "right";
                }
            } else if (NatTableCSSConstants.VERTICAL_ALIGNMENT.equalsIgnoreCase(property)) {
                VerticalAlignmentEnum align = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        CellStyleAttributes.VERTICAL_ALIGNMENT,
                        displayMode,
                        label);
                switch (align) {
                    case TOP:
                        return "top";
                    case MIDDLE:
                        return "middle";
                    case BOTTOM:
                        return "bottom";
                }
            } else if (NatTableCSSConstants.FONT.equalsIgnoreCase(property)) {
                Font font = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        CellStyleAttributes.FONT,
                        displayMode,
                        label);
                return CSSSWTFontHelper.getFontComposite(font);
            } else if (NatTableCSSConstants.FONT_FAMILY.equalsIgnoreCase(property)) {
                Font font = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        CellStyleAttributes.FONT,
                        displayMode,
                        label);
                return CSSSWTFontHelper.getFontFamily(font);
            } else if (NatTableCSSConstants.FONT_SIZE.equalsIgnoreCase(property)) {
                Font font = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        CellStyleAttributes.FONT,
                        displayMode,
                        label);
                return CSSSWTFontHelper.getFontSize(font);
            } else if (NatTableCSSConstants.FONT_STYLE.equalsIgnoreCase(property)) {
                Font font = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        CellStyleAttributes.FONT,
                        displayMode,
                        label);
                return CSSSWTFontHelper.getFontStyle(font);
            } else if (NatTableCSSConstants.FONT_WEIGHT.equalsIgnoreCase(property)) {
                Font font = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        CellStyleAttributes.FONT,
                        displayMode,
                        label);
                return CSSSWTFontHelper.getFontWeight(font);
            } else if (NatTableCSSConstants.IMAGE.equalsIgnoreCase(property)) {
                // TODO : manage path of Image.
                return "none";
            } else if (NatTableCSSConstants.BORDER.equalsIgnoreCase(property)) {
                BorderStyle border = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        CellStyleAttributes.BORDER_STYLE,
                        displayMode,
                        label);

                ICSSValueConverter cssValueConverter = engine.getCSSValueConverter(String.class);
                return border.getThickness() + " "
                        + border.getLineStyle().toString().toLowerCase()
                        + " " + border.getBorderMode().toString().toLowerCase()
                        + " " + cssValueConverter.convert(
                                border.getColor(),
                                engine,
                                null);
            } else if (NatTableCSSConstants.BORDER_COLOR.equalsIgnoreCase(property)) {
                BorderStyle border = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        CellStyleAttributes.BORDER_STYLE,
                        displayMode,
                        label);
                ICSSValueConverter cssValueConverter = engine.getCSSValueConverter(String.class);
                return cssValueConverter.convert(
                        border.getColor(),
                        engine,
                        null);
            } else if (NatTableCSSConstants.BORDER_STYLE.equalsIgnoreCase(property)) {
                BorderStyle border = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        CellStyleAttributes.BORDER_STYLE,
                        displayMode,
                        label);
                return border.getLineStyle().toString().toLowerCase();
            } else if (NatTableCSSConstants.BORDER_WIDTH.equalsIgnoreCase(property)) {
                BorderStyle border = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        CellStyleAttributes.BORDER_STYLE,
                        displayMode,
                        label);
                return "" + border.getThickness();
            } else if (NatTableCSSConstants.BORDER_MODE.equalsIgnoreCase(property)) {
                BorderStyle border = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        CellStyleAttributes.BORDER_STYLE,
                        displayMode,
                        label);
                return "" + border.getBorderMode();
            } else if (NatTableCSSConstants.PASSWORD_ECHO_CHAR.equalsIgnoreCase(property)) {
                return "" + NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        CellStyleAttributes.PASSWORD_ECHO_CHAR,
                        displayMode,
                        label);
            } else if (NatTableCSSConstants.TEXT_DECORATION.equalsIgnoreCase(property)) {
                TextDecorationEnum decoration = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        CellStyleAttributes.TEXT_DECORATION,
                        displayMode,
                        label);
                switch (decoration) {
                    case NONE:
                        return "none";
                    case STRIKETHROUGH:
                        return "line-through";
                    case UNDERLINE:
                        return "underline";
                    case UNDERLINE_STRIKETHROUGH:
                        return "underline line-through";
                }
            } else if (NatTableCSSConstants.FREEZE_SEPARATOR_COLOR.equalsIgnoreCase(property)) {
                ICSSValueConverter cssValueConverter = engine.getCSSValueConverter(String.class);
                return cssValueConverter.convert(
                        natTable.getConfigRegistry().getConfigAttribute(
                                IFreezeConfigAttributes.SEPARATOR_COLOR,
                                displayMode,
                                label),
                        engine,
                        null);
            } else if (NatTableCSSConstants.FREEZE_SEPARATOR_WIDTH.equalsIgnoreCase(property)) {
                Integer width = natTable.getConfigRegistry().getConfigAttribute(
                        IFreezeConfigAttributes.SEPARATOR_WIDTH,
                        displayMode,
                        label);
                if (width == null) {
                    return "0";
                } else {
                    return width.toString();
                }
            } else if (NatTableCSSConstants.GRID_LINE_COLOR.equalsIgnoreCase(property)) {
                ICSSValueConverter cssValueConverter = engine.getCSSValueConverter(String.class);
                return cssValueConverter.convert(
                        natTable.getConfigRegistry().getConfigAttribute(
                                CellConfigAttributes.GRID_LINE_COLOR,
                                displayMode,
                                label),
                        engine,
                        null);
            } else if (NatTableCSSConstants.GRID_LINE_WIDTH.equalsIgnoreCase(property)) {
                Integer width = natTable.getConfigRegistry().getConfigAttribute(
                        CellConfigAttributes.GRID_LINE_WIDTH,
                        displayMode,
                        label);
                if (width == null) {
                    return "0";
                } else {
                    return width.toString();
                }
            } else if (NatTableCSSConstants.RENDER_GRID_LINES.equalsIgnoreCase(property)) {
                Boolean render = natTable.getConfigRegistry().getConfigAttribute(
                        CellConfigAttributes.RENDER_GRID_LINES,
                        displayMode,
                        label);
                if (render == null) {
                    return Boolean.TRUE.toString();
                } else {
                    return render.toString();
                }
            } else if (NatTableCSSConstants.CONVERSION_ERROR_FONT.equalsIgnoreCase(property)) {
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        EditConfigAttributes.CONVERSION_ERROR_STYLE,
                        displayMode,
                        label);
                Font font = style.getAttributeValue(CellStyleAttributes.FONT);
                return CSSSWTFontHelper.getFontComposite(font);
            } else if (NatTableCSSConstants.CONVERSION_ERROR_FONT_FAMILY.equalsIgnoreCase(property)) {
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        EditConfigAttributes.CONVERSION_ERROR_STYLE,
                        displayMode,
                        label);
                Font font = style.getAttributeValue(CellStyleAttributes.FONT);
                return CSSSWTFontHelper.getFontFamily(font);
            } else if (NatTableCSSConstants.CONVERSION_ERROR_FONT_SIZE.equalsIgnoreCase(property)) {
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        EditConfigAttributes.CONVERSION_ERROR_STYLE,
                        displayMode,
                        label);
                Font font = style.getAttributeValue(CellStyleAttributes.FONT);
                return CSSSWTFontHelper.getFontSize(font);
            } else if (NatTableCSSConstants.CONVERSION_ERROR_FONT_STYLE.equalsIgnoreCase(property)) {
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        EditConfigAttributes.CONVERSION_ERROR_STYLE,
                        displayMode,
                        label);
                Font font = style.getAttributeValue(CellStyleAttributes.FONT);
                return CSSSWTFontHelper.getFontStyle(font);
            } else if (NatTableCSSConstants.CONVERSION_ERROR_FONT_WEIGHT.equalsIgnoreCase(property)) {
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        EditConfigAttributes.CONVERSION_ERROR_STYLE,
                        displayMode,
                        label);
                Font font = style.getAttributeValue(CellStyleAttributes.FONT);
                return CSSSWTFontHelper.getFontWeight(font);
            } else if (NatTableCSSConstants.CONVERSION_ERROR_BACKGROUND_COLOR.equalsIgnoreCase(property)) {
                ICSSValueConverter cssValueConverter = engine.getCSSValueConverter(String.class);
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        EditConfigAttributes.CONVERSION_ERROR_STYLE,
                        displayMode,
                        label);
                return cssValueConverter.convert(
                        style.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR),
                        engine,
                        null);
            } else if (NatTableCSSConstants.CONVERSION_ERROR_FOREGROUND_COLOR.equalsIgnoreCase(property)) {
                ICSSValueConverter cssValueConverter = engine.getCSSValueConverter(String.class);
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        EditConfigAttributes.CONVERSION_ERROR_STYLE,
                        displayMode,
                        label);
                return cssValueConverter.convert(
                        style.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR),
                        engine,
                        null);
            } else if (NatTableCSSConstants.VALIDATION_ERROR_FONT.equalsIgnoreCase(property)) {
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        EditConfigAttributes.VALIDATION_ERROR_STYLE,
                        displayMode,
                        label);
                Font font = style.getAttributeValue(CellStyleAttributes.FONT);
                return CSSSWTFontHelper.getFontComposite(font);
            } else if (NatTableCSSConstants.VALIDATION_ERROR_FONT_FAMILY.equalsIgnoreCase(property)) {
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        EditConfigAttributes.VALIDATION_ERROR_STYLE,
                        displayMode,
                        label);
                Font font = style.getAttributeValue(CellStyleAttributes.FONT);
                return CSSSWTFontHelper.getFontFamily(font);
            } else if (NatTableCSSConstants.VALIDATION_ERROR_FONT_SIZE.equalsIgnoreCase(property)) {
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        EditConfigAttributes.VALIDATION_ERROR_STYLE,
                        displayMode,
                        label);
                Font font = style.getAttributeValue(CellStyleAttributes.FONT);
                return CSSSWTFontHelper.getFontSize(font);
            } else if (NatTableCSSConstants.VALIDATION_ERROR_FONT_STYLE.equalsIgnoreCase(property)) {
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        EditConfigAttributes.VALIDATION_ERROR_STYLE,
                        displayMode,
                        label);
                Font font = style.getAttributeValue(CellStyleAttributes.FONT);
                return CSSSWTFontHelper.getFontStyle(font);
            } else if (NatTableCSSConstants.VALIDATION_ERROR_FONT_WEIGHT.equalsIgnoreCase(property)) {
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        EditConfigAttributes.VALIDATION_ERROR_STYLE,
                        displayMode,
                        label);
                Font font = style.getAttributeValue(CellStyleAttributes.FONT);
                return CSSSWTFontHelper.getFontWeight(font);
            } else if (NatTableCSSConstants.VALIDATION_ERROR_BACKGROUND_COLOR.equalsIgnoreCase(property)) {
                ICSSValueConverter cssValueConverter = engine.getCSSValueConverter(String.class);
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        EditConfigAttributes.VALIDATION_ERROR_STYLE,
                        displayMode,
                        label);
                return cssValueConverter.convert(
                        style.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR),
                        engine,
                        null);
            } else if (NatTableCSSConstants.VALIDATION_ERROR_FOREGROUND_COLOR.equalsIgnoreCase(property)) {
                ICSSValueConverter cssValueConverter = engine.getCSSValueConverter(String.class);
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        EditConfigAttributes.VALIDATION_ERROR_STYLE,
                        displayMode,
                        label);
                return cssValueConverter.convert(
                        style.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR),
                        engine,
                        null);
            } else if (NatTableCSSConstants.WORD_WRAP.equalsIgnoreCase(property)) {
                Boolean wrap = Boolean.FALSE;

                ICellPainter painter = natTable.getConfigRegistry().getConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        displayMode,
                        label);
                if (painter != null) {
                    while (painter instanceof CellPainterWrapper) {
                        painter = ((CellPainterWrapper) painter).getWrappedPainter();
                    }
                }

                if (painter instanceof AbstractTextPainter) {
                    wrap = ((AbstractTextPainter) painter).isWordWrapping();
                }

                return wrap.toString();
            } else if (NatTableCSSConstants.TEXT_WRAP.equalsIgnoreCase(property)) {
                Boolean wrap = Boolean.FALSE;

                ICellPainter painter = natTable.getConfigRegistry().getConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        displayMode,
                        label);
                if (painter != null) {
                    while (painter instanceof CellPainterWrapper) {
                        painter = ((CellPainterWrapper) painter).getWrappedPainter();
                    }
                }

                if (painter instanceof AbstractTextPainter) {
                    wrap = ((AbstractTextPainter) painter).isWrapText();
                }

                return wrap.toString();
            } else if (NatTableCSSConstants.TEXT_TRIM.equalsIgnoreCase(property)) {
                Boolean trim = Boolean.FALSE;

                ICellPainter painter = natTable.getConfigRegistry().getConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        displayMode,
                        label);
                if (painter != null) {
                    while (painter instanceof CellPainterWrapper) {
                        painter = ((CellPainterWrapper) painter).getWrappedPainter();
                    }
                }

                if (painter instanceof AbstractTextPainter) {
                    trim = ((AbstractTextPainter) painter).isTrimText();
                }

                return trim.toString();
            } else if (NatTableCSSConstants.LINE_SPACING.equalsIgnoreCase(property)) {
                int spacing = 0;

                ICellPainter painter = natTable.getConfigRegistry().getConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        displayMode,
                        label);
                if (painter != null) {
                    while (painter instanceof CellPainterWrapper) {
                        painter = ((CellPainterWrapper) painter).getWrappedPainter();
                    }
                }

                if (painter instanceof AbstractTextPainter) {
                    spacing = ((AbstractTextPainter) painter).getLineSpacing();
                }

                return "" + spacing;
            } else if (NatTableCSSConstants.TEXT_DIRECTION.equalsIgnoreCase(property)) {
                String direction = "horizontal";

                ICellPainter painter = natTable.getConfigRegistry().getConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        displayMode,
                        label);
                if (painter != null) {
                    while (painter instanceof CellPainterWrapper) {
                        painter = ((CellPainterWrapper) painter).getWrappedPainter();
                    }
                }

                if (painter instanceof VerticalTextPainter) {
                    direction = "vertical";
                }

                return direction;
            } else if (NatTableCSSConstants.COLUMN_WIDTH.equalsIgnoreCase(property)) {
                // return a value that would not trigger any action for resizing
                // inherit is not supported here
                return "default";
            } else if (NatTableCSSConstants.ROW_HEIGHT.equalsIgnoreCase(property)) {
                // return a value that would not trigger any action for resizing
                // inherit is not supported here
                return "default";
            } else if (NatTableCSSConstants.PADDING.equalsIgnoreCase(property)) {
                ICellPainter painter = natTable.getConfigRegistry().getConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        displayMode,
                        label);
                if (painter != null) {
                    while (painter instanceof CellPainterWrapper) {
                        painter = ((CellPainterWrapper) painter).getWrappedPainter();
                        if (painter instanceof PaddingDecorator) {
                            PaddingDecorator decorator = (PaddingDecorator) painter;
                            return new StringBuilder()
                                    .append(decorator.getTopPadding())
                                    .append(" ")
                                    .append(decorator.getRightPadding())
                                    .append(" ")
                                    .append(decorator.getBottomPadding())
                                    .append(" ")
                                    .append(decorator.getLeftPadding())
                                    .toString();
                        }
                    }
                }

            } else if (NatTableCSSConstants.PADDING_TOP.equalsIgnoreCase(property)) {
                ICellPainter painter = natTable.getConfigRegistry().getConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        displayMode,
                        label);
                if (painter != null) {
                    while (painter instanceof CellPainterWrapper) {
                        painter = ((CellPainterWrapper) painter).getWrappedPainter();
                        if (painter instanceof PaddingDecorator) {
                            return "" + ((PaddingDecorator) painter).getTopPadding();
                        }
                    }
                }
                return "0";
            } else if (NatTableCSSConstants.PADDING_RIGHT.equalsIgnoreCase(property)) {
                ICellPainter painter = natTable.getConfigRegistry().getConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        displayMode,
                        label);
                if (painter != null) {
                    while (painter instanceof CellPainterWrapper) {
                        painter = ((CellPainterWrapper) painter).getWrappedPainter();
                        if (painter instanceof PaddingDecorator) {
                            return "" + ((PaddingDecorator) painter).getRightPadding();
                        }
                    }
                }
                return "0";
            } else if (NatTableCSSConstants.PADDING_BOTTOM.equalsIgnoreCase(property)) {
                ICellPainter painter = natTable.getConfigRegistry().getConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        displayMode,
                        label);
                if (painter != null) {
                    while (painter instanceof CellPainterWrapper) {
                        painter = ((CellPainterWrapper) painter).getWrappedPainter();
                        if (painter instanceof PaddingDecorator) {
                            return "" + ((PaddingDecorator) painter).getBottomPadding();
                        }
                    }
                }
                return "0";
            } else if (NatTableCSSConstants.PADDING_LEFT.equalsIgnoreCase(property)) {
                ICellPainter painter = natTable.getConfigRegistry().getConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        displayMode,
                        label);
                if (painter != null) {
                    while (painter instanceof CellPainterWrapper) {
                        painter = ((CellPainterWrapper) painter).getWrappedPainter();
                        if (painter instanceof PaddingDecorator) {
                            return "" + ((PaddingDecorator) painter).getLeftPadding();
                        }
                    }
                }
                return "0";
            } else if (NatTableCSSConstants.TABLE_BORDER_COLOR.equalsIgnoreCase(property)) {
                for (Iterator<IOverlayPainter> it = natTable.getOverlayPainters().iterator(); it.hasNext();) {
                    IOverlayPainter painter = it.next();
                    if (painter instanceof NatTableBorderOverlayPainter) {
                        return engine.getCSSValueConverter(String.class).convert(
                                ((NatTableBorderOverlayPainter) painter).getBorderColor(),
                                engine,
                                null);
                    }
                }
            } else if (NatTableCSSConstants.FILL_HANDLE_BORDER.equalsIgnoreCase(property)) {
                BorderStyle border = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        FillHandleConfigAttributes.FILL_HANDLE_BORDER_STYLE,
                        displayMode,
                        label);

                ICSSValueConverter cssValueConverter = engine.getCSSValueConverter(String.class);
                return border.getThickness() + " "
                        + border.getLineStyle().toString().toLowerCase()
                        + " " + border.getBorderMode().toString().toLowerCase()
                        + " " + cssValueConverter.convert(
                                border.getColor(),
                                engine,
                                null);
            } else if (NatTableCSSConstants.FILL_HANDLE_COLOR.equalsIgnoreCase(property)) {
                ICSSValueConverter cssValueConverter = engine.getCSSValueConverter(String.class);
                return cssValueConverter.convert(
                        natTable.getConfigRegistry().getConfigAttribute(
                                FillHandleConfigAttributes.FILL_HANDLE_COLOR,
                                displayMode,
                                label),
                        engine,
                        null);
            } else if (NatTableCSSConstants.FILL_REGION_BORDER.equalsIgnoreCase(property)) {
                BorderStyle border = NatTableCSSHelper.getNatTableStyle(
                        natTable,
                        FillHandleConfigAttributes.FILL_HANDLE_REGION_BORDER_STYLE,
                        displayMode,
                        label);

                ICSSValueConverter cssValueConverter = engine.getCSSValueConverter(String.class);
                return border.getThickness() + " "
                        + border.getLineStyle().toString().toLowerCase()
                        + " " + border.getBorderMode().toString().toLowerCase()
                        + " " + cssValueConverter.convert(
                                border.getColor(),
                                engine,
                                null);
            } else if (NatTableCSSConstants.INVERT_ICONS.equalsIgnoreCase(property)) {
                // that information is hidden in specific painters and we are
                // not able to determine it easily here
                return Boolean.FALSE.toString();
            } else if (NatTableCSSConstants.DECORATION.equalsIgnoreCase(property)) {
                // TODO : manage path of Image.
                return "none";
            } else if (NatTableCSSConstants.PERCENTAGE_DECORATOR_COLORS.equalsIgnoreCase(property)) {
                IStyle style = natTable.getConfigRegistry().getConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        displayMode,
                        label);

                Color c1 = style.getAttributeValue(PercentageBarDecorator.PERCENTAGE_BAR_COMPLETE_REGION_START_COLOR);
                Color c2 = style.getAttributeValue(PercentageBarDecorator.PERCENTAGE_BAR_COMPLETE_REGION_END_COLOR);
                Color c3 = style.getAttributeValue(PercentageBarDecorator.PERCENTAGE_BAR_INCOMPLETE_REGION_COLOR);

                ICSSValueConverter cssValueConverter = engine.getCSSValueConverter(String.class);
                return cssValueConverter.convert(c1, engine, null) + " "
                        + cssValueConverter.convert(c2, engine, null) + " "
                        + cssValueConverter.convert(c3, engine, null);
            } else if (NatTableCSSConstants.CONVERTER.equalsIgnoreCase(property)) {
                IDisplayConverter converter = natTable.getConfigRegistry().getConfigAttribute(
                        CellConfigAttributes.DISPLAY_CONVERTER,
                        displayMode,
                        label);
                return NatTableCSSHelper.getDisplayConverterString(converter);
            }
        }
        return null;
    }

    @Override
    public void onAllCSSPropertiesApplyed(Object element, CSSEngine engine, String pseudo) throws Exception {
        Object control = null;

        if (element instanceof CSSStylableElement) {
            CSSStylableElement elt = (CSSStylableElement) element;
            control = elt.getNativeWidget();
        }

        NatTable natTable = null;
        String label = null;

        CSSElementContext context = null;
        CSSElementContext natTableContext = null;

        if (control instanceof NatTable) {
            natTable = (NatTable) control;
        } else if (control instanceof NatTableWrapper) {
            natTable = ((NatTableWrapper) control).getNatTable();
            label = ((NatTableWrapper) control).getLabel();
            natTableContext = engine.getCSSElementContext(natTable);
        }

        if (natTable != null) {
            context = engine.getCSSElementContext(control);
            String displayMode = NatTableCSSHelper.getDisplayMode(pseudo);

            CSS2FontProperties fontProperties =
                    (CSS2FontProperties) NatTableCSSHelper.getContextValue(
                            context,
                            displayMode,
                            CSS2FontPropertiesHelpers.CSS2FONT_KEY);
            if (fontProperties != null) {
                // if there are font properties, register the font
                // check if there is a font registered in the hierarchy
                Font font = NatTableCSSHelper.getNatTableStyle(natTable, CellStyleAttributes.FONT, displayMode, label);

                if (font == null) {
                    // if there are no font properties already and no font, use
                    // the default font
                    font = GUIHelper.DEFAULT_FONT;
                }

                FontData fontData = CSSSWTFontHelper.getFontData(
                        fontProperties,
                        CSSSWTFontHelper.getFirstFontData(font));
                NatTableCSSHelper.applyNatTableStyle(
                        natTable,
                        CellStyleAttributes.FONT,
                        GUIHelper.getFont(fontData),
                        displayMode,
                        label);
            }

            BorderStyle borderStyle =
                    (BorderStyle) NatTableCSSHelper.getContextValue(
                            context,
                            displayMode,
                            NatTableCSSConstants.CV_BORDER_CONFIGURATION);
            if (borderStyle != null) {
                NatTableCSSHelper.applyNatTableStyle(
                        natTable,
                        CellStyleAttributes.BORDER_STYLE,
                        borderStyle,
                        displayMode,
                        label);
            }

            CSS2FontProperties conversionErrorFontProperties =
                    (CSS2FontProperties) NatTableCSSHelper.getContextValue(
                            context,
                            displayMode,
                            NatTableCSSConstants.CV_CONVERSION_ERROR_FONT_PROPERTIES);
            if (conversionErrorFontProperties != null) {
                // if there are font properties, register the font
                FontData fontData = CSSSWTFontHelper.getFontData(
                        conversionErrorFontProperties,
                        CSSSWTFontHelper.getFirstFontData(GUIHelper.DEFAULT_FONT));
                NatTableCSSHelper.applyNatTableStyle(
                        natTable,
                        EditConfigAttributes.CONVERSION_ERROR_STYLE,
                        CellStyleAttributes.FONT,
                        GUIHelper.getFont(fontData),
                        displayMode,
                        label);
            }

            CSS2FontProperties validationErrorFontProperties =
                    (CSS2FontProperties) NatTableCSSHelper.getContextValue(
                            context,
                            displayMode,
                            NatTableCSSConstants.CV_VALIDATION_ERROR_FONT_PROPERTIES);
            if (validationErrorFontProperties != null) {
                // if there are font properties, register the font
                FontData fontData = CSSSWTFontHelper.getFontData(
                        validationErrorFontProperties,
                        CSSSWTFontHelper.getFirstFontData(GUIHelper.DEFAULT_FONT));
                NatTableCSSHelper.applyNatTableStyle(
                        natTable,
                        EditConfigAttributes.VALIDATION_ERROR_STYLE,
                        CellStyleAttributes.FONT,
                        GUIHelper.getFont(fontData),
                        displayMode,
                        label);
            }

            Object pv = NatTableCSSHelper.getContextValue(context, displayMode, NatTableCSSConstants.PAINTER);
            if (pv != null && pv instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> painterValues = (List<String>) pv;

                Map<String, Object> painterProperties =
                        NatTableCSSHelper.getPainterPropertiesInherited(context, natTableContext, displayMode);

                ICellPainter painter = CellPainterFactory.getInstance().getCellPainter(painterValues, painterProperties);

                if (painter != null) {
                    natTable.getConfigRegistry().registerConfigAttribute(
                            CellConfigAttributes.CELL_PAINTER,
                            painter,
                            displayMode,
                            label);
                }
            } else if (NatTableCSSHelper.resolvePainter(context, natTableContext, displayMode)) {
                String backgroundKey = (String) NatTableCSSHelper.getContextValueInherited(
                        context,
                        natTableContext,
                        displayMode,
                        NatTableCSSConstants.CV_BACKGROUND_PAINTER);
                List<String> decoratorKeys = NatTableCSSHelper.getDecoratorPainter(context, displayMode);
                String contentKey = (String) NatTableCSSHelper.getContextValue(
                        context,
                        displayMode,
                        NatTableCSSConstants.CV_CONTENT_PAINTER);
                Map<String, Object> painterProperties =
                        NatTableCSSHelper.getPainterPropertiesInherited(context, natTableContext, displayMode);

                if ((painterProperties.containsKey(NatTableCSSConstants.PADDING_TOP)
                        || painterProperties.containsKey(NatTableCSSConstants.PADDING_LEFT)
                        || painterProperties.containsKey(NatTableCSSConstants.PADDING_BOTTOM)
                        || painterProperties.containsKey(NatTableCSSConstants.PADDING_RIGHT))
                        && !decoratorKeys.contains(CellPainterFactory.PADDING_DECORATOR_KEY)) {
                    decoratorKeys.add(0, CellPainterFactory.PADDING_DECORATOR_KEY);
                }

                if (borderStyle != null && !decoratorKeys.contains(CellPainterFactory.LINE_BORDER_DECORATOR_KEY)) {
                    decoratorKeys.add(0, CellPainterFactory.LINE_BORDER_DECORATOR_KEY);
                }

                ICellPainter painter = CellPainterFactory.getInstance().getCellPainter(
                        backgroundKey,
                        decoratorKeys,
                        contentKey,
                        painterProperties);

                if (painter != null) {
                    natTable.getConfigRegistry().registerConfigAttribute(
                            CellConfigAttributes.CELL_PAINTER,
                            painter,
                            displayMode,
                            label);
                }
            }
        }
    }
}

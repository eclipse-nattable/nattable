/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.TextDecorationEnum;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.theme.DefaultNatTableThemeConfiguration;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * ThemeConfiguration that extends the DefaultNatTableThemeConfiguration and adds styling
 * configuration for the GroupBy feature that comes only with the GlazedLists extension.
 * <p>
 * Note: It is not possible to change the {@link ICellPainter} via this default theme configuration.
 * 		 The reason for this is that the implementation {@link GroupByHeaderPainter} contains
 * 		 several technical details that are necessary to make the GroupBy feature work as it is.
 * 		 Changing the painter could break the feature.
 * </p>
 * <p>
 * Note: Simply changing the font doesn't result in automatically resizing the GroupBy header height.
 * 		 This is the same as for all other layers too, as for calculation of the row height, the
 * 		 GC is necessary. To support also bigger fonts in the GroupBy header region, you are able
 * 		 to resize the GroupBy header manually like this:<br/>
 * <pre>
 * natTable.doCommand(new RowResizeCommand(groupByHeaderLayer, 0, 50));
 * </pre>
 * </p>
 * 
 * @author Dirk Fauth
 *
 */
public class DefaultGroupByNatTableThemeConfiguration extends DefaultNatTableThemeConfiguration {

	// group by style
	
	public Color groupByBgColor 						= null;
	public Color groupByFgColor 						= null;
	public Color groupByGradientBgColor 				= null;
	public Color groupByGradientFgColor 				= null;
	public HorizontalAlignmentEnum groupByHAlign 		= null;
	public VerticalAlignmentEnum groupByVAlign 			= null;
	public Font groupByFont 							= null;
	public Image groupByImage 							= null;
	public BorderStyle groupByBorderStyle 				= null;
	public Character groupByPWEchoChar 					= null;
	public TextDecorationEnum groupByTextDecoration 	= null;
	
	// group by hint style
	
	public String groupByHint = null;

	public Color groupByHintBgColor 					= null;
	public Color groupByHintFgColor 					= null;
	public Color groupByHintGradientBgColor 			= null;
	public Color groupByHintGradientFgColor 			= null;
	public HorizontalAlignmentEnum groupByHintHAlign 	= null;
	public VerticalAlignmentEnum groupByHintVAlign 		= null;
	public Font groupByHintFont 						= null;
	public Image groupByHintImage 						= null;
	public BorderStyle groupByHintBorderStyle 			= null;
	public Character groupByHintPWEchoChar 				= null;
	public TextDecorationEnum groupByHintTextDecoration	= null;

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		super.configureRegistry(configRegistry);
		
		configureGroupByStyle(configRegistry);
		configureGroupByHint(configRegistry);
	}

	/**
	 * Registering the style configuration for the GroupBy header region.
	 * Note that it is not possible to exchange the ICellPainter that is used to
	 * render the GroupBy header region, as it contains a lot of internal code.
	 * @param configRegistry The IConfigRegistry that is used by the NatTable instance
	 * 			to which the style configuration should be applied to.
	 */
	protected void configureGroupByStyle(IConfigRegistry configRegistry) {
		IStyle groupByStyle = getGroupByStyle();
		if (!isStyleEmpty(groupByStyle)) {
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_STYLE,
					groupByStyle,
					DisplayMode.NORMAL,
					GroupByHeaderLayer.GROUP_BY_REGION);
		}
	}
	
	/**
	 * Returns the {@link IStyle} that should be used to render the GroupBy region in a NatTable.
	 * <p>
	 * That means this {@link IStyle} is registered against {@link DisplayMode#NORMAL}
	 * and the configuration label {@link GroupByHeaderLayer#GROUP_BY_REGION}.
	 * </p>
	 * <p>
	 * If this method returns <code>null</code>, no value will be registered to keep the
	 * IConfigRegistry clean. The result would be the same, as if no value is found in the
	 * IConfigRegistry. In this case the rendering will fallback to the default configuration.
	 * </p>
	 * @return The {@link IStyle} that should be used to render the GroupBy region in a NatTable. 
	 */
	protected IStyle getGroupByStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, groupByBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, groupByFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, groupByGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, groupByGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, groupByHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, groupByVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, groupByFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, groupByImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, groupByBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, groupByPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, groupByTextDecoration);
		return cellStyle;
	}
	
	/**
	 * Method to configure the styling of the GroupBy hint in the GroupBy header.
	 * <p>
	 * The GroupBy hint is usually rendered by the GroupByHeaderPainter. If you registered
	 * a different {@link ICellPainter} for the GroupByHeaderLayer, this might not be interpreted.
	 * </p>
	 * @param configRegistry The IConfigRegistry that is used by the NatTable instance
	 * 			to which the style configuration should be applied to.
	 */
	protected void configureGroupByHint(IConfigRegistry configRegistry) {
		String groupByHint = getGroupByHint();
		if (groupByHint != null && groupByHint.length() > 0) {
			configRegistry.registerConfigAttribute(
					GroupByConfigAttributes.GROUP_BY_HINT,
					groupByHint);
		}
		
		IStyle hintStyle = getGroupByHintStyle();
		if (!isStyleEmpty(hintStyle)) {
			configRegistry.registerConfigAttribute(
					GroupByConfigAttributes.GROUP_BY_HINT_STYLE,
					hintStyle);
		}
	}
	
	/**
	 * @return The hint that should be rendered in case there is no grouping applied.
	 */
	protected String getGroupByHint() {
		return this.groupByHint;
	}
	
	/**
	 * @return The {@link IStyle} that should be used to render the GroupBy hint.
	 */
	protected IStyle getGroupByHintStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, groupByHintBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, groupByHintFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, groupByHintGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, groupByHintGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, groupByHintHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, groupByHintVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, groupByHintFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, groupByHintImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, groupByHintBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, groupByHintPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, groupByHintTextDecoration);
		return cellStyle;
	}
	
	@Override
	public void unregisterThemeStyleConfigurations(IConfigRegistry configRegistry) {
		super.unregisterThemeStyleConfigurations(configRegistry);
		
		if (!isStyleEmpty(getGroupByStyle()))
			configRegistry.unregisterConfigAttribute(
					CellConfigAttributes.CELL_STYLE,
					DisplayMode.NORMAL,
					GroupByHeaderLayer.GROUP_BY_REGION);
		
		String groupByHint = getGroupByHint();
		if (groupByHint != null && groupByHint.length() > 0)
			configRegistry.unregisterConfigAttribute(
					GroupByConfigAttributes.GROUP_BY_HINT,
					groupByHint);
		
		if (!isStyleEmpty(getGroupByHintStyle()))
			configRegistry.unregisterConfigAttribute(
					GroupByConfigAttributes.GROUP_BY_HINT_STYLE);
	}
}

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
package org.eclipse.nebula.widgets.nattable.style.theme;

import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.LineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.TextDecorationEnum;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;

/**
 * ThemeConfiguration that contains the basic styles that can be configured for a NatTable. The style
 * values are the same as in the single default configurations, which leads to the "classic" NatTable
 * styling.
 * <p>
 * There are several ways to create a new theme using this configuration as basis:
 * <ol>
 * <li>Set different values for the style attributes via instance init block. For example:<br/>
 * <code><pre>new DefaultNatTableThemeConfiguration {
 * 	{
 * 		this.defaultFgColor = GUIHelper.COLOR_BLUE;
 * 		...
 * 	}
 * }
 * </pre></code>
 * </li>
 * <li>Override the getters directly</li>
 * <li>Override the configureXxx() methods directly</li>
 * </ol>
 * </p>
 * <p>
 * While this ThemeConfiguration only contains the basic styling, you are also able to extend this and
 * add additional stylings, e.g. conditional stylings for custom labels. But on adding new stylings you
 * also need to ensure that the custom styling is removed in 
 * {@link ThemeConfiguration#unregisterThemeStyleConfigurations(org.eclipse.nebula.widgets.nattable.config.IConfigRegistry)}.
 * This is necessary so the applied styles do not stay in case of theme switches.
 * </p>
 * <p>
 * Note: If styling of the GroupBy header should also be involved in the theme, you need to use the
 * 		 ThemeConfiguration in the GlazedLists extension. The reason for this is that the labels 
 * 		 against which the styles need to be registered are specified there, and there should be
 * 		 no dependency from core to the extensions.
 * </p>
 * 
 * @author Dirk Fauth
 *
 */
public class DefaultNatTableThemeConfiguration extends ThemeConfiguration {

	{
		this.styleCornerLikeColumnHeader = true;
	}
	
	//default styling
	
	public Color defaultBgColor 										= GUIHelper.COLOR_WHITE;
	public Color defaultFgColor 										= GUIHelper.COLOR_BLACK;
	public Color defaultGradientBgColor 								= GUIHelper.COLOR_WHITE;
	public Color defaultGradientFgColor 								= GUIHelper.getColor(136, 212, 215);
	public HorizontalAlignmentEnum defaultHAlign 						= HorizontalAlignmentEnum.CENTER;
	public VerticalAlignmentEnum defaultVAlign 							= VerticalAlignmentEnum.MIDDLE;
	public Font defaultFont 											= GUIHelper.DEFAULT_FONT;
	public Image defaultImage 											= null;
	public BorderStyle defaultBorderStyle 								= null;
	public Character defaultPWEchoChar 									= null;
	public TextDecorationEnum defaultTextDecoration 					= null;

	public ICellPainter defaultCellPainter 								= new LineBorderDecorator(new TextPainter());

	// column header styling
	
	public Color cHeaderBgColor 										= GUIHelper.COLOR_WIDGET_BACKGROUND;
	public Color cHeaderFgColor 										= GUIHelper.COLOR_WIDGET_FOREGROUND;
	public Color cHeaderGradientBgColor 								= GUIHelper.COLOR_WHITE;
	public Color cHeaderGradientFgColor 								= GUIHelper.getColor(136, 212, 215);
	public HorizontalAlignmentEnum cHeaderHAlign 						= HorizontalAlignmentEnum.CENTER;
	public VerticalAlignmentEnum cHeaderVAlign 							= VerticalAlignmentEnum.MIDDLE;
	public Font cHeaderFont 											= GUIHelper.getFont(new FontData("Verdana", 10, SWT.NORMAL)); //$NON-NLS-1$
	public Image cHeaderImage 											= null;
	public BorderStyle cHeaderBorderStyle 								= null;
	public Character cHeaderPWEchoChar 									= null;
	public TextDecorationEnum cHeaderTextDecoration 					= null;

	public ICellPainter cHeaderCellPainter 								= new BeveledBorderDecorator(new TextPainter());

	// row header styling
	
	public Color rHeaderBgColor 										= GUIHelper.COLOR_WIDGET_BACKGROUND;
	public Color rHeaderFgColor 										= GUIHelper.COLOR_WIDGET_FOREGROUND;
	public Color rHeaderGradientBgColor 								= GUIHelper.COLOR_WHITE;
	public Color rHeaderGradientFgColor 								= GUIHelper.getColor(136, 212, 215);
	public HorizontalAlignmentEnum rHeaderHAlign 						= HorizontalAlignmentEnum.CENTER;
	public VerticalAlignmentEnum rHeaderVAlign 							= VerticalAlignmentEnum.MIDDLE;
	public Font rHeaderFont 											= GUIHelper.getFont(new FontData("Verdana", 10, SWT.NORMAL)); //$NON-NLS-1$
	public Image rHeaderImage 											= null;
	public BorderStyle rHeaderBorderStyle 								= null;
	public Character rHeaderPWEchoChar 									= null;
	public TextDecorationEnum rHeaderTextDecoration 					= null;

	public ICellPainter rHeaderCellPainter 								= new TextPainter();

	// corner styling
	
	public Color cornerBgColor 											= GUIHelper.COLOR_WIDGET_BACKGROUND;
	public Color cornerFgColor 											= GUIHelper.COLOR_WIDGET_FOREGROUND;
	public Color cornerGradientBgColor 									= GUIHelper.COLOR_WHITE;
	public Color cornerGradientFgColor 									= GUIHelper.getColor(136, 212, 215);
	public HorizontalAlignmentEnum cornerHAlign 						= HorizontalAlignmentEnum.CENTER;
	public VerticalAlignmentEnum cornerVAlign 							= VerticalAlignmentEnum.MIDDLE;
	public Font cornerFont 												= GUIHelper.getFont(new FontData("Verdana", 10, SWT.NORMAL)); //$NON-NLS-1$
	public Image cornerImage 											= null;
	public BorderStyle cornerBorderStyle 								= null;
	public Character cornerPWEchoChar 									= null;
	public TextDecorationEnum cornerTextDecoration 						= null;

	public ICellPainter cornerCellPainter 								= new BeveledBorderDecorator(new TextPainter());

	// hover styling
	
	public Color defaultHoverBgColor 									= null;
	public Color defaultHoverFgColor 									= null;
	public Color defaultHoverGradientBgColor 							= null;
	public Color defaultHoverGradientFgColor 							= null;
	public HorizontalAlignmentEnum defaultHoverHAlign 					= null;
	public VerticalAlignmentEnum defaultHoverVAlign 					= null;
	public Font defaultHoverFont 										= null;
	public Image defaultHoverImage 										= null;
	public BorderStyle defaultHoverBorderStyle 							= null;
	public Character defaultHoverPWEchoChar 							= null;
	public TextDecorationEnum defaultHoverTextDecoration 				= null;

	public ICellPainter defaultHoverCellPainter 						= null;
	
	public Color bodyHoverBgColor 										= null;
	public Color bodyHoverFgColor 										= null;
	public Color bodyHoverGradientBgColor 								= null;
	public Color bodyHoverGradientFgColor 								= null;
	public HorizontalAlignmentEnum bodyHoverHAlign 						= null;
	public VerticalAlignmentEnum bodyHoverVAlign 						= null;
	public Font bodyHoverFont 											= null;
	public Image bodyHoverImage 										= null;
	public BorderStyle bodyHoverBorderStyle 							= null;
	public Character bodyHoverPWEchoChar 								= null;
	public TextDecorationEnum bodyHoverTextDecoration 					= null;

	public ICellPainter bodyHoverCellPainter 							= null;
	
	public Color cHeaderHoverBgColor 									= null;
	public Color cHeaderHoverFgColor 									= null;
	public Color cHeaderHoverGradientBgColor 							= null;
	public Color cHeaderHoverGradientFgColor 							= null;
	public HorizontalAlignmentEnum cHeaderHoverHAlign 					= null;
	public VerticalAlignmentEnum cHeaderHoverVAlign 					= null;
	public Font cHeaderHoverFont 										= null;
	public Image cHeaderHoverImage 										= null;
	public BorderStyle cHeaderHoverBorderStyle 							= null;
	public Character cHeaderHoverPWEchoChar 							= null;
	public TextDecorationEnum cHeaderHoverTextDecoration 				= null;

	public ICellPainter cHeaderHoverCellPainter 						= null;
	
	public Color rHeaderHoverBgColor 									= null;
	public Color rHeaderHoverFgColor 									= null;
	public Color rHeaderHoverGradientBgColor 							= null;
	public Color rHeaderHoverGradientFgColor 							= null;
	public HorizontalAlignmentEnum rHeaderHoverHAlign 					= null;
	public VerticalAlignmentEnum rHeaderHoverVAlign 					= null;
	public Font rHeaderHoverFont 										= null;
	public Image rHeaderHoverImage 										= null;
	public BorderStyle rHeaderHoverBorderStyle 							= null;
	public Character rHeaderHoverPWEchoChar 							= null;
	public TextDecorationEnum rHeaderHoverTextDecoration 				= null;

	public ICellPainter rHeaderHoverCellPainter 						= null;
	
	//default selection style
	
	public Color defaultSelectionBgColor 								= GUIHelper.COLOR_TITLE_INACTIVE_BACKGROUND;
	public Color defaultSelectionFgColor 								= GUIHelper.COLOR_BLACK;
	public Color defaultSelectionGradientBgColor 						= null;
	public Color defaultSelectionGradientFgColor 						= null;
	public HorizontalAlignmentEnum defaultSelectionHAlign 				= null;
	public VerticalAlignmentEnum defaultSelectionVAlign 				= null;
	public Font defaultSelectionFont 									= GUIHelper.getFont(new FontData("Verdana", 8, SWT.BOLD | SWT.ITALIC)); //$NON-NLS-1$
	public Image defaultSelectionImage 									= null;
	public BorderStyle defaultSelectionBorderStyle 						= null;
	public Character defaultSelectionPWEchoChar 						= null;
	public TextDecorationEnum defaultSelectionTextDecoration 			= null;

	public ICellPainter defaultSelectionCellPainter 					= null;

	// column header selection styling
	
	public Color cHeaderSelectionBgColor 								= GUIHelper.COLOR_GRAY;
	public Color cHeaderSelectionFgColor 								= GUIHelper.COLOR_WHITE;
	public Color cHeaderSelectionGradientBgColor 						= null;
	public Color cHeaderSelectionGradientFgColor 						= null;
	public HorizontalAlignmentEnum cHeaderSelectionHAlign 				= null;
	public VerticalAlignmentEnum cHeaderSelectionVAlign 				= null;
	public Font cHeaderSelectionFont 									= GUIHelper.getFont(new FontData("Verdana", 10, SWT.BOLD)); //$NON-NLS-1$
	public Image cHeaderSelectionImage 									= null;
	public BorderStyle cHeaderSelectionBorderStyle 						= null;
	public Character cHeaderSelectionPWEchoChar 						= null;
	public TextDecorationEnum cHeaderSelectionTextDecoration 			= null;

	public ICellPainter cHeaderSelectionCellPainter 					= null;

	// column header full selection styling
	
	public Color cHeaderFullSelectionBgColor 							= GUIHelper.COLOR_WIDGET_NORMAL_SHADOW;
	public Color cHeaderFullSelectionFgColor 							= null;
	public Color cHeaderFullSelectionGradientBgColor 					= null;
	public Color cHeaderFullSelectionGradientFgColor 					= null;
	public HorizontalAlignmentEnum cHeaderFullSelectionHAlign 			= null;
	public VerticalAlignmentEnum cHeaderFullSelectionVAlign 			= null;
	public Font cHeaderFullSelectionFont 								= null;
	public Image cHeaderFullSelectionImage 								= null;
	public BorderStyle cHeaderFullSelectionBorderStyle 					= null;
	public Character cHeaderFullSelectionPWEchoChar 					= null;
	public TextDecorationEnum cHeaderFullSelectionTextDecoration 		= null;

	public ICellPainter cHeaderFullSelectionCellPainter 				= null;

	// row header selection styling
	
	public Color rHeaderSelectionBgColor 								= GUIHelper.COLOR_GRAY;
	public Color rHeaderSelectionFgColor 								= GUIHelper.COLOR_WHITE;
	public Color rHeaderSelectionGradientBgColor 						= null;
	public Color rHeaderSelectionGradientFgColor 						= null;
	public HorizontalAlignmentEnum rHeaderSelectionHAlign 				= null;
	public VerticalAlignmentEnum rHeaderSelectionVAlign 				= null;
	public Font rHeaderSelectionFont 									= GUIHelper.getFont(new FontData("Verdana", 10, SWT.BOLD)); //$NON-NLS-1$
	public Image rHeaderSelectionImage 									= null;
	public BorderStyle rHeaderSelectionBorderStyle 						= null;
	public Character rHeaderSelectionPWEchoChar 						= null;
	public TextDecorationEnum rHeaderSelectionTextDecoration 			= null;

	public ICellPainter rHeaderSelectionCellPainter 					= null;

	// row header full selection styling
	
	public Color rHeaderFullSelectionBgColor 							= GUIHelper.COLOR_WIDGET_NORMAL_SHADOW;
	public Color rHeaderFullSelectionFgColor 							= null;
	public Color rHeaderFullSelectionGradientBgColor 					= null;
	public Color rHeaderFullSelectionGradientFgColor 					= null;
	public HorizontalAlignmentEnum rHeaderFullSelectionHAlign 			= null;
	public VerticalAlignmentEnum rHeaderFullSelectionVAlign 			= null;
	public Font rHeaderFullSelectionFont 								= null;
	public Image rHeaderFullSelectionImage 								= null;
	public BorderStyle rHeaderFullSelectionBorderStyle 					= null;
	public Character rHeaderFullSelectionPWEchoChar 					= null;
	public TextDecorationEnum rHeaderFullSelectionTextDecoration 		= null;

	public ICellPainter rHeaderFullSelectionCellPainter 				= null;

	// corner selection styling
	
	public Color cornerSelectionBgColor 								= GUIHelper.COLOR_GRAY;
	public Color cornerSelectionFgColor 								= GUIHelper.COLOR_WHITE;
	public Color cornerSelectionGradientBgColor 						= null;
	public Color cornerSelectionGradientFgColor 						= null;
	public HorizontalAlignmentEnum cornerSelectionHAlign 				= null;
	public VerticalAlignmentEnum cornerSelectionVAlign 					= null;
	public Font cornerSelectionFont 									= GUIHelper.getFont(new FontData("Verdana", 10, SWT.BOLD)); //$NON-NLS-1$
	public Image cornerSelectionImage 									= null;
	public BorderStyle cornerSelectionBorderStyle 						= null;
	public Character cornerSelectionPWEchoChar 							= null;
	public TextDecorationEnum cornerSelectionTextDecoration 			= null;

	public ICellPainter cornerSelectionCellPainter 						= null;
	
	// selection anchor
	
	public Color selectionAnchorBgColor 								= null;
	public Color selectionAnchorFgColor 								= null;
	public Color selectionAnchorGradientBgColor 						= null;
	public Color selectionAnchorGradientFgColor 						= null;
	public HorizontalAlignmentEnum selectionAnchorHAlign 				= null;
	public VerticalAlignmentEnum selectionAnchorVAlign 					= null;
	public Font selectionAnchorFont 									= null;
	public Image selectionAnchorImage 									= null;
	public BorderStyle selectionAnchorBorderStyle 						= new BorderStyle(1, GUIHelper.COLOR_DARK_GRAY, LineStyleEnum.SOLID);
	public Character selectionAnchorPWEchoChar 							= null;
	public TextDecorationEnum selectionAnchorTextDecoration 			= null;

	public ICellPainter selectionAnchorCellPainter 						= null;
	
	public Color selectionAnchorSelectionBgColor 						= GUIHelper.COLOR_GRAY;
	public Color selectionAnchorSelectionFgColor 						= GUIHelper.COLOR_WHITE;
	public Color selectionAnchorSelectionGradientBgColor 				= null;
	public Color selectionAnchorSelectionGradientFgColor 				= null;
	public HorizontalAlignmentEnum selectionAnchorSelectionHAlign 		= null;
	public VerticalAlignmentEnum selectionAnchorSelectionVAlign 		= null;
	public Font selectionAnchorSelectionFont 							= null;
	public Image selectionAnchorSelectionImage 							= null;
	public BorderStyle selectionAnchorSelectionBorderStyle 				= null;
	public Character selectionAnchorSelectionPWEchoChar 				= null;
	public TextDecorationEnum selectionAnchorSelectionTextDecoration 	= null;

	public ICellPainter selectionAnchorSelectionCellPainter 			= null;
	
	// selection anchor grid line style
	
	public BorderStyle selectionAnchorGridBorderStyle 					= new BorderStyle(1, GUIHelper.COLOR_BLACK, LineStyleEnum.DOTTED);
	
	// alternating row style
	
	public Color evenRowBgColor 										= GUIHelper.COLOR_WIDGET_BACKGROUND;
	public Color evenRowFgColor 										= null;
	public Color evenRowGradientBgColor 								= null;
	public Color evenRowGradientFgColor 								= null;
	public HorizontalAlignmentEnum evenRowHAlign 						= null;
	public VerticalAlignmentEnum evenRowVAlign 							= null;
	public Font evenRowFont 											= null;
	public Image evenRowImage 											= null;
	public BorderStyle evenRowBorderStyle 								= null;
	public Character evenRowPWEchoChar 									= null;
	public TextDecorationEnum evenRowTextDecoration 					= null;

	public ICellPainter evenRowCellPainter 								= null;
	
	public Color oddRowBgColor 											= GUIHelper.COLOR_WHITE;
	public Color oddRowFgColor 											= null;
	public Color oddRowGradientBgColor 									= null;
	public Color oddRowGradientFgColor 									= null;
	public HorizontalAlignmentEnum oddRowHAlign 						= null;
	public VerticalAlignmentEnum oddRowVAlign 							= null;
	public Font oddRowFont 												= null;
	public Image oddRowImage 											= null;
	public BorderStyle oddRowBorderStyle 								= null;
	public Character oddRowPWEchoChar 									= null;
	public TextDecorationEnum oddRowTextDecoration 						= null;

	public ICellPainter oddRowCellPainter 								= null;
	
	// column/row group header style
	
	public Color cGroupHeaderBgColor 									= null;
	public Color cGroupHeaderFgColor 									= null;
	public Color cGroupHeaderGradientBgColor 							= null;
	public Color cGroupHeaderGradientFgColor 							= null;
	public HorizontalAlignmentEnum cGroupHeaderHAlign 					= null;
	public VerticalAlignmentEnum cGroupHeaderVAlign 					= null;
	public Font cGroupHeaderFont 										= null;
	public Image cGroupHeaderImage 										= null;
	public BorderStyle cGroupHeaderBorderStyle 							= null;
	public Character cGroupHeaderPWEchoChar 							= null;
	public TextDecorationEnum cGroupHeaderTextDecoration 				= null;

	public ICellPainter cGroupHeaderCellPainter 						= null;
	
	public Color rGroupHeaderBgColor 									= null;
	public Color rGroupHeaderFgColor 									= null;
	public Color rGroupHeaderGradientBgColor 							= null;
	public Color rGroupHeaderGradientFgColor 							= null;
	public HorizontalAlignmentEnum rGroupHeaderHAlign 					= null;
	public VerticalAlignmentEnum rGroupHeaderVAlign 					= null;
	public Font rGroupHeaderFont 										= null;
	public Image rGroupHeaderImage 										= null;
	public BorderStyle rGroupHeaderBorderStyle 							= null;
	public Character rGroupHeaderPWEchoChar 							= null;
	public TextDecorationEnum rGroupHeaderTextDecoration 				= null;

	public ICellPainter rGroupHeaderCellPainter 						= null;
	
	// sort header style
	
	public Color sortHeaderBgColor 										= null;
	public Color sortHeaderFgColor 										= null;
	public Color sortHeaderGradientBgColor 								= null;
	public Color sortHeaderGradientFgColor 								= null;
	public HorizontalAlignmentEnum sortHeaderHAlign 					= null;
	public VerticalAlignmentEnum sortHeaderVAlign 						= null;
	public Font sortHeaderFont 											= null;
	public Image sortHeaderImage 										= null;
	public BorderStyle sortHeaderBorderStyle 							= null;
	public Character sortHeaderPWEchoChar 								= null;
	public TextDecorationEnum sortHeaderTextDecoration 					= null;

	public ICellPainter sortHeaderCellPainter 							= new BeveledBorderDecorator(new SortableHeaderTextPainter());
	
	public Color selectedSortHeaderBgColor 								= null;
	public Color selectedSortHeaderFgColor 								= null;
	public Color selectedSortHeaderGradientBgColor 						= null;
	public Color selectedSortHeaderGradientFgColor 						= null;
	public HorizontalAlignmentEnum selectedSortHeaderHAlign 			= null;
	public VerticalAlignmentEnum selectedSortHeaderVAlign 				= null;
	public Font selectedSortHeaderFont 									= null;
	public Image selectedSortHeaderImage 								= null;
	public BorderStyle selectedSortHeaderBorderStyle 					= null;
	public Character selectedSortHeaderPWEchoChar 						= null;
	public TextDecorationEnum selectedSortHeaderTextDecoration 			= null;

	public ICellPainter selectedSortHeaderCellPainter 					= null;
	
	// filter row style
	
	public Color filterRowBgColor 										= null;
	public Color filterRowFgColor 										= null;
	public Color filterRowGradientBgColor 								= null;
	public Color filterRowGradientFgColor 								= null;
	public HorizontalAlignmentEnum filterRowHAlign 						= null;
	public VerticalAlignmentEnum filterRowVAlign 						= null;
	public Font filterRowFont 											= null;
	public Image filterRowImage 										= null;
	public BorderStyle filterRowBorderStyle 							= null;
	public Character filterRowPWEchoChar 								= null;
	public TextDecorationEnum filterRowTextDecoration 					= null;

	public ICellPainter filterRowCellPainter 							= new FilterRowPainter();
	
	// tree style
	
	public Color treeBgColor 											= null;
	public Color treeFgColor 											= null;
	public Color treeGradientBgColor 									= null;
	public Color treeGradientFgColor 									= null;
	public HorizontalAlignmentEnum treeHAlign 							= HorizontalAlignmentEnum.LEFT;
	public VerticalAlignmentEnum treeVAlign 							= null;
	public Font treeFont 												= null;
	public Image treeImage 												= null;
	public BorderStyle treeBorderStyle 									= null;
	public Character treePWEchoChar 									= null;
	public TextDecorationEnum treeTextDecoration 						= null;

	public ICellPainter treeCellPainter 								= null;
	
	// summary row style
	
	public Color summaryRowBgColor 										= GUIHelper.COLOR_WHITE;
	public Color summaryRowFgColor 										= GUIHelper.COLOR_BLACK;
	public Color summaryRowGradientBgColor 								= null;
	public Color summaryRowGradientFgColor 								= null;
	public HorizontalAlignmentEnum summaryRowHAlign 					= null;
	public VerticalAlignmentEnum summaryRowVAlign 						= null;
	public Font summaryRowFont 											= GUIHelper.getFont(new FontData("Verdana", 8, SWT.BOLD)); //$NON-NLS-1$
	public Image summaryRowImage 										= null;
	public BorderStyle summaryRowBorderStyle 							= new BorderStyle(0, GUIHelper.COLOR_BLACK, LineStyleEnum.DOTTED);
	public Character summaryRowPWEchoChar 								= null;
	public TextDecorationEnum summaryRowTextDecoration 					= null;

	public ICellPainter summaryRowCellPainter 							= null;

	// freeze style
	public Color freezeSeparatorColor									= null;
	
	@Override
	protected IStyle getDefaultCellStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, defaultBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, defaultFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, defaultGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, defaultGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, defaultHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, defaultVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, defaultFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, defaultImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, defaultBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, defaultPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, defaultTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getDefaultCellPainter() {
		return this.defaultCellPainter;
	}

	@Override
	protected IStyle getColumnHeaderStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, cHeaderBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, cHeaderFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, cHeaderGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, cHeaderGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, cHeaderHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, cHeaderVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, cHeaderFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, cHeaderImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, cHeaderBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, cHeaderPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, cHeaderTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getColumnHeaderCellPainter() {
		return this.cHeaderCellPainter;
	}

	@Override
	protected IStyle getRowHeaderStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, rHeaderBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, rHeaderFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, rHeaderGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, rHeaderGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, rHeaderHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, rHeaderVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, rHeaderFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, rHeaderImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, rHeaderBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, rHeaderPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, rHeaderTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getRowHeaderCellPainter() {
		return this.rHeaderCellPainter;
	}

	@Override
	protected IStyle getCornerStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, cornerBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, cornerFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, cornerGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, cornerGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, cornerHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, cornerVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, cornerFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, cornerImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, cornerBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, cornerPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, cornerTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getCornerCellPainter() {
		return this.cornerCellPainter;
	}

	@Override
	protected IStyle getDefaultHoverStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, defaultHoverBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, defaultHoverFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, defaultHoverGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, defaultHoverGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, defaultHoverHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, defaultHoverVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, defaultHoverFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, defaultHoverImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, defaultHoverBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, defaultHoverPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, defaultHoverTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getDefaultHoverCellPainter() {
		return this.defaultHoverCellPainter;
	}

	@Override
	protected IStyle getBodyHoverStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, bodyHoverBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, bodyHoverFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, bodyHoverGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, bodyHoverGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, bodyHoverHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, bodyHoverVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, bodyHoverFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, bodyHoverImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, bodyHoverBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, bodyHoverPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, bodyHoverTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getBodyHoverCellPainter() {
		return this.bodyHoverCellPainter;
	}

	@Override
	protected IStyle getColumnHeaderHoverStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, cHeaderHoverBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, cHeaderHoverFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, cHeaderHoverGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, cHeaderHoverGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, cHeaderHoverHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, cHeaderHoverVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, cHeaderHoverFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, cHeaderHoverImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, cHeaderHoverBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, cHeaderHoverPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, cHeaderHoverTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getColumnHeaderHoverCellPainter() {
		return this.cHeaderHoverCellPainter;
	}

	@Override
	protected IStyle getRowHeaderHoverStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, rHeaderHoverBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, rHeaderHoverFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, rHeaderHoverGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, rHeaderHoverGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, rHeaderHoverHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, rHeaderHoverVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, rHeaderHoverFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, rHeaderHoverImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, rHeaderHoverBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, rHeaderHoverPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, rHeaderHoverTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getRowHeaderHoverCellPainter() {
		return this.rHeaderHoverCellPainter;
	}

	@Override
	protected IStyle getDefaultSelectionCellStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, defaultSelectionBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, defaultSelectionFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, defaultSelectionGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, defaultSelectionGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, defaultSelectionHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, defaultSelectionVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, defaultSelectionFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, defaultSelectionImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, defaultSelectionBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, defaultSelectionPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, defaultSelectionTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getDefaultSelectionCellPainter() {
		return this.defaultSelectionCellPainter;
	}

	@Override
	protected IStyle getColumnHeaderSelectionStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, cHeaderSelectionBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, cHeaderSelectionFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, cHeaderSelectionGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, cHeaderSelectionGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, cHeaderSelectionHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, cHeaderSelectionVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, cHeaderSelectionFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, cHeaderSelectionImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, cHeaderSelectionBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, cHeaderSelectionPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, cHeaderSelectionTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getColumnHeaderSelectionCellPainter() {
		return this.cHeaderSelectionCellPainter;
	}

	@Override
	protected IStyle getColumnHeaderFullSelectionStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, cHeaderFullSelectionBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, cHeaderFullSelectionFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, cHeaderFullSelectionGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, cHeaderFullSelectionGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, cHeaderFullSelectionHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, cHeaderFullSelectionVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, cHeaderFullSelectionFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, cHeaderFullSelectionImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, cHeaderFullSelectionBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, cHeaderFullSelectionPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, cHeaderFullSelectionTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getColumnHeaderFullSelectionCellPainter() {
		return this.cHeaderFullSelectionCellPainter;
	}

	@Override
	protected IStyle getRowHeaderSelectionStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, rHeaderSelectionBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, rHeaderSelectionFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, rHeaderSelectionGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, rHeaderSelectionGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, rHeaderSelectionHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, rHeaderSelectionVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, rHeaderSelectionFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, rHeaderSelectionImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, rHeaderSelectionBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, rHeaderSelectionPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, rHeaderSelectionTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getRowHeaderSelectionCellPainter() {
		return this.rHeaderSelectionCellPainter;
	}

	@Override
	protected IStyle getRowHeaderFullSelectionStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, rHeaderFullSelectionBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, rHeaderFullSelectionFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, rHeaderFullSelectionGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, rHeaderFullSelectionGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, rHeaderFullSelectionHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, rHeaderFullSelectionVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, rHeaderFullSelectionFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, rHeaderFullSelectionImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, rHeaderFullSelectionBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, rHeaderFullSelectionPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, rHeaderFullSelectionTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getRowHeaderFullSelectionCellPainter() {
		return this.rHeaderFullSelectionCellPainter;
	}

	@Override
	protected IStyle getCornerSelectionStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, cornerSelectionBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, cornerSelectionFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, cornerSelectionGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, cornerSelectionGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, cornerSelectionHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, cornerSelectionVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, cornerSelectionFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, cornerSelectionImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, cornerSelectionBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, cornerSelectionPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, cornerSelectionTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getCornerSelectionCellPainter() {
		return this.cornerSelectionCellPainter;
	}

	@Override
	protected IStyle getSelectionAnchorStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, selectionAnchorBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, selectionAnchorFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, selectionAnchorGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, selectionAnchorGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, selectionAnchorHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, selectionAnchorVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, selectionAnchorFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, selectionAnchorImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, selectionAnchorBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, selectionAnchorPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, selectionAnchorTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getSelectionAnchorCellPainter() {
		return this.selectionAnchorCellPainter;
	}

	@Override
	protected IStyle getSelectionAnchorSelectionStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, selectionAnchorSelectionBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, selectionAnchorSelectionFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, selectionAnchorSelectionGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, selectionAnchorSelectionGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, selectionAnchorSelectionHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, selectionAnchorSelectionVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, selectionAnchorSelectionFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, selectionAnchorSelectionImage);
		
		//if there is not explicitly another border style configured use the same as in getSelectionAnchorStyle()
		BorderStyle border = selectionAnchorSelectionBorderStyle != null ? selectionAnchorSelectionBorderStyle : selectionAnchorBorderStyle;
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, border);
		
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, selectionAnchorSelectionPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, selectionAnchorSelectionTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getSelectionAnchorSelectionCellPainter() {
		return this.selectionAnchorSelectionCellPainter;
	}

	@Override
	protected IStyle getSelectionAnchorGridLineStyle() {
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, selectionAnchorGridBorderStyle);
		return cellStyle;
	}

	@Override
	protected IStyle getEvenRowStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, evenRowBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, evenRowFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, evenRowGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, evenRowGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, evenRowHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, evenRowVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, evenRowFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, evenRowImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, evenRowBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, evenRowPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, evenRowTextDecoration);
		return cellStyle;
	}
	
	@Override
	protected ICellPainter getEvenRowCellPainter() {
		return this.evenRowCellPainter;
	}

	@Override
	protected IStyle getOddRowStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, oddRowBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, oddRowFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, oddRowGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, oddRowGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, oddRowHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, oddRowVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, oddRowFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, oddRowImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, oddRowBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, oddRowPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, oddRowTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getOddRowCellPainter() {
		return this.oddRowCellPainter;
	}

	@Override
	protected IStyle getColumnGroupHeaderStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, cGroupHeaderBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, cGroupHeaderFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, cGroupHeaderGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, cGroupHeaderGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, cGroupHeaderHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, cGroupHeaderVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, cGroupHeaderFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, cGroupHeaderImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, cGroupHeaderBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, cGroupHeaderPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, cGroupHeaderTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getColumnGroupHeaderCellPainter() {
		return this.cGroupHeaderCellPainter;
	}

	@Override
	protected IStyle getRowGroupHeaderStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, rGroupHeaderBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, rGroupHeaderFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, rGroupHeaderGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, rGroupHeaderGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, rGroupHeaderHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, rGroupHeaderVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, rGroupHeaderFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, rGroupHeaderImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, rGroupHeaderBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, rGroupHeaderPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, rGroupHeaderTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getRowGroupHeaderCellPainter() {
		return this.rGroupHeaderCellPainter;
	}

	@Override
	protected IStyle getSortHeaderStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, sortHeaderBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, sortHeaderFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, sortHeaderGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, sortHeaderGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, sortHeaderHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, sortHeaderVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, sortHeaderFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, sortHeaderImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, sortHeaderBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, sortHeaderPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, sortHeaderTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getSortHeaderCellPainter() {
		return this.sortHeaderCellPainter;
	}

	@Override
	protected IStyle getSelectedSortHeaderStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, selectedSortHeaderBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, selectedSortHeaderFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, selectedSortHeaderGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, selectedSortHeaderGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, selectedSortHeaderHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, selectedSortHeaderVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, selectedSortHeaderFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, selectedSortHeaderImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, selectedSortHeaderBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, selectedSortHeaderPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, selectedSortHeaderTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getSelectedSortHeaderCellPainter() {
		return this.selectedSortHeaderCellPainter;
	}

	@Override
	protected IStyle getFilterRowStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, filterRowBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, filterRowFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, filterRowGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, filterRowGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, filterRowHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, filterRowVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, filterRowFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, filterRowImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, filterRowBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, filterRowPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, filterRowTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getFilterRowCellPainter() {
		return this.filterRowCellPainter;
	}

	@Override
	protected IStyle getTreeStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, treeBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, treeFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, treeGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, treeGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, treeHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, treeVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, treeFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, treeImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, treeBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, treePWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, treeTextDecoration);
		return cellStyle;
	}

	@Override
	protected ICellPainter getTreeCellPainter() {
		return this.treeCellPainter;
	}
	
	@Override
	protected IStyle getSummaryRowStyle() {
		IStyle cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, summaryRowBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, summaryRowFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, summaryRowGradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, summaryRowGradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, summaryRowHAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, summaryRowVAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, summaryRowFont);
		cellStyle.setAttributeValue(CellStyleAttributes.IMAGE, summaryRowImage);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, summaryRowBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR, summaryRowPWEchoChar);
		cellStyle.setAttributeValue(CellStyleAttributes.TEXT_DECORATION, summaryRowTextDecoration);
		return cellStyle;
	}
	
	@Override
	protected ICellPainter getSummaryRowCellPainter() {
		return this.summaryRowCellPainter;
	}

	@Override
	protected Color getFreezeSeparatorColor() {
		return this.freezeSeparatorColor;
	}

}

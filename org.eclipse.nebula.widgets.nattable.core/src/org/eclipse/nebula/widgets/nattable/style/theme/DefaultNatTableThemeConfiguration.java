/*******************************************************************************
 * Copyright (c) 2014, 2016 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *    Loris Securo <lorissek@gmail.com> - Bug 499622, 500764
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style.theme;

import org.eclipse.nebula.widgets.nattable.group.painter.ColumnGroupHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.group.painter.RowGroupHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.LineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.BorderModeEnum;
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
 * ThemeConfiguration that contains the basic styles that can be configured for
 * a NatTable. The style values are the same as in the single default
 * configurations, which leads to the "classic" NatTable styling.
 * <p>
 * There are several ways to create a new theme using this configuration as
 * basis:
 * </p>
 * <ol>
 * <li>Set different values for the style attributes via instance init block.
 * For example:<br>
 *
 * <pre>
 * new DefaultNatTableThemeConfiguration {
 * 	{
 * 		this.defaultFgColor = GUIHelper.COLOR_BLUE;
 * 		...
 * 	}
 * }
 * </pre>
 *
 * </li>
 * <li>Override the getters directly</li>
 * <li>Override the configureXxx() methods directly</li>
 * </ol>
 * <p>
 * While this ThemeConfiguration only contains the basic styling, you are also
 * able to extend this and add additional stylings, e.g. conditional stylings
 * for custom labels. But on adding new stylings you also need to ensure that
 * the custom styling is removed in
 * {@link ThemeConfiguration#unregisterThemeStyleConfigurations(org.eclipse.nebula.widgets.nattable.config.IConfigRegistry)}
 * . This is necessary so the applied styles do not stay in case of theme
 * switches.
 * </p>
 * <p>
 * Instead of extending an existing ThemeConfiguration you are also able to
 * create and register {@link IThemeExtension}s to add additional styling.
 * Creating IThemeExtension gives you the most possible flexibility on creating,
 * modifying and extending existing themes.
 * </p>
 * <p>
 * Note: If styling of the GroupBy header should also be involved in the theme,
 * you need to register a matching IThemeExtension out of the GlazedLists
 * extension. The reason for this is that the labels against which the styles
 * need to be registered are specified there, and there should be no dependency
 * from core to the extensions. Have a look at the DefaultGroupByThemeExtension
 * or the ModernGroupByThemeExtension for example.
 * </p>
 */
public class DefaultNatTableThemeConfiguration extends ThemeConfiguration {

    {
        this.styleCornerLikeColumnHeader = true;
    }

    // default styling

    public Color defaultBgColor = GUIHelper.COLOR_WHITE;
    public Color defaultFgColor = GUIHelper.COLOR_BLACK;
    public Color defaultGradientBgColor = GUIHelper.COLOR_WHITE;
    public Color defaultGradientFgColor = GUIHelper.getColor(136, 212, 215);
    public HorizontalAlignmentEnum defaultHAlign = HorizontalAlignmentEnum.CENTER;
    public VerticalAlignmentEnum defaultVAlign = VerticalAlignmentEnum.MIDDLE;
    public Font defaultFont = GUIHelper.DEFAULT_FONT;
    public Image defaultImage = null;
    public BorderStyle defaultBorderStyle = null;
    public Character defaultPWEchoChar = null;
    public TextDecorationEnum defaultTextDecoration = null;

    public ICellPainter defaultCellPainter = new LineBorderDecorator(new TextPainter());

    // column header styling

    public Color cHeaderBgColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
    public Color cHeaderFgColor = GUIHelper.COLOR_WIDGET_FOREGROUND;
    public Color cHeaderGradientBgColor = GUIHelper.COLOR_WHITE;
    public Color cHeaderGradientFgColor = GUIHelper.getColor(136, 212, 215);
    public HorizontalAlignmentEnum cHeaderHAlign = HorizontalAlignmentEnum.CENTER;
    public VerticalAlignmentEnum cHeaderVAlign = VerticalAlignmentEnum.MIDDLE;
    public Font cHeaderFont = GUIHelper.getFont(new FontData("Verdana", 10, SWT.NORMAL)); //$NON-NLS-1$
    public Image cHeaderImage = null;
    public BorderStyle cHeaderBorderStyle = null;
    public Character cHeaderPWEchoChar = null;
    public TextDecorationEnum cHeaderTextDecoration = null;

    public ICellPainter cHeaderCellPainter = new BeveledBorderDecorator(new TextPainter());

    // row header styling

    public Color rHeaderBgColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
    public Color rHeaderFgColor = GUIHelper.COLOR_WIDGET_FOREGROUND;
    public Color rHeaderGradientBgColor = GUIHelper.COLOR_WHITE;
    public Color rHeaderGradientFgColor = GUIHelper.getColor(136, 212, 215);
    public HorizontalAlignmentEnum rHeaderHAlign = HorizontalAlignmentEnum.CENTER;
    public VerticalAlignmentEnum rHeaderVAlign = VerticalAlignmentEnum.MIDDLE;
    public Font rHeaderFont = GUIHelper.getFont(new FontData("Verdana", 10, SWT.NORMAL)); //$NON-NLS-1$
    public Image rHeaderImage = null;
    public BorderStyle rHeaderBorderStyle = null;
    public Character rHeaderPWEchoChar = null;
    public TextDecorationEnum rHeaderTextDecoration = null;

    public ICellPainter rHeaderCellPainter = new TextPainter();

    // corner styling

    public Color cornerBgColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
    public Color cornerFgColor = GUIHelper.COLOR_WIDGET_FOREGROUND;
    public Color cornerGradientBgColor = GUIHelper.COLOR_WHITE;
    public Color cornerGradientFgColor = GUIHelper.getColor(136, 212, 215);
    public HorizontalAlignmentEnum cornerHAlign = HorizontalAlignmentEnum.CENTER;
    public VerticalAlignmentEnum cornerVAlign = VerticalAlignmentEnum.MIDDLE;
    public Font cornerFont = GUIHelper.getFont(new FontData("Verdana", 10, SWT.NORMAL)); //$NON-NLS-1$
    public Image cornerImage = null;
    public BorderStyle cornerBorderStyle = null;
    public Character cornerPWEchoChar = null;
    public TextDecorationEnum cornerTextDecoration = null;

    public ICellPainter cornerCellPainter = new BeveledBorderDecorator(new TextPainter());

    // hover styling

    public Color defaultHoverBgColor = null;
    public Color defaultHoverFgColor = null;
    public Color defaultHoverGradientBgColor = null;
    public Color defaultHoverGradientFgColor = null;
    public HorizontalAlignmentEnum defaultHoverHAlign = null;
    public VerticalAlignmentEnum defaultHoverVAlign = null;
    public Font defaultHoverFont = null;
    public Image defaultHoverImage = null;
    public BorderStyle defaultHoverBorderStyle = null;
    public Character defaultHoverPWEchoChar = null;
    public TextDecorationEnum defaultHoverTextDecoration = null;

    public ICellPainter defaultHoverCellPainter = null;

    public Color bodyHoverBgColor = null;
    public Color bodyHoverFgColor = null;
    public Color bodyHoverGradientBgColor = null;
    public Color bodyHoverGradientFgColor = null;
    public HorizontalAlignmentEnum bodyHoverHAlign = null;
    public VerticalAlignmentEnum bodyHoverVAlign = null;
    public Font bodyHoverFont = null;
    public Image bodyHoverImage = null;
    public BorderStyle bodyHoverBorderStyle = null;
    public Character bodyHoverPWEchoChar = null;
    public TextDecorationEnum bodyHoverTextDecoration = null;

    public ICellPainter bodyHoverCellPainter = null;

    public Color cHeaderHoverBgColor = null;
    public Color cHeaderHoverFgColor = null;
    public Color cHeaderHoverGradientBgColor = null;
    public Color cHeaderHoverGradientFgColor = null;
    public HorizontalAlignmentEnum cHeaderHoverHAlign = null;
    public VerticalAlignmentEnum cHeaderHoverVAlign = null;
    public Font cHeaderHoverFont = null;
    public Image cHeaderHoverImage = null;
    public BorderStyle cHeaderHoverBorderStyle = null;
    public Character cHeaderHoverPWEchoChar = null;
    public TextDecorationEnum cHeaderHoverTextDecoration = null;

    public ICellPainter cHeaderHoverCellPainter = null;

    public Color rHeaderHoverBgColor = null;
    public Color rHeaderHoverFgColor = null;
    public Color rHeaderHoverGradientBgColor = null;
    public Color rHeaderHoverGradientFgColor = null;
    public HorizontalAlignmentEnum rHeaderHoverHAlign = null;
    public VerticalAlignmentEnum rHeaderHoverVAlign = null;
    public Font rHeaderHoverFont = null;
    public Image rHeaderHoverImage = null;
    public BorderStyle rHeaderHoverBorderStyle = null;
    public Character rHeaderHoverPWEchoChar = null;
    public TextDecorationEnum rHeaderHoverTextDecoration = null;

    public ICellPainter rHeaderHoverCellPainter = null;

    // hover styling

    public Color defaultHoverSelectionBgColor = null;
    public Color defaultHoverSelectionFgColor = null;
    public Color defaultHoverSelectionGradientBgColor = null;
    public Color defaultHoverSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum defaultHoverSelectionHAlign = null;
    public VerticalAlignmentEnum defaultHoverSelectionVAlign = null;
    public Font defaultHoverSelectionFont = null;
    public Image defaultHoverSelectionImage = null;
    public BorderStyle defaultHoverSelectionBorderStyle = null;
    public Character defaultHoverSelectionPWEchoChar = null;
    public TextDecorationEnum defaultHoverSelectionTextDecoration = null;

    public ICellPainter defaultHoverSelectionCellPainter = null;

    public Color bodyHoverSelectionBgColor = null;
    public Color bodyHoverSelectionFgColor = null;
    public Color bodyHoverSelectionGradientBgColor = null;
    public Color bodyHoverSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum bodyHoverSelectionHAlign = null;
    public VerticalAlignmentEnum bodyHoverSelectionVAlign = null;
    public Font bodyHoverSelectionFont = null;
    public Image bodyHoverSelectionImage = null;
    public BorderStyle bodyHoverSelectionBorderStyle = null;
    public Character bodyHoverSelectionPWEchoChar = null;
    public TextDecorationEnum bodyHoverSelectionTextDecoration = null;

    public ICellPainter bodyHoverSelectionCellPainter = null;

    public Color cHeaderHoverSelectionBgColor = null;
    public Color cHeaderHoverSelectionFgColor = null;
    public Color cHeaderHoverSelectionGradientBgColor = null;
    public Color cHeaderHoverSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum cHeaderHoverSelectionHAlign = null;
    public VerticalAlignmentEnum cHeaderHoverSelectionVAlign = null;
    public Font cHeaderHoverSelectionFont = null;
    public Image cHeaderHoverSelectionImage = null;
    public BorderStyle cHeaderHoverSelectionBorderStyle = null;
    public Character cHeaderHoverSelectionPWEchoChar = null;
    public TextDecorationEnum cHeaderHoverSelectionTextDecoration = null;

    public ICellPainter cHeaderHoverSelectionCellPainter = null;

    public Color rHeaderHoverSelectionBgColor = null;
    public Color rHeaderHoverSelectionFgColor = null;
    public Color rHeaderHoverSelectionGradientBgColor = null;
    public Color rHeaderHoverSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum rHeaderHoverSelectionHAlign = null;
    public VerticalAlignmentEnum rHeaderHoverSelectionVAlign = null;
    public Font rHeaderHoverSelectionFont = null;
    public Image rHeaderHoverSelectionImage = null;
    public BorderStyle rHeaderHoverSelectionBorderStyle = null;
    public Character rHeaderHoverSelectionPWEchoChar = null;
    public TextDecorationEnum rHeaderHoverSelectionTextDecoration = null;

    public ICellPainter rHeaderHoverSelectionCellPainter = null;

    // default selection style

    public Color defaultSelectionBgColor = GUIHelper.COLOR_TITLE_INACTIVE_BACKGROUND;
    public Color defaultSelectionFgColor = GUIHelper.COLOR_BLACK;
    public Color defaultSelectionGradientBgColor = null;
    public Color defaultSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum defaultSelectionHAlign = null;
    public VerticalAlignmentEnum defaultSelectionVAlign = null;
    public Font defaultSelectionFont = GUIHelper.getFont(new FontData("Verdana", 8, SWT.BOLD | SWT.ITALIC)); //$NON-NLS-1$
    public Image defaultSelectionImage = null;
    public BorderStyle defaultSelectionBorderStyle = null;
    public Character defaultSelectionPWEchoChar = null;
    public TextDecorationEnum defaultSelectionTextDecoration = null;

    public ICellPainter defaultSelectionCellPainter = null;

    // column header selection styling

    public Color cHeaderSelectionBgColor = GUIHelper.COLOR_GRAY;
    public Color cHeaderSelectionFgColor = GUIHelper.COLOR_WHITE;
    public Color cHeaderSelectionGradientBgColor = null;
    public Color cHeaderSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum cHeaderSelectionHAlign = null;
    public VerticalAlignmentEnum cHeaderSelectionVAlign = null;
    public Font cHeaderSelectionFont = GUIHelper.getFont(new FontData("Verdana", 10, SWT.BOLD)); //$NON-NLS-1$
    public Image cHeaderSelectionImage = null;
    public BorderStyle cHeaderSelectionBorderStyle = null;
    public Character cHeaderSelectionPWEchoChar = null;
    public TextDecorationEnum cHeaderSelectionTextDecoration = null;

    public ICellPainter cHeaderSelectionCellPainter = null;

    // column header full selection styling

    public Color cHeaderFullSelectionBgColor = GUIHelper.COLOR_WIDGET_NORMAL_SHADOW;
    public Color cHeaderFullSelectionFgColor = null;
    public Color cHeaderFullSelectionGradientBgColor = null;
    public Color cHeaderFullSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum cHeaderFullSelectionHAlign = null;
    public VerticalAlignmentEnum cHeaderFullSelectionVAlign = null;
    public Font cHeaderFullSelectionFont = null;
    public Image cHeaderFullSelectionImage = null;
    public BorderStyle cHeaderFullSelectionBorderStyle = null;
    public Character cHeaderFullSelectionPWEchoChar = null;
    public TextDecorationEnum cHeaderFullSelectionTextDecoration = null;

    public ICellPainter cHeaderFullSelectionCellPainter = null;

    // row header selection styling

    public Color rHeaderSelectionBgColor = GUIHelper.COLOR_GRAY;
    public Color rHeaderSelectionFgColor = GUIHelper.COLOR_WHITE;
    public Color rHeaderSelectionGradientBgColor = null;
    public Color rHeaderSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum rHeaderSelectionHAlign = null;
    public VerticalAlignmentEnum rHeaderSelectionVAlign = null;
    public Font rHeaderSelectionFont = GUIHelper.getFont(new FontData("Verdana", 10, SWT.BOLD)); //$NON-NLS-1$
    public Image rHeaderSelectionImage = null;
    public BorderStyle rHeaderSelectionBorderStyle = null;
    public Character rHeaderSelectionPWEchoChar = null;
    public TextDecorationEnum rHeaderSelectionTextDecoration = null;

    public ICellPainter rHeaderSelectionCellPainter = null;

    // row header full selection styling

    public Color rHeaderFullSelectionBgColor = GUIHelper.COLOR_WIDGET_NORMAL_SHADOW;
    public Color rHeaderFullSelectionFgColor = null;
    public Color rHeaderFullSelectionGradientBgColor = null;
    public Color rHeaderFullSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum rHeaderFullSelectionHAlign = null;
    public VerticalAlignmentEnum rHeaderFullSelectionVAlign = null;
    public Font rHeaderFullSelectionFont = null;
    public Image rHeaderFullSelectionImage = null;
    public BorderStyle rHeaderFullSelectionBorderStyle = null;
    public Character rHeaderFullSelectionPWEchoChar = null;
    public TextDecorationEnum rHeaderFullSelectionTextDecoration = null;

    public ICellPainter rHeaderFullSelectionCellPainter = null;

    // corner selection styling

    public Color cornerSelectionBgColor = GUIHelper.COLOR_GRAY;
    public Color cornerSelectionFgColor = GUIHelper.COLOR_WHITE;
    public Color cornerSelectionGradientBgColor = null;
    public Color cornerSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum cornerSelectionHAlign = null;
    public VerticalAlignmentEnum cornerSelectionVAlign = null;
    public Font cornerSelectionFont = GUIHelper.getFont(new FontData("Verdana", 10, SWT.BOLD)); //$NON-NLS-1$
    public Image cornerSelectionImage = null;
    public BorderStyle cornerSelectionBorderStyle = null;
    public Character cornerSelectionPWEchoChar = null;
    public TextDecorationEnum cornerSelectionTextDecoration = null;

    public ICellPainter cornerSelectionCellPainter = null;

    // selection anchor

    public Color selectionAnchorBgColor = null;
    public Color selectionAnchorFgColor = null;
    public Color selectionAnchorGradientBgColor = null;
    public Color selectionAnchorGradientFgColor = null;
    public HorizontalAlignmentEnum selectionAnchorHAlign = null;
    public VerticalAlignmentEnum selectionAnchorVAlign = null;
    public Font selectionAnchorFont = null;
    public Image selectionAnchorImage = null;
    public BorderStyle selectionAnchorBorderStyle = new BorderStyle(1, GUIHelper.COLOR_DARK_GRAY, LineStyleEnum.SOLID);
    public Character selectionAnchorPWEchoChar = null;
    public TextDecorationEnum selectionAnchorTextDecoration = null;

    public ICellPainter selectionAnchorCellPainter = null;

    public Color selectionAnchorSelectionBgColor = GUIHelper.COLOR_GRAY;
    public Color selectionAnchorSelectionFgColor = GUIHelper.COLOR_WHITE;
    public Color selectionAnchorSelectionGradientBgColor = null;
    public Color selectionAnchorSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum selectionAnchorSelectionHAlign = null;
    public VerticalAlignmentEnum selectionAnchorSelectionVAlign = null;
    public Font selectionAnchorSelectionFont = null;
    public Image selectionAnchorSelectionImage = null;
    public BorderStyle selectionAnchorSelectionBorderStyle = null;
    public Character selectionAnchorSelectionPWEchoChar = null;
    public TextDecorationEnum selectionAnchorSelectionTextDecoration = null;

    public ICellPainter selectionAnchorSelectionCellPainter = null;

    // selection anchor grid line style

    public BorderStyle selectionAnchorGridBorderStyle = new BorderStyle(1, GUIHelper.COLOR_BLACK, LineStyleEnum.DOTTED);

    // alternating row style

    public Color evenRowBgColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
    public Color evenRowFgColor = null;
    public Color evenRowGradientBgColor = null;
    public Color evenRowGradientFgColor = null;
    public HorizontalAlignmentEnum evenRowHAlign = null;
    public VerticalAlignmentEnum evenRowVAlign = null;
    public Font evenRowFont = null;
    public Image evenRowImage = null;
    public BorderStyle evenRowBorderStyle = null;
    public Character evenRowPWEchoChar = null;
    public TextDecorationEnum evenRowTextDecoration = null;

    public ICellPainter evenRowCellPainter = null;

    public Color oddRowBgColor = GUIHelper.COLOR_WHITE;
    public Color oddRowFgColor = null;
    public Color oddRowGradientBgColor = null;
    public Color oddRowGradientFgColor = null;
    public HorizontalAlignmentEnum oddRowHAlign = null;
    public VerticalAlignmentEnum oddRowVAlign = null;
    public Font oddRowFont = null;
    public Image oddRowImage = null;
    public BorderStyle oddRowBorderStyle = null;
    public Character oddRowPWEchoChar = null;
    public TextDecorationEnum oddRowTextDecoration = null;

    public ICellPainter oddRowCellPainter = null;

    // column/row group header style

    public Color cGroupHeaderBgColor = null;
    public Color cGroupHeaderFgColor = null;
    public Color cGroupHeaderGradientBgColor = null;
    public Color cGroupHeaderGradientFgColor = null;
    public HorizontalAlignmentEnum cGroupHeaderHAlign = null;
    public VerticalAlignmentEnum cGroupHeaderVAlign = null;
    public Font cGroupHeaderFont = null;
    public Image cGroupHeaderImage = null;
    public BorderStyle cGroupHeaderBorderStyle = null;
    public Character cGroupHeaderPWEchoChar = null;
    public TextDecorationEnum cGroupHeaderTextDecoration = null;

    public ICellPainter cGroupHeaderCellPainter =
            new BeveledBorderDecorator(
                    new ColumnGroupHeaderTextPainter());

    public Color rGroupHeaderBgColor = null;
    public Color rGroupHeaderFgColor = null;
    public Color rGroupHeaderGradientBgColor = null;
    public Color rGroupHeaderGradientFgColor = null;
    public HorizontalAlignmentEnum rGroupHeaderHAlign = null;
    public VerticalAlignmentEnum rGroupHeaderVAlign = null;
    public Font rGroupHeaderFont = null;
    public Image rGroupHeaderImage = null;
    public BorderStyle rGroupHeaderBorderStyle = null;
    public Character rGroupHeaderPWEchoChar = null;
    public TextDecorationEnum rGroupHeaderTextDecoration = null;

    public ICellPainter rGroupHeaderCellPainter =
            new BeveledBorderDecorator(
                    new RowGroupHeaderTextPainter());

    // sort header style

    public Color sortHeaderBgColor = null;
    public Color sortHeaderFgColor = null;
    public Color sortHeaderGradientBgColor = null;
    public Color sortHeaderGradientFgColor = null;
    public HorizontalAlignmentEnum sortHeaderHAlign = null;
    public VerticalAlignmentEnum sortHeaderVAlign = null;
    public Font sortHeaderFont = null;
    public Image sortHeaderImage = null;
    public BorderStyle sortHeaderBorderStyle = null;
    public Character sortHeaderPWEchoChar = null;
    public TextDecorationEnum sortHeaderTextDecoration = null;

    public ICellPainter sortHeaderCellPainter =
            new BeveledBorderDecorator(
                    new SortableHeaderTextPainter());

    public Color selectedSortHeaderBgColor = null;
    public Color selectedSortHeaderFgColor = null;
    public Color selectedSortHeaderGradientBgColor = null;
    public Color selectedSortHeaderGradientFgColor = null;
    public HorizontalAlignmentEnum selectedSortHeaderHAlign = null;
    public VerticalAlignmentEnum selectedSortHeaderVAlign = null;
    public Font selectedSortHeaderFont = null;
    public Image selectedSortHeaderImage = null;
    public BorderStyle selectedSortHeaderBorderStyle = null;
    public Character selectedSortHeaderPWEchoChar = null;
    public TextDecorationEnum selectedSortHeaderTextDecoration = null;

    public ICellPainter selectedSortHeaderCellPainter = null;

    // filter row style

    public Color filterRowBgColor = null;
    public Color filterRowFgColor = null;
    public Color filterRowGradientBgColor = null;
    public Color filterRowGradientFgColor = null;
    public HorizontalAlignmentEnum filterRowHAlign = null;
    public VerticalAlignmentEnum filterRowVAlign = null;
    public Font filterRowFont = null;
    public Image filterRowImage = null;
    public BorderStyle filterRowBorderStyle = null;
    public Character filterRowPWEchoChar = null;
    public TextDecorationEnum filterRowTextDecoration = null;

    public ICellPainter filterRowCellPainter = null;

    // tree style

    public Color treeBgColor = null;
    public Color treeFgColor = null;
    public Color treeGradientBgColor = null;
    public Color treeGradientFgColor = null;
    public HorizontalAlignmentEnum treeHAlign = HorizontalAlignmentEnum.LEFT;
    public VerticalAlignmentEnum treeVAlign = null;
    public Font treeFont = null;
    public Image treeImage = null;
    public BorderStyle treeBorderStyle = null;
    public Character treePWEchoChar = null;
    public TextDecorationEnum treeTextDecoration = null;

    public ICellPainter treeCellPainter = null;

    public Color treeSelectionBgColor = null;
    public Color treeSelectionFgColor = null;
    public Color treeSelectionGradientBgColor = null;
    public Color treeSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum treeSelectionHAlign = HorizontalAlignmentEnum.LEFT;
    public VerticalAlignmentEnum treeSelectionVAlign = null;
    public Font treeSelectionFont = null;
    public Image treeSelectionImage = null;
    public BorderStyle treeSelectionBorderStyle = null;
    public Character treeSelectionPWEchoChar = null;
    public TextDecorationEnum treeSelectionTextDecoration = null;

    public ICellPainter treeSelectionCellPainter = null;

    public ICellPainter treeStructurePainter = null;
    public ICellPainter treeStructureSelectionPainter = null;

    // summary row style

    public Color summaryRowBgColor = GUIHelper.COLOR_WHITE;
    public Color summaryRowFgColor = GUIHelper.COLOR_BLACK;
    public Color summaryRowGradientBgColor = null;
    public Color summaryRowGradientFgColor = null;
    public HorizontalAlignmentEnum summaryRowHAlign = null;
    public VerticalAlignmentEnum summaryRowVAlign = null;
    public Font summaryRowFont = GUIHelper.getFont(new FontData("Verdana", 8, SWT.BOLD)); //$NON-NLS-1$
    public Image summaryRowImage = null;
    public BorderStyle summaryRowBorderStyle = new BorderStyle(0, GUIHelper.COLOR_BLACK, LineStyleEnum.DOTTED);
    public Character summaryRowPWEchoChar = null;
    public TextDecorationEnum summaryRowTextDecoration = null;

    public ICellPainter summaryRowCellPainter = null;

    public Color summaryRowSelectionBgColor = null;
    public Color summaryRowSelectionFgColor = null;
    public Color summaryRowSelectionGradientBgColor = null;
    public Color summaryRowSelectionGradientFgColor = null;
    public HorizontalAlignmentEnum summaryRowSelectionHAlign = null;
    public VerticalAlignmentEnum summaryRowSelectionVAlign = null;
    public Font summaryRowSelectionFont = null;
    public Image summaryRowSelectionImage = null;
    public BorderStyle summaryRowSelectionBorderStyle = null;
    public Character summaryRowSelectionPWEchoChar = null;
    public TextDecorationEnum summaryRowSelectionTextDecoration = null;

    public ICellPainter summaryRowSelectionCellPainter = null;

    // freeze style
    public Color freezeSeparatorColor = null;

    // grid color
    public Color gridLineColor = null;

    // grid line configuration
    public Boolean renderColumnHeaderGridLines = Boolean.FALSE;
    public Boolean renderCornerGridLines = Boolean.FALSE;
    public Boolean renderRowHeaderGridLines = Boolean.TRUE;
    public Boolean renderBodyGridLines = Boolean.TRUE;
    public Boolean renderFilterRowGridLines = Boolean.TRUE;

    // edit error styles
    public Color conversionErrorBgColor = null;
    public Color conversionErrorFgColor = null;
    public Font conversionErrorFont = null;

    public Color validationErrorBgColor = null;
    public Color validationErrorFgColor = null;
    public Font validationErrorFont = null;

    // fill handle style
    /**
     * @since 1.5
     */
    public Color fillHandleColor = GUIHelper.getColor(0, 125, 10);
    /**
     * @since 1.5
     */
    public BorderStyle fillHandleBorderStyle = new BorderStyle(1, GUIHelper.COLOR_WHITE, LineStyleEnum.SOLID);
    /**
     * @since 1.5
     */
    public BorderStyle fillHandleRegionBorderStyle = new BorderStyle(2, GUIHelper.getColor(0, 125, 10), LineStyleEnum.SOLID, BorderModeEnum.INTERNAL);

    // copy border style
    /**
     * @since 1.5
     */
    public BorderStyle copyBorderStyle = new BorderStyle(1, GUIHelper.COLOR_BLACK, LineStyleEnum.DASHED);

    @Override
    protected IStyle getDefaultCellStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.defaultBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.defaultFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.defaultGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.defaultGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.defaultHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.defaultVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.defaultFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.defaultImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.defaultBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.defaultPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.defaultTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getDefaultCellPainter() {
        return this.defaultCellPainter;
    }

    @Override
    protected IStyle getColumnHeaderStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.cHeaderBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.cHeaderFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.cHeaderGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.cHeaderGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.cHeaderHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.cHeaderVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.cHeaderFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.cHeaderImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.cHeaderBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.cHeaderPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.cHeaderTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getColumnHeaderCellPainter() {
        return this.cHeaderCellPainter;
    }

    @Override
    protected IStyle getRowHeaderStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.rHeaderBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.rHeaderFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.rHeaderGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.rHeaderGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.rHeaderHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.rHeaderVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.rHeaderFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.rHeaderImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.rHeaderBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.rHeaderPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.rHeaderTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getRowHeaderCellPainter() {
        return this.rHeaderCellPainter;
    }

    @Override
    protected IStyle getCornerStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.cornerBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.cornerFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.cornerGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.cornerGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.cornerHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.cornerVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.cornerFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.cornerImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.cornerBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.cornerPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.cornerTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getCornerCellPainter() {
        return this.cornerCellPainter;
    }

    @Override
    protected IStyle getDefaultHoverStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.defaultHoverBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.defaultHoverFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.defaultHoverGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.defaultHoverGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.defaultHoverHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.defaultHoverVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.defaultHoverFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.defaultHoverImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.defaultHoverBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.defaultHoverPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.defaultHoverTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getDefaultHoverCellPainter() {
        return this.defaultHoverCellPainter;
    }

    @Override
    protected IStyle getBodyHoverStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.bodyHoverBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.bodyHoverFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.bodyHoverGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.bodyHoverGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.bodyHoverHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.bodyHoverVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.bodyHoverFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.bodyHoverImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.bodyHoverBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.bodyHoverPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.bodyHoverTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getBodyHoverCellPainter() {
        return this.bodyHoverCellPainter;
    }

    @Override
    protected IStyle getColumnHeaderHoverStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.cHeaderHoverBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.cHeaderHoverFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.cHeaderHoverGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.cHeaderHoverGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.cHeaderHoverHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.cHeaderHoverVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.cHeaderHoverFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.cHeaderHoverImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.cHeaderHoverBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.cHeaderHoverPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.cHeaderHoverTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getColumnHeaderHoverCellPainter() {
        return this.cHeaderHoverCellPainter;
    }

    @Override
    protected IStyle getRowHeaderHoverStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.rHeaderHoverBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.rHeaderHoverFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.rHeaderHoverGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.rHeaderHoverGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.rHeaderHoverHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.rHeaderHoverVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.rHeaderHoverFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.rHeaderHoverImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.rHeaderHoverBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.rHeaderHoverPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.rHeaderHoverTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getRowHeaderHoverCellPainter() {
        return this.rHeaderHoverCellPainter;
    }

    @Override
    protected IStyle getDefaultHoverSelectionStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.defaultHoverSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.defaultHoverSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.defaultHoverSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.defaultHoverSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.defaultHoverSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.defaultHoverSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.defaultHoverSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.defaultHoverSelectionImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.defaultHoverSelectionBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.defaultHoverSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.defaultHoverSelectionTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getDefaultHoverSelectionCellPainter() {
        return this.defaultHoverSelectionCellPainter;
    }

    @Override
    protected IStyle getBodyHoverSelectionStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.bodyHoverSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.bodyHoverSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.bodyHoverSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.bodyHoverSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.bodyHoverSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.bodyHoverSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.bodyHoverSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.bodyHoverSelectionImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.bodyHoverSelectionBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.bodyHoverSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.bodyHoverSelectionTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getBodyHoverSelectionCellPainter() {
        return this.bodyHoverSelectionCellPainter;
    }

    @Override
    protected IStyle getColumnHeaderHoverSelectionStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.cHeaderHoverSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.cHeaderHoverSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.cHeaderHoverSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.cHeaderHoverSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.cHeaderHoverSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.cHeaderHoverSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.cHeaderHoverSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.cHeaderHoverSelectionImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.cHeaderHoverSelectionBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.cHeaderHoverSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.cHeaderHoverSelectionTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getColumnHeaderHoverSelectionCellPainter() {
        return this.cHeaderHoverSelectionCellPainter;
    }

    @Override
    protected IStyle getRowHeaderHoverSelectionStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.rHeaderHoverSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.rHeaderHoverSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.rHeaderHoverSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.rHeaderHoverSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.rHeaderHoverSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.rHeaderHoverSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.rHeaderHoverSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.rHeaderHoverSelectionImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.rHeaderHoverSelectionBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.rHeaderHoverSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.rHeaderHoverSelectionTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getRowHeaderHoverSelectionCellPainter() {
        return this.rHeaderHoverSelectionCellPainter;
    }

    @Override
    protected IStyle getDefaultSelectionCellStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.defaultSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.defaultSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.defaultSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.defaultSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.defaultSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.defaultSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.defaultSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.defaultSelectionImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.defaultSelectionBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.defaultSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.defaultSelectionTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getDefaultSelectionCellPainter() {
        return this.defaultSelectionCellPainter;
    }

    @Override
    protected IStyle getColumnHeaderSelectionStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.cHeaderSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.cHeaderSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.cHeaderSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.cHeaderSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.cHeaderSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.cHeaderSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.cHeaderSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.cHeaderSelectionImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.cHeaderSelectionBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.cHeaderSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.cHeaderSelectionTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getColumnHeaderSelectionCellPainter() {
        return this.cHeaderSelectionCellPainter;
    }

    @Override
    protected IStyle getColumnHeaderFullSelectionStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.cHeaderFullSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.cHeaderFullSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.cHeaderFullSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.cHeaderFullSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.cHeaderFullSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.cHeaderFullSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.cHeaderFullSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.cHeaderFullSelectionImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.cHeaderFullSelectionBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.cHeaderFullSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.cHeaderFullSelectionTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getColumnHeaderFullSelectionCellPainter() {
        return this.cHeaderFullSelectionCellPainter;
    }

    @Override
    protected IStyle getRowHeaderSelectionStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.rHeaderSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.rHeaderSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.rHeaderSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.rHeaderSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.rHeaderSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.rHeaderSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.rHeaderSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.rHeaderSelectionImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.rHeaderSelectionBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.rHeaderSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.rHeaderSelectionTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getRowHeaderSelectionCellPainter() {
        return this.rHeaderSelectionCellPainter;
    }

    @Override
    protected IStyle getRowHeaderFullSelectionStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.rHeaderFullSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.rHeaderFullSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.rHeaderFullSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.rHeaderFullSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.rHeaderFullSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.rHeaderFullSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.rHeaderFullSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.rHeaderFullSelectionImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.rHeaderFullSelectionBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.rHeaderFullSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.rHeaderFullSelectionTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getRowHeaderFullSelectionCellPainter() {
        return this.rHeaderFullSelectionCellPainter;
    }

    @Override
    protected IStyle getCornerSelectionStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.cornerSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.cornerSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.cornerSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.cornerSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.cornerSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.cornerSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.cornerSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.cornerSelectionImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.cornerSelectionBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.cornerSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.cornerSelectionTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getCornerSelectionCellPainter() {
        return this.cornerSelectionCellPainter;
    }

    @Override
    protected IStyle getSelectionAnchorStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.selectionAnchorBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.selectionAnchorFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.selectionAnchorGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.selectionAnchorGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.selectionAnchorHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.selectionAnchorVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.selectionAnchorFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.selectionAnchorImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.selectionAnchorBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.selectionAnchorPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.selectionAnchorTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getSelectionAnchorCellPainter() {
        return this.selectionAnchorCellPainter;
    }

    @Override
    protected IStyle getSelectionAnchorSelectionStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.selectionAnchorSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.selectionAnchorSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.selectionAnchorSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.selectionAnchorSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.selectionAnchorSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.selectionAnchorSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.selectionAnchorSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.selectionAnchorSelectionImage);

        // if there is not explicitly another border style configured use the
        // same as in getSelectionAnchorStyle()
        BorderStyle border = this.selectionAnchorSelectionBorderStyle != null
                ? this.selectionAnchorSelectionBorderStyle
                : this.selectionAnchorBorderStyle;
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                border);

        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.selectionAnchorSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.selectionAnchorSelectionTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getSelectionAnchorSelectionCellPainter() {
        return this.selectionAnchorSelectionCellPainter;
    }

    @Override
    protected IStyle getSelectionAnchorGridLineStyle() {
        Style cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.selectionAnchorGridBorderStyle);
        return cellStyle;
    }

    @Override
    protected IStyle getEvenRowStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.evenRowBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.evenRowFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.evenRowGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.evenRowGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.evenRowHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.evenRowVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.evenRowFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.evenRowImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.evenRowBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.evenRowPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.evenRowTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getEvenRowCellPainter() {
        return this.evenRowCellPainter;
    }

    @Override
    protected IStyle getOddRowStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.oddRowBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.oddRowFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.oddRowGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.oddRowGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.oddRowHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.oddRowVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.oddRowFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.oddRowImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.oddRowBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.oddRowPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.oddRowTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getOddRowCellPainter() {
        return this.oddRowCellPainter;
    }

    @Override
    protected IStyle getColumnGroupHeaderStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.cGroupHeaderBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.cGroupHeaderFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.cGroupHeaderGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.cGroupHeaderGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.cGroupHeaderHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.cGroupHeaderVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.cGroupHeaderFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.cGroupHeaderImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.cGroupHeaderBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.cGroupHeaderPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.cGroupHeaderTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getColumnGroupHeaderCellPainter() {
        return this.cGroupHeaderCellPainter;
    }

    @Override
    protected IStyle getRowGroupHeaderStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.rGroupHeaderBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.rGroupHeaderFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.rGroupHeaderGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.rGroupHeaderGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.rGroupHeaderHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.rGroupHeaderVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.rGroupHeaderFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.rGroupHeaderImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.rGroupHeaderBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.rGroupHeaderPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.rGroupHeaderTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getRowGroupHeaderCellPainter() {
        return this.rGroupHeaderCellPainter;
    }

    @Override
    protected IStyle getSortHeaderStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.sortHeaderBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.sortHeaderFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.sortHeaderGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.sortHeaderGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.sortHeaderHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.sortHeaderVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.sortHeaderFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.sortHeaderImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.sortHeaderBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.sortHeaderPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.sortHeaderTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getSortHeaderCellPainter() {
        return this.sortHeaderCellPainter;
    }

    @Override
    protected IStyle getSelectedSortHeaderStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.selectedSortHeaderBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.selectedSortHeaderFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.selectedSortHeaderGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.selectedSortHeaderGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.selectedSortHeaderHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.selectedSortHeaderVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.selectedSortHeaderFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.selectedSortHeaderImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.selectedSortHeaderBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.selectedSortHeaderPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.selectedSortHeaderTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getSelectedSortHeaderCellPainter() {
        return this.selectedSortHeaderCellPainter;
    }

    @Override
    protected IStyle getFilterRowStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.filterRowBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.filterRowFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.filterRowGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.filterRowGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.filterRowHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.filterRowVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.filterRowFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.filterRowImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.filterRowBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.filterRowPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.filterRowTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getFilterRowCellPainter() {
        return this.filterRowCellPainter;
    }

    @Override
    protected IStyle getTreeStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.treeBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.treeFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.treeGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.treeGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.treeHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.treeVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.treeFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.treeImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.treeBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.treePWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.treeTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getTreeCellPainter() {
        return this.treeCellPainter;
    }

    @Override
    protected IStyle getTreeSelectionStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.treeSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.treeSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.treeSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.treeSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.treeSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.treeSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.treeSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.treeSelectionImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.treeSelectionBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.treeSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.treeSelectionTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getTreeSelectionCellPainter() {
        return this.treeSelectionCellPainter;
    }

    @Override
    protected ICellPainter getTreeStructurePainter() {
        return this.treeStructurePainter;
    }

    @Override
    protected ICellPainter getTreeStructureSelectionPainter() {
        return this.treeStructureSelectionPainter;
    }

    @Override
    protected IStyle getSummaryRowStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.summaryRowBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.summaryRowFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.summaryRowGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.summaryRowGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.summaryRowHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.summaryRowVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.summaryRowFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.summaryRowImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.summaryRowBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.summaryRowPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.summaryRowTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getSummaryRowCellPainter() {
        return this.summaryRowCellPainter;
    }

    @Override
    protected IStyle getSummaryRowSelectionStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.summaryRowSelectionBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.summaryRowSelectionFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_BACKGROUND_COLOR,
                this.summaryRowSelectionGradientBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.GRADIENT_FOREGROUND_COLOR,
                this.summaryRowSelectionGradientFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                this.summaryRowSelectionHAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                this.summaryRowSelectionVAlign);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.summaryRowSelectionFont);
        cellStyle.setAttributeValue(
                CellStyleAttributes.IMAGE,
                this.summaryRowSelectionImage);
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.summaryRowSelectionBorderStyle);
        cellStyle.setAttributeValue(
                CellStyleAttributes.PASSWORD_ECHO_CHAR,
                this.summaryRowSelectionPWEchoChar);
        cellStyle.setAttributeValue(
                CellStyleAttributes.TEXT_DECORATION,
                this.summaryRowSelectionTextDecoration);
        return cellStyle;
    }

    @Override
    protected ICellPainter getSummaryRowSelectionCellPainter() {
        return this.summaryRowSelectionCellPainter;
    }

    @Override
    protected Color getFreezeSeparatorColor() {
        return this.freezeSeparatorColor;
    }

    @Override
    protected Color getGridLineColor() {
        return this.gridLineColor;
    }

    @Override
    protected Boolean getRenderColumnHeaderGridLines() {
        return this.renderColumnHeaderGridLines;
    }

    @Override
    protected Boolean getRenderCornerGridLines() {
        return this.renderCornerGridLines;
    }

    @Override
    protected Boolean getRenderRowHeaderGridLines() {
        return this.renderRowHeaderGridLines;
    }

    @Override
    protected Boolean getRenderBodyGridLines() {
        return this.renderBodyGridLines;
    }

    @Override
    protected Boolean getRenderFilterRowGridLines() {
        return this.renderFilterRowGridLines;
    }

    @Override
    protected IStyle getConversionErrorStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.conversionErrorBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.conversionErrorFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.conversionErrorFont);
        return cellStyle;
    }

    @Override
    protected IStyle getValidationErrorStyle() {
        IStyle cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.validationErrorBgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                this.validationErrorFgColor);
        cellStyle.setAttributeValue(
                CellStyleAttributes.FONT,
                this.validationErrorFont);
        return cellStyle;
    }

    @Override
    protected Color getFillHandleColor() {
        return this.fillHandleColor;
    }

    @Override
    protected BorderStyle getFillHandleBorderStyle() {
        return this.fillHandleBorderStyle;
    }

    @Override
    protected BorderStyle getFillHandleRegionBorderStyle() {
        return this.fillHandleRegionBorderStyle;
    }

    @Override
    protected IStyle getCopyBorderStyle() {
        Style cellStyle = new Style();
        cellStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                this.copyBorderStyle);
        return cellStyle;
    }
}

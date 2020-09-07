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
package org.eclipse.nebula.widgets.nattable.extension.e4.painterfactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.extension.e4.css.NatTableCSSConstants;
import org.eclipse.nebula.widgets.nattable.group.painter.ColumnGroupExpandCollapseImagePainter;
import org.eclipse.nebula.widgets.nattable.group.painter.ColumnGroupHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.group.painter.RowGroupExpandCollapseImagePainter;
import org.eclipse.nebula.widgets.nattable.group.painter.RowGroupHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.AbstractTextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.GradientBackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.PasswordTextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.PercentageBarCellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TableCellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.VerticalTextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CustomLineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.LineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortIconPainter;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.nebula.widgets.nattable.tree.painter.TreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Image;

/**
 * Factory that creates {@link ICellPainter} for a specified key in combination
 * with a configuration map.
 */
public class CellPainterFactory {

    // background painter keys

    public static final String BACKGROUND_PAINTER_KEY = "background";
    public static final String BACKGROUND_IMAGE_PAINTER_KEY = "image-background";
    public static final String GRADIENT_BACKGROUND_PAINTER_KEY = "gradient-background";

    // painter decorator keys

    public static final String DECORATOR_KEY = "decorator";
    public static final String LINE_BORDER_DECORATOR_KEY = "line-border";
    public static final String CUSTOM_LINE_BORDER_DECORATOR_KEY = "custom-line-border";
    public static final String BEVELED_BORDER_DECORATOR_KEY = "beveled-border";
    public static final String PADDING_DECORATOR_KEY = "padding";
    public static final String SORTABLE_HEADER_KEY = "sort-header";
    public static final String COLUMN_GROUP_HEADER_KEY = "column-group";
    public static final String ROW_GROUP_HEADER_KEY = "row-group";
    public static final String TREE_STRUCTURE_KEY = "tree";

    // content painter keys

    public static final String CHECKBOX_PAINTER_KEY = "checkbox";
    public static final String COMBOBOX_PAINTER_KEY = "combobox";
    public static final String IMAGE_PAINTER_KEY = "image";
    public static final String PASSWORD_PAINTER_KEY = "password";
    public static final String PERCENTAGEBAR_PAINTER_KEY = "percentage";
    public static final String TABLE_PAINTER_KEY = "table";
    public static final String TEXT_PAINTER_KEY = "text";

    public static final String NONE = "none";

    private final Map<String, CellPainterWrapperCreator> backgroundPainter = new HashMap<>();
    private final Map<String, CellPainterWrapperCreator> decoratorPainter = new HashMap<>();
    private final Map<String, CellPainterCreator> contentPainter = new HashMap<>();

    /**
     * Singleton instance
     */
    private static CellPainterFactory INSTANCE;

    /**
     * @return The singleton instance of {@link CellPainterFactory}
     */
    public static CellPainterFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CellPainterFactory();
        }
        return INSTANCE;
    }

    /**
     * Private constructor for the singleton pattern
     */
    private CellPainterFactory() {
        // default background painter initializations
        this.backgroundPainter.put(
                BACKGROUND_PAINTER_KEY,
                (painterProperties, underlying) -> {
                    return new BackgroundPainter(underlying);
                });
        this.backgroundPainter.put(
                BACKGROUND_IMAGE_PAINTER_KEY,
                (painterProperties, underlying) -> {
                    Image image = (Image) painterProperties.get(NatTableCSSConstants.CELL_BACKGROUND_IMAGE);
                    return new BackgroundImagePainter(underlying, image);
                });
        this.backgroundPainter.put(
                GRADIENT_BACKGROUND_PAINTER_KEY,
                (painterProperties, underlying) -> {
                    Boolean vertical = (Boolean) painterProperties.get(NatTableCSSConstants.GRADIENT_BACKGROUND_VERTICAL);
                    if (vertical == null) {
                        vertical = false;
                    }
                    return new GradientBackgroundPainter(underlying, vertical);
                });

        // default decorator painter initializations
        this.decoratorPainter.put(
                LINE_BORDER_DECORATOR_KEY,
                (painterProperties, underlying) -> {
                    return new LineBorderDecorator(underlying);
                });
        this.decoratorPainter.put(
                CUSTOM_LINE_BORDER_DECORATOR_KEY,
                (painterProperties, underlying) -> {
                    return new CustomLineBorderDecorator(underlying);
                });
        this.decoratorPainter.put(
                BEVELED_BORDER_DECORATOR_KEY,
                (painterProperties, underlying) -> {
                    return new BeveledBorderDecorator(underlying);
                });

        this.decoratorPainter.put(
                PADDING_DECORATOR_KEY,
                (painterProperties, underlying) -> {
                    Integer topPadding = (Integer) painterProperties.get(NatTableCSSConstants.PADDING_TOP);
                    if (topPadding == null) {
                        topPadding = 0;
                    }
                    Integer bottomPadding = (Integer) painterProperties.get(NatTableCSSConstants.PADDING_BOTTOM);
                    if (bottomPadding == null) {
                        bottomPadding = 0;
                    }
                    Integer leftPadding = (Integer) painterProperties.get(NatTableCSSConstants.PADDING_LEFT);
                    if (leftPadding == null) {
                        leftPadding = 0;
                    }
                    Integer rightPadding = (Integer) painterProperties.get(NatTableCSSConstants.PADDING_RIGHT);
                    if (rightPadding == null) {
                        rightPadding = 0;
                    }
                    return new PaddingDecorator(underlying, topPadding, rightPadding, bottomPadding, leftPadding, false);
                });
        this.decoratorPainter.put(
                SORTABLE_HEADER_KEY,
                (painterProperties, underlying) -> {
                    boolean invert = false;
                    if (painterProperties.containsKey(NatTableCSSConstants.INVERT_ICONS)) {
                        invert = (Boolean) painterProperties.get(NatTableCSSConstants.INVERT_ICONS);
                    }

                    return new SortableHeaderTextPainter(
                            underlying,
                            CellEdgeEnum.RIGHT,
                            new SortIconPainter(false, invert),
                            false,
                            0,
                            false);
                });
        this.decoratorPainter.put(
                COLUMN_GROUP_HEADER_KEY,
                (painterProperties, underlying) -> {
                    boolean invert = false;
                    if (painterProperties.containsKey(NatTableCSSConstants.INVERT_ICONS)) {
                        invert = (Boolean) painterProperties.get(NatTableCSSConstants.INVERT_ICONS);
                    }

                    return new ColumnGroupHeaderTextPainter(
                            underlying,
                            CellEdgeEnum.RIGHT,
                            new ColumnGroupExpandCollapseImagePainter(false, invert),
                            false,
                            0,
                            false);
                });
        this.decoratorPainter.put(
                ROW_GROUP_HEADER_KEY,
                (painterProperties, underlying) -> {
                    boolean invert = false;
                    if (painterProperties.containsKey(NatTableCSSConstants.INVERT_ICONS)) {
                        invert = (Boolean) painterProperties.get(NatTableCSSConstants.INVERT_ICONS);
                    }

                    return new RowGroupHeaderTextPainter(
                            underlying,
                            CellEdgeEnum.BOTTOM,
                            new RowGroupExpandCollapseImagePainter(false, invert),
                            false,
                            0,
                            true);
                });
        this.decoratorPainter.put(
                TREE_STRUCTURE_KEY,
                (painterProperties, underlying) -> {
                    boolean invert = false;
                    if (painterProperties.containsKey(NatTableCSSConstants.INVERT_ICONS)) {
                        invert = (Boolean) painterProperties.get(NatTableCSSConstants.INVERT_ICONS);
                    }

                    String postFix = ""; //$NON-NLS-1$
                    if (invert)
                        postFix = "_inv"; //$NON-NLS-1$

                    TreeImagePainter imagePainter =
                            new TreeImagePainter(
                                    false,
                                    GUIHelper.getImage("right" + postFix),
                                    GUIHelper.getImage("right_down" + postFix),
                                    null);

                    return new IndentedTreeImagePainter(10, null, CellEdgeEnum.LEFT, imagePainter, false, 2, true);
                });

        // default content painter initializations
        this.contentPainter.put(
                TEXT_PAINTER_KEY,
                (painterProperties, underlying) -> {
                    AbstractTextPainter result = null;
                    String textDirection = (String) painterProperties.get(NatTableCSSConstants.TEXT_DIRECTION);
                    if (!"vertical".equalsIgnoreCase(textDirection)) {
                        result = new TextPainter(false, false);
                    } else {
                        result = new VerticalTextPainter(false, false);
                    }
                    initTextPainter(result, painterProperties);
                    return result;
                });
        this.contentPainter.put(
                IMAGE_PAINTER_KEY,
                (painterProperties, underlying) -> {
                    ImagePainter result = new ImagePainter(false);

                    // init
                    if (painterProperties.containsKey(NatTableCSSConstants.CALCULATE_CELL_HEIGHT)) {
                        result.setCalculateByHeight(
                                (Boolean) painterProperties.get(NatTableCSSConstants.CALCULATE_CELL_HEIGHT));
                    }

                    if (painterProperties.containsKey(NatTableCSSConstants.CALCULATE_CELL_WIDTH)) {
                        result.setCalculateByWidth(
                                (Boolean) painterProperties.get(NatTableCSSConstants.CALCULATE_CELL_WIDTH));
                    }
                    return result;
                });
        this.contentPainter.put(
                CHECKBOX_PAINTER_KEY,
                (painterProperties, underlying) -> {
                    boolean invert = false;
                    if (painterProperties.containsKey(NatTableCSSConstants.INVERT_ICONS)) {
                        invert = (Boolean) painterProperties.get(NatTableCSSConstants.INVERT_ICONS);
                    }
                    return new CheckBoxPainter(false, invert);
                });
        this.contentPainter.put(
                COMBOBOX_PAINTER_KEY,
                (painterProperties, underlying) -> {
                    boolean invert = false;
                    if (painterProperties.containsKey(NatTableCSSConstants.INVERT_ICONS)) {
                        invert = (Boolean) painterProperties.get(NatTableCSSConstants.INVERT_ICONS);
                    }
                    return new ComboBoxPainter(invert);
                });
        this.contentPainter.put(
                PASSWORD_PAINTER_KEY,
                (painterProperties, underlying) -> {
                    return new PasswordTextPainter(false, false);
                });
        this.contentPainter.put(
                PERCENTAGEBAR_PAINTER_KEY,
                (painterProperties, underlying) -> {
                    return new PercentageBarCellPainter();
                });
        this.contentPainter.put(
                TABLE_PAINTER_KEY,
                (painterProperties, underlying) -> {
                    TableCellPainter result = new TableCellPainter(new TextPainter(false, false));
                    result.setPaintBg(false);
                    return result;
                });

    }

    /**
     *
     * @param painterValues
     *            The list of all painter representation values in correct
     *            order.
     * @param painterProperties
     *            The properties to set to the painters
     * @return The {@link ICellPainter} construct that should be used for
     *         rendering
     */
    public ICellPainter getCellPainter(List<String> painterValues, Map<String, Object> painterProperties) {
        String backgroundKey = null;
        List<String> decoratorKeys = new ArrayList<>();
        String contentKey = null;

        if (isBackgroundPainterKey(painterValues.get(0))) {
            backgroundKey = painterValues.get(0);
        }

        String last = painterValues.get(painterValues.size() - 1);
        if (isContentPainterKey(last)
                || CellPainterFactory.NONE.equals(last)) {
            contentKey = last;
        }

        int decoratorStart = (backgroundKey != null) ? 1 : 0;
        int decoratorEnd = (contentKey != null) ? painterValues.size() - 1 : painterValues.size();
        decoratorKeys.addAll(painterValues.subList(decoratorStart, decoratorEnd));

        return getCellPainter(
                backgroundKey,
                decoratorKeys,
                contentKey,
                painterProperties);
    }

    /**
     *
     * @param backgroundKey
     *            The key of the background painter to use
     * @param decoratorKeys
     *            The list of keys of decorator painter to use
     * @param contentKey
     *            The key of the content painter to use
     * @param painterProperties
     *            The properties to set to the painters
     * @return The {@link ICellPainter} construct that should be used for
     *         rendering
     */
    public ICellPainter getCellPainter(
            String backgroundKey, List<String> decoratorKeys, String contentKey, Map<String, Object> painterProperties) {

        ICellPainter painter = null;

        // resolve content painter
        ICellPainter contentPainter = null;
        if (isContentPainterKey(contentKey)) {
            contentPainter = getContentPainter(contentKey, painterProperties);
        } else if (!NONE.equalsIgnoreCase(contentKey)) {
            // fallback for unknown content painter key
            contentPainter = getContentPainter(TEXT_PAINTER_KEY, painterProperties);
        }

        // intermediate result = content painter
        painter = contentPainter;

        // resolve decorators
        String decoratorKey = null;
        for (int i = decoratorKeys.size() - 1; i >= 0; i--) {
            decoratorKey = decoratorKeys.get(i);
            if (DECORATOR_KEY.equalsIgnoreCase(decoratorKey)) {
                CellPainterDecorator decorator = new CellPainterDecorator();
                decorator.setPaintBackground(false);
                decorator.setBaseCellPainter(painter);

                Image image = (Image) painterProperties.get(NatTableCSSConstants.DECORATOR_IMAGE);
                CellEdgeEnum edge = (CellEdgeEnum) painterProperties.get(NatTableCSSConstants.DECORATOR_EDGE);
                Integer spacing = (Integer) painterProperties.get(NatTableCSSConstants.DECORATOR_SPACING);
                Boolean paintDecorationDependent = (Boolean) painterProperties.get(NatTableCSSConstants.PAINT_DECORATION_DEPENDENT);

                decorator.setDecoratorCellPainter(new ImagePainter(image, false));
                decorator.setCellEdge(edge);
                decorator.setSpacing(spacing);
                decorator.setPaintDecorationDependent(paintDecorationDependent);

                painter = decorator;
            } else {
                CellPainterWrapper decorator = getDecoratorPainter(decoratorKey, painterProperties, painter);
                if (decorator != null) {
                    painter = decorator;
                }
            }
        }

        // add background painter
        if (backgroundKey != null) {
            CellPainterWrapper bgPainter = getBackgroundPainter(backgroundKey, painterProperties, painter);
            if (bgPainter != null) {
                bgPainter.setWrappedPainter(painter);
                painter = bgPainter;
            }
        }

        return painter;
    }

    /**
     * Create the background painter for the given key and properties.
     *
     * @param key
     *            The background painter key.
     * @param painterProperties
     *            The painter properties for painter initialization.
     * @param underlying
     *            The {@link ICellPainter} that should be applied as wrapped
     *            painter to the created decorator.
     * @return The background painter to use
     */
    public CellPainterWrapper getBackgroundPainter(String key, Map<String, Object> painterProperties, ICellPainter underlying) {
        CellPainterWrapper result = null;

        String lowerKey = key.toLowerCase();
        CellPainterWrapperCreator creator = this.backgroundPainter.get(lowerKey);
        if (creator != null) {
            result = creator.createCellPainterWrapper(painterProperties, underlying);
        }

        return result;
    }

    /**
     * Create the decorator painter for the given key and properties.
     *
     * @param key
     *            The decorator painter key.
     * @param painterProperties
     *            The painter properties for painter initialization.
     * @param underlying
     *            the {@link ICellPainter} that should be applied as wrapped
     *            painter to the created decorator.
     * @return The decorator painter to use
     */
    public CellPainterWrapper getDecoratorPainter(String key, Map<String, Object> painterProperties, ICellPainter underlying) {
        CellPainterWrapper result = null;

        String lowerKey = key.toLowerCase();
        CellPainterWrapperCreator creator = this.decoratorPainter.get(lowerKey);
        if (creator != null) {
            result = creator.createCellPainterWrapper(painterProperties, underlying);
        }

        if (result == null) {
            // a background painter can also be a decorator
            result = getBackgroundPainter(key, painterProperties, underlying);
        }

        return result;
    }

    /**
     * Create the content painter for the given key and properties.
     *
     * @param key
     *            The content painter key.
     * @param painterProperties
     *            The painter properties for painter initialization.
     * @return The content painter to use
     */
    public ICellPainter getContentPainter(String key, Map<String, Object> painterProperties) {
        ICellPainter result = null;

        String lowerKey = key.toLowerCase();
        CellPainterCreator creator = this.contentPainter.get(lowerKey);
        if (creator != null) {
            result = creator.createCellPainter(painterProperties, null);
        }

        return result;
    }

    /**
     * Initialize the given {@link AbstractTextPainter} with the values in the
     * given properties map.
     *
     * @param painter
     *            The {@link AbstractTextPainter} to initialize.
     * @param painterProperties
     *            The painter properties to apply.
     */
    public void initTextPainter(AbstractTextPainter painter, Map<String, Object> painterProperties) {
        boolean wrapWord = false;
        if (painterProperties.containsKey(NatTableCSSConstants.WORD_WRAP)) {
            wrapWord = (Boolean) painterProperties.get(NatTableCSSConstants.WORD_WRAP);
        }
        painter.setWordWrapping(wrapWord);

        boolean wrapText = false;
        if (painterProperties.containsKey(NatTableCSSConstants.TEXT_WRAP)) {
            wrapText = (Boolean) painterProperties.get(NatTableCSSConstants.TEXT_WRAP);
        }
        painter.setWrapText(wrapText);

        boolean trimText = true;
        if (painterProperties.containsKey(NatTableCSSConstants.TEXT_TRIM)) {
            trimText = (Boolean) painterProperties.get(NatTableCSSConstants.TEXT_TRIM);
        }
        painter.setTrimText(trimText);

        String textDirection = (String) painterProperties.get(NatTableCSSConstants.TEXT_DIRECTION);
        boolean vertical = false;
        if ("vertical".equalsIgnoreCase(textDirection)) {
            vertical = true;
        }

        int lineSpacing = 0;
        if (painterProperties.containsKey(NatTableCSSConstants.LINE_SPACING)) {
            lineSpacing = (Integer) painterProperties.get(NatTableCSSConstants.LINE_SPACING);
        }
        painter.setLineSpacing(lineSpacing);

        boolean calculateHeight = false;
        if (painterProperties.containsKey(NatTableCSSConstants.CALCULATE_CELL_HEIGHT)) {
            calculateHeight = (Boolean) painterProperties.get(NatTableCSSConstants.CALCULATE_CELL_HEIGHT);
        }

        boolean calculateWidth = false;
        if (painterProperties.containsKey(NatTableCSSConstants.CALCULATE_CELL_WIDTH)) {
            calculateWidth = (Boolean) painterProperties.get(NatTableCSSConstants.CALCULATE_CELL_WIDTH);
        }

        if (!vertical) {
            painter.setCalculateByTextHeight(calculateHeight);
            painter.setCalculateByTextLength(calculateWidth);
        } else {
            painter.setCalculateByTextLength(calculateHeight);
            painter.setCalculateByTextHeight(calculateWidth);
        }
    }

    /**
     * Check the given key if it represents a background painter.
     *
     * @param key
     *            The key to check.
     * @return <code>true</code> if the given key represents a background
     *         painter, <code>false</code> if not.
     */
    public boolean isBackgroundPainterKey(String key) {
        return key != null && this.backgroundPainter.containsKey(key.toLowerCase());
    }

    /**
     * Check the given key if it represents a decorator painter.
     *
     * @param key
     *            The key to check.
     * @return <code>true</code> if the given key represents a decorator
     *         painter, <code>false</code> if not.
     */
    public boolean isDecoratorPainterKey(String key) {
        return key != null && this.decoratorPainter.containsKey(key.toLowerCase());
    }

    /**
     * Check the given key if it represents a content painter.
     *
     * @param key
     *            The key to check.
     * @return <code>true</code> if the given key represents a content painter,
     *         <code>false</code> if not.
     */
    public boolean isContentPainterKey(String key) {
        return key != null && this.contentPainter.containsKey(key.toLowerCase());
    }

    /**
     * Register a {@link CellPainterWrapperCreator} to create a background
     * painter for a given key. This way custom painters can be registered with
     * the NatTable CSS mechanism.
     *
     * @param key
     *            The key for which the background painter should be created.
     * @param creator
     *            The {@link CellPainterWrapperCreator} that should be
     *            registered for the given key.
     */
    public void registerBackgroundPainter(String key, CellPainterWrapperCreator creator) {
        this.backgroundPainter.put(key, creator);
    }

    /**
     * Registers a {@link CellPainterWrapperCreator} to create a decorator
     * painter for a given key. This way custom painters can be registered with
     * the NatTable CSS mechanism.
     *
     * @param key
     *            The key for which the decorator painter should be created.
     * @param creator
     *            The {@link CellPainterWrapperCreator} that should be
     *            registered for the given key.
     */
    public void registerDecoratorPainter(String key, CellPainterWrapperCreator creator) {
        this.decoratorPainter.put(key, creator);
    }

    /**
     * Registers a {@link CellPainterCreator} to create a content painter for a
     * given key. This way custom painters can be registered with the NatTable
     * CSS mechanism.
     *
     * @param key
     *            The key for which the content painter should be created.
     * @param creator
     *            The {@link CellPainterCreator} that should be registered for
     *            the given key.
     */
    public void registerContentPainter(String key, CellPainterCreator creator) {
        this.contentPainter.put(key, creator);
    }
}

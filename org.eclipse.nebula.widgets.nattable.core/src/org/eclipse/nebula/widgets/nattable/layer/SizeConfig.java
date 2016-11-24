/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Added percentage sizing
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Added scaling
 *     neal zhang <nujiah001@126.com> - change some methods and fields visibility
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;

/**
 * This class stores the size configuration of rows/columns within the NatTable.
 *
 * Mixed mode (fixed/percentage sizing):<br>
 * The mixed mode is only working if percentage sizing is enabled globally, and
 * the fixed sized positions are marked separately.
 */
public class SizeConfig implements IPersistable {

    public static final String PERSISTENCE_KEY_DEFAULT_SIZE = ".defaultSize"; //$NON-NLS-1$
    public static final String PERSISTENCE_KEY_DEFAULT_SIZES = ".defaultSizes"; //$NON-NLS-1$
    public static final String PERSISTENCE_KEY_SIZES = ".sizes"; //$NON-NLS-1$
    public static final String PERSISTENCE_KEY_RESIZABLE_BY_DEFAULT = ".resizableByDefault"; //$NON-NLS-1$
    public static final String PERSISTENCE_KEY_RESIZABLE_INDEXES = ".resizableIndexes"; //$NON-NLS-1$
    public static final String PERSISTENCE_KEY_PERCENTAGE_SIZING = ".percentageSizing"; //$NON-NLS-1$
    public static final String PERSISTENCE_KEY_PERCENTAGE_SIZING_INDEXES = ".percentageSizingIndexes"; //$NON-NLS-1$

    /**
     * The global default size of this {@link SizeConfig}.
     */
    protected int defaultSize;
    /**
     * Map that contains default sizes per column.
     */
    protected final Map<Integer, Integer> defaultSizeMap = new TreeMap<Integer, Integer>();
    /**
     * Map that contains sizes per column.
     */
    protected final Map<Integer, Integer> sizeMap = new TreeMap<Integer, Integer>();
    /**
     * Map that contains the resizable information per row/column.
     */
    protected final Map<Integer, Boolean> resizablesMap = new TreeMap<Integer, Boolean>();
    /**
     * The global resizable information of this {@link SizeConfig}.
     */
    protected boolean resizableByDefault = true;
    /**
     * Map that contains the percentage sizing information per row/column.
     */
    protected final Map<Integer, Boolean> percentageSizingMap = new TreeMap<Integer, Boolean>();
    /**
     * Flag to tell whether the sizing is done for pixel or percentage values.
     */
    protected boolean percentageSizing = false;
    /**
     * The available space needed for percentage calculation on resizing.
     */
    protected int availableSpace = -1;
    /**
     * Map that contains the real pixel size. Will only be used on percentage
     * sizing. This map is not persisted as it will be calculated on resize.
     */
    protected final Map<Integer, Integer> realSizeMap = new TreeMap<Integer, Integer>();
    /**
     * Map that contains the cached aggregated sizes.
     */
    protected final Map<Integer, Integer> aggregatedSizeCacheMap = new HashMap<Integer, Integer>();
    /**
     * Flag that indicates if the aggregated size cache is valid or if it needs
     * to get recalculated.
     */
    protected boolean isAggregatedSizeCacheValid = true;
    /**
     * The {@link IDpiConverter} that is used for scaling DPI conversion.
     */
    protected IDpiConverter dpiConverter;

    /**
     * Create a new {@link SizeConfig} with the given default size.
     *
     * @param defaultSize
     *            The default size to use.
     */
    public SizeConfig(int defaultSize) {
        this.defaultSize = defaultSize;
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        properties.put(prefix + PERSISTENCE_KEY_DEFAULT_SIZE, String.valueOf(this.defaultSize));
        saveMap(this.defaultSizeMap, prefix + PERSISTENCE_KEY_DEFAULT_SIZES, properties);
        saveMap(this.sizeMap, prefix + PERSISTENCE_KEY_SIZES, properties);
        properties.put(prefix + PERSISTENCE_KEY_RESIZABLE_BY_DEFAULT, String.valueOf(this.resizableByDefault));
        saveMap(this.resizablesMap, prefix + PERSISTENCE_KEY_RESIZABLE_INDEXES, properties);
        properties.put(prefix + PERSISTENCE_KEY_PERCENTAGE_SIZING, String.valueOf(this.percentageSizing));
        saveMap(this.percentageSizingMap, prefix + PERSISTENCE_KEY_PERCENTAGE_SIZING_INDEXES, properties);
    }

    private void saveMap(Map<Integer, ?> map, String key, Properties properties) {
        if (map.size() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            for (Integer index : map.keySet()) {
                strBuilder.append(index);
                strBuilder.append(':');
                strBuilder.append(map.get(index));
                strBuilder.append(',');
            }
            properties.setProperty(key, strBuilder.toString());
        }
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        // ensure to cleanup the current states prior loading new ones
        this.defaultSizeMap.clear();
        this.sizeMap.clear();
        this.resizablesMap.clear();
        this.aggregatedSizeCacheMap.clear();

        String persistedDefaultSize = properties.getProperty(prefix + PERSISTENCE_KEY_DEFAULT_SIZE);
        if (persistedDefaultSize != null && persistedDefaultSize.length() > 0) {
            this.defaultSize = Integer.valueOf(persistedDefaultSize).intValue();
        }

        String persistedResizableDefault = properties.getProperty(prefix + PERSISTENCE_KEY_RESIZABLE_BY_DEFAULT);
        if (persistedResizableDefault != null && persistedResizableDefault.length() > 0) {
            this.resizableByDefault = Boolean.valueOf(persistedResizableDefault).booleanValue();
        }

        String persistedPercentageSizing = properties.getProperty(prefix + PERSISTENCE_KEY_PERCENTAGE_SIZING);
        if (persistedPercentageSizing != null && persistedPercentageSizing.length() > 0) {
            setPercentageSizing(Boolean.valueOf(persistedPercentageSizing).booleanValue());
        }

        loadBooleanMap(prefix + PERSISTENCE_KEY_RESIZABLE_INDEXES, properties, this.resizablesMap);
        loadIntegerMap(prefix + PERSISTENCE_KEY_DEFAULT_SIZES, properties, this.defaultSizeMap);
        loadIntegerMap(prefix + PERSISTENCE_KEY_SIZES, properties, this.sizeMap);
        loadBooleanMap(prefix + PERSISTENCE_KEY_PERCENTAGE_SIZING_INDEXES, properties, this.percentageSizingMap);
    }

    private void loadIntegerMap(String key, Properties properties, Map<Integer, Integer> map) {
        String property = properties.getProperty(key);
        if (property != null) {
            map.clear();

            StringTokenizer tok = new StringTokenizer(property, ","); //$NON-NLS-1$
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                int separatorIndex = token.indexOf(':');
                map.put(Integer.valueOf(token.substring(0, separatorIndex)),
                        Integer.valueOf(token.substring(separatorIndex + 1)));
            }
        }
    }

    private void loadBooleanMap(String key, Properties properties, Map<Integer, Boolean> map) {
        String property = properties.getProperty(key);
        if (property != null) {
            StringTokenizer tok = new StringTokenizer(property, ","); //$NON-NLS-1$
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                int separatorIndex = token.indexOf(':');
                map.put(Integer.valueOf(token.substring(0, separatorIndex)),
                        Boolean.valueOf(token.substring(separatorIndex + 1)));
            }
        }
    }

    // Default size

    /**
     * Set the default size that should be used in case there is no position
     * based size configured.
     *
     * @param size
     *            The default size to set.
     */
    public void setDefaultSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size < 0"); //$NON-NLS-1$
        }
        this.defaultSize = size;
        this.isAggregatedSizeCacheValid = false;
    }

    /**
     * @return The default size that is used in case there is no position based
     *         size configured.
     */
    public int getDefaultSize() {
        return upScale(this.defaultSize);
    }

    public void setDefaultSize(int position, int size) {
        if (this.defaultSize < 0) {
            throw new IllegalArgumentException("size < 0"); //$NON-NLS-1$
        }
        this.defaultSizeMap.put(position, size);
        this.isAggregatedSizeCacheValid = false;
    }

    private int getDefaultSize(int position) {
        Integer size = this.defaultSizeMap.get(position);
        if (size != null) {
            return size.intValue();
        } else {
            return this.defaultSize;
        }
    }

    // Size

    public int getAggregateSize(int position) {
        if (position < 0) {
            return -1;
        } else if (position == 0) {
            return 0;
        } else if (isAllPositionsSameSize() && !isPercentageSizing()) {
            // if percentage sizing is used, the sizes in defaultSize are used
            // as percentage values and not as pixel values, therefore another
            // value needs to be considered
            return position * upScale(this.defaultSize);
        } else {
            // See if the cache is valid, if not clear it.
            if (!this.isAggregatedSizeCacheValid) {
                this.aggregatedSizeCacheMap.clear();
                this.isAggregatedSizeCacheValid = true;
            }

            if (!this.aggregatedSizeCacheMap.containsKey(position)) {
                int aggregatedSize = calculateAggregatedSize(position);
                this.aggregatedSizeCacheMap.put(position, aggregatedSize);
            }
            return this.aggregatedSizeCacheMap.get(position);
        }
    }

    public int getSize(int position) {
        Integer size = null;
        if (isPercentageSizing()) {
            size = this.realSizeMap.get(position);
        } else {
            if (this.sizeMap.containsKey(position)) {
                size = this.sizeMap.get(position);
            }
        }
        if (size != null) {
            return upScale(size.intValue());
        } else {
            return upScale(getDefaultSize(position));
        }
    }

    /**
     * Sets the given size for the given position. This method can be called
     * manually for configuration via {@link DataLayer} and will be called on
     * resizing within the rendered UI. This is why there is a check for
     * percentage configuration. If this {@link SizeConfig} is configured to not
     * use percentage sizing, the size is taken as is. If percentage sizing is
     * enabled, the given size will be calculated to percentage value based on
     * the already known pixel values.
     * <p>
     * If you want to use percentage sizing you should use
     * {@link SizeConfig#setPercentage(int, int)} for manual size configuration
     * to avoid unnecessary calculations.
     *
     * @param position
     *            The position for which the size should be set.
     * @param size
     *            The size in pixels to set for the given position.
     */
    public void setSize(int position, int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size < 0"); //$NON-NLS-1$
        }
        if (isPositionResizable(position)) {
            // check whether the given value should be remembered as is or if it
            // needs to be calculated
            if (!isPercentageSizing(position)) {
                this.sizeMap.put(position, size);
            } else {
                if (this.availableSpace > 0) {
                    Double percentage = ((double) size * 100) / this.availableSpace;

                    Integer oldValue = this.sizeMap.get(position);
                    int diff = percentage.intValue();
                    if (oldValue != null) {
                        diff = diff - oldValue;
                    } else {
                        // there was no percentage value before
                        // we need to calculate the before value out of the
                        // realSizeMap otherwise the resizing effect would
                        // have strange effects
                        if (this.realSizeMap.containsKey(position)) {
                            Double calculated = ((double) this.realSizeMap.get(position) * 100) / this.availableSpace;
                            diff = diff - calculated.intValue();
                        }

                    }

                    this.sizeMap.put(position, percentage.intValue());

                    // check the adjacent positions for percentage corrections
                    int nextPosition = position + 1;
                    while (diff != 0 && this.realSizeMap.containsKey(nextPosition)) {
                        diff = updateAdjacentPosition(nextPosition, diff);
                        nextPosition++;
                    }

                    int previousPosition = position - 1;
                    while (diff != 0
                            && this.realSizeMap.containsKey(previousPosition)) {
                        diff = updateAdjacentPosition(previousPosition, diff);
                        previousPosition--;
                    }

                    if (diff != 0 && oldValue == null) {
                        // if the diff is not 0 and there was no size value set
                        // before we will remove the prior set value again
                        // this is because the position was configured as the
                        // only percentage sizing position with no specified
                        // value, which technically means that it should always
                        // take the remaining space
                        this.sizeMap.remove(position);
                    }
                }
            }

            calculatePercentages(this.availableSpace, this.realSizeMap.size());
            this.isAggregatedSizeCacheValid = false;
        }
    }

    /**
     * This method is used for resizing in percentage mode. If a position that
     * is configured for percentage sizing is resized, the size diff needs to be
     * added/removed from adjacent cells, to ensure consistent size calculation.
     *
     * @param position
     *            The position to update.
     * @param diff
     *            The diff that should be applied to the position
     * @return The remaining diff or 0 if the diff could be completely applied
     *         to the position.
     */
    private int updateAdjacentPosition(int position, int diff) {
        if (this.sizeMap.containsKey(position) || this.realSizeMap.containsKey(position)) {
            if (isPercentageSizing(position)) {
                if (this.sizeMap.containsKey(position)) {
                    // there is a follow-up position that is configured for
                    // percentage sizing
                    // and there is value specified for that position
                    int currentValue = this.sizeMap.get(position);
                    if (diff < currentValue) {
                        this.sizeMap.put(position, currentValue - diff);
                        return 0;
                    } else {
                        diff = diff - (currentValue + 1);
                        // never accept a percentage value < 1 as then the
                        // position would disappear
                        this.sizeMap.put(position, 1);
                    }
                }

                return 0;
            }
        }

        return diff;
    }

    /**
     * Will set the given percentage size information for the given position and
     * will set the given position to be sized via percentage value.
     *
     * @param position
     *            The positions whose percentage sizing information should be
     *            set.
     * @param percentage
     *            The percentage value to set, always dependent on the available
     *            space for percentage sizing, which can be less than the real
     *            available space in case there are also positions that are
     *            configured for fixed size.
     */
    public void setPercentage(int position, int percentage) {
        if (percentage < 0) {
            throw new IllegalArgumentException("percentage < 0"); //$NON-NLS-1$
        }
        if (isPositionResizable(position)) {
            this.percentageSizingMap.put(position, Boolean.TRUE);
            this.sizeMap.put(position, percentage);
            this.realSizeMap.put(position, calculatePercentageValue(percentage, this.availableSpace));
            calculatePercentages(this.availableSpace, this.realSizeMap.size());
        }
    }

    // Resizable

    /**
     * @return The global resizable information of this {@link SizeConfig}.
     */
    public boolean isResizableByDefault() {
        return this.resizableByDefault;
    }

    /**
     * Checks if there is a special resizable configuration for the given
     * position. If not the global resizable information is returned.
     *
     * @param position
     *            The position of the row/column for which the resizable
     *            information is requested.
     * @return <code>true</code> if the given row/column position is resizable,
     *         <code>false</code> if not.
     */
    public boolean isPositionResizable(int position) {
        Boolean resizable = this.resizablesMap.get(position);
        if (resizable != null) {
            return resizable.booleanValue();
        }
        return this.resizableByDefault;
    }

    /**
     * Sets the resizable configuration for the given row/column position.
     *
     * @param position
     *            The position of the row/column for which the resizable
     *            configuration should be set.
     * @param resizable
     *            <code>true</code> if the given row/column position should be
     *            resizable, <code>false</code> if not.
     */
    public void setPositionResizable(int position, boolean resizable) {
        this.resizablesMap.put(position, resizable);
    }

    /**
     * Sets the global resizable configuration. Will reset all special resizable
     * configurations.
     *
     * @param resizableByDefault
     *            <code>true</code> if all rows/columns should be resizable,
     *            <code>false</code> if no row/column should be resizable.
     */
    public void setResizableByDefault(boolean resizableByDefault) {
        this.resizablesMap.clear();
        this.resizableByDefault = resizableByDefault;
    }

    // All positions same size

    public boolean isAllPositionsSameSize() {
        return this.defaultSizeMap.size() == 0 && this.sizeMap.size() == 0;
    }

    /**
     * @return <code>true</code> if the size of at least one position is
     *         interpreted in percentage, <code>false</code> if the size of all
     *         positions is interpreted by pixel.
     */
    public boolean isPercentageSizing() {
        if (!this.percentageSizingMap.isEmpty()) {
            for (Boolean pSize : this.percentageSizingMap.values()) {
                if (pSize)
                    return true;
            }
        }
        return this.percentageSizing;
    }

    /**
     * @param percentageSizing
     *            <code>true</code> if the size of the positions should be
     *            interpreted percentaged, <code>false</code> if the size of the
     *            positions should be interpreted by pixel.
     */
    public void setPercentageSizing(boolean percentageSizing) {
        this.percentageSizing = percentageSizing;
        this.isAggregatedSizeCacheValid = false;
    }

    /**
     * Checks if there is a special percentage sizing configuration for the
     * given position. If not the global percentage sizing information is
     * returned.
     *
     * @param position
     *            The position of the row/column for which the percentage sizing
     *            information is requested.
     * @return <code>true</code> if the given row/column position is sized by
     *         percentage value, <code>false</code> if not.
     */
    public boolean isPercentageSizing(int position) {
        Boolean percentageSizing = this.percentageSizingMap.get(position);
        if (percentageSizing != null) {
            return percentageSizing;
        }
        return this.percentageSizing;
    }

    /**
     * Sets the percentage sizing configuration for the given row/column
     * position.
     *
     * @param position
     *            The position of the row/column for which the percentage sizing
     *            configuration should be set.
     * @param percentageSizing
     *            <code>true</code> if the given row/column position should be
     *            interpreted in percentage, <code>false</code> if not.
     */
    public void setPercentageSizing(int position, boolean percentageSizing) {
        this.percentageSizingMap.put(position, percentageSizing);
        this.isAggregatedSizeCacheValid = false;
    }

    /**
     * Will calculate the real pixel values for the positions if percentage
     * sizing is enabled.
     *
     * @param space
     *            The space that is available for rendering.
     * @param positionCount
     *            The number of positions that should be handled by this
     *            {@link SizeConfig}
     */
    public void calculatePercentages(int space, int positionCount) {
        if (isPercentageSizing()) {
            this.isAggregatedSizeCacheValid = false;
            this.availableSpace = space;

            int percentageSpace = calculateAvailableSpace(space);

            int sum = 0;
            int real = 0;
            int realSum = 0;
            int fixedSum = 0;
            List<Integer> noInfoPositions = new ArrayList<Integer>();
            Integer positionValue = null;
            for (int i = 0; i < positionCount; i++) {
                positionValue = this.sizeMap.get(i);
                if (positionValue != null) {
                    if (isPercentageSizing(i)) {
                        sum += positionValue;
                        real = calculatePercentageValue(positionValue,
                                percentageSpace);
                    } else {
                        real = positionValue;
                        fixedSum += real;
                    }
                    realSum += real;
                    this.realSizeMap.put(i, real);
                } else {
                    // remember the position for which no size information
                    // exists needed to calculate the size for those positions
                    // dependent on the remaining space
                    noInfoPositions.add(i);
                }
            }

            int[] correction = correctPercentageValues(sum, positionCount);
            if (correction != null) {
                sum = correction[0];
                realSum = correction[1] + fixedSum;
            }

            if (!noInfoPositions.isEmpty()) {
                // now calculate the size for the remaining columns
                double remaining = new Double(space) - realSum;
                Double remainingColSpace = remaining / noInfoPositions.size();
                for (Integer position : noInfoPositions) {
                    sum += (remainingColSpace / space) * 100;
                    this.realSizeMap.put(position, remainingColSpace.intValue());
                }
                // If there are positions for which no size information exist,
                // the size config will use 100 percent of the available space
                // on percentage sizing. To handle rounding issues just set the
                // sum to 100 for correct calculation results.
                sum = 100;
            }
            if (sum == 100) {
                // check if the sum of the calculated values is the same as the
                // given space if not distribute the missing pixels to some of
                // the other columns this is needed because of rounding issues
                // on 100% with odd-numbered pixel values
                int valueSum = 0;
                int lastPos = -1;
                for (Map.Entry<Integer, Integer> entry : this.realSizeMap.entrySet()) {
                    valueSum += entry.getValue();
                    lastPos = Math.max(lastPos, entry.getKey());
                }

                if (space > 0 && valueSum < space) {
                    // distribute the missing pixels
                    int missingPixels = (space - valueSum);
                    int pos = 0;
                    for (int i = missingPixels; i > 0; i--) {
                        if (!this.realSizeMap.containsKey(pos)) {
                            // there are more missing pixels than columns
                            // start over at position 0
                            pos = 0;
                        }
                        int posValue = this.realSizeMap.get(pos);
                        this.realSizeMap.put(pos, posValue + 1);
                        pos++;
                    }
                }
            }
        }
    }

    /**
     * @param percentage
     *            The percentage value.
     * @param space
     *            The available space
     * @return The percentage value of the given space.
     */
    private int calculatePercentageValue(int percentage, int space) {
        double factor = (double) percentage / 100;
        return new Double(space * factor).intValue();
    }

    /**
     * Calculates the available space for percentage size calculation. This is
     * necessary to support mixed mode of sizing, e.g. if two columns are
     * configured to have fixed size of 50 pixels and one column that should
     * take the rest of the available space of 500 pixels, the available space
     * for percentage sizing is 400 pixels.
     *
     * @param space
     *            The whole available space for rendering.
     * @return The available space for percentage sizing. Might be negative if
     *         the width of all fixed sized positions is greater than the
     *         available space.
     */
    protected int calculateAvailableSpace(int space) {
        if (!this.percentageSizingMap.isEmpty()) {
            if (this.percentageSizing) {
                for (Map.Entry<Integer, Boolean> entry : this.percentageSizingMap
                        .entrySet()) {
                    if (!entry.getValue()) {
                        if (this.sizeMap.containsKey(entry.getKey()))
                            space -= this.sizeMap.get(entry.getKey());
                    }
                }
            }
        }
        return space;
    }

    /**
     * This method is used to correct the calculated percentage values in case a
     * user configured more than 100 percent. In that case the set percentage
     * values are scaled down to not exceed 100 percent.
     *
     * @param sum
     *            The sum of all configured percentage sized positions.
     * @param positionCount
     *            The number of positions to check.
     * @return Integer array with the sum value at first position and the new
     *         calculated real pixel sum at second position in case a
     *         corrections took place. Will return <code>null</code> in case no
     *         correction happened.
     */
    protected int[] correctPercentageValues(int sum, int positionCount) {
        Map<Integer, Integer> toModify = new TreeMap<Integer, Integer>();
        for (int i = 0; i < positionCount; i++) {
            Integer positionValue = this.sizeMap.get(i);
            if (positionValue != null && isPercentageSizing(i)) {
                toModify.put(i, this.realSizeMap.get(i));
            }
        }

        // if the sum is greater than 100 we need to normalize the percentage
        // values
        if (sum > 100) {
            // calculate the factor which needs to be used to normalize the
            // values
            double factor = Double.valueOf(100) / Double.valueOf(sum);

            // update the percentage size values by the calculated factor
            int realSum = 0;
            for (Map.Entry<Integer, Integer> mod : toModify.entrySet()) {
                int oldValue = mod.getValue();
                int newValue = Double.valueOf(oldValue * factor).intValue();
                realSum += newValue;
                this.realSizeMap.put(mod.getKey(), newValue);
            }

            return new int[] { 100, realSum };
        }

        // the given sum is not greater than 100 so we do not have to modify
        // anything
        return null;
    }

    private int calculateAggregatedSize(int position) {
        int resizeAggregate = 0;
        int resizedColumns = 0;

        Map<Integer, Integer> mapToUse = isPercentageSizing() ? this.realSizeMap : this.sizeMap;

        for (Integer resizedPosition : mapToUse.keySet()) {
            if (resizedPosition.intValue() < position) {
                resizedColumns++;
                int size = mapToUse.get(resizedPosition);
                resizeAggregate += upScale(size);
            } else {
                break;
            }
        }

        // also take into account the default size configuration per position
        for (Integer defaultPosition : this.defaultSizeMap.keySet()) {
            if (defaultPosition.intValue() < position) {
                if (!mapToUse.containsKey(defaultPosition)) {
                    resizedColumns++;
                    int size = this.defaultSizeMap.get(defaultPosition);
                    resizeAggregate += upScale(size);
                }
            } else {
                break;
            }
        }

        int result = (position - resizedColumns) * upScale(this.defaultSize);
        result += resizeAggregate;
        return result;
    }

    /**
     * Recalculate the percentage values for the given amount of columns. Needed
     * for structural changes that aren't caused by a client are resize, e.g.
     * adding a column.
     *
     * @param positionCount
     *            The number of columns that should be used to calculate the
     *            percentage values.
     */
    public void updatePercentageValues(int positionCount) {
        calculatePercentages(this.availableSpace, positionCount);
    }

    /**
     * Calculates the size value dependent on a possible configured scaling from
     * pixel to DPI value.
     *
     * @param value
     *            The value that should be up scaled.
     * @return The scaled value if a {@link IDpiConverter} is configured, the
     *         value itself if no {@link IDpiConverter} is set.
     *
     * @see IDpiConverter#convertPixelToDpi(int)
     */
    protected int upScale(int value) {
        if (this.dpiConverter == null) {
            return value;
        }
        return this.dpiConverter.convertPixelToDpi(value);
    }

    /**
     * Calculates the size value dependent on a possible configured scaling from
     * DPI to pixel value.
     *
     * <p>
     * This method is used for percentage sizing calculations.
     * </p>
     *
     * @param value
     *            The value that should be down scaled.
     * @return The scaled value if a {@link IDpiConverter} is configured, the
     *         value itself if no {@link IDpiConverter} is set.
     */
    protected int downScale(int value) {
        if (this.dpiConverter == null) {
            return value;
        }
        return this.dpiConverter.convertDpiToPixel(value);
    }

    /**
     *
     * @param dpiConverter
     *            The {@link IDpiConverter} to use for size scaling.
     */
    public void setDpiConverter(IDpiConverter dpiConverter) {
        this.dpiConverter = dpiConverter;
    }
}

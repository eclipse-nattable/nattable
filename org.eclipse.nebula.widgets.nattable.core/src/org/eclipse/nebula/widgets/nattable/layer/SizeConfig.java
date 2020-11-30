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
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Added percentage sizing
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Added scaling
 *     neal zhang <nujiah001@126.com> - change some methods and fields visibility
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.MutableIntBooleanMap;
import org.eclipse.collections.api.map.primitive.MutableIntDoubleMap;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.tuple.primitive.IntBooleanPair;
import org.eclipse.collections.api.tuple.primitive.IntDoublePair;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.factory.primitive.IntBooleanMaps;
import org.eclipse.collections.impl.factory.primitive.IntDoubleMaps;
import org.eclipse.collections.impl.factory.primitive.IntIntMaps;
import org.eclipse.collections.impl.factory.primitive.IntLists;
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
     * Persistence key for percentage size configuration map.
     *
     * @since 1.6
     */
    public static final String PERSISTENCE_KEY_PERCENTAGE_SIZES = ".percentageSizes"; //$NON-NLS-1$
    /**
     * Persistence key for distributeRemainingSpace property.
     *
     * @since 1.6
     */
    public static final String PERSISTENCE_KEY_DISTRIBUTE_REMAINING_SPACE = ".distributeRemainingSpace"; //$NON-NLS-1$
    /**
     * Persistence key for default min size property.
     *
     * @since 1.6
     */
    public static final String PERSISTENCE_KEY_DEFAULT_MIN_SIZE = ".defaultMinSize"; //$NON-NLS-1$
    /**
     * Persistence key for min size configuration map.
     *
     * @since 1.6
     */
    public static final String PERSISTENCE_KEY_MIN_SIZES = ".minSizes"; //$NON-NLS-1$

    /**
     * The global default size of this {@link SizeConfig}.
     */
    protected int defaultSize;

    /**
     * Map that contains default sizes per column.
     */
    protected final MutableIntIntMap defaultSizeMap = IntIntMaps.mutable.empty();
    /**
     * Map that contains sizes per column.
     */
    protected final MutableIntIntMap sizeMap = IntIntMaps.mutable.empty();
    /**
     * Map that contains the resizable information per row/column.
     */
    protected final MutableIntBooleanMap resizablesMap = IntBooleanMaps.mutable.empty();
    /**
     * The global resizable information of this {@link SizeConfig}.
     */
    protected boolean resizableByDefault = true;
    /**
     * Map that contains percentage values per position for percentage sizing.
     *
     * @since 1.6
     */
    protected final MutableIntDoubleMap percentageSizeMap = IntDoubleMaps.mutable.empty();
    /**
     * Map that contains the percentage sizing information per row/column.
     */
    protected final MutableIntBooleanMap percentageSizingMap = IntBooleanMaps.mutable.empty();
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
    protected final MutableIntIntMap realSizeMap = IntIntMaps.mutable.empty();
    /**
     * Map that contains the cached aggregated sizes.
     */
    protected final MutableIntIntMap aggregatedSizeCacheMap = IntIntMaps.mutable.empty();
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
     * Flag to configure percentage sizing behavior in case the percentage
     * configuration is not 100% and there is space left. Setting this flag to
     * <code>true</code> will cause distribution of the remaining space to the
     * fixed percentage columns according to their ratio. This means the
     * available space will always be fully taken. Setting it to
     * <code>false</code> will not increase, so there will be space left.
     *
     * @since 1.6
     */
    private boolean distributeRemainingSpace = true;
    /**
     * The default minimum size in pixels. Will be used on percentage sizing to
     * avoid shrinking of columns/rows below a configured minimum.
     *
     * @since 1.6
     */
    private int defaultMinSize = 0;
    /**
     * Map that contains the minimum size in pixels. Will be used on percentage
     * sizing to avoid shrinking of columns/rows below a configured minimum.
     *
     * @since 1.6
     */
    private final MutableIntIntMap minSizeMap = IntIntMaps.mutable.empty();
    /**
     * Flag to configure whether dynamic percentage sized positions should be
     * fixed on any resize or not. This means, if positions are configured for
     * percentage sizing without a specific percentage value, the size is
     * calculated based on the space that is still available. If this flag is
     * set to <code>false</code> only the position that is resized will get a
     * fixed value. The other positions will still be dynamic and therefore will
     * also resize as the available space is changed. Setting this flag to
     * <code>true</code> will cause that all positions with dynamic percentage
     * configuration will get a fixed percentage value to have a deterministic
     * resize behavior for the user that triggers the resize. Also percentage
     * sized positions with a minimum size, where the minimum is bigger than the
     * calculated percentage value will be recalculated to set the percentage
     * value that matches the current state. Default is <code>true</code>.
     *
     * @since 1.6
     */
    private boolean fixPercentageValuesOnResize = true;

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

        StringBuilder builder = new StringBuilder();
        if (!this.defaultSizeMap.isEmpty()) {
            for (IntIntPair pair : this.defaultSizeMap.keyValuesView().toSortedList()) {
                builder.append(pair.getOne()).append(":").append(pair.getTwo()).append(","); //$NON-NLS-1$//$NON-NLS-2$
            }
            properties.setProperty(
                    prefix + PERSISTENCE_KEY_DEFAULT_SIZES,
                    builder.toString());
        }

        if (!this.sizeMap.isEmpty()) {
            builder = new StringBuilder();
            for (IntIntPair pair : this.sizeMap.keyValuesView().toSortedList()) {
                builder.append(pair.getOne()).append(":").append(pair.getTwo()).append(","); //$NON-NLS-1$//$NON-NLS-2$
            }
            properties.setProperty(
                    prefix + PERSISTENCE_KEY_SIZES,
                    builder.toString());

        }

        properties.put(prefix + PERSISTENCE_KEY_RESIZABLE_BY_DEFAULT, String.valueOf(this.resizableByDefault));

        if (!this.resizablesMap.isEmpty()) {
            builder = new StringBuilder();
            for (IntBooleanPair pair : this.resizablesMap.keyValuesView().toSortedList()) {
                builder.append(pair.getOne()).append(":").append(pair.getTwo()).append(","); //$NON-NLS-1$//$NON-NLS-2$
            }
            properties.setProperty(
                    prefix + PERSISTENCE_KEY_RESIZABLE_INDEXES,
                    builder.toString());

        }

        properties.put(prefix + PERSISTENCE_KEY_PERCENTAGE_SIZING, String.valueOf(this.percentageSizing));

        if (!this.percentageSizeMap.isEmpty()) {
            builder = new StringBuilder();
            for (IntDoublePair pair : this.percentageSizeMap.keyValuesView().toSortedList()) {
                builder.append(pair.getOne()).append(":").append(pair.getTwo()).append(","); //$NON-NLS-1$//$NON-NLS-2$
            }
            properties.setProperty(
                    prefix + PERSISTENCE_KEY_PERCENTAGE_SIZES,
                    builder.toString());
        }

        if (!this.percentageSizingMap.isEmpty()) {
            builder = new StringBuilder();
            for (IntBooleanPair pair : this.percentageSizingMap.keyValuesView().toSortedList()) {
                builder.append(pair.getOne()).append(":").append(pair.getTwo()).append(","); //$NON-NLS-1$//$NON-NLS-2$
            }
            properties.setProperty(
                    prefix + PERSISTENCE_KEY_PERCENTAGE_SIZING_INDEXES,
                    builder.toString());
        }

        properties.put(prefix + PERSISTENCE_KEY_DISTRIBUTE_REMAINING_SPACE, String.valueOf(this.distributeRemainingSpace));
        properties.put(prefix + PERSISTENCE_KEY_DEFAULT_MIN_SIZE, String.valueOf(this.defaultMinSize));

        if (!this.minSizeMap.isEmpty()) {
            builder = new StringBuilder();
            for (IntIntPair pair : this.minSizeMap.keyValuesView().toSortedList()) {
                builder.append(pair.getOne()).append(":").append(pair.getTwo()).append(","); //$NON-NLS-1$//$NON-NLS-2$
            }
            properties.setProperty(
                    prefix + PERSISTENCE_KEY_MIN_SIZES,
                    builder.toString());
        }

    }

    @Override
    public void loadState(String prefix, Properties properties) {
        // ensure to cleanup the current states prior loading new ones
        this.defaultSizeMap.clear();
        this.sizeMap.clear();
        this.percentageSizeMap.clear();
        this.percentageSizingMap.clear();
        this.resizablesMap.clear();
        this.aggregatedSizeCacheMap.clear();
        this.minSizeMap.clear();

        this.resizableByDefault = true;
        this.percentageSizing = false;
        this.distributeRemainingSpace = false;
        this.isAggregatedSizeCacheValid = false;
        this.defaultMinSize = 0;

        String persistedDefaultSize = properties.getProperty(prefix + PERSISTENCE_KEY_DEFAULT_SIZE);
        if (persistedDefaultSize != null && persistedDefaultSize.length() > 0) {
            this.defaultSize = Integer.parseInt(persistedDefaultSize);
        }

        String persistedResizableDefault = properties.getProperty(prefix + PERSISTENCE_KEY_RESIZABLE_BY_DEFAULT);
        if (persistedResizableDefault != null && persistedResizableDefault.length() > 0) {
            this.resizableByDefault = Boolean.parseBoolean(persistedResizableDefault);
        }

        String persistedPercentageSizing = properties.getProperty(prefix + PERSISTENCE_KEY_PERCENTAGE_SIZING);
        if (persistedPercentageSizing != null && persistedPercentageSizing.length() > 0) {
            this.percentageSizing = Boolean.parseBoolean(persistedPercentageSizing);
        }

        String persistedDistributeRemainingSpace = properties.getProperty(prefix + PERSISTENCE_KEY_DISTRIBUTE_REMAINING_SPACE);
        if (persistedDistributeRemainingSpace != null && persistedDistributeRemainingSpace.length() > 0) {
            this.distributeRemainingSpace = Boolean.parseBoolean(persistedDistributeRemainingSpace);
        }

        String persistedDefaultMinSize = properties.getProperty(prefix + PERSISTENCE_KEY_DEFAULT_MIN_SIZE);
        if (persistedDefaultMinSize != null && persistedDefaultMinSize.length() > 0) {
            this.defaultMinSize = Integer.parseInt(persistedDefaultMinSize);
        }

        loadBooleanMap(prefix + PERSISTENCE_KEY_RESIZABLE_INDEXES, properties, this.resizablesMap);
        loadIntegerMap(prefix + PERSISTENCE_KEY_DEFAULT_SIZES, properties, this.defaultSizeMap);
        loadIntegerMap(prefix + PERSISTENCE_KEY_SIZES, properties, this.sizeMap);
        loadDoubleMap(prefix + PERSISTENCE_KEY_PERCENTAGE_SIZES, properties, this.percentageSizeMap);
        loadBooleanMap(prefix + PERSISTENCE_KEY_PERCENTAGE_SIZING_INDEXES, properties, this.percentageSizingMap);
        loadIntegerMap(prefix + PERSISTENCE_KEY_MIN_SIZES, properties, this.minSizeMap);

        // trigger percentage re-calculation
        calculatePercentages(this.availableSpace, this.realSizeMap.size());
    }

    private void loadIntegerMap(String key, Properties properties, MutableIntIntMap map) {
        String property = properties.getProperty(key);
        if (property != null) {
            map.clear();

            StringTokenizer tok = new StringTokenizer(property, ","); //$NON-NLS-1$
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                int separatorIndex = token.indexOf(':');
                map.put(Integer.parseInt(token.substring(0, separatorIndex)),
                        Integer.parseInt(token.substring(separatorIndex + 1)));
            }
        }
    }

    private void loadBooleanMap(String key, Properties properties, MutableIntBooleanMap map) {
        String property = properties.getProperty(key);
        if (property != null) {
            map.clear();

            StringTokenizer tok = new StringTokenizer(property, ","); //$NON-NLS-1$
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                int separatorIndex = token.indexOf(':');
                map.put(Integer.parseInt(token.substring(0, separatorIndex)),
                        Boolean.parseBoolean(token.substring(separatorIndex + 1)));
            }
        }
    }

    private void loadDoubleMap(String key, Properties properties, MutableIntDoubleMap map) {
        String property = properties.getProperty(key);
        if (property != null) {
            map.clear();

            StringTokenizer tok = new StringTokenizer(property, ","); //$NON-NLS-1$
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                int separatorIndex = token.indexOf(':');
                map.put(Integer.parseInt(token.substring(0, separatorIndex)),
                        Double.parseDouble(token.substring(separatorIndex + 1)));
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
        if (size < 0) {
            throw new IllegalArgumentException("size < 0"); //$NON-NLS-1$
        }
        this.defaultSizeMap.put(position, size);
        this.isAggregatedSizeCacheValid = false;
    }

    private int getDefaultSize(int position) {
        int size = this.defaultSizeMap.getIfAbsent(position, -1);
        if (size != -1) {
            return size;
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
        int size = -1;
        if (isPercentageSizing()) {
            int value = this.realSizeMap.getIfAbsent(position, -1);
            if (value != -1) {
                return value;
            }
        } else {
            if (this.sizeMap.containsKey(position)) {
                size = this.sizeMap.get(position);
            }
        }
        if (size != -1) {
            return upScale(size);
        } else {
            return upScale(getDefaultSize(position));
        }
    }

    /**
     * Returns the minimum size for the given position. If no specific value is
     * configured for the given position, the default minimum size is returned.
     *
     * @param position
     *            The position for which the minimum size is requested.
     * @return The minimum size for the given position.
     * @see #getDefaultMinSize()
     *
     * @since 1.6
     */
    public int getMinSize(int position) {
        if (this.minSizeMap.containsKey(position)) {
            return upScale(this.minSizeMap.get(position));
        }
        return upScale(getDefaultMinSize());
    }

    /**
     * Set the minimum size for the given position. Will affect percentage
     * sizing to avoid sizes smaller than the given minimum value.
     *
     * @param position
     *            The position for which the minimum size should be set.
     * @param size
     *            The minimum size for the given position.
     * @throws IllegalArgumentException
     *             if size is less than 0.
     *
     * @since 1.6
     */
    public void setMinSize(int position, int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size < 0"); //$NON-NLS-1$
        }
        this.minSizeMap.put(position, size);
        // calculate the percentages if a position would be smaller than the min
        calculatePercentages(this.availableSpace, this.realSizeMap.size());
    }

    /**
     *
     * @return The default minimum size. Default value is 0.
     *
     * @since 1.6
     */
    public int getDefaultMinSize() {
        return this.defaultMinSize;
    }

    /**
     * Set the default minimum size. Will affect percentage sizing to avoid
     * sizes smaller than the given minimum value.
     *
     * @param defaultMinSize
     *            The default minimum size to use, can not be less than 0.
     * @throws IllegalArgumentException
     *             if defaultMinSize is less than 0.
     *
     * @since 1.6
     */
    public void setDefaultMinSize(int defaultMinSize) {
        if (defaultMinSize < 0) {
            throw new IllegalArgumentException("defaultMinSize < 0"); //$NON-NLS-1$
        }
        this.defaultMinSize = defaultMinSize;
        // calculate the percentages if a position would be smaller than the min
        calculatePercentages(this.availableSpace, this.realSizeMap.size());
    }

    /**
     *
     * @return <code>true</code> if the default min size or at least one
     *         position has a min size configured, <code>false</code> if no min
     *         size configuration is set.
     *
     * @since 1.6
     */
    public boolean isMinSizeConfigured() {
        return (this.defaultMinSize > 0 || !this.minSizeMap.isEmpty());
    }

    /**
     *
     * @param position
     *            The position for which it should be checked if a minimum size
     *            is configured.
     * @return <code>true</code> if the given position has a minimum size
     *         configured or a default minimum size is configured,
     *         <code>false</code> if not
     *
     * @since 1.6
     */
    public boolean isMinSizeConfigured(int position) {
        return (this.minSizeMap.containsKey(position) && this.minSizeMap.get(position) > 0) || this.defaultMinSize > 0;
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
                int oldValue = this.sizeMap.getIfAbsent(position, -1);
                int diff = (oldValue != -1) ? size - oldValue : size - getDefaultSize(position);
                this.sizeMap.put(position, size);

                // if percentage sizing is enabled and percentage values should
                // be fixed on resize, we recalculate percentages to the left
                // for the updated available space and update percentages of
                // positions to the right to give a user a predictable behavior
                if (isPercentageSizing() && this.fixPercentageValuesOnResize && this.availableSpace > 0) {
                    int percentageSpace = calculateAvailableSpace(this.availableSpace);

                    // first re-calculate the percentages based on the updated
                    // available space
                    fixPercentageValues(percentageSpace);

                    // now update the adjacent positions
                    double diffPercentage = ((double) upScale(diff) * 100) / percentageSpace;
                    updateAdjacent(position, diffPercentage, percentageSpace);
                }
            } else if (this.availableSpace > 0) {
                int percentageSpace = calculateAvailableSpace(this.availableSpace);

                double percentage = ((double) size * 100) / percentageSpace;

                boolean minSizeUpdate = isMinSizeConfigured(position) && size < getMinSize(position);

                if (this.fixPercentageValuesOnResize) {
                    fixPercentageValues(percentageSpace);
                }

                double oldValue = this.percentageSizeMap.getIfAbsent(position, -1);
                double diff = percentage;
                if (oldValue != -1 && !minSizeUpdate) {
                    diff = diff - oldValue;
                } else if (this.realSizeMap.containsKey(position)) {
                    // there was no percentage value before
                    // we need to calculate the before value out of the
                    // realSizeMap otherwise the resizing effect would
                    // have strange effects
                    double calculated = ((double) this.realSizeMap.get(position) * 100) / percentageSpace;
                    diff = diff - calculated;
                }

                // if a min size is configured and the size is set to a lower
                // value via resize, the min size needs to be adjusted
                if (minSizeUpdate) {
                    this.percentageSizeMap.forEachKeyValue((key, value) -> {
                        if (key != position && isPercentageSizing(key)) {
                            double calculated = ((double) this.realSizeMap.get(key) * 100) / percentageSpace;
                            if (calculated < value) {
                                this.percentageSizeMap.put(key, calculated);
                            }
                        }
                    });
                    setMinSize(position, size);
                }

                this.percentageSizeMap.put(position, percentage);

                // check the adjacent positions for percentage corrections
                diff = updateAdjacent(position, diff, percentageSpace);

                if (diff != 0 && oldValue == -1) {
                    // if the diff is not 0 and there was no size value set
                    // before we will remove the prior set value again
                    // this is because the position was configured as the
                    // only percentage sizing position with no specified
                    // value, which technically means that it should always
                    // take the remaining space
                    this.percentageSizeMap.remove(position);
                }

                // if the percentage sum is bigger than 100 after setSize, we
                // need to modify the set value so 100 is not exceeded to avoid
                // resize errors as a follow up
                if (percentage > 100) {
                    double adjusted = 100 - this.percentageSizeMap.select((key, value) -> key != position).sum();
                    this.percentageSizeMap.put(position, adjusted);
                }

            } else {
                // if a size is set, the position is configured for percentage
                // sizing but the available space is 0 we simply store the size
                // value as percentage value
                this.percentageSizeMap.put(position, size);
            }

            calculatePercentages(this.availableSpace, this.realSizeMap.size());
            this.isAggregatedSizeCacheValid = false;
        }
    }

    private void fixPercentageValues(int percentageSpace) {
        this.realSizeMap.forEachKeyValue((pos, value) -> {
            if (isPercentageSizing(pos)) {
                if (!this.percentageSizeMap.containsKey(pos)) {
                    // position is configured for percentage sizing
                    // but has no fixed percentage value
                    double calculatedPercentage = ((double) value * 100) / percentageSpace;
                    this.percentageSizeMap.put(pos, calculatedPercentage);
                } else {
                    // we have a fixed percentage value
                    // check if there is a difference to a min size
                    // or if pixels are distributed
                    int calculated = calculatePercentageValue(this.percentageSizeMap.get(pos), percentageSpace);
                    if (isMinSizeConfigured(pos) && calculated < getMinSize(pos)) {
                        double minPercentageValue = ((double) getMinSize(pos) * 100) / percentageSpace;
                        this.percentageSizeMap.put(pos, minPercentageValue);
                    } else if (this.realSizeMap.containsKey(pos)) {
                        double distributedPercentageValue = ((double) this.realSizeMap.get(pos) * 100) / percentageSpace;
                        this.percentageSizeMap.put(pos, distributedPercentageValue);
                    }
                }
            }
        });
    }

    /**
     * Updates the adjacent positions of the given original position by applying
     * the given diff. This way not all percentage sized position will change on
     * updating one position value, but only the adjacent ones.
     *
     * @param originalPosition
     *            The position whose size was changed.
     * @param diff
     *            The percentage difference that should be applied to an
     *            adjacent position.
     * @param percentageSpace
     *            The space available for percentage calculations, needed for
     *            min size percentage calculations.
     * @return The remaining diff or 0 if the diff could be completely applied
     *         to the position.
     */
    private double updateAdjacent(int originalPosition, double diff, int percentageSpace) {
        int nextPosition = originalPosition + 1;
        while (diff != 0 && this.realSizeMap.containsKey(nextPosition)) {
            if (isPositionResizable(nextPosition)) {
                diff = updateAdjacentPosition(nextPosition, diff, percentageSpace);
            }
            nextPosition++;
        }

        int previousPosition = originalPosition - 1;
        while (diff != 0 && this.realSizeMap.containsKey(previousPosition)) {
            if (isPositionResizable(previousPosition)) {
                diff = updateAdjacentPosition(previousPosition, diff, percentageSpace);
            }
            previousPosition--;
        }

        return diff;
    }

    /**
     * This method is used for resizing in percentage mode. If a position that
     * is configured for percentage sizing is resized, the size diff needs to be
     * added/removed from adjacent cells, to ensure consistent size calculation.
     *
     * @param position
     *            The position to update.
     * @param diff
     *            The diff that should be applied to the position.
     * @param percentageSpace
     *            The space available for percentage calculations, needed for
     *            min size percentage calculations.
     * @return The remaining diff or 0 if the diff could be completely applied
     *         to the position.
     */
    private double updateAdjacentPosition(int position, double diff, int percentageSpace) {
        // if previous resized position was at min size before, the adjacent
        // position needs to be increased more if the min size is reset
        boolean percentageConfigured = this.percentageSizeMap.containsKey(position);
        if (percentageConfigured || (isPercentageSizing(position) && this.realSizeMap.containsKey(position))) {
            if (isPercentageSizing(position) && percentageConfigured) {
                // there is a follow-up position that is configured for
                // percentage sizing and there is value specified for that
                // position
                double currentValue = this.percentageSizeMap.get(position);
                if (diff < currentValue) {
                    double newPercentageValue = currentValue - diff;
                    if (isMinSizeConfigured(position)
                            && calculatePercentageValue(newPercentageValue, percentageSpace) < getMinSize(position)) {
                        // calculate the new value to ensure we are not below
                        // min width with this
                        double minPercentageValue = ((double) getMinSize(position) * 100) / percentageSpace;
                        double newDiff = diff - (currentValue - minPercentageValue);
                        this.percentageSizeMap.put(position, minPercentageValue);
                        return newDiff;
                    } else {
                        this.percentageSizeMap.put(position, newPercentageValue);
                        return 0;
                    }
                } else {
                    diff = (diff - currentValue) + 1;
                    // never accept a percentage value < 1 as then the
                    // position would disappear
                    this.percentageSizeMap.put(position, 1d);
                    return diff;
                }
            }

            return 0;
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
        setPercentage(position, (double) percentage);
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
     *
     * @since 1.6
     */
    public void setPercentage(int position, double percentage) {
        if (percentage < 0) {
            throw new IllegalArgumentException("percentage < 0"); //$NON-NLS-1$
        }
        if (isPositionResizable(position)) {
            this.percentageSizingMap.put(position, true);
            this.percentageSizeMap.put(position, percentage);
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
        return this.resizablesMap.getIfAbsent(position, this.resizableByDefault);
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
        return this.defaultSizeMap.size() == 0 && this.sizeMap.size() == 0 && this.percentageSizeMap.size() == 0;
    }

    /**
     * @return <code>true</code> if the size of at least one position is
     *         interpreted in percentage, <code>false</code> if the size of all
     *         positions is interpreted by pixel.
     */
    public boolean isPercentageSizing() {
        if (!this.percentageSizingMap.isEmpty() && this.percentageSizingMap.containsValue(true)) {
            return true;
        }
        return this.percentageSizing;
    }

    /**
     * Configure whether the positions should be interpreted as percentage
     * values or pixel values.
     * <p>
     * <b>Note:</b> The configuration of this flag impacts the size calculation
     * in mixed mode. If this flag is set to <code>false</code>, positions that
     * are configured for fixed percentages will use the full available space
     * for percentage calculation. Setting it to <code>true</code> will cause
     * using the remaining space for percentage calculation. This means if also
     * fixed pixel sized positions are configured, they will be subtracted from
     * the full available space.
     * </p>
     *
     * @param percentageSizing
     *            <code>true</code> if the size of the positions should be
     *            interpreted as percentage values, <code>false</code> if the
     *            size of the positions should be interpreted by pixel.
     */
    public void setPercentageSizing(boolean percentageSizing) {
        this.percentageSizing = percentageSizing;
        this.isAggregatedSizeCacheValid = false;

        // if sizes are already configured, check if the sizes need to be
        // transferred to percentages
        if (!this.sizeMap.isEmpty()) {
            MutableIntIntMap transfer = this.sizeMap.select((key, value) -> isPercentageSizing(key));
            transfer.forEachKeyValue((key, value) -> {
                this.percentageSizeMap.put(key, value);
                this.sizeMap.remove(key);
            });
        }
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
        return this.percentageSizingMap.getIfAbsent(position, this.percentageSizing);
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
        if (space > -1 && isPercentageSizing()) {
            this.isAggregatedSizeCacheValid = false;
            this.availableSpace = space;

            int percentageSpace = calculateAvailableSpace(space);

            double sum = 0;
            int real = 0;
            int realSum = 0;
            int fixedSum = 0;
            int minSizeIncrease = 0;
            MutableIntList noInfoPositions = IntLists.mutable.empty();
            MutableIntList fixedPercentagePositions = IntLists.mutable.empty();
            int positionValue = -1;
            double positionPercentageValue = -1;
            for (int i = 0; i < positionCount; i++) {
                positionValue = this.sizeMap.getIfAbsent(i, -1);
                positionPercentageValue = this.percentageSizeMap.getIfAbsent(i, Double.valueOf("-1")); //$NON-NLS-1$
                if (positionPercentageValue == -1 && isPercentageSizing(i)) {
                    // remember the position for which no size information
                    // exists needed to calculate the size for those positions
                    // dependent on the remaining space
                    noInfoPositions.add(i);
                } else if (positionValue == -1 && !isPercentageSizing(i)) {
                    positionValue = getDefaultSize(i);
                }

                if (positionPercentageValue != -1 && isPercentageSizing(i)) {
                    real = calculatePercentageValue(positionPercentageValue, percentageSpace);
                    int minSize = getMinSize(i);
                    if (real < minSize) {
                        // remember the added pixels so they can be removed
                        // from other fixed percentage sized positions
                        minSizeIncrease += upScale(minSize - real);
                        // use the min size value
                        real = minSize;
                    } else {
                        sum += positionPercentageValue;
                    }
                    fixedPercentagePositions.add(i);
                    realSum += real;
                    this.realSizeMap.put(i, real);
                } else if (positionValue != -1) {
                    real = upScale(positionValue);
                    fixedSum += real;
                    realSum += real;
                    this.realSizeMap.put(i, real);
                }
            }

            int[] correction = correctPercentageValues(sum, positionCount);
            if (correction != null && correction.length > 0) {
                sum = correction[0];
                realSum = correction[1] + fixedSum;
            }

            // if percentage sizing and min size is configured and the used
            // space is bigger than the available space, check if there is a
            // percentage sized column without min size that needs to be reduced
            if (!fixedPercentagePositions.isEmpty() && realSum > space) {
                MutableIntList noMinWidth = IntLists.mutable.empty();
                int sumMod = 0;
                for (MutableIntIterator it = fixedPercentagePositions.intIterator(); it.hasNext();) {
                    int pos = it.next();
                    if (this.realSizeMap.get(pos) == getMinSize(pos)) {
                        sumMod += this.percentageSizeMap.get(pos);
                        it.remove();
                    } else {
                        noMinWidth.add(pos);
                    }
                }
                for (int pos : noMinWidth.toArray()) {
                    double percentage = this.percentageSizeMap.get(pos);
                    double ratio = percentage / sum;
                    int dist = (int) Math.round(minSizeIncrease * ratio);
                    int newValue = this.realSizeMap.get(pos) - dist;
                    newValue = (newValue > 0) ? newValue : 0;
                    realSum -= (this.realSizeMap.get(pos) - newValue);
                    this.realSizeMap.put(pos, newValue);
                }
                // update the sum to contain also the min size using position
                // values
                sum += sumMod;
            }

            // if min size configured, check noInfoPositions and update
            // according to the min size that gets applied
            if (!noInfoPositions.isEmpty() && isMinSizeConfigured()) {
                double remaining = (double) space - (double) realSum;
                double remainingColSpace = remaining / noInfoPositions.size();

                for (MutableIntIterator it = noInfoPositions.intIterator(); it.hasNext();) {
                    int position = it.next();
                    int minSize = getMinSize(position);
                    if (minSize > remainingColSpace) {
                        // a configured min size is bigger than the remaining
                        // space so treat the min size like a fixed value
                        realSum += minSize;
                        this.realSizeMap.put(position, minSize);
                        it.remove();
                    }
                }

                // if applying the min size to a non info position happened that
                // caused an increase of the real sum, we need to correct the
                // other positions
                if (realSum > space && !fixedPercentagePositions.isEmpty()) {
                    MutableIntList noMinWidth = IntLists.mutable.empty();
                    for (MutableIntIterator it = fixedPercentagePositions.intIterator(); it.hasNext();) {
                        int pos = it.next();
                        if (this.realSizeMap.get(pos) != getMinSize(pos)) {
                            noMinWidth.add(pos);
                        }
                    }
                    int exceed = realSum - space;
                    for (int pos : noMinWidth.toArray()) {
                        double percentage = this.percentageSizeMap.get(pos);
                        double ratio = percentage / sum;
                        int dist = (int) Math.round(exceed * ratio);
                        int newValue = this.realSizeMap.get(pos) - dist;
                        newValue = (newValue > 0) ? newValue : 0;
                        realSum -= (this.realSizeMap.get(pos) - newValue);
                        this.realSizeMap.put(pos, newValue);
                    }
                }
            }

            if (!noInfoPositions.isEmpty()) {
                // now calculate the size for the remaining columns
                double remaining = (double) space - (double) realSum;
                double remainingColSpace = remaining / noInfoPositions.size();
                for (int position : noInfoPositions.toArray()) {
                    sum += (remainingColSpace / space) * 100;
                    int minSize = getMinSize(position);
                    this.realSizeMap.put(position, remainingColSpace < minSize ? minSize : (int) remainingColSpace);
                }

                // If there are positions for which no size information exist,
                // the size config will use 100 percent of the available space
                // on percentage sizing. To handle rounding issues just set the
                // sum to 100 for correct calculation results.
                sum = 100;
            }

            // if percentage sizing is configured with fixed percentage values
            // and not 100 percent are used and it is configured to distribute
            // the remaining space, the fixed percentage positions are increased
            // according to their ratio to always fill the whole space
            if (sum < 100
                    && !fixedPercentagePositions.isEmpty()
                    && this.distributeRemainingSpace) {
                double remaining = (double) space - (double) realSum;
                if (remaining > 0) {
                    // calculate sum of eligible fixed percentage positions
                    double eligibleSum = fixedPercentagePositions.primitiveStream()
                            .mapToDouble(this.percentageSizeMap::get)
                            .sum();

                    // calculate ratio
                    for (int pos : fixedPercentagePositions.toArray()) {
                        if (getMinSize(pos) != this.realSizeMap.get(pos)) {
                            double percentage = this.percentageSizeMap.get(pos);
                            double ratio = percentage / eligibleSum;
                            int dist = (int) (remaining * ratio);
                            this.realSizeMap.put(pos, this.realSizeMap.get(pos) + dist);
                        }
                    }
                    sum = 100;
                }
            }

            if (sum == 100) {
                // check if the sum of the calculated values is the same as the
                // given space if not distribute the missing pixels to some of
                // the other columns. this is needed because of rounding issues
                // on 100% with odd-numbered pixel values
                int valueSum = (int) this.realSizeMap.values().sum();

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

                        // only increase positions that are not configured to
                        // be hidden or fixed size
                        int posValue = this.realSizeMap.getIfAbsent(pos, -1);
                        while (posValue != -1 && (posValue == 0 || !isPercentageSizing(pos) || getMinSize(pos) == posValue)) {
                            pos++;
                            if (pos > this.realSizeMap.keySet().max()) {
                                break;
                            }
                            posValue = this.realSizeMap.get(pos);
                        }

                        if (posValue != -1) {
                            this.realSizeMap.put(pos, posValue + 1);
                            pos++;
                        }
                    }
                }
            }

            // if the real sum is bigger than the available space we need to
            // perform corrections. this can happen in mixed mode sometimes
            if (realSum > this.availableSpace) {
                int extend = realSum - this.availableSpace;
                while (extend > 0) {
                    int remainingExtend = correctExtend(extend, fixedPercentagePositions);
                    // the correction caused nothing, so probably it is not
                    // possible to correct because of min size configuration
                    if (remainingExtend == extend) {
                        extend = 0;
                    } else {
                        extend = remainingExtend;
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
    private int calculatePercentageValue(double percentage, int space) {
        double factor = percentage / 100;
        return (int) (space * factor);
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
        if (!this.percentageSizingMap.isEmpty() && this.percentageSizing) {
            long toReduce = this.percentageSizingMap
                    .select((key, value) -> (!value && this.sizeMap.containsKey(key)))
                    .keySet()
                    .collectInt(key -> upScale(this.sizeMap.get(key)), IntLists.mutable.empty())
                    .sum();
            space -= toReduce;
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
     *
     * @since 1.6
     */
    protected int[] correctPercentageValues(double sum, int positionCount) {
        MutableIntIntMap toModify = IntIntMaps.mutable.empty();
        int fixedSum = 0;
        double modifySum = 0;
        for (int i = 0; i < positionCount; i++) {
            int positionValue = this.sizeMap.getIfAbsent(i, -1);
            double positionPercentageValue = this.percentageSizeMap.getIfAbsent(i, Double.valueOf("-1")); //$NON-NLS-1$
            if (positionPercentageValue != -1 && isPercentageSizing(i)
                    && (!isMinSizeConfigured(i) || (isMinSizeConfigured(i) && this.realSizeMap.get(i) != getMinSize(i)))) {
                toModify.put(i, this.realSizeMap.get(i));
                modifySum += positionPercentageValue;
            } else if (!isPercentageSizing(i) && positionValue != -1) {
                fixedSum += upScale(positionValue);
            }
        }

        // if the sum is greater than 100 we need to normalize the percentage
        // values
        if (sum > 100) {
            // calculate the factor which needs to be used to normalize the
            // values
            double excess = sum - 100;
            int excessPixel = (int) ((this.availableSpace - fixedSum) * excess / 100);

            double newPercentageSum = 0;
            int realSum = 0;

            for (IntIntPair mod : toModify.keyValuesView().toSortedList()) {
                double ratio = this.percentageSizeMap.get(mod.getOne()) / modifySum;
                int exc = (int) Math.ceil(excessPixel * ratio);

                int newValue = mod.getTwo() - exc;

                if (isMinSizeConfigured(mod.getOne()) && newValue < getMinSize(mod.getOne())) {
                    newValue = getMinSize(mod.getOne());
                }

                double newPercentage = ((double) newValue / (double) this.availableSpace - fixedSum) * 100;
                newPercentageSum += newPercentage;

                realSum += newValue;
                this.realSizeMap.put(mod.getOne(), newValue);
            }

            // if there are no excessPixels but the sum is greater than 100, we
            // probably came across a double calculation issue
            // as it is a recursive call, we do not check in advance and return
            // the calculation result in case a previous iteration made some
            // changes
            if (newPercentageSum > 100 && excessPixel > 0) {
                return correctPercentageValues(newPercentageSum, positionCount);
            }

            return new int[] { 100, realSum };
        }

        // the given sum is not greater than 100 so we do not have to modify
        // anything
        return new int[0];
    }

    private int calculateAggregatedSize(int position) {
        int resizeAggregate = 0;
        int resizedColumns = 0;

        boolean pSizing = isPercentageSizing();
        MutableIntIntMap mapToUse = pSizing ? this.realSizeMap : this.sizeMap;

        for (int resizedPosition : mapToUse.keySet().toSortedArray()) {
            if (resizedPosition < position) {
                resizedColumns++;
                int size = mapToUse.get(resizedPosition);
                resizeAggregate += pSizing ? size : upScale(size);
            } else {
                break;
            }
        }

        // also take into account the default size configuration per position
        for (int defaultPosition : this.defaultSizeMap.keySet().toSortedArray()) {
            if (defaultPosition < position) {
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

    private int correctExtend(int extend, MutableIntList fixedPercentagePositions) {
        int remainingExtend = extend;
        double eligibleSum = fixedPercentagePositions.primitiveStream()
                .mapToDouble(this.percentageSizeMap::get)
                .sum();
        // calculate ratio
        for (int pos : fixedPercentagePositions.toArray()) {
            if (remainingExtend > 0 && getMinSize(pos) != this.realSizeMap.get(pos)) {
                double percentage = this.percentageSizeMap.get(pos);
                double ratio = percentage / eligibleSum;
                int dist = extend == 1 ? 1 : (int) (extend * ratio);
                int oldValue = this.realSizeMap.get(pos);
                int newValue = oldValue - dist;
                // ensure that we do not go below the minimum
                if (isMinSizeConfigured(pos) && newValue < getMinSize(pos)) {
                    newValue = getMinSize(pos);
                    dist = oldValue - newValue;
                } else if (newValue < 0) {
                    // we can not be smaller than 0
                    newValue = 0;
                    dist = oldValue;
                }
                this.realSizeMap.put(pos, newValue);
                remainingExtend -= dist;
            }
        }
        return remainingExtend;
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
     * @since 1.6
     */
    public int upScale(int value) {
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
     *
     * @see IDpiConverter#convertDpiToPixel(int)
     * @since 1.6
     */
    public int downScale(int value) {
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
        this.isAggregatedSizeCacheValid = false;
    }

    /**
     * Resets the configured size values held by this {@link SizeConfig}.
     *
     * @since 1.6
     */
    public void reset() {
        this.defaultSizeMap.clear();
        this.sizeMap.clear();
        this.resizablesMap.clear();
        this.percentageSizeMap.clear();
        this.percentageSizingMap.clear();
        this.realSizeMap.clear();
        this.aggregatedSizeCacheMap.clear();
        this.minSizeMap.clear();
    }

    /**
     * Resets the custom configured size of the given position. This way the
     * default size will be applied for that position afterwards.
     *
     * @param position
     *            The position for which the size configuration should be reset.
     *
     * @since 1.6
     */
    public void resetConfiguredSize(int position) {
        this.sizeMap.remove(position);
        this.percentageSizeMap.remove(position);
        this.isAggregatedSizeCacheValid = false;
        calculatePercentages(this.availableSpace, this.realSizeMap.size());
    }

    /**
     * Resets the custom configured min size of the given position. This way the
     * default min size will be applied for that position afterwards.
     *
     * @param position
     *            The position for which the min size configuration should be
     *            reset.
     *
     * @since 1.6
     */
    public void resetConfiguredMinSize(int position) {
        this.minSizeMap.remove(position);
        this.isAggregatedSizeCacheValid = false;
        calculatePercentages(this.availableSpace, this.realSizeMap.size());
    }

    /**
     * Returns the configured size value for the given position or -1 if no
     * custom size was configured and therefore the default size is used for
     * that position.
     *
     * @param position
     *            The position for which the configured size should be returned.
     * @return The configured size or -1 in case the default size is used for
     *         that position.
     *
     * @since 1.6
     */
    public int getConfiguredSize(int position) {
        return this.sizeMap.getIfAbsent(position, -1);
    }

    /**
     * Returns the configured percentage size value for the given position or -1
     * if no custom percentage value was configured and therefore the size is
     * dynamic for that position.
     *
     * @param position
     *            The position for which the configured size should be returned.
     * @return The configured size or -1 in case the default size is used for
     *         that position.
     *
     * @since 1.6
     */
    public double getConfiguredPercentageSize(int position) {
        return this.percentageSizeMap.getIfAbsent(position, Double.valueOf("-1")); //$NON-NLS-1$
    }

    /**
     * Returns the configured minimum size value for the given position or -1 if
     * no custom minimum size was configured and therefore the default minimum
     * size is used for that position.
     *
     * @param position
     *            The position for which the configured minimum size should be
     *            returned.
     * @return The configured minimum size or -1 in case the default minimum
     *         size is used for that position.
     *
     * @since 1.6
     */
    public int getConfiguredMinSize(int position) {
        return this.minSizeMap.getIfAbsent(position, -1);
    }

    /**
     *
     * @return <code>true</code> if remaining space on fixed percentage sizing
     *         is distributed to other percentage sized positions,
     *         <code>false</code> if not. Default is <code>false</code>.
     *
     * @since 1.6
     */
    public boolean isDistributeRemainingSpace() {
        return this.distributeRemainingSpace;
    }

    /**
     * Configure the percentage sizing behavior when manually specifying
     * percentages and not having 100% configured. By default the remaining
     * space is not distributed to the configured positions. That means for
     * example that 25% of 100 pixels will be 25, regardless of the other
     * positions. When setting this flag to <code>true</code> the 25% will be
     * increased so the whole available space is filled.
     * <p>
     * <b>Note:</b> For dynamic percentage sized positions this flag should also
     * be set to <code>true</code> to avoid sizing issues because of rounding
     * issues after resizing.
     * </p>
     *
     * @param distributeRemaining
     *            <code>true</code> if remaining space on fixed percentage
     *            sizing should be distributed to other percentage sized
     *            positions, <code>false</code> if not.
     *
     * @since 1.6
     */
    public void setDistributeRemainingSpace(boolean distributeRemaining) {
        this.distributeRemainingSpace = distributeRemaining;
    }

    /**
     * Return whether percentage sized positions should be fixed on any resize
     * or not. This means, if positions are configured for percentage sizing
     * without a specific percentage value, the size is calculated based on the
     * space that is still available. If this flag is set to <code>false</code>
     * only the position that is resized will get a fixed value. The other
     * positions will still be dynamic and therefore will also resize as the
     * available space is changed. Setting this flag to <code>true</code> will
     * cause that all positions with dynamic percentage configuration will get a
     * fixed percentage value to have a deterministic resize behavior for the
     * user that triggers the resize. Also percentage sized positions with a
     * minimum size, where the minimum is bigger than the calculated percentage
     * value will be recalculated to set the percentage value that matches the
     * current state. Default is <code>true</code>.
     *
     * @return <code>true</code> if calculating the fix percentage value for
     *         dynamic percentage sized positions and position with a configured
     *         minimum on resize, <code>false</code> if the dynamic percentage
     *         sized positions stay dynamic on resize.
     *
     * @since 1.6
     */
    public boolean isFixPercentageValuesOnResize() {
        return this.fixPercentageValuesOnResize;
    }

    /**
     * Configure whether percentage sized positions should be fixed on any
     * resize or not. This means, if positions are configured for percentage
     * sizing without a specific percentage value, the size is calculated based
     * on the space that is still available. If this flag is set to
     * <code>false</code> only the position that is resized will get a fixed
     * value. The other positions will still be dynamic and therefore will also
     * resize as the available space is changed. Setting this flag to
     * <code>true</code> will cause that all positions with dynamic percentage
     * configuration will get a fixed percentage value to have a deterministic
     * resize behavior for the user that triggers the resize. Also percentage
     * sized positions with a minimum size, where the minimum is bigger than the
     * calculated percentage value will be recalculated to set the percentage
     * value that matches the current state. Default is <code>true</code>.
     *
     * @param enabled
     *            <code>true</code> to calculate the fix percentage value for
     *            dynamic percentage sized positions and position with a
     *            configured minimum on resize, <code>false</code> if the
     *            dynamic percentage sized positions should stay dynamic on
     *            resize.
     *
     * @since 1.6
     */
    public void setFixPercentageValuesOnResize(boolean enabled) {
        this.fixPercentageValuesOnResize = enabled;
    }
}

/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;


/**
 * This class stores the size configuration of rows/columns within the NatTable.
 */
public class SizeConfig implements IPersistable {

	public static final String PERSISTENCE_KEY_DEFAULT_SIZE = ".defaultSize"; //$NON-NLS-1$
	public static final String PERSISTENCE_KEY_DEFAULT_SIZES = ".defaultSizes"; //$NON-NLS-1$
	public static final String PERSISTENCE_KEY_SIZES = ".sizes"; //$NON-NLS-1$
	public static final String PERSISTENCE_KEY_RESIZABLE_BY_DEFAULT = ".resizableByDefault"; //$NON-NLS-1$
	public static final String PERSISTENCE_KEY_RESIZABLE_INDEXES = ".resizableIndexes"; //$NON-NLS-1$
	public static final String PERSISTENCE_KEY_PERCENTAGE_SIZING = ".percentageSizing"; //$NON-NLS-1$

	/**
	 * The global default size of this {@link SizeConfig}.
	 */
	private int defaultSize;
	/**
	 * Map that contains default sizes per column.
	 */
	private final Map<Integer, Integer> defaultSizeMap = new TreeMap<Integer, Integer>();
	/**
	 * Map that contains sizes per column.
	 */
	private final Map<Integer, Integer> sizeMap = new TreeMap<Integer, Integer>();
	/**
	 * Map that contains the resizable information per row/column.
	 */
	private final Map<Integer, Boolean> resizablesMap = new TreeMap<Integer, Boolean>();
	/**
	 * The global resizable information of this {@link SizeConfig}.
	 */
	private boolean resizableByDefault = true;
	/**
	 * Flag to tell whether the sizing is done for pixel or percentage values.
	 */
	private boolean percentageSizing = false;
	/**
	 * The available space needed for percentage calculation on resizing.
	 */
	private int availableSpace = -1;
	/**
	 * Map that contains the real pixel size. Will only be used on percentage sizing.
	 * This map is not persisted as it will be calculated on resize.
	 */
	private final Map<Integer, Integer> realSizeMap = new TreeMap<Integer, Integer>();

	/**
	 * Create a new {@link SizeConfig} with the given default size.
	 * @param defaultSize The default size to use.
	 */
	public SizeConfig(int defaultSize) {
		this.defaultSize = defaultSize;
	}

	// Persistence

	@Override
	public void saveState(String prefix, Properties properties) {
		properties.put(prefix + PERSISTENCE_KEY_DEFAULT_SIZE, String.valueOf(defaultSize));
		saveMap(defaultSizeMap, prefix + PERSISTENCE_KEY_DEFAULT_SIZES, properties);
		saveMap(sizeMap, prefix + PERSISTENCE_KEY_SIZES, properties);
		properties.put(prefix + PERSISTENCE_KEY_RESIZABLE_BY_DEFAULT, String.valueOf(resizableByDefault));
		saveMap(resizablesMap, prefix + PERSISTENCE_KEY_RESIZABLE_INDEXES, properties);
		properties.put(prefix + PERSISTENCE_KEY_PERCENTAGE_SIZING, String.valueOf(percentageSizing));
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
		//ensure to cleanup the current states prior loading new ones
		defaultSizeMap.clear();
		sizeMap.clear();
		resizablesMap.clear();
		
		String persistedDefaultSize = properties.getProperty(prefix + PERSISTENCE_KEY_DEFAULT_SIZE);
		if (!StringUtils.isEmpty(persistedDefaultSize)) {
			defaultSize = Integer.valueOf(persistedDefaultSize).intValue();
		}

		String persistedResizableDefault = properties.getProperty(prefix + PERSISTENCE_KEY_RESIZABLE_BY_DEFAULT);
		if (!StringUtils.isEmpty(persistedResizableDefault)) {
			resizableByDefault = Boolean.valueOf(persistedResizableDefault).booleanValue();
		}

		String persistedPercentageSizing = properties.getProperty(prefix + PERSISTENCE_KEY_PERCENTAGE_SIZING);
		if (!StringUtils.isEmpty(persistedPercentageSizing)) {
			setPercentageSizing(Boolean.valueOf(persistedPercentageSizing).booleanValue());
		}

		loadBooleanMap(prefix + PERSISTENCE_KEY_RESIZABLE_INDEXES, properties, resizablesMap);
		loadIntegerMap(prefix + PERSISTENCE_KEY_DEFAULT_SIZES, properties, defaultSizeMap);
		loadIntegerMap(prefix + PERSISTENCE_KEY_SIZES, properties, sizeMap);
	}

	private void loadIntegerMap(String key, Properties properties, Map<Integer, Integer> map) {
		String property = properties.getProperty(key);
		if (property != null) {
			map.clear();

			StringTokenizer tok = new StringTokenizer(property, ","); //$NON-NLS-1$
			while (tok.hasMoreTokens()) {
				String token = tok.nextToken();
				int separatorIndex = token.indexOf(':');
				map.put(Integer.valueOf(token.substring(0, separatorIndex)), Integer.valueOf(token.substring(separatorIndex + 1)));
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
				map.put(Integer.valueOf(token.substring(0, separatorIndex)), Boolean.valueOf(token.substring(separatorIndex + 1)));
			}
		}
	}

	// Default size

	public void setDefaultSize(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("size < 0"); //$NON-NLS-1$
		}
		this.defaultSize = size;
	}

	public void setDefaultSize(int position, int size) {
		if (defaultSize < 0) {
			throw new IllegalArgumentException("size < 0"); //$NON-NLS-1$
		}
		defaultSizeMap.put(Integer.valueOf(position), Integer.valueOf(size));
	}

	private int getDefaultSize(int position) {
		Integer size = defaultSizeMap.get(Integer.valueOf(position));
		if (size != null) {
			return size.intValue();
		} else {
			return defaultSize;
		}
	}

	// Size

	public int getAggregateSize(int position) {
		if (position < 0) {
			return -1;
		} else if (position == 0) {
			return 0;
		} else if (isAllPositionsSameSize() && !percentageSizing) {
			return position * defaultSize;
		} else {
			int resizeAggregate = 0;

			int resizedColumns = 0;
			
			Map<Integer, Integer> mapToUse = percentageSizing ? realSizeMap : sizeMap;
			
			for (Integer resizedPosition : mapToUse.keySet()) {
				if (resizedPosition.intValue() < position) {
					resizedColumns++;
					resizeAggregate += mapToUse.get(resizedPosition);
				} else {
					break;
				}
			}

			return (position * defaultSize) + (resizeAggregate - (resizedColumns * defaultSize));
		}
	}

	public int getSize(int position) {
		Integer size;
		if (percentageSizing) {
			size = realSizeMap.get(Integer.valueOf(position));
		} else {
			size = sizeMap.get(Integer.valueOf(position));
		}
		if (size != null) {
			return size.intValue();
		} else {
			return getDefaultSize(position);
		}
	}

	/**
	 * Sets the given size for the given position. This method can be called manually for configuration
	 * via {@link DataLayer} and will be called on resizing within the rendered UI. This is why there
	 * is a check for percentage configuration. If this {@link SizeConfig} is configured to not use
	 * percentage sizing, the size is taken as is. If percentage sizing is enabled, the given size
	 * will be calculated to percentage value based on the already known pixel values.
	 * 
	 * If you want to use percentage sizing you should use {@link SizeConfig#setPercentage(int, int)}
	 * for manual size configuration to avoid unnecessary calculations.
	 * 
	 * @param position The position for which the size should be set.
	 * @param size The size in pixels to set for the given position.
	 */
	public void setSize(int position, int size) {
		if (size < 0) {
			throw new IllegalArgumentException("size < 0"); //$NON-NLS-1$
		}
		if (isPositionResizable(position)) {
			//check whether the given value should be remembered as is or if it needs to be calculated
			if (!isPercentageSizing()) {
				sizeMap.put(Integer.valueOf(position), Integer.valueOf(size));
			} else {
				if (availableSpace > 0) {
					Double percentage = ((double) size * 100)/ availableSpace;
					sizeMap.put(position, percentage.intValue());
					calculatePercentages(availableSpace, realSizeMap.size());
				}
			}
			
		}
	}

	/**
	 * Will set the given percentage for the given position if this {@link SizeConfig}
	 * is configured for percentage sizing.
	 * @param position
	 * @param percentage
	 */
	public void setPercentage(int position, int percentage) {
		if (percentage < 0) {
			throw new IllegalArgumentException("percentage < 0"); //$NON-NLS-1$
		}
		if (isPositionResizable(position) && isPercentageSizing()) {
			sizeMap.put(Integer.valueOf(position), Integer.valueOf(percentage));
			realSizeMap.put(position, calculatePercentageValue(percentage, availableSpace));
			calculatePercentages(availableSpace, realSizeMap.size());
		}
	}

	// Resizable

	/**
	 * @return The global resizable information of this {@link SizeConfig}.
	 */
	public boolean isResizableByDefault() {
		return resizableByDefault;
	}

	/**
	 * Checks if there is a special resizable configuration for the given position. If not the
	 * global resizable information is returned.
	 * @param position The position of the row/column for which the resizable information is requested.
	 * @return <code>true</code> if the given row/column position is resizable,
	 * 			<code>false</code> if not.
	 */
	public boolean isPositionResizable(int position) {
		Boolean resizable = resizablesMap.get(Integer.valueOf(position));
		if (resizable != null) {
			return resizable.booleanValue();
		}
		return resizableByDefault;
	}

	/**
	 * Sets the resizable configuration for the given row/column position.
	 * @param position The position of the row/column for which the resizable configuration should be set.
	 * @param resizable <code>true</code> if the given row/column position should be resizable,
	 * 			<code>false</code> if not.
	 */
	public void setPositionResizable(int position, boolean resizable) {
		if (percentageSizing && resizable) {
			throw new IllegalStateException("Resizable columns are not allowed when using percentaged sizing."); //$NON-NLS-1$
		}
		resizablesMap.put(Integer.valueOf(position), Boolean.valueOf(resizable));
	}

	/**
	 * Sets the global resizable configuration.
	 * Will reset all special resizable configurations.
	 * @param resizableByDefault <code>true</code> if all rows/columns should be resizable,
	 * 			<code>false</code> if no row/column should be resizable.
	 */
	public void setResizableByDefault(boolean resizableByDefault) {
		if (percentageSizing && resizableByDefault) {
			throw new IllegalStateException("Resizable columns are not allowed when using percentaged sizing."); //$NON-NLS-1$
		}
		resizablesMap.clear();
		this.resizableByDefault = resizableByDefault;
	}

	// All positions same size

	public boolean isAllPositionsSameSize() {
		return defaultSizeMap.size() == 0 && sizeMap.size() == 0;
	}

	/**
	 * @return <code>true</code> if the size of the positions is interpreted percentaged,
	 * 			<code>false</code> if the size of the positions is interpreted by pixel.
	 */
	public boolean isPercentageSizing() {
		return this.percentageSizing;
	}
	
	/**
	 * @param percentageSizing <code>true</code> if the size of the positions should be interpreted percentaged,
	 * 			<code>false</code> if the size of the positions should be interpreted by pixel.
	 */
	public void setPercentageSizing(boolean percentageSizing) {
		this.percentageSizing = percentageSizing;
	}

	/**
	 * Will calculate the real pixel values for the positions if percentage sizing is enabled.
	 * @param space The space that is available for rendering.
	 * @param positionCount The number of positions that should be handled by this {@link SizeConfig}
	 */
	public void calculatePercentages(int space, int positionCount) {
		if (this.percentageSizing) {
			this.availableSpace = space;
			int sum = 0;
			int real = 0;
			int realSum = 0;
			List<Integer> noInfoPositions = new ArrayList<Integer>();
			Integer positionValue = null;
			for (int i = 0; i < positionCount; i++) {
				positionValue = this.sizeMap.get(i);
				if (positionValue != null) {
					sum += positionValue;
					real = calculatePercentageValue(positionValue, space);
					realSum += real;
					this.realSizeMap.put(i, real);
				} else {
					//remember the position for which no size information exists
					//needed to calculate the size for those positions dependent on the 
					//remaining space
					noInfoPositions.add(i);
				}
			}
			
			if (!noInfoPositions.isEmpty()) {
				//now calculate the size for the remaining columns
				double remaining = new Double(space) - realSum;
				Double remainingColSpace = remaining / noInfoPositions.size();
				for (Integer position : noInfoPositions) {
					sum += (remainingColSpace / space) * 100;
					this.realSizeMap.put(position, remainingColSpace.intValue());
				}
				//If there are positions for which no size information exist, the size config
				//will use 100 percent of the available space on percentage sizing. To handle
				//rounding issues just set the sum to 100 for correct calculation results.
				sum = 100;
			}
			if (sum == 100) {
				//check if the sum of the calculated values is the same as the given space
				//if not add the missing pixels to the last value
				//this is needed because of rounding issues on 100% with odd-numbered pixel values
				int valueSum = 0;
				int lastPos = -1;
				for (Map.Entry<Integer, Integer> entry : this.realSizeMap.entrySet()) {
					valueSum += entry.getValue();
					lastPos = Math.max(lastPos, entry.getKey());
				}
				
				if (valueSum < space) {
					int lastPosValue = this.realSizeMap.get(lastPos);
					this.realSizeMap.put(lastPos, lastPosValue + (space - valueSum));
				}
			}
			
		}
	}
	
	/**
	 * @param percentage The percentage value.
	 * @param space The available space
	 * @return The percentaged value of the given space.
	 */
	private int calculatePercentageValue(int percentage, int space) {
		double factor = (double) percentage / 100;
		return new Double(space * factor).intValue();
	}

}

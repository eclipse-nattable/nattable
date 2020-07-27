/*****************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Helper class to de-normalize a collection of objects with hierarchical object
 * relations into a simple collection.
 *
 * @see HierarchicalWrapper
 *
 * @since 1.6
 */
public final class HierarchicalHelper {

    private static final Object lockObj = new Object();

    private HierarchicalHelper() {
        // empty default constructor for helper class
    }

    /**
     * The regular expression that is used to separate properties for accessing
     * fields on different levels of a {@link HierarchicalWrapper}.
     */
    public static final String PROPERTY_SEPARATOR_REGEX = "\\."; //$NON-NLS-1$

    /**
     * De-normalizes a given list of objects with a hierarchical object graph to
     * a flattened list. The flattened list therefore contains redundant data
     * for each entry that has the same parent objects. It will created
     * {@link HierarchicalWrapper} objects for this, which contain references to
     * the original objects. Therefore property changes will be directly
     * reflected in the hierarchical object graph.
     * <p>
     * To identify the nested objects, the property names are inspected if they
     * contain dots for hierarchical object paths. For example
     * <code>parent.children.name</code> says that the parent has a property
     * that is a collection (children) where each entry in the children
     * collection is an object that has a property name. In this example there
     * will be an entry in the flattened list for each child, where the parent
     * is repeated in each wrapper object.
     * </p>
     *
     * @param input
     *            The collection of objects with a hierarchical object graph.
     * @param addParentObject
     *            <code>true</code> to add row objects for parent objects that
     *            do no have object references for deeper levels,
     *            <code>false</code> to have no dedicated row object for parent
     *            objects.
     * @param propertyNames
     *            The property names to access the properties in the object
     *            graph.
     * @return The flattened list of the hierarchical object graph.
     */
    public static List<HierarchicalWrapper> deNormalize(List<?> input, boolean addParentObject, String... propertyNames) {
        return deNormalize(input, addParentObject, Arrays.asList(propertyNames));
    }

    /**
     * De-normalizes a given list of objects with a hierarchical object graph to
     * a flattened list. The flattened list therefore contains redundant data
     * for each entry that has the same parent objects. It will created
     * {@link HierarchicalWrapper} objects for this, which contain references to
     * the original objects. Therefore property changes will be directly
     * reflected in the hierarchical object graph.
     * <p>
     * To identify the nested objects, the property names are inspected if they
     * contain dots for hierarchical object paths. For example
     * <code>parent.children.name</code> says that the parent has a property
     * that is a collection (children) where each entry in the children
     * collection is an object that has a property name. In this example there
     * will be an entry in the flattened list for each child, where the parent
     * is repeated in each wrapper object.
     * </p>
     *
     * @param input
     *            The collection of objects with a hierarchical object graph.
     * @param addParentObject
     *            <code>true</code> to add row objects for parent objects that
     *            do no have object references for deeper levels,
     *            <code>false</code> to have no dedicated row object for parent
     *            objects.
     * @param propertyNames
     *            The property names to access the properties in the object
     *            graph.
     * @return The flattened list of the hierarchical object graph.
     */
    public static List<HierarchicalWrapper> deNormalize(List<?> input, boolean addParentObject, List<String> propertyNames) {
        ArrayList<HierarchicalWrapper> result = new ArrayList<>();

        if (input != null) {
            LinkedHashSet<String> nested = new LinkedHashSet<>();
            for (String name : propertyNames) {
                String[] prop = name.split(PROPERTY_SEPARATOR_REGEX);
                if (prop.length > 1) {
                    nested.add(prop[prop.length - 2]);
                }
            }

            String[] nestedArray = nested.toArray(new String[] {});

            for (Object root : input) {
                if (root != null) {
                    // length + 1 because of root
                    HierarchicalWrapper rootWrapper = new HierarchicalWrapper(nestedArray.length + 1);
                    rootWrapper.setObject(0, root);
                    result.addAll(deNormalizeWithDirectChildren(rootWrapper, 0, nestedArray,
                            new HashMap<Class<?>, Map<String, PropertyDescriptor>>(), addParentObject));
                }
            }
        }

        return result;
    }

    /**
     * De-normalizes the next level in the object graph.
     *
     * @param parent
     *            The already available {@link HierarchicalWrapper} with the
     *            parent levels resolved. Will be cloned for de-normalizing
     *            deeper levels.
     * @param level
     *            The current level of de-normalization.
     * @param nested
     *            The properties of the collections that specify the object
     *            graph without the parent properties.
     * @param propertyDescriptorMap
     *            The map of already identified {@link PropertyDescriptor}s, to
     *            avoid multiple lookups via reflection.
     * @param addParentObject
     *            <code>true</code> if the already available
     *            {@link HierarchicalWrapper} with the resolved parent levels
     *            should be added to the result, <code>false</code> if not.
     * @return The flattened list of the object at the given level with regards
     *         to the nested objects, or a collection with only the given parent
     *         object if that object has not children.
     */
    private static List<HierarchicalWrapper> deNormalizeWithDirectChildren(HierarchicalWrapper parent, int level,
            String[] nested, Map<Class<?>, Map<String, PropertyDescriptor>> propertyDescriptorMap, boolean addParentObject) {

        if (level < nested.length) {
            Object child = getDataValue(parent.getObject(level), nested[level], propertyDescriptorMap);
            if (child instanceof Collection<?>) {
                ArrayList<HierarchicalWrapper> result = new ArrayList<>();
                Collection<?> children = (Collection<?>) child;

                if (addParentObject) {
                    result.add(parent);
                }

                for (Object root : children) {
                    HierarchicalWrapper rootWrapper = new HierarchicalWrapper(parent);
                    rootWrapper.setObject(level + 1, root);
                    result.addAll(deNormalizeWithDirectChildren(rootWrapper, level + 1, nested, propertyDescriptorMap, addParentObject));
                }
                return result;
            }
        }

        return Arrays.asList(parent);
    }

    /**
     * Get the data value for the given property name out of the given object
     * via reflection.
     *
     * @param rowObj
     *            The row object from which the data value should be read.
     * @param propertyName
     *            The name of the property that should be read.
     * @param propertyDescriptorMap
     *            The map of already identified {@link PropertyDescriptor}s, to
     *            avoid multiple lookups via reflection.
     * @return The data value for the given property name out of the given
     *         object.
     */
    private static Object getDataValue(Object rowObj, String propertyName,
            Map<Class<?>, Map<String, PropertyDescriptor>> propertyDescriptorMap) {

        try {
            PropertyDescriptor propertyDesc = getPropertyDescriptor(rowObj, propertyName, propertyDescriptorMap);
            Method readMethod = propertyDesc.getReadMethod();
            return readMethod.invoke(rowObj);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Retrieves the {@link PropertyDescriptor} for the given object and
     * property name.
     *
     * @param rowObj
     *            The object for which the {@link PropertyDescriptor} is
     *            requested.
     * @param propertyName
     *            The name of the property for which the
     *            {@link PropertyDescriptor} is requested.
     * @param propertyDescriptorMap
     *            The map of already identified {@link PropertyDescriptor}s, to
     *            avoid multiple lookups via reflection.
     * @return The {@link PropertyDescriptor} for the property with the given
     *         name in the given object.
     * @throws IntrospectionException
     *             if an exception occurs during introspection
     */
    private static PropertyDescriptor getPropertyDescriptor(Object rowObj, String propertyName,
            Map<Class<?>, Map<String, PropertyDescriptor>> propertyDescriptorMap) throws IntrospectionException {

        synchronized (lockObj) {
            Map<String, PropertyDescriptor> descriptorMap = propertyDescriptorMap.get(rowObj.getClass());

            if (descriptorMap == null) {
                PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(rowObj.getClass())
                        .getPropertyDescriptors();

                Map<String, PropertyDescriptor> propertiesByAttribute = new HashMap<>();
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    propertiesByAttribute.put(propertyDescriptor.getName(), propertyDescriptor);
                }
                descriptorMap = propertiesByAttribute;
                propertyDescriptorMap.put(rowObj.getClass(), propertiesByAttribute);
            }

            return descriptorMap.get(propertyName);
        }
    }

    /**
     * Returns the level object from the given {@link HierarchicalWrapper}
     * object by the given property name.
     *
     * @param wrapper
     *            The {@link HierarchicalWrapper} from which the level object
     *            should be retrieved.
     * @param propertyName
     *            The name of the property to identify the level object from.
     * @return The level object from the {@link HierarchicalWrapper} according
     *         to the given property name.
     */
    public static Object getLevelObjectByProperty(HierarchicalWrapper wrapper, String propertyName) {
        String[] split = propertyName.split(PROPERTY_SEPARATOR_REGEX);
        return wrapper.getObject(split.length - 1);
    }

    /**
     * Calculates the mapping of hierarchy levels to column indexes belonging to
     * the level.
     *
     * @param propertyNames
     *            The property names to access the properties in the object
     *            graph.
     * @return The mapping of the level to the list of all column indexes
     *         belonging to a level.
     */
    public static Map<Integer, List<Integer>> getLevelIndexMapping(String[] propertyNames) {
        LinkedHashMap<Integer, List<Integer>> levelIndexMapping = new LinkedHashMap<>();
        if (propertyNames.length > 0) {
            int currentLevel = 1;
            ArrayList<Integer> columns = new ArrayList<>();
            columns.add(0);
            levelIndexMapping.put(0, columns);
            for (int col = 1; col < propertyNames.length; col++) {
                String[] split = propertyNames[col].split(PROPERTY_SEPARATOR_REGEX);
                if (split.length == currentLevel) {
                    columns.add(col);
                } else if (split.length > currentLevel) {
                    columns = new ArrayList<Integer>();
                    columns.add(col);
                    levelIndexMapping.put(currentLevel, columns);
                    currentLevel++;
                }
            }
        }
        return levelIndexMapping;
    }
}

/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;

/**
 * Model implementation to track groups of columns/rows.
 *
 * @since 1.6
 */
public class GroupModel implements IPersistable {

    /**
     * Persistence key for persisting the group model states.
     */
    private static final String PERSISTENCE_KEY_GROUP_MODEL = ".groupModel"; //$NON-NLS-1$

    /**
     * Converter to support layer based position-index conversion.
     */
    protected IndexPositionConverter indexPositionConverter;

    /**
     * Flag to configure whether newly created groups should be initially
     * expanded or collapsed.
     */
    private boolean defaultCollapseable = true;

    /**
     * Flag to configure whether newly created groups should be initially
     * unbreakable or not.
     */
    private boolean defaultUnbreakable = false;

    /**
     * Collection of groups managed by this GroupModel.
     */
    private final List<Group> groups = new LinkedList<Group>();

    /**
     *
     * @return The unmodifiable list of {@link Group}s contained in this
     *         {@link GroupModel}.
     */
    List<Group> getGroups() {
        return Collections.unmodifiableList(this.groups);
    }

    /**
     * This method is typically called by a group header layer to ensure that
     * the cell positions match the underlying scrollable layer below the
     * ViewportLayer.
     *
     * @param converter
     *            Converter to support layer based position-index conversion.
     */
    void setIndexPositionConverter(IndexPositionConverter converter) {
        this.indexPositionConverter = converter;

        // update the visible start positions of the already registered groups
        updateVisibleStartPositions();
    }

    /**
     * Converts the given position to the corresponding index in case the
     * {@link #indexPositionConverter} is set. Otherwise simply returns the
     * given position.
     *
     * @param position
     *            The position to convert.
     * @return The index that corresponds to the given position or the position
     *         itself if a conversion is not possible.
     */
    int getIndexByPosition(int position) {
        if (this.indexPositionConverter != null) {
            return this.indexPositionConverter.convertPositionToIndex(position);
        }
        return position;
    }

    /**
     * Converts the given index to the corresponding position in case the
     * {@link #indexPositionConverter} is set. Otherwise simply returns the
     * given index.
     *
     * @param index
     *            The index to convert.
     * @return The position for the given index or the position itself if a
     *         conversion is not possible.
     */
    int getPositionByIndex(int index) {
        if (this.indexPositionConverter != null) {
            return this.indexPositionConverter.convertIndexToPosition(index);
        }
        return index;
    }

    /**
     * Updates the visible start position of all {@link Group}s based on the
     * currently set visible start index.
     */
    void updateVisibleStartPositions() {
        for (Group group : this.groups) {
            group.updateVisibleStartPosition();
        }
    }

    /**
     * Executes a consistency check for the configured groups to ensure that the
     * current visible state matches the position state. Should only be
     * triggered in case it is expected that the consistency is not given, e.g.
     * if hide events without details where triggered.
     *
     * @param updateStartIndex
     *            flag to indicate if the start index of the group should also
     *            be updated. Needed in case of complete structural refreshes to
     *            be able to reset the group completely.
     */
    void performConsistencyCheck(boolean updateStartIndex) {
        for (Group group : this.groups) {
            group.consistencyCheck(updateStartIndex);
        }
    }

    @Override
    public void saveState(String prefix, Properties properties) {
        StringBuilder strBuilder = new StringBuilder();

        for (Group group : this.groups) {
            String groupName = group.getName();

            // if this group has no valid start index, continue without
            // saving state.
            // A group can have an invalid start index if groups are used to
            // organize columns/rows on a higher abstraction level ...
            if (group.getStartIndex() < 0) {
                continue;
            }

            strBuilder.append(groupName);
            strBuilder.append('=');

            strBuilder.append(group.startIndex).append(':');
            strBuilder.append(group.visibleStartIndex).append(':');
            strBuilder.append(group.visibleStartPosition).append(':');
            strBuilder.append(group.originalSpan).append(':');
            strBuilder.append(group.visibleSpan).append(':');

            strBuilder.append(group.collapsed ? "collapsed" : "expanded"); //$NON-NLS-1$ //$NON-NLS-2$
            strBuilder.append(':');

            strBuilder.append(group.collapseable ? "collapseable" : "uncollapseable"); //$NON-NLS-1$ //$NON-NLS-2$
            strBuilder.append(':');

            strBuilder.append(group.unbreakable ? "unbreakable" : "breakable"); //$NON-NLS-1$ //$NON-NLS-2$

            if (!group.staticIndexes.isEmpty()) {
                strBuilder.append(':').append(group.staticIndexes.toSortedList().makeString(IPersistable.VALUE_SEPARATOR));
            }

            strBuilder.append('|');
        }

        properties.setProperty(prefix + PERSISTENCE_KEY_GROUP_MODEL, strBuilder.toString());
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        String property = properties.getProperty(prefix + PERSISTENCE_KEY_GROUP_MODEL);
        if (property != null) {
            clear();

            StringTokenizer groupTokenizer = new StringTokenizer(property, "|"); //$NON-NLS-1$
            while (groupTokenizer.hasMoreTokens()) {
                String groupToken = groupTokenizer.nextToken();

                int separatorIndex = groupToken.indexOf('=');

                // group name
                String groupName = groupToken.substring(0, separatorIndex);

                String[] groupProperties = groupToken.substring(separatorIndex + 1).split(":"); //$NON-NLS-1$

                String state = groupProperties[0];
                int startIndex = Integer.parseInt(state);

                state = groupProperties[1];
                int visibleStartIndex = Integer.parseInt(state);

                state = groupProperties[2];
                int visibleStartPosition = Integer.parseInt(state);

                state = groupProperties[3];
                int originalSpan = Integer.parseInt(state);

                state = groupProperties[4];
                int visibleSpan = Integer.parseInt(state);

                Group group = new Group(groupName, startIndex, originalSpan);
                this.groups.add(group);

                group.visibleStartIndex = visibleStartIndex;
                group.visibleStartPosition = visibleStartPosition;
                group.visibleSpan = visibleSpan;

                // Expanded/collapsed
                state = groupProperties[5];
                if ("collapsed".equals(state)) { //$NON-NLS-1$
                    group.collapsed = true;
                } else if ("expanded".equals(state)) { //$NON-NLS-1$
                    group.collapsed = false;
                } else {
                    throw new IllegalArgumentException(state + " not one of 'expanded' or 'collapsed'"); //$NON-NLS-1$
                }

                // collapseble / uncollapseable
                state = groupProperties[6];
                if ("collapseable".equals(state)) { //$NON-NLS-1$
                    group.collapseable = true;
                } else if ("uncollapseable".equals(state)) { //$NON-NLS-1$
                    group.collapseable = false;
                } else {
                    throw new IllegalArgumentException(state + " not one of 'uncollapseable' or 'collapseable'"); //$NON-NLS-1$
                }

                // breakable / unbreakable
                state = groupProperties[7];
                if ("breakable".equals(state)) { //$NON-NLS-1$
                    group.unbreakable = false;
                } else if ("unbreakable".equals(state)) { //$NON-NLS-1$
                    group.unbreakable = true;
                } else {
                    throw new IllegalArgumentException(state + " not one of 'breakable' or 'unbreakable'"); //$NON-NLS-1$
                }

                if (groupProperties.length == 9) {
                    String statics = groupProperties[8];
                    StringTokenizer staticTokenizer = new StringTokenizer(statics, ","); //$NON-NLS-1$
                    while (staticTokenizer.hasMoreTokens()) {
                        int index = Integer.parseInt(staticTokenizer.nextToken());
                        group.staticIndexes.add(index);
                    }
                }
            }
        }
    }

    /**
     * Adds the given positions to the given group.
     *
     * @param group
     *            The {@link Group} to which the positions should be added.
     * @param positions
     *            The positions to add.
     */
    public void addPositionsToGroup(Group group, int... positions) {
        if (group != null && !group.isUnbreakable()) {
            // first sort the positions to ensure we check in the correct order
            Arrays.sort(positions);

            // separate position arrays by start position
            MutableIntList beforeStartPosition = IntLists.mutable.empty();
            MutableIntList afterStartPosition = IntLists.mutable.empty();
            for (int pos : positions) {
                if (pos < group.getVisibleStartPosition()) {
                    beforeStartPosition.add(pos);
                } else {
                    afterStartPosition.add(pos);
                }
            }

            // iterate backwards before start position
            for (int i = beforeStartPosition.size() - 1; i >= 0; i--) {
                int pos = beforeStartPosition.get(i);

                // check if position is directly before start
                // if there is a gap stop processing
                if (pos == group.getVisibleStartPosition() - 1) {
                    group.setOriginalSpan(group.getOriginalSpan() + 1);
                    group.setVisibleSpan(group.getVisibleSpan() + 1);

                    int index = getIndexByPosition(pos);

                    // add index to group members
                    group.members.add(index);

                    group.setStartIndex(index);
                    group.setVisibleStartIndex(index);

                    group.updateVisibleStartPosition();
                } else {
                    break;
                }
            }

            // iterate forward after group end
            for (int pos : afterStartPosition.toArray()) {
                int nextPos = group.getVisibleStartPosition() + group.getVisibleSpan();
                // check for gap
                if (pos == nextPos) {
                    group.setOriginalSpan(group.getOriginalSpan() + 1);
                    group.setVisibleSpan(group.getVisibleSpan() + 1);

                    // add index to group members
                    group.members.add(getIndexByPosition(nextPos));
                } else {
                    // there is a gap so we break and do not update
                    break;
                }
            }
        }
    }

    /**
     * Removes the given positions from the given group.
     * <p>
     * <b>Note:</b><br>
     * A removal will only happen for positions at the beginning or the end of a
     * group. Removing a position in the middle will cause removal of positions
     * at the end of the group to avoid splitting a group.
     * </p>
     * <p>
     * <b>Note:</b><br>
     * A removal does only work for visible positions. That means removing
     * something from a collapsed group does not work.
     * </p>
     *
     * @param group
     *            The {@link Group} from which the positions should be removed.
     * @param positions
     *            The positions to remove.
     */
    public void removePositionsFromGroup(Group group, int... positions) {
        if (group != null && !group.isUnbreakable()) {
            Arrays.sort(positions);
            IntSet visiblePositions = IntSets.immutable.of(group.getVisiblePositions());
            for (int i = positions.length - 1; i >= 0; i--) {
                int pos = positions[i];
                if (visiblePositions.contains(pos)) {
                    int index = getIndexByPosition(pos);
                    if (index == group.getStartIndex()) {
                        // the start index was removed, we need to update the
                        // start index
                        group.setStartIndex(getIndexByPosition(pos + 1));
                        group.members.remove(index);
                        group.staticIndexes.remove(index);
                    } else {
                        int memberIndex = getIndexByPosition(group.getVisibleStartPosition() + group.getVisibleSpan() - 1);
                        group.members.remove(memberIndex);
                        group.staticIndexes.remove(memberIndex);
                    }

                    group.setOriginalSpan(group.getOriginalSpan() - 1);
                    group.setVisibleSpan(group.getVisibleSpan() - 1);

                    // the visible start index was removed, we need to update
                    if (index == group.getVisibleStartIndex()) {
                        if (group.getOriginalSpan() > 0) {
                            group.setVisibleStartIndex(getIndexByPosition(pos + 1));
                        } else {
                            // all members where removed
                            group.setStartIndex(-1);
                            group.setVisibleStartIndex(-1);
                            removeGroup(group);
                        }
                    }

                    group.updateVisibleStartPosition();
                }
            }
        }
    }

    /**
     * Removes the given positions from corresponding groups. Searches the
     * groups by position and removes the position in case the group is not
     * unbreakable.
     * <p>
     * <b>Note:</b><br>
     * A removal will only happen for positions at the beginning or the end of a
     * group. Removing a position in the middle will cause removal of positions
     * at the end of the group to avoid splitting a group.
     * </p>
     * <p>
     * <b>Note:</b><br>
     * A removal does only work for visible positions. That means removing
     * something from a collapsed group does not work.
     * </p>
     *
     * @param positions
     *            The positions to remove.
     * @return The collection of {@link Group}s that have been modified.
     */
    public Collection<Group> removePositionsFromGroup(int... positions) {
        Set<Group> changed = new HashSet<Group>();
        Group group = null;
        Arrays.sort(positions);
        for (int i = positions.length - 1; i >= 0; i--) {
            int pos = positions[i];
            group = getGroupByPosition(pos);
            if (group != null && !group.isUnbreakable()) {
                int index = getIndexByPosition(pos);
                if (index == group.getStartIndex()) {
                    // the start index was removed, we need to update the
                    // start index
                    group.setStartIndex(getIndexByPosition(pos + 1));
                    group.members.remove(index);
                    group.staticIndexes.remove(index);
                } else {
                    int memberIndex = getIndexByPosition(group.getVisibleStartPosition() + group.getVisibleSpan() - 1);
                    group.members.remove(memberIndex);
                    group.staticIndexes.remove(memberIndex);
                }

                group.setOriginalSpan(group.getOriginalSpan() - 1);
                group.setVisibleSpan(group.getVisibleSpan() - 1);

                // the visible start index was removed, we need to update
                if (index == group.getVisibleStartIndex()) {
                    if (group.getOriginalSpan() > 0) {
                        group.setVisibleStartIndex(getIndexByPosition(pos + 1));
                    } else {
                        // all members where removed
                        group.setStartIndex(-1);
                        group.setVisibleStartIndex(-1);
                        removeGroup(group);
                    }
                }

                group.updateVisibleStartPosition();
                changed.add(group);
            }
        }

        return changed;
    }

    /**
     * This method will add static indexes to the given group.
     * <p>
     * Static indexes remain visible when a group is collapsed.
     * </p>
     *
     * @param groupName
     *            The name of the group on which the static indexes should be
     *            inserted.
     * @param indexes
     *            The static indexes to add.
     */
    public void addStaticIndexesToGroup(String groupName, int... indexes) {
        Group group = getGroupByName(groupName);
        if (group != null) {
            addStaticIndexesToGroup(group, indexes);
        }
    }

    /**
     * This method will add static indexes to the given group.
     * <p>
     * Static indexes remain visible when a group is collapsed.
     * </p>
     *
     * @param position
     *            The position of a group on which the static indexes should be
     *            inserted.
     * @param indexes
     *            The static indexes to add.
     */
    public void addStaticIndexesToGroup(int position, int... indexes) {
        Group group = getGroupByPosition(position);
        if (group != null) {
            addStaticIndexesToGroup(group, indexes);
        }
    }

    /**
     * This method will add static indexes to the given group.
     * <p>
     * Static indexes remain visible when a group is collapsed.
     * </p>
     *
     * @param group
     *            The group on which the static indexes should be inserted.
     * @param indexes
     *            The static indexes to add.
     */
    public void addStaticIndexesToGroup(Group group, int... indexes) {

        int[] staticIndexes = Arrays.stream(indexes)
                .map(this::getPositionByIndex)
                .filter(pos -> (pos >= group.getVisibleStartPosition()
                        && pos < (group.getVisibleStartPosition() + group.getVisibleSpan())))
                .toArray();

        if (staticIndexes.length > 0) {
            group.staticIndexes.addAll(staticIndexes);
        }
    }

    // Getters

    /**
     *
     * @param groupName
     *            The name of the requested group.
     * @return The group with the given group name or <code>null</code> if there
     *         is no group with such a name.
     */
    public Group getGroupByName(String groupName) {
        for (Group group : this.groups) {
            if (group.getName().equals(groupName)) {
                return group;
            }
        }

        return null;
    }

    /**
     * Checks if the given position is part of a group and returns the group if
     * the position is part of it.
     *
     * @param position
     *            The position to check.
     * @return The Group to which the given position belongs to or
     *         <code>null</code> if the position is not part of a group.
     */
    public Group getGroupByPosition(int position) {
        for (Group group : this.groups) {
            // first check the visible start position of the group
            if (position == group.getVisibleStartPosition()
                    || (position >= group.getVisibleStartPosition() && position < (group.getVisibleStartPosition() + group.getVisibleSpan()))) {
                return group;
            }
        }
        return null;
    }

    /**
     * Checks if there is a group that has the given index as static index.
     * <p>
     * <b>Note:</b> This method iterates over all groups and checks if the
     * static index collection contains the given index. Could have a bad
     * performance in case of huge groups.
     * </p>
     *
     * @param staticIndex
     *            The index to check.
     * @return The Group in which the given index is configured as static index
     *         or <code>null</code> if the index is not a static index in any
     *         group.
     */
    public Group getGroupByStaticIndex(int staticIndex) {
        for (Group group : this.groups) {
            if (group.staticIndexes.contains(staticIndex)) {
                return group;
            }
        }
        return null;
    }

    /**
     * Searches for a group that has a given member index.
     * <p>
     * <b>Note:</b> This method iterates over all groups and checks if the
     * member collection contains the given index. Could have a bad performance
     * in case of huge groups.
     * </p>
     *
     * @param memberIndex
     *            The index to check.
     * @return The Group that contains the given index or <code>null</code> if
     *         the index is not a member in any group.
     */
    public Group findGroupByMemberIndex(int memberIndex) {
        for (Group group : this.groups) {
            if (group.hasMember(memberIndex)) {
                return group;
            }
        }
        return null;
    }

    /**
     * Check if the given position is part of a group in this
     * {@link GroupModel}.
     *
     * @param position
     *            The position to check.
     * @return <code>true</code> if the position is part of a group,
     *         <code>false</code> if not.
     */
    public boolean isPartOfAGroup(int position) {
        Group group = getGroupByPosition(position);
        return group != null;
    }

    /**
     * Creates and adds a group.
     *
     * @param groupName
     *            The name of the group. Typically used as value in the cell.
     * @param startIndex
     *            The index of the first item in the group.
     * @param span
     *            The configured number of items that belong to this group.
     */
    public void addGroup(String groupName, int startIndex, int span) {
        Group group = new Group(groupName, startIndex, span);
        group.collapseable = this.defaultCollapseable;
        group.unbreakable = this.defaultUnbreakable;
        addGroup(group);
    }

    /**
     * Adds the given group.
     *
     * @param group
     *            The group to add.
     */
    public void addGroup(Group group) {
        this.groups.add(group);
    }

    /**
     * Removes the group identified by the given name.
     *
     * @param groupName
     *            The name of the group to remove.
     * @return The group that was removed from the model.
     */
    public Group removeGroup(String groupName) {
        Group group = getGroupByName(groupName);
        if (group != null) {
            removeGroup(group);
        }
        return group;
    }

    /**
     * Removes the group identified by the given position.
     *
     * @param position
     *            The group that contains the given position.
     * @return The group that was removed from the model.
     */
    public Group removeGroup(int position) {
        Group group = getGroupByPosition(position);
        if (group != null) {
            removeGroup(group);
        }
        return group;
    }

    /**
     * Removes the given group.
     *
     * @param group
     *            The group to remove.
     */
    public void removeGroup(Group group) {
        this.groups.remove(group);
    }

    /**
     * Removes all groups from this {@link GroupModel}.
     */
    public void clear() {
        this.groups.clear();
    }

    /**
     * @return Number of {@link Group}s configured in this {@link GroupModel}.
     */
    public int size() {
        return this.groups.size();
    }

    /**
     * @return <code>true</code> if no group is configured in this
     *         {@link GroupModel}.
     */
    public boolean isEmpty() {
        return this.groups.size() == 0;
    }

    /**
     * Checks if the given position is configured to be static in one of the
     * groups.
     *
     * @param position
     *            The position to check.
     * @return <code>true</code> if the given position is configured to be
     *         static in a group.
     */
    public boolean isStatic(int position) {
        Group group = getGroupByPosition(position);
        if (group != null) {
            return group.staticIndexes.contains(getIndexByPosition(position));
        }
        return false;
    }

    /**
     * Check if the specified position belongs to a {@link Group} and if this
     * {@link Group} is collabseable.
     *
     * @param position
     *            The position used to retrieve the corresponding group.
     * @return <code>true</code> if the specified position belongs to a
     *         {@link Group} and this {@link Group} is collabseable,
     *         <code>false</code> if not.
     */
    public boolean isPartOfACollapseableGroup(int position) {
        Group group = getGroupByPosition(position);
        if (group != null) {
            return group.isCollapseable();
        }
        return false;
    }

    /**
     * Set the {@link Group} with the given group name to be collapseable or
     * not.
     *
     * @param groupName
     *            The name of the group that should be modified.
     * @param collabseable
     *            <code>true</code> to set the group collapseable,
     *            <code>false</code> to set it not to be collapseable.
     */
    public void setGroupCollapseable(String groupName, boolean collabseable) {
        Group group = getGroupByName(groupName);
        if (group != null) {
            group.setCollapseable(collabseable);
        }
    }

    /**
     * Set the {@link Group} to which the specified position belongs to, to be
     * collapseable or not.
     *
     * @param position
     *            The position used to retrieve the corresponding group.
     * @param collabseable
     *            <code>true</code> to set the group collapseable,
     *            <code>false</code> to set it not to be collapseable.
     */
    public void setGroupCollapseable(int position, boolean collabseable) {
        Group group = getGroupByPosition(position);
        if (group != null) {
            group.setCollapseable(collabseable);
        }
    }

    /**
     * Check if the specified position belongs to a {@link Group} and if this
     * {@link Group} is unbreakable.
     *
     * @param position
     *            The position used to retrieve the corresponding group.
     * @return <code>true</code> if the specified position belongs to a
     *         {@link Group} and this {@link Group} is unbreakable,
     *         <code>false</code> if not.
     */
    public boolean isPartOfAnUnbreakableGroup(int position) {
        Group group = getGroupByPosition(position);
        if (group != null) {
            return group.isUnbreakable();
        }
        return false;
    }

    /**
     * Set the group with the given name to unbreakable/breakable.
     *
     * @param groupName
     *            The name of the group that should be modified.
     * @param unbreakable
     *            <code>true</code> to set the group unbreakable,
     *            <code>false</code> to remove the unbreakable state.
     */
    public void setGroupUnbreakable(String groupName, boolean unbreakable) {
        Group group = getGroupByName(groupName);
        if (group != null) {
            group.setUnbreakable(unbreakable);
        }
    }

    /**
     * Set the {@link Group} to which the position belongs to
     * unbreakable/breakable.
     *
     * @param position
     *            The position used to retrieve the corresponding group.
     * @param unbreakable
     *            <code>true</code> to set the group unbreakable,
     *            <code>false</code> to remove the unbreakable state.
     */
    public void setGroupUnbreakable(int position, boolean unbreakable) {
        Group group = getGroupByPosition(position);
        if (group != null) {
            group.setUnbreakable(unbreakable);
        }
    }

    /**
     *
     * @return The default value for the collapseable flag of newly created
     *         {@link Group} objects.
     */
    public boolean isDefaultCollapseable() {
        return this.defaultCollapseable;
    }

    /**
     * Sets the default value for the collapseable flag when creating
     * {@link Group} objects.
     *
     * @param defaultCollapseable
     *            the default value for {@link Group#collapseable} that should
     *            be set on creating {@link Group}.
     */
    public void setDefaultCollapseable(boolean defaultCollapseable) {
        this.defaultCollapseable = defaultCollapseable;
    }

    /**
     *
     * @return The default value for the unbreakable flag of newly created
     *         {@link Group} objects.
     */
    public boolean isDefaultUnbreakable() {
        return this.defaultUnbreakable;
    }

    /**
     * Sets the default value for the unbreakable flag when creating
     * {@link Group} objects.
     *
     * @param defaultUnbreakable
     *            the default value for {@link Group#unbreakable} that should be
     *            set on creating {@link Group}.
     */
    public void setDefaultUnbreakable(boolean defaultUnbreakable) {
        this.defaultUnbreakable = defaultUnbreakable;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Group Model:\n"); //$NON-NLS-1$

        for (Group group : this.groups) {
            builder.append(group);
        }
        return builder.toString();
    }

    /**
     * Model class to track the states of a group. Only carries the state, does
     * not contain logic with regards to changing the states.
     */
    public class Group {

        /**
         * The name of the group. Typically used as value in the cell.
         */
        private String name;

        /**
         * The index of the first item in the group.
         */
        private int startIndex;

        /**
         * The index of the first visible item in the group. Could differ from
         * {@link #startIndex} if that item is hidden.
         */
        private int visibleStartIndex;

        /**
         * The position of the first visible item in the group matching the
         * position layer of the {@link GroupModel}. Needed in case the first
         * column of a group is hidden for example.
         */
        private int visibleStartPosition;

        /**
         * The configured number of items that belong to this group.
         */
        private int originalSpan;

        /**
         * The number of items that are currently visible in this group. Might
         * differ from the {@link #originalSpan} if columns/rows are hidden.
         */
        private int visibleSpan;

        /**
         * The indexes that remain visible when collapsing this group.
         */
        private final MutableIntSet staticIndexes = IntSets.mutable.empty();

        /**
         * Flag to configure whether this group can be collapsed or not.
         */
        private boolean collapseable = true;

        /**
         * Flag that indicates whether this group is collapsed or not.
         */
        private boolean collapsed = false;

        /**
         * Flag to configure whether this group can be broken or not. If a group
         * is marked as unbreakable, the composition of the group cannot be
         * changed. This means the originalSpan cannot be changed by adding or
         * removing items. Items can be reordered within the group.
         */
        private boolean unbreakable = false;

        /**
         * Indexes of the members of this group. Only for internal use in case
         * hide operations performed at the end of a table lead to an
         * inconsistent group state.
         */
        private final MutableIntSet members = IntSets.mutable.empty();

        /**
         *
         * @param groupName
         *            The name of the group. Typically used as value in the
         *            cell.
         * @param startIndex
         *            The index of the first item in the group.
         * @param span
         *            The configured number of items that belong to this group.
         */
        Group(String groupName, int startIndex, int span) {
            this.name = groupName;
            this.startIndex = startIndex;
            this.visibleStartIndex = startIndex;
            this.visibleStartPosition = getPositionByIndex(startIndex);
            this.originalSpan = span;
            this.visibleSpan = span;

            for (int pos = this.visibleStartPosition; pos < this.visibleStartPosition + this.visibleSpan; pos++) {
                this.members.add(getIndexByPosition(pos));
            }
        }

        /**
         * Returns the name of this group. Typically this value is used as value
         * in the cell.
         *
         * @return The name of this group.
         */
        public String getName() {
            return this.name;
        }

        /**
         * Set the name of this group. The value is typically used as value in
         * the cell.
         *
         * @param name
         *            The name of the group.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Returns whether the group is collapsed or not.
         *
         * @return <code>true</code> if this group is collapsed,
         *         <code>false</code> if it is expanded.
         */
        public boolean isCollapsed() {
            return this.collapsed;
        }

        /**
         * Set the collapsed state of this group.
         *
         * @param collapsed
         *            <code>true</code> to set the group in the collapsed state,
         *            <code>false</code> to set it to the expanded state.
         */
        public void setCollapsed(boolean collapsed) {
            if (this.collapseable) {
                this.collapsed = collapsed;
            }
        }

        /**
         * Toggles the collapsed state.
         */
        public void toggleCollapsed() {
            setCollapsed(!this.collapsed);
        }

        /**
         *
         * @return <code>true</code> if this {@link Group} can be collapsed,
         *         <code>false</code> if not.
         */
        public boolean isCollapseable() {
            return this.collapseable;
        }

        /**
         * Configure this {@link Group} whether it can be collapsed or not.
         *
         * @param collapseable
         *            <code>true</code> if this {@link Group} can be collapsed,
         *            <code>false</code> if not.
         */
        public void setCollapseable(boolean collapseable) {
            this.collapseable = collapseable;
            if (!this.collapseable && this.isCollapsed()) {
                // expand if the group is set to be not collapseable but
                // currently collapsed
                this.collapsed = false;
            }
        }

        /**
         *
         * @return <code>true</code> if this {@link Group} can not be changed.
         *         <code>false</code> if the number of items in this
         *         {@link Group} can be modified.
         */
        public boolean isUnbreakable() {
            return this.unbreakable;
        }

        /**
         *
         * @param unbreakable
         *            <code>true</code> if this {@link Group} should not be
         *            changeable. <code>false</code> if the number of items in
         *            this {@link Group} should be changeable.
         */
        public void setUnbreakable(boolean unbreakable) {
            this.unbreakable = unbreakable;
        }

        /**
         *
         * @return The index of the first item in the group.
         */
        public int getStartIndex() {
            return this.startIndex;
        }

        /**
         * Sets the index of the first item in the group. Needed in case of
         * reordering or if the first item is removed from the group.
         *
         * @param startIndex
         *            The index of the first item in the group.
         */
        public void setStartIndex(int startIndex) {
            this.startIndex = startIndex;
        }

        /**
         *
         * @return The index of the first visible item in the group. Could
         *         differ from {@link #startIndex} if that item is hidden.
         */
        public int getVisibleStartIndex() {
            return this.visibleStartIndex;
        }

        /**
         * Sets the index of the first visible item in the group. Needed in case
         * the first real item in the group is hidden.
         *
         * @param visibleStartIndex
         *            The index of the first visible item in the group.
         */
        public void setVisibleStartIndex(int visibleStartIndex) {
            this.visibleStartIndex = visibleStartIndex;
        }

        /**
         *
         * @return The position of the first visible item in the group matching
         *         the position layer of the GroupModel. Needed in case the
         *         first column of a group is hidden for example.
         */
        public int getVisibleStartPosition() {
            return this.visibleStartPosition;
        }

        /**
         * Updates the visible start position based on the currently set visible
         * start index.
         */
        void updateVisibleStartPosition() {
            int startPosition = getPositionByIndex(this.startIndex);
            if (startPosition >= 0) {
                this.visibleStartIndex = this.startIndex;
            }
            this.visibleStartPosition = getPositionByIndex(this.visibleStartIndex);

            if (this.visibleStartPosition == -1) {
                // if a multi hide command was triggered for non-contiguous
                // column ranges, where one range is at the end, the group could
                // be in an inconsistent state which needs to be corrected.
                consistencyCheck(false);
            }
        }

        /**
         * Triggers a consistency check on the group based on the locally stored
         * group member indexes. Needed in case events where not processed in
         * the corresponding header layer because of position transformations,
         * which then lead to an inconsistent group state.
         *
         * @param updateStartIndex
         *            flag to indicate if the start index of the group should
         *            also be updated. Needed in case of complete structural
         *            refreshes to be able to reset the group completely.
         */
        void consistencyCheck(boolean updateStartIndex) {
            // check if the member indexes are all visible
            MutableIntList memberPositions = this.members
                    .collectInt(member -> getPositionByIndex(member), IntLists.mutable.empty());

            int hidden = memberPositions.count(pos -> pos == -1);
            int smallestPosition = memberPositions.select(pos -> pos >= 0).minIfEmpty(-1);

            setVisibleSpan(this.originalSpan - hidden);

            int smallestIndex = getIndexByPosition(smallestPosition);
            if (updateStartIndex
                    || (this.startIndex < 0 && smallestIndex >= 0)) {
                setStartIndex(smallestIndex);
            }
            setVisibleStartIndex(smallestIndex);
            this.visibleStartPosition = smallestPosition;
        }

        /**
         *
         * @return The indexes of the members in this collection. Not modifiable
         *         to avoid side effects from the outside of the GroupModel.
         */
        int[] getMembers() {
            return this.members.toSortedArray();
        }

        /**
         * Adds the given member indexes to the local list of members that are
         * needed for consistency checks.
         *
         * @param memberIndexes
         *            The indexes of the positions that should be added to the
         *            local group members.
         */
        void addMembers(int... memberIndexes) {
            this.members.addAll(memberIndexes);
        }

        /**
         * Removes the given member indexes from the local list of members that
         * are needed for consistency checks.
         *
         * @param memberIndexes
         *            The indexes of the positions that should be removed from
         *            the local group members.
         */
        void removeMembers(int... memberIndexes) {
            this.members.removeAll(memberIndexes);
        }

        /**
         * Check if the given index is a member of this {@link Group} or not.
         *
         * @param memberIndex
         *            The index to check.
         * @return <code>true</code> if the given index is a member of this
         *         {@link Group}, <code>false</code> if not.
         */
        public boolean hasMember(int memberIndex) {
            return this.members.contains(memberIndex);
        }

        /**
         *
         * @return The configured number of items that belong to this group.
         */
        public int getOriginalSpan() {
            return this.originalSpan;
        }

        /**
         *
         * @param originalSpan
         *            The number of items that should belong to this group.
         */
        public void setOriginalSpan(int originalSpan) {
            this.originalSpan = originalSpan;
        }

        /**
         *
         * @return The number of items that are currently visible in this
         *         {@link Group}. Might differ from the {@link #originalSpan} if
         *         columns/rows are hidden.
         */
        public int getVisibleSpan() {
            return this.visibleSpan;
        }

        /**
         *
         * @param visibleSpan
         *            The number of items that are currently visible in this
         *            {@link Group}. Cannot be bigger than the original span.
         */
        public void setVisibleSpan(int visibleSpan) {
            if (visibleSpan <= this.originalSpan) {
                this.visibleSpan = visibleSpan;
            }
        }

        /**
         *
         * @return <code>true</code> if this {@link Group} has no spanning,
         *         <code>false</code> if at least one position is part of this
         *         {@link Group}.
         */
        public boolean isEmpty() {
            return this.originalSpan == 0;
        }

        /**
         * Adds the given indexes as static indexes to this group. Static
         * indexes are the indexes that stay visible when the group is
         * collapsed.
         *
         * @param indexes
         *            The static indexes to add.
         */
        public void addStaticIndexes(int... indexes) {
            this.staticIndexes.addAll(indexes);
        }

        /**
         * Removes the given indexes as static indexes from this group.
         *
         * @param indexes
         *            The static indexes to remove.
         */
        public void removeStaticIndexes(int... indexes) {
            this.staticIndexes.removeAll(indexes);
        }

        /**
         * Checks if the given index is a static index of this group.
         *
         * @param index
         *            The index to check.
         * @return <code>true</code> if the given index is configured as static
         *         index of this group, <code>false</code> if not.
         * @since 2.0
         */
        public boolean containsStaticIndex(int index) {
            return this.staticIndexes.contains(index);
        }

        /**
         * @return The indexes that remain visible when collapsing this group.
         * @since 2.0
         */
        public int[] getStaticIndexes() {
            return this.staticIndexes.toSortedArray();
        }

        /**
         *
         * @return The positions of the visible items in the group matching the
         *         position layer of the GroupModel.
         * @since 2.0
         */
        public int[] getVisiblePositions() {
            int[] groupPositions = new int[this.visibleSpan];
            int i = 0;
            for (int pos = this.visibleStartPosition; pos < (this.visibleStartPosition + this.visibleSpan); pos++) {
                groupPositions[i] = pos;
                i++;
            }
            return groupPositions;
        }

        /**
         *
         * @return The indexes of the positions that are currently visible.
         * @since 2.0
         */
        public int[] getVisibleIndexes() {
            int[] groupIndexes = new int[this.visibleSpan];
            int i = 0;
            for (int pos = this.visibleStartPosition; pos < (this.visibleStartPosition + this.visibleSpan); pos++) {
                groupIndexes[i] = getIndexByPosition(pos);
                i++;
            }
            return groupIndexes;
        }

        /**
         * Searches for the position of the last item in this group for the
         * given layer. Needed as the Group positions are based on the
         * positionLayer, but on transporting commands down the layer stack the
         * positions might need to be converted, e.g. to find the last item even
         * if it is hidden.
         *
         * @param layer
         *            The layer needed for index-position-transformation.
         * @return The position of the last item in this group.
         *
         * @since 2.0
         */
        public int getGroupEndPosition(IUniqueIndexLayer layer) {
            return this.members
                    .collectInt(layer::getColumnPositionByIndex, IntSets.mutable.empty())
                    .maxIfEmpty(-1);
        }

        /**
         * Checks if the given position is the left/top most position of this
         * group. This actually means if the given position is the visible start
         * position.
         *
         * @param position
         *            The position to check.
         * @return <code>true</code> if the given position is the left/top most
         *         position of this group, <code>false</code> if not.
         */
        public boolean isGroupStart(int position) {
            return position == this.visibleStartPosition;
        }

        /**
         * Checks if the given position is the right/bottom most position of
         * this group. This actually means if the given position is the visible
         * start position + visible span.
         *
         * @param position
         *            The position to check.
         * @return <code>true</code> if the given position is the right/bottom
         *         most position of this group, <code>false</code> if not.
         */
        public boolean isGroupEnd(int position) {
            return (this.visibleStartPosition + this.visibleSpan - 1) == position;
        }

        @Override
        public String toString() {
            return "Group:\n\t name: " + this.name //$NON-NLS-1$
                    + "\n\t startIndex: " + this.startIndex //$NON-NLS-1$
                    + "\n\t visibleStartIndex: " + this.visibleStartIndex //$NON-NLS-1$
                    + "\n\t visibleStartPosition: " + this.visibleStartPosition //$NON-NLS-1$
                    + "\n\t originalSpan: " + this.originalSpan //$NON-NLS-1$
                    + "\n\t visibleSpan: " + this.visibleSpan //$NON-NLS-1$
                    + "\n\t collapseable: " + this.collapseable //$NON-NLS-1$
                    + "\n\t collapsed: " + this.collapsed //$NON-NLS-1$
                    + "\n\t unbreakable: " + this.unbreakable //$NON-NLS-1$
                    + "\n\t staticIndexes: [ " + this.staticIndexes.makeString(", ") + " ]\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    /**
     * Interface to support layer based position-index conversion.
     */
    public interface IndexPositionConverter {

        /**
         * Convert the given position on the given position layer to the
         * corresponding index.
         *
         * @param position
         *            The position to convert.
         * @return The index for the given position.
         *
         * @since 2.0
         */
        int convertPositionToIndex(int position);

        /**
         * Convert the given index to the corresponding position on the given
         * position layer.
         *
         * @param index
         *            The index to convert.
         * @return The position on the position layer for the given index.
         *
         * @since 2.0
         */
        int convertIndexToPosition(int index);

    }
}

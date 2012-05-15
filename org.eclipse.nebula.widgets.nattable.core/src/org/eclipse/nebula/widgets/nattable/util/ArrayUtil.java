/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArrayUtil {

	public static final String[] STRING_TYPE_ARRAY = new String[] {};
	public static final int[] INT_TYPE_ARRAY = new int[] {};

	public static <T> List<T> asList(T[] array) {
		return new ArrayList<T>(ArrayUtil.asCollection(array));
	}

	public static <T> Collection<T> asCollection(T[] array) {
		List<T> list = new ArrayList<T>();
		for (int i = 0; i < array.length; i++) {
			list.add(array[i]);
		}
		return list;
	}

	public static int[] asIntArray(int... ints) {
		return ints;
	}

	public static List<Integer> asIntegerList(int... ints) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (Integer integer : ints) {
			list.add(integer);
		}
		return list;
	}

	public static boolean isEmpty(int[] array){
		return (array == null) || (array.length == 0);
	}

	public static boolean isEmpty(String[] array){
		return (array == null) || (array.length == 0);
	}

	public static boolean isNotEmpty(int[] array){
		return !isEmpty(array);
	}

	public static boolean isNotEmpty(String[] array){
		return !isEmpty(array);
	}

	public static int[] asIntArray(List<Integer> list) {
		int[] ints = new int[list.size()];
		int i = 0;
		for (int fromSet : list) {
			ints[i] = fromSet;
			i++;
		}
		return ints;
	}

}

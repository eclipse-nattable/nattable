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
package org.eclipse.nebula.widgets.nattable.dataset.generator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.nebula.widgets.nattable.dataset.valuegenerator.DoubleValueGenerator;
import org.eclipse.nebula.widgets.nattable.dataset.valuegenerator.ListValueGenerator;



public class DataGenerator<T> {

	private static final Random random = new Random();
	private static final Lock lock = new ReentrantLock();
	
	public T generate(Class<T> dataClass) throws GeneratorException {
		
		try {
			T dataContainer = dataClass.newInstance();
			final ValueGeneratorFactory generatorFactory = new ValueGeneratorFactory();
			
			for (Field field: dataClass.getDeclaredFields()) {
				
				final IValueGenerator generator = generatorFactory.createValueGenerator(field);
				if (generator != null) {
					setField(dataClass, dataContainer, field, generator);
				}
			}
			
			return dataContainer;
			
		} catch (InstantiationException ie) {
			ie.printStackTrace();
			throw new GeneratorException(ie);
		} catch (IllegalAccessException iae) {
			iae.printStackTrace();
			throw new GeneratorException(iae);
		} catch (SecurityException se) {
			se.printStackTrace();
			throw new GeneratorException(se);
		} catch (NoSuchMethodException nsme) {
			nsme.printStackTrace();
			throw new GeneratorException(nsme);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new GeneratorException(e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new GeneratorException(e);
		}
	}
	
	private void setField(Class<T> dataClass, T dataContainer, Field field, IValueGenerator generator)
				throws IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		lock.lock();
		try {
			Method setter = dataClass.getDeclaredMethod("set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1), field.getType());
			setter.invoke(dataContainer, generator.newValue(random));
		} finally {
			lock.unlock();
		}
	}

	
	private static interface IValueGeneratorFactory {
		IValueGenerator createValueGenerator(Field field) throws InstantiationException, IllegalAccessException;
	}
	
	private static class DataValueGeneratorFactory implements IValueGeneratorFactory {
		public IValueGenerator createValueGenerator(Field field) throws InstantiationException, IllegalAccessException {
			field.setAccessible(true);
			DataValueGenerator annotation = field.getAnnotation(DataValueGenerator.class);
			//Class<? extends IValueGenerator> generatorClass = annotation.value();
			Class<? extends IValueGenerator> generatorClass = annotation.value();
			return generatorClass.newInstance();
		}
	}
	
	private static class DoubleValueGeneratorFactory implements IValueGeneratorFactory {
		public IValueGenerator createValueGenerator(Field field) throws InstantiationException, IllegalAccessException {
			field.setAccessible(true);
			GenerateDouble generateDouble = field.getAnnotation(GenerateDouble.class);
			return new DoubleValueGenerator(generateDouble.floor(), generateDouble.range());
		}
	}
	
	private static class StringListValueGeneratorFactory implements IValueGeneratorFactory {
		public IValueGenerator createValueGenerator(Field field) throws InstantiationException, IllegalAccessException {
			field.setAccessible(true);
			GenerateListOfStrings generateList = field.getAnnotation(GenerateListOfStrings.class);
			return new ListValueGenerator<String>(generateList.nullLoadFactor(), generateList.values());
		}
	}
	
	private static class ValueGeneratorFactory implements IValueGeneratorFactory {
		public IValueGenerator createValueGenerator(Field field) throws InstantiationException, IllegalAccessException {
			if (field.isAnnotationPresent(DataValueGenerator.class)) {
				return new DataValueGeneratorFactory().createValueGenerator(field);
			}
			
			if (field.isAnnotationPresent(GenerateDouble.class)) {
				return new DoubleValueGeneratorFactory().createValueGenerator(field);
			}
			
			if (field.isAnnotationPresent(GenerateListOfStrings.class)) {
				return new StringListValueGeneratorFactory().createValueGenerator(field);
			}
			
			return null;
		}
	}
}

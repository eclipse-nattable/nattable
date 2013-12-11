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
package org.eclipse.nebula.widgets.nattable.test.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.eclipse.nebula.widgets.nattable.test.data.Person.Gender;

/**
 * Class that acts as service for accessing numerous {@link Person}s.
 * The values are randomly put together out of names and places from "The Simpsons"
 * 
 * @author Dirk Fauth
 */
public class PersonService {

	static String[] maleNames = {
		"Bart", 
		"Homer", 
		"Lenny", 
		"Carl", 
		"Waylon", 
		"Ned", 
		"Timothy",
		"Rodd",
		"Todd"};
	static String[] femaleNames = {
		"Marge", 
		"Lisa", 
		"Maggie", 
		"Edna", 
		"Helen", 
		"Jessica",
		"Maude"};
	static String[] lastNames = {
		"Simpson", 
		"Leonard", 
		"Carlson", 
		"Smithers", 
		"Flanders", 
		"Krabappel", 
		"Lovejoy"};

	/**
	 * Creates a list of random {@link Person}s. 
	 * @param numberOfPersons The number of {@link Person}s that should be generated.
	 * @return
	 */
	public static List<Person> getRandomPersons(int numberOfPersons) {
		List<Person> result = new ArrayList<Person>();
		
		for (int i = 0; i < numberOfPersons; i++) {
			result.add(createPerson(i));
		}
		
		return result;
	}

	/**
	 * Creates a fixed list of {@link Person}s. 
	 */
	public static List<Person> getFixedPersons() {
		List<Person> result = new ArrayList<Person>();
		
		//create 10 Simpsons
		//3 Homer
		result.add(new Person(1, maleNames[1], lastNames[0], Gender.MALE, true, new Date()));
		result.add(new Person(2, maleNames[1], lastNames[0], Gender.MALE, true, new Date()));
		result.add(new Person(3, maleNames[1], lastNames[0], Gender.MALE, true, new Date()));
		//3 Bart
		result.add(new Person(4, maleNames[0], lastNames[0], Gender.MALE, false, new Date()));
		result.add(new Person(5, maleNames[0], lastNames[0], Gender.MALE, false, new Date()));
		result.add(new Person(6, maleNames[0], lastNames[0], Gender.MALE, false, new Date()));
		//2 Marge
		result.add(new Person(7, femaleNames[0], lastNames[0], Gender.FEMALE, true, new Date()));
		result.add(new Person(8, femaleNames[0], lastNames[0], Gender.FEMALE, true, new Date()));
		//2 Lisa
		result.add(new Person(9, femaleNames[1], lastNames[0], Gender.FEMALE, false, new Date()));
		result.add(new Person(10, femaleNames[1], lastNames[0], Gender.FEMALE, false, new Date()));
		
		//create 8 Flanders
		//2 Ned
		result.add(new Person(11, maleNames[5], lastNames[4], Gender.MALE, true, new Date()));
		result.add(new Person(12, maleNames[5], lastNames[4], Gender.MALE, true, new Date()));
		//2 Maude
		result.add(new Person(13, femaleNames[6], lastNames[4], Gender.FEMALE, true, new Date()));
		result.add(new Person(14, femaleNames[6], lastNames[4], Gender.FEMALE, true, new Date()));
		//2 Rod
		result.add(new Person(15, maleNames[7], lastNames[4], Gender.MALE, false, new Date()));
		result.add(new Person(16, maleNames[7], lastNames[4], Gender.MALE, false, new Date()));
		//2 Tod
		result.add(new Person(17, maleNames[8], lastNames[4], Gender.MALE, false, new Date()));
		result.add(new Person(18, maleNames[8], lastNames[4], Gender.MALE, false, new Date()));
		
		return result;
	}
	
	public static List<Person> getFixedMixedPersons() {
		List<Person> result = new ArrayList<Person>();
		
		result.add(new Person(21, maleNames[0], lastNames[2], Gender.MALE, true, new Date()));
		result.add(new Person(22, maleNames[1], lastNames[2], Gender.MALE, true, new Date()));
		result.add(new Person(23, maleNames[5], lastNames[2], Gender.MALE, true, new Date()));
		result.add(new Person(24, femaleNames[0], lastNames[2], Gender.FEMALE, false, new Date()));
		result.add(new Person(25, femaleNames[6], lastNames[2], Gender.FEMALE, false, new Date()));
		
		//add doubles
		result.add(new Person(30, maleNames[1], lastNames[0], Gender.MALE, true, new Date()));
		result.add(new Person(31, maleNames[1], lastNames[0], Gender.MALE, true, new Date()));
		result.add(new Person(32, maleNames[1], lastNames[2], Gender.MALE, true, new Date()));
		result.add(new Person(33, maleNames[1], lastNames[2], Gender.MALE, true, new Date()));

		return result;
	}
	
	/**
	 * Creates a random person out of names which are taken from "The Simpsons" 
	 * and enrich them with random generated married state and birthday date.
	 * @return
	 */
	private static Person createPerson(int id) {
		Random randomGenerator = new Random();
		
		Person result = new Person(id);
		result.setGender(Gender.values()[randomGenerator.nextInt(2)]);
		
		if (result.getGender().equals(Gender.MALE)) {
			result.setFirstName(maleNames[randomGenerator.nextInt(maleNames.length)]);
		}
		else {
			result.setFirstName(femaleNames[randomGenerator.nextInt(femaleNames.length)]);
		}
		
		result.setLastName(lastNames[randomGenerator.nextInt(lastNames.length)]);
		result.setMarried(randomGenerator.nextBoolean());
		
		int month = randomGenerator.nextInt(12);
		int day = 0;
		if (month == 2) {
			day = randomGenerator.nextInt(28);
		}
		else {
			day = randomGenerator.nextInt(30);
		}
		int year = 1920 + randomGenerator.nextInt(90);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			result.setBirthday(sdf.parse(""+year+"-"+month+"-"+day));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}

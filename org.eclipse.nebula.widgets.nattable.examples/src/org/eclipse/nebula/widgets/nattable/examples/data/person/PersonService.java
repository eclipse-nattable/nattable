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
package org.eclipse.nebula.widgets.nattable.examples.data.person;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.nebula.widgets.nattable.examples.data.person.Person.Gender;


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
		"Timothy"};
	static String[] femaleNames = {
		"Marge", 
		"Lisa", 
		"Maggie", 
		"Edna", 
		"Helen", 
		"Jessica"};
	static String[] lastNames = {
		"Simpson", 
		"Leonard", 
		"Carlson", 
		"Smithers", 
		"Flanders", 
		"Krabappel", 
		"Lovejoy"};
	static String[] streetNames = new String[] {
		"Evergreen Terrace", 
		"Main Street", 
		"South Street", 
		"Plympton Street", 
		"Highland Avenue", 
		"Elm Street", 
		"Oak Grove Street"};
	static String[] cityNames = new String[] {
		"Springfield", 
		"Shelbyville", 
		"Ogdenville", 
		"Waverly Hills", 
		"North Haverbrook", 
		"Capital City"};
	static String[] foodList = new String[] {
		"Donut",
		"Bacon",
		"Fish",
		"Vegetables",
		"Ham",
		"Prezels"};
	static String[] drinkList = new String[] {
		"Beer",
		"Water",
		"Soda",
		"Milk",
		"Coke",
		"Fizzy Bubblech"};
	static String baseText = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " + 
			"sed diam nonumy eirmod tempor invidunt ut labore et dolore " + 
			"magna aliquyam erat, sed diam voluptua.";

	/**
	 * Creates a list of {@link Person}s. 
	 * @param numberOfPersons The number of {@link Person}s that should be generated.
	 * @return
	 */
	public static List<Person> getPersons(int numberOfPersons) {
		List<Person> result = new ArrayList<Person>();
		
		for (int i = 0; i < numberOfPersons; i++) {
			result.add(createPerson(i));
		}
		
		return result;
	}
	
	/**
	 * Creates a list of {@link PersonWithAddress}. 
	 * @param numberOfPersons The number of {@link PersonWithAddress} that should be generated.
	 * @return
	 */
	public static List<PersonWithAddress> getPersonsWithAddress(int numberOfPersons) {
		List<PersonWithAddress> result = new ArrayList<PersonWithAddress>();
		
		for (int i = 0; i < numberOfPersons; i++) {
			result.add(new PersonWithAddress(createPerson(i), createAddress()));
		}
		
		return result;
	}
	
	/**
	 * Creates a list of {@link ExtendedPersonWithAddress}. 
	 * @param numberOfPersons The number of {@link ExtendedPersonWithAddress} that should be generated.
	 * @return
	 */
	public static List<ExtendedPersonWithAddress> getExtendedPersonsWithAddress(int numberOfPersons) {
		List<ExtendedPersonWithAddress> result = new ArrayList<ExtendedPersonWithAddress>();
		
		for (int i = 0; i < numberOfPersons; i++) {
			result.add(new ExtendedPersonWithAddress(createPerson(i), createAddress(), 
					generateSimplePassword(), createRandomLengthText(), createRandomMoneyAmount(), 
					createFavouriteFood(), createFavouriteDrinks()));
		}
		
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
		
		SimpleDateFormat sdf = new SimpleDateFormat(DataModelConstants.DATE_FORMAT_PATTERN);
		try {
			result.setBirthday(sdf.parse(""+year+"-"+month+"-"+day));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Creates a random address out of street names, postal codes and city names which are
	 * taken from "The Simpsons" 
	 * (i haven't found postal codes, so here i invented some for the example)
	 * @return
	 */
	private static Address createAddress() {
		String[] streets = getStreetNames();
		int[] plz = {11111, 22222, 33333, 44444, 55555, 66666};
		String[] cities = getCityNames();
		
		Random randomGenerator = new Random();
		
		Address result = new Address();
		
		result.setStreet(streets[randomGenerator.nextInt(streets.length)]);
		result.setHousenumber(randomGenerator.nextInt(200));
		int cityRandom = randomGenerator.nextInt(cities.length);
		result.setPostalCode(plz[cityRandom]);
		result.setCity(cities[cityRandom]);
		
		return result;
	}
	
	/**
	 * @return A simple password consisting of 8 characters in the value ranges a-z, A-Z
	 */
	public static String generateSimplePassword() {
		String result = "";
		for (int i = 0; i < 7; i++) {
			int rnd = (int) (Math.random() * 52);
			char base = (rnd < 26) ? 'A' : 'a';
			result += (char) (base + rnd % 26);
		}
		return result;
	}
	
	
	private static List<String> createFavouriteFood() {
		String[] food = getFoodList();
		Random rand = new Random();
		int favCount = rand.nextInt(food.length);
		
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < favCount; i++) {
			int randIndex = rand.nextInt(food.length);
			if (!result.contains(food[randIndex])) {
				result.add(food[randIndex]);
			}
		}
		return result;
	}
	
	private static List<String> createFavouriteDrinks() {
		String[] drinks = getDrinkList();
		Random rand = new Random();
		int favCount = rand.nextInt(drinks.length);
		
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < favCount; i++) {
			int randIndex = rand.nextInt(drinks.length);
			if (!result.contains(drinks[randIndex])) {
				result.add(drinks[randIndex]);
			}
		}
		return result;
	}
	
	/**
	 * @return An array of street names that are also used to create random addresses.
	 */
	public static String[] getStreetNames() {
		return streetNames;
	}
	
	/**
	 * @return An array of city names that are also used to create random addresses.
	 */
	public static String[] getCityNames() {
		return cityNames;
	}
	
	/**
	 * @return An array of food names.
	 */
	public static String[] getFoodList() {
		return foodList;
	}
	
	/**
	 * @return An array of drink names.
	 */
	public static String[] getDrinkList() {
		return drinkList;
	}
	
	/**
	 * @return A custom length text containing line breaks
	 */
	public static String createRandomLengthText() {
		String[] words = baseText.split(" ");
		
		Random wordRandom = new Random();
		String msg = "";
		int randWords = wordRandom.nextInt(words.length);
		for (int j = 0; j < randWords; j++) {
			msg += words[j];
			if (msg.endsWith(",") || msg.endsWith(".")) {
				msg += "\n";
			} else {
				msg += " ";
			}
		}
		
		return msg;
	}
	
	public static Double createRandomMoneyAmount() {
		Double result = new Random().nextDouble() * 1000;
		BigDecimal bd = new BigDecimal(result);
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}
}

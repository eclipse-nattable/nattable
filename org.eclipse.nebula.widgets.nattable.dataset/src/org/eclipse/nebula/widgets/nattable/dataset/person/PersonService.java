/*******************************************************************************
 * Copyright (c) 2012, 2013, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.dataset.person;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;

/**
 * Class that acts as service for accessing numerous {@link Person}s. The values
 * are randomly put together out of names and places from "The Simpsons"
 */
public class PersonService {

    static String[] maleNames = { "Bart", "Homer", "Lenny", "Carl", "Waylon",
            "Ned", "Timothy", "Rodd", "Todd" };
    static String[] femaleNames = { "Marge", "Lisa", "Maggie", "Edna", "Helen",
            "Jessica", "Maude" };
    static String[] lastNames = { "Simpson", "Leonard", "Carlson", "Smithers",
            "Flanders", "Krabappel", "Lovejoy" };
    static String[] streetNames = new String[] { "Evergreen Terrace",
            "Main Street", "South Street", "Plympton Street",
            "Highland Avenue", "Elm Street", "Oak Grove Street" };
    static String[] cityNames = new String[] { "Springfield", "Shelbyville",
            "Ogdenville", "Waverly Hills", "North Haverbrook", "Capital City" };
    static String[] foodList = new String[] { "Donut", "Bacon", "Fish",
            "Vegetables", "Ham", "Prezels" };
    static String[] drinkList = new String[] { "Beer", "Water", "Soda", "Milk",
            "Coke", "Fizzy Bubblech" };
    static String baseText = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
            + "sed diam nonumy eirmod tempor invidunt ut labore et dolore "
            + "magna aliquyam erat, sed diam voluptua.";

    /**
     * Creates a list of random {@link Person}s.
     *
     * @param numberOfPersons
     *            The number of {@link Person}s that should be generated.
     * @return A list containing the given amount of random generated persons.
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

        // create 10 Simpsons
        // 3 Homer
        result.add(new Person(1, maleNames[1], lastNames[0], Gender.MALE, true,
                new Date(), 100d));
        result.add(new Person(2, maleNames[1], lastNames[0], Gender.MALE, true,
                new Date(), 100d));
        result.add(new Person(3, maleNames[1], lastNames[0], Gender.MALE, true,
                new Date(), 100d));
        // 3 Bart
        result.add(new Person(4, maleNames[0], lastNames[0], Gender.MALE,
                false, new Date(), 100d));
        result.add(new Person(5, maleNames[0], lastNames[0], Gender.MALE,
                false, new Date(), 100d));
        result.add(new Person(6, maleNames[0], lastNames[0], Gender.MALE,
                false, new Date(), 100d));
        // 2 Marge
        result.add(new Person(7, femaleNames[0], lastNames[0], Gender.FEMALE,
                true, new Date(), 100d));
        result.add(new Person(8, femaleNames[0], lastNames[0], Gender.FEMALE,
                true, new Date(), 100d));
        // 2 Lisa
        result.add(new Person(9, femaleNames[1], lastNames[0], Gender.FEMALE,
                false, new Date(), 100d));
        result.add(new Person(10, femaleNames[1], lastNames[0], Gender.FEMALE,
                false, new Date(), 100d));

        // create 8 Flanders
        // 2 Ned
        result.add(new Person(11, maleNames[5], lastNames[4], Gender.MALE,
                true, new Date(), 100d));
        result.add(new Person(12, maleNames[5], lastNames[4], Gender.MALE,
                true, new Date(), 100d));
        // 2 Maude
        result.add(new Person(13, femaleNames[6], lastNames[4], Gender.FEMALE,
                true, new Date(), 100d));
        result.add(new Person(14, femaleNames[6], lastNames[4], Gender.FEMALE,
                true, new Date(), 100d));
        // 2 Rod
        result.add(new Person(15, maleNames[7], lastNames[4], Gender.MALE,
                false, new Date(), 100d));
        result.add(new Person(16, maleNames[7], lastNames[4], Gender.MALE,
                false, new Date(), 100d));
        // 2 Tod
        result.add(new Person(17, maleNames[8], lastNames[4], Gender.MALE,
                false, new Date(), 100d));
        result.add(new Person(18, maleNames[8], lastNames[4], Gender.MALE,
                false, new Date(), 100d));

        return result;
    }

    /**
     * Creates a fixed list of {@link Person}s with a few null values.
     */
    public static List<Person> getFixedPersonsWithNull() {
        List<Person> result = new ArrayList<Person>();

        // create 5 Simpsons
        // 2 Homer
        result.add(new Person(1, maleNames[1], lastNames[0], Gender.MALE, true,
                new Date(), 100d));
        result.add(new Person(3, maleNames[1], lastNames[0], Gender.MALE, true,
                new Date(), 100d));
        // 2 Marge
        result.add(new Person(7, femaleNames[0], lastNames[0], Gender.FEMALE,
                true, new Date(), 100d));
        result.add(new Person(8, femaleNames[0], lastNames[0], Gender.FEMALE,
                true, new Date(), 100d));
        // 1 Bart without money
        result.add(new Person(7, femaleNames[0], lastNames[0], Gender.FEMALE,
                true, new Date(), null));

        // create 2 Flanders without last name
        // 1 Ned
        result.add(new Person(11, maleNames[5], null, Gender.MALE,
                true, new Date(), 100d));
        // 1 Maude
        result.add(new Person(13, femaleNames[6], null, Gender.FEMALE,
                true, new Date(), 100d));

        return result;
    }

    public static List<Person> getFixedMixedPersons() {
        List<Person> result = new ArrayList<Person>();

        result.add(new Person(21, maleNames[0], lastNames[2], Gender.MALE,
                true, new Date(), 100d));
        result.add(new Person(22, maleNames[1], lastNames[2], Gender.MALE,
                true, new Date(), 100d));
        result.add(new Person(23, maleNames[5], lastNames[2], Gender.MALE,
                true, new Date(), 100d));
        result.add(new Person(24, femaleNames[0], lastNames[2], Gender.FEMALE,
                false, new Date(), 100d));
        result.add(new Person(25, femaleNames[6], lastNames[2], Gender.FEMALE,
                false, new Date(), 100d));

        // add doubles
        result.add(new Person(30, maleNames[1], lastNames[0], Gender.MALE,
                true, new Date(), 100d));
        result.add(new Person(31, maleNames[1], lastNames[0], Gender.MALE,
                true, new Date(), 100d));
        result.add(new Person(32, maleNames[1], lastNames[2], Gender.MALE,
                true, new Date(), 100d));
        result.add(new Person(33, maleNames[1], lastNames[2], Gender.MALE,
                true, new Date(), 100d));

        return result;
    }

    /**
     * Creates a list of {@link Person}s.
     *
     * @param numberOfPersons
     *            The number of {@link Person}s that should be generated.
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
     * Creates a list of {@link Address}.
     *
     * @param number
     *            The number of {@link Address} that should be generated.
     * @return
     */
    public static List<Address> getAddress(int number) {
        List<Address> result = new ArrayList<Address>();

        for (int i = 0; i < number; i++) {
            result.add(createAddress());
        }

        return result;
    }

    /**
     * Creates a list of {@link PersonWithAddress}.
     *
     * @param numberOfPersons
     *            The number of {@link PersonWithAddress} that should be
     *            generated.
     * @return
     */
    public static List<PersonWithAddress> getPersonsWithAddress(
            int numberOfPersons) {
        List<PersonWithAddress> result = new ArrayList<PersonWithAddress>();

        for (int i = 0; i < numberOfPersons; i++) {
            result.add(createPersonWithAddress(i));
        }

        return result;
    }

    /**
     * Creates a list of {@link ExtendedPersonWithAddress}.
     *
     * @param numberOfPersons
     *            The number of {@link ExtendedPersonWithAddress} that should be
     *            generated.
     * @return
     */
    public static List<ExtendedPersonWithAddress> getExtendedPersonsWithAddress(
            int numberOfPersons) {
        List<ExtendedPersonWithAddress> result = new ArrayList<ExtendedPersonWithAddress>();

        for (int i = 0; i < numberOfPersons; i++) {
            result.add(createExtendedPersonWithAddress(i));
        }

        return result;
    }

    /**
     * Creates a random person out of names which are taken from "The Simpsons"
     * and enrich them with random generated married state and birthday date.
     *
     * @return
     */
    private static Person createPerson(int id) {
        Random randomGenerator = new Random();

        Person result = new Person(id);
        result.setGender(Gender.values()[randomGenerator.nextInt(2)]);

        if (result.getGender().equals(Gender.MALE)) {
            result.setFirstName(maleNames[randomGenerator.nextInt(maleNames.length)]);
        } else {
            result.setFirstName(femaleNames[randomGenerator.nextInt(femaleNames.length)]);
        }

        result.setLastName(lastNames[randomGenerator.nextInt(lastNames.length)]);
        result.setMarried(randomGenerator.nextBoolean());

        int month = randomGenerator.nextInt(12);
        int day = 0;
        if (month == 2) {
            day = randomGenerator.nextInt(28);
        } else {
            day = randomGenerator.nextInt(30);
        }
        int year = 1920 + randomGenerator.nextInt(90);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            result.setBirthday(sdf.parse("" + year + "-" + month + "-" + day));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        result.setMoney(randomGenerator.nextDouble() * 100);
        return result;
    }

    /**
     * Creates a random address out of street names, postal codes and city names
     * which are taken from "The Simpsons" (i haven't found postal codes, so
     * here i invented some for the example)
     *
     * @return
     */
    public static Address createAddress() {
        String[] streets = getStreetNames();
        int[] plz = { 11111, 22222, 33333, 44444, 55555, 66666 };
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
     * Creates a random person out of names which are taken from "The Simpsons"
     * and enrich them with random generated married state and birthday date.
     * Also adds a random address out of street names, postal codes and city
     * names which are taken from "The Simpsons" (i haven't found postal codes,
     * so here i invented some for the example)
     *
     * @return
     */
    public static PersonWithAddress createPersonWithAddress(int id) {
        return new PersonWithAddress(createPerson(id), createAddress());
    }

    /**
     * Creates a random person out of names which are taken from "The Simpsons"
     * and enrich them with random generated married state and birthday date.
     * Adds a random address out of street names, postal codes and city names
     * which are taken from "The Simpsons" (i haven't found postal codes, so
     * here i invented some for the example). Also adds extended information
     * like a password, a random long description text, a money balance and
     * collections of favourite food and drinks.
     *
     * @return
     */
    public static ExtendedPersonWithAddress createExtendedPersonWithAddress(int id) {
        return new ExtendedPersonWithAddress(createPerson(id),
                createAddress(), generateSimplePassword(),
                createRandomLengthText(), createRandomMoneyAmount(),
                createFavouriteFood(), createFavouriteDrinks());
    }

    /**
     * @return A simple password consisting of 8 characters in the value ranges
     *         a-z, A-Z
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
     * @return An array of street names that are also used to create random
     *         addresses.
     */
    public static String[] getStreetNames() {
        return streetNames;
    }

    /**
     * @return An array of city names that are also used to create random
     *         addresses.
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

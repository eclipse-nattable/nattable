package org.eclipse.nebula.widgets.nattable.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class RectangleTest {

    // equals testing

    @Test
    public void pixelRectanglesShouldBeEqual() {
        PixelRectangle rect1 = new PixelRectangle(20, 20, 100, 100);
        PixelRectangle rect2 = new PixelRectangle(20, 20, 100, 100);

        assertEquals(rect1, rect2);
    }

    @Test
    public void pixelRectanglesShouldNotBeEqual() {
        PixelRectangle rect1 = new PixelRectangle(20, 20, 100, 100);
        PixelRectangle rect2 = new PixelRectangle(10, 10, 90, 90);

        assertFalse("rectangles are equal", rect1.equals(rect2));
    }

    @Test
    public void positionRectanglesShouldBeEqual() {
        PositionRectangle rect1 = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));
        PositionRectangle rect2 = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));

        assertEquals(rect1, rect2);
    }

    @Test
    public void positionRectanglesShouldNotBeEqual() {
        PositionRectangle rect1 = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));
        PositionRectangle rect2 = new PositionRectangle(
                BigInteger.valueOf(10),
                BigInteger.valueOf(10),
                BigInteger.valueOf(90),
                BigInteger.valueOf(90));

        assertFalse("rectangles are equal", rect1.equals(rect2));
    }

    // hashCode testing

    @Test
    public void pixelRectanglesHashCode() {
        PixelRectangle rect1 = new PixelRectangle(20, 20, 100, 100);
        PixelRectangle rect2 = new PixelRectangle(20, 20, 100, 100);

        Set<PixelRectangle> set = new HashSet<PixelRectangle>();
        set.add(rect1);
        set.add(rect2);

        assertEquals(1, set.size());

        PixelRectangle rect3 = new PixelRectangle(10, 10, 90, 90);
        set.add(rect3);

        assertEquals(2, set.size());
    }

    @Test
    public void positionRectanglesHashCode() {
        PositionRectangle rect1 = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));
        PositionRectangle rect2 = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));

        Set<PositionRectangle> set = new HashSet<PositionRectangle>();
        set.add(rect1);
        set.add(rect2);

        assertEquals(1, set.size());

        PositionRectangle rect3 = new PositionRectangle(
                BigInteger.valueOf(10),
                BigInteger.valueOf(10),
                BigInteger.valueOf(90),
                BigInteger.valueOf(90));
        set.add(rect3);

        assertEquals(2, set.size());
    }

    // empty

    @Test
    public void pixelRectangleShouldBeEmpty() {
        PixelRectangle rect = new PixelRectangle(20, 20, 100, 100);
        assertFalse("rectangle is empty", rect.isEmpty());

        rect = new PixelRectangle(20, 20, 100, -10);
        assertTrue("rectangle is not empty", rect.isEmpty());

        rect = new PixelRectangle(20, 20, -10, 100);
        assertTrue("rectangle is not empty", rect.isEmpty());

        rect = new PixelRectangle(20, 20, -10, -10);
        assertTrue("rectangle is not empty", rect.isEmpty());
    }

    @Test
    public void positionRectangleShouldBeEmpty() {
        PositionRectangle rect = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));
        assertFalse("rectangle is empty", rect.isEmpty());

        rect = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(-10));
        assertTrue("rectangle is not empty", rect.isEmpty());

        rect = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(-10),
                BigInteger.valueOf(100));
        assertTrue("rectangle is not empty", rect.isEmpty());

        rect = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(-10),
                BigInteger.valueOf(-10));
        assertTrue("rectangle is not empty", rect.isEmpty());
    }

    // contains

    @Test(expected = IllegalArgumentException.class)
    public void pixelRectangleContainsNull() {
        PixelRectangle rect = new PixelRectangle(20, 20, 100, 100);
        rect.contains(null);
    }

    @Test
    public void pixelRectangleShouldContainCoordinate() {
        PixelRectangle rect = new PixelRectangle(20, 20, 100, 100);

        PixelCoordinate coord = new PixelCoordinate(20, 20);
        assertTrue("retangle does not contain coordinate " + coord, rect.contains(coord));

        coord = new PixelCoordinate(100, 100);
        assertTrue("retangle does not contain coordinate " + coord, rect.contains(coord));

        coord = new PixelCoordinate(40, 40);
        assertTrue("retangle does not contain coordinate " + coord, rect.contains(coord));
    }

    @Test
    public void pixelRectangleShouldNotContainCoordinate() {
        PixelRectangle rect = new PixelRectangle(20, 20, 100, 100);

        PixelCoordinate coord = new PixelCoordinate(19, 19);
        assertFalse("retangle does not contain coordinate " + coord, rect.contains(coord));

        coord = new PixelCoordinate(121, 121);
        assertFalse("retangle does contain coordinate " + coord, rect.contains(coord));

        coord = new PixelCoordinate(10, 40);
        assertFalse("retangle does contain coordinate " + coord, rect.contains(coord));

        coord = new PixelCoordinate(40, 140);
        assertFalse("retangle does contain coordinate " + coord, rect.contains(coord));
    }

    @Test(expected = IllegalArgumentException.class)
    public void positionRectangleContainsNull() {
        PositionRectangle rect = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));
        rect.contains(null);
    }

    @Test
    public void positionRectangleShouldContainCoordinate() {
        PositionRectangle rect = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));

        PositionCoordinate coord = new PositionCoordinate(BigInteger.valueOf(20), BigInteger.valueOf(20));
        assertTrue("retangle does not contain coordinate " + coord, rect.contains(coord));

        coord = new PositionCoordinate(BigInteger.valueOf(100), BigInteger.valueOf(100));
        assertTrue("retangle does not contain coordinate " + coord, rect.contains(coord));

        coord = new PositionCoordinate(BigInteger.valueOf(40), BigInteger.valueOf(40));
        assertTrue("retangle does not contain coordinate " + coord, rect.contains(coord));
    }

    @Test
    public void positionRectangleShouldNotContainCoordinate() {
        PositionRectangle rect = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));

        PositionCoordinate coord = new PositionCoordinate(BigInteger.valueOf(19), BigInteger.valueOf(19));
        assertFalse("retangle does not contain coordinate " + coord, rect.contains(coord));

        coord = new PositionCoordinate(BigInteger.valueOf(121), BigInteger.valueOf(121));
        assertFalse("retangle does contain coordinate " + coord, rect.contains(coord));

        coord = new PositionCoordinate(BigInteger.valueOf(10), BigInteger.valueOf(40));
        assertFalse("retangle does contain coordinate " + coord, rect.contains(coord));

        coord = new PositionCoordinate(BigInteger.valueOf(40), BigInteger.valueOf(140));
        assertFalse("retangle does contain coordinate " + coord, rect.contains(coord));
    }

    // add - modify the current rectangle

    @Test
    public void pixelRectangleShouldAddSmaller() {
        PixelRectangle rect1 = new PixelRectangle(20, 20, 100, 100);
        PixelRectangle rect2 = new PixelRectangle(40, 40, 80, 80);

        rect1.add(rect2);

        assertEquals(20, rect1.x, 0);
        assertEquals(20, rect1.y, 0);
        assertEquals(100, rect1.width, 0);
        assertEquals(100, rect1.height, 0);
    }

    @Test
    public void pixelRectangleShouldAddBigger() {
        PixelRectangle rect1 = new PixelRectangle(20, 20, 100, 100);
        PixelRectangle rect2 = new PixelRectangle(0, 0, 120, 120);

        rect1.add(rect2);

        assertEquals(0, rect1.x, 0);
        assertEquals(0, rect1.y, 0);
        assertEquals(120, rect1.width, 0);
        assertEquals(120, rect1.height, 0);
    }

    @Test
    public void positionRectangleShouldAddSmaller() {
        PositionRectangle rect1 = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));
        PositionRectangle rect2 = new PositionRectangle(
                BigInteger.valueOf(40),
                BigInteger.valueOf(40),
                BigInteger.valueOf(80),
                BigInteger.valueOf(80));

        rect1.add(rect2);

        assertEquals(BigInteger.valueOf(20), rect1.x);
        assertEquals(BigInteger.valueOf(20), rect1.y);
        assertEquals(BigInteger.valueOf(100), rect1.width);
        assertEquals(BigInteger.valueOf(100), rect1.height);
    }

    @Test
    public void positionRectangleShouldAddBigger() {
        PositionRectangle rect1 = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));
        PositionRectangle rect2 = new PositionRectangle(
                BigInteger.valueOf(0),
                BigInteger.valueOf(0),
                BigInteger.valueOf(120),
                BigInteger.valueOf(120));

        rect1.add(rect2);

        assertEquals(BigInteger.valueOf(0), rect1.x);
        assertEquals(BigInteger.valueOf(0), rect1.y);
        assertEquals(BigInteger.valueOf(120), rect1.width);
        assertEquals(BigInteger.valueOf(120), rect1.height);
    }

    // union - return a new rectangle

    @Test
    public void pixelRectangleShouldUnionSmaller() {
        PixelRectangle rect1 = new PixelRectangle(20, 20, 100, 100);
        PixelRectangle rect2 = new PixelRectangle(40, 40, 80, 80);

        PixelRectangle union = rect1.union(rect2);

        assertEquals(20, union.x, 0);
        assertEquals(20, union.y, 0);
        assertEquals(100, union.width, 0);
        assertEquals(100, union.height, 0);
    }

    @Test
    public void pixelRectangleShouldUnionBigger() {
        PixelRectangle rect1 = new PixelRectangle(20, 20, 100, 100);
        PixelRectangle rect2 = new PixelRectangle(0, 0, 120, 120);

        PixelRectangle union = rect1.union(rect2);

        assertEquals(0, union.x, 0);
        assertEquals(0, union.y, 0);
        assertEquals(120, union.width, 0);
        assertEquals(120, union.height, 0);
    }

    @Test
    public void positionRectangleShouldUnionSmaller() {
        PositionRectangle rect1 = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));
        PositionRectangle rect2 = new PositionRectangle(
                BigInteger.valueOf(40),
                BigInteger.valueOf(40),
                BigInteger.valueOf(80),
                BigInteger.valueOf(80));

        PositionRectangle union = rect1.union(rect2);

        assertEquals(BigInteger.valueOf(20), union.x);
        assertEquals(BigInteger.valueOf(20), union.y);
        assertEquals(BigInteger.valueOf(100), union.width);
        assertEquals(BigInteger.valueOf(100), union.height);
    }

    @Test
    public void positionRectangleShouldUnionBigger() {
        PositionRectangle rect1 = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));
        PositionRectangle rect2 = new PositionRectangle(
                BigInteger.valueOf(0),
                BigInteger.valueOf(0),
                BigInteger.valueOf(120),
                BigInteger.valueOf(120));

        PositionRectangle union = rect1.union(rect2);

        assertEquals(BigInteger.valueOf(0), union.x);
        assertEquals(BigInteger.valueOf(0), union.y);
        assertEquals(BigInteger.valueOf(120), union.width);
        assertEquals(BigInteger.valueOf(120), union.height);
    }

    // intersect

    @Test
    public void pixelRectanglesShouldIntersect() {
        PixelRectangle rect1 = new PixelRectangle(20, 20, 100, 100);
        PixelRectangle rect2 = new PixelRectangle(60, 60, 100, 100);

        assertTrue("rectangles do not intersect", rect1.intersects(rect2));

        rect1.intersect(rect2);

        assertEquals(60, rect1.x, 0);
        assertEquals(60, rect1.y, 0);
        assertEquals(60, rect1.width, 0);
        assertEquals(60, rect1.height, 0);
    }

    @Test
    public void pixelRectanglesShouldIntersect2() {
        PixelRectangle rect1 = new PixelRectangle(20, 20, 100, 100);
        PixelRectangle rect2 = new PixelRectangle(60, 60, 100, 100);

        assertTrue("rectangles do not intersect", rect2.intersects(rect1));

        rect2.intersect(rect1);

        assertEquals(60, rect2.x, 0);
        assertEquals(60, rect2.y, 0);
        assertEquals(60, rect2.width, 0);
        assertEquals(60, rect2.height, 0);
    }

    @Test
    public void pixelRectanglesShouldProvideIntersection() {
        PixelRectangle rect1 = new PixelRectangle(20, 20, 100, 100);
        PixelRectangle rect2 = new PixelRectangle(60, 60, 100, 100);

        assertTrue("rectangles do not intersect", rect1.intersects(rect2));

        PixelRectangle inter = rect1.intersection(rect2);

        assertEquals(60, inter.x, 0);
        assertEquals(60, inter.y, 0);
        assertEquals(60, inter.width, 0);
        assertEquals(60, inter.height, 0);
    }

    @Test
    public void pixelRectanglesShouldProvideIntersection2() {
        PixelRectangle rect1 = new PixelRectangle(20, 20, 100, 100);
        PixelRectangle rect2 = new PixelRectangle(60, 60, 100, 100);

        assertTrue("rectangles do not intersect", rect2.intersects(rect1));

        PixelRectangle inter = rect1.intersection(rect2);

        assertEquals(60, inter.x, 0);
        assertEquals(60, inter.y, 0);
        assertEquals(60, inter.width, 0);
        assertEquals(60, inter.height, 0);
    }

    @Test
    public void pixelRectanglesShouldNotIntersect() {
        PixelRectangle rect1 = new PixelRectangle(20, 20, 40, 40);
        PixelRectangle rect2 = new PixelRectangle(60, 60, 100, 100);

        assertFalse("rectangles do intersect", rect1.intersects(rect2));

        rect1.intersect(rect2);

        assertEquals(60, rect1.x, 0);
        assertEquals(60, rect1.y, 0);
        assertEquals(0, rect1.width, 0);
        assertEquals(0, rect1.height, 0);
    }

    @Test
    public void positionRectanglesShouldIntersect() {
        PositionRectangle rect1 = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));
        PositionRectangle rect2 = new PositionRectangle(
                BigInteger.valueOf(60),
                BigInteger.valueOf(60),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));

        assertTrue("rectangles do not intersect", rect1.intersects(rect2));

        rect1.intersect(rect2);

        assertEquals(BigInteger.valueOf(60), rect1.x);
        assertEquals(BigInteger.valueOf(60), rect1.y);
        assertEquals(BigInteger.valueOf(60), rect1.width);
        assertEquals(BigInteger.valueOf(60), rect1.height);
    }

    @Test
    public void positionRectanglesShouldIntersect2() {
        PositionRectangle rect1 = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));
        PositionRectangle rect2 = new PositionRectangle(
                BigInteger.valueOf(60),
                BigInteger.valueOf(60),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));

        assertTrue("rectangles do not intersect", rect2.intersects(rect1));

        rect2.intersect(rect1);

        assertEquals(BigInteger.valueOf(60), rect2.x);
        assertEquals(BigInteger.valueOf(60), rect2.y);
        assertEquals(BigInteger.valueOf(60), rect2.width);
        assertEquals(BigInteger.valueOf(60), rect2.height);
    }

    @Test
    public void positionRectanglesShouldProvideIntersection() {
        PositionRectangle rect1 = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));
        PositionRectangle rect2 = new PositionRectangle(
                BigInteger.valueOf(60),
                BigInteger.valueOf(60),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));

        assertTrue("rectangles do not intersect", rect1.intersects(rect2));

        PositionRectangle inter = rect1.intersection(rect2);

        assertEquals(BigInteger.valueOf(60), inter.x);
        assertEquals(BigInteger.valueOf(60), inter.y);
        assertEquals(BigInteger.valueOf(60), inter.width);
        assertEquals(BigInteger.valueOf(60), inter.height);
    }

    @Test
    public void positionRectanglesShouldProvideIntersection2() {
        PositionRectangle rect1 = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));
        PositionRectangle rect2 = new PositionRectangle(
                BigInteger.valueOf(60),
                BigInteger.valueOf(60),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));

        assertTrue("rectangles do not intersect", rect2.intersects(rect1));

        PositionRectangle inter = rect1.intersection(rect2);

        assertEquals(BigInteger.valueOf(60), inter.x);
        assertEquals(BigInteger.valueOf(60), inter.y);
        assertEquals(BigInteger.valueOf(60), inter.width);
        assertEquals(BigInteger.valueOf(60), inter.height);
    }

    @Test
    public void positionRectanglesShouldNotIntersect() {
        PositionRectangle rect1 = new PositionRectangle(
                BigInteger.valueOf(20),
                BigInteger.valueOf(20),
                BigInteger.valueOf(40),
                BigInteger.valueOf(40));
        PositionRectangle rect2 = new PositionRectangle(
                BigInteger.valueOf(60),
                BigInteger.valueOf(60),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));

        assertFalse("rectangles do intersect", rect1.intersects(rect2));

        rect1.intersect(rect2);

        assertEquals(BigInteger.valueOf(60), rect1.x);
        assertEquals(BigInteger.valueOf(60), rect1.y);
        assertEquals(BigInteger.valueOf(0), rect1.width);
        assertEquals(BigInteger.valueOf(0), rect1.height);
    }
}

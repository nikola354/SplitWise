package bg.sofia.uni.fmi.mjt.splitwise.server.logic.calculation;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.SplitException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SplitterTest {
    @Test
    void testSplitNegativeInput() {
        assertThrows(IllegalArgumentException.class, () -> Splitter.split(-0.0001, 5),
            "Exception expected because of negative input");
    }

    @Test
    void testSplitPartsAreZero() {
        assertThrows(IllegalArgumentException.class, () -> Splitter.split(10, 0),
            "Exception expected because parts are 0");
    }

    @Test
    void testSplitInTwoSimple() throws SplitException {
        Map<Double, Integer> result = Splitter.split(9, 2);
        assertEquals(1, result.size(), "Simple splitting does not work");
        assertEquals(2, result.get(4.5), "Simple splitting does not work");
    }

    @Test
    void testSplitInThreeSimple() throws SplitException {
        Map<Double, Integer> result = Splitter.split(15.99, 3);
        assertEquals(1, result.size(), "Simple splitting in 3 does not work");
        assertEquals(3, result.get(5.33), "Simple splitting in 3 does not work");
    }

    @Test
    void testSplitInTwoOdd() throws SplitException {
        Map<Double, Integer> result = Splitter.split(0.99, 2);
        assertEquals(2, result.size(), "Odd splitting does not work");
        assertEquals(1, result.get(0.50), "Odd splitting does not work");
        assertEquals(1, result.get(0.49), "Odd splitting does not work");
    }

    @Test
    void testSplitOneInThree() throws SplitException {
        Map<Double, Integer> result = Splitter.split(1, 3);
        assertEquals(2, result.size(), "Odd splitting does not work");
        assertEquals(2, result.get(0.33), "Odd splitting does not work");
        assertEquals(1, result.get(0.34), "Odd splitting does not work");
    }

    @Test
    void testSplit100In7() throws SplitException {
        Map<Double, Integer> result = Splitter.split(100, 7);
        assertEquals(2, result.size(), "Odd splitting does not work");
        assertEquals(4, result.get(14.29), "Odd splitting does not work");
        assertEquals(3, result.get(14.28), "Odd splitting does not work");
    }

    @Test
    void testSplitBigNumbers() throws SplitException {
        Map<Double, Integer> result = Splitter.split(100000, 100000);
        assertEquals(1, result.size(), "Big numbers splitting does not work");
        assertEquals(100000, result.get(1.0), "Big numbers splitting does not work");
    }

    @Test
    void testSplitSmallNumbers() throws SplitException {
        Map<Double, Integer> result = Splitter.split(0.03, 2);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0.01), "Small numbers splitting does not work");
        assertEquals(1, result.get(0.02), "Small numbers splitting does not work");
    }

    @Test
    void testSplitIn30() throws SplitException {
        Map<Double, Integer> result = Splitter.split(457, 30);
        assertEquals(2, result.size());
        assertEquals(20, result.get(15.23), "Splitting does not work");
        assertEquals(10, result.get(15.24), "Splitting does not work");
    }

    @Test
    void testSplitThrowsSplitExceptionSplitIn3() {
        assertThrows(SplitException.class, () -> Splitter.split(0.02, 3),
            "Expected exception because of too little amount");
    }

    @Test
    void testSplitThrowsSplitExceptionSplitIn2() {
        assertThrows(SplitException.class, () -> Splitter.split(0.01, 2),
            "Expected exception because of too little amount");
    }
}

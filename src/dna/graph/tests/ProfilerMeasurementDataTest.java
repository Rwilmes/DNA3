package dna.graph.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.Test;

import dna.profiler.ProfilerMeasurementData;
import dna.profiler.datatypes.ComparableEntry;
import dna.profiler.datatypes.complexity.Complexity;
import dna.profiler.datatypes.complexity.ComplexityType;
import dna.profiler.datatypes.complexity.ComplexityType.Type;

public class ProfilerMeasurementDataTest {

	@Test
	public void testParseEmptyInput() {
		ProfilerMeasurementData.loadFromProperties(new Properties());
	}

	@Test
	public void testGetUnknownKeyFromEmptyList() {
		ProfilerMeasurementData.loadFromProperties(new Properties());
		assertNull(ProfilerMeasurementData.get("Unknown"));
	}

	@Test
	public void testGetUnknownKeyFromNonEmptyList() {
		Properties p = new Properties();
		p.setProperty("RUNTIMECOMPLEXITY_Key", "");
		ProfilerMeasurementData.loadFromProperties(p);
		assertNull(ProfilerMeasurementData.get("Unknown"));
	}

	@Test
	public void testParsingOfEmptyKey() {
		ComparableEntry c = ProfilerMeasurementData.parseString("RUNTIMECOMPLEXITY", "");
		assertEquals(new Complexity(), c);
	}

	@Test(expected = RuntimeException.class)
	public void testParsingOfBrokenKey() {
		ComparableEntry c = ProfilerMeasurementData.parseString("RUNTIMECOMPLEXITY", "10");
		fail("Assertion not caught on parsing " + c);
	}

	@Test
	public void testParsingOfSimpleString() {
		ComparableEntry c = ProfilerMeasurementData
				.parseString("RUNTIMECOMPLEXITY", "1 Static");
		assertEquals(new Complexity(1, new ComplexityType(Type.Static, null)),
				c);
	}

	@Test(expected = RuntimeException.class)
	public void testParsingOfLoop() {
		Properties p = new Properties();
		p.setProperty("Get", "1 Remove");
		p.setProperty("Remove", "1 Get");
		ProfilerMeasurementData.loadFromProperties(p);
		fail("Assertion not caught on parsing a properties file with loops");
	}
}

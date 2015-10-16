package diva.brokeratcloud.fpr.input.json;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import diva.brokeratcloud.fpr.input.abstracts.ServiceAttribute;
import diva.brokeratcloud.fpr.input.abstracts.ServiceCategory;
import diva.brokeratcloud.fpr.input.abstracts.ServiceDependency;

public class TestJsonInput {

	// @Before
	// public void setUp(){
	// jsonRoot = JsonRoot.INSTANCE;
	// }

	@Test
	public void testNumOfOfferings() {
		assertEquals(3, JsonRoot.INSTANCE.offerings.size());
	}

	@Test
	public void testServiceCategory() {
		ServiceCategory sc = new ServiceCategoryJson();
		System.out.println(sc.getCategories());

		assertEquals(4, sc.getCategories().size());

		System.out.println(sc.getServices("DatabaseOfferingServiceModel"));
		System.out.println(sc.getServices("ApplicationServerOfferingServiceModel"));
		// assertEquals("OrbiInstance1",sc.getServices("OrbiOfferingServiceModel").get(0));
		// assertEquals("DBInstance1",
		// sc.getServices("DatabaseOfferingServiceModel").get(0));
		// assertEquals(0, sc.getServices("SomethingElse").size());
		// assertEquals("DBInstance1", sc.getGroup("DBInstance1").get(0));
		// assertEquals(0, sc.getGroup("SomethingElse").size());
	}

	@Test
	public void testServiceAttribute() {
		ServiceAttribute sa = new ServiceAttributeJson();
		System.out.println(sa.listCommonAttributes());
		// ((ServiceAttributeJson)sa).printMinMax();
		assertEquals(7, sa.get("OrbiInstance1", "Uptime"));
		assertEquals(5, sa.get("OrbiInstance1", "HTTPResponseTime"));
		assertEquals(1, sa.get("OrbiInstance1", "CPUvCores"));
		assertEquals(7, sa.get("ASInstance1", "Uptime"));
		assertEquals(3, sa.get("ASInstance1", "CPULoadAvgPerCore"));
	}

	@Test
	public void testServiceDependencies() {
		ServiceDependency sd = new ServiceDependencyJson();
		assertEquals(Arrays.asList("DBInstance1", "ASInstance1"), sd.getDependency("OrbiInstance1"));
	}

}

package eu.brokeratcloud.fpr.input.sparql;

import static org.junit.Assert.*;

import org.junit.Test;

public class QueryInputTest {

	@Test
	public void testCategoryInit() {
		ServiceCategorySparql sc = new ServiceCategorySparql();
		sc.init();
		assertEquals(44, sc.catRecord.size());
		// System.out.println(sc.catRecord);
	}

	@Test
	public void testServiceAndCategory() {
		ServiceCategorySparql sc = new ServiceCategorySparql();
		sc.init();
		assertEquals(14, sc.getCategories().size());
		assertEquals(3, sc.getServices("CASTaskApp").size());
		assertEquals(5, sc.getServices("FC").size());
	}

	@Test
	public void testServiceAttribute() {
		ServiceAttributeSparql sa = new ServiceAttributeSparql();
		assertEquals(2, sa.get("CASReportsApp2", "hasExtensibility"));
		assertEquals(4, sa.get("CASCalenderApp1", "hasAdaptability"));
		assertEquals(0, sa.get("CASCalenderApp1", "someNosense"));
	}

}

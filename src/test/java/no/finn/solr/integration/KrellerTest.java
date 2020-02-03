package no.finn.solr.integration;

import org.junit.jupiter.api.Test;

public class KrellerTest extends SolrTestCase {

    @Test
    public void krell_should_hit() throws Exception {
        Long[] expectedHits = solr.addDocumentsWith("2 finne krell høytalere");
        solr.performSearchAndAssertHits("krell", expectedHits);
    }

    @Test
    public void kr_should_not_hit() throws Exception {
        solr.addDocumentsWith("selges for 200kr.eller høystbydede");
        solr.performSearchAndAssertNoHits("krell");
    }

    @Test
    public void kr_should_not_hit_2() throws Exception {
        solr.addDocumentsWith("selges for 200 kr.eller høystbydede");
        solr.performSearchAndAssertNoHits("krell");
    }

    @Test
    public void kr_should_not_hit_3() throws Exception {
        solr.addDocumentsWith("selges for 200 kr eller høystbydede");
        solr.performSearchAndAssertNoHits("krell");
    }
}

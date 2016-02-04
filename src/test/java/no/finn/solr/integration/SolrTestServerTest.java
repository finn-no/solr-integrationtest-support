package no.finn.solr.integration;

import org.junit.Test;

public class SolrTestServerTest {

    @Test
    public void testThatWeCanStartupAnExampleServer() throws Exception {
        SolrTestServer server = new SolrTestServer("example").withDefaultContentField("content");
        Long docId = server.addDocumentWith("Hello");
        server.performSearchAndAssertHits("Hello", docId);
    }
}

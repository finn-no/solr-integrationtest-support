package no.finn.solr.integration;

import org.junit.BeforeClass;

public class SolrTestCase {
    protected static SolrTestServer solr;
    private static String coreName;

    private SolrTestCase() {}
    public SolrTestCase(String coreName) {
        this.coreName = coreName;
    }

    @BeforeClass
    public static void setupSolr() {
        solr = new SolrTestServer(coreName).withEmptyIndex();
    }
}

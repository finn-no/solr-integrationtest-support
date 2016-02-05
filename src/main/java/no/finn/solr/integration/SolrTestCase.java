package no.finn.solr.integration;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class SolrTestCase {
    protected static SolrTestServer solr;

    @BeforeClass
    public static void setupSolr() throws IOException, SolrServerException {
        solr = new SolrTestServer().withEmptyIndex();
    }

    @AfterClass
    public static void teardownSolr() throws IOException, SolrServerException {
        solr.shutdown();
    }

    @Before
    public void delete_all_docs() throws IOException, SolrServerException {
        solr = solr.withEmptyIndex();
    }

    public static SolrTestServer getSolr() {
        return solr;
    }
}

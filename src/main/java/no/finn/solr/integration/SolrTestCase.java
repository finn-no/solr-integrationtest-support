package no.finn.solr.integration;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class SolrTestCase {
    protected static SolrTestServer solr;

    /**
     * Wires up an embedded solr server
     * @throws IOException if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    @BeforeClass
    public static void setupSolr() throws IOException, SolrServerException {
        solr = new SolrTestServer().withEmptyIndex();
    }

    /**
     * Stops the Solr server
     * @throws IOException if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    @AfterClass
    public static void teardownSolr() throws IOException, SolrServerException {
        if (solr != null) {
            solr.shutdown();
        }
    }

    /**
     * Removes all documents from the solr index
     * @throws IOException if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    @Before
    public void delete_all_docs() throws IOException, SolrServerException {
        solr = solr.withEmptyIndex();
    }

    /**
     * @return the embedded solr server
     */
    public static SolrTestServer getSolr() {
        return solr;
    }
}

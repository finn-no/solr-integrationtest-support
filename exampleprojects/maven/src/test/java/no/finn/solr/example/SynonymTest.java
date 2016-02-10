package no.finn.solr.example;

import no.finn.solr.integration.SolrTestCase;

import org.junit.Test;

public class SynonymTest extends SolrTestCase {

    @Test
    public void volkswagenIsSynonymousWithVW() throws Exception {
      Long docId = solr.addDocumentWith("vw");
      solr.performSearchAndAssertHits("volkswagen", docId);
    }

    @Test
    public void vWIsSynonymousWithVolkswagen() throws Exception {
      Long docId = solr.addDocumentWith("volkswagen");
      solr.performSearchAndAssertHits("vw", docId);
    }

    @Test
    public void flatscreenIsSynonymousWithLcd() throws Exception {
      Long docId = solr.addDocumentWith("flatscreen");
      solr.performSearchAndAssertHits("lcd", docId);
    }

    @Test
    public void LcdIsNotSynonymousWithFlatscreen() throws Exception {
      Long docId = solr.addDocumentWith("lcd");
      solr.performSearchAndAssertNoHits("flatscreen");
    }
}

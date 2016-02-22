# Changelog

## 2.0.0
### Features
* Startup
    * Exceptions in coreloading will now be printed
    * DataDir is now unique per initialization of SolrTestServer - should support multithreading
* API change
    * getClient is now getServer and returns an EmbeddedSolrServer

## 1.0.1
### Bugfix
* Shutdown on failed test should no longer throw NPE due to non-initialized solr field

## 1.0.0
Initial release
### Features
* Indexing
    * Add document with single value
    * Add document with custom field and custom value
    * Add documents with single value
    * Add documents with custom field and custom values
* Searching
    * Verify single match for query
    * Search field:query
    * Verify that all expected documents match search
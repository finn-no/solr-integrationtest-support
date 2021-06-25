# Changelog

## 7.1.0
### Feature
* Support SOLR 8.9.0

## 7.0.0
### Feature
* Converted to maven and moved release from bintray to maven central.

## 4.0.2
### Feature
* Now uses Solr/Lucene 7.1.0

## 4.0.1
### Feature
* Now uses Solr/Lucene 7.0.1

## 4.0.0
### Feature
* Works with Solr/Lucene 7.0.0+
* Need to remove mergeFactor from solrconfig.xml
* Need to remove defaultSearchField from schema.xml
* Need to remove defaultQueryOperator from schema.xml

## 3.0.0
### Feature
* Works with Solr/Lucene 6.3.0
* Needed change
   * Remove AdminHandler from solrconfig.xml (Deprecated in Solr 6.2)


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

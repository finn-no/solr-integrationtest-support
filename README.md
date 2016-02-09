[![][Build Status img]][Build Status]
[![][license img]][license]
[![][Maven Central img][Maven Central]

# solr-integrationtest-support

Integration testing with Solr made easy

# Versioning
* http://semver.org


# Contributing?
See [Contributing](CONTRIBUTING.md)

# How-to
## Maven

```xml
<dependency>
  <groupId>no.finn.search</groupId>
  <artifactId>solr-integrationtest-support</artifactId>
  <version>1.0-SNAPSHOT</version>
  <scope>test</scope>
</dependency>
```

## Gradle

```groovy
testCompile "no.finn.search:solr-integrationtest-support:1.0-SNAPSHOT
```

## Further steps

* For now, you'll need to run singlethreaded, because the EmbeddedSolrServer locks the indexdirectory
* We usually put our Solr Configs in src/main/resources or src/test/resources
* Have your JUnit test extend `SolrTestCase`
* Test your schema and solrconfig.xml

## Exampletest

```java

import no.finn.solr.integration.SolrTestCase;

public class SolrConfigTest extends SolrTestCase {
    @Test
    public void weCanFindADocumentWeJustAdded() throws Exception {
        Long docId = solr.addDocumentWith("Hello");
        solr.performSearchAndAssertHits("hello", docId);
    }
}

```

# How to Use

Our goal is that the javadocs available here: https://finn-no.github.io/solr-integrationtest-support/development/javadocs

## Adding documents
1.
```java
Long docId = solr.addDocumentWith(content);
```
Adds a single document to the index with `content` to what is set as `defaultContentField`.
By default this is the `body` field.
Returns the randomly generated id of the document. The id can be used later for verification

2.
```java
Long[] docIds = solr.addDocumentsWith(... content);
```
A varargs version of `addDocumentWith`, this adds content to the `defaultContentField`
Returns an array of the documentIds acquired when adding the content
3.
```java
Long docIds = solr.addDocumentWithField(field, content);
```
Adds a single document to the index with `content` to the field in `field`
Returns the randomly generated id of the document
4.
```java
Long[] docIds = solr.addDocumentsWithField(field, ... content);
```
A varargs version of `addDocumentWithField`. This adds contents to the `field`
Returns an array of the documentIds acquired when adding the content

## Searching
1.
```java
QueryResponse response = solr.search(String query);
```
2.
```java
QueryResponse response = solr.dismaxSearch(String dismaxQuery); // Uses the dismax queryhandler instead of the standard/default handler (qt=dismax)
``
## Asserts / Checks




[Build Status]:https://travis-ci.org/finn-no/solr-integrationtest-support
[Build Status img]:https://travis-ci.org/finn-no/solr-integrationtest-support.svg?branch=master
[license]:LICENSE
[license img]:https://img.shields.io/badge/License-Apache%202-blue.svg
[Maven Central img]:https://maven-badges.herokuapp.com/maven-central/no.finn.search/solr-integrationtest-support/badge.svg
[Maven Central]:https://maven-badges.herokuapp.com/maven-central/no.finn.search/solr-integreationtest-support



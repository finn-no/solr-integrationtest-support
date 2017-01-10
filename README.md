[![][Build Status img]][Build Status]
[![][license img]][license]
[![][Maven Central img]][Maven Central]
[![][Bintray img]][Bintray Latest]
# solr-integrationtest-support

Integration testing with Solr made easy

# Versioning
* http://semver.org

# Contributing?
See [Contributing](CONTRIBUTING.md)

# How-to

## Maven

### Repository (jcenter)

```xml
<repositories>
   ...
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>central</id>
        <name>bintray</name>
        <url>http://jcenter.bintray.com</url>
    </repository>
    ...
</repositories>
```
### Artifact
```xml
<dependency>
  <groupId>no.finn.search</groupId>
  <artifactId>solr-integrationtest-support</artifactId>
  <version>3.0.0</version>
  <scope>test</scope>
</dependency>
```

## Gradle

### Repository
```groovy
repositories {
    jcenter()
}
```
### Artifact
```groovy
testCompile "no.finn.search:solr-integrationtest-support:3.0.0
```

## Further steps

* For now, you'll need to run singlethreaded, because the EmbeddedSolrServer locks the indexdirectory (see [pom.xml](exampleprojects/maven/pom.xml) for configuration of surefire.)
* We usually put our Solr Configs in src/main/resources or src/test/resources
* Have your JUnit test extend `SolrTestCase`
* Test your schema and solrconfig.xml

## Examples

Example setup is available for both maven and gradle inside the exampleprojects folder

### [Maven](exampleprojects/maven)

### [Gradle](exampleprojects/gradle)

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

For other examples - see the provided [SolrExampleTestCase](src/test/java/no/finn/solr/integration/SolrExampleTestCase.java)

# How to Use

Our goal is that the [Javadocs](https://finn-no.github.io/solr-integrationtest-support/development/javadocs) should be all you need.

But a quickstart is available below:

## Adding documents (indexing)
### Adding a single document with a string value to default field
```java
Long docId = solr.addDocumentWith(content);
```
Adds a single document to the index with `content` to what is set as `defaultContentField`.
By default this is the `body` field.
Returns the randomly generated id of the document. The id can be used later for verification

### Adding several values to default field
```java
Long[] docIds = solr.addDocumentsWith(... content);
```
A varargs version of `addDocumentWith`, this adds content to the `defaultContentField`
Returns an array of the documentIds acquired when adding the content

### Adding a single document with value to custom field
```java
Long docIds = solr.addDocumentWithField(field, content);
```
Adds a single document to the index with `content` to the field in `field`
Returns the randomly generated id of the document

### Adding several values to custom field
```java
Long[] docIds = solr.addDocumentsWithField(field, ... content);
```
A varargs version of `addDocumentWithField`. This adds contents to the `field`
Returns an array of the documentIds acquired when adding the content

## Searching

### Search using default search handler
```java
QueryResponse response = solr.search(String query);
```

### Search using dismax search handler
```java
QueryResponse response = solr.dismaxSearch(String dismaxQuery);
```
Uses the dismax queryhandler instead of the standard/default handler (qt=dismax).
This performs a search and updates the response field on the class. Call

### Search for specified field with value
```java
solr.search(field, query);
```
Offers a simple way to run a query against a specific field


### Performing search with custom parameters set
If you've set parameters directly on SolrClient or with `solr.withParam(..)` you can execute a search by calling
```java
QueryResponse response = solr.search();
```
This will then perform a search with the parameters currently set.

## Asserts / Checks

### Search and find one document
```java
solr.performSearchAndAssertOneHit(String search);
```
Calls search, and then verifies that the result has exactly one hit

### Search and verify ids of documents
```java
solr.performSearchAndAssertHits(String search, Long... ids);
```
Calls search, and then verifies that the hits returned have exactly the ids passed in. Works well with the ids returned from the
`solr.addDocument_` methods

### Search and verify not found
```java
solr.performSearchAndAssertNoHits(String search);
```

Typically you'd want to confirm that a certain word does not yield results
```java

import no.finn.solr.integration.SolrTestCase;

public class NonExistantDocumentTest extends SolrTestCase {
    @Test
    public void documentDoesNotExist() throws Exception {
      Long docId = solr.addDocumentWith("doc");
      solr.performSearchAndAssertNoHits("doctor");
    }
}
```


Makes sure that the index does not contain anything that matches the passed in search


# [Release procedure](RELEASING.md)


[Build Status]:https://travis-ci.org/finn-no/solr-integrationtest-support
[Build Status img]:https://travis-ci.org/finn-no/solr-integrationtest-support.svg?branch=master
[license]:LICENSE
[license img]:https://img.shields.io/badge/License-Apache%202-blue.svg
[Maven Central img]:https://maven-badges.herokuapp.com/maven-central/no.finn.search/solr-integrationtest-support/badge.svg
[Maven Central]:https://maven-badges.herokuapp.com/maven-central/no.finn.search/solr-integreationtest-support
[Bintray img]:https://api.bintray.com/packages/finn-no/search/solr-integrationtest-support/images/download.svg
[Bintray Latest]:https://bintray.com/finn-no/search/solr-integrationtest-support/_latestVersion



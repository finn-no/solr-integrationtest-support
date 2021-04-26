# Releasing a new version
* Make sure that all changes are committed
* Run `./gradlew release`
    * Follow on-screen instructions
# Bintray
* Checkout tag to release
* Set BINTRAY_USER and BINTRAY_API_KEY to your Bintray username and Api key respectively
* Run `./gradlew bintrayUpload`
* Remember to publish the uploaded files through the Bintray GUI
* Done

## New release - WIP
* Permission to deploy to maven central granted: https://issues.sonatype.org/browse/OSSRH-67804
* Hos to release on mvn central, step-by-step: https://dzone.com/articles/publish-your-artifacts-to-maven-central
* Backup step-by-step: https://blog.10pines.com/2018/06/25/publish-artifacts-on-maven-central/

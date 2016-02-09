# Releasing a new version
* Make sure that all changes are committed
* Run `gradle release`
    * Follow on-screen instructions
# Bintray
* Checkout tag to release
* Set BINTRAY_USER and BINTRAY_API_KEY to your Bintray username and Api key respectively
* Run `gradle bintrayUpload`
* Done

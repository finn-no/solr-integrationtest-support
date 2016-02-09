#!/bin/bash
invokeJavadoc=$1
invokeDoc=false
branchVersion="development"

# Only invoke the javadoc deployment process
# for the first job in the build matrix, so as
# to avoid multiple deployments.

echo -e "Staring with project Javadocs...\n"

if [ "$invokeJavadoc" == true ]; then

  echo -e "Started to publish latest Javadoc to gh-pages...\n"

  echo -e "Invoking build to generate the project site...\n"
  ./gradlew javadoc -q -Dorg.gradle.configureondemand=true -Dorg.gradle.workers.max=8 --parallel

  echo -e "Copying the generated docs over...\n"
  cp -R build/javadoc $HOME/javadoc-latest

fi

echo -e "Finished with project Javadocs...\n"

if [[ "$invokeJavadoc" == true || "$invokeDoc" == true ]]; then

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  echo -e "Cloning the gh-pages branch...\n"
  git clone --depth 1 --quiet --branch=gh-pages https://${GH_TOKEN}@${GH_REF} gh-pages > /dev/null

  cd gh-pages

  echo -e "Staring to move project Javadocs over...\n"

  if [ "$invokeJavadoc" == true ]; then
    echo -e "Removing previous Javadocs from /$branchVersion/javadocs...\n"
    git rm -rf ./"$branchVersion"/javadocs > /dev/null

    echo -e "Creating $branchVersion directory...\n"
    test -d "./$branchVersion" || mkdir -m777 -v ./"$branchVersion"

    echo -e "Creating Javadocs directory at /$branchVersion/javadocs...\n"
    test -d "./$branchVersion/javadocs" || mkdir -m777 -v ./"$branchVersion"/javadocs

    echo -e "Copying new Javadocs...\n"
    cp -Rf $HOME/javadoc-latest/* ./"$branchVersion"/javadocs
    echo -e "Copied project Javadocs...\n"

  fi

  echo -e "Adding changes to the git index...\n"
  git add -f . > /dev/null

  echo -e "Committing changes...\n"
  git commit -m "Published documentation to [gh-pages]. Build $TRAVIS_BUILD_NUMBER" > /dev/null

  echo -e "Pushing upstream to origin...\n"
  git push -fq origin gh-pages > /dev/null

  echo -e "Successfully published documenetation to [gh-pages] branch.\n"

fi

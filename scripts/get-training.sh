#!/bin/bash

mkdir -p src/main/resources

curl https://spamassassin.apache.org/publiccorpus/20030228_spam.tar.bz2  > src/main/resources/spam.tar.bz2
curl https://spamassassin.apache.org/publiccorpus/20021010_easy_ham.tar.bz2 > src/main/resources/ham.tar.bz2

pushd src/main/resources
tar zxf spam.tar.bz2
tar zxf ham.tar.bz2

mv easy_ham ham

popd

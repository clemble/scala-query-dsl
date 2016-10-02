Scala Query DSL
========
[![CircleCI](https://circleci.com/gh/clemble/scala-query-dsl.svg?style=svg)](https://circleci.com/gh/clemble/scala-query-dsl)
[![Coverage Status](https://coveralls.io/repos/github/clemble/scala-query-dsl/badge.svg?branch=master)](https://coveralls.io/github/clemble/scala-query-dsl?branch=master)

Scala Query DSL was inspired by Query DSL in Java and Spring Data.

The goal is to create transparent search mechanism, that would be independent of used engine and could easily be replaced with another engine as needed.
Currently supported engines

  - Mongo (https://github.com/ReactiveMongo/ReactiveMongo, http://reactivemongo.org/)
  - ElasticSearch (https://github.com/sksamuel/elastic4s)

Currently only query functionality supported:

by providing a basic explanation of how to do it easily.

Look how easy it is to use:

    import project
    # Get your stuff done
    project.do_stuff()

Features
--------

- Be awesome
- Make things faster

Installation
------------

Install scala-query-dsl by running:

    libraryDependencies ++= Seq(
      "com.clemble" %% "scala-query-dsl" % "0.0.1"
    )

Contribute
----------

- Issue Tracker: github.com/clemble/scala-query-dsl/issues
- Source Code: github.com/clemble/scala-query-dsl

Support
-------

If you are having issues, please let us know.
We have a mailing list located at: antono@clemble.com

License
-------

The project is licensed under the Apache 2.0 license.
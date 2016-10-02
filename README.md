Scala Query DSL
========
[![CircleCI](https://circleci.com/gh/clemble/scala-query-dsl.svg?style=svg)](https://circleci.com/gh/clemble/scala-query-dsl)
[![Coverage Status](https://coveralls.io/repos/github/clemble/scala-query-dsl/badge.svg?branch=master)](https://coveralls.io/github/clemble/scala-query-dsl?branch=master)

Scala Query DSL was inspired by Query DSL in Java.

I found myself implementing the same pattern for handling queries over and over again. So, when thinking of implementing this for a 3rd time, I decided to generalize acquired experience in this open source project.

The goal is to create simple set of abstractions to simplify basic search implementation. It should be extensible and have a performance on a pair with writing queries manually.  

Supported data stores:

  - Mongo JSON collections (https://github.com/ReactiveMongo/ReactiveMongo, http://reactivemongo.org/)
  - Mongo BSON collections (https://github.com/ReactiveMongo/ReactiveMongo, http://reactivemongo.org/)
  - ElasticSearch with Elastic4s (https://github.com/sksamuel/elastic4s)


### Query mechanism

Supported data stores

### Usage

Given following User

   case class User {
        firstName: String,
        lastName: String,
        age: BigDecimal
   }

You can create query

   import com.clemble.query.model.Expression.Implicits._ 

   val query = Query("firstName" is "Bob")
   
This query can be used with 

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
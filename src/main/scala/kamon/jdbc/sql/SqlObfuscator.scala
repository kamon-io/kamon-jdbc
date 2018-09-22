/*
 * =========================================================================================
 * Copyright © 2013-2018 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */


package kamon.jdbc.sql

import java.util.regex.Pattern

import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap

class SqlObfuscator2(config: Config) {

  val allStringPatterns =  config.getStringList("obfuscator.pattern-list").asScala.mkString("|")
  val Patterns = Pattern.compile(allStringPatterns)
  val Unmatched = Pattern.compile("""'|"|/\*|\*/|\$""")

  val cache = TrieMap.empty[String, String]

  def obfuscate(sql:String):String = {
    cache.getOrElseUpdate(sql, {
      if (sql == null || sql.length == 0) sql
      else {
        val obfuscatedSql = Patterns.matcher(sql).replaceAll("?")
        if (Unmatched.matcher(obfuscatedSql).find) "?" else obfuscatedSql
      }
    })
  }
}

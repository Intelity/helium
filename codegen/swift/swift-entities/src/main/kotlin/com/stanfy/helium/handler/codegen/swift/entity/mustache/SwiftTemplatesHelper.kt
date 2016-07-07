package com.stanfy.helium.handler.codegen.swift.entity.mustache

import com.github.mustachejava.DefaultMustacheFactory
import com.stanfy.helium.handler.codegen.swift.entity.SwiftEntitiesAccessLevel
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityArray
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityEnumCase
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftProperty
import java.io.StringWriter

class SwiftTemplatesHelper {

  companion object {
    fun generateSwiftStruct(name: String, properties: List<SwiftProperty>, accessLevel: SwiftEntitiesAccessLevel): String {
      return generatedTemplateWithName("SwiftStruct.mustache", object : Any () {
        val name = name
        val props = properties
        val accessLevel = when(accessLevel) {
          SwiftEntitiesAccessLevel.INTERNAL -> ""
          SwiftEntitiesAccessLevel.PUBLIC -> "public"
        }
      })
    }

    fun generateSwiftEnum(name: String, values: List<Any>, accessLevel: SwiftEntitiesAccessLevel): String {
      return generatedTemplateWithName("SwiftEnum.mustache", object : Any () {
        val name = name
        val values = values
        val accessLevel = when(accessLevel) {
          SwiftEntitiesAccessLevel.INTERNAL -> ""
          SwiftEntitiesAccessLevel.PUBLIC -> "public"
        }
      })
    }

    fun generateSwiftTypeAlias(name: String, itemType: SwiftEntityArray, accessLevel: SwiftEntitiesAccessLevel): Any {
      return generatedTemplateWithName("SwiftTypeAlias.mustache", object : Any () {
        val name = name
        val type = itemType.unaliasedTypeString()
        val accessLevel = when(accessLevel) {
          SwiftEntitiesAccessLevel.INTERNAL -> ""
          SwiftEntitiesAccessLevel.PUBLIC -> "public"
        }
      })
    }

    fun generateSwiftEnumDecodables(name: String, values: List<SwiftEntityEnumCase>): String {
      return generatedTemplateWithName("decodable/SwiftEnumDecodable.mustache", object : Any () {
        val name = name
        val values = values
      })
    }
    fun generateSwiftStructDecodables(name: String, properties: List<SwiftProperty>): String {
      return generatedTemplateWithName("decodable/SwiftStructDecodable.mustache", object : Any () {
        val name = name
        val props = properties.mapIndexed { i, pr ->
          object {
            val optional = if (pr.type.optional) "?" else ""
            val delimiter = if (i != properties.lastIndex) "," else ""
            val name = pr.name
            val jsonKey = pr.originalName
          }
        }
      })
    }

    fun generateSwiftStructEquatable(name: String, properties: List<SwiftProperty>): String {
      return generatedTemplateWithName("equatable/SwiftStructEquatable.mustache", object : Any () {
        val name = name
        val props = properties.mapIndexed { i, pr ->
          object {
            val delimiter = if (i == 0) "return" else "    &&"
            val name = pr.name
          }
        }
      })
    }

    fun generateSwiftEnumEquatable(name: String, values: List<SwiftEntityEnumCase>): String {
      return generatedTemplateWithName("equatable/SwiftEnumEquatable.mustache", object : Any () {
        val name = name
        val values = values
      })
    }

    fun generatedTemplateWithName(templateName: String, templateObject: Any): String {
      val mustacheFactory = DefaultMustacheFactory()
      val mustache = mustacheFactory.compile(templateName)
      val stringWriter = StringWriter()
      mustache.execute(stringWriter, templateObject)
      return stringWriter.toString()
    }



  }

}
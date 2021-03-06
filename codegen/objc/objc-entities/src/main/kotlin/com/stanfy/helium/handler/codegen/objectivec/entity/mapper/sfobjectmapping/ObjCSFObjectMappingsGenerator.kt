package com.stanfy.helium.handler.codegen.objectivec.entity.mapper.sfobjectmapping

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProjectStructureGenerator
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCClass
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCMethod
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCMethodImplementationSourcePart
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCMethodSourcePart
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCStringSourcePart
import com.stanfy.helium.model.Project

/**
 * Created by ptaykalo on 9/2/14.
 * Class that is responsible for generate files those are responsible for
 * correct mapping performing from JSON Objects to Messages
 * Generated classes will could be used with
 * https://github.com/stanfy/SFObjectMapping
 */
class ObjCSFObjectMappingsGenerator : ObjCProjectStructureGenerator {
  val MAPPINGS_FILENAME = "HeliumMappings"

  override fun generate(project: ObjCProject, projectDSL: Project, options: ObjCEntitiesOptions) {
    val mappingsClassName = options.prefix + MAPPINGS_FILENAME
    val mappingsClass = ObjCClass(mappingsClassName)
    project.classesTree.addClass(mappingsClass)

    mappingsClass.implementation.importClassWithName("SFMapping")
    mappingsClass.implementation.importClassWithName("NSObject+SFMapping")

    val initializeMethod = ObjCMethod("initialize", ObjCMethod.ObjCMethodType.CLASS, "void")
    val initializeMethodSourcePart = ObjCMethodImplementationSourcePart(initializeMethod)
    mappingsClass.implementation.addBodySourcePart(initializeMethodSourcePart)

    // get property definitions
    val contentsBuilder = StringBuilder()

    // Generate all them all
    for (m in projectDSL.messages) {
      val objCClass = project.classesTree.getClassForType(m.name) ?: continue
      mappingsClass.implementation.importClassWithName(objCClass.name)

      // Get the implementation
      contentsBuilder.append("    [").append(objCClass.name).append(" setMappingInfo:").append("\n")

      for (prop in objCClass.definition.propertyDefinitions) {
        contentsBuilder.append("      [SFMapping ")
        val field = prop.correspondingField
        if (field != null) {
          if (field.isSequence) {
            val itemClass = prop.sequenceType!!.name
            contentsBuilder.append("collection:@\"").append(prop.name).append("\" itemClass:@\"").append(itemClass).append("\" toKeyPath:@\"").append(field.name).append("\"],\n")
          } else {
            contentsBuilder.append("property:@\"").append(prop.name).append("\" toKeyPath:@\"").append(field.name).append("\"],\n")
          }

        } else {
          contentsBuilder.append("property:@\"").append(prop.name).append("\" toKeyPath:@\"").append(prop.name).append("\"],\n")
        }

      }
      contentsBuilder.append("    nil];").append("\n\n")

    }

    initializeMethodSourcePart.addSourcePart(ObjCStringSourcePart(contentsBuilder.toString()))

  }

}

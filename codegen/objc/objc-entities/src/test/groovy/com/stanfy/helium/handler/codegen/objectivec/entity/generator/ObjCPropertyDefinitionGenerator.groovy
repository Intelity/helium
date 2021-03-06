package com.stanfy.helium.handler.codegen.objectivec.entity.generator

import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCType
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.AccessModifier
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.AtomicModifier
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCPropertyDefinition
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/25/14.
 */
class ObjCPropertyDefinitionGenerator extends Specification{

  ObjCPropertyDefinition propertyDefinition;
  String propertyName;
  String propertyType;

  def setup() {
    propertyName = "uniqueString";
    propertyType = "uniquetype";
  }

  def "should have @property definition"() {
    when:
    propertyDefinition = new ObjCPropertyDefinition(propertyName, new ObjCType(propertyType));
    then:
    propertyDefinition.asString().contains("@property");
  }

  def "should contain property name and type"() {
    when:
    propertyDefinition = new ObjCPropertyDefinition(propertyName,new ObjCType(propertyType));
    then:
    propertyDefinition.asString().contains(propertyName);
    propertyDefinition.asString().contains(propertyType);
  }

  def "should contain correct access modifiers"(AccessModifier accessModifier, AtomicModifier atomicModifier,
                                                String accessModifierString, String atomicModifierString) {
    given:
    propertyDefinition = new ObjCPropertyDefinition(propertyName, new ObjCType(propertyType, false), accessModifier, atomicModifier);
    def propertyRegexp = "\\s*@property\\s*\\(\\s*" + atomicModifierString + "\\s*,\\s*" + accessModifierString + "\\s*\\)\\s*" + propertyType + "\\s*" + propertyName + "\\s*" + ";"

    expect:
    propertyDefinition.asString().contains("("+atomicModifierString+", "+accessModifierString+")");
    propertyDefinition.asString().matches(propertyRegexp)

    where:
    accessModifier        | atomicModifier           | accessModifierString | atomicModifierString
    AccessModifier.ASSIGN | AtomicModifier.ATOMIC    | "assign"             | "atomic"
    AccessModifier.ASSIGN | AtomicModifier.NONATOMIC | "assign"             | "nonatomic"
    AccessModifier.STRONG | AtomicModifier.ATOMIC    | "strong"             | "atomic"
    AccessModifier.STRONG | AtomicModifier.NONATOMIC | "strong"             | "nonatomic"
    AccessModifier.RETAIN | AtomicModifier.ATOMIC    | "retain"             | "atomic"
    AccessModifier.RETAIN | AtomicModifier.NONATOMIC | "retain"             | "nonatomic"
    AccessModifier.WEAK   | AtomicModifier.ATOMIC    | "weak"               | "atomic"
    AccessModifier.WEAK   | AtomicModifier.NONATOMIC | "weak"               | "nonatomic"
    AccessModifier.COPY   | AtomicModifier.ATOMIC    | "copy"               | "atomic"
    AccessModifier.COPY   | AtomicModifier.NONATOMIC | "copy"               | "nonatomic"


  }



}

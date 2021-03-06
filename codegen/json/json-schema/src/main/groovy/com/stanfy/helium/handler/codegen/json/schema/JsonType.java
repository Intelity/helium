package com.stanfy.helium.handler.codegen.json.schema;

import java.util.Locale;

/** JSON schema types. */
public enum JsonType {
  ARRAY,
  BOOLEAN,
  INTEGER,
  NUMBER,
  OBJECT,
  STRING,
  ENUM,

  ANY, // Used to mark generic type.

  FILE; // Not really a JSON type but something that can be used in Swagger.

  public String getName() {
    return this.name().toLowerCase(Locale.US);
  }
}

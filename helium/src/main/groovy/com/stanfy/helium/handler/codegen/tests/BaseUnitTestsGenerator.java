package com.stanfy.helium.handler.codegen.tests;

import com.squareup.javawriter.JavaWriter;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.stanfy.helium.Helium;
import com.stanfy.helium.handler.tests.RestApiMethods;
import com.stanfy.helium.internal.dsl.ProjectDsl;
import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.handler.codegen.BaseGenerator;
import com.stanfy.helium.model.MethodType;
import com.stanfy.helium.model.Project;
import com.stanfy.helium.model.Service;
import com.stanfy.helium.internal.utils.Names;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.squareup.javawriter.JavaWriter.stringLiteral;

/**
 * Base generator.
 */
abstract class BaseUnitTestsGenerator implements Handler {

  /** Encoding constant string. */
  protected static final String UTF_8 = "UTF-8";

  /** Default package name. */
  protected static final String DEFAULT_PACKAGE_NAME = "spec.tests.rest";

  /** Public method. */
  protected static final Set<Modifier> PUBLIC = Collections.singleton(Modifier.PUBLIC);
  /** Protected method. */
  protected static final Set<Modifier> PROTECTED = Collections.singleton(Modifier.PROTECTED);

  private final String prefix;

  /** Output directory. */
  private final File srcOutput;

  /** Resources output. */
  private final File resourcesOutput;

  /** Package name for tests. */
  private final String packageName;

  public BaseUnitTestsGenerator(final File srcOutput, final File resourcesOutput, final String packageName,
                                final String prefix) {
    checkDirectory(srcOutput, "Sources output");
    if (resourcesOutput != null) {
      checkDirectory(resourcesOutput, "Resources output");
    }

    this.srcOutput = srcOutput;
    this.resourcesOutput = resourcesOutput == null ? srcOutput : resourcesOutput;
    this.packageName = packageName == null ? DEFAULT_PACKAGE_NAME : packageName;
    this.prefix = prefix;
  }

  private static void checkDirectory(final File dir, final String name) {
    if (dir == null) {
      throw new IllegalArgumentException(name + " is not defined");
    }
    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        throw new IllegalArgumentException(name + " does not exist and cannot be created");
      }
    } else if (!dir.isDirectory()) {
      throw new IllegalArgumentException(name + " is not a directory");
    }
  }

  public File getSrcOutput() {
    return srcOutput;
  }

  public File getResourcesOutput() {
    return resourcesOutput;
  }

  public String getPackageName() {
    return packageName;
  }

  private File withPackage(final File dir) {
    File result = new File(dir, Names.packageNameToPath(packageName));
    if (!result.mkdirs() && !result.exists()) {
      throw new IllegalStateException("Cannot create dir " + result);
    }
    return result;
  }

  public File getSourcesPackageDir() {
    return withPackage(getSrcOutput());
  }

  public File getResourcesPackageDir() {
    return withPackage(getResourcesOutput());
  }

  File getSpecFile() {
    return new File(getResourcesPackageDir(), RestApiMethods.TEST_SPEC_NAME + "-" + prefix);
  }

  protected void startTest(final JavaWriter java, final Service service, final Project project) throws IOException {
    java.emitPackage(getPackageName())
        .emitImports(
            Test.class.getName(),
            MethodType.class.getName(), RestApiMethods.class.getName(), URI.class.getName(),
            Request.class.getName(), Response.class.getName(), OkHttpClient.class.getName(),
            RequestBody.class.getName(), MediaType.class.getName(),
            Helium.class.getName()
        )
        .emitStaticImports(Assertions.class.getName() + ".assertThat")
        .beginType(getClassName(service), "class", PUBLIC, RestApiMethods.class.getSimpleName());

    java.beginConstructor(PUBLIC);
    emitConstructorCode(java);
    java.endConstructor();

    java.emitAnnotation(Override.class);
    java.beginMethod("void", "prepareVariables", PROTECTED, "final Helium", "helium");
    if (project instanceof ProjectDsl) {
      Map<?, ?> varMap = ((ProjectDsl) project).getVariablesBinding().getVariables();
      for (Map.Entry entry : varMap.entrySet()) {
        String name = String.valueOf(entry.getKey());
        if ("baseDir".equals(name)) {
          continue;
        }
        String value = String.valueOf(entry.getValue());
        java.emitStatement("helium.set(%1$s, %2$s)", stringLiteral(name), stringLiteral(value));
      }
    }
    java.endMethod();
    java.emitEmptyLine();
  }

  protected void emitConstructorCode(final JavaWriter java) { }

  protected File getTestFile(final String className) {
    return new File(getSourcesPackageDir(), className + ".java");
  }

  private JavaWriter createTestsWriter(final String className) {
    File dst = getTestFile(className);
    try {
      OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dst), UTF_8);
      return new JavaWriter(out);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract String getClassName(final Service service);

  protected void eachService(final Project project, final ServiceHandler handler) throws IOException {
    for (Service service : project.getServices()) {
      BaseGenerator.ensureServiceNamePresent(service);
      String className = getClassName(service);
      JavaWriter writer = createTestsWriter(className);
      boolean typeWritten = false;
      try {
        startTest(writer, service, project);
        typeWritten = handler.process(service, writer);
        writer.endType();
      } finally {
        writer.close();
        if (!typeWritten) {
          //noinspection ResultOfMethodCallIgnored
          getTestFile(className).delete();
        }
      }
    }
  }

  public interface ServiceHandler {
    boolean process(final Service service, final JavaWriter writer);
  }

}

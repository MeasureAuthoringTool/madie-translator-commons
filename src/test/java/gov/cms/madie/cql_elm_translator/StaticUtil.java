package gov.cms.madie.cql_elm_translator;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class StaticUtil {

  public static void setPublicStaticFinalField(Class<?> clazz, String fieldName, Object newValue)
      throws Exception {

    // Ensure class initialization (important for some JVMs / situations)
    Class.forName(
        clazz.getName(),
        true,
        clazz
            .getClassLoader()); // [5](https://stackoverflow.com/questions/75713240/why-must-create-or-call-of-the-java-class-before-modifying-static-final-field-of)

    Field field = clazz.getField(fieldName);
    field.setAccessible(true);

    int mods = field.getModifiers();
    if (!Modifier.isStatic(mods)) throw new IllegalArgumentException(fieldName + " must be static");

    if (newValue != null && !field.getType().isInstance(newValue)) {
      throw new IllegalArgumentException("Wrong type for " + fieldName);
    }

    // If it isn't final, plain reflection works
    if (!Modifier.isFinal(mods)) {
      field.set(null, newValue);
      return;
    }

    // Try clearing FINAL by editing Field::modifiers (often needs --add-opens)
    try {
      VarHandle MODIFIERS =
          MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup())
              .findVarHandle(Field.class, "modifiers", int.class);
      MODIFIERS.set(field, mods & ~Modifier.FINAL);
      field.set(null, newValue);
      return;
    } catch (Throwable ignored) {
      // Fallback to Unsafe
    }

    // Unsafe fallback (test-only). Offset APIs are deprecated in newer JDKs.
    // [6](https://bugs.openjdk.org/browse/JDK-8278432)[7](https://www.baeldung.com/java-unsafe)
    Object unsafe = getUnsafe();
    Class<?> uc = unsafe.getClass();

    Object base = uc.getMethod("staticFieldBase", Field.class).invoke(unsafe, field);
    long offset = (long) uc.getMethod("staticFieldOffset", Field.class).invoke(unsafe, field);

    uc.getMethod("putObjectVolatile", Object.class, long.class, Object.class)
        .invoke(unsafe, base, offset, newValue);
  }

  private static Object getUnsafe() throws Exception {
    Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
    Field f = unsafeClass.getDeclaredField("theUnsafe");
    f.setAccessible(true);
    return f.get(null); // [7](https://www.baeldung.com/java-unsafe)
  }
}

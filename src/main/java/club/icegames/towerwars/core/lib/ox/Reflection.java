package club.icegames.towerwars.core.lib.ox;

import static club.icegames.towerwars.core.lib.ox.util.Utils.first;
import static club.icegames.towerwars.core.lib.ox.util.Utils.propagate;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import club.icegames.towerwars.core.lib.ox.util.Utils;
import club.icegames.towerwars.core.lib.ox.x.XList;
import club.icegames.towerwars.core.lib.ox.x.XOptional;
import sun.misc.Unsafe;

public class Reflection {

  private static final Objenesis objenesis = new ObjenesisStd(true);
  private static final Map<String, Field> fieldCache = Maps.newConcurrentMap();
  private static final Map<String, Method> methodCache = Maps.newConcurrentMap();
  private static final Field modifiersField;

  private static final Field NULL_FIELD;
  private static final Method NULL_METHOD;
  static {
    try {
      NULL_FIELD = Reflection.class.getDeclaredField("fieldCache");
      NULL_METHOD = Reflection.class.getDeclaredMethod("nullMethod");
    } catch (Exception e) {
      throw propagate(e);
    }
  }

  static {
    disableWarning();
    try {
      modifiersField = Field.class.getDeclaredField("modifiers");
    } catch (Exception e) {
      throw propagate(e);
    }
    modifiersField.setAccessible(true);
  }

  public static void nullMethod() {
    // this is for the NULL_METHOD field
  }

  /**
   * Turns off the warning that puts 5 warnings out to the console:
   * 
   * WARNING: An illegal reflective access operation has occurred
   */
  public static void disableWarning() {
    try {
      Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
      theUnsafe.setAccessible(true);
      Unsafe u = (Unsafe) theUnsafe.get(null);

      Class<?> c = Class.forName("jdk.internal.module.IllegalAccessLogger");
      Field logger = c.getDeclaredField("logger");
      u.putObjectVolatile(c, u.staticFieldOffset(logger), null);
    } catch (ClassNotFoundException e) {
      // do nothing
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void load(Class<?> c) {
    try {
      Class.forName(c.getName());
    } catch (ClassNotFoundException e) {
      throw propagate(e);
    }
  }

  public static <T> T get(Object o, String fieldName) {
    Field field = getField(o.getClass(), fieldName);
    return get(o, field);
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(Object o, Field field) {
    if (field == null) {
      return null;
    }
    try {
      return (T) field.get(o);
    } catch (Exception e) {
      throw propagate(e);
    }
  }

  /**
   * Sets a static field.
   */
  public static void set(Class<?> c, String fieldName, Object value) {
    Field field = getField(c, fieldName);
    try {
      field.set(null, value);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw propagate(e);
    }
  }

  public static void set(Object o, String fieldName, Object value) {
    Field field = getField(o.getClass(), fieldName);
    if (field == null) {
      return;
    }
    value = convert(value, field.getGenericType());
    try {
      field.set(o, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Object convert(Object value, Type targetType) {
    Class<?> wrappedClass = TypeToken.of(targetType).getRawType();
    Class<?> targetClass;
    if (wrappedClass == Optional.class || wrappedClass == XOptional.class) {
      targetClass = getTypeArgument(targetType);
    } else {
      targetClass = wrappedClass;
    }
    if (value instanceof String) {
      if (targetClass.isEnum()) {
        value = Utils.parseEnum((String) value, (Class<? extends Enum>) targetClass);
      } else if (targetClass == LocalDateTime.class) {
        value = LocalDateTime.parse((String) value);
      } else if (targetClass == Json.class) {
        value = new Json((String) value);
      } else if (targetClass == LocalTime.class) {
        value = LocalTime.parse((String) value);
      } else if (targetClass == UUID.class) {
        value = UUID.fromString((String) value);
      } else if (targetClass == Percent.class) {
        value = Percent.parse((String) value);
      } else if (targetClass == ZoneId.class) {
        value = ZoneId.of((String) value);
      }
    } else if (value instanceof java.sql.Date) {
      if (targetClass == LocalDate.class) {
        value = ((java.sql.Date) value).toLocalDate();
      }
    } else if (value instanceof Long) {
      if (targetClass == Money.class) {
        value = Money.fromLong((Long) value);
      } else if (targetClass == Instant.class) {
        value = Instant.ofEpochMilli((Long) value);
      }
    } else if (value instanceof Integer) {
      if (targetClass == Money.class) {
        value = Money.fromLong((Integer) value);
      } else if (targetClass == Long.class) {
        value = ((Integer) value).longValue();
      } else if (targetClass == String.class) {
        value = value.toString();
      }
    }

    if (value != null) {
      if (!targetClass.isPrimitive() && !targetClass.isAssignableFrom(value.getClass())) {
        throw new IllegalStateException(
            "Trying to convert " + value.getClass() + " to incompatible type: " + targetClass.getSimpleName());
        // throw new IllegalStateException(
        // "Trying to set " + field.getType().getSimpleName() + " " + o.getClass().getSimpleName() + "."
        // + field.getName() + " to incompatible type: " + value.getClass());
      }
    }
    
    if (wrappedClass == Optional.class) {
      value = Optional.ofNullable(value);
    } else if (wrappedClass == XOptional.class) {
      value = XOptional.ofNullable(value);
    }

    return value;
  }

  public static Object call(Object o, String methodName) {
    try {
      for (Method m : o.getClass().getDeclaredMethods()) {
        if (m.getName().equals(methodName)) {
          if (m.getParameterCount() == 0) {
            return m.invoke(o);
          }
        }
      }
      for (Method m : o.getClass().getMethods()) {
        if (m.getName().equals(methodName)) {
          if (m.getParameterCount() == 0) {
            return m.invoke(o);
          }
        }
      }
      throw new RuntimeException("Method not found: " + o.getClass().getSimpleName() + "." + methodName);
    } catch (Exception e) {
      Log.error("Problem calling method: " + methodName);
      throw propagate(e);
    }
  }

  /**
   * Constructs an instance of the class without calling any constructors.
   */
  public static <T> T newInstance(Class<T> c) {
    return objenesis.newInstance(c);
  }

  /**
   * Constructs an instance of the class using its default (empty) constructor.
   */
  public static <T> T constructNewInstance(Class<T> c) {
    try {
      return c.getConstructor().newInstance();
    } catch (Exception e) {
      throw propagate(e);
    }
  }

  public static Field getField(Object o, String fieldName) {
    return getField(o.getClass(), fieldName);
  }

  public static Field getField(Class<?> c, String fieldName) {
    String key = c.getName() + fieldName;

    Field ret = fieldCache.get(key);
    if (ret != null) {
      return ret == NULL_FIELD ? null : ret;
    }

    try {
      ret = c.getDeclaredField(fieldName);
      ret.setAccessible(true);
      modifiersField.setInt(ret, ret.getModifiers() & ~Modifier.FINAL);
    } catch (NoSuchFieldException e) {
      Class<?> parent = c.getSuperclass();
      if (parent == null) {
        ret = null;
      } else {
        ret = getField(parent, fieldName);
      }
    } catch (IllegalAccessException e) {
      throw propagate(e);
    }

    if (ret == null) {
      fieldCache.put(key, NULL_FIELD);
    } else {
      fieldCache.put(key, ret);
    }

    return ret;
  }

  public static XList<Field> getFields(Class<?> c) {
    return XList.of(c.getDeclaredFields());
  }

  public static XList<Method> getMethods(Class<?> c) {
    return XList.of(c.getDeclaredMethods());
  }

  public static Method getMethod(Class<?> c, String methodName) {
    return methodCache.computeIfAbsent(c.getName() + methodName, s -> {
      try {
        Method ret = c.getMethod(methodName);
        ret.setAccessible(true);
        return ret;
      } catch (NoSuchMethodException e) {
        return NULL_METHOD;
      } catch (Exception e) {
        throw propagate(e);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public static <T> T callMethod(Object o, String methodName) {
    Method m = getMethod(o.getClass(), methodName);
    try {
      return (T) m.invoke(o);
    } catch (Exception e) {
      throw propagate(e);
    }
  }

  /*
   * For example:<br>
   * public abstract class AbstractDB<T><br>
   * public class UserDB extends AbstractDB<User> <br>
   * <br>
   * If this method is given UserDB.class as input, it will return User.class
   */
  public static Class<?> getGenericClass(Class<?> c) {
    Type t = c.getGenericSuperclass();
    if (t instanceof ParameterizedType) {
      return getTypeArgument(t);
    } else {
      return null;
    }
  }

  private static Class<?> getTypeArgument(Type t) {
    Type type = ((ParameterizedType) t).getActualTypeArguments()[0];
    if (type instanceof ParameterizedType) {
      type = ((ParameterizedType) type).getRawType();
    }
    return (Class<?>) type;
  }

  @SuppressWarnings("unchecked")
  public static <T> XList<Constructor<T>> getConstructors(Class<?> c) {
    return XList.of((Constructor<T>[]) c.getConstructors());
  }

  @SuppressWarnings("unchecked")
  public static <T> XList<Class<? extends T>> findClasses(String packageName, Class<T> classType) {
    XList<Class<? extends T>> ret = XList.create();
    for (Class<?> c : findClasses(packageName)) {
      if (classType.isAssignableFrom(c)) {
        ret.add((Class<? extends T>) c);
      }
    }
    return ret;
  }

  public static List<Class<?>> findClasses(String packageName) {
    try {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      String path = packageName.replace('.', '/');
      Enumeration<URL> resources = classLoader.getResources(path);
      List<URI> dirs = Lists.newArrayList();
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        dirs.add(resource.toURI());
      }
      Set<String> classes = Sets.newHashSet();
      for (URI directory : dirs) {
        classes.addAll(findClasses(directory, packageName));
      }
      List<Class<?>> classList = Lists.newArrayList();
      for (String className : classes) {
        if (className.startsWith(packageName) && !className.contains("$")) {
          classList.add(Class.forName(className));
        }
      }
      return classList;
    } catch (Exception e) {
      throw propagate(e);
    }
  }

  private static Set<String> findClasses(URI directory, String packageName) throws Exception {
    final String scheme = directory.getScheme();

    if (scheme.equals("jar") && directory.getSchemeSpecificPart().contains("!")) {
      return findClassesInJar(directory);
    } else if (scheme.equals("file")) {
      return findClassesInFileSystemDirectory(directory, packageName);
    }

    throw new IllegalStateException(
        "cannot handle URI with scheme [" + scheme + "]" +
            "; received directory=[" + directory + "], packageName=[" + packageName + "]");
  }

  private static Set<String> findClassesInJar(URI jarDirectory) throws Exception {
    Set<String> ret = Sets.newHashSet();

    URL jar = new URL(first(jarDirectory.getSchemeSpecificPart(), "!"));
    ZipInputStream zip = new ZipInputStream(jar.openStream());
    while (true) {
      ZipEntry entry = zip.getNextEntry();
      if (entry == null) {
        break;
      }
      String name = entry.getName();
      if (name.endsWith(".class") && !name.contains("$")) {
        ret.add(name.substring(0, name.length() - 6).replace('/', '.'));
      }
    }

    return ret;
  }

  private static Set<String> findClassesInFileSystemDirectory(URI fileSystemDirectory, String packageName) {
    Set<String> ret = Sets.newHashSet();

    for (File file : new File(fileSystemDirectory).listFiles()) {
      String name = file.getName();
      if (file.isDirectory()) {
        ret.addAll(findClassesInFileSystemDirectory(file.getAbsoluteFile().toURI(), packageName + "." + name));
      } else if (name.endsWith(".class")) {
        ret.add(packageName + '.' + name.substring(0, name.length() - 6));
      }
    }

    return ret;
  }

  public static boolean isAbstract(Class<?> c) {
    return Modifier.isAbstract(c.getModifiers());
  }

  public static boolean isPublic(Field f) {
    return Modifier.isPublic(f.getModifiers());
  }

  public static ClassWrapper is(Class<?> a) {
    return new ClassWrapper(a);
  }

  public static class ClassWrapper {

    private final Class<?> a;

    public ClassWrapper(Class<?> a) {
      this.a = a;
    }

    public boolean subclassOf(Class<?> b) {
      return b.isAssignableFrom(a);
    }

    public boolean superclassOf(Class<?> b) {
      return a.isAssignableFrom(b);
    }
  }

}

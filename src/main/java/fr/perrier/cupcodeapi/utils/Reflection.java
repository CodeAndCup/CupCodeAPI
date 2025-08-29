package fr.perrier.cupcodeapi.utils;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Reflection {

    /**
     * Gets the NMS class from the given class name.
     *
     * @param className The name of the NMS class.
     * @return The NMS class, or null if not found.
     */
    public static Class<?> getNMSClass(final String className) {
        try {
            return PackageType.MINECRAFT_SERVER.getClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the OBC class from the given class name.
     *
     * @param className The name of the OBC class.
     * @return The OBC class, or null if not found.
     */
    public static Class<?> getOBCClass(final String className) {
        try {
            return PackageType.CRAFTBUKKIT.getClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sends a packet to the given player.
     *
     * @param player The player to send the packet to.
     * @param packet The packet to send.
     */
    public static void sendPacket(final Player player, final Object packet) {
        try {
            final Class<?> packetClass = getNMSClass("Packet");
            final Class<?> entityPlayerClass = getNMSClass("EntityPlayer");
            final Field playerConnectionField = getField(entityPlayerClass, "playerConnection");
            final Method sendPacketMethod = getMethod(playerConnectionField.getType(), "sendPacket", packetClass);
            final Object entityPlayer = getHandle(player);
            final Object playerConnection = playerConnectionField.get(entityPlayer);
            sendPacketMethod.invoke(playerConnection, packet);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Makes a method from the given class, method name and parameters.
     *
     * @param clazz      The class to get the method from.
     * @param methodName The name of the method.
     * @param paramaters The parameters of the method.
     * @return The method, or null if not found.
     */
    public static Method makeMethod(final Class<?> clazz, final String methodName, final Class<?>... paramaters) {
        try {
            return clazz.getDeclaredMethod(methodName, paramaters);
        } catch (NoSuchMethodException ex2) {
            return null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Calls the given method on the given instance with the given parameters.
     *
     * @param method     The method to call.
     * @param instance   The instance to call the method on.
     * @param paramaters The parameters to pass to the method.
     * @param <T>        The return type of the method.
     * @return The result of the method call.
     */
    public static <T> T callMethod(final Method method, final Object instance, final Object... paramaters) {
        if (method == null) {
            throw new RuntimeException("No such method");
        }
        method.setAccessible(true);
        try {
            return (T) method.invoke(instance, paramaters);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause());
        } catch (Exception ex2) {
            throw new RuntimeException(ex2);
        }
    }

    /**
     * Makes a constructor from the given class and parameter types.
     *
     * @param clazz          The class to get the constructor from.
     * @param paramaterTypes The parameter types of the constructor.
     * @param <T>            The type of the class.
     * @return The constructor, or null if not found.
     */
    public static <T> Constructor<T> makeConstructor(final Class<?> clazz, final Class<?>... paramaterTypes) {
        try {
            return (Constructor<T>) clazz.getConstructor(paramaterTypes);
        } catch (NoSuchMethodException ex2) {
            return null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Calls the given constructor with the given parameters.
     *
     * @param constructor The constructor to call.
     * @param paramaters  The parameters to pass to the constructor.
     * @param <T>         The type of the class.
     * @return The result of the constructor call.
     */
    public static <T> T callConstructor(final Constructor<T> constructor, final Object... paramaters) {
        if (constructor == null) {
            throw new RuntimeException("No such constructor");
        }
        constructor.setAccessible(true);
        try {
            return constructor.newInstance(paramaters);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause());
        } catch (Exception ex2) {
            throw new RuntimeException(ex2);
        }
    }

    /**
     * Makes a field from the given class and field name.
     *
     * @param clazz The class to get the field from.
     * @param name  The name of the field.
     * @return The field, or null if not found.
     */
    public static Field makeField(final Class<?> clazz, final String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException ex2) {
            return null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the value of the given field from the given instance.
     *
     * @param field    The field to get the value from.
     * @param instance The instance to get the value from.
     * @param <T>      The type of the field.
     * @return The value of the field.
     */
    public static <T> T getField(final Field field, final Object instance) {
        if (field == null) {
            throw new RuntimeException("No such field");
        }
        field.setAccessible(true);
        try {
            return (T) field.get(instance);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Sets the value of the given field on the given instance.
     *
     * @param field    The field to set the value on.
     * @param instance The instance to set the value on.
     * @param value    The value to set.
     */
    public static void setField(final Field field, final Object instance, final Object value) {
        if (field == null) {
            throw new RuntimeException("No such field");
        }
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the class from the given class name.
     *
     * @param name The name of the class.
     * @return The class, or null if not found.
     */
    public static Class<?> getClass(final String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    /**
     * Gets the class from the given class name and superclass.
     *
     * @param name       The name of the class.
     * @param superClass The superclass of the class.
     * @param <T>        The type of the superclass.
     * @return The class, or null if not found or not a subclass of the superclass.
     */
    public static <T> Class<? extends T> getClass(final String name, final Class<T> superClass) {
        try {
            return Class.forName(name).asSubclass(superClass);
        } catch (ClassCastException | ClassNotFoundException ignored) {
            return null;
        }
    }

    /**
     * Gets the handle of the given object.
     *
     * @param obj The object to get the handle from.
     * @return The handle of the object, or null if not found.
     */
    public static Object getHandle(final Object obj) {
        try {
            return getMethod(obj.getClass(), "getHandle").invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the field from the given class and field name.
     *
     * @param clazz The class to get the field from.
     * @param name  The name of the field.
     * @return The field, or null if not found.
     */
    public static Field getField(final Class<?> clazz, final String name) {
        try {
            final Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Compares two class lists for equality.
     * The lists are considered equal if they have the same length and
     * all corresponding classes are equal.
     * The order of the classes in the lists matters.
     *
     * @param l1 The first class list to compare.
     * @param l2 The class lists to compare.
     * @return true if the lists are equal, false otherwise.
     */
    public static boolean compareClassList(final Class<?>[] l1, final Class<?>[] l2) {
        boolean equal = true;
        if (l1.length != l2.length) {
            return false;
        }
        for (int i = 0; i < l1.length; ++i) {
            if (!Objects.equals(l1[i], l2[i])) {
                equal = false;
                break;
            }
        }
        return equal;
    }

    /**
     * Gets the constructor from the given class and parameter types.
     *
     * @param clazz          The class to get the constructor from.
     * @param parameterTypes The parameter types of the constructor.
     * @return The constructor.
     * @throws NoSuchMethodException If there is no such constructor in this class with the specified parameter types.
     */
    public static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>... parameterTypes) throws NoSuchMethodException {
        final Class<?>[] primitiveTypes = DataType.getPrimitive(parameterTypes);
        for (final Constructor<?> constructor : clazz.getConstructors()) {
            if (DataType.compare(DataType.getPrimitive(constructor.getParameterTypes()), primitiveTypes)) {
                return constructor;
            }
        }
        throw new NoSuchMethodException("There is no such constructor in this class with the specified parameter types");
    }

    /**
     * Gets the constructor from the given class name, package type and parameter types.
     *
     * @param className      The name of the class to get the constructor from.
     * @param packageType    The package type of the class to get the constructor from.
     * @param parameterTypes The parameter types of the constructor.
     * @return The constructor.
     * @throws NoSuchMethodException  If there is no such constructor in this class with the specified parameter types.
     * @throws ClassNotFoundException If the class with the specified name and package type cannot be found.
     */
    public static Constructor<?> getConstructor(final String className, final PackageType packageType, final Class<?>... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        return getConstructor(packageType.getClass(className), parameterTypes);
    }

    /**
     * Instantiates an object of the given class with the given arguments.
     *
     * @param clazz     The class to instantiate.
     * @param arguments The arguments to pass to the constructor.
     * @return The instantiated object.
     * @throws InstantiationException    If the class that declares the underlying constructor represents an abstract class.
     * @throws IllegalAccessException    If this Constructor object is enforcing Java language access control and the underlying constructor is inaccessible.
     * @throws IllegalArgumentException  If the number of actual and formal parameters differ; if an unwrapping conversion for primitive arguments fails; or if, after possible unwrapping, a parameter value cannot be converted to the corresponding formal parameter type by a method invocation conversion.
     * @throws InvocationTargetException If the underlying constructor throws an exception.
     * @throws NoSuchMethodException     If there is no such constructor in this class with the specified parameter types.
     */
    public static Object instantiateObject(final Class<?> clazz, final Object... arguments) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        return getConstructor(clazz, DataType.getPrimitive(arguments)).newInstance(arguments);
    }

    /**
     * Instantiates an object of the given class name and package type with the given arguments.
     *
     * @param className   The name of the class to instantiate.
     * @param packageType The package type of the class to instantiate.
     * @param arguments   The arguments to pass to the constructor.
     * @return The instantiated object.
     * @throws InstantiationException    If the class that declares the underlying constructor represents an abstract class.
     * @throws IllegalAccessException    If this Constructor object is enforcing Java language access control and the underlying constructor is inaccessible.
     * @throws IllegalArgumentException  If the number of actual and formal parameters differ; if an unwrapping conversion for primitive arguments fails; or if, after possible unwrapping, a parameter value cannot be converted to the corresponding formal parameter type by a method invocation conversion.
     * @throws InvocationTargetException If the underlying constructor throws an exception.
     * @throws NoSuchMethodException     If there is no such constructor in this class with the specified parameter types.
     * @throws ClassNotFoundException    If the class with the specified name and package type cannot be found.
     */
    public static Object instantiateObject(final String className, final PackageType packageType, final Object... arguments) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        return instantiateObject(packageType.getClass(className), arguments);
    }

    /**
     * Gets the method from the given class, method name and parameter types.
     *
     * @param clazz          The class to get the method from.
     * @param methodName     The name of the method.
     * @param parameterTypes The parameter types of the method.
     * @return The method.
     * @throws NoSuchMethodException If there is no such method in this class with the specified name and parameter types.
     */
    public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) throws NoSuchMethodException {
        final Class<?>[] primitiveTypes = DataType.getPrimitive(parameterTypes);
        for (final Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName) && DataType.compare(DataType.getPrimitive(method.getParameterTypes()), primitiveTypes)) {
                return method;
            }
        }
        throw new NoSuchMethodException("There is no such method in this class with the specified name and parameter types");
    }

    /**
     * Gets the method from the given class name, package type, method name and parameter types.
     *
     * @param className      The name of the class to get the method from.
     * @param packageType    The package type of the class to get the method from.
     * @param methodName     The name of the method.
     * @param parameterTypes The parameter types of the method.
     * @return The method.
     * @throws NoSuchMethodException  If there is no such method in this class with the specified name and parameter types.
     * @throws ClassNotFoundException If the class with the specified name and package type cannot be found.
     */
    public static Method getMethod(final String className, final PackageType packageType, final String methodName, final Class<?>... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        return getMethod(packageType.getClass(className), methodName, parameterTypes);
    }

    /**
     * Invokes the given method on the given instance with the given arguments.
     *
     * @param instance   The instance to invoke the method on.
     * @param methodName The name of the method to invoke.
     * @param arguments  The arguments to pass to the method.
     * @return The result of the method invocation.
     * @throws IllegalAccessException    If this Method object is enforcing Java language access control and the underlying method is inaccessible.
     * @throws IllegalArgumentException  If the method is an instance method and the specified object argument is not an instance of the class or interface declaring the underlying method (or of a subclass or implementor thereof); if the number of actual and formal parameters differ; if an unwrapping conversion for primitive arguments fails; or if, after possible unwrapping, a parameter value cannot be converted to the corresponding formal parameter type by a method invocation conversion.
     * @throws InvocationTargetException If the underlying method throws an exception.
     * @throws NoSuchMethodException     If there is no such method in this class with the specified name and parameter types.
     */
    public static Object invokeMethod(final Object instance, final String methodName, final Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        return getMethod(instance.getClass(), methodName, DataType.getPrimitive(arguments)).invoke(instance, arguments);
    }

    /**
     * Invokes the given method on the given instance with the given arguments.
     *
     * @param instance   The instance to invoke the method on.
     * @param clazz      The class to get the method from.
     * @param methodName The name of the method to invoke.
     * @param arguments  The arguments to pass to the method.
     * @return The result of the method invocation.
     * @throws IllegalAccessException    If this Method object is enforcing Java language access control and the underlying method is inaccessible.
     * @throws IllegalArgumentException  If the method is an instance method and the specified object argument is not an instance of the class or interface declaring the underlying method (or of a subclass or implementor thereof); if the number of actual and formal parameters differ; if an unwrapping conversion for primitive arguments fails; or if, after possible unwrapping, a parameter value cannot be converted to the corresponding formal parameter type by a method invocation conversion.
     * @throws InvocationTargetException If the underlying method throws an exception.
     * @throws NoSuchMethodException     If there is no such method in this class with the specified name and parameter types.
     */
    public static Object invokeMethod(final Object instance, final Class<?> clazz, final String methodName, final Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        return getMethod(clazz, methodName, DataType.getPrimitive(arguments)).invoke(instance, arguments);
    }

    /**
     * Invokes the given method on the given instance with the given arguments.
     *
     * @param instance   The instance to invoke the method on.
     * @param className  The name of the class to get the method from.
     * @param packageType The package type of the class to get the method from.
     * @param methodName The name of the method to invoke.
     * @param arguments  The arguments to pass to the method.
     * @return The result of the method invocation.
     * @throws IllegalAccessException    If this Method object is enforcing Java language access control and the underlying method is inaccessible.
     * @throws IllegalArgumentException  If the method is an instance method and the specified object argument is not an instance of the class or interface declaring the underlying method (or of a subclass or implementor thereof); if the number of actual and formal parameters differ; if an unwrapping conversion for primitive arguments fails; or if, after possible unwrapping, a parameter value cannot be converted to the corresponding formal parameter type by a method invocation conversion.
     * @throws InvocationTargetException If the underlying method throws an exception.
     * @throws NoSuchMethodException     If there is no such method in this class with the specified name and parameter types.
     * @throws ClassNotFoundException    If the class with the specified name and package type cannot be found.
     */
    public static Object invokeMethod(final Object instance, final String className, final PackageType packageType, final String methodName, final Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        return invokeMethod(instance, packageType.getClass(className), methodName, arguments);
    }

    /**
     * Gets the field from the given class, declared flag and field name.
     *
     * @param clazz      The class to get the field from.
     * @param declared   Whether to get a declared field or not.
     * @param fieldName  The name of the field.
     * @return The field.
     * @throws NoSuchFieldException If there is no such field in this class with the specified name.
     * @throws SecurityException    If a security manager, s, is present and any of the following conditions is met:
     *                              <ul>
     *                              <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPackageAccess() denies access to the package of this class.</li>
     *                              <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link RuntimePermission}("accessDeclaredMembers")</li>
     *                              <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link ReflectPermission}("suppressAccessChecks")</li>
     *                              </ul>
     */
    public static Field getField(final Class<?> clazz, final boolean declared, final String fieldName) throws NoSuchFieldException, SecurityException {
        final Field field = declared ? clazz.getDeclaredField(fieldName) : clazz.getField(fieldName);
        field.setAccessible(true);
        return field;
    }

    /**
     * Gets the field from the given class name, package type, declared flag and field name.
     *
     * @param className  The name of the class to get the field from.
     * @param packageType The package type of the class to get the field from.
     * @param declared   Whether to get a declared field or not.
     * @param fieldName  The name of the field.
     * @return The field.
     * @throws NoSuchFieldException  If there is no such field in this class with the specified name.
     * @throws SecurityException     If a security manager, s, is present and any of the following conditions is met:
     *                               <ul>
     *                               <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPackageAccess() denies access to the package of this class.</li>
     *                               <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link RuntimePermission}("accessDeclaredMembers")</li>
     *                               <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link ReflectPermission}("suppressAccessChecks")</li>
     *                               </ul>
     * @throws ClassNotFoundException If the class with the specified name and package type cannot be found.
     */
    public static Field getField(final String className, final PackageType packageType, final boolean declared, final String fieldName) throws NoSuchFieldException, SecurityException, ClassNotFoundException {
        return getField(packageType.getClass(className), declared, fieldName);
    }

    /**
     * Gets the value of the given field from the given instance.
     *
     * @param instance  The instance to get the field value from.
     * @param clazz     The class to get the field from.
     * @param declared  Whether to get a declared field or not.
     * @param fieldName The name of the field.
     * @return The value of the field.
     * @throws IllegalArgumentException If the specified object is not an instance of the class or interface declaring the underlying field (or a subclass or implementor thereof).
     * @throws IllegalAccessException   If this Field object is enforcing Java language access control and the underlying field is either inaccessible or final.
     * @throws NoSuchFieldException     If there is no such field in this class with the specified name.
     * @throws SecurityException        If a security manager, s, is present and any of the following conditions is met:
     *                                  <ul>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPackageAccess() denies access to the package of this class.</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link RuntimePermission}("accessDeclaredMembers")</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link ReflectPermission}("suppressAccessChecks")</li>
     *                                  </ul>
     */
    public static Object getValue(final Object instance, final Class<?> clazz, final boolean declared, final String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        return getField(clazz, declared, fieldName).get(instance);
    }

    /**
     * Gets the value of the given field from the given instance.
     *
     * @param instance   The instance to get the field value from.
     * @param className  The name of the class to get the field from.
     * @param packageType The package type of the class to get the field from.
     * @param declared   Whether to get a declared field or not.
     * @param fieldName  The name of the field.
     * @return The value of the field.
     * @throws IllegalArgumentException If the specified object is not an instance of the class or interface declaring the underlying field (or a subclass or implementor thereof).
     * @throws IllegalAccessException   If this Field object is enforcing Java language access control and the underlying field is either inaccessible or final.
     * @throws NoSuchFieldException     If there is no such field in this class with the specified name.
     * @throws SecurityException        If a security manager, s, is present and any of the following conditions is met:
     *                                  <ul>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPackageAccess() denies access to the package of this class.</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link RuntimePermission}("accessDeclaredMembers")</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link ReflectPermission}("suppressAccessChecks")</li>
     *                                  </ul>
     * @throws ClassNotFoundException    If the class with the specified name and package type cannot be found.
     */
    public static Object getValue(final Object instance, final String className, final PackageType packageType, final boolean declared, final String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
        return getValue(instance, packageType.getClass(className), declared, fieldName);
    }

    /**
     * Gets the value of the given field from the given instance.
     *
     * @param instance  The instance to get the field value from.
     * @param declared  Whether to get a declared field or not.
     * @param fieldName The name of the field.
     * @return The value of the field.
     * @throws IllegalArgumentException If the specified object is not an instance of the class or interface declaring the underlying field (or a subclass or implementor thereof).
     * @throws IllegalAccessException   If this Field object is enforcing Java language access control and the underlying field is either inaccessible or final.
     * @throws NoSuchFieldException     If there is no such field in this class with the specified name.
     * @throws SecurityException        If a security manager, s, is present and any of the following conditions is met:
     *                                  <ul>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPackageAccess() denies access to the package of this class.</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link RuntimePermission}("accessDeclaredMembers")</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link ReflectPermission}("suppressAccessChecks")</li>
     *                                  </ul>
     */
    public static Object getValue(final Object instance, final boolean declared, final String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        return getValue(instance, instance.getClass(), declared, fieldName);
    }

    /**
     * Sets the value of the given field on the given instance.
     *
     * @param instance  The instance to set the field value on.
     * @param clazz     The class to get the field from.
     * @param declared  Whether to get a declared field or not.
     * @param fieldName The name of the field.
     * @param value     The value to set.
     * @throws IllegalArgumentException If the specified object is not an instance of the class or interface declaring the underlying field (or a subclass or implementor thereof).
     * @throws IllegalAccessException   If this Field object is enforcing Java language access control and the underlying field is either inaccessible or final.
     * @throws NoSuchFieldException     If there is no such field in this class with the specified name.
     * @throws SecurityException        If a security manager, s, is present and any of the following conditions is met:
     *                                  <ul>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPackageAccess() denies access to the package of this class.</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link RuntimePermission}("accessDeclaredMembers")</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link ReflectPermission}("suppressAccessChecks")</li>
     *                                  </ul>
     */
    public static void setValue(final Object instance, final Class<?> clazz, final boolean declared, final String fieldName, final Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        getField(clazz, declared, fieldName).set(instance, value);
    }

    /**
     * Sets the value of the given field on the given instance.
     *
     * @param instance   The instance to set the field value on.
     * @param className  The name of the class to get the field from.
     * @param packageType The package type of the class to get the field from.
     * @param declared   Whether to get a declared field or not.
     * @param fieldName  The name of the field.
     * @param value      The value to set.
     * @throws IllegalArgumentException If the specified object is not an instance of the class or interface declaring the underlying field (or a subclass or implementor thereof).
     * @throws IllegalAccessException   If this Field object is enforcing Java language access control and the underlying field is either inaccessible or final.
     * @throws NoSuchFieldException     If there is no such field in this class with the specified name.
     * @throws SecurityException        If a security manager, s, is present and any of the following conditions is met:
     *                                  <ul>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPackageAccess() denies access to the package of this class.</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link RuntimePermission}("accessDeclaredMembers")</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link ReflectPermission}("suppressAccessChecks")</li>
     *                                  </ul>
     * @throws ClassNotFoundException    If the class with the specified name and package type cannot be found.
     */
    public static void setValue(final Object instance, final String className, final PackageType packageType, final boolean declared, final String fieldName, final Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
        setValue(instance, packageType.getClass(className), declared, fieldName, value);
    }

    /**
     * Sets the value of the given field on the given instance.
     *
     * @param instance  The instance to set the field value on.
     * @param declared  Whether to get a declared field or not.
     * @param fieldName The name of the field.
     * @param value     The value to set.
     * @throws IllegalArgumentException If the specified object is not an instance of the class or interface declaring the underlying field (or a subclass or implementor thereof).
     * @throws IllegalAccessException   If this Field object is enforcing Java language access control and the underlying field is either inaccessible or final.
     * @throws NoSuchFieldException     If there is no such field in this class with the specified name.
     * @throws SecurityException        If a security manager, s, is present and any of the following conditions is met:
     *                                  <ul>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPackageAccess() denies access to the package of this class.</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link RuntimePermission}("accessDeclaredMembers")</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link ReflectPermission}("suppressAccessChecks")</li>
     *                                  </ul>
     */
    public static void setValue(final Object instance, final boolean declared, final String fieldName, final Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        setValue(instance, instance.getClass(), declared, fieldName, value);
    }

    /**
     * Sets the value of the given field on the given instance.
     * This method assumes that the field is declared in the class of the instance.
     *
     * @param instance  The instance to set the field value on.
     * @param fieldName The name of the field.
     * @param value     The value to set.
     * @throws IllegalArgumentException If the specified object is not an instance of the class or interface declaring the underlying field (or a subclass or implementor thereof).
     * @throws IllegalAccessException   If this Field object is enforcing Java language access control and the underlying field is either inaccessible or final.
     * @throws NoSuchFieldException     If there is no such field in this class with the specified name.
     * @throws SecurityException        If a security manager, s, is present and any of the following conditions is met:
     *                                  <ul>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPackageAccess() denies access to the package of this class.</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link RuntimePermission}("accessDeclaredMembers")</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link ReflectPermission}("suppressAccessChecks")</li>
     *                                  </ul>
     */
    public static void setValue(final Object instance, final String fieldName, final Object value) throws NoSuchFieldException, IllegalAccessException {
        setValue(instance, true, fieldName, value);
    }

    /**
     * Gets the value of the given field from the given instance.
     * This method assumes that the field is declared in the class of the instance.
     *
     * @param instance  The instance to get the field value from.
     * @param fieldName The name of the field.
     * @return The value of the field.
     * @throws IllegalArgumentException If the specified object is not an instance of the class or interface declaring the underlying field (or a subclass or implementor thereof).
     * @throws IllegalAccessException   If this Field object is enforcing Java language access control and the underlying field is either inaccessible or final.
     * @throws NoSuchFieldException     If there is no such field in this class with the specified name.
     * @throws SecurityException        If a security manager, s, is present and any of the following conditions is met:
     *                                  <ul>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPackageAccess() denies access to the package of this class.</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link RuntimePermission}("accessDeclaredMembers")</li>
     *                                  <li>The caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPermission() denies {@link ReflectPermission}("suppressAccessChecks")</li>
     *                                  </ul>
     */
    public static Object getValue(final Object instance, final String fieldName) throws NoSuchFieldException, IllegalAccessException {
        return getValue(instance, true, fieldName);
    }

    /**
     * Sets the value of the given static final field.
     *
     * @param field The field to set the value of.
     * @param value The value to set.
     * @throws ReflectiveOperationException If the field is not static or final, or if the field is not accessible.
     */
    public static void setFinalStatic(final Field field, final Object value) throws ReflectiveOperationException {
        field.setAccessible(true);
        Class.class.getModifiers();
        final Field mf = Field.class.getDeclaredField("modifiers");
        mf.setAccessible(true);
        mf.setInt(field, field.getModifiers() & 0xFFFFFFEF);
        field.set(null, value);
    }

    /**
     * Sets the value of the given final field on the given object.
     *
     * @param object The object to set the field value on.
     * @param field  The field to set the value of.
     * @param value  The value to set.
     * @throws ReflectiveOperationException If the field is not final, or if the field is not accessible.
     */
    public static void setFinal(final Object object, final Field field, final Object value) throws ReflectiveOperationException {
        field.setAccessible(true);
        final Field mf = Field.class.getDeclaredField("modifiers");
        mf.setAccessible(true);
        mf.setInt(field, field.getModifiers() & 0xFFFFFFEF);
        field.set(object, value);
    }

    /**
     * Enum representing different package types in a Minecraft server environment.
     */
    public enum PackageType {
        MINECRAFT_SERVER("net.minecraft.server." + getServerVersion()),
        CRAFTBUKKIT("org.bukkit.craftbukkit." + getServerVersion()),
        CRAFTBUKKIT_BLOCK(PackageType.CRAFTBUKKIT, "block"),
        CRAFTBUKKIT_CHUNKIO(PackageType.CRAFTBUKKIT, "chunkio"),
        CRAFTBUKKIT_COMMAND(PackageType.CRAFTBUKKIT, "command"),
        CRAFTBUKKIT_CONVERSATIONS(PackageType.CRAFTBUKKIT, "conversations"),
        CRAFTBUKKIT_ENCHANTMENS(PackageType.CRAFTBUKKIT, "enchantments"),
        CRAFTBUKKIT_ENTITY(PackageType.CRAFTBUKKIT, "entity"),
        CRAFTBUKKIT_EVENT(PackageType.CRAFTBUKKIT, "event"),
        CRAFTBUKKIT_GENERATOR(PackageType.CRAFTBUKKIT, "generator"),
        CRAFTBUKKIT_HELP(PackageType.CRAFTBUKKIT, "help"),
        CRAFTBUKKIT_INVENTORY(PackageType.CRAFTBUKKIT, "inventory"),
        CRAFTBUKKIT_MAP(PackageType.CRAFTBUKKIT, "map"),
        CRAFTBUKKIT_METADATA(PackageType.CRAFTBUKKIT, "metadata"),
        CRAFTBUKKIT_POTION(PackageType.CRAFTBUKKIT, "potion"),
        CRAFTBUKKIT_PROJECTILES(PackageType.CRAFTBUKKIT, "projectiles"),
        CRAFTBUKKIT_SCHEDULER(PackageType.CRAFTBUKKIT, "scheduler"),
        CRAFTBUKKIT_SCOREBOARD(PackageType.CRAFTBUKKIT, "scoreboard"),
        CRAFTBUKKIT_UPDATER(PackageType.CRAFTBUKKIT, "updater"),
        CRAFTBUKKIT_UTIL(PackageType.CRAFTBUKKIT, "util"),
        BUKKIT("org.bukkit");

        private final String path;

        /**
         * Constructs a PackageType with the specified path.
         *
         * @param path The path of the package type.
         */
        private PackageType(final String path) {
            this.path = path;
        }

        /**
         * Constructs a PackageType with the specified parent package type and path.
         *
         * @param parent The parent package type.
         * @param path   The path of the package type.
         */
        private PackageType(final PackageType parent, final String path) {
            this(parent + "." + path);
        }

        /**
         * Gets the server version from the Bukkit server package name.
         *
         * @return The server version as a string.
         */
        public static String getServerVersion() {
            final String name = Bukkit.getServer().getClass().getPackage().getName();
            return name.substring(name.lastIndexOf(46) + 1);
        }

        /**
         * Gets the path of the package type.
         *
         * @return The path of the package type.
         */
        public String getPath() {
            return this.path;
        }

        /**
         * Gets the class with the specified name from this package type.
         *
         * @param className The name of the class to get.
         * @return The class with the specified name.
         * @throws ClassNotFoundException If the class with the specified name cannot be found in this package type.
         */
        public Class<?> getClass(final String className) throws ClassNotFoundException {
            return Class.forName(this + "." + className);
        }

        /**
         * Returns the string representation of the package type, which is its path.
         *
         * @return The path of the package type.
         */
        @Override
        public String toString() {
            return this.path;
        }
    }

    public enum DataType {
        BYTE(Byte.TYPE, (Class<?>) Byte.class),
        SHORT(Short.TYPE, (Class<?>) Short.class),
        INTEGER(Integer.TYPE, (Class<?>) Integer.class),
        LONG(Long.TYPE, (Class<?>) Long.class),
        CHARACTER(Character.TYPE, (Class<?>) Character.class),
        FLOAT(Float.TYPE, (Class<?>) Float.class),
        DOUBLE(Double.TYPE, (Class<?>) Double.class),
        BOOLEAN(Boolean.TYPE, (Class<?>) Boolean.class);

        private static final Map<Class<?>, DataType> CLASS_MAP;
        private final Class<?> primitive;
        private final Class<?> reference;

        /**
         * Constructs a DataType with the specified primitive and reference classes.
         *
         * @param primitive The primitive class.
         * @param reference The reference class.
         */
        private DataType(final Class<?> primitive, final Class<?> reference) {
            this.primitive = primitive;
            this.reference = reference;
        }

        /**
         * Gets the DataType corresponding to the given class.
         *
         * @param clazz The class to get the DataType for.
         * @return The DataType corresponding to the given class, or null if the class is not a primitive or reference type.
         */
        public static DataType fromClass(final Class<?> clazz) {
            return DataType.CLASS_MAP.get(clazz);
        }

        /**
         * Gets the primitive class corresponding to the given class.
         * If the given class is not a primitive or reference type, it is returned as is.
         *
         * @param clazz The class to get the primitive class for.
         * @return The primitive class corresponding to the given class, or the given class if it is not a primitive or reference type.
         */
        public static Class<?> getPrimitive(final Class<?> clazz) {
            final DataType type = fromClass(clazz);
            return (type == null) ? clazz : type.getPrimitive();
        }

        /**
         * Gets the reference class corresponding to the given class.
         * If the given class is not a primitive or reference type, it is returned as is.
         *
         * @param clazz The class to get the reference class for.
         * @return The reference class corresponding to the given class, or the given class if it is not a primitive or reference type.
         */
        public static Class<?> getReference(final Class<?> clazz) {
            final DataType type = fromClass(clazz);
            return (type == null) ? clazz : type.getReference();
        }

        /**
         * Gets an array of primitive classes corresponding to the given array of classes.
         * If a class in the array is not a primitive or reference type, it is returned as is.
         *
         * @param classes The array of classes to get the primitive classes for.
         * @return An array of primitive classes corresponding to the given array of classes.
         */
        public static Class<?>[] getPrimitive(final Class<?>[] classes) {
            final int length = (classes == null) ? 0 : classes.length;
            final Class<?>[] types = (Class<?>[]) new Class[length];
            for (int index = 0; index < length; ++index) {
                types[index] = getPrimitive(classes[index]);
            }
            return types;
        }

        /**
         * Gets an array of reference classes corresponding to the given array of classes.
         * If a class in the array is not a primitive or reference type, it is returned as is.
         *
         * @param classes The array of classes to get the reference classes for.
         * @return An array of reference classes corresponding to the given array of classes.
         */
        public static Class<?>[] getReference(final Class<?>[] classes) {
            final int length = (classes == null) ? 0 : classes.length;
            final Class<?>[] types = (Class<?>[]) new Class[length];
            for (int index = 0; index < length; ++index) {
                types[index] = getReference(classes[index]);
            }
            return types;
        }

        /**
         * Gets an array of primitive classes corresponding to the classes of the given array of objects.
         * If the class of an object in the array is not a primitive or reference type, it is returned as is.
         *
         * @param objects The array of objects to get the primitive classes for.
         * @return An array of primitive classes corresponding to the classes of the given array of objects.
         */
        public static Class<?>[] getPrimitive(final Object[] objects) {
            final int length = (objects == null) ? 0 : objects.length;
            final Class<?>[] types = (Class<?>[]) new Class[length];
            for (int index = 0; index < length; ++index) {
                types[index] = getPrimitive(objects[index].getClass());
            }
            return types;
        }

        /**
         * Gets an array of reference classes corresponding to the classes of the given array of objects.
         * If the class of an object in the array is not a primitive or reference type, it is returned as is.
         *
         * @param objects The array of objects to get the reference classes for.
         * @return An array of reference classes corresponding to the classes of the given array of objects.
         */
        public static Class<?>[] getReference(final Object[] objects) {
            final int length = (objects == null) ? 0 : objects.length;
            final Class<?>[] types = (Class<?>[]) new Class[length];
            for (int index = 0; index < length; ++index) {
                types[index] = getReference(objects[index].getClass());
            }
            return types;
        }

        /**
         * Compares two arrays of classes for equality, considering assignability.
         * Two arrays are considered equal if they have the same length and each class in the primary array is either equal to or assignable from the corresponding class in the secondary array.
         *
         * @param primary   The primary array of classes.
         * @param secondary The secondary array of classes.
         * @return true if the arrays are considered equal, false otherwise.
         */
        public static boolean compare(final Class<?>[] primary, final Class<?>[] secondary) {
            if (primary == null || secondary == null || primary.length != secondary.length) {
                return false;
            }
            for (int index = 0; index < primary.length; ++index) {
                final Class<?> primaryClass = primary[index];
                final Class<?> secondaryClass = secondary[index];
                if (!primaryClass.equals(secondaryClass) && !primaryClass.isAssignableFrom(secondaryClass)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Gets the primitive class of this DataType.
         *
         * @return The primitive class of this DataType.
         */
        public Class<?> getPrimitive() {
            return this.primitive;
        }

        /**
         * Gets the reference class of this DataType.
         *
         * @return The reference class of this DataType.
         */
        public Class<?> getReference() {
            return this.reference;
        }

        /**
         * Static initializer to populate the CLASS_MAP with primitive and reference class mappings.
         */
        static {
            CLASS_MAP = new HashMap<Class<?>, DataType>();
            for (final DataType type : values()) {
                DataType.CLASS_MAP.put(type.primitive, type);
                DataType.CLASS_MAP.put(type.reference, type);
            }
        }
    }

    /**
     * Enum representing different packet types in a Minecraft server environment.
     */
    public enum PacketType {
        HANDSHAKING_IN_SET_PROTOCOL("PacketHandshakingInSetProtocol"),
        LOGIN_IN_ENCRYPTION_BEGIN("PacketLoginInEncryptionBegin"),
        LOGIN_IN_START("PacketLoginInStart"),
        LOGIN_OUT_DISCONNECT("PacketLoginOutDisconnect"),
        LOGIN_OUT_ENCRYPTION_BEGIN("PacketLoginOutEncryptionBegin"),
        LOGIN_OUT_SUCCESS("PacketLoginOutSuccess"),
        PLAY_IN_ABILITIES("PacketPlayInAbilities"),
        PLAY_IN_ARM_ANIMATION("PacketPlayInArmAnimation"),
        PLAY_IN_BLOCK_DIG("PacketPlayInBlockDig"),
        PLAY_IN_BLOCK_PLACE("PacketPlayInBlockPlace"),
        PLAY_IN_CHAT("PacketPlayInChat"),
        PLAY_IN_CLIENT_COMMAND("PacketPlayInClientCommand"),
        PLAY_IN_CLOSE_WINDOW("PacketPlayInCloseWindow"),
        PLAY_IN_CUSTOM_PAYLOAD("PacketPlayInCustomPayload"),
        PLAY_IN_ENCHANT_ITEM("PacketPlayInEnchantItem"),
        PLAY_IN_ENTITY_ACTION("PacketPlayInEntityAction"),
        PLAY_IN_FLYING("PacketPlayInFlying"),
        PLAY_IN_HELD_ITEM_SLOT("PacketPlayInHeldItemSlot"),
        PLAY_IN_KEEP_ALIVE("PacketPlayInKeepAlive"),
        PLAY_IN_LOOK("PacketPlayInLook"),
        PLAY_IN_POSITION("PacketPlayInPosition"),
        PLAY_IN_POSITION_LOOK("PacketPlayInPositionLook"),
        PLAY_IN_SET_CREATIVE_SLOT("PacketPlayInSetCreativeSlot "),
        PLAY_IN_SETTINGS("PacketPlayInSettings"),
        PLAY_IN_STEER_VEHICLE("PacketPlayInSteerVehicle"),
        PLAY_IN_TAB_COMPLETE("PacketPlayInTabComplete"),
        PLAY_IN_TRANSACTION("PacketPlayInTransaction"),
        PLAY_IN_UPDATE_SIGN("PacketPlayInUpdateSign"),
        PLAY_IN_USE_ENTITY("PacketPlayInUseEntity"),
        PLAY_IN_WINDOW_CLICK("PacketPlayInWindowClick"),
        PLAY_OUT_ABILITIES("PacketPlayOutAbilities"),
        PLAY_OUT_ANIMATION("PacketPlayOutAnimation"),
        PLAY_OUT_ATTACH_ENTITY("PacketPlayOutAttachEntity"),
        PLAY_OUT_BED("PacketPlayOutBed"),
        PLAY_OUT_BLOCK_ACTION("PacketPlayOutBlockAction"),
        PLAY_OUT_BLOCK_BREAK_ANIMATION("PacketPlayOutBlockBreakAnimation"),
        PLAY_OUT_BLOCK_CHANGE("PacketPlayOutBlockChange"),
        PLAY_OUT_CHAT("PacketPlayOutChat"),
        PLAY_OUT_CLOSE_WINDOW("PacketPlayOutCloseWindow"),
        PLAY_OUT_COLLECT("PacketPlayOutCollect"),
        PLAY_OUT_CRAFT_PROGRESS_BAR("PacketPlayOutCraftProgressBar"),
        PLAY_OUT_CUSTOM_PAYLOAD("PacketPlayOutCustomPayload"),
        PLAY_OUT_ENTITY("PacketPlayOutEntity"),
        PLAY_OUT_ENTITY_DESTROY("PacketPlayOutEntityDestroy"),
        PLAY_OUT_ENTITY_EFFECT("PacketPlayOutEntityEffect"),
        PLAY_OUT_ENTITY_EQUIPMENT("PacketPlayOutEntityEquipment"),
        PLAY_OUT_ENTITY_HEAD_ROTATION("PacketPlayOutEntityHeadRotation"),
        PLAY_OUT_ENTITY_LOOK("PacketPlayOutEntityLook"),
        PLAY_OUT_ENTITY_METADATA("PacketPlayOutEntityMetadata"),
        PLAY_OUT_ENTITY_STATUS("PacketPlayOutEntityStatus"),
        PLAY_OUT_ENTITY_TELEPORT("PacketPlayOutEntityTeleport"),
        PLAY_OUT_ENTITY_VELOCITY("PacketPlayOutEntityVelocity"),
        PLAY_OUT_EXPERIENCE("PacketPlayOutExperience"),
        PLAY_OUT_EXPLOSION("PacketPlayOutExplosion"),
        PLAY_OUT_GAME_STATE_CHANGE("PacketPlayOutGameStateChange"),
        PLAY_OUT_HELD_ITEM_SLOT("PacketPlayOutHeldItemSlot"),
        PLAY_OUT_KEEP_ALIVE("PacketPlayOutKeepAlive"),
        PLAY_OUT_KICK_DISCONNECT("PacketPlayOutKickDisconnect"),
        PLAY_OUT_LOGIN("PacketPlayOutLogin"),
        PLAY_OUT_MAP("PacketPlayOutMap"),
        PLAY_OUT_MAP_CHUNK("PacketPlayOutMapChunk"),
        PLAY_OUT_MAP_CHUNK_BULK("PacketPlayOutMapChunkBulk"),
        PLAY_OUT_MULTI_BLOCK_CHANGE("PacketPlayOutMultiBlockChange"),
        PLAY_OUT_NAMED_ENTITY_SPAWN("PacketPlayOutNamedEntitySpawn"),
        PLAY_OUT_NAMED_SOUND_EFFECT("PacketPlayOutNamedSoundEffect"),
        PLAY_OUT_OPEN_SIGN_EDITOR("PacketPlayOutOpenSignEditor"),
        PLAY_OUT_OPEN_WINDOW("PacketPlayOutOpenWindow"),
        PLAY_OUT_PLAYER_INFO("PacketPlayOutPlayerInfo"),
        PLAY_OUT_POSITION("PacketPlayOutPosition"),
        PLAY_OUT_REL_ENTITY_MOVE("PacketPlayOutRelEntityMove"),
        PLAY_OUT_REL_ENTITY_MOVE_LOOK("PacketPlayOutRelEntityMoveLook"),
        PLAY_OUT_REMOVE_ENTITY_EFFECT("PacketPlayOutRemoveEntityEffect"),
        PLAY_OUT_RESPAWN("PacketPlayOutRespawn"),
        PLAY_OUT_SCOREBOARD_DISPLAY_OBJECTIVE("PacketPlayOutScoreboardDisplayObjective"),
        PLAY_OUT_SCOREBOARD_OBJECTIVE("PacketPlayOutScoreboardObjective"),
        PLAY_OUT_SCOREBOARD_SCORE("PacketPlayOutScoreboardScore"),
        PLAY_OUT_SCOREBOARD_TEAM("PacketPlayOutScoreboardTeam"),
        PLAY_OUT_SET_SLOT("PacketPlayOutSetSlot"),
        PLAY_OUT_SPAWN_ENTITY("PacketPlayOutSpawnEntity"),
        PLAY_OUT_SPAWN_ENTITY_EXPERIENCE_ORB("PacketPlayOutSpawnEntityExperienceOrb"),
        PLAY_OUT_SPAWN_ENTITY_LIVING("PacketPlayOutSpawnEntityLiving"),
        PLAY_OUT_SPAWN_ENTITY_PAINTING("PacketPlayOutSpawnEntityPainting"),
        PLAY_OUT_SPAWN_ENTITY_WEATHER("PacketPlayOutSpawnEntityWeather"),
        PLAY_OUT_SPAWN_POSITION("PacketPlayOutSpawnPosition"),
        PLAY_OUT_STATISTIC("PacketPlayOutStatistic"),
        PLAY_OUT_TAB_COMPLETE("PacketPlayOutTabComplete"),
        PLAY_OUT_TILE_ENTITY_DATA("PacketPlayOutTileEntityData"),
        PLAY_OUT_TRANSACTION("PacketPlayOutTransaction"),
        PLAY_OUT_UPDATE_ATTRIBUTES("PacketPlayOutUpdateAttributes"),
        PLAY_OUT_UPDATE_HEALTH("PacketPlayOutUpdateHealth"),
        PLAY_OUT_UPDATE_SIGN("PacketPlayOutUpdateSign"),
        PLAY_OUT_UPDATE_TIME("PacketPlayOutUpdateTime"),
        PLAY_OUT_WINDOW_ITEMS("PacketPlayOutWindowItems"),
        PLAY_OUT_WORLD_EVENT("PacketPlayOutWorldEvent"),
        PLAY_OUT_WORLD_PARTICLES("PacketPlayOutWorldParticles"),
        STATUS_IN_PING("PacketStatusInPing"),
        STATUS_IN_START("PacketStatusInStart"),
        STATUS_OUT_PONG("PacketStatusOutPong"),
        STATUS_OUT_SERVER_INFO("PacketStatusOutServerInfo");

        private static final Map<String, PacketType> NAME_MAP;
        @Getter
        private final String name;
        private Class<?> packet;

        /**
         * Constructs a PacketType with the specified name.
         *
         * @param name The name of the packet type.
         */
        private PacketType(final String name) {
            this.name = name;
        }

        /**
         * Gets the Class object representing the packet type.
         *
         * @return The Class object representing the packet type.
         * @throws ClassNotFoundException If the class with the specified name cannot be found in the Minecraft server package.
         */
        public Class<?> getPacket() throws ClassNotFoundException {
            return (this.packet == null) ? (this.packet = PackageType.MINECRAFT_SERVER.getClass(this.name)) : this.packet;
        }

        static {
            NAME_MAP = new HashMap<String, PacketType>();
            for (final PacketType type : values()) {
                PacketType.NAME_MAP.put(type.name, type);
            }
        }
    }
}
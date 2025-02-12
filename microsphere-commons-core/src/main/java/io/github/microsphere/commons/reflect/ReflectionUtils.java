package io.github.microsphere.commons.reflect;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reflection Utility class , generic methods are defined from {@link FieldUtils} , {@link MethodUtils} , {@link
 * ConstructorUtils}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @version 1.0.0
 * @see Method
 * @see Field
 * @see Constructor
 * @see Array
 * @see MethodUtils
 * @see FieldUtils
 * @see ConstructorUtils
 * @since 1.0.0
 */
public abstract class ReflectionUtils {

    /**
     * Sun JDK implementation class: full name of sun.reflect.Reflection
     */
    public static final String SUN_REFLECT_REFLECTION_CLASS_NAME = "sun.reflect.Reflection";

    /**
     * Current Type
     */
    private static final Class<?> TYPE = ReflectionUtils.class;

    /**
     * sun.reflect.Reflection method name
     */
    private static final String getCallerClassMethodName = "getCallerClass";

    /**
     * sun.reflect.Reflection invocation frame
     */
    private static final int sunReflectReflectionInvocationFrame;

    /**
     * {@link StackTraceElement} invocation frame
     */
    private static final int stackTraceElementInvocationFrame;

    /**
     * Is Supported sun.reflect.Reflection ?
     */
    private static final boolean supportedSunReflectReflection;

    /**
     * sun.reflect.Reflection#getCallerClass(int) method
     */
    private static final Method getCallerClassMethod;

    // Initialize sun.reflect.Reflection
    static {
        Method method = null;
        boolean supported = false;
        int invocationFrame = 0;
        try {
            // Use sun.reflect.Reflection to calculate frame
            Class<?> type = Class.forName(SUN_REFLECT_REFLECTION_CLASS_NAME);
            method = type.getMethod(getCallerClassMethodName, int.class);
            method.setAccessible(true);
            // Adapt SUN JDK ,The value of invocation frame in JDK 6/7/8 may be different
            for (int i = 0; i < 9; i++) {
                Class<?> callerClass = (Class<?>) method.invoke(null, i);
                if (TYPE.equals(callerClass)) {
                    invocationFrame = i;
                    break;
                }
            }
            supported = true;
        } catch (Exception e) {
            method = null;
            supported = false;
        }
        // set method info
        getCallerClassMethod = method;
        supportedSunReflectReflection = supported;
        // getCallerClass() -> getCallerClass(int)
        // Plugs 1 , because Invocation getCallerClass() method was considered as increment invocation frame
        // Plugs 1 , because Invocation getCallerClass(int) method was considered as increment invocation frame
        sunReflectReflectionInvocationFrame = invocationFrame + 2;
    }

    // Initialize StackTraceElement
    static {
        int invocationFrame = 0;
        // Use java.lang.StackTraceElement to calculate frame
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            String className = stackTraceElement.getClassName();
            if (TYPE.getName().equals(className)) {
                break;
            }
            invocationFrame++;
        }
        // getCallerClass() -> getCallerClass(int)
        // Plugs 1 , because Invocation getCallerClass() method was considered as increment invocation frame
        // Plugs 1 , because Invocation getCallerClass(int) method was considered as increment invocation frame
        stackTraceElementInvocationFrame = invocationFrame + 2;
    }

    /**
     * Get Caller class
     *
     * @return Get the Class name that called the method
     * @version 1.0.0
     * @since 1.0.0
     */
    @Nonnull
    public static String getCallerClassName() {
        return getCallerClassName(sunReflectReflectionInvocationFrame);
    }

    /**
     * Get Caller Class name
     *
     * @param invocationFrame invocation frame
     * @return Class name under specified invocation frame
     * @throws IndexOutOfBoundsException If the <code>invocation Frame<code> value is negative or exceeds the actual level
     * @version 1.0.0
     * @see Thread#getStackTrace()
     * @since 1.0.0
     */
    @Nonnull
    protected static String getCallerClassName(int invocationFrame) throws IndexOutOfBoundsException {
        if (supportedSunReflectReflection) {
            Class<?> callerClass = getCallerClassInSunJVM(invocationFrame + 1);
            if (callerClass != null)
                return callerClass.getName();
        }
        return getCallerClassNameInGeneralJVM(invocationFrame + 1);
    }

    /**
     * General implementation, get the calling class name
     *
     * @return call class name
     * @version 1.0.0
     * @see #getCallerClassNameInGeneralJVM(int)
     * @since 1.0.0
     */
    static String getCallerClassNameInGeneralJVM() {
        return getCallerClassNameInGeneralJVM(stackTraceElementInvocationFrame);
    }

    /**
     * General implementation, get the calling class name by specifying the calling level value
     *
     * @param invocationFrame invocation frame
     * @return specified invocation frame class
     * @throws IndexOutOfBoundsException If the <code>invocation Frame<code> value is negative or exceeds the actual level
     * @version 1.0.0
     * @since 1.0.0
     */
    static String getCallerClassNameInGeneralJVM(int invocationFrame) throws IndexOutOfBoundsException {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StackTraceElement targetStackTraceElement = elements[invocationFrame];
        return targetStackTraceElement.getClassName();
    }

    static Class<?> getCallerClassInSunJVM(int realFramesToSkip) throws UnsupportedOperationException {
        if (!supportedSunReflectReflection) {
            throw new UnsupportedOperationException("Requires SUN's JVM!");
        }
        Class<?> callerClass = null;
        if (getCallerClassMethod != null) {
            try {
                callerClass = (Class<?>) getCallerClassMethod.invoke(null, realFramesToSkip);
            } catch (Exception ignored) {
            }
        }
        if (callerClass != null) {
        }
        return callerClass;
    }

    /**
     * Get caller class in General JVM
     *
     * @param invocationFrame invocation frame
     * @return caller class
     * @version 1.0.0
     * @see #getCallerClassNameInGeneralJVM(int)
     * @since 1.0.0
     */
    static Class<?> getCallerClassInGeneralJVM(int invocationFrame) {
        String className = getCallerClassNameInGeneralJVM(invocationFrame + 1);
        Class<?> targetClass = null;
        try {
            targetClass = Class.forName(className);
        } catch (ClassNotFoundException impossibleException) {
            throw new IllegalStateException("How can?");
        }
        return targetClass;
    }

    /**
     * Get caller class
     * <p/>
     * For instance,
     * <pre>
     *     package com.acme;
     *     import ...;
     *     class Foo {
     *         public void bar(){
     *
     *         }
     *     }
     * </pre>
     *
     * @return Get caller class
     * @throws IllegalStateException If the caller class cannot be found
     * @version 1.0.0
     * @since 1.0.0 2012-2-28 下午07:42:26
     */
    @Nonnull
    public static Class<?> getCallerClass() throws IllegalStateException {
        return getCallerClass(sunReflectReflectionInvocationFrame);
    }

    /**
     * Get caller class In SUN HotSpot JVM
     *
     * @return Caller Class
     * @throws UnsupportedOperationException If JRE is not a SUN HotSpot JVM
     * @version 1.0.0
     * @see #getCallerClassInSunJVM(int)
     * @since 1.0.0
     */
    static Class<?> getCallerClassInSunJVM() throws UnsupportedOperationException {
        return getCallerClassInSunJVM(sunReflectReflectionInvocationFrame);
    }

    /**
     * Get caller class name In SUN HotSpot JVM
     *
     * @return Caller Class
     * @throws UnsupportedOperationException If JRE is not a SUN HotSpot JVM
     * @version 1.0.0
     * @see #getCallerClassInSunJVM(int)
     * @since 1.0.0
     */
    static String getCallerClassNameInSunJVM() throws UnsupportedOperationException {
        Class<?> callerClass = getCallerClassInSunJVM(sunReflectReflectionInvocationFrame);
        return callerClass.getName();
    }

    /**
     * @param invocationFrame invocation frame
     * @return
     * @version 1.0.0
     * @see
     * @since 1.0.0
     */
    static Class<?> getCallerClass(int invocationFrame) {
        if (supportedSunReflectReflection) {
            Class<?> callerClass = getCallerClassInSunJVM(invocationFrame + 1);
            if (callerClass != null)
                return callerClass;
        }
        return getCallerClassInGeneralJVM(invocationFrame + 1);
    }

    /**
     * Get caller class in General JVM
     *
     * @return Caller Class
     * @version 1.0.0
     * @see #getCallerClassInGeneralJVM(int)
     * @since 1.0.0
     */
    static Class<?> getCallerClassInGeneralJVM() {
        return getCallerClassInGeneralJVM(stackTraceElementInvocationFrame);
    }

    /**
     * Get caller class's {@link Package}
     *
     * @return caller class's {@link Package}
     * @throws IllegalStateException see {@link #getCallerClass()}
     * @version 1.0.0
     * @see #getCallerClass()
     * @since 1.0.0
     */
    public static Package getCallerPackage() throws IllegalStateException {
        return getCallerClass().getPackage();
    }

    /**
     * Assert array index
     *
     * @param array Array object
     * @param index index
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index</code> is less than 0 or equals or greater than length of array
     */
    public static void assertArrayIndex(Object array, int index) throws IllegalArgumentException {
        if (index < 0) {
            String message = String.format("The index argument must be positive , actual is %s", index);
            throw new ArrayIndexOutOfBoundsException(message);
        }
        ReflectionUtils.assertArrayType(array);
        int length = Array.getLength(array);
        if (index > length - 1) {
            String message = String.format("The index must be less than %s , actual is %s", length, index);
            throw new ArrayIndexOutOfBoundsException(message);
        }
    }

    /**
     * Assert the object is array or not
     *
     * @param array asserted object
     * @throws IllegalArgumentException if the object is not a array
     */
    public static void assertArrayType(Object array) throws IllegalArgumentException {
        Class<?> type = array.getClass();
        if (!type.isArray()) {
            String message = String.format("The argument is not an array object, its type is %s", type.getName());
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert Field type match
     *
     * @param object       Object
     * @param fieldName    field name
     * @param expectedType expected type
     * @throws IllegalArgumentException if type is not matched
     */
    public static void assertFieldMatchType(Object object, String fieldName, Class<?> expectedType) throws IllegalArgumentException {
        Class<?> type = object.getClass();
        Field field = FieldUtils.getDeclaredField(type, fieldName, true);
        Class<?> fieldType = field.getType();
        if (!expectedType.isAssignableFrom(fieldType)) {
            String message = String.format("The type[%s] of field[%s] in Class[%s] can't match expected type[%s]", fieldType.getName(), fieldName, type.getName(), expectedType.getName());
            throw new IllegalArgumentException(message);
        }
    }


    /**
     * Convert {@link Array} object to {@link List}
     *
     * @param array array object
     * @return {@link List}
     * @throws IllegalArgumentException if the object argument is not an array
     */
    @Nonnull
    public static <T> List<T> toList(Object array) throws IllegalArgumentException {
        int length = Array.getLength(array);
        List<T> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            list.add((T) toObject(element));
        }
        return list;
    }

    private static Object toObject(Object object) {
        if (object == null) {
            return object;
        }
        Class<?> type = object.getClass();
        if (type.isArray()) {
            return toList(object);
        } else {
            return object;
        }
    }


    /**
     * Read fields value as {@link Map}
     *
     * @param object object to be read
     * @return fields value as {@link Map}
     */
    @Nonnull
    public static Map<String, Object> readFieldsAsMap(Object object) {
        Map<String, Object> fieldsAsMap = new LinkedHashMap();
        Class<?> type = object.getClass();
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {

            if (Modifier.isStatic(field.getModifiers())) { // To filter static fields
                continue;
            }

            field.setAccessible(true);

            try {
                String fieldName = field.getName();
                Object fieldValue = field.get(object);
                if (fieldValue != null) {
                    Class<?> fieldValueType = fieldValue.getClass();
                    if (ClassUtils.isPrimitiveOrWrapper(fieldValueType)) {
                    } else if (fieldValueType.isArray()) {
                        fieldValue = toList(fieldValue);
                    } else if ("java.lang".equals(fieldValueType.getPackage().getName())) {

                    } else {
                        fieldValue = readFieldsAsMap(fieldValue);
                    }
                }
                fieldsAsMap.put(fieldName, fieldValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return fieldsAsMap;
    }


}
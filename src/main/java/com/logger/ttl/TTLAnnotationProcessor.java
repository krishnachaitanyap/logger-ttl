package com.logger.ttl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Stack;

/**
 * Processor for LogTTL annotations that determines TTL configuration
 * based on annotation hierarchy and scope.
 * 
 * <p>This class uses reflection to inspect LogTTL annotations at runtime
 * and provides the appropriate TTL configuration for logging operations.</p>
 */
public class TTLAnnotationProcessor {
    
    /**
     * Gets the TTL configuration for a specific logging context.
     * 
     * <p>The method inspects the call stack to determine the appropriate
     * TTL configuration based on LogTTL annotations at class, method, and field levels.</p>
     * 
     * @param loggerClass the class of the logger instance
     * @param logLevel the log level being used
     * @return the TTL configuration to use
     */
    public static TTLConfig getTTLConfig(Class<?> loggerClass, LogLevel logLevel) {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            
            // Find the calling method (skip getStackTrace and getTTLConfig)
            String callingClassName = null;
            String callingMethodName = null;
            
            for (int i = 2; i < stackTrace.length; i++) {
                StackTraceElement element = stackTrace[i];
                String className = element.getClassName();
                
                // Skip our own classes and find the first external caller
                if (!className.startsWith("com.logger.ttl") && 
                    !className.startsWith("java.lang") &&
                    !className.startsWith("sun.reflect")) {
                    callingClassName = className;
                    callingMethodName = element.getMethodName();
                    break;
                }
            }
            
            if (callingClassName == null) {
                return TTLConfig.defaultConfig();
            }
            
            Class<?> callingClass = Class.forName(callingClassName);
            return getTTLConfigForContext(callingClass, callingMethodName, loggerClass, logLevel);
            
        } catch (Exception e) {
            // If reflection fails, return default config
            return TTLConfig.defaultConfig();
        }
    }
    
    /**
     * Gets the TTL configuration for a specific context.
     * 
     * @param callingClass the class where the log statement is located
     * @param callingMethod the method where the log statement is located
     * @param loggerClass the class of the logger instance
     * @param logLevel the log level being used
     * @return the TTL configuration to use
     */
    private static TTLConfig getTTLConfigForContext(Class<?> callingClass, String callingMethod, 
                                                   Class<?> loggerClass, LogLevel logLevel) {
        
        // Priority order: field-level > method-level > class-level
        
        // Check field-level annotation (logger instance)
        TTLConfig fieldConfig = getFieldLevelTTL(callingClass, loggerClass);
        if (fieldConfig != null && fieldConfig.isLevelAffected(logLevel)) {
            return fieldConfig;
        }
        
        // Check method-level annotation
        TTLConfig methodConfig = getMethodLevelTTL(callingClass, callingMethod);
        if (methodConfig != null && methodConfig.isLevelAffected(logLevel)) {
            return methodConfig;
        }
        
        // Check class-level annotation
        TTLConfig classConfig = getClassLevelTTL(callingClass);
        if (classConfig != null && classConfig.isLevelAffected(logLevel)) {
            return classConfig;
        }
        
        // No TTL restrictions found
        return TTLConfig.defaultConfig();
    }
    
    /**
     * Gets the field-level TTL configuration for a logger instance.
     * 
     * @param callingClass the class containing the logger field
     * @param loggerClass the class of the logger instance
     * @return the TTL configuration, or null if not found
     */
    private static TTLConfig getFieldLevelTTL(Class<?> callingClass, Class<?> loggerClass) {
        try {
            Field[] fields = callingClass.getDeclaredFields();
            
            for (Field field : fields) {
                if (loggerClass.isAssignableFrom(field.getType())) {
                    LogTTL annotation = field.getAnnotation(LogTTL.class);
                    if (annotation != null) {
                        return new TTLConfig(annotation.start(), annotation.ttlDays(), annotation.levels());
                    }
                }
            }
            
            // Check inherited fields
            Class<?> superClass = callingClass.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return getFieldLevelTTL(superClass, loggerClass);
            }
            
        } catch (Exception e) {
            // Ignore reflection errors
        }
        
        return null;
    }
    
    /**
     * Gets the method-level TTL configuration.
     * 
     * @param callingClass the class containing the method
     * @param methodName the name of the method
     * @return the TTL configuration, or null if not found
     */
    private static TTLConfig getMethodLevelTTL(Class<?> callingClass, String methodName) {
        try {
            Method[] methods = callingClass.getDeclaredMethods();
            
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    LogTTL annotation = method.getAnnotation(LogTTL.class);
                    if (annotation != null) {
                        return new TTLConfig(annotation.start(), annotation.ttlDays(), annotation.levels());
                    }
                }
            }
            
            // Check inherited methods
            Class<?> superClass = callingClass.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return getMethodLevelTTL(superClass, methodName);
            }
            
        } catch (Exception e) {
            // Ignore reflection errors
        }
        
        return null;
    }
    
    /**
     * Gets the class-level TTL configuration.
     * 
     * @param callingClass the class to inspect
     * @return the TTL configuration, or null if not found
     */
    private static TTLConfig getClassLevelTTL(Class<?> callingClass) {
        try {
            LogTTL annotation = callingClass.getAnnotation(LogTTL.class);
            if (annotation != null) {
                return new TTLConfig(annotation.start(), annotation.ttlDays(), annotation.levels());
            }
            
            // Check interfaces
            Class<?>[] interfaces = callingClass.getInterfaces();
            for (Class<?> iface : interfaces) {
                TTLConfig config = getClassLevelTTL(iface);
                if (config != null) {
                    return config;
                }
            }
            
            // Check superclass
            Class<?> superClass = callingClass.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return getClassLevelTTL(superClass);
            }
            
        } catch (Exception e) {
            // Ignore reflection errors
        }
        
        return null;
    }
}

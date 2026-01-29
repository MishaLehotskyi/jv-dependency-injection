package mate.academy.lib;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> implementationClazz = findImplementation(interfaceClazz);
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Can't create an instance of class without Component annotation: "
                            + implementationClazz.getName());
        }
        Field[] fields = implementationClazz.getDeclaredFields();
        for (Field field: fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());

                clazzImplementationInstance = createNewInstance(implementationClazz);

                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "Can't set field: " + field.getName()
                                    + " for object: " + clazzImplementationInstance);
                }
            }
        }

        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(implementationClazz);
        }

        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> implementationClazz) {
        if (instances.containsKey(implementationClazz)) {
            return instances.get(implementationClazz);
        }

        try {
            Object newInstance = implementationClazz.getConstructor().newInstance();
            instances.put(implementationClazz, newInstance);
            return newInstance;
        } catch (InstantiationException | IllegalAccessException
                 | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(
                    "Can't create an instance of: " + implementationClazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementationsMap = new HashMap<>();
        implementationsMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationsMap.put(ProductParser.class, ProductParserImpl.class);
        implementationsMap.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return implementationsMap.get(interfaceClazz);
        }

        return interfaceClazz;
    }
}

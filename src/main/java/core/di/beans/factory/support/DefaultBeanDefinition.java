package core.di.beans.factory.support;

import com.google.common.collect.Sets;
import core.aop.FactoryBean;
import core.di.beans.factory.config.BeanDefinition;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

public class DefaultBeanDefinition implements BeanDefinition {
    private Class<?> beanClazz;
    private Constructor<?> injectConstructor;
    private Set<Field> injectFields;

    public DefaultBeanDefinition(Class<?> clazz) {
        this.beanClazz = clazz;
        injectConstructor = getInjectConstructor(clazz);
        injectFields = getInjectFields(clazz, injectConstructor);
    }

    private static Constructor<?> getInjectConstructor(Class<?> clazz) {
        return BeanFactoryUtils.getInjectedConstructor(clazz);
    }

    private Set<Field> getInjectFields(Class<?> clazz, Constructor<?> constructor) {
        if (constructor != null) {
            return Sets.newHashSet();
        }

        Set<Field> injectFields = Sets.newHashSet();
        Set<Class<?>> injectProperties = getInjectPropertiesType(clazz);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (injectProperties.contains(field.getType())) {
                injectFields.add(field);
            }
        }
        return injectFields;
    }

    private static Set<Class<?>> getInjectPropertiesType(Class<?> clazz) {
        Set<Class<?>> injectProperties = Sets.newHashSet();
        Set<Method> injectMethod = BeanFactoryUtils.getInjectedMethods(clazz);
        for (Method method : injectMethod) {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != 1) {
                throw new IllegalStateException("DI할 메소드 인자는 하나여야 합니다.");
            }

            injectProperties.add(paramTypes[0]);
        }

        Set<Field> injectField = BeanFactoryUtils.getInjectedFields(clazz);
        for (Field field : injectField) {
            injectProperties.add(field.getType());
        }
        return injectProperties;
    }

    @Override
    public Constructor<?> getInjectConstructor() {
        return injectConstructor;
    }

    @Override
    public Set<Field> getInjectFields() {
        return this.injectFields;
    }

    @Override
    public Class<?> getBeanClass() {
        return this.beanClazz;
    }

    @Override
    public Method getMethod() {
        return null;
    }

    @Override
    public boolean isFactoryBeanType() {
        return FactoryBean.class.isAssignableFrom(beanClazz);
    }

    @Override
    public InjectType getResolvedInjectMode() {
        if (Objects.nonNull(getMethod())) {
            return InjectType.INJECT_METHOD;
        }

        if (Objects.nonNull(injectConstructor)) {
            return InjectType.INJECT_CONSTRUCTOR;
        }

        if (!CollectionUtils.isEmpty(injectFields)) {
            return InjectType.INJECT_FIELD;
        }

        return InjectType.INJECT_NO;
    }
}

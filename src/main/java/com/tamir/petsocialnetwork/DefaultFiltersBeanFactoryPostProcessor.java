package com.tamir.petsocialnetwork;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 *  This class is a hack to disable spring boot from automatically registering filters.
 *  With this activated the programmer has full control over which filters will be registered.
 */
@Component
public class DefaultFiltersBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory bf)
            throws BeansException {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) bf;

        Arrays.stream(beanFactory.getBeanNamesForType(javax.servlet.Filter.class))
                .forEach(name -> {

                    BeanDefinition definition = BeanDefinitionBuilder
                            .genericBeanDefinition(FilterRegistrationBean.class)
                            .setScope(BeanDefinition.SCOPE_SINGLETON)
                            .addConstructorArgReference(name)
                            .addConstructorArgValue(new ServletRegistrationBean[]{})
                            .addPropertyValue("enabled", false)
                            .getBeanDefinition();

                    beanFactory.registerBeanDefinition(name + "FilterRegistrationBean",
                            definition);
                });
    }
}

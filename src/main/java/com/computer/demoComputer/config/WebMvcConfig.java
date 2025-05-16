package com.computer.demoComputer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry resourceHandlerRegistry) {
        resourceHandlerRegistry.addResourceHandler("/uploads/images/avatar/**")
                               .addResourceLocations("file:uploads/images/avatar/");

        resourceHandlerRegistry.addResourceHandler("/uploads/images/product/**")
                               .addResourceLocations("file:uploads/images/product/");
    }
}












// package com.computer.demoComputer.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.ViewResolver;
// import org.springframework.web.servlet.config.annotation.EnableWebMvc;
// import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
// import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
// import org.springframework.web.servlet.view.InternalResourceViewResolver;
// import org.springframework.web.servlet.view.JstlView;

// @Configuration
// @EnableWebMvc
// public class WebMvcConfig implements WebMvcConfigurer {

//     @Bean
//     public ViewResolver viewResolver() {
//         final InternalResourceViewResolver bean = new InternalResourceViewResolver();
//         bean.setViewClass(JstlView.class);
//         bean.setPrefix("webapp/WEB-INF/view/");
//         bean.setSuffix(".jsp");
//         return bean;
//     }

//     @Override
//     public void configureViewResolvers(ViewResolverRegistry registry) {
//         registry.viewResolver(viewResolver());
//     }

//     @Override
//     public void addResourceHandlers(ResourceHandlerRegistry registry) {
//         registry.addResourceHandler("/css/**").addResourceLocations("/resources/css/");
//         registry.addResourceHandler("/js/**").addResourceLocations("/resources/js/");
//         registry.addResourceHandler("/images/**").addResourceLocations("/resources/images/");
//     }

// }


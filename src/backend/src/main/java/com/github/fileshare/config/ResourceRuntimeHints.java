package com.github.fileshare.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import com.github.fileshare.specifications.UserSettingsProjection;
import com.github.fileshare.specifications.UserWithStorageProjection;

@Configuration
@ImportRuntimeHints(ResourceRuntimeHints.ResourcesRegistrar.class)
public class ResourceRuntimeHints {
    static class ResourcesRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        	
        	hints.reflection().registerType(java.util.UUID[].class);
        	
        	hints.reflection().registerType(UserWithStorageProjection.class, builder -> builder
        	        .withMembers(MemberCategory.DECLARED_FIELDS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS));

        	    hints.reflection().registerType(UserSettingsProjection.class, builder -> builder
        	        .withMembers(MemberCategory.DECLARED_FIELDS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS));
        	
        	hints.resources()
            	.registerPattern("handlebars/*.html");
        	hints.resources()
        		.registerPattern("db/migration/*.sql");
        	
        	hints.resources().registerPattern("helpers.nashorn.js");
        }
    }
}

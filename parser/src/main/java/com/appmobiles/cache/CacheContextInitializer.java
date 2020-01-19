package com.appmobiles.cache;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

/**
 * Abstracts cache initialization through code generation for all classes listed in the annotation.
 *
 * @see AutoProtoSchemaBuilder#includeClasses()
 */
@AutoProtoSchemaBuilder(includeClasses = {Feed.class, StorageItem.class}, schemaPackageName = "fun_contest")
public interface CacheContextInitializer extends SerializationContextInitializer {
}
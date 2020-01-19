package com.appmobiles.data.model;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

/**
 * Abstracts cache initialization through code generation for all classes listed in the annotation.
 *
 * @see AutoProtoSchemaBuilder#includeClasses()
 */
@AutoProtoSchemaBuilder(includeClasses = {Feed.class}, schemaPackageName = "fun_contest")
public interface FeedContextInitializer extends SerializationContextInitializer {
}

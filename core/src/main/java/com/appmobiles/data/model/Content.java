package com.appmobiles.data.model;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;

/**
 * Custom Content implementation. Implements the interface of interaction with the Mongodb database.
 *
 * @see PanacheMongoEntity
 */
@RegisterForReflection
@MongoEntity(database = "feeds", collection = "content")
public class Content extends PanacheMongoEntity {

    @BsonId
    public String id;

    public List<String> links;
    public String title;
    public String text;
    public String language;
    public Integer ups;

    @Override
    public String toString() {

        return "Content{" +
                "id='" + id + '\'' +
                ", link=" + links +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", language='" + language + '\'' +
                ", ups=" + ups +
                '}';
    }
}
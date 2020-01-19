package com.appmobiles.cache;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.vertx.core.json.JsonObject;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;

import static com.appmobiles.Constants.*;
import static com.appmobiles.Utils.rndUUID;
import static java.util.stream.Collectors.toList;

@MongoEntity(database = "feeds", collection = "content")
public class Content extends PanacheMongoEntity {

    @BsonId
    public String id;

    public List<String> links;
    public String title;
    public String text;
    public String language;
    public Integer ups;

    public static Content of(JsonObject json) {
        final Content content = new Content();
        content.id = rndUUID();
        content.ups = json.getInteger("ups", 0);
        content.text = json.getString("text", EMPTY_STRING);
        content.title = json.getString("title", EMPTY_STRING);
        content.language = json.getString("language", DEFAULT_LANGUAGE);
        content.links = json.getJsonArray("links", EMPTY_JSON_ARRAY).stream().map(Object::toString).collect(toList());
        content.persistOrUpdate();
        return content;
    }

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
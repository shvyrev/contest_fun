package com.appmobiles.cache;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.util.List;
import java.util.Objects;

import static com.appmobiles.Constants.EMPTY_JSON_ARRAY;
import static com.appmobiles.Constants.EMPTY_STRING;

public class StorageItem {
    private final List<String> links;
    private final String title;
    private final String text;
    private final String language;
    private final Integer ups;

    @ProtoFactory
    public StorageItem(List<String> links, String title, String text, String language, Integer ups) {
        this.links = links;
        this.title = title;
        this.text = text;
        this.language = language;
        this.ups = ups;
    }

    @SuppressWarnings("unchecked")
    public static StorageItem of(JsonObject value) {
        return new StorageItem(
                value.getJsonArray("links", EMPTY_JSON_ARRAY).getList(),
                value.getString("title", EMPTY_STRING),
                value.getString("text", EMPTY_STRING),
                value.getString("language", EMPTY_STRING),
                value.getInteger("ups", 0)
        );
    }

    public JsonObject toJson(){
        return new JsonObject()
                .put("links", new JsonArray(getLinks()))
                .put("title", getTitle())
                .put("text", getText())
                .put("language", getLanguage())
                .put("ups", getUps());
    }

    public String toJsonString() {
        return Json.encode(this);
    }

    @ProtoField(number = 1)
    public List<String> getLinks() {
        return links;
    }

    @ProtoField(number = 2)
    public String getTitle() {
        return title;
    }

    @ProtoField(number = 3)
    public String getText() {
        return text;
    }

    @ProtoField(number = 4)
    public String getLanguage() {
        return language;
    }

    @ProtoField(number = 5)
    public Integer getUps() {
        return ups;
    }

    @Override
    public String toString() {
        return "StorageItem{" +
                "links=" + links +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", language='" + language + '\'' +
                ", ups=" + ups +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageItem that = (StorageItem) o;
        return Objects.equals(links, that.links) &&
                Objects.equals(title, that.title) &&
                Objects.equals(text, that.text) &&
                Objects.equals(language, that.language) &&
                Objects.equals(ups, that.ups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(links, title, text, language, ups);
    }
}

package com.appmobiles.data.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.util.Objects;

/**
 * Custom feed implementation.
 */
@RegisterForReflection
public class Feed {
    public final String id;
    public final String title;
    public final Long timestamp;
    public final String language;
    public final Integer ups;

    @ProtoFactory
    public Feed(String id, String title, Long timestamp, String language, Integer ups) {
        this.id = id;
        this.title = title;
        this.timestamp = timestamp;
        this.language = language;
        this.ups = ups;
    }

    @ProtoField(number = 1, required = true)
    public String getId() {
        return id;
    }

    @ProtoField(number = 2)
    public String getTitle() {
        return title;
    }

    @ProtoField(number = 3, required = true)
    public Long getTimestamp() {
        return timestamp;
    }

    @ProtoField(number = 4, required = true)
    public String getLanguage() {
        return language;
    }

    @ProtoField(number = 5, required = true)
    public Integer getUps() {
        return ups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feed feed = (Feed) o;
        return Objects.equals(id, feed.id) &&
                Objects.equals(title, feed.title) &&
                Objects.equals(timestamp, feed.timestamp) &&
                Objects.equals(language, feed.language) &&
                Objects.equals(ups, feed.ups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, timestamp, language, ups);
    }

    @Override
    public String toString() {
        return "Feed{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", timestamp=" + timestamp +
                ", language='" + language + '\'' +
                ", ups=" + ups +
                '}';
    }
}
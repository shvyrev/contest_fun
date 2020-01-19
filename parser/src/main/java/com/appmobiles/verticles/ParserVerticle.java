package com.appmobiles.verticles;

import com.appmobiles.exceptions.WrongLanguageException;
import com.appmobiles.verticles.abstracts.AbstractTaskVerticle;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.appmobiles.Addresses.*;
import static com.appmobiles.Constants.*;
import static com.appmobiles.Types.*;
import static com.appmobiles.Utils.messageHandler;
import static com.appmobiles.Utils.promiseHandler;

public class ParserVerticle extends AbstractTaskVerticle {

    private JsonArray languages;
    private LanguageDetector languageDetector;

    @Override
    protected void initSuccess(Promise<Void> startPromise) {

        List<LanguageProfile> languageProfiles = null;
        languages = config().getJsonArray("textLanguages", EMPTY_JSON_ARRAY);

        try {
            languageProfiles = new LanguageProfileReader().readAllBuiltIn();
            languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard()).withProfiles(languageProfiles)
                    .build();

            requestNextTask(PARSER_TASK_TYPE);
            startPromise.complete();
        } catch (Exception e) {
            startPromise.fail(e);
        }
    }

    private static final Logger log = LoggerFactory.getLogger( ParserVerticle.class );

    @Override
    protected void startTask(JsonObject json) {
        log.info(" $ startTask : " + json.encodePrettily());

        if (!json.isEmpty()) {
            final String url = json.getString(URL, "");

            switch (json.getString(TYPE)){
                case SEED:
                    parse(GET_CONTENT_PAGE_URLS, url).onComplete(parseHandler(CONTENT_PAGE_URLS));
                    break;
                case CONTENT_PAGE_URL:
                    parse(GET_CONTENT_PAGE, url).compose(o -> detectLanguage((JsonObject) o))
                            .onComplete(parseHandler(PARSED_PAGE_CONTENT));
                    break;
            }
        } else {
            requestNextTask(PARSER_TASK_TYPE);
        }
    }

    /**
     * Определение языка текста. Если язык текста не содержиться в списке разрешенных языков,
     * future завершается с ошибкой.
     * @param jsonObject - объект содержащий данные для определения языка
     * @return
     */
    @SuppressWarnings("unchecked")
    private Future<JsonObject> detectLanguage(JsonObject jsonObject) {

        final Promise<JsonObject> promise = Promise.promise();
        final String text = jsonObject.getString("text", EMPTY_STRING);

        if (!jsonObject.isEmpty() && !text.isEmpty()) {
            getVertx().executeBlocking(event -> {
                try {
                    final String language = languageDetector.detect(textFactory(text))
                            .or(() -> LdLocale.fromString(DEFAULT_LANGUAGE)).getLanguage();
                    if (!languages.contains(language)) {
                        throw new WrongLanguageException(language, languages.getList());
                    }
                    jsonObject.put("language", language);
                    event.complete(jsonObject);
                } catch (Exception e) {
                    event.fail(e);
                }
            }, promiseHandler(promise));
        } else {
            promise.complete(new JsonObject());
        }
        return promise.future();
    }

    @SuppressWarnings("UnstableApiUsage")
    private TextObject textFactory(String text) {
        TextObjectFactory textObjectFactory = null;
        if (text.length() > MAX_TWITTER_MESSAGE_LENGTH) {
            textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
        } else {
            textObjectFactory = CommonTextObjectFactories.forDetectingShortCleanText();
        }
        return textObjectFactory.forText(text);
    }

    private <V> Handler<AsyncResult<V>> parseHandler(String type) {
        return event -> {
            if (event.succeeded()) {
                eventBus().send(type, event.result());
            } else {
                event.cause().printStackTrace();
            }
            requestNextTask(PARSER_TASK_TYPE);
        };
    }

    private <V> Future<V> parse(String type, String url) {
        final Promise<V> promise = Promise.promise();
        eventBus().<V>send(type, url, messageHandler(promise));
        return promise.future();
    }
}

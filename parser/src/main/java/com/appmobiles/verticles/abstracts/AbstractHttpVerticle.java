package com.appmobiles.verticles.abstracts;

import com.appmobiles.Utils;
import com.google.common.collect.Iterables;
import com.google.common.hash.Hashing;
import com.google.common.net.HostAndPort;
import io.minio.MinioClient;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.net.ProxyOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;

import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import static com.appmobiles.Constants.*;
import static com.appmobiles.Utils.rndUUID;
import static java.lang.Long.valueOf;
import static java.lang.String.format;

/**
 * An abstract base class that you can extend to write Verticles for workers that process HTTP requests.
 * <p>
 * In the simplest case, just override {@link #getHtml(String)}, {@link #download(String)}
 * and {@link #upload(String, String)} methods.
 * </p>
 *
 */
public abstract class AbstractHttpVerticle extends AbstractWorkerVerticle {

    private String bucketName;
    private String storageLinkPattern;
    private Iterator<HostAndPort> proxies;
    private MinioClient minioClient;

    /**
     * In this method, a list of proxies is required for processing web requests,
     * a client is instantiated to access the S3/Minio storage.
     * {@inheritDoc}
     *
     * @param startPromise a future which should be called when verticle start-up is complete.
     *
     * @throws Exception if could not get the proxy list, or could not establish a connection to the S3/Minio storage
     */
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        bucketName = config().getString("bucketName", DEFAULT_BUCKET_NAME);
        storageLinkPattern = config().getString("storageLinkPattern", EMPTY_STRING);
        proxies = Iterables.cycle(
                config().getJsonArray("proxies", EMPTY_JSON_ARRAY).stream()
                        .map(o -> HostAndPort.fromString(o.toString()))
                        .toArray(HostAndPort[]::new)
        ).iterator();

        minioClient = new MinioClient(
                config().getString("storageUrl", EMPTY_STRING),
                config().getString("storageUser", EMPTY_STRING),
                config().getString("storagePassword", EMPTY_STRING)
        );

        super.start(startPromise);
    }

    /**
     * Gets HTML by Url.
     *
     * @param url http link, not null
     *
     * @return {@link Future} Determines the success of receiving HTML text from a link.
     * If the text could not be obtained, it ends with an error describing the situation.
     */
    protected Future<String> getHtml(@NotNull String url){

        final Promise<String> promise = Promise.promise();

        final WebClient client = getClient();
        client.getAbs(url).as(BodyCodec.string(UTF_8))
                .send(ar -> {
                    client.close();
                    if (ar.succeeded()) {
                        promise.complete(ar.result().body());
                    } else {
                        promise.fail(ar.cause());
                    }
                });
        return promise.future();
    }

    /**
     * Saves the media content available by url to a temporary file on the hard drive.
     *
     * @param url http link, not null
     *
     * @return {@link Future} Determines the success of receiving a media file from a link.
     * If the media file could not be saved, it ends with an error describing the situation.
     */
    protected Future<String> download(String url){

        final Promise<String> promise = Promise.promise();

        final WebClient client = getClient();
        final FileSystem fileSystem = getVertx().fileSystem();

        fileSystem.createTempFile(rndUUID(), "", ar -> {
            if (ar.succeeded()) {
                final AsyncFile asyncFile = fileSystem.openBlocking(ar.result(), new OpenOptions().setSync(true));
                client.getAbs(url).as(BodyCodec.pipe(asyncFile)).send(event -> {
                    client.close();
                    if (event.succeeded()) {
                        promise.complete(ar.result());
                    } else {
                        promise.fail(event.cause());
                    }
                });
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    private WebClient getClient(){
        return WebClient.create(getVertx(), getOptions());
    }

    @SuppressWarnings("UnstableApiUsage")
    private WebClientOptions getOptions() {

        final WebClientOptions clientOptions = new WebClientOptions()
                .setSsl(true)
                .setLogActivity(true)
                .setDefaultPort(SSL_PORT)
                .setConnectTimeout(TIMEOUT)
                .setUserAgent(USER_AGENT)
                .setTrustAll(true)
                .setVerifyHost(false)
                .setFollowRedirects(true);
        if (proxies.hasNext()) {
            final HostAndPort hostAndPort = proxies.next();
            clientOptions.setProxyOptions(new ProxyOptions()
                    .setHost(hostAndPort.getHost())
                    .setPort(hostAndPort.getPort())
            );
        }
        return clientOptions;
    }

    /**
     * Upload a temporary file to the storage server S3/Minio.
     * As file name, hash the sum murmur_32 is contents of file.
     * As a file extension, source file extension is used.
     *
     * @param filePath path to temporary file on disk, not null
     * @param fileName source file name, not null
     *
     * @return {@link Future} Determines the success of file upload to the storage S3/Minio.
     * If the file could not be uploaded, it ends with an error describing the situation.
     * @see Hashing#murmur3_32()
     */
    protected Future<String> upload(@NotNull String filePath, @NotNull String fileName){

        final Promise<String> promise = Promise.promise();
        try {
            final String hashedFilename = Utils.hashedFileName(filePath, fileName);
            final String prefix = Utils.hashFile(filePath);

            if (!minioClient.bucketExists(bucketName)) {
                minioClient.makeBucket(bucketName);
            }

            final String format = format(storageLinkPattern, bucketName, hashedFilename);
            if (!existObjectInStorage(bucketName, prefix)) {
                final InputStream inputStream = Files.newInputStream(Paths.get(filePath));
                minioClient.putObject(bucketName, hashedFilename, filePath, valueOf(inputStream.available()),null, null, CONTENT_TYPE);
                inputStream.close();
            }
            promise.complete(format);
        } catch (Exception e) {
            promise.fail(e);
        }
        return promise.future();
    }

    private boolean existObjectInStorage(String bucketName, String filePrefix) throws Exception {
        return StreamSupport.stream(minioClient.listObjects(bucketName, filePrefix).spliterator(), true)
                .findAny().isPresent();
    }
}

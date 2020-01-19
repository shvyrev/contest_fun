package com.appmobiles;

/**
 * A list of addresses used to organize messaging in the application.
 */
public class Addresses {
    /**
     *  The address of the event for receiving information about the seeds.
     */
    public static final String GET_SEEDS = "get-seeds";
    /**
     * This address is used to request a parsing task.
     */
    public static final String GET_PARSER_TASK = "get-parser-task";
    /**
     * This address is used to request a storage task.
     */
    public static final String GET_STORAGE_TASK = "get-storage-task";
    /**
     * This address is used to save a list of links to content pages.
     */
    public static final String CONTENT_PAGE_URLS = "content-page-urls";
    /**
     * This address is used to getting a list of links to content pages.
     */
    public static final String GET_CONTENT_PAGE_URLS = "get-content-page-urls";
    /**
     * This address is used to getting a content pages.
     */
    public static final String GET_CONTENT_PAGE = "get-content-page";
    /**
     * This address is used to save parsed page content.
     */
    public static final String PARSED_PAGE_CONTENT = "parsed-page-content";
    /**
     * This address is used to save information on received content in client applications for updating.
     */
    public static final String CONTENT_READY = "content-ready";

    /**
     * This address is used to compare text is already exist in bloom filter store.
     */
    public static final String CHECK_TEXT_EXISTS = "check-text-exists";

    /**
     * This address is used to upload media content to S3/Minio storage.
     */
    public static final String UPLOAD_MEDIA_CONTENT = "upload";



}

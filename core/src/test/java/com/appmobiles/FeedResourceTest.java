package com.appmobiles;

import com.appmobiles.data.model.Content;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.appmobiles.EndPoints.FEED;
import static com.appmobiles.EndPoints.FEED_LIST;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.*;

@Disabled
@QuarkusTest
@DisplayName("Feed requests test.")
public class FeedResourceTest {

    public static final int ITEMS_PER_PAGE = 25;
    private static RequestSpecification requestSpec;

    private static final Logger log = LoggerFactory.getLogger( FeedResourceTest.class );

    @BeforeAll
    static void init(){
        requestSpec = new RequestSpecBuilder()
                .setPort(8081)
                .setAccept(ContentType.JSON)
//                .setAccept("application/json;application/javascript;text/javascript;text/json;charset=utf-8")
                .setContentType(ContentType.ANY)
                .log(LogDetail.ALL)
                .build();
    }

    @ParameterizedTest(name = "Test getting feed by id.")
    @ValueSource(strings = {"e3fb6572-e0e5-460e-9c32-36377fb0bc30"})
    public void feed(String id){
        feedResponse(id, OK.getStatusCode())
                .extract().body().as(Content.class);
    }

    @ParameterizedTest(name = "Test getting feed not found error.")
    @ValueSource(strings = {"10"})
    public void feedNotFound(String id){
        feedResponse(id, NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest(name = "Test getting feed list for page.")
    @ValueSource(ints = {0})
    public void feedListFullPageSize(int pageNumber){
       feedListResponse(pageNumber, OK.getStatusCode())
                .and().body("", Matchers.hasSize(ITEMS_PER_PAGE));
    }

    @ParameterizedTest(name = "Test getting empty feed list.")
    @ValueSource(ints = {Integer.MAX_VALUE})
    public void feedListEmptyPage(int pageNumber){
        feedListResponse(pageNumber, OK.getStatusCode())
                .and().body("[]", Matchers.empty());
    }

    @ParameterizedTest(name = "Test getting feed list bad request error.")
    @ValueSource(ints = {Integer.MIN_VALUE})
    public void feedListWrongRequest(int pageNumber){
        feedListResponse(pageNumber, BAD_REQUEST.getStatusCode());
    }

    private ValidatableResponse feedResponse(String id, int statusCode) {
        return given()
                .contentType(ContentType.JSON)
                .when().get(FEED, id)
                .then()
                .assertThat()
                .statusCode(statusCode);
    }

    private ValidatableResponse feedListResponse(int pageNumber, int statusCode) {
        return given()
                .spec(requestSpec)
                .param("page", pageNumber)
                .param("size", ITEMS_PER_PAGE)
                .when().get(FEED_LIST)
                .then()
                .assertThat()
                .statusCode(statusCode);
    }

}
package io.kubernetes.client.examples;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Call;

import java.lang.reflect.Type;

/**
 * A simple example of how to use Watch API to watch changes in Namespace list.
 * <p>https://github.com/kubernetes-client/java/wiki/3.-Code-Examples
 * <p>watch on namespace object
 */
public class WatchExample {
    private static final Integer LIMIT = 5;
    private static final Boolean WATCH = Boolean.TRUE;

    public static void main(String[] args) throws IOException, ApiException {
        ApiClient apiClient = Config.defaultClient();
        // infinite timeout
        OkHttpClient httpClient =
                apiClient.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
        apiClient.setHttpClient(httpClient);
        Configuration.setDefaultApiClient(apiClient);

        CoreV1Api coreV1Api = new CoreV1Api();

        /**
         * watch flag is set in the call
         * CoreV1Api.listNamespaceCall and set watch to True and watch the changes to namespaces.
         */
        Call call = coreV1Api.listNamespaceCall(
                null, null, null, null, null, LIMIT, null, null, null, WATCH, null);

        /**
         * watchType The type of the WatchResponse
         */
        Type watchType = new TypeToken<Watch.Response<V1Namespace>>() {
        }.getType();

        /**
         * Creates a watch on a V1Namespace using an API Client and a Call object.
         * @return Watch object on V1Namespace
         * @throws ApiException on IO exceptions.
         */
        Watch<V1Namespace> watch = Watch.createWatch(apiClient, call, watchType);
        try {
            for (Watch.Response<V1Namespace> item : watch) {
                System.out.printf("%s : %s%n", item.type, item.object.getMetadata().getName());
            }
        } finally {
            watch.close();
        }
    }
}

package io.kubernetes.client.examples;

import io.kubernetes.client.monitoring.Monitoring;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;

import java.io.IOException;

/**
 * A simple example of how to use the Java API with Prometheus metrics
 *
 * <p>Easiest way to run this: mvn exec:java
 * -Dexec.mainClass="io.kubernetes.client.examples.PrometheusExample"
 *
 * <p>From inside $REPO_DIR/examples
 */
public class PrometheusExample {
  public static void main(String[] args) throws IOException, ApiException {
    ApiClient apiClient = Config.defaultClient();
    Configuration.setDefaultApiClient(apiClient);

    // Install an HTTP Interceptor that adds metrics
    Monitoring.installMetrics(apiClient);

    // Install a simple HTTP server to serve prometheus metrics. If you already are serving
    // metrics elsewhere, this is unnecessary.
    Monitoring.startMetricsServer("localhost", 8080);

    CoreV1Api coreV1Api = new CoreV1Api();
    String podName = "foo";
    String namespace = "bar";
    while (true) {
      // A request that should return 200
      coreV1Api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null);
      // A request that should return 404
      try {
        coreV1Api.readNamespacedPod(podName, namespace, null);
      } catch (ApiException ex) {
        if (ex.getCode() != 404) {
          throw ex;
        }
      }
      try {
        Thread.sleep(10000);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }
}

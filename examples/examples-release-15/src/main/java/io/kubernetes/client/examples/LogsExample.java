package io.kubernetes.client.examples;

import io.kubernetes.client.PodLogs;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Streams;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * A simple example of how to use the Java API
 *
 * <p>Easiest way to run this: mvn exec:java
 * -Dexec.mainClass="io.kubernetes.client.examples.LogsExample"
 *
 * <p>From inside $REPO_DIR/examples
 */
public class LogsExample {
  public static void main(String[] args) throws IOException, ApiException, InterruptedException {
    ApiClient apiClient = Config.defaultClient();
    Configuration.setDefaultApiClient(apiClient);
    CoreV1Api coreV1Api = new CoreV1Api(apiClient);

    String namespace = "default";
    String pretty = "false";

    V1PodList pods = coreV1Api.listNamespacedPod(
        namespace, pretty, null, null, null, null, null, null, null, null, null);
    if (pods != null && CollectionUtils.isNotEmpty(pods.getItems())) {
      V1Pod pod = pods.getItems().get(0);
      /**
       * Fetch logs from running containers, equal to kubectl logs
       */
      PodLogs podLogs = new PodLogs();
      InputStream is = podLogs.streamNamespacedPodLog(pod);
      Streams.copy(is, System.out);
    }
  }
}

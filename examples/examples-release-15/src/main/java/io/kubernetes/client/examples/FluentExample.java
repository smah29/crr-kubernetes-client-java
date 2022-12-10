package io.kubernetes.client.examples;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodBuilder;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.util.Config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

/**
 * A simple example of how to use the Java API
 *
 * <p>Easiest way to run this: mvn exec:java
 * -Dexec.mainClass="io.kubernetes.client.examples.FluentExample"
 *
 * <p>From inside $REPO_DIR/examples
 */
public class FluentExample {
  private static final String NAMESPACE = "default";

  public static void main(String[] args) throws IOException, ApiException {
    ApiClient apiClient = Config.defaultClient();
    Configuration.setDefaultApiClient(apiClient);

    CoreV1Api coreV1Api = new CoreV1Api();
    MyContainer container = new MyContainer("www", "nginx");
    /**
     * Construct arbitrary resource in a fluent builder style.
     */
    V1Pod pod1 = createPod("apod", container, true);

    coreV1Api.createNamespacedPod(NAMESPACE, pod1, null, null, null, null);

    V1Pod pod2 = createPod("anotherpod", container, false);

    coreV1Api.createNamespacedPod(NAMESPACE, pod2, null, null, null, null);

    V1PodList pods =
        coreV1Api.listNamespacedPod(
            NAMESPACE, null, null, null, null, null, null, null, null, null, null);
    if (pods != null && CollectionUtils.isNotEmpty(pods.getItems())) {
      pods.getItems().forEach((pod) -> System.out.println(pod.getMetadata().getName()));
    }
  }

  private static V1Pod createPod(String podName, MyContainer container, boolean fluent) {
    if (fluent) return createPodFluentStyle(podName, container);

    List<V1Container> containers = Arrays.asList(new V1Container().name(container.getName()).image(container.getImage()));
    return new V1Pod()
        .metadata(new V1ObjectMeta().name(podName))
        .spec(new V1PodSpec().containers(containers));
  }

  private static V1Pod createPodFluentStyle(String podName, MyContainer container) {
    return new V1PodBuilder()
        .withNewMetadata()
        .withName(podName)
        .endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName(container.getName())
        .withImage(container.getImage())
        .endContainer()
        .endSpec()
        .build();
  }

  @AllArgsConstructor
  @Getter
  static class MyContainer {
    String name;
    String image;
  }
}

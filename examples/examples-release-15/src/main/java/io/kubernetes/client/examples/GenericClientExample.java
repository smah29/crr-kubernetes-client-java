package io.kubernetes.client.examples;

import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.generic.GenericKubernetesApi;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.AllArgsConstructor;

public class GenericClientExample {
  private static final String NAMESPACE = "default";
  private static final String POD_NAME = "foo";
  private static final String API_VERSION = "v1";
  private static final String RESOURCE = "pods";

  // The following codes demonstrates using generic client to manipulate pods
  public static void main(String[] args) throws Exception {

    ApiClient apiClient = ClientBuilder.standard().build();
    GenericKubernetesApi<V1Pod, V1PodList> podClient =
        new GenericKubernetesApi<>(V1Pod.class, V1PodList.class, "", API_VERSION, RESOURCE, apiClient);

    MyContainer container = new MyContainer("c", "test");
    V1Pod pod = getPod(NAMESPACE, POD_NAME, container);

    createPod(pod, podClient);

    patchPod(podClient);

    deletePod(podClient);
  }

  private static void createPod(V1Pod pod, GenericKubernetesApi<V1Pod, V1PodList> podClient) throws ApiException {
    podClient.create(pod).throwsApiException().getObject();
    System.out.println("Created!");
  }

  private static void patchPod(GenericKubernetesApi<V1Pod, V1PodList> podClient) throws ApiException {
    podClient
        .patch(
            NAMESPACE,
            POD_NAME,
            V1Patch.PATCH_FORMAT_STRATEGIC_MERGE_PATCH,
            new V1Patch("{\"metadata\":{\"finalizers\":[\"example.io/foo\"]}}"))
        .throwsApiException()
        .getObject();
    System.out.println("Patched!");
  }

  private static void deletePod(GenericKubernetesApi<V1Pod, V1PodList> podClient) throws ApiException {
    V1Pod deletedPod = podClient.delete(NAMESPACE, POD_NAME).throwsApiException().getObject();
    if (deletedPod != null) {
      System.out.println(
          "Received after-deletion status of the requested object, will be deleting in background!");
    }
    System.out.println("Deleted!");
  }

  private static V1Pod getPod(String namespace, String podName, MyContainer container) {
    List<V1Container> containers = Arrays.asList(new V1Container().name(container.getName()).image(container.getImage()));
    V1ObjectMeta metadata = new V1ObjectMeta().name(podName).namespace(namespace);
    return new V1Pod().metadata(metadata).spec(new V1PodSpec().containers(containers));
  }

  @AllArgsConstructor
  @Getter
  static class MyContainer {
    String name;
    String image;
  }

}

package io.kubernetes.client.examples;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.apache.commons.collections.CollectionUtils;

import java.io.FileReader;
import java.io.IOException;

/**
 * A simple example of how to use the Java API from an application outside a kubernetes cluster
 *
 * <p>Easiest way to run this: mvn exec:java
 * -Dexec.mainClass="io.kubernetes.client.examples.KubeConfigFileClientExample"
 *
 * <p>From inside $REPO_DIR/examples
 * <p>Configure a client to access a Kubernetes cluster from outside.
 */
public class KubeConfigFileClientExample {
  public static void main(String[] args) throws IOException, ApiException {

    // file path to your KubeConfig

    String kubeConfigPath = System.getenv("HOME") + "/.kube/config";

    KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath));
    // loading the out-of-cluster config, a kubeconfig from file-system
    ApiClient apiClient = ClientBuilder.kubeconfig(kubeConfig).build();

    // set the global default coreV1Api-apiClient to the in-cluster one from above
    Configuration.setDefaultApiClient(apiClient);

    // the CoreV1Api loads default coreV1Api-apiClient from global configuration.
    CoreV1Api coreV1Api = new CoreV1Api();

    // invokes the CoreV1Api apiClient
    V1PodList pods =
        coreV1Api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null);

    // pods.getItems() are List<V1Pod>
    if (pods != null && CollectionUtils.isNotEmpty(pods.getItems())) {
      pods.getItems().forEach((pod) -> System.out.println(pod.getMetadata().getName()));
    }
  }
}

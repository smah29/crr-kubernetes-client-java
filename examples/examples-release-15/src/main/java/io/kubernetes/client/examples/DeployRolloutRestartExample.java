package io.kubernetes.client.examples;

import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentBuilder;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.PatchUtils;
import io.kubernetes.client.util.wait.Wait;

import java.util.function.Supplier;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;

public class DeployRolloutRestartExample {
  private static final String deploymentName = "example-nginx";
  private static final String imageName = "nginx:1.21.6";
  private static final String namespace = "default";

  public static void main(String[] args) throws IOException, ApiException {
    ApiClient apiClient = Config.defaultClient();
    Configuration.setDefaultApiClient(apiClient);
    AppsV1Api appsV1Api = new AppsV1Api(apiClient);

    V1Deployment deployment = getDeployment(deploymentName, imageName, namespace);

    appsV1Api.createNamespacedDeployment(
        namespace, deployment, null, null, null, null);

    waitUntilDeploymentIsReady(appsV1Api, deploymentName, namespace);
    System.out.println("Created example deployment!");

    // Trigger a rollout restart of the example deployment
    V1Deployment runningDeployment =
        appsV1Api.readNamespacedDeployment(deploymentName, namespace, null);

    // Explicitly set "restartedAt" annotation with current date/time to trigger rollout when patch
    // is applied
    updateDeployment(runningDeployment, "kubectl.kubernetes.io/restartedAt", LocalDateTime.now().toString());
    try {
      patchDeployment(apiClient, appsV1Api, runningDeployment);

      // Wait until deployment has stabilized after rollout restart
      waitUntilDeploymentIsReady(appsV1Api, deploymentName, namespace);
      System.out.println("Example deployment restarted successfully!");
    } catch (ApiException e) {
      e.printStackTrace();
    }
  }

  private static void patchDeployment(ApiClient apiClient, AppsV1Api appsV1Api, V1Deployment runningDeployment) throws ApiException {
    String deploymentJson = apiClient.getJSON().serialize(runningDeployment);

    PatchUtils.patch(
        V1Deployment.class,
        () ->
            appsV1Api.patchNamespacedDeploymentCall(
                deploymentName,
                namespace,
                new V1Patch(deploymentJson),
                null,
                null,
                "kubectl-rollout",
                null,
                null,
                null),
        V1Patch.PATCH_FORMAT_STRATEGIC_MERGE_PATCH,
        apiClient);
  }

  private static void updateDeployment(V1Deployment runningDeployment, String key, String value) {
    runningDeployment
        .getSpec()
        .getTemplate()
        .getMetadata()
        .putAnnotationsItem(key, value);
  }

  private static void waitUntilDeploymentIsReady(AppsV1Api appsV1Api, String deploymentName, String namespace) {
    Duration initialDelayAndInterval = Duration.ofSeconds(3);
    Duration timeout = Duration.ofSeconds(60);
    Supplier<Boolean> condition = () -> {
      try {
        System.out.println("Waiting until example deployment is ready...");
        return appsV1Api
            .readNamespacedDeployment(deploymentName, namespace, null)
            .getStatus()
            .getReadyReplicas()
            > 0;
      } catch (ApiException e) {
        e.printStackTrace();
        return false;
      }
    };
    Wait.poll(
        initialDelayAndInterval,
        timeout, condition
    );
  }

  // Create an example deployment
  private static V1Deployment getDeployment(String deploymentName, String imageName, String namespace) {

    V1ObjectMeta deploymentMeta = new V1ObjectMeta().name(deploymentName).namespace(namespace);
    int numberOfReplicas = 1;
    V1LabelSelector deploymentSelectorSpec = new V1LabelSelector().putMatchLabelsItem("name", deploymentName);

    V1ObjectMeta deploymentTemplateSpecMeta = new V1ObjectMeta().putLabelsItem("name", deploymentName);

    V1Container container = new V1Container().name(deploymentName).image(imageName);
    V1PodSpec podSpec = new V1PodSpec().containers(Collections.singletonList(container));

    V1PodTemplateSpec deploymentTemplateSpec = new V1PodTemplateSpec()
        .metadata(deploymentTemplateSpecMeta)
        .spec(podSpec);

    return new V1DeploymentBuilder()
        .withApiVersion("apps/v1")
        .withKind("Deployment")
        .withMetadata(deploymentMeta)
        .withSpec(
            new V1DeploymentSpec()
                .replicas(numberOfReplicas)
                .selector(deploymentSelectorSpec)
                .template(deploymentTemplateSpec))
        .build();
  }

}

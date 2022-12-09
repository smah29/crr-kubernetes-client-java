package io.kubernetes.client.examples;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodBuilder;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceBuilder;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * A simple example of how to parse a Kubernetes object.
 *
 * <p>Easiest way to run this: mvn exec:java
 * -Dexec.mainClass="io.kubernetes.client.examples.YamlExample"
 *
 * <p>From inside $REPO_DIR/examples
 * <p>Suggested way to load or dump resource in Yaml.
 */
public class YamlExample {
  private static final String NAMESPACE = "default";

  public static void main(String[] args) throws IOException, ApiException, ClassNotFoundException {
    V1Pod pod = createPod("apod", "www", "nginx");
    System.out.println(Yaml.dump(pod));

    V1Service svc = createService("aservice", "NodePort", "ClientIP", new ServicePort(8080, "TCP", "client"));
    System.out.println(Yaml.dump(svc));

    // Read yaml configuration file, and deploy it
    ApiClient apiClient = Config.defaultClient();
    Configuration.setDefaultApiClient(apiClient);

    //  See issue #474. Not needed at most cases, but it is needed if you are using war
    //  packging or running this on JUnit.
    Yaml.addModelMap("v1", "Service", V1Service.class);

    // Example yaml file can be found in $REPO_DIR/test-svc.yaml
    File file = new File("test-svc.yaml");
    V1Service yamlSvc = (V1Service) Yaml.load(file);

    // Deployment and StatefulSet is defined in apps/v1, so you should use AppsV1Api instead of
    // CoreV1API
    CoreV1Api coreV1Api = new CoreV1Api();
    V1Service newService =
        coreV1Api.createNamespacedService(NAMESPACE, yamlSvc, null, null, null, null);

    System.out.println(newService);

    V1Service deleteResult =
        coreV1Api.deleteNamespacedService(
            yamlSvc.getMetadata().getName(),
            NAMESPACE,
            null,
            null,
            null,
            null,
            null,
            new V1DeleteOptions());
    System.out.println(deleteResult);
  }

  @AllArgsConstructor
  @Getter
  static class ServicePort {
    int port;
    String protocol;
    String name;
  }

  private static V1Service createService(String serviceName, String serviceType, String sessionAffinity, ServicePort servicePort) {
    /**
     * Service is a named abstraction of software service (for example, mysql) consisting of local port
     * (for example 3306) that the proxy listens on, and the selector that determines which pods will
     * answer requests sent through the proxy.
     */
    V1Service svc =
        new V1ServiceBuilder()
            .withNewMetadata()
            .withName(serviceName)
            .endMetadata()
            .withNewSpec()
            /** Directs a particular client’s requests to the same backend VM
             * based on a hash created from the client’s IP address and the destination IP address.
             */
            .withSessionAffinity(sessionAffinity)
            .withType(serviceType)
            .addNewPort()
            .withProtocol(servicePort.getProtocol())
            .withName(servicePort.getName())
            .withPort(servicePort.getPort())
            .withNodePort(servicePort.getPort())
            .withTargetPort(new IntOrString(servicePort.getPort()))
            .endPort()
            .endSpec()
            .build();
    return svc;
  }

  private static V1Pod createPod(String podName, String instanceName, String image) {
    /**
     * Pod is a collection of containers that can run on a host. This resource is created by clients and
     * scheduled onto hosts.
     */
    V1Pod pod =
        new V1PodBuilder()
            .withNewMetadata()
            .withName(podName)
            .endMetadata()
            .withNewSpec()
            .addNewContainer()
            // Each Pod is meant to run a single instance of a given application.
            .withName(instanceName) // instance name
            // a Pod which consists of a container running the image nginix
            .withImage(image) // instance.getImage()
            .withNewResources()
            .withLimits(new HashMap<>())
            .endResources()
            .endContainer()
            .endSpec()
            .build();
    return pod;
  }
}

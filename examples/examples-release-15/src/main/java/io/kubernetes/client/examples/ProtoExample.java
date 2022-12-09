package io.kubernetes.client.examples;

import io.kubernetes.client.ProtoClient;
import io.kubernetes.client.ProtoClient.ObjectOrStatus;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.proto.Meta.ObjectMeta;
import io.kubernetes.client.proto.V1.Namespace;
import io.kubernetes.client.proto.V1.NamespaceSpec;
import io.kubernetes.client.proto.V1.Pod;
import io.kubernetes.client.proto.V1.PodList;
import io.kubernetes.client.util.Config;

import java.io.IOException;

/**
 * A simple example of how to use the Java API
 *
 * <p>Easiest way to run this: mvn exec:java
 * -Dexec.mainClass="io.kubernetes.client.examples.ProtoExample"
 *
 * <p>From inside $REPO_DIR/examples
 * <p>Request/receive payloads in protobuf serialization protocol.
 */
public class ProtoExample {

  private static final String NAMESPACE_DEFAULT = "default";
  private static final String NAMESPACE_TEST = "test";
  private static final String NAMESPACE_BASE_PATH = "/api/v1/namespaces";
  private static final String PODS_PATH = "/api/v1/namespaces/{0}/pods";
  private static final String KIND = "Namespace";
  private static final String API_VERSION = "v1";

  public static void main(String[] args) throws IOException, ApiException, InterruptedException {
    ApiClient apiClient = Config.defaultClient();
    Configuration.setDefaultApiClient(apiClient);

    ProtoClient protoClient = new ProtoClient(apiClient);
    fetchAllPodsByNameSpace(protoClient, NAMESPACE_DEFAULT);

    ObjectOrStatus<Namespace> ns = createNameSpace(protoClient, NAMESPACE_TEST);

    if (ns.object != null)
      updateNameSpace(protoClient, ns, NAMESPACE_TEST);

    deleteNameSpace(protoClient, NAMESPACE_TEST);
  }

  private static ObjectOrStatus<Namespace> createNameSpace(ProtoClient protoClient, String namespace) throws IOException, ApiException {
    Namespace namespaceObj =
        Namespace.newBuilder().setMetadata(ObjectMeta.newBuilder().setName(namespace).build()).build();
    ObjectOrStatus<Namespace> ns = protoClient.create(namespaceObj, NAMESPACE_BASE_PATH, API_VERSION, KIND);
    System.out.println(ns);
    return ns;
  }

  private static void updateNameSpace(ProtoClient protoClient, ObjectOrStatus<Namespace> ns, String namespace) throws IOException, ApiException {
    String nameSpacePath = NAMESPACE_BASE_PATH + namespace;
    Namespace namespaceObj =
        ns.object
            .toBuilder()
            .setSpec(NamespaceSpec.newBuilder().addFinalizers(namespace).build())
            .build();
    // This is how you would update an object, but you can't actually
    // update namespaces, so this returns a 405
    ns = protoClient.update(namespaceObj, nameSpacePath, API_VERSION, KIND);
    System.out.println(ns.status);
  }

  private static void deleteNameSpace(ProtoClient protoClient, String namespace) throws IOException, ApiException {
    String nameSpacePath = NAMESPACE_BASE_PATH + namespace;
    ObjectOrStatus<Namespace> ns = protoClient.delete(Namespace.newBuilder(), nameSpacePath);
    System.out.println(ns);
  }

  private static void fetchAllPodsByNameSpace(ProtoClient protoClient, String namespace) throws IOException, ApiException {
    String newPodsPath = PODS_PATH.replace("{0}", namespace);
    ObjectOrStatus<PodList> list = protoClient.list(PodList.newBuilder(), newPodsPath);

    if (list.object.getItemsCount() > 0) {
      Pod pod = list.object.getItems(0);
      System.out.println(pod);
    }
  }
}

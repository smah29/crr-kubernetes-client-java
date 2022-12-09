package io.kubernetes.client.examples;

import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.CallGenerator;
import io.kubernetes.client.util.CallGeneratorParams;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * A simple example of how to use the Java API
 *
 * <p>Easiest way to run this: mvn exec:java
 * -Dexec.mainClass="io.kubernetes.client.examples.InformerExample"
 *
 * <p>From inside $REPO_DIR/examples
 * <p>Build an informer which list-watches resources and reflects the notifications to a local cache.
 */
public class InformerExample {
  public static void main(String[] args) throws Exception {
    CoreV1Api coreV1Api = new CoreV1Api();
    ApiClient apiClient = coreV1Api.getApiClient();
    OkHttpClient httpClient =
        apiClient.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
    apiClient.setHttpClient(httpClient);

    SharedInformerFactory factory = new SharedInformerFactory(apiClient);

    SharedIndexInformer<V1Node> nodeInformer = getNodeSharedIndexInformer(coreV1Api, factory);

    factory.startAllRegisteredInformers();

    String nodeName = "noxu";

    createNode(coreV1Api, nodeName);
    Thread.sleep(3000);

    listenNode(nodeInformer, nodeName);
    factory.stopAllRegisteredInformers();
    Thread.sleep(3000);
    System.out.println("informer stopped..");
  }

  private static void listenNode(SharedIndexInformer<V1Node> nodeInformer, String nodeName) {
    Lister<V1Node> nodeLister = new Lister<V1Node>(nodeInformer.getIndexer());
    V1Node node = nodeLister.get(nodeName);
    System.out.printf(nodeName + " created! %s\n", node.getMetadata().getCreationTimestamp());
  }

  private static SharedIndexInformer<V1Node> getNodeSharedIndexInformer(CoreV1Api coreV1Api, SharedInformerFactory factory) {
    // **NOTE**:
    // The following "CallGeneratorParams" lambda merely generates a stateless
    // HTTPs requests, the effective apiClient is the one specified when constructing
    // the informer-factory.
    CallGenerator callGenerator = (CallGeneratorParams params) -> {
      return coreV1Api.listNodeCall(
          null,
          null,
          null,
          null,
          null,
          null,
          params.resourceVersion,
          null,
          params.timeoutSeconds,
          params.watch,
          null);
    };
    // Node informer
    SharedIndexInformer<V1Node> nodeInformer =
        factory.sharedIndexInformerFor(
            callGenerator,
            V1Node.class,
            V1NodeList.class);

    nodeInformer.addEventHandler(
        new ResourceEventHandler<V1Node>() {
          @Override
          public void onAdd(V1Node node) {
            System.out.printf("%s node added!\n", node.getMetadata().getName());
          }

          @Override
          public void onUpdate(V1Node oldNode, V1Node newNode) {
            System.out.printf(
                "%s => %s node updated!\n",
                oldNode.getMetadata().getName(), newNode.getMetadata().getName());
          }

          @Override
          public void onDelete(V1Node node, boolean deletedFinalStateUnknown) {
            System.out.printf("%s node deleted!\n", node.getMetadata().getName());
          }
        });
    return nodeInformer;
  }

  /**
   * A node may be a virtual or physical machine, depending on the cluster.
   * Each node is managed by the control plane and contains the services necessary to run Pods.
   * https://kubernetes.io/docs/concepts/architecture/nodes/
   */
  private static void createNode(CoreV1Api coreV1Api, String nodeName) throws ApiException {
    V1Node nodeToCreate = new V1Node();
    V1ObjectMeta metadata = new V1ObjectMeta();
    metadata.setName(nodeName);
    nodeToCreate.setMetadata(metadata);
    V1Node createdNode = coreV1Api.createNode(nodeToCreate, null, null, null, null);
  }
}

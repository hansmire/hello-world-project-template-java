// @@@SNIPSTART hello-world-project-template-java-workflow-initiator
package helloworldapp;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.temporal.api.common.v1.Payload;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.common.converter.CodecDataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.payload.codec.PayloadCodec;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class InitiateHelloWorld {

    public static List<PayloadCodec> codecs() {
        return  Collections.singletonList(new CryptCodec());
    }
    public static WorkflowClient workflowClient(
            String chainPath, String keyPath, String host
    ) throws Exception {
        InputStream chainStream = new FileInputStream(chainPath);
        InputStream  keyStream = new FileInputStream(keyPath);
        //InputStream  caStream = new FileInputStream("/Users/mhansmire/source/hello-world-project-template-java/private_key.pem"

        ChannelCredentials creds = TlsChannelCredentials.newBuilder()
                .keyManager(chainStream, keyStream)
                .build();

        ManagedChannel channel = Grpc.newChannelBuilderForAddress(host, 7233, creds).build();

        // This gRPC stubs wrapper talks to the local docker instance of the Temporal service.
        WorkflowServiceStubsOptions workflowServiceOptions =
                WorkflowServiceStubsOptions
                        .newBuilder()
                        .setChannel(channel)
                        .build();

        WorkflowServiceStubs service = WorkflowServiceStubs.newConnectedServiceStubs(workflowServiceOptions, null);

        // WorkflowClient can be used to start, signal, query, cancel, and terminate Workflows.
        WorkflowClient client = WorkflowClient.newInstance(
                service,
                WorkflowClientOptions
                        .newBuilder()
                        .setNamespace("max-test-namespace.rd0wc")
                        .setDataConverter(
                                new CodecDataConverter(
                                        DefaultDataConverter.newDefaultInstance(),
                                        codecs()
                                )
                        )
                        .build()
        );
        return client;
    }

    public static void main(String[] args) throws Exception {

        WorkflowClient client = workflowClient(args[0], args[1], args[2]);
        // Define our workflow unique id
        final String WORKFLOW_ID = "HelloWorldWorkflowID";

        /*
         * Set Workflow options such as WorkflowId and Task Queue so the worker knows where to list and which workflows to execute.
         */
        WorkflowOptions options = WorkflowOptions.newBuilder()
                    .setWorkflowId(WORKFLOW_ID)
                    .setTaskQueue(Shared.HELLO_WORLD_TASK_QUEUE)
                    .build();

        // Create the workflow client stub. It is used to start our workflow execution.
        HelloWorldWorkflow workflow = client.newWorkflowStub(HelloWorldWorkflow.class, options);

        /*
         * Execute our workflow and wait for it to complete. The call to our getGreeting method is
         * synchronous.
         * 
         * Replace the parameter "World" in the call to getGreeting() with your name.
         */
        String greeting = workflow.getGreeting("World");

        String workflowId = WorkflowStub.fromTyped(workflow).getExecution().getWorkflowId();
        // Display workflow execution results
        System.out.println(workflowId + " " + greeting);
        System.exit(0);
    }
}
// @@@SNIPEND

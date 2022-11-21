package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.Integration;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.RestApiProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.Bucket;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class LambdastackStack extends Stack {
    public LambdastackStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public LambdastackStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Lambda function
        Map<String,String> lambdaEnvMap = new HashMap<>();
        lambdaEnvMap.put("CACHE_ENDPOINT","dev/redis/data");
        lambdaEnvMap.put("DB_USER_SECRET","APP_USER-TPA_BO_DB");
        lambdaEnvMap.put("ENV_REGION","ap-northeast-1");

        Bucket  bucket = Bucket.Builder.create(this, "tpa-pp-events-poc")
                .versioned(true)
                .removalPolicy(RemovalPolicy.DESTROY)
                .autoDeleteObjects(true)
                .build();

        Function eventsFunction = new Function(this, "UpcomingEventLambdaPOC",
                getLambdaFunctionProps(lambdaEnvMap, "org.example.Function",bucket));

        RestApi api = new RestApi(this, "eventsApiPOC",
                RestApiProps.builder().restApiName("Upcoming Events POC")
                        .build());
        Resource events = api.getRoot().addResource("events");
        Integration eventsIntegration = new LambdaIntegration(eventsFunction);
        events.addMethod("GET",eventsIntegration);
    }

    private FunctionProps getLambdaFunctionProps(Map<String, String> lambdaEnvMap, String handler, Bucket bucket) {
        return FunctionProps.builder()
                .code(Code.fromAsset("C:\\Users\\Srinivas Reddy\\awswork\\hello-lambda\\target\\hello-lambda-1.0-SNAPSHOT.jar"))
                .handler(handler)
                .runtime(Runtime.JAVA_11)
                .environment(lambdaEnvMap)
                .timeout(Duration.seconds(30))
                .memorySize(512)
                .functionName("UpcomingEventsPOC")
                //.code(Code.fromBucket(bucket,"s3://tpa-pp-events-poc/hello-lambda-1.0-SNAPSHOT.jar"))
                .build();
    }
}

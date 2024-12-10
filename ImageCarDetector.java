import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImageCarDetector {

    public static void main(String[] args) {
        // Define the name of the S3 bucket containing the images
        String bucketName = "njit-cs-643";

        // Define the URL of the SQS queue for sending messages
        String queueUrl = "https://sqs.us-east-1.amazonaws.com/896973371682/pa1.fifo";

        // Initialize AWS S3 client for accessing S3 bucket
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

        // Initialize AWS Rekognition client for image analysis
        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

        // Initialize AWS SQS client for message queue operations
        AmazonSQS sqsClient = AmazonSQSClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

        // List to hold image file names (keys) from the S3 bucket
        List<String> imageKeys = new ArrayList<>();

        // Fetch the list of objects (images) from the specified S3 bucket
        ListObjectsV2Result result = s3Client.listObjectsV2(bucketName);
        for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
            // Add each image's key to the list
            imageKeys.add(objectSummary.getKey());
            // Print the image key to the console
            System.out.println("Image : " + objectSummary.getKey());
        }

        // Process each image retrieved from the S3 bucket
        for (String imageName : imageKeys) {
            // Create a request to detect labels in the image using Rekognition
            DetectLabelsRequest request = new DetectLabelsRequest()
                    .withImage(new Image().withS3Object(new S3Object().withName(imageName).withBucket(bucketName)))
                    .withMinConfidence(90F); // Minimum confidence level for detected labels

            // Get the labels detected in the image
            DetectLabelsResult labelResult = rekognitionClient.detectLabels(request);
            List<Label> labels = labelResult.getLabels();

            // Check if the label "Car" is detected in the image
            for (Label label : labels) {
                if ("Car".equalsIgnoreCase(label.getName())) {
                    // If a car is detected, send a message to the SQS queue
                    SendMessageRequest sendMessageRequest = new SendMessageRequest()
                            .withQueueUrl(queueUrl)
                            .withMessageBody(imageName) // Message contains the name of the image
                            .withMessageGroupId("carMessageGroup") // Required for FIFO queues
                            .withMessageDeduplicationId(UUID.randomUUID().toString()); // Unique deduplication ID
                    sqsClient.sendMessage(sendMessageRequest);
                    // Print confirmation to the console
                    System.out.println("Car detected in " + imageName + ", message sent to SQS!");
                    break; // Exit the loop as we only need one detection per image
                }
            }
        }

        // Send a termination signal message to the SQS queue to indicate processing is complete
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody("-1") // Special message to signal termination
                .withMessageGroupId("carMessageGroup") // Required for FIFO queues
                .withMessageDeduplicationId(UUID.randomUUID().toString()); // Unique deduplication ID
        sqsClient.sendMessage(sendMessageRequest);

        // Print confirmation to the console
        System.out.println("End of processing message sent to SQS!");
    }
}

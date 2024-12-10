import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

public class TextDetector {

    public static void main(String[] args) {
        // S3 bucket name containing the images
        String bucketName = "njit-cs-643";

        // URL of the SQS queue to receive messages from
        String queueUrl = "https://sqs.us-east-1.amazonaws.com/896973371682/pa1.fifo";

        // File path where the text detection results will be written
        String outputFilePath = "/home/ec2-user/text-detection-results.txt";

        // Initialize AWS S3, Rekognition, and SQS clients
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        AmazonSQS sqsClient = AmazonSQSClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            boolean processing = true;

            while (processing) {
                // Receive messages from the SQS queue
                ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                        .withQueueUrl(queueUrl)
                        .withMaxNumberOfMessages(1) // Process one message at a time
                        .withWaitTimeSeconds(5);  // Long polling for up to 5 seconds

                List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();

                for (Message message : messages) {
                    String imageName = message.getBody();
                    System.out.println("Received message: " + message.getBody());

                    // Check if the termination signal (-1) is received
                    if (imageName.equals("-1")) {
                        sqsClient.deleteMessage(queueUrl, message.getReceiptHandle());
                        System.out.println("Termination signal received. Ending processing.");
                        processing = false;
                        break;
                    }

                    // Perform text detection on the specified image
                    DetectTextRequest textRequest = new DetectTextRequest()
                            .withImage(new Image().withS3Object(new S3Object().withName(imageName).withBucket(bucketName)));

                    // Get the text detection results
                    DetectTextResult textResult = rekognitionClient.detectText(textRequest);
                    List<TextDetection> textDetections = textResult.getTextDetections();

                    // Write results to the output file
                    writer.write("Image: " + imageName + "\n");
                    if (!textDetections.isEmpty()) {
                        for (TextDetection text : textDetections) {
                            writer.write("Detected Text: " + text.getDetectedText() + "\n");
                        }
                        System.out.println("Text detected in " + imageName + ". Results written to file.");
                    } else {
                        writer.write("No text detected.\n");
                        System.out.println("No text detected in " + imageName + ". Results written to file.");
                    }
                    writer.write("\n");

                    // Delete the processed message from the SQS queue
                    sqsClient.deleteMessage(queueUrl, message.getReceiptHandle());
                }
            }
        } catch (Exception e) {
            // Print stack trace for any exceptions encountered during execution
            e.printStackTrace();
        }
    }
}

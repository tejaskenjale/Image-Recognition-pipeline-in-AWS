package cloud_computing_assign1.car_text;

import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;
import java.util.*;
import java.io.*;

public class Car_text 
{
	

	//functioning method for processing the images
	
    public static void functioning(S3Client s3_client, RekognitionClient rec_client, SqsClient sqs_client, String bucket_1, String name_of_queue) 
    {    
    	boolean w = false;
        while (!w) 
        {
            ListQueuesRequest ReqQList = ListQueuesRequest.builder().queueNamePrefix(name_of_queue).build();
            ListQueuesResponse ResQList = sqs_client.listQueues(ReqQList);
            if (ResQList.queueUrls().size() > 0)
                w = true;
        }

        
        String url = "";
        try 
        {
            GetQueueUrlRequest getReqQ = GetQueueUrlRequest.builder().queueName(name_of_queue).build();
            url = sqs_client.getQueueUrl(getReqQ).queueUrl();
        }
        catch (QueueNameExistsException e) 
        {
            throw e;
        }

        
        try 
        {
            boolean z = false;
            HashMap<String, String> outputs = new HashMap<String, String>();

            while (!z) 
            {
                
                ReceiveMessageRequest MsgReqRx = ReceiveMessageRequest.builder().queueUrl(url).maxNumberOfMessages(1).build();
                List<Message> messages = sqs_client.receiveMessage(MsgReqRx).messages();

                if (messages.size() > 0) 
                {
                    Message message = messages.get(0);
                    String l2 = message.body();

                    if (l2.equals("-1")) 
                    {
                        z = true;
                    } 
                    else 
                    {
                    	System.out.println("The image " + l2 + " with text processed");

                        Image img = Image.builder().s3Object(S3Object.builder().bucket(bucket_1).name(l2).build()).build();
                        DetectTextRequest req2 = DetectTextRequest.builder().image(img).build();
                        DetectTextResponse result = rec_client.detectText(req2);
                        List<TextDetection> textDetections = result.textDetections();

                        if (textDetections.size() != 0) 
                        {
                            String t = "";
                            for (TextDetection textDetection : textDetections) 
                            {
                                if (textDetection.type().equals(TextTypes.WORD))
                                    t = t.concat(" " + textDetection.detectedText());
                            }
                            outputs.put(l2, t);
                        }
                    }

                    
                    DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder().queueUrl(url).receiptHandle(message.receiptHandle()).build();
                    sqs_client.deleteMessage(deleteMessageRequest);
                }
            }
            
            try 
            {
                FileWriter writer = new FileWriter("Results.txt");

                Iterator<Map.Entry<String, String>> y = outputs.entrySet().iterator();
                while (y.hasNext()) {
                    Map.Entry<String, String> kv = y.next();
                    writer.write("The image is: - " + "\n");
                    writer.write(kv.getKey() + ":" + kv.getValue() + "\n");
                    y.remove();
                }

                writer.close();
                System.out.println("Image containing text are stored in a file 'Results.txt'");			//Storing images in Results.txt file.
            }
            
            catch (IOException e) 
            {
                System.out.println(e);
            }
        }
        
        catch (Exception e) 
        {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }
    
    
    //Main method declaring string variables and calling functioning method.
    
    public static void main(String[] args) 
    {

        String bucket_1 = "njit-cs-643";
        String name_of_queue = "sqs.fifo"; 

        S3Client s3_client = S3Client.builder().region(Region.US_EAST_1).build();
        RekognitionClient rec_client = RekognitionClient.builder().region(Region.US_EAST_1).build();
        SqsClient sqs_client = SqsClient.builder().region(Region.US_EAST_1).build();

        functioning(s3_client, rec_client, sqs_client, bucket_1, name_of_queue);
    }
    
    
    
}
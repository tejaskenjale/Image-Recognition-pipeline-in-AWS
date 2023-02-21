package cloud_computing_assign1.image_recognition;

import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import java.util.*;

public class Image_recognition 
{


    public static void bucket_Image(S3Client s3_client, RekognitionClient rec_client, SqsClient sqs_client, String bucket_1, String name_of_queue, String qg) 
    {

        String url = "";
        try 
        {
            ListQueuesRequest req = ListQueuesRequest.builder().queueNamePrefix(name_of_queue).build();
            ListQueuesResponse res = sqs_client.listQueues(req);

            if (res.queueUrls().size() == 0) 
            {
                CreateQueueRequest request = CreateQueueRequest.builder().attributesWithStrings(Map.of("FifoQueue", "true", "ContentBasedDeduplication", "true")).queueName(name_of_queue).build();
                sqs_client.createQueue(request);

                GetQueueUrlRequest url2 = GetQueueUrlRequest.builder().queueName(name_of_queue).build();
                url = sqs_client.getQueueUrl(url2).queueUrl();
            } else 
            {
            	url = res.queueUrls().get(0);
            }
        } 
        catch (QueueNameExistsException e) 
        {
            throw e;
        }

        
        try 
        {
            ListObjectsV2Request listObjectsReqManual = ListObjectsV2Request.builder().bucket(bucket_1).maxKeys(10).build();
            ListObjectsV2Response listObjResponse = s3_client.listObjectsV2(listObjectsReqManual);

            for (S3Object ob : listObjResponse.contents()) 
            {
                System.out.println("Image " + ob.key()+ " stored");

                Image img = Image.builder().s3Object(software.amazon.awssdk.services.rekognition.model.S3Object.builder().bucket(bucket_1).name(ob.key()).build()).build();
                DetectLabelsRequest req1 = DetectLabelsRequest.builder().image(img).minConfidence((float) 90).build();
                DetectLabelsResponse result = rec_client.detectLabels(req1);
                List<Label> labels = result.labels();

                for (Label l1 : labels) 
                {
                    if (l1.name().equals("Car")) 
                    {
                        sqs_client.sendMessage(SendMessageRequest.builder().messageGroupId(qg).queueUrl(url).messageBody(ob.key()).build());
                        break;
                    }
                }
            }

            sqs_client.sendMessage(SendMessageRequest.builder().queueUrl(url).messageGroupId(qg).messageBody("-1").build());
        } 
        catch (Exception e) 
        {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }
    
    
    
    //Main method declaring string variables and calling bucket_Image method.
    
    public static void main(String[] args) 
    {

        String bucket_1 = "njit-cs-643";
        String name_of_queue = "sqs.fifo";
        String qg = "abc";

        S3Client s3_client = S3Client.builder().region(Region.US_EAST_1).build();
        RekognitionClient rec_client = RekognitionClient.builder().region(Region.US_EAST_1).build();
        SqsClient sqs_client = SqsClient.builder().region(Region.US_EAST_1).build();

        bucket_Image(s3_client, rec_client, sqs_client, bucket_1, name_of_queue, qg);
    }
    
    
    
}

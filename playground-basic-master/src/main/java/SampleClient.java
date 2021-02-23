import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SampleClient {

    public static void main(String[] theArgs) {

        SampleClient sampleClientObj = new SampleClient();
        // Create a FHIR client
        IGenericClient client = sampleClientObj.initRestClient(SampleClientConstants.FHIR_URL);
        // Search for Patient resources
//        Bundle response = client
//                .search()
//                .forResource("Patient")
//                .where(Patient.FAMILY.matches().value("SMITH"))
//                .returnBundle(Bundle.class)
//                .execute();
        // read last names from file into a list of strings
        List<String> lastNameList = sampleClientObj.readLastNamesFromFile(SampleClientConstants.LAST_NAME_FILE_PATH);
        // process runs for the last name list
        List<Long> averageTimes = sampleClientObj.processRuns(client,lastNameList);
        if(averageTimes != null)
            System.out.println("Average times for " + averageTimes.size() + " runs: " + averageTimes.toString());
        else
            System.out.println("Generic client is null or invalid input last name list or SampleClientResponseTimeInterceptor is missing");
    }

    /*
        Create FHIR client and register interceptors
    */
    protected IGenericClient initRestClient(String url)
    {
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient(url);
        client.registerInterceptor(new LoggingInterceptor(false));
        // add new Interceptor for getting request response time
        client.registerInterceptor(new SampleClientResponseTimeInterceptor());
        client.forceConformanceCheck();
        return client;
    }

    /*
       Process a number of searches of a last name list, waiting between calls a number of seconds
       and getting a list of average response time for each of searches
    */
    public List<Long> processRuns(IGenericClient client, List<String> lastNameList)
    {
        if(client == null) {
            System.out.println("Generic client is null");
            return null;
        }
        // if the last name list is invalid return empty list
        if(lastNameList == null || lastNameList.isEmpty())
        {
            System.out.println("Null or empty last name list");
            return null;
        }
        // get SampleClientInterceptor for response time
        SampleClientResponseTimeInterceptor sampleClientResponseTimeInterceptor = (SampleClientResponseTimeInterceptor)client.
                getInterceptorService().
                getAllRegisteredInterceptors().stream().filter(x -> x instanceof SampleClientResponseTimeInterceptor).
                findFirst().
                orElse(null);
        if(sampleClientResponseTimeInterceptor == null) {
            System.out.println("No registered interceptor found for SampleClientInterceptor class");
            return null;
        }
        // list of average response time
        List<Long> averageTimes = new ArrayList<>();
        Long averageTime;
        boolean noCache;
        int index;
        for(index = 0; index < SampleClientConstants.NUMBER_OF_RUNS; index++)
        {
            // disable cache for the last search
            noCache = index == SampleClientConstants.NUMBER_OF_RUNS - 1;
            // get average response time for last name list search
            averageTime = searchLastNames(client, sampleClientResponseTimeInterceptor, lastNameList, noCache);
            // add to the averageTime list
            averageTimes.add(averageTime);
            System.out.println("Average time per " + lastNameList.size() + " runs for iteration " + (index + 1) + " : " + averageTime);
            // sleep if not the last run
            if(index != SampleClientConstants.NUMBER_OF_RUNS - 1)
            {
                try
                {
                    Thread.sleep(SampleClientConstants.WAIT_BETWEEN_RUNS);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        // return average time list
        return averageTimes;
    }

    /*
        Read last names from file
     */
    protected List<String> readLastNamesFromFile(String fileName)
    {
        List<String> lastNameList = null;
        if(fileName == null || fileName.isEmpty() || fileName.trim().isEmpty())
            return lastNameList;
        try {
            lastNameList = Files.readAllLines(Paths.get(fileName));

        } catch (IOException e) {
            System.out.println("Could not open file: " + fileName);
        }
        return lastNameList;
    }

    /*
        Call REST search for a list of last names and returns the average response time for the list
    */
    private Long searchLastNames(IGenericClient client, SampleClientResponseTimeInterceptor sampleClientResponseTimeInterceptor, List<String> lastNameList, boolean noCache)
    {
        long sumTimeForRun = 0L;
        Bundle response;
        for(String lastName: lastNameList)
        {
            response = client
                .search()
                .forResource(SampleClientConstants.PATIENT_KEY)
                .where(Patient.FAMILY.matches().value(lastName)).cacheControl(new CacheControlDirective().setNoCache(noCache))
                .returnBundle(Bundle.class)
                .execute();
            sumTimeForRun += sampleClientResponseTimeInterceptor.getResponseTime();
            System.out.println("Time per request: " + sampleClientResponseTimeInterceptor.getResponseTime());
        }
        System.out.println("Total sum for " + lastNameList.size() + " requests: " + sumTimeForRun);
        return sumTimeForRun/lastNameList.size();
    }
}

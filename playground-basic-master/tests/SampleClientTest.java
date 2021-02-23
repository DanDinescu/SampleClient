import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SampleClientTest {

    @Test
    public void testReadLastNamesFromInvalidFile() {
        SampleClient obj = new SampleClient();
        List<String> result = obj.readLastNamesFromFile("abc");
        Assert.assertNull(result);

    }

    @Test
    public void testReadLastNamesFromValidFile() {
        SampleClient obj = new SampleClient();
        List<String> result = obj.readLastNamesFromFile("tests/SampleClientLastNamesTest.txt");
        Assert.assertEquals(result.size(),2);

    }

    @Test
    public void testProcessRunsWithValidList() {
        SampleClient obj = new SampleClient();
        IGenericClient client = obj.initRestClient(SampleClientConstants.FHIR_URL);
        List<String> testList = new ArrayList<>();
        testList.add("WRIGHT");
        testList.add("FORD");
        testList.add("DENNIS");
        testList.add("ANA");
        testList.add("ANDREA");
        testList.add("LINDA");
        testList.add("THOMS");
        testList.add("GARISTO");
        testList.add("CHEN");
        List<Long> result = obj.processRuns(client,testList);
        Assert.assertEquals(result.size(),SampleClientConstants.NUMBER_OF_RUNS);
        System.out.println("Result Average List: "+ result.toString());
        Long minValue = result.stream().mapToLong(v -> v).min().getAsLong();
        System.out.println("Min average: "+ minValue);
        Assert.assertEquals(minValue,result.get(1));

    }

    @Test
    public void testProcessRunsWithNullList() {
        SampleClient obj = new SampleClient();
        IGenericClient client = obj.initRestClient(SampleClientConstants.FHIR_URL);
        List<Long> result = obj.processRuns(client,null);
        Assert.assertNull(result);

    }

    @Test
    public void testProcessRunsWithEmptyList() {
        SampleClient obj = new SampleClient();
        IGenericClient client = obj.initRestClient(SampleClientConstants.FHIR_URL);
        List<Long> result = obj.processRuns(client,new ArrayList());
        Assert.assertNull(result);

    }

    @Test
    public void testProcessRunsWithNullRestClient() {
        SampleClient obj = new SampleClient();
        List<Long> result = obj.processRuns(null,new ArrayList());
        Assert.assertNull(result);

    }

    @Test
    public void testInitRestClient() {
        SampleClient obj = new SampleClient();
        IGenericClient client = obj.initRestClient(SampleClientConstants.FHIR_URL);
        client.forceConformanceCheck();
    }

    @Test(expected = FhirClientConnectionException.class)
    public void testInitRestClientInvalidUrl() {
        SampleClient obj = new SampleClient();
        IGenericClient client = obj.initRestClient("InvalidUrl");
        client.forceConformanceCheck();
    }

    @Test(expected = FhirClientConnectionException.class)
    public void testProcessRunsWithInvalidUrl() {
        SampleClient obj = new SampleClient();
        IGenericClient client = obj.initRestClient("InvalidUrl");
        List<String> testList = new ArrayList<>();
        testList.add("WRIGHT");
        testList.add("FORD");
        obj.processRuns(client,testList);
    }


    @Test()
    public void testProcessRunsWithoutSampleClientInterceptor() {
        SampleClient obj = new SampleClient();
        IGenericClient client = obj.initRestClient(SampleClientConstants.FHIR_URL);
        client.getInterceptorService().unregisterAllInterceptors();
        List<String> testList = new ArrayList<>();
        testList.add("WRIGHT");
        List<Long> result = obj.processRuns(client,testList);
        Assert.assertEquals(result,null);
    }


}
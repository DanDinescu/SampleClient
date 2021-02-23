import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
/*
Class for capturing response request time
 */
@Interceptor
public class SampleClientResponseTimeInterceptor implements IClientInterceptor
{
    private long responseTime;
    public SampleClientResponseTimeInterceptor()
    {
    }

    public long getResponseTime() {
        return responseTime;
    }

    @Override
    public void interceptResponse(IHttpResponse theRequest) {
        responseTime = theRequest.getRequestStopWatch().getMillis();
    }

    @Override
    public void interceptRequest(IHttpRequest theRequest) {
    }
}

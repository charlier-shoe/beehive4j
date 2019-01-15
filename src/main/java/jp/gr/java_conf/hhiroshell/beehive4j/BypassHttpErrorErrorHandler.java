package jp.gr.java_conf.hhiroshell.beehive4j;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class BypassHttpErrorErrorHandler implements ResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse arg0) {
        // Error responses will be handled in BeehiveInvoker.
    }

    @Override
    public boolean hasError(ClientHttpResponse arg0) {
        // Error responses will be handled in BeehiveInvoker.
        return false;
    }

}

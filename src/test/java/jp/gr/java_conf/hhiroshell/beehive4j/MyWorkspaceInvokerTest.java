package jp.gr.java_conf.hhiroshell.beehive4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jp.gr.java_conf.hhiroshell.beehive4j.BeehiveApiDefinitions;
import jp.gr.java_conf.hhiroshell.beehive4j.BeehiveContext;
import jp.gr.java_conf.hhiroshell.beehive4j.BeehiveResponse;
import jp.gr.java_conf.hhiroshell.beehive4j.MyWorkspaceInvoker;
import jp.gr.java_conf.hhiroshell.beehive4j.exception.Beehive4jException;

public class MyWorkspaceInvokerTest {

    private BeehiveContext context = null;

    @Before
    public void setUp() throws Exception {
        context = TestUtils.setUpContext();
    }

    @Test
    public void test() {
        MyWorkspaceInvoker invoker = context.getInvoker(BeehiveApiDefinitions.TYPEDEF_MY_WORKSPACE);
        try {
            ResponseEntity<BeehiveResponse> response = invoker.invoke();
            assertEquals("Status code is expected to be 200 (OK).", HttpStatus.OK, response.getStatusCode());
            assertEquals("BeeType of the resopnse is expected to be \"personalWorkspace\"",
                    "personalWorkspace", response.getBody().getBeeType());
        } catch (Beehive4jException e) {
            fail(e.getMessage());
        }
    }

}

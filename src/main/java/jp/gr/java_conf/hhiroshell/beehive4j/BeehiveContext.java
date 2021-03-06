package jp.gr.java_conf.hhiroshell.beehive4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpCookie;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import jp.gr.java_conf.hhiroshell.beehive4j.exception.BeehiveApiFaultException;

/**
 * BeehiveContext represents session context with Beehive server.
 * You can get API invokers of Beehive REST interface.
 * 
 * This class is the entry point of API calls. An example of usage is following.
 * 
 * <code>
 *     BeehiveContext context = BeehiveContext.getBeehiveContext(
 *            new URL("https://beehive.example.com"), "user", "password");
 *     MyWorkspaceInvoker invoker =
 *            context.getInvoker(BeehiveApiDefinitions.TYPEDEF_MY_WORKSPACE);
 *     try {
 *         JsonNode json = invoker.invoke();
 *     } catch (BeeClientException e) {
 *         // error handling.
 *     } 
 * </code>
 * 
 * @author hhayakaw
 *
 */
public class BeehiveContext {

    /**
     * The context root of beehive REST API.
     */
    private static final String BEEHIVE_API_CONTEXT_ROOT = "comb/v1/d/";
    /**
     * The credential of authenticated user in this context.
     */
    private final BeehiveCredential credential;
    /**
     * The URL string of root of beehive REST API.<br>
     * 
     * e.g) "https://beehive.example.com/comb/v1/d/"
     */
    private final String api_root;

    // This object is not instanciable by using constructor directly.
    private BeehiveContext(String api_root, BeehiveCredential credential) {
        this.api_root = api_root;
        this.credential = credential;
    }

    /**
     * Get an object represents session context with Beehive.
     * You can get API invokers of Beehive REST interface.
     * 
     * This method calls the "session/login" REST API of Beehive using specified
     * user name and password.<br>
     * So if the REST API call is failed, because such as incorrect user/password
     * or connection failure, it throws BeeClietException.
     * 
     * @param host - URL object of destination host. e.g) "https://beehive.example.com/"
     * @param user - user name
     * @param password - password
     * @return BeehiveContext that represents session context with beehive.
     * @throws BeehiveApiFaultException - When it failed to call the "session/login" of Beehive REST API.
     */
    public static BeehiveContext getBeehiveContext(URL host, String user, String password)
            throws BeehiveApiFaultException {
        if (user == null || user.length() == 0 || password == null || password.length() == 0) {
            throw new IllegalArgumentException("User name or password is not specified.");
        }
        String basicAuthHeader = makeBasicAuthString(user, password);
        return getBeehiveContext(host, basicAuthHeader);
    }

    /**
     * Get an object represents session context with Beehive.
     * You can get API invokers of Beehive REST interface.
     * 
     * This method calls the "session/login" REST API of Beehive using specified
     * basic authentication http header value.<br>
     * So if the REST API call is failed, because such as incorrect header value
     * or connection failure, it throws BeeClietException.
     * 
     * @param host - URL object of destination host. e.g) "https://beehive.example.com/"
     * @param basicAuthHeader - Basic authentication http header value. e.g) "Basic ZxCvBnMaSdFgHjKl="
     * @return BeehiveContext that represents session context with beehive.
     * @throws BeehiveApiFaultException - When it failed to call the "session/login" of Beehive REST API.
     */
    public static BeehiveContext getBeehiveContext(
            URL host, String basicAuthHeader) throws BeehiveApiFaultException {
        if (host == null) {
            throw new IllegalArgumentException("Destination URL is not specified.");
        }
        if (basicAuthHeader == null) {
            throw new IllegalArgumentException("Basic auth header is not specified.");
        }
        String api_root = makeApiRootString(host);
        return new BeehiveContext(api_root, login(api_root, basicAuthHeader));
    }

    private static BeehiveCredential login(String api_root, String basicAuthHeader) throws BeehiveApiFaultException {
        BeehiveInvoker<Object> invoker = new SessionLoginInvoker(api_root, null);
        Map<String, String> header = new HashMap<>(1);
        header.put(HttpHeaders.AUTHORIZATION, basicAuthHeader);
        invoker.addHeader(header);
        ResponseEntity<BeehiveResponse> response = invoker.invoke();

        List<String> sessionHeader = response.getHeaders().get("Set-Cookie");
        if (sessionHeader == null || sessionHeader.size() == 0) {
            throw new IllegalStateException("Cookie is not set.");
        }
        HttpCookie jsessionid = parseCookie(sessionHeader, "JSESSIONID");
        if (jsessionid == null) {
            throw new IllegalStateException("JSESSIONID is not set.");
        }

        BeehiveResponse body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("Response body is null.");
        }
        String token = body.getJson().get("token").asText();
        return new BeehiveCredential(jsessionid, token);
    }

    private static String makeApiRootString(URL host) {
        StringBuffer buf = new StringBuffer()
                .append(host.getProtocol())
                .append("://")
                .append(host.getHost());
        int port = host.getPort();
        if (port >= 0) {
            buf.append(":").append(port);
        }
        buf.append("/").append(BEEHIVE_API_CONTEXT_ROOT);
        return buf.toString();
    }

    private static String makeBasicAuthString(String user, String password) {
        if (user.contains(":")) {
            throw new IllegalArgumentException("User name must not contain \":\".");
        }
        String src = user.trim() + ":" + password;
        byte[] encoded = Base64.getEncoder().encode(src.getBytes());
        return "Basic " + new String(encoded);
    }

    private static HttpCookie parseCookie(List<String> setCookieHeader, String cookieName) {
        String[] attributes = null;
        for (String elm : setCookieHeader) {
            if (elm.startsWith(cookieName + "=")) {
                attributes = elm.split(";");
                break;
            }
        }
        if (attributes == null || attributes.length == 0) {
            return null;
        }
        String[] keyValue = attributes[0].split("=");
        if (keyValue.length != 2) {
            throw new IllegalStateException("Invalid cookie is going to be set.");
        }
        HttpCookie cookie = new HttpCookie(
                keyValue[0].trim(), keyValue[1].trim());
        return cookie;
    }

    boolean isActive() {
        // TODO: implement
        return false;
    }

    /**
     * Get an invoker object of specified type.<br>
     * An invoker type corresponds to one Beehive REST API.
     * 
     * @param InvokerType - Class object of concrete invoker object.
     * @return An invoker.
     */
    public <T extends BeehiveInvoker<?>> T getInvoker(Class<T> InvokerType) {
        try {
            Class<?>[] argTypes = {String.class, BeehiveCredential.class};
            Constructor<T> constructor = InvokerType.getConstructor(argTypes);
            // api_root and credential must not be null.
            Object[] args = {this.api_root, this.credential};
            T invoker = constructor.newInstance(args);
            return invoker;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException(
                    "Failed to create a invoker instance using reflection.", e);
        }
    }

}
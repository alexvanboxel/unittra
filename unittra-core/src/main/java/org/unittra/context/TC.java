package org.unittra.context;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.unittra.catalog.Issue;

//import com.amazonaws.ClientConfiguration;
//import com.amazonaws.Protocol;
//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3Client;
//import com.amazonaws.services.s3.model.S3Object;

/**
 * The test context of the run. In a test run there is always one singleton instance over a complete test run.
 * 
 * @author alex.van_boxel@alcatel-lucent.com
 */
public abstract class TC {
    
    private static TC _instance;
    
//    public static void aws(String awsBucket, String awsId, String awsKey, String awsProxyHost, String awsProxyPort, String profile) throws Exception {
//        AWSCredentials creds = new BasicAWSCredentials(awsId, awsKey);
//        AmazonS3 s3client = null;
//        if (awsProxyHost != null && !awsProxyHost.equals("")) {
//            ClientConfiguration clientConfig = new ClientConfiguration();
//            clientConfig.setProtocol(Protocol.HTTP);
//            clientConfig.setProxyHost(awsProxyHost);
//            clientConfig.setProxyPort(Integer.valueOf(awsProxyPort));
//            s3client = new AmazonS3Client(creds, clientConfig);
//        } else {
//            s3client = new AmazonS3Client(creds);
//        }
//        S3Object s3object = s3client.getObject(awsBucket, "test-context/" + profile);
//        InputStream stream = s3object.getObjectContent();
//        _instance = loadContext(URI.create("http://" + awsBucket + ".aws.amazon.com/test-context/" + profile), stream);
//    }
    
    public static synchronized void profile(String profile, boolean forceNew) throws Exception {
        if(forceNew) {
            _instance = null;
        }
        if (_instance == null) {
            Preferences prefs = Preferences.userRoot().node("org/unittra/context");
            String contextFile = prefs.get("profiles." + profile, "");
            if (contextFile.length() == 0) {
                throw new RuntimeException("TestContext error: No prepared test context file found for profile " + profile);
            }
            _instance = loadContext(contextFile);
        }
    }
    
    /**
     * <p>
     * Get the singleton text context for this run.
     * </p>
     * <p>
     * To get a test context you should set one of the following system properties:
     * </p>
     * 
     * <ul>
     * <li><code>test.config</code>: A TC configuration file.</li>
     * <li><code>test.context</code>: A TC implementation class.</li>
     * </ul>
     * 
     * @return the singleton test context.
     * @throws ClassNotFoundException
     *             if the TC implementation class specified in <code>test.context</code> or specified in the factory section of the file specified in <code>test.config</code>.
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     * @throws XMLStreamException
     * @throws MalformedURLException
     * @throws
     */
    public static synchronized <T> T i(Class<T> clazz) throws Exception {
        if (_instance == null) {
            String factory = System.getProperty("test.factory");
            System.out.println("test.factory: "+factory);
            String context = System.getProperty("test.context");
            System.out.println("test.context: "+context);
            if (context == null) {
                // backwards compatibilty
                context = System.getProperty("test.config");
                System.out.println("backwards compatible test.context: "+context);
            }
            String profile = System.getProperty("test.profile");
            System.out.println("test.profile: "+profile);
            String aws = System.getProperty("test.aws");
            System.out.println("test.aws: "+aws);
            
            if (context != null) {
                _instance = loadContext(context);
            } else if (factory != null) {
                _instance = (TC) Class.forName(factory).newInstance();
            } else if (aws != null) {
//                Preferences prefs = Preferences.userRoot().node("org/unittra/context");
//                String awsId = getProperty(prefs, "aws.id", null);
//                String awsKey = getProperty(prefs, "aws.key", null);
//                String awsProxyHost = getProperty(prefs, "aws.proxy.host", null);
//                String awsProxyPort = getProperty(prefs, "aws.proxy.port", null);
//                String awsBucket = getProperty(prefs, "aws.bucket", null);
//                
//                AWSCredentials creds = new BasicAWSCredentials(awsId, awsKey);
//                AmazonS3 s3client = null;
//                if (awsProxyHost != null) {
//                    ClientConfiguration clientConfig = new ClientConfiguration();
//                    clientConfig.setProtocol(Protocol.HTTP);
//                    clientConfig.setProxyHost(awsProxyHost);
//                    clientConfig.setProxyPort(Integer.valueOf(awsProxyPort));
//                    s3client = new AmazonS3Client(creds, clientConfig);
//                } else {
//                    s3client = new AmazonS3Client(creds);
//                }
//                S3Object s3object = s3client.getObject(System.getProperty("aws.bucket"), "test-context/" + aws);
//                InputStream stream = s3object.getObjectContent();
//                _instance = loadContext(URI.create("http://" + awsBucket + ".aws.amazon.com/test-context/" + aws), stream);
            } else {
                Preferences prefs = Preferences.userRoot().node("org/unittra/context");
                if (profile == null) {
                    profile = prefs.get("profile.default", "");
                }
                if (profile.length() > 0) {
                    String contextFile = prefs.get("profiles." + profile, "");
                    if (contextFile.length() == 0) {
                        throw new RuntimeException("TestContext error: No prepared test context file found for profile " + profile);
                    }
                    _instance = loadContext(contextFile);
                }
            }
        }
        if(_instance == null) {
            throw new RuntimeException("TestContext error: instance.");
        }
        if ((clazz != TC.class) && (!clazz.isInstance(_instance))) {
            throw new RuntimeException("TestContext error: Expected a test context of class " + clazz + ", but was ("+_instance.getClass()+"). Is the test.context system property set correctly?");
        }
        return clazz.cast(_instance);
    }
    
    /**
     * DANGER!!! DANGER!!! DANGER!!! Swaps the instance with a new one. Only use it for testing the tests.
     */
    public static void swap(TC newInstance) {
        _instance = newInstance;
    }
    
    public static String getProperty(Preferences prefs, String key, String def) {
        String value = System.getProperty(key);
        if (value == null) {
            return prefs.get(key, def);
        }
        return value;
    }
    
    protected synchronized static TC loadContext(URI configURI, InputStream stream) throws Exception {
        TC instance = null;
        
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new StreamSource(stream));
        
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                if (QName.valueOf("platform").equals(reader.getName())) {
                    if ("java".equals(reader.getAttributeValue(null, "name"))) {
                        String factory = reader.getAttributeValue(null, "factory");
                        if (factory != null) {
                            Constructor constructor = Class.forName(factory).getConstructor(String.class);
                            instance = (TC) constructor.newInstance("@Singlton");
                        }
                    }
                }
            } else if (event == XMLStreamReader.END_ELEMENT) {
                if (QName.valueOf("platform-section").equals(reader.getName()))
                    break;
            }
        }
                
        if (instance == null) {
            throw new RuntimeException("No test context set in configuration file.");
        }
        
        instance.readConfigFile(configURI, reader);
        return instance;
    }
    
    protected synchronized static TC loadContext(String config) throws Exception {
        URI configURI = new File(config).toURI();
        return loadContext(configURI, configURI.toURL().openStream());
    }
    
    abstract void initDefault();
    
    /**
     * Reads the rest of the configuration file. This kicks in after the factory block has been read.
     * 
     * @param reader
     * @throws Exception
     */
    protected abstract void readConfigFile(URI configURI, XMLStreamReader reader) throws Exception;
    
    /**
     * Do post initialization work after the complete configuration file has been read in.
     * 
     * @throws Exception
     */
    protected abstract void postConfigFile() throws Exception;
    
    public boolean shouldRun(Description description) {
        System.out.println(description.toString());
        return false;
    }
    
    public abstract void addTestListeners(RunNotifier arg0) throws Exception;
    
    public abstract List<Issue> getIssues(Description description);
    
    public abstract Collection<Issue> getIssues();
    
    public abstract URI getResultLogger();
    
    public abstract URI getRunLogger();
    
    public abstract URI getFailLogger();
    
    public abstract void init();
    
    public abstract void destroy();
    
    public Map<String, PropertyDefinition> getPropertyMap() {
        return Collections.unmodifiableMap(new HashMap<String, PropertyDefinition>());
    }
    
    public abstract <T> T getModule(T className);
    
    /**
     * 
     * @param version
     * @return
     */
    public abstract boolean since(String version);
    
    public abstract boolean till(String version);
}

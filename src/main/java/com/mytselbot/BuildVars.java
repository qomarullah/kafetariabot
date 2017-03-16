package com.mytselbot;

/**
 * @author Ruben Bermudez
 * @version 1.0
 * @brief Custom build vars FILL EVERYTHING CORRECTLY
 * @date 20 of June of 2015
 */

public class BuildVars {
    public static final Boolean debug = true;
    public static final Boolean useWebHook = false;
    public static final int PORT = 8443;
    public static final String EXTERNALWEBHOOKURL = "https://example.changeme.com:" + PORT; // https://(xyz.)externaldomain.tld
    public static final String INTERNALWEBHOOKURL = "https://localhost.changeme.com:" + PORT; // https://(xyz.)localip/domain(.tld)
    public static final String pathToCertificatePublicKey = "./YOURPEM.pem"; //only for self-signed webhooks
    public static final String pathToCertificateStore = "./YOURSTORE.jks"; //self-signed and non-self-signed.
    public static final String certificateStorePassword = "yourpass"; //password for your certificate-store

    public static final String OPENWEATHERAPIKEY = "b7fafd6b06dfb06c560eedfdb074a024";

    public static final String DirectionsApiKey = "<your-api-key>";

    public static final String TRANSIFEXUSER = "<transifex-user>";
    public static final String TRANSIFEXPASSWORD = "<transifex-password>";

    
    //public static final String pathToLogs = "./";
    //public static final String pathToLogs = "D:/telegram/";
    public static final String pathToData = "D:/telegram/";
    
    public static final String pathToLogs = "/apps/kafetariabot/log/";
    //public static final String pathToData = "/apps/kafetariabot/menu/";
    
    public static final String urlMenu = "http://localhost/sample/share.php";
    public static final String urlConfirm = "http://localhost/sample/conf.php";
    public static final String urlTrx = "http://localhost/sample/trx.php";
    public static final String urlReport = "http://localhost/sample/performance.php";
    

    //for long pooling enough
    public static final String linkDB = "jdbc:mysql://localhost:3306/kafetaria?useUnicode=true&characterEncoding=UTF-8";
    public static final String controllerDB = "com.mysql.jdbc.Driver";
    public static final String userDB = "apps";
    public static final String password = "aplikasi";
}

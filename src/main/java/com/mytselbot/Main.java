package com.mytselbot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.mytselbot.BuildVars;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;

import com.mytselbot.handler.InlineBot;
import com.mytselbot.handler.KafetariaBot;
import com.mytselbot.handler.MyKafetariaBot;
/**
 * Hello world!
 *
 */
import com.mytselbot.BotLogger;

public class Main 
{
	public static String LOGTAG="Main";
	public static Properties prop = new Properties();
	static InputStream input = null;
	
	
    public static void main(String[] args )
    {
        //System.out.println( "Hello World!" );
        
        //System.setProperty("log4j.configurationFile","config/log4j2.xml");
    	System.setProperty("log4j.configurationFile",args[1]);
        
        
        try {
			input = new FileInputStream(args[0]);
			// load a properties file
			prop.load(input);
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
        
        String test="OK";
        BotLogger.info("info! {}", test);
        BotLogger.debug("debug! {}", test);
        
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        
        try {
            telegramBotsApi.registerBot(new KafetariaBot());
           
            //telegramBotsApi.registerBot(new MyKafetariaBot());
            
        	//telegramBotsApi.registerBot(new InlineBot());
            
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
    
/*
    private static TelegramBotsApi createTelegramBotsApi() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi;
        if (!BuildVars.useWebHook) {
            // Default (long polling only)
            telegramBotsApi = createLongPollingTelegramBotsApi();
        } else if (!BuildVars.pathToCertificatePublicKey.isEmpty()) {
            //Filled a path to a pem file ? looks like you're going for the self signed option then, invoke with store and pem file to supply.
            telegramBotsApi = createSelfSignedTelegramBotsApi();
           //telegramBotsApi.registerBot(new WebHookExampleHandlers());
        } else {
            //Non self signed, make sure you've added private/public and if needed intermediate to your cert-store.
            telegramBotsApi = createNoSelfSignedTelegramBotsApi();
            //telegramBotsApi.registerBot(new WebHookExampleHandlers());
        }
        return telegramBotsApi;
    }

    *//**
     * @brief Creates a Telegram Bots Api to use Long Polling (getUpdates) bots.
     * @return TelegramBotsApi to register the bots.
     *//*
    private static TelegramBotsApi createLongPollingTelegramBotsApi() {
        return new TelegramBotsApi();
    }

    *//**
     * @brief Creates a Telegram Bots Api to use Long Polling bots and webhooks bots with self-signed certificates.
     * @return TelegramBotsApi to register the bots.
     *
     * @note https://core.telegram.org/bots/self-signed#java-keystore for generating a keypair in store and exporting the pem.
    *  @note Don't forget to split the pem bundle (begin/end), use only the public key as input!
     *//*
    private static TelegramBotsApi createSelfSignedTelegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(BuildVars.pathToCertificateStore, BuildVars.certificateStorePassword, BuildVars.EXTERNALWEBHOOKURL, BuildVars.INTERNALWEBHOOKURL, BuildVars.pathToCertificatePublicKey);
    }

    *//**
     * @brief Creates a Telegram Bots Api to use Long Polling bots and webhooks bots with no-self-signed certificates.
     * @return TelegramBotsApi to register the bots.
     *
     * @note Coming from a set of pem files here's one way to do it:
     * @code{.sh}
     * openssl pkcs12 -export -in public.pem -inkey private.pem > keypair.p12
     * keytool -importkeystore -srckeystore keypair.p12 -destkeystore server.jks -srcstoretype pkcs12
     * #have (an) intermediate(s) to supply? first:
     * cat public.pem intermediate.pem > set.pem (use set.pem as -in)
     * @endcode
     *//*
    private static TelegramBotsApi createNoSelfSignedTelegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(BuildVars.pathToCertificateStore, BuildVars.certificateStorePassword, BuildVars.EXTERNALWEBHOOKURL, BuildVars.INTERNALWEBHOOKURL);
    }*/
}

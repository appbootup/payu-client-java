package ro.payu.example;

import org.apache.http.NameValuePair;
import ro.payu.example.alu.AluRequestParametersBuilder;
import ro.payu.example.alu.AluResponseInterpreter;
import ro.payu.example.idn.IdnRequestParametersBuilder;
import ro.payu.example.idn.IdnResponseInterpreter;
import ro.payu.example.ipn.IpnHttpServerBuilder;
import ro.payu.example.ipn.IpnRequestProcessor;
import ro.payu.lib.alu.AluAuthenticationService;
import ro.payu.lib.alu.AluClient;
import ro.payu.lib.alu.AluResponseParser;
import ro.payu.lib.common.authentication.AuthenticationService;
import ro.payu.lib.common.authentication.InvalidSignatureException;
import ro.payu.lib.common.authentication.SignatureCalculator;
import ro.payu.lib.common.client.*;
import ro.payu.lib.common.server.DefaultHttpServer;
import ro.payu.lib.idn.IdnAuthenticationService;
import ro.payu.lib.idn.IdnClient;
import ro.payu.lib.idn.IdnResponseParser;

import java.util.List;
import java.util.concurrent.Semaphore;

public class ClientUsageExample {

    private static final String SERVER_SCHEMA = "http";
    private static final int SERVER_PORT = 80;
    private static final String SERVER_HOST = "tr.payu.local";

    private static final String MERCHANT_CODE = "PAYU_2";
    private static final String MERCHANT_SECRET_KEY = "SECRET_KEY";

    private static AluClient aluClient;
    private static IdnClient idnClient;

    private static AluRequestParametersBuilder aluRequestParametersBuilder;
    private static AluResponseInterpreter aluResponseInterpreter;

    private static IdnRequestParametersBuilder idnRequestParametersBuilder;
    private static IdnResponseInterpreter idnResponseInterpreter;

    private static DefaultHttpServer ipnHttpServer;
    private static Semaphore semaphore;
    private static IpnRequestProcessor ipnRequestProcessor;

    public static void main(String[] args) {

        setUp();

        try {
            semaphore.acquire();
            callAlu();

            semaphore.acquire();
            final List<NameValuePair> ipnRequestParameters = getIpnRequestParameters();
            callIdn(ipnRequestParameters);

            semaphore.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ipnHttpServer.stop();
        }
    }

    private static void callAlu() throws CommunicationException, InvalidXmlResponseParsingException, InvalidSignatureException {

        final List<NameValuePair> aluRequestParameters = aluRequestParametersBuilder.buildRequestParameters();

        final List<NameValuePair> aluResponseParameters = aluClient.call(aluRequestParameters);

        aluResponseInterpreter.interpretResponseParameters(aluResponseParameters);
        if (!aluResponseInterpreter.isSuccess(aluResponseParameters)) {
            throw new RuntimeException("ALU response ERROR!");
        }
    }

    private static List<NameValuePair> getIpnRequestParameters() {

        return ipnRequestProcessor.getRequestParameters();
    }

    private static void callIdn(List<NameValuePair> ipnRequestParameters) throws CommunicationException, InvalidXmlResponseParsingException, InvalidSignatureException {

        final List<NameValuePair> idnRequestParameters = idnRequestParametersBuilder.build(ipnRequestParameters);

        final List<NameValuePair> idnResponseParameters = idnClient.call(idnRequestParameters);

        idnResponseInterpreter.interpretResponseParameters(idnResponseParameters);
        if (!idnResponseInterpreter.isSuccess(idnResponseParameters)) {
            throw new RuntimeException("IDN response ERROR!");
        }
    }

    private static void setUp() {

        final ApiHttpClient apiHttpClient = new ApiHttpClient(SERVER_HOST, SERVER_PORT, SERVER_SCHEMA);
        final AuthenticationService authenticationService = new AuthenticationService(
                new SignatureCalculator(),
                MERCHANT_SECRET_KEY
        );
        final XmlResponseParser xmlResponseParser = new XmlResponseParser();
        aluClient = new AluClient(new ApiClient(
                apiHttpClient,
                new AluAuthenticationService(authenticationService),
                new AluResponseParser(xmlResponseParser)
        ));

        idnClient = new IdnClient(new ApiClient(
                apiHttpClient,
                new IdnAuthenticationService(authenticationService),
                new IdnResponseParser(xmlResponseParser)
        ));

        aluRequestParametersBuilder = new AluRequestParametersBuilder(MERCHANT_CODE);
        aluResponseInterpreter = new AluResponseInterpreter();

        idnRequestParametersBuilder = new IdnRequestParametersBuilder(MERCHANT_CODE);
        idnResponseInterpreter = new IdnResponseInterpreter();

        semaphore = new Semaphore(1);
        ipnRequestProcessor = new IpnRequestProcessor(semaphore);
        ipnHttpServer = IpnHttpServerBuilder.createServer(ipnRequestProcessor);
        ipnHttpServer.start();
    }

}

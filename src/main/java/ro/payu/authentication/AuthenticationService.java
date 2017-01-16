package ro.payu.authentication;

import org.apache.http.NameValuePair;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.stream.Collectors;

public final class AuthenticationService {

    public final boolean validateSignature(List<NameValuePair> parameters, String secretKey, String signature) {
        return computeSignature(parameters, secretKey).equals(signature.toLowerCase());
    }

    public final String computeSignature(List<NameValuePair> parameters, String secretKey) {

        final String stringToHash = parameters.stream()
                .sorted(Comparator.comparing(NameValuePair::getName))
                .map(nameValuePair -> String.valueOf(nameValuePair.getValue().length()) + nameValuePair.getValue())
                .collect(Collectors.joining(""));

        final SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacMD5");
        final Mac hMacMD5;

        try {
            hMacMD5 = Mac.getInstance("HmacMD5");
            hMacMD5.init(secretKeySpec);
        } catch (NoSuchAlgorithmException|InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        byte[] signatureBytes = hMacMD5.doFinal(stringToHash.getBytes(StandardCharsets.UTF_8));

        Formatter formatter = new Formatter();
        for (byte signatureByte : signatureBytes) {
            formatter.format("%02x", signatureByte);
        }

        return formatter.toString();
    }
}

package ro.payu.example.alu;

import org.apache.http.NameValuePair;

import java.util.List;

public class AluResponseInterpreter {

    public boolean isSuccess(List<NameValuePair> responseParameters) {
        for (NameValuePair pair : responseParameters) {
            if (pair.getName().equals("STATUS") && pair.getValue().equals("SUCCESS")) {
                return true;
            }
        }
        return false;
    }

    public void interpretResponseParameters(List<NameValuePair> responseParameters) {
        System.out.println();
        System.out.println("ALU response:");
        System.out.println(responseParameters);
    }
}

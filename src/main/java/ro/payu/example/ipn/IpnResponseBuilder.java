package ro.payu.example.ipn;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ro.payu.example.ipn.server.ResponseBuilder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;

public class IpnResponseBuilder implements ResponseBuilder {

    @Override
    public List<NameValuePair> getHeaders() {

        List<NameValuePair> headers = new ArrayList<>();
        headers.add(new BasicNameValuePair("Content-Type", "text/xml"));

        return headers;
    }

    @Override
    public String getBody(List<NameValuePair> requestParameters) {
        final DocumentBuilder builder;

        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        Document document = builder.newDocument();

        Element rootElement = document.createElement("EPAYMENT");
        document.appendChild(rootElement);

        // TODO
        Text textElement = document.createTextNode("DATE|HASH");
        rootElement.appendChild(textElement);

        return document.toString();
    }
}

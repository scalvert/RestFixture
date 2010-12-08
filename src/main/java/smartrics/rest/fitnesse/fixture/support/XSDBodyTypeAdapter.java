package smartrics.rest.fitnesse.fixture.support;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XSDBodyTypeAdapter extends BodyTypeAdapter {
	private static Log LOG = LogFactory.getLog(XPathBodyTypeAdapter.class);

	@Override
	public boolean equals(Object expected, Object actual) {
		if (checkNoBody(expected)) {
			return checkNoBody(actual);
		}
		if (checkNoBody(actual)) {
			return checkNoBody(expected);
		}

		String xsdFilePath = "C:\\FitNesseRoot\\files\\schemas\\"
				+ (String) expected;

		if (!fileExists(xsdFilePath)) {
			addError("The XSD file does not exist");
			return false;
		}

		boolean b = eval(xsdFilePath, actual.toString());

		if (!b) {
			addError("returned XML does not match the schema: '" + xsdFilePath
					+ "'");
		}

		return getErrors().size() == 0;
	}

	private boolean fileExists(String path) {
		File xsd = new File(path);

		return xsd.exists();
	}

	private boolean eval(String xsdFilePath, String content) {
		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		try {
			Schema schema = factory.newSchema(new File(xsdFilePath));
			Validator validator = schema.newValidator();
			DocumentBuilder parser = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document document = parser.parse(new ByteArrayInputStream(content
					.getBytes()));
			validator.validate(new DOMSource(document));
		} catch (ParserConfigurationException e) {
			addError(e.getMessage());
			return false;
		} catch (SAXException e) {
			addError(e.getMessage());
			return false;
		} catch (IOException e) {
			addError(e.getMessage());
			return false;
		}

		return true;
	}

	@Override
	public Object parse(String xsdFilePath) throws Exception {
		if (xsdFilePath == null) {
			return "";
		}
		String expStr = xsdFilePath.trim();
		if ("no-body".equals(expStr.trim()) || "".equals(expStr.trim())) {
			return "";
		}
		return Tools.fromHtml(expStr);
	}
}

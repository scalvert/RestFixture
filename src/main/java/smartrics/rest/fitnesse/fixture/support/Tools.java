/*  Copyright 2008 Fabrizio Cannizzo
 *
 *  This file is part of RestFixture.
 *
 *  RestFixture (http://code.google.com/p/rest-fixture/) is free software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *
 *  RestFixture is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with RestFixture.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  If you want to contact the author please leave a comment here
 *  http://smartrics.blogspot.com/2008/08/get-fitnesse-with-some-rest.html
 */
package smartrics.rest.fitnesse.fixture.support;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.copy.HierarchicalStreamCopier;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

public final class Tools {

	private Tools() {

	}

	public static NodeList extractXPath(String xpathExpression, String content) {
		// Use the java Xpath API to return a NodeList to the caller so they
		// can iterate through
		return (NodeList) extractXPath(xpathExpression, content,
				XPathConstants.NODESET);
	}

	private static XPathExpression toExpression(String xpathExpression) {
		try {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			XPathExpression expr = xpath.compile(xpathExpression);
			return expr;
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(
					"xPath expression can not be compiled: " + xpathExpression,
					e);
		}
	}

	private static Document toDocument(String content) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(getInputStreamFromString(content));
			return doc;
		} catch (ParserConfigurationException e) {
			throw new IllegalArgumentException(
					"parser for last response body caused an error", e);
		} catch (SAXException e) {
			throw new IllegalArgumentException(
					"last response body cannot be parsed", e);
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"IO Exception when reading the document", e);
		}
	}

	public static String fromJSONtoXML(String json) throws IOException {
		HierarchicalStreamDriver driver = new JettisonMappedXmlDriver();
		StringReader reader = new StringReader(json);
		HierarchicalStreamReader hsr = driver.createReader(reader);
		StringWriter writer = new StringWriter();
		new HierarchicalStreamCopier().copy(hsr, new PrettyPrintWriter(writer));
		writer.close();
		return writer.toString();
	}

	/**
	 * extract the XPath from the content. the return value type is passed in
	 * input using one of the {@link XPathConstants}. See also
	 * {@link XPathExpression#evaluate(Object item, QName returnType)} ;
	 */
	public static Object extractXPath(String xpathExpression, String content,
			QName returnType) {
		Document doc = toDocument(content);
		XPathExpression expr = toExpression(xpathExpression);
		try {
			Object o = expr.evaluate(doc, returnType);
			return o;
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(
					"xPath expression can not be executed: " + xpathExpression);
		}
	}

	public static String getStringFromInputStream(InputStream is) {
		String line = null;
		if (is == null)
			return "";
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		try {
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Unable to read from stream", e);
		}
		return sb.toString();
	}

	public static InputStream getInputStreamFromString(String string) {
		if (string == null)
			throw new IllegalArgumentException("null input");
		byte[] byteArray = string.getBytes();
		return new ByteArrayInputStream(byteArray);
	}

	public static String convertMapToString(Map<String, String> map,
			String nvSep, String entrySep) {
		StringBuffer sb = new StringBuffer();
		if (map != null) {
			for (Entry<String, String> entry : map.entrySet()) {
				String el = entry.getKey();
				sb.append(convertEntryToString(el, map.get(el), nvSep)).append(
						entrySep);
			}
		}
		String repr = sb.toString();
		int pos = repr.lastIndexOf(entrySep);
		return repr.substring(0, pos);
	}

	public static String convertEntryToString(String name, String value,
			String nvSep) {
		return String.format("%s%s%s", name, nvSep, value);
	}

	public static boolean regex(String text, String expr) {
		try {
			Pattern p = Pattern.compile(expr);
			boolean find = p.matcher(text).find();
			return find;
		} catch (PatternSyntaxException e) {
			throw new IllegalArgumentException("Invalid regex " + expr);
		}
	}

	public static Map<String, String> convertStringToMap(final String expStr,
			final String nvSep, final String entrySep) {
		String[] nvpArray = expStr.split(entrySep);
		Map<String, String> ret = new HashMap<String, String>();
		for (String nvp : nvpArray) {
			try {
				int pos = nvp.indexOf(nvSep);
				String v = "";
				String k = nvp;
				if (pos != -1) {
					int pos2 = pos + nvSep.length();
					v = nvp.substring(pos2).trim();
					k = nvp.substring(0, pos).trim();
				}
				ret.put(k, v);
			} catch (RuntimeException e) {
				throw new IllegalArgumentException(
						"Each entry in the must be separated by '"
								+ entrySep
								+ "' and each entry must be expressed as a name"
								+ nvSep + "value");
			}
		}
		return ret;
	}

	public static String toHtml(String text) {
		// if(text.trim().startsWith("<"))
		// return "<textarea cols='180' rows='10' readonly='true'>" +
		// prettyPrint(text).replaceAll("&gt;", "&gt;\n") + "</textarea>";
		return text.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;").replaceAll(
						System.getProperty("line.separator"), "<br/>")
				.replaceAll(" ", "&nbsp;");
	}

	public static String toJSON(String text) {
		return text.trim();
	}

	public static String fromHtml(String text) {
		String ls = System.getProperty("line.separator");
		return text.replaceAll("<br[\\s]*/>", ls).replaceAll("<BR[\\s]*/>", ls)
				.replaceAll("<pre>", "").replaceAll("</pre>", "").replaceAll(
						"&nbsp;", " ").replaceAll("&gt;", ">").replaceAll(
						"&lt;", "<").replaceAll("&nbsp;", " ");
	}

}

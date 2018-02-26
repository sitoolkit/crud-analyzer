package org.sitoolkit.util.crudanalyzer.domain.crud;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.sitoolkit.util.crudanalyzer.infra.config.Config;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyBatisMapperReader {

	private static final String REPLACED_PLACE_HOLDER = "replaced";
	private static final String REPLACED_PARAM = "'" + REPLACED_PLACE_HOLDER + "'";
	private static final Pattern COUNT_OVER = Pattern.compile("(COUNT.*)OVER.*?\\)");
	private static final Pattern OFFSET = Pattern.compile("OFFSET.*?ONLY", Pattern.DOTALL);
	private static final Pattern PARAM_NUMERIC = Pattern.compile("#\\{.*NUMERIC?\\\\}");
	private static final Pattern PARAM = Pattern.compile("#\\{.*?\\}");
	private static final Pattern PLACE_HOLDER = Pattern.compile("\\$\\{.*?\\}");
	private static final Pattern MULTIPLE_REPLACE = Pattern.compile("replaced[,\\s]replaced", Pattern.DOTALL);
	
	private DocumentBuilderFactory dbf;
	private Map<String, String> replaceMap;

	public static void main(String[] args) throws Exception {
		MyBatisMapperReader reader = new MyBatisMapperReader();
		reader.init();

		List<RepositoryFunction> list = reader.read(Paths.get(args[0]));

		list.stream().forEach(System.out::println);
	}

	public void init() {
		dbf = DocumentBuilderFactory.newInstance();
		replaceMap = Config.getInstance().getReplaceMap();
	}

	public List<RepositoryFunction> read(Path filePath) {
		List<RepositoryFunction> list = new ArrayList<>();

		try {
			DocumentBuilder builder = dbf.newDocumentBuilder();
			Document document = builder.parse(filePath.toFile());

			Map<String, String> preDefinedSqlMap = readDocument(document, "sql", Collections.emptyMap()).stream()
					.collect(Collectors.toMap(RepositoryFunction::getFunction, RepositoryFunction::getSqlText));
			list.addAll(readDocument(document, "insert", preDefinedSqlMap));
			list.addAll(readDocument(document, "select", preDefinedSqlMap));
			list.addAll(readDocument(document, "update", preDefinedSqlMap));
			list.addAll(readDocument(document, "delete", preDefinedSqlMap));

			list.stream()
					.forEach(function -> function
							.setFunction(StringUtils.substringBefore(filePath.getFileName().toString(), ".xml") + "."
									+ function.getFunction()));

		} catch (Exception e) {
			log.warn("warn", e);
		}

		return list;
	}

	List<RepositoryFunction> readDocument(Document document, String tag, Map<String, String> preDefinedSqlMap) {
		List<RepositoryFunction> list = new ArrayList<>();

		NodeList statementNodeList = document.getElementsByTagName(tag);

		for (int i = 0; i < statementNodeList.getLength(); i++) {
			list.add(readStatementNode(statementNodeList.item(i), preDefinedSqlMap));
		}

		return list;
	}

	RepositoryFunction readStatementNode(Node statementNode, Map<String, String> preDefinedSqlMap) {
		RepositoryFunction function = new RepositoryFunction();

		function.setFunction(statementNode.getAttributes().getNamedItem("id").getNodeValue());
		Node statementTypeNode = statementNode.getAttributes().getNamedItem("statementType");

		if (statementTypeNode != null && "callable".equalsIgnoreCase(statementTypeNode.getNodeValue())) {
			return function;
		}

		NodeList statementChildren = statementNode.getChildNodes();

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < statementChildren.getLength(); i++) {
			Node statementChild = statementChildren.item(i);
			if (!checkIfElse(statementChild)) {
				continue;
			}
			sb.append(readBodyNode(statementChild, preDefinedSqlMap));
		}

		function.setSqlText(removeUnsuportedFunction(sb.toString()));
		return function;
	}

	String removeUnsuportedFunction(String sql) {
		sql = removeUnpivot(sql);
		sql = removeFirstValue(sql);
		sql = removeOffset(sql);
		sql = removeMultipleReplaced(sql);
		
		return sql;
	}
	

	String removeCountOrver(String sql) {
		Matcher m = COUNT_OVER.matcher(sql);
		if (!m.find()) {
			return sql;
		}
		return m.replaceAll(m.group(1));
	}

	String removeFirstValue(String sql) {
		if (!sql.contains("FIRST_VALUE(")) {
			return sql;
		}
		return sql.replaceAll("FIRST_VALUE\\(.*?\\) AS", "");
	}

	String removeUnpivot(String sql) {
		int index = sql.indexOf("UNPIVOT");
		
		if (index < 0 ) {
			return sql;
		}
		
		int count = 0;
		int lastIndex = 0;
		int start = sql.indexOf("(", index);
		for (int i = start; i < sql.length(); i++) {
			char c = sql.charAt(i);

			if (c == '(') {
				count++;
				continue;
			} else if (c == ')') {
				count--;
				continue;
			}

			if (count == 0) {
				lastIndex = i;
				break;
			}
		}

		sql = sql.substring(0, index) + sql.substring(lastIndex, sql.length());
		log.debug("unpivot removed:{}", sql);
		return removeUnpivot(sql);
	}
	
	
	String removeOffset(String sql) {
		return OFFSET.matcher(sql).replaceAll("");
	}

	String removeMultipleReplaced(String sql) {
		return MULTIPLE_REPLACE.matcher(sql).replaceAll(REPLACED_PLACE_HOLDER);
	}

	String readBodyNode(Node node, Map<String, String> preDefinedSqlMap) {

		if (node.getChildNodes().getLength() == 0) {
			return readLeafNode(node, preDefinedSqlMap);
		}

		StringBuilder sb = new StringBuilder();

		String suffix = "";
		String remove = null;

		switch (node.getNodeName()) {
		case "foreach":
			Node openAttribute = node.getAttributes().getNamedItem("open");
			if (openAttribute != null) {
				sb.append(openAttribute.getNodeValue());
			}

			Node closeAttribute = node.getAttributes().getNamedItem("close");

			if (closeAttribute != null) {
				suffix = closeAttribute.getNodeValue();
			}
			break;
		case "set":
			sb.append("set");
			remove = ",";
			break;

		case "where":
			sb.append("where");
			break;

		case "trim":
			sb.append(node.getAttributes().getNamedItem("prefix").getNodeValue());
			suffix = node.getAttributes().getNamedItem("suffix").getNodeValue();
			Node so = node.getAttributes().getNamedItem("suffixOverrides");
			if (so != null) {
				remove = so.getNodeValue();
			}
			break;

		case "otherwise":
			return "";

		}

		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			Node childNode = node.getChildNodes().item(i);

			if (!checkIfElse(childNode)) {
				continue;
			}

			sb.append(readBodyNode(childNode, preDefinedSqlMap));
		}

		String str = sb.toString();
		if (remove != null) {
			int lastIndex = str.lastIndexOf(remove);
			str = str.substring(0, lastIndex);
		}

		return str + suffix;
	}

	boolean checkIfElse(Node node) {
		if (!"if".equals(node.getNodeName())) {
			return true;
		}

		Node prevNode = node.getPreviousSibling();

		if (prevNode == null) {
			return true;
		}

		if ("#text".equals(prevNode.getNodeName())) {
			prevNode = prevNode.getPreviousSibling();

			if (prevNode == null) {
				return true;
			}
		}

		if ("if".equals(prevNode.getNodeName())) {
			String condition = node.getAttributes().getNamedItem("test").getNodeValue();
			String prevCondition = prevNode.getAttributes().getNamedItem("test").getNodeValue();

			String leftSide = StringUtils.substringBefore(condition, " ");

			if (prevCondition.startsWith(leftSide)) {
				return false;
			}
		}

		return true;
	}

	String readLeafNode(Node node, Map<String, String> preDefinedSqlMap) {
		if ("#comment".equals(node.getNodeName())) {
			return "";
		} else if ("include".equals(node.getNodeName())) {

			return preDefinedSqlMap.getOrDefault(node.getAttributes().getNamedItem("refid").getNodeValue(), "");
			
		}

		String text = node.getTextContent();
		if (StringUtils.isWhitespace(text)) {
			return "";
		} else {
			text = PARAM_NUMERIC.matcher(text).replaceAll("1");
			text = PARAM.matcher(text).replaceAll(REPLACED_PARAM);
			text = PLACE_HOLDER.matcher(text).replaceAll(REPLACED_PLACE_HOLDER);
//			text = text.replaceAll("#\\{.*NUMERIC?\\}", "1");
//			text = text.replaceAll("#\\{.*?\\}", "'replaced'");
//			text = text.replaceAll("\\$\\{.*?\\}", "replaced");

			text = removeCountOrver(text);
			
			for(Entry<String, String> replace : replaceMap.entrySet()) {
				text = text.replace(replace.getKey(), replace.getValue());
			}
			
			return text;
		}
	}

}

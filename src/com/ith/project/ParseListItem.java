package com.ith.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import android.content.Context;

public class ParseListItem {

	static final String itemsName = "items.xml";
	static final String gridItemsName = "griditems.xml";

	private DocumentBuilderFactory dFactory;
	private DocumentBuilder dBuilder;
	private Document document;
	private NodeList nodeList;
	private int NO_OF_ITEMS;
	private InputStream stream = null;
	private Context context;
	private ArrayList<ItemDetails> itemDetails;
	private ArrayList<GridItemDetails> gridItemDetails;

	public ParseListItem(Context context, String itemType) {
		this.context = context;
		if (itemType.equals("LIST_ITEM"))
			this.itemDetails = startListParsing();
		else if (itemType.equals("GRID_ITEM"))
			this.gridItemDetails = startGridParsing();

	}

	/********************************************************************************************
	 * Returns the ItemDetails to the caller
	 * *************************************************************************************/
	public ArrayList<ItemDetails> getItemDetails() {
		return this.itemDetails;
	}

	/********************************************************************************************
	 * Returns the GridItemDetails to the caller
	 * *************************************************************************************/
	public ArrayList<GridItemDetails> getGridItemDetails() {
		return this.gridItemDetails;
	}

	/*****************************************************************************************
	 * Start parsing items.xml file
	 * *****************************************************************************************/
	private ArrayList<ItemDetails> startListParsing() {
		try {
			stream = context.getAssets().open(itemsName);

			dFactory = DocumentBuilderFactory.newInstance();

			dBuilder = dFactory.newDocumentBuilder();
			document = dBuilder.parse(stream);
			document.getDocumentElement().normalize();

			nodeList = document.getElementsByTagName("item");
			NO_OF_ITEMS = nodeList.getLength();

			ArrayList<ItemDetails> itemDets = new ArrayList<ItemDetails>();
			String id, desc, dateTime, isNewStr;

			for (int i = 0; i < NO_OF_ITEMS; i++) {
				Node node = (Node) nodeList.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;

					id = element.getElementsByTagName("itemId").item(0)
							.getTextContent();
					desc = element.getElementsByTagName("itemDescription")
							.item(0).getTextContent();
					dateTime = element.getElementsByTagName("dateTime").item(0)
							.getTextContent();
					isNewStr = element.getElementsByTagName("isNew").item(0)
							.getTextContent();

					itemDets.add(new ItemDetails(id, desc, dateTime, isNewStr));
				}
			}
			return itemDets;

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*****************************************************************************************
	 * Start parsing griditems.xml file
	 * *****************************************************************************************/
	private ArrayList<GridItemDetails> startGridParsing() {
		try {
			stream = context.getAssets().open(gridItemsName);

			dFactory = DocumentBuilderFactory.newInstance();

			dBuilder = dFactory.newDocumentBuilder();
			document = dBuilder.parse(stream);
			document.getDocumentElement().normalize();

			nodeList = document.getElementsByTagName("menuItem");
			NO_OF_ITEMS = nodeList.getLength();

			ArrayList<GridItemDetails> itemDets = new ArrayList<GridItemDetails>();
			String menuId, menuName, menuIcon;

			for (int i = 0; i < NO_OF_ITEMS; i++) {
				Node node = (Node) nodeList.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;

					menuId = element.getElementsByTagName("menuId").item(0)
							.getTextContent();
					menuName = element.getElementsByTagName("menuName").item(0)
							.getTextContent();
					menuIcon = element.getElementsByTagName("menuIcon").item(0)
							.getTextContent();

					itemDets.add(new GridItemDetails(menuId, menuName, menuIcon));
				}
			}
			return itemDets;

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

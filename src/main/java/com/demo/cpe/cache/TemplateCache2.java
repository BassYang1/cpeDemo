package com.demo.cpe.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.cmiot.acs.model.Fault;
import com.cmiot.acs.model.struct.FaultStruct;
import com.cmiot.acs.model.struct.ParameterInfoStruct;
import com.demo.cpe.code.BaseUtil;
import com.demo.cpe.code.DecodeSoapXml;
import com.demo.cpe.server.NettyHttpClient;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Created by ZJL on 2016/11/9.
 */
public class TemplateCache2 extends BaseUtil {

	public static ConcurrentMap<String, Document> cpeMap = new ConcurrentHashMap<String, Document>();

	public static void load(String fileLocation, String sn) throws Exception {
		String path = getFilePath(fileLocation);
		// 创建saxReader对象
		SAXReader reader = new SAXReader();
		// 通过read方法读取一个文件 转换成Document对象
		Document document = reader.read(new File(path));
		// 获取根节点元素对象
		Element root = document.getRootElement();
		cpeMap.put(sn, document);
	}

	/**
	 * 查询对应的节点
	 * 
	 * @param node
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Element> getElementListByName(Document node, String name) {
		String query = "";
		String[] names = name.split("\\.");
		for (int i = 0; i < names.length; i++) {
			if (i == 0) {
				query += names[i];
			} else {
				if (names[i].matches("[0-9]+")) {
					query += "[@instance=" + names[i] + "]";
				} else {
					query += "/" + names[i];
				}
			}
		}
		List<Element> list = node.selectNodes(query);
		return list;
	}

	/**
	 * 查询节点下面的节点名称
	 * 
	 * @param node
	 * @param bl
	 * @param bl
	 * @return
	 */
	public static List<ParameterInfoStruct> getPathByName(Document node, String name, boolean bl) {
		List<ParameterInfoStruct> parameterList = new ArrayList<ParameterInfoStruct>();
		List<String> nameList = new ArrayList<String>();
		List<Element> list = getElementListByName(node, name);
		if (list.size() > 0) {
			if (bl) {// 只取下一层节点名称
			    nameList = getNodeNameOne(list, name);
			} else {// 查询所有子节点名称
			    for (Element e : list) {
				    String arrName = e.attributeValue("instance");
	                String newName = "";
	                if (arrName != null) {
	                    newName = name + arrName;
	                }else {
	                    newName = name;
	                }
					getNodeName(e, parameterList, newName);
			    }
			}
		} else {
			Fault fault = new Fault(new FaultStruct(9005, "没有这个节点"), "9005", "没有这个节点");
			StringBuilder builder = DecodeSoapXml.methodToString(fault);
			FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/",
					Unpooled.wrappedBuffer(builder.toString().getBytes()));
			NettyHttpClient.sendMsg(request);
		}
		for (String str : nameList) {
		    ParameterInfoStruct pis = new ParameterInfoStruct();
		    pis.setName(str);
		    pis.setWritable(true);
		    parameterList.add(pis);
		}

		return parameterList;
	}

	/**
	 * 只取下一层节点名称
	 * @param elist
	 * @param name
     * @return
     */
	@SuppressWarnings("unchecked")
	public static List<String> getNodeNameOne(List<Element> elist, String name) {
	    List<String> nameList = new ArrayList<String>();
        if (name.matches(".*\\.[0-9]+\\.")) {//以.+数字+.结尾
            Element element = elist.get(0);
            List<Element> listElement = element.elements();
            for (Element e : listElement) {
                //如果下面还有孩子，需要返回.结尾
                if (e.elements().size() > 0 || e.attributeValue("instance") != null) {
                    if (!nameList.contains(name + e.getName() + ".")) {
                        nameList.add(name + e.getName() + ".");
                    }
                }else {
                    if (!nameList.contains(name + e.getName())) {
                        nameList.add(name + e.getName());
                    }
                }
            }
        }else {
            for (Element element : elist) {
                String arrName = element.attributeValue("instance");
                if (arrName != null) {
                    if (!nameList.contains(name + arrName + ".")) {
                        nameList.add(name + arrName + ".");
                    }
                }else {
                    List<Element> listElement = element.elements();
                    for (Element e : listElement) {
                        if (e.elements().size() > 0 || e.attributeValue("instance") != null) {
                            if (!nameList.contains(name + e.getName() + ".")) {
                                nameList.add(name + e.getName() + ".");
                            }
                        }else {
                            if (!nameList.contains(name + e.getName())) {
                                nameList.add(name + e.getName());
                            }
                        }
                    }
                }
            }
        }
		return nameList;
	}

	/**
	 * 查询所有子节点名称
	 * 
	 * @param node
	 * @param list
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<ParameterInfoStruct> getNodeName(Element node, List<ParameterInfoStruct> list, String name) {
		// 递归遍历当前节点所有的子节点
		List<Element> listElement = node.elements();// 所有一级子节点的list
		if (listElement.size() > 0) {
			for (Element e : listElement) {
				String arrName = e.attributeValue("instance");
				if (arrName != null) {
					getNodeName(e, list, name + "." + e.getName() + "." + arrName);
				} else {
					getNodeName(e, list, name + "." + e.getName());
				}

			}
		} else {
			ParameterInfoStruct infoStruct = new ParameterInfoStruct();
			infoStruct.setName(name);
			infoStruct.setWritable(true);
			list.add(infoStruct);
		}
		return list;
	}

	/**
	 * 添加节点
	 * 
	 * @param node
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static int addObject(Document node, String name) {
		List<Element> list = getElementListByName(node, name);
		List<Integer> coll = new ArrayList<Integer>();
		if (list.size() > 0) {
			for (Element e : list) {
				coll.add(Integer.parseInt(e.attributeValue("instance")));
			}
		} else {
			Fault fault = new Fault(new FaultStruct(9003, "这个节点没有任何实例，请先添加实例"), "9003", "这个节点没有任何实例，请先添加实例");
			StringBuilder builder = DecodeSoapXml.methodToString(fault);
			FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/",
					Unpooled.wrappedBuffer(builder.toString().getBytes()));
			NettyHttpClient.sendMsg(request);
		}
		int num = Collections.max(coll) + 1;
		
		Element old = list.get(0);
		Element parent = old.getParent();
		Element newE = parent.addElement(old.getName());
		newE.addAttribute("instance", num + "");
		List<Element> old_1 = old.elements();
		for (Element e : old_1) {
		    listNodes(e, newE);
		}
		return num;
	}
	
	public static void listNodes(Element old, Element parent) {
        Element newE = parent.addElement(old.getName());
        if (old.attributeValue("instance") != null) {
            newE.addAttribute("instance", old.attributeValue("instance"));
        }
        if (!(old.getTextTrim().equals(""))) {  
            newE.addText(old.getTextTrim());
        }
        // 当前节点下面子节点迭代器
        Iterator<Element> it = old.elementIterator();
        // 遍历
        while (it.hasNext()) {
            // 获取某个子节点对象
            Element e = it.next();
            // 对子节点进行遍历
            listNodes(e, newE);
        }
    }

	/**
	 * 删除节点
	 * 
	 * @param node
	 * @param name
	 * @return
	 */
	public static boolean delObject(Document node, String name) {
		List<Element> list = getElementListByName(node, name);
		if (list.size() == 1) {
			Element child = list.get(0);
			return child.getParent().remove(child);
		} else {
			Fault fault = new Fault(new FaultStruct(9003, "找到多个或没找到节点"), "9003", "找到多个或没找到节点");
			StringBuilder builder = DecodeSoapXml.methodToString(fault);
			FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/",
					Unpooled.wrappedBuffer(builder.toString().getBytes()));
			NettyHttpClient.sendMsg(request);
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		TemplateCache2 tc = new TemplateCache2();
		tc.load("cpe.xml", "ODJI8202LYN956");
		List<Element> list = getElementListByName(cpeMap.get("ODJI8202LYN956"), "InternetGatewayDevice.IPPingDiagnostics.NumberOfRepetitions");
		System.out.println(list.get(0).getText());
		// List<ParameterInfoStruct> list =
		// getPathByName(cpeMap.get("ODJI8202LYN956"),
		// "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANIPConnection.",
		// false);
		// for (ParameterInfoStruct str : list) {
		// System.out.println(str.getName());
		// }
		
//		addObject(cpeMap.get("ODJI8202LYN956"),
//				"InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANIPConnection.1");
//		System.out.println("===============wangshuodong================");
//		getElementListByName(cpeMap.get("ODJI8202LYN956"),
//				"InternetGatewayDevice.X_CMCC_UplinkQoS.PriorityQueue.1.");
//		System.out.println(delObject(cpeMap.get("ODJI8202LYN956"),
//				"InternetGatewayDevice.X_CMCC_UplinkQoS.PriorityQueue.1."));
//		System.out.println("===============wangshuodong================");
//		getElementListByName(cpeMap.get("ODJI8202LYN956"),
//				"InternetGatewayDevice.X_CMCC_UplinkQoS.PriorityQueue.1.");

//		 String phone = "InternetGatewayDevice.Services.VoiceService.1.PhyInterface.1.";
//		 //检查phone是否是合格的手机号(标准:1开头，第二位为3,5,8，后9位为任意数字)
//		 System.out.println(phone + ":" + phone.matches(".*\\.[0-9]+\\."));
	}

}

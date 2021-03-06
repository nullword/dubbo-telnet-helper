package com.mutech.dubbo.telnet.helper.util;

import com.mutech.dubbo.telnet.helper.pojo.FunctionData;
import com.mutech.dubbo.telnet.helper.pojo.ServiceData;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huchao
 */
public class DubboTelnetUtil {

    /**
     * 服务器返回的结尾
     */
    private static final String OUT_ENDING = "dubbo>";
    /**
     * 服务器返回的结尾的最后字符
     */
    private static final char OUT_ENDING_LAST_CHAR = '>';
    /**
     * telnet客户端
     */
    private TelnetClient telnetClient;
    /**
     * 服务器返回
     */
    private InputStream out;
    /**
     * telnet客户端输入
     */
    private PrintStream in;

    public List<ServiceData> initData(String ip, int port) throws IOException {
        telnetClient = new TelnetClient("VT220");
        telnetClient.connect(ip, port);
        out = telnetClient.getInputStream();
        in = new PrintStream(telnetClient.getOutputStream());
        writeIn("ls");
        String serviceNames = readOut();
        System.out.println(serviceNames);
        String[] serviceNameArray = serviceNames.split("\r\n");
        List<ServiceData> serviceDatas = new ArrayList<>();
        for (int i = 0, size = serviceNameArray.length - 1; i < size; i++) {
            ServiceData serviceData = new ServiceData();
            serviceData.setServiceName(serviceNameArray[i]);
            writeIn("ls -l " + serviceNameArray[i]);
            String functions = readOut();
            System.out.println(functions);
            String[] functionArray = functions.split("\r\n");
            List<FunctionData> functionDatas = new ArrayList<>();
            for (int j = 0, functionArraySize = functionArray.length - 1; j < functionArraySize; j++) {
                FunctionData functionData = new FunctionData();
                String functionName = functionArray[j];
                functionData.setFunctionName(functionName.substring(functionName.indexOf(' ') + 1, functionName.indexOf('(')));
                String paramNames = functionName.substring(functionName.indexOf('(') + 1, functionName.indexOf(')'));
                String[] paramNameArray = paramNames.split(",");
                List<String> functionParams = new ArrayList<>();
                for (int k = 0, paramNameArraySize = paramNameArray.length; k < paramNameArraySize; k++) {
                    functionParams.add(paramNameArray[k]);
                }
                functionData.setFunctionParams(functionParams);
                functionDatas.add(functionData);
            }
            serviceData.setFunctionDatas(functionDatas);
            serviceDatas.add(serviceData);
        }
        return serviceDatas;
    }

    public String readOut() throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        char c = (char) out.read();
        while (true) {
            stringBuffer.append(c);
            if (c == OUT_ENDING_LAST_CHAR) {
                if (stringBuffer.toString().endsWith(OUT_ENDING)) {
                    byte[] temp = stringBuffer.toString().getBytes("iso8859-1");
                    return new String(temp, "GBK");
                }
            }
            c = (char) out.read();
        }
    }

    public void writeIn(String value) {
        in.println(value);
        in.flush();
    }

}

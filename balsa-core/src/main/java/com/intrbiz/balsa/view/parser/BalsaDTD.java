package com.intrbiz.balsa.view.parser;

public class BalsaDTD
{
    public static final String Balsa_DTD = getBalsaDTD();

    public static final String Balsa_DTD_URI = "http://balsa.intrbiz.net/balsa.dtd";

    private static final String getBalsaDTD()
    {
        StringBuilder ret = new StringBuilder();
        ret.append("<!ENTITY pound \"&#x00A3;\" >").append("\r\n");
        ret.append("<!ENTITY nbsp \"&#x00A0;\" >").append("\r\n");
        return ret.toString();
    }

}

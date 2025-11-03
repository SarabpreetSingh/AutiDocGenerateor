package com.boa.hackathon.autodocgen.util;



import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    private static final ObjectMapper M = new ObjectMapper();
    public static String toJson(Object o){
        try { return M.writerWithDefaultPrettyPrinter().writeValueAsString(o);}
        catch(Exception e){ return "{}"; }
    }
}

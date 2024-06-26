package com.news.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author 归林
 * @date 2024/4/2
 */
public class SensitiveWordInit {
    private String ENCODING = "UTF-8";
    public HashMap sensitiveWordMap;

    public SensitiveWordInit(){
        super();
    }

    public Map initKeyWord(){
        try {
            //读取敏感词库
            Set<String> keyWordSet = readSensitiveWordFile();
            //将敏感词库加入到HashMap中
            addSensitiveWordToHashMap(keyWordSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sensitiveWordMap;
    }

    private void addSensitiveWordToHashMap(Set<String> keyWordSet) {
        sensitiveWordMap = new HashMap(keyWordSet.size());
        String key = null;
        Map nowMap = null;
        Map<String, String> newWorMap = null;
        //迭代keyWordSet
        Iterator<String> iterator = keyWordSet.iterator();
        while(iterator.hasNext()){
            key = iterator.next();
            nowMap = sensitiveWordMap;
            for(int i = 0 ; i < key.length() ; i++){
                char keyChar = key.charAt(i);
                Object wordMap = nowMap.get(keyChar);

                if(wordMap != null){
                    nowMap = (Map) wordMap;
                }
                else{
                    newWorMap = new HashMap<String,String>();
                    newWorMap.put("isEnd", "0");
                    nowMap.put(keyChar, newWorMap);
                    nowMap = newWorMap;
                }

                if(i == key.length() - 1){
                    nowMap.put("isEnd", "1");
                }
            }
        }
    }

    private Set<String> readSensitiveWordFile() throws Exception{
        Set<String> set = null;

        File file = new File("src/main/resources/static/word.txt");  
        InputStreamReader read = new InputStreamReader(new FileInputStream(file),ENCODING);
        try {
            if(file.isFile() && file.exists()){
                set = new HashSet<String>();
                BufferedReader bufferedReader = new BufferedReader(read);
                String txt = null;
                while((txt = bufferedReader.readLine()) != null){
                    set.add(txt);
                }
            }
            else{
                throw new Exception("敏感词库文件不存在");
            }
        } catch (Exception e) {
            throw e;
        }finally{
            read.close();
        }
        return set;
    }
}

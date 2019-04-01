package com.example.exportdemo.service.impl;

import com.example.exportdemo.entity.BaseEntity;
import com.example.exportdemo.service.IExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class ExportServiceImpl implements IExportService {

    //每个csv数据量
    private static final Integer fileCapacity = 10000;

    @Override
    public ByteArrayOutputStream exportData(List dataList, String[] fieldNames, String[] fieldDescs) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("生成csv byte数组数据");
        //csv文件名前缀
        String fileNameStr = "统计导出";

        //生成csv字节数组流列表
        List<byte[]> bytesList = new ArrayList<>();
        Integer fileCount = dataList.size() / fileCapacity;
        for (int i=0; i<fileCount; i++) {
            //分组，每个csv文件保存10000条数据，整体打包为一个zip文件
            List subList = dataList.subList(i*fileCapacity, (i+1)*fileCapacity);
            //生成byte数组
            byte[] bytes = this.generateCsvFile(subList, fieldNames, fieldDescs);
            bytesList.add(bytes);
            //这里不能清除，否则原list中对应的数据也会清除掉
            //subList.clear();
        }
        //最后不足10000的数据
        if (fileCount*fileCapacity < dataList.size()) {
            List subList = dataList.subList(fileCount*fileCapacity, dataList.size());
            //生成byte数组
            byte[] bytes = this.generateCsvFile(subList, fieldNames, fieldDescs);
            bytesList.add(bytes);
            subList.clear();
        }


        stopWatch.stop();

        //打包zip文件；
        stopWatch.start("压缩数据到下载流");

        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(temp);
        try {
            for (int i = 0; i < bytesList.size(); i++) {
                try {
                    String entryName = fileNameStr + (i + 1) + ".csv";
                    ZipEntry entry = new ZipEntry(entryName);
                    zos.putNextEntry(entry);
                    zos.write(bytesList.get(i));
                    zos.closeEntry();
                } catch (Exception e) {
                    log.error("zip文件生成错误。", e);
                }
            }
            zos.close();
            stopWatch.stop();
            log.info(stopWatch.prettyPrint());
            return temp;
        } catch (Exception e) {
            log.error("zip文件生成错误。", e);
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
                if (temp != null) {
                    temp.close();
                }
            } catch (IOException e) {
                log.error("文件流关闭错误。", e);
            }
        }
        return null;
    }


    /**
     * 传入数据列表、目标文件、字段名和字段标题，生成csv文件
     *
     * @param dataList
     * @param fieldNames
     * @param fieldDescs
     * @return
     */
    public byte[] generateCsvFile(List dataList, String[] fieldNames, String[] fieldDescs) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("生成csv");
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream)
        ) {
            //文件头部插入excel的BOM信息，否则打开会中文乱码
            bufferedOutputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            //插入标题行
            writeRow(fieldDescs, bufferedOutputStream);

            String[] contents = new String[fieldNames.length];
            if (dataList.get(0) instanceof Map) {
                for (int i = 0; i < dataList.size(); i++) {
                    Map<String, Object> dataMap = (HashMap) dataList.get(i);
                    for (int j = 0; fieldNames != null && j < fieldNames.length; j++) {
                        String filedName = fieldNames[j];
                        Object obj = dataMap.get(filedName);
                        if (obj == null || "null".equals(String.valueOf(obj))) {
                            obj = "";
                        }
                        contents[j] = String.valueOf(obj);
                    }
                    writeRow(contents, bufferedOutputStream);
                }
            } else if (dataList.get(0) instanceof BaseEntity) {
                for (int i = 0; i < dataList.size(); i++) {
                    Class clazz = dataList.get(i).getClass();
                    for (int j = 0; fieldNames != null && j < fieldNames.length; j++) {
                        String filedName = toUpperCaseFirstOne(fieldNames[j]);
                        Method method = clazz.getMethod(filedName);
                        method.setAccessible(true);
                        Object obj = method.invoke(dataList.get(i));
                        if (obj == null || obj.equals("null")) {
                            obj = "";
                        }
                        contents[j] = String.valueOf(obj);
                    }
                    writeRow(contents, bufferedOutputStream);
                }
            }
            bufferedOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException ", e);
        } catch (Exception e) {
            log.error("未捕获异常。", e);
        } finally {
            stopWatch.stop();
            log.info(stopWatch.prettyPrint());
        }
        return null;
    }

    /**
     * 获取get方法名
     * @param origin
     * @return
     */
    private String toUpperCaseFirstOne(String origin) {
        StringBuffer sb = new StringBuffer(origin);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.insert(0, "get");
        return sb.toString();
    }

    /**
     * 向csv文件写一行数据
     *
     * @param row
     * @param bufferedOutputStream
     * @throws IOException
     */
    private void writeRow(String[] row, BufferedOutputStream bufferedOutputStream) throws IOException {
        // 写入文件头部
        for (Object data : row) {
            StringBuilder sb = new StringBuilder();
            byte[] rowStr = sb.append("\"").append(data).append("\t\",").toString().getBytes("utf-8");
            bufferedOutputStream.write(rowStr);
        }
        bufferedOutputStream.write(System.getProperty("line.separator").getBytes());
    }
}

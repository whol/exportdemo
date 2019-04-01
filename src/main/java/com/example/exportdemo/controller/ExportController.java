package com.example.exportdemo.controller;

import com.example.exportdemo.entity.Status;
import com.example.exportdemo.service.IExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
public class ExportController {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private IExportService exportService;

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportStatus(HttpServletResponse response) {
        StopWatch stopWatch = new StopWatch();//统计导出耗时
        stopWatch.start("模拟生成数据");

        //模拟导出数据
        List<Status> statusList = new ArrayList<>();
        for (int i=1; i<=100000; i++) {
            Status status = new Status();
            status.setId("" + i + "");
            status.setName("第" + i + "条");
            status.setValue("第" + i + "条内容");
            status.setMark("第" + i + "条备注");
            status.setCrateTime(sdf.format(new Date()));
            statusList.add(status);
        }
        stopWatch.stop();

        stopWatch.start("生成csv");
        String[] fieldNames = new String[]{"id", "name", "value", "mark", "crateTime"};
        //筛选条件中文
        String[] fieldDescs = new String[]{"编号", "名称", "内容", "备注", "创建时间"};

        ByteArrayOutputStream output = exportService.exportData(statusList, fieldNames, fieldDescs);
        stopWatch.stop();

        stopWatch.start("生成下载流");
        byte[] content = output.toByteArray();
        InputStream is = new ByteArrayInputStream(content);

        //下载文件
        String fileName = new Date().getTime() + ".zip";
        try {
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            ServletOutputStream out = response.getOutputStream();
            // 设置response参数，可以打开下载页面
            response.reset();
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename="+ new String(fileName.getBytes(), "iso-8859-1"));
            try {
                bis = new BufferedInputStream(is);
                bos = new BufferedOutputStream(out);
                byte[] buff = new byte[2048];
                int bytesRead;
                // Simple read/write loop.
                while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                    bos.write(buff, 0, bytesRead);
                }
            } catch (final IOException e) {
                throw e;
            } finally {
                if (bis != null)
                    bis.close();
                if (bos != null)
                    bos.close();
                stopWatch.stop();
                log.info(stopWatch.prettyPrint());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}

package com.example.exportdemo.service;

import com.example.exportdemo.entity.BaseEntity;

import java.io.ByteArrayOutputStream;
import java.util.List;

public interface IExportService {
    /**
     * 导出方法，返回字节数组输出流
     *
     * @param dataList   待导出的列表，列表内容可以为实体类或Map，实体类需继承BaseEntity
     * @param fieldNames   导出字段
     * @param fieldDescs   导出标题
     */
    ByteArrayOutputStream exportData(List dataList, String[] fieldNames, String[] fieldDescs);
}

package com.ric.studio.zkdemo.controller;

import com.alibaba.fastjson.JSON;
import com.ric.studio.zkdemo.common.BaseResponse;
import com.ric.studio.zkdemo.zookeeper.ChildListener;
import com.ric.studio.zkdemo.zookeeper.ZkCuratorKit;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Richard_yyf
 * @version 1.0 2019/3/28
 */

@Controller
@RequestMapping("/curator")
public class CuratorController {

    private ConcurrentMap<String, ChildListener> map = new ConcurrentHashMap<>();
//    private ConcurrentMap<String, IZkDataListener> dataListenerMap = new ConcurrentHashMap<>();

    @Autowired
    private ZkCuratorKit zk;

    @ResponseBody
    @PostMapping(value = "/create")
    public BaseResponse create(@RequestParam String path,
                               @RequestParam boolean ephemeral) {
        try {
            zk.create(path, ephemeral);
        } catch (Exception e) {
            return new BaseResponse<>(false, ExceptionUtils.getFullStackTrace(e));
        }
        return new BaseResponse<>(true, path + "节点 创建成功");
    }

    @ResponseBody
    @PostMapping(value = "/delete")
    public BaseResponse delete(@RequestParam String path) {
        try {
            zk.delete(path);
        } catch (Exception e) {
            return new BaseResponse<>(false, ExceptionUtils.getFullStackTrace(e));
        }
        return new BaseResponse<>(true, path + "节点 删除成功");
    }

    @ResponseBody
    @PostMapping(value = "/getChildren")
    public BaseResponse getChildren(@RequestParam String path) {
        try {
            return new BaseResponse<>(true, zk.getChildren(path));
        } catch (Exception e) {
            return new BaseResponse<>(false, ExceptionUtils.getFullStackTrace(e));
        }
    }

    @ResponseBody
    @PostMapping(value = "/addChildListener")
    public BaseResponse addChildListener(@RequestParam String path) {
        try {
            ChildListener listener = (parentPath, currentChilds) -> System.out.println(String.format("parentPath [%s] detect child change, currentChild [%s]", parentPath, JSON.toJSONString(currentChilds)));
            map.putIfAbsent(path, listener);
            return new BaseResponse<>(true, zk.addChildListener(path, listener));
        } catch (Exception e) {
            return new BaseResponse<>(false, ExceptionUtils.getFullStackTrace(e));
        }
    }


    @ResponseBody
    @PostMapping(value = "/removeChildListener")
    public BaseResponse removeChildListener(@RequestParam String path) {
        try {
            zk.removeChildListener(path, map.get(path));
        } catch (Exception e) {
            return new BaseResponse<>(false, ExceptionUtils.getFullStackTrace(e));
        }
        return new BaseResponse<>(true, path + "节点 删除子节点监听器成功");
    }
}

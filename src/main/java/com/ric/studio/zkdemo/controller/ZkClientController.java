package com.ric.studio.zkdemo.controller;

import com.alibaba.fastjson.JSON;
import com.ric.studio.zkdemo.common.BaseResponse;
import com.ric.studio.zkdemo.zookeeper.ChildListener;
import com.ric.studio.zkdemo.zookeeper.ZkClientKit;
import org.I0Itec.zkclient.IZkDataListener;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Richard_yyf
 * @version 1.0 2019/3/28
 */

@RequestMapping("/zkclient")
@Controller
public class ZkClientController {

    private ConcurrentMap<String, ChildListener> map = new ConcurrentHashMap<>();
    private ConcurrentMap<String, IZkDataListener> dataListenerMap = new ConcurrentHashMap<>();

    @Autowired
    private ZkClientKit zk;

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
    @PostMapping(value = "/addData")
    public BaseResponse create(@RequestParam String path,
                               @RequestParam String data) {
        try {
            zk.addData(path, data);
        } catch (Exception e) {
            return new BaseResponse<>(false, ExceptionUtils.getFullStackTrace(e));
        }
        return new BaseResponse<>(true, path + "节点 赋值成功");
    }

    @ResponseBody
    @PostMapping(value = "/getData")
    public BaseResponse getData(@RequestParam String path) {
        try {
            return new BaseResponse<>(true, zk.getData(path));
        } catch (Exception e) {
            return new BaseResponse<>(false, ExceptionUtils.getFullStackTrace(e));
        }
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
    @PostMapping(value = "/checkExists")
    public BaseResponse checkExists(@RequestParam String path) {
        try {
            return new BaseResponse<>(true, zk.checkExists(path));
        } catch (Exception e) {
            return new BaseResponse<>(false, ExceptionUtils.getFullStackTrace(e));
        }
    }

    @ResponseBody
    @PostMapping(value = "/addChildListener")
    public BaseResponse addChildListener(@RequestParam String path) {
        try {
            ChildListener listener = new ChildListener() {
                @Override
                public void childChanged(String parentPath, List<String> currentChilds) {
                    System.out.println(String.format("parentPath [%s] detect child change, currentChild [%s]", parentPath, JSON.toJSONString(currentChilds)));
                }
            };
            map.putIfAbsent(path, listener);
            return new BaseResponse<>(true, zk.addChildListener(path, listener));
        } catch (Exception e) {
            return new BaseResponse<>(false, ExceptionUtils.getFullStackTrace(e));
        }
    }

    @ResponseBody
    @PostMapping(value = "/addChildDataListener")
    public BaseResponse addChildDataListener(@RequestParam String path) {
        try {
            IZkDataListener listener = new IZkDataListener() {

                @Override
                public void handleDataChange(String dataPath, Object data) throws Exception {
                    System.out.println(String.format("dataPath[%s] data has been altered to [%s]", dataPath, data));
                }

                @Override
                public void handleDataDeleted(String dataPath) throws Exception {
                    System.out.println(String.format("dataPath[%s] data has been deleted", dataPath));
                }
            };
            dataListenerMap.putIfAbsent(path, listener);
            zk.addChildDataListener(path, listener);
            return new BaseResponse<>(true, path + "节点 增加数据监听器成功");
        } catch (Exception e) {
            return new BaseResponse<>(false, ExceptionUtils.getFullStackTrace(e));
        }
    }

    @ResponseBody
    @PostMapping(value = "/removeChildDataListener")
    public BaseResponse removeChildDataListener(@RequestParam String path) {
        try {
            zk.removeChildDataListener(path, dataListenerMap.remove(path));
        } catch (Exception e) {
            return new BaseResponse<>(false, ExceptionUtils.getFullStackTrace(e));
        }
        return new BaseResponse<>(true, path + "节点 删除子节点监听器成功");
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

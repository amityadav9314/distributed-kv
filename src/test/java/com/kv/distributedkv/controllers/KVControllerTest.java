//package com.kv.distributedkv.controllers;
//
//import com.kv.distributedkv.dtos.KVResponse;
//import com.kv.distributedkv.dtos.ResponseStatus;
//import com.kv.distributedkv.services.KVService;
//import com.kv.distributedkv.utils.KVUtil;
//import org.junit.Assert;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import javax.servlet.http.HttpServletResponse;
//
//@RunWith(MockitoJUnitRunner.class)
//@ExtendWith(SpringExtension.class)
//public class KVControllerTest {
//
//    @InjectMocks
//    private KVController kvController;
//
//    @Mock
//    private KVService kvService;
//
//    @Test
//    public void testGetKV_when_noKeyFound() {
//        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
//        Mockito.when(kvService.get(Mockito.anyString())).thenReturn(failedKVResponse());
//        KVResponse kvResponse = kvController.get("dummy", httpServletResponse);
//        Assert.assertEquals(ResponseStatus.FAILED, kvResponse.getStatus());
//        Assert.assertNull(null, kvResponse.getValue());
//    }
//
//    @Test
//    public void testGetKV_when_keyIsFound() {
//        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
//        Mockito.when(kvService.get(Mockito.anyString())).thenReturn(successVResponse());
//        KVResponse kvResponse = kvController.get("dummy", httpServletResponse);
//        Assert.assertEquals(ResponseStatus.SUCCESS, kvResponse.getStatus());
//        Assert.assertEquals("value", kvResponse.getValue());
//        Assert.assertEquals("dummy", kvResponse.getKey());
//    }
//
//    private KVResponse failedKVResponse() {
//        return KVUtil.getErrorBaseResponse(500, new RuntimeException());
//    }
//
//    private KVResponse successVResponse() {
//        KVResponse kvResponse = new KVResponse();
//        kvResponse.setKey("dummy");
//        kvResponse.setValue("value");
//        kvResponse.setStatus(ResponseStatus.SUCCESS);
//        kvResponse.setError(null);
//        return kvResponse;
//    }
//}

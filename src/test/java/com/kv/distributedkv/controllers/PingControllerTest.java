//package com.kv.distributedkv.controllers;
//
//import com.kv.distributedkv.dtos.HostStatus;
//import com.kv.distributedkv.dtos.PingResponse;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.runner.RunWith;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import static org.junit.Assert.assertEquals;
//
//@RunWith(MockitoJUnitRunner.class)
//@ExtendWith(SpringExtension.class)
//public class PingControllerTest {
//
//    @Test
//    public void testPingController() {
//        PingController pingController = new PingController();
//        PingResponse pingResponse = pingController.getPingResponse();
//        assertEquals(HostStatus.UP, pingResponse.getStatus());
//    }
//}

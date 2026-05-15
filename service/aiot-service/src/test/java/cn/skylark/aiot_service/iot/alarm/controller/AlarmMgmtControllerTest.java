package cn.skylark.aiot_service.iot.alarm.controller;

import cn.skylark.aiot_service.iot.alarm.dto.AlarmRecordPageResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRecordResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRulePageResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRuleResponse;
import cn.skylark.aiot_service.iot.alarm.service.AlarmMgmtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Standalone MockMvc: avoids loading {@code AiotServiceApplication} (MyBatis / DB) which breaks {@code @WebMvcTest}.
 */
@ExtendWith(MockitoExtension.class)
class AlarmMgmtControllerTest {

  @Mock
  private AlarmMgmtService alarmMgmtService;

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    AlarmMgmtController controller = new AlarmMgmtController(alarmMgmtService);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .build();
  }

  @Test
  void listRules_returnsEnvelope() throws Exception {
    AlarmRulePageResponse page = new AlarmRulePageResponse();
    page.setPageNum(1);
    page.setPageSize(10);
    page.setTotal(0L);
    page.setRecords(Collections.emptyList());
    when(alarmMgmtService.listRules(any())).thenReturn(page);

    mockMvc.perform(get("/api/aiot-service/alarm/rules"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.total").value(0));
  }

  @Test
  void createRule_returnsRule() throws Exception {
    AlarmRuleResponse created = new AlarmRuleResponse();
    created.setId(99L);
    created.setName("t-rule");
    created.setRecoveryMode("MANUAL");
    when(alarmMgmtService.createRule(any())).thenReturn(created);

    String body = objectMapper.writeValueAsString(minimalRuleBody());

    mockMvc.perform(post("/api/aiot-service/alarm/rules")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.id").value(99))
        .andExpect(jsonPath("$.data.recoveryMode").value("MANUAL"));
  }

  @Test
  void getRule_returnsRule() throws Exception {
    AlarmRuleResponse r = new AlarmRuleResponse();
    r.setId(7L);
    r.setName("one");
    when(alarmMgmtService.getRule(7L)).thenReturn(r);

    mockMvc.perform(get("/api/aiot-service/alarm/rules/7"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("one"));
  }

  @Test
  void updateRule_returnsRule() throws Exception {
    AlarmRuleResponse r = new AlarmRuleResponse();
    r.setId(7L);
    r.setName("updated");
    when(alarmMgmtService.updateRule(eq(7L), any())).thenReturn(r);

    String body = objectMapper.writeValueAsString(minimalRuleBody());

    mockMvc.perform(put("/api/aiot-service/alarm/rules/7")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("updated"));
  }

  @Test
  void deleteRule_callsService() throws Exception {
    mockMvc.perform(delete("/api/aiot-service/alarm/rules/12"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));

    verify(alarmMgmtService).deleteRule(12L);
  }

  @Test
  void listRecords_returnsEnvelope() throws Exception {
    AlarmRecordPageResponse page = new AlarmRecordPageResponse();
    page.setPageNum(1);
    page.setPageSize(20);
    page.setTotal(1L);
    AlarmRecordResponse rec = new AlarmRecordResponse();
    rec.setId(100L);
    rec.setStatus("ACTIVE");
    rec.setRuleRecoveryMode("MANUAL");
    page.setRecords(Collections.singletonList(rec));
    when(alarmMgmtService.listRecords(any())).thenReturn(page);

    mockMvc.perform(get("/api/aiot-service/alarm/records"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.records[0].ruleRecoveryMode").value("MANUAL"))
        .andExpect(jsonPath("$.data.records[0].status").value("ACTIVE"));
  }

  @Test
  void getRecord_returnsRecord() throws Exception {
    AlarmRecordResponse r = new AlarmRecordResponse();
    r.setId(55L);
    r.setStatus("RECOVERED");
    when(alarmMgmtService.getRecord(55L)).thenReturn(r);

    mockMvc.perform(get("/api/aiot-service/alarm/records/55"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("RECOVERED"));
  }

  @Test
  void recoverRecord_returnsRecord() throws Exception {
    AlarmRecordResponse r = new AlarmRecordResponse();
    r.setId(3L);
    r.setStatus("RECOVERED");
    r.setRuleRecoveryMode("MANUAL");
    when(alarmMgmtService.recoverRecordManually(3L)).thenReturn(r);

    mockMvc.perform(post("/api/aiot-service/alarm/records/3/recover"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("RECOVERED"));

    verify(alarmMgmtService).recoverRecordManually(3L);
  }

  private static Map<String, Object> minimalRuleBody() {
    Map<String, Object> m = new LinkedHashMap<String, Object>();
    m.put("deviceGroupKey", "dg1");
    m.put("name", "n");
    m.put("sourceType", "PROPERTY");
    m.put("severity", "HIGH");
    m.put("triggerMode", "INSTANT");
    m.put("durationSeconds", 0);
    m.put("recoveryMode", "AUTO");
    m.put("dedupMode", "SINGLE_ACTIVE");
    m.put("enabled", true);
    m.put("conditionJson", "{\"propertyKey\":\"t\",\"operator\":\"GT\",\"threshold\":1}");
    return m;
  }
}

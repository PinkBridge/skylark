package cn.skylark.aiot_service.iot.notify.controller;

import cn.skylark.aiot_service.iot.notify.dto.NotifyChannelCreateRequest;
import cn.skylark.aiot_service.iot.notify.dto.NotifyChannelPageQuery;
import cn.skylark.aiot_service.iot.notify.dto.NotifyChannelPageResponse;
import cn.skylark.aiot_service.iot.notify.dto.NotifyChannelResponse;
import cn.skylark.aiot_service.iot.notify.dto.NotifyChannelTestRequest;
import cn.skylark.aiot_service.iot.notify.dto.NotifyChannelUpdateRequest;
import cn.skylark.aiot_service.iot.notify.service.NotifyChannelMgmtService;
import cn.skylark.web.common.Ret;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/aiot-service/notify")
public class NotifyChannelController {
  private final NotifyChannelMgmtService notifyChannelMgmtService;

  public NotifyChannelController(NotifyChannelMgmtService notifyChannelMgmtService) {
    this.notifyChannelMgmtService = notifyChannelMgmtService;
  }

  @GetMapping("/channels")
  public Ret<NotifyChannelPageResponse> listChannelsPaged(@ModelAttribute NotifyChannelPageQuery query) {
    return Ret.data(notifyChannelMgmtService.listChannels(query));
  }

  @GetMapping("/channels/options")
  public Ret<List<NotifyChannelResponse>> listChannelOptions(@RequestParam("channelKind") String channelKind) {
    return Ret.data(notifyChannelMgmtService.listChannelsSimple(channelKind));
  }

  @PostMapping("/channels")
  public Ret<NotifyChannelResponse> createChannel(@Validated @RequestBody NotifyChannelCreateRequest request) {
    return Ret.data(notifyChannelMgmtService.createChannel(request));
  }

  @GetMapping("/channels/{id}")
  public Ret<NotifyChannelResponse> getChannel(@PathVariable("id") Long id) {
    return Ret.data(notifyChannelMgmtService.getChannel(id));
  }

  @PutMapping("/channels/{id}")
  public Ret<NotifyChannelResponse> updateChannel(@PathVariable("id") Long id,
                                                @Validated @RequestBody NotifyChannelUpdateRequest request) {
    return Ret.data(notifyChannelMgmtService.updateChannel(id, request));
  }

  @DeleteMapping("/channels/{id}")
  public Ret<Void> deleteChannel(@PathVariable("id") Long id) {
    notifyChannelMgmtService.deleteChannel(id);
    return Ret.ok();
  }

  @PostMapping("/channels/{id}/test")
  public Ret<Void> testChannel(@PathVariable("id") Long id, @Validated @RequestBody NotifyChannelTestRequest request) {
    notifyChannelMgmtService.testChannel(id, request);
    return Ret.ok();
  }
}

package cn.skylark.aiot_service.iot.mgmt.service;

import cn.skylark.aiot_service.iot.access.mapper.AclPolicyMapper;
import cn.skylark.aiot_service.iot.access.model.AclPolicyRecord;
import cn.skylark.aiot_service.iot.mgmt.mapper.ProductMapper;
import cn.skylark.aiot_service.iot.mgmt.model.dto.CopyProductRequest;
import cn.skylark.aiot_service.iot.mgmt.model.dto.CreateProductRequest;
import cn.skylark.aiot_service.iot.mgmt.model.dto.ProductDataChannelResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.ProductPageQuery;
import cn.skylark.aiot_service.iot.mgmt.model.dto.ProductPageResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.ProductResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.UpdateProductDataChannelRequest;
import cn.skylark.aiot_service.iot.mgmt.model.dto.UpdateProductRequest;
import cn.skylark.aiot_service.iot.mgmt.model.entity.ProductEntity;
import cn.skylark.aiot_service.iot.mgmt.model.enums.DeviceType;
import cn.skylark.aiot_service.iot.mgmt.model.enums.ProductProtocolType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ProductServiceImpl implements ProductService {
  private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
  private static final String STATUS_ENABLED = "enabled";
  private static final String STATUS_DISABLED = "disabled";
  private static final String PROTOCOL_MQTT_ALINK_JSON = "MQTT_ALINK_JSON";
  private static final String DEVICE_KEY_PLACEHOLDER = "${deviceKey}";
  private static final int PRODUCT_SECRET_LENGTH = 16;
  private static final char[] PRODUCT_SECRET_ALPHABET =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final ProductMapper productMapper;
  private final AclPolicyMapper aclPolicyMapper;

  public ProductServiceImpl(ProductMapper productMapper, AclPolicyMapper aclPolicyMapper) {
    this.productMapper = productMapper;
    this.aclPolicyMapper = aclPolicyMapper;
  }

  @Override
  public ProductResponse create(CreateProductRequest req) {
    ProductEntity entity = new ProductEntity();
    entity.setTenantId(AiotDataDomainSupport.requireTenantId());
    entity.setOrgId(AiotDataDomainSupport.currentOrgId());
    entity.setProductKey(req.getProductKey().trim());
    entity.setProductSecret(generateProductSecret());
    entity.setName(req.getName().trim());
    entity.setCoverImageUrl(trimToNull(req.getCoverImageUrl()));
    entity.setThumbnailUrl(trimToNull(req.getThumbnailUrl()));
    entity.setDescription(req.getDescription());
    entity.setProtocolType(ProductProtocolType.normalize(req.getProtocolType()));
    entity.setDeviceType(DeviceType.normalize(req.getDeviceType()));
    entity.setStatus(STATUS_ENABLED);
    try {
      productMapper.insert(entity);
    } catch (DuplicateKeyException e) {
      throw new MgmtException(HttpStatus.CONFLICT, "productKey already exists");
    }
    initDefaultAclTemplatesIfNeeded(entity);
    return get(entity.getProductKey());
  }

  @Override
  public ProductResponse get(String productKey) {
    ProductEntity entity = productMapper.findByProductKey(productKey);
    if (entity == null) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "product not found");
    }
    return toResponse(entity, true);
  }

  @Override
  public ProductPageResponse list(ProductPageQuery query) {
    int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
    int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : Math.min(query.getPageSize(), 100);
    int offset = (pageNum - 1) * pageSize;
    List<ProductEntity> list = productMapper.listPage(
        trimToNull(query.getProductKey()),
        trimToNull(query.getName()),
        trimToNull(query.getStatus()),
        offset,
        pageSize
    );
    List<ProductResponse> records = new ArrayList<ProductResponse>();
    for (ProductEntity item : list) {
      records.add(toResponse(item, false));
    }
    ProductPageResponse response = new ProductPageResponse();
    response.setRecords(records);
    response.setTotal(productMapper.countPage(
        trimToNull(query.getProductKey()),
        trimToNull(query.getName()),
        trimToNull(query.getStatus())
    ));
    response.setPageNum(pageNum);
    response.setPageSize(pageSize);
    return response;
  }

  @Override
  public ProductResponse update(String productKey, UpdateProductRequest req) {
    ProductEntity entity = new ProductEntity();
    entity.setProductKey(productKey);
    entity.setName(req.getName().trim());
    entity.setCoverImageUrl(trimToNull(req.getCoverImageUrl()));
    entity.setThumbnailUrl(trimToNull(req.getThumbnailUrl()));
    entity.setDescription(req.getDescription());
    entity.setProtocolType(ProductProtocolType.normalize(req.getProtocolType()));
    entity.setDeviceType(DeviceType.normalize(req.getDeviceType()));
    if (productMapper.updateByProductKey(entity) == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "product not found");
    }
    return get(productKey);
  }

  @Override
  public ProductResponse enable(String productKey) {
    return updateStatus(productKey, STATUS_ENABLED);
  }

  @Override
  public ProductResponse disable(String productKey) {
    return updateStatus(productKey, STATUS_DISABLED);
  }

  @Override
  public ProductResponse copy(String productKey, CopyProductRequest req) {
    ProductEntity source = productMapper.findByProductKey(productKey);
    if (source == null) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "product not found");
    }
    ProductEntity target = new ProductEntity();
    target.setTenantId(AiotDataDomainSupport.requireTenantId());
    target.setOrgId(AiotDataDomainSupport.currentOrgId());
    target.setProductKey(req.getTargetProductKey().trim());
    target.setProductSecret(generateProductSecret());
    target.setName(req.getTargetName().trim());
    target.setCoverImageUrl(source.getCoverImageUrl());
    target.setThumbnailUrl(source.getThumbnailUrl());
    target.setDescription(source.getDescription());
    target.setProtocolType(source.getProtocolType());
    target.setDeviceType(source.getDeviceType());
    target.setStatus(source.getStatus());
    try {
      productMapper.insert(target);
    } catch (DuplicateKeyException e) {
      throw new MgmtException(HttpStatus.CONFLICT, "productKey already exists");
    }
    copyProductDataChannels(productKey, target);
    return get(target.getProductKey());
  }

  @Override
  public List<ProductDataChannelResponse> listDataChannels(String productKey) {
    assertProductExists(productKey);
    List<AclPolicyRecord> list = aclPolicyMapper.listProductChannels(productKey);
    List<ProductDataChannelResponse> result = new ArrayList<ProductDataChannelResponse>();
    if (list == null) {
      return result;
    }
    for (AclPolicyRecord item : list) {
      if (item == null) continue;
      ProductDataChannelResponse row = new ProductDataChannelResponse();
      row.setId(item.getId());
      row.setAction(item.getAction());
      row.setTopicPattern(item.getTopicPattern());
      row.setEffect(item.getEffect());
      row.setPriority(item.getPriority());
      row.setEnabled(item.getEnabled() != null && item.getEnabled() != 0);
      result.add(row);
    }
    return result;
  }

  @Override
  public void updateDataChannel(String productKey, Long id, UpdateProductDataChannelRequest request) {
    assertProductExists(productKey);
    if (id == null || id.longValue() <= 0) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "id invalid");
    }
    String effect = request.getEffect() == null ? "" : request.getEffect().trim().toLowerCase(Locale.ROOT);
    if (!"allow".equals(effect) && !"deny".equals(effect)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "effect invalid");
    }
    int updated = aclPolicyMapper.updateEffectAndEnabledById(id, productKey, effect, request.getEnabled());
    if (updated == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "data channel not found");
    }
  }

  @Override
  public void delete(String productKey) {
    if (productMapper.findByProductKey(productKey) == null) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "product not found");
    }
    aclPolicyMapper.deleteByProductKey(productKey);
    productMapper.deleteByProductKey(productKey);
  }

  private ProductResponse updateStatus(String productKey, String status) {
    if (productMapper.updateStatus(productKey, status) == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "product not found");
    }
    return get(productKey);
  }

  private void assertProductExists(String productKey) {
    if (productMapper.findByProductKey(productKey) == null) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "product not found");
    }
  }

  private ProductResponse toResponse(ProductEntity entity, boolean includeSecret) {
    ProductResponse resp = new ProductResponse();
    resp.setProductKey(entity.getProductKey());
    if (includeSecret) {
      resp.setProductSecret(entity.getProductSecret());
    }
    resp.setName(entity.getName());
    resp.setCoverImageUrl(entity.getCoverImageUrl());
    resp.setThumbnailUrl(entity.getThumbnailUrl());
    resp.setDescription(entity.getDescription());
    resp.setProtocolType(entity.getProtocolType());
    resp.setDeviceType(entity.getDeviceType());
    resp.setStatus(entity.getStatus());
    resp.setDeviceCount(0L);
    return resp;
  }

  private static String generateProductSecret() {
    char[] buf = new char[PRODUCT_SECRET_LENGTH];
    for (int i = 0; i < PRODUCT_SECRET_LENGTH; i++) {
      buf[i] = PRODUCT_SECRET_ALPHABET[SECURE_RANDOM.nextInt(PRODUCT_SECRET_ALPHABET.length)];
    }
    return new String(buf);
  }

  private String trimToNull(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private void initDefaultAclTemplatesIfNeeded(ProductEntity product) {
    if (product == null || !PROTOCOL_MQTT_ALINK_JSON.equalsIgnoreCase(safe(product.getProtocolType()))) {
      return;
    }
    String productKey = safe(product.getProductKey());
    if (!StringUtils.hasText(productKey)) {
      return;
    }
    Long tenantId = product.getTenantId();

    insertProductAclTemplate(tenantId, productKey, "publish",
        "/sys/" + productKey + "/" + DEVICE_KEY_PLACEHOLDER + "/thing/event/property/post", "allow", 300);
    insertProductAclTemplate(tenantId, productKey, "publish",
        "/sys/" + productKey + "/" + DEVICE_KEY_PLACEHOLDER + "/thing/event/+/post", "allow", 300);
    insertProductAclTemplate(tenantId, productKey, "publish",
        "/sys/" + productKey + "/" + DEVICE_KEY_PLACEHOLDER + "/thing/service/+/reply", "allow", 300);
    insertProductAclTemplate(tenantId, productKey, "publish",
        "/sys/" + productKey + "/" + DEVICE_KEY_PLACEHOLDER + "/thing/service/#", "deny", 200);

    insertProductAclTemplate(tenantId, productKey, "subscribe",
        "/sys/" + productKey + "/" + DEVICE_KEY_PLACEHOLDER + "/thing/service/#", "allow", 300);
    insertProductAclTemplate(tenantId, productKey, "subscribe",
        "/sys/" + productKey + "/" + DEVICE_KEY_PLACEHOLDER + "/thing/event/property/post_reply", "allow", 300);
    insertProductAclTemplate(tenantId, productKey, "subscribe",
        "/sys/" + productKey + "/" + DEVICE_KEY_PLACEHOLDER + "/thing/event/+/post_reply", "allow", 300);
    insertProductAclTemplate(tenantId, productKey, "subscribe",
        "/sys/" + productKey + "/" + DEVICE_KEY_PLACEHOLDER + "/thing/service/+/reply_ack", "allow", 300);
  }

  private void insertProductAclTemplate(Long tenantId, String productKey, String action,
                                        String topicPattern, String effect, int priority) {
    try {
      AclPolicyRecord policy = new AclPolicyRecord();
      policy.setTenantId(tenantId);
      policy.setProductKey(productKey);
      policy.setSubjectType("product");
      policy.setSubjectValue(productKey);
      policy.setAction(action);
      policy.setTopicPattern(topicPattern);
      policy.setEffect(effect);
      policy.setPriority(priority);
      policy.setEnabled(1);
      aclPolicyMapper.insert(policy);
    } catch (RuntimeException e) {
      log.warn("init product acl template failed for {} action={} topic={} effect={} priority={}: {}",
          productKey, action, topicPattern, effect, priority, e.getMessage());
    }
  }

  private void copyProductDataChannels(String sourceProductKey, ProductEntity target) {
    if (target == null) return;
    String sourceKey = safe(sourceProductKey);
    String targetKey = safe(target.getProductKey());
    if (!StringUtils.hasText(sourceKey) || !StringUtils.hasText(targetKey)) return;
    List<AclPolicyRecord> channels = aclPolicyMapper.listProductChannels(sourceKey);
    if (channels == null || channels.isEmpty()) return;

    Long tenantId = target.getTenantId();
    for (AclPolicyRecord item : channels) {
      if (item == null) continue;
      try {
        AclPolicyRecord copied = new AclPolicyRecord();
        copied.setTenantId(tenantId);
        copied.setProductKey(targetKey);
        copied.setSubjectType("product");
        copied.setSubjectValue(targetKey);
        copied.setAction(safe(item.getAction()));
        copied.setTopicPattern(rewriteTopicPatternForCopiedProduct(item.getTopicPattern(), sourceKey, targetKey));
        copied.setEffect(safe(item.getEffect()));
        copied.setPriority(item.getPriority());
        copied.setEnabled(item.getEnabled());
        aclPolicyMapper.insert(copied);
      } catch (RuntimeException e) {
        log.warn("copy product acl template failed source={} target={} action={} topic={}: {}",
            sourceKey, targetKey, item.getAction(), item.getTopicPattern(), e.getMessage());
      }
    }
  }

  private static String rewriteTopicPatternForCopiedProduct(String topicPattern, String sourceProductKey, String targetProductKey) {
    String topic = safe(topicPattern);
    if (!StringUtils.hasText(topic)) return topic;
    String sourcePrefix = "/sys/" + sourceProductKey + "/";
    String targetPrefix = "/sys/" + targetProductKey + "/";
    if (topic.startsWith(sourcePrefix)) {
      return targetPrefix + topic.substring(sourcePrefix.length());
    }
    return topic.replace(sourcePrefix, targetPrefix);
  }

  private static String safe(String s) {
    return s == null ? "" : s.trim();
  }
}


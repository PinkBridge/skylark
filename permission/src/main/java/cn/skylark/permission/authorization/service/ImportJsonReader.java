package cn.skylark.permission.authorization.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class ImportJsonReader {
  private final ObjectMapper objectMapper;

  public ImportJsonReader(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public <T> T read(MultipartFile file, Class<T> clazz) throws IOException {
    if (file == null || file.isEmpty()) {
      return null;
    }
    return objectMapper.readValue(file.getInputStream(), clazz);
  }
}


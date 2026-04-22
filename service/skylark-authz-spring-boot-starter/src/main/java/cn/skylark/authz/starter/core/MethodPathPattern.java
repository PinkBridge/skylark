package cn.skylark.authz.starter.core;

import lombok.Value;

@Value
public class MethodPathPattern {
  /**
   * Uppercase HTTP method or "*" to match all.
   */
  String method;
  /**
   * Ant-style pattern.
   */
  String pathPattern;
}


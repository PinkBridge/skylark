package cn.skylark.web.exception;

import java.lang.reflect.Method;

/**
 * Create a response body compatible with each service's preferred Ret shape.
 *
 * Priority:
 * 1) cn.skylark.web.common.Ret (used by business/demo services)
 * 2) cn.skylark.permission.utils.Ret (used by permission service)
 * 3) SkylarkErrorResponse fallback
 */
final class SkylarkResponseBodies {
  private SkylarkResponseBodies() {}

  static Object fail(int code, String message) {
    Object ret = tryRetFail("cn.skylark.web.common.Ret", code, message);
    if (ret != null) return ret;

    ret = tryRetFail("cn.skylark.permission.utils.Ret", code, message);
    if (ret != null) return ret;

    return SkylarkErrorResponse.of(code, message);
  }

  private static Object tryRetFail(String className, int code, String message) {
    try {
      Class<?> clazz = Class.forName(className);
      Method m = clazz.getMethod("fail", int.class, String.class);
      return m.invoke(null, code, message);
    } catch (Throwable ignored) {
      return null;
    }
  }
}


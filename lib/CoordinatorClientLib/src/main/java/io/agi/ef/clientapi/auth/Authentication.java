package io.agi.ef.clientapi.auth;

import java.util.Map;

public interface Authentication {
  /** Apply authentication settings to header and query params. */
  void applyToParams(Map<String, String> queryParams, Map<String, String> headerParams);
}
